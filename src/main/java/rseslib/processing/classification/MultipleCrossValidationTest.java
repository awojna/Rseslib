/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
 * 
 *  This file is part of Rseslib.
 *
 *  Rseslib is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rseslib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package rseslib.processing.classification;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Multitest object that performs a number of tests.
 * In each test a given table is tested
 * with the cross-validation test.
 *
 * @author      Arkadiusz Wojna
 */
public class MultipleCrossValidationTest extends Configuration
{
    /** Name of property for number of folds in the cross-validation test. */
    public static final String NO_OF_FOLDS_PROPERTY_NAME = "noOfFolds";
    /** Name of property for the number of tests in this multitest. */
    public static final String NO_OF_TESTS_PROPERTY_NAME = "noOfTests";

    /** Number of tests in this multitest. */
    private final int m_nNoOfTests = getIntProperty(NO_OF_TESTS_PROPERTY_NAME);
    /** The set of classifiers. */
    private ClassifierSet m_Classifiers;
    /** The set of classifiers. */
    private CrossValidationTest m_SingleCrossValidation;

    /**
     * Constructs the multitest for a given classifiers.
     *
     * @param prop              Map between property names and property values.
     * @param classifiers       Multi-classifier for classifiers.
     */
    public MultipleCrossValidationTest(Properties prop, ClassifierSet classifiers) throws PropertyConfigurationException
    {
        super(prop);
        Properties cvprop = Configuration.loadDefaultProperties(CrossValidationTest.class);
        cvprop.setProperty(CrossValidationTest.NO_OF_FOLDS_PROPERTY_NAME, getProperty(NO_OF_FOLDS_PROPERTY_NAME));
        m_Classifiers = classifiers;
        m_SingleCrossValidation = new CrossValidationTest(cvprop, m_Classifiers);
    }

    /**
     * Test the table.
     *
     * @param table Table to be tested.
     * @param prog  Progress object.
     * @return          Map of entries: name of a classifier
     *                  and the object MulitpleTestResult with a classification result.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public Map<String,MultipleTestResult> test(DoubleDataTable table, Progress prog) throws InterruptedException
    {
        prog.set("Multiple cross-validation test", m_nNoOfTests);
        Map<String,double[]> mapOfAccuracyForClassifiers = new HashMap<String,double[]>();
        for (int t = 0; t < m_nNoOfTests; t++)
        {
            // wykonanie testu kroswalidacyjnego
            Map<String,MultipleTestResult> classificationResults = m_SingleCrossValidation.test(table, new EmptyProgress());
            for (Map.Entry<String,MultipleTestResult> clRes : classificationResults.entrySet())
            {
                double[] results = (double[])mapOfAccuracyForClassifiers.get(clRes.getKey());
                if (results==null)
                {
                    results = new double[m_nNoOfTests];
                    mapOfAccuracyForClassifiers.put(clRes.getKey(), results);
                }
                results[t] = clRes.getValue().getAverage();
            }
            prog.step();
        }
        Map<String,MultipleTestResult> multipleCrossValidationTestResults = new HashMap<String,MultipleTestResult>();
        for (Map.Entry<String,double[]> clRes : mapOfAccuracyForClassifiers.entrySet())
        {
            multipleCrossValidationTestResults.put(clRes.getKey(), new MultipleTestResult(clRes.getValue()));
        }
        return multipleCrossValidationTestResults;
    }
}

/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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


package rseslib.processing.classification.parameterised;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import rseslib.processing.classification.ClassifierSet;
import rseslib.processing.classification.MultipleTestResult;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Multitest object that performs a number of tests.
 * In each test a given table is tested
 * with cross-validation test.
 *
 * @author      Arkadiusz Wojna
 */
public class MultipleCrossValidationTestForParameterised extends Configuration
{
    /** Name of property for the number of tests in this multitest. */
    private static final String NO_OF_TESTS_PROPERTY_NAME = "noOfTests";

    /** Number of tests in this multitest. */
    private final int m_nNoOfTests = getIntProperty(NO_OF_TESTS_PROPERTY_NAME);
    /** The set of non-parameterised classifiers. */
    private ClassifierSet m_Classifiers;
    /** The set of parameterised classifiers. */
    private AbstractParameterisedMultiClassifier m_ParameterisedClassifiers;
    /** Single cross-validation test. */
    private CrossValidationTestForParameterised m_SingleCrossValidation;

    /**
     * Constructs the multitest for a given classifiers.
     *
     * @param prop              Map between property names and property values.
     * @param classifiers       Multi-classifier for non-parameterised classifiers.
     * @param paramClassifiers  Multi-classifier for parameterised classifiers.
     */
    public MultipleCrossValidationTestForParameterised(Properties prop, ClassifierSet classifiers, AbstractParameterisedMultiClassifier paramClassifiers) throws PropertyConfigurationException
    {
        super(prop);
        m_Classifiers = classifiers;
        m_ParameterisedClassifiers = paramClassifiers;
        m_SingleCrossValidation = new CrossValidationTestForParameterised(null, m_Classifiers, m_ParameterisedClassifiers);
    }

    /**
     * Test the table.
     *
     * @param table Table to be tested.
     * @param prog  Progress object.
     * @return      Array of maps of results: a classifier name - results for this classifier.
     *              The array length is 2: the position 0 is for the results of non-parameterised classifiers
     *              and the position 1 is for the results of parameterised classifiers.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public Map[] test(DoubleDataTable table, Progress prog) throws InterruptedException
    {
        prog.set("Multitest", m_nNoOfTests);
        Map<String,double[]> mapOfAccuracyForClassifiers = new HashMap<String,double[]>();
        Map<String,double[][]> mapOfAccuracyForParameterisedClassifiers = new HashMap<String,double[][]>();
        Map<String,String> mapOfParameterNamesForParameterisedClassifiers = new HashMap<String,String>();
        for (int t = 0; t < m_nNoOfTests; t++)
        {
            // wykonanie testu kroswalidacyjnego
            Map[] classificationResults = m_SingleCrossValidation.test(table, new EmptyProgress());
            for (Map.Entry<String,MultipleTestResult> clRes : ((Map<String,MultipleTestResult>)classificationResults[0]).entrySet())
            {
                double[] results = (double[])mapOfAccuracyForClassifiers.get(clRes.getKey());
                if (results==null)
                {
                    results = new double[m_nNoOfTests];
                    mapOfAccuracyForClassifiers.put(clRes.getKey(), results);
                }
                results[t] = clRes.getValue().getAvgAccuracy();
            }
            for (Map.Entry<String,ParameterisedMultiTestResult> clRes : ((Map<String,ParameterisedMultiTestResult>)classificationResults[1]).entrySet())
            {
                double[][] results = (double[][])mapOfAccuracyForParameterisedClassifiers.get(clRes.getKey());
                if (results==null)
                {
                    results = new double[m_nNoOfTests][];
                    mapOfAccuracyForParameterisedClassifiers.put(clRes.getKey(), results);
                }
                results[t] = new double[clRes.getValue().parameterRange()];
                for (int parValue = 0; parValue < results[t].length; parValue++)
                    results[t][parValue] = clRes.getValue().getResultForParameterValue(parValue).getAvgAccuracy();
                if (t==0) mapOfParameterNamesForParameterisedClassifiers.put(clRes.getKey(), clRes.getValue().getParameterName());
            }
            prog.step();
        }
        Map<String,MultipleTestResult> resultMap = new HashMap<String,MultipleTestResult>();
        for (Map.Entry<String,double[]> clRes : mapOfAccuracyForClassifiers.entrySet())
        	resultMap.put(clRes.getKey(), new MultipleTestResult(clRes.getValue()));
        Map<String,ParameterisedMultiTestResult> paramResultMap = new HashMap<String,ParameterisedMultiTestResult>();
        for (Map.Entry<String,double[][]> clRes : mapOfAccuracyForParameterisedClassifiers.entrySet())
        	paramResultMap.put(clRes.getKey(), new ParameterisedMultiTestResult(
                (String)mapOfParameterNamesForParameterisedClassifiers.get(clRes.getKey()), clRes.getValue()));
        Map[] crossValidationMultiTestResults = new Map[2];
        crossValidationMultiTestResults[0] = resultMap;
        crossValidationMultiTestResults[1] = paramResultMap;
        return crossValidationMultiTestResults;
    }
}

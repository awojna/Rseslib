/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Multitest object that performs a number of tests.
 * In each test a given table is randomly split
 * into a training and a test table
 * and the classifiers are trained with the generated trainig table
 * and tested with the generated test table.
 *
 * @author      Arkadiusz Wojna
 */
public class MultipleRandomSplitTest extends Configuration
{
    /** Name of property for the number of tests in this multitest. */
    public static final String NO_OF_TESTS_PROPERTY_NAME = "noOfTests";
    /** Name of property for the number of parts for training. */
    public static final String NO_OF_TRAINING_PARTS_PROPERTY_NAME = "noOfPartsForTraining";
    /** Name of property for the number of parts for testing. */
    public static final String NO_OF_TESTINIG_PARTS_PROPERTY_NAME = "noOfPartsForTesting";

    /** Number of tests in this multitest. */
    private final int m_nNoOfTests = getIntProperty(NO_OF_TESTS_PROPERTY_NAME);
    /** Number of parts for training in splitting ratio. */
    private final int m_nNoOfPartsForTraining = getIntProperty(NO_OF_TRAINING_PARTS_PROPERTY_NAME);
    /** Number of parts for testing in splitting ratio. */
    private final int m_nNoOfPartsForTesting = getIntProperty(NO_OF_TESTINIG_PARTS_PROPERTY_NAME);
    /** The set of classifiers. */
    private ClassifierSet m_Classifiers;

    /**
     * Constructs the multitest for a given classifiers.
     *
     * @param prop              Map between property names and property values.
     * @param classifiers       Multi-classifier for classifiers.
     */
    public MultipleRandomSplitTest(Properties prop, ClassifierSet classifiers) throws PropertyConfigurationException
    {
        super(prop);
        m_Classifiers = classifiers;
    }

    /**
     * Test the table.
     *
     * @param table Table to be tested.
     * @param prog              Progress object.
     * @return          Map of entries: name of a classifier
     *                  and the object MulitpleTestResult with a classification result.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public Map<String,MultipleTestResult> test(DoubleDataTable table, Progress prog) throws InterruptedException
    {
        prog.set("Multiple test", 2*m_nNoOfTests);
        Map<String,TestResult[]> mapOfAccuracyForClassifiers = new HashMap<String,TestResult[]>();
        Progress emptyProg = new EmptyProgress();
        for (int t = 0; t < m_nNoOfTests; t++)
        {
            // podzial danych na tabele treningowa i testowa
            ArrayList<DoubleData>[] parts = table.randomSplit(m_nNoOfPartsForTraining, m_nNoOfPartsForTesting);
            DoubleDataTable trnTable = new ArrayListDoubleDataTable(parts[0]);
            DoubleDataTable tstTable = new ArrayListDoubleDataTable(parts[1]);

            // wyuczenie klasyfikatorow
            m_Classifiers.train(trnTable, emptyProg);
            prog.step();

            // klasyfikacja tabeli testowej
            Map<String,TestResult> classificationResults = m_Classifiers.classify(tstTable, emptyProg);
            for (Map.Entry<String,TestResult> clRes : classificationResults.entrySet())
            {
                TestResult[] results = (TestResult[])mapOfAccuracyForClassifiers.get(clRes.getKey());
                if (results==null)
                {
                    results = new TestResult[m_nNoOfTests];
                    mapOfAccuracyForClassifiers.put(clRes.getKey(), results);
                }
                results[t] = clRes.getValue();
            }
            prog.step();
        }
        Map<String,MultipleTestResult> multipleTestResults = new HashMap<String,MultipleTestResult>();
        for (Map.Entry<String,TestResult[]> clRes : mapOfAccuracyForClassifiers.entrySet())
            multipleTestResults.put(clRes.getKey(), new MultipleTestResult(clRes.getValue()));
        return multipleTestResults;
    }
}

/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import rseslib.processing.classification.ClassifierSet;
import rseslib.processing.classification.MultipleTestResult;
import rseslib.processing.classification.TestResult;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Cross-validation object that splits a given test table
 * into a number of folds and performs a cross-validation test.
 *
 * @author      Arkadiusz Wojna
 */
public class CrossValidationTestForParameterised extends Configuration
{
    /** Name of property for number of folds in a cross-validation test. */
    private static final String NO_OF_FOLDS_PROPERTY_NAME = "noOfFolds";

    /** Number of folds in a cross-validation test. */
    private final int m_nNoOfFolds = getIntProperty(NO_OF_FOLDS_PROPERTY_NAME);
    /** The set of non-parameterised classifiers. */
    private ClassifierSet m_Classifiers;
    /** The set of parameterised classifiers. */
    private AbstractParameterisedMultiClassifier m_ParameterisedClassifiers;

    /**
     * Constructs the cross-validation tester for a given classifiers.
     *
     * @param prop              Map between property names and property values.
     * @param classifiers       Multi-classifier for non-parameterised classifiers.
     * @param paramClassifiers  Multi-classifier for parameterised classifiers.
     */
    public CrossValidationTestForParameterised(Properties prop, ClassifierSet classifiers, AbstractParameterisedMultiClassifier paramClassifiers) throws PropertyConfigurationException
    {
        super(prop);
        m_Classifiers = classifiers;
        m_ParameterisedClassifiers = paramClassifiers;
    }

    /**
     * Test the table.
     *
     * @param table Table to be tested.
     * @param prog  Progress object for test process.
     * @return      Array of maps of results: a classifier name - results for this classifier.
     *              The array length is 2: the position 0 is for the results of non-parameterised classifiers
     *              and the position 1 is for the results of parameterised classifiers.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public Map[] test(DoubleDataTable table, Progress prog) throws InterruptedException
    {
        Collection<DoubleData>[] parts =  table.randomPartition(m_nNoOfFolds);
        prog.set("Cross-validation test", parts.length);
        Map<String,TestResult[]> mapOfAccuracyForClassifiers = new HashMap<String,TestResult[]>();
        Map<String,ParameterisedTestResult[]> mapOfAccuracyForParameterisedClassifiers = new HashMap<String,ParameterisedTestResult[]>();
        Progress emptyProg = new EmptyProgress();
        for (int cv = 0; cv < parts.length; cv++)
        {
            // podzial danych na tabele treningowa i testowa
            DoubleDataTable trnTable = new ArrayListDoubleDataTable(table.attributes());
            DoubleDataTable tstTable = new ArrayListDoubleDataTable(table.attributes());
            for (int part = 0; part < parts.length; part++)
            {
                DoubleDataTable tab = trnTable;
                if (part==cv) tab = tstTable;
                for (DoubleData obj : parts[part]) tab.add(obj);
            }

            // wyuczenie klasyfikatorow
            if (m_Classifiers!=null) m_Classifiers.train(trnTable, emptyProg);
            if (m_ParameterisedClassifiers!=null) m_ParameterisedClassifiers.train(trnTable);

            // klasyfikacja tabeli testowej
            if (m_Classifiers!=null)
            {
                Map<String,TestResult> classificationResults = m_Classifiers.classify(tstTable, emptyProg);
                for (Map.Entry<String,TestResult> clRes : classificationResults.entrySet())
                {
                    TestResult[] results = (TestResult[])mapOfAccuracyForClassifiers.get(clRes.getKey());
                    if (results==null)
                    {
                        results = new TestResult[parts.length];
                        mapOfAccuracyForClassifiers.put(clRes.getKey(), results);
                    }
                    results[cv] = clRes.getValue();
                }
            }
            if (m_ParameterisedClassifiers!=null)
            {
                Map<String,ParameterisedTestResult> classificationResults = m_ParameterisedClassifiers.classify(tstTable, emptyProg);
                for (Map.Entry<String,ParameterisedTestResult> clRes : classificationResults.entrySet())
                {
                    ParameterisedTestResult[] results = (ParameterisedTestResult[])mapOfAccuracyForParameterisedClassifiers.get(clRes.getKey());
                    if (results==null)
                    {
                        results = new ParameterisedTestResult[parts.length];
                        mapOfAccuracyForParameterisedClassifiers.put(clRes.getKey(), results);
                    }
                    results[cv] = clRes.getValue();
                }
            }
            prog.step();
        }
        Map<String,MultipleTestResult> resultMap = new HashMap<String,MultipleTestResult>();
        for (Map.Entry<String,TestResult[]> clRes : mapOfAccuracyForClassifiers.entrySet())
        	resultMap.put(clRes.getKey(), new MultipleTestResult(clRes.getValue()));
        Map<String,ParameterisedMultiTestResult> paramResultMap = new HashMap<String,ParameterisedMultiTestResult>();
        for (Map.Entry<String,ParameterisedTestResult[]> clRes : mapOfAccuracyForParameterisedClassifiers.entrySet())
        	paramResultMap.put(clRes.getKey(), new ParameterisedMultiTestResult(clRes.getValue()[0].getParameterName(), clRes.getValue()));
        Map[] crossValidationResults = new Map[2];
        crossValidationResults[0] = resultMap;
        crossValidationResults[1] = paramResultMap;
        return crossValidationResults;
    }
}

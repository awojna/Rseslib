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


package rseslib.processing.classification.parameterised;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.Progress;

/**
 * Multiclassifier comparing the classification results
 * for different parameterised classifiers.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class AbstractParameterisedMultiClassifier extends Configuration
{
    /** Training data set. */
    protected DoubleDataTable m_TrainTable;
    /** Map between classifier names and classifiers. */
    private Map<String,AbstractParameterisedClassifier> m_Classifiers = new HashMap<String,AbstractParameterisedClassifier>();

    /**
     * Constructor.
     *
     * @param prop   Map between property names and property values.
     */
    public AbstractParameterisedMultiClassifier(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
    }

    /**
     * Add a classifier to this set of classifiers.
     *
     * @param name Name of a classifier to be added.
     * @param cl   Classifier to be added.
     */
    public void addClassifier(String name, AbstractParameterisedClassifier cl)
    {
        m_Classifiers.put(name, cl);
    }

    /**
     * Constructs classifiers to be tested.
     *
     * @param trainTable Training data set.
     */
    public abstract void train(DoubleDataTable trainTable);

    /**
     * Returns map of classifier names into classifiers.
     *
     * @return Map of classifier names into classifiers.
     */
    public Map<String,AbstractParameterisedClassifier> getClassifiers()
    {
        return m_Classifiers;
    }

    /**
     * Computes the optimal values for all parameterised classifiers.
     *
     * @param prog       Progress object for optimal parameter value search.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public void learnOptimalParameterValues(Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        NominalAttribute decAttr = m_TrainTable.attributes().nominalDecisionAttribute();
        Map<String,int[][][]> mapOfDistributions = new HashMap<String,int[][][]>();
        prog.set("Learning optimal parameter values", m_TrainTable.noOfObjects());
        for (DoubleData dObj : m_TrainTable.getDataObjects())
        {
            for (Map.Entry<String,AbstractParameterisedClassifier> cl : m_Classifiers.entrySet())
            {
                try
                {
                    double[] decisions = cl.getValue().classifyWithParameter(dObj);
                    int[][][] confusionMatrices = (int[][][])mapOfDistributions.get(cl.getKey());
                    if (confusionMatrices==null)
                    {
                        confusionMatrices = new int[decisions.length][][];
                        for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
                        {
                            confusionMatrices[parVal] = new int[decAttr.noOfValues()][];
                            for (int i = 0; i < confusionMatrices[parVal].length; i++)
                                confusionMatrices[parVal][i] = new int[decAttr.noOfValues()];
                        }
                        mapOfDistributions.put(cl.getKey(), confusionMatrices);
                    }
                    for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
                        confusionMatrices[parVal][decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())][decAttr.localValueCode(decisions[parVal])]++;
                }
                catch (RuntimeException e)
                {
                    Report.exception(e);
                }
            }
            prog.step();
        }
        for (Map.Entry<String,AbstractParameterisedClassifier> cl : m_Classifiers.entrySet())
        {
            cl.getValue().calculateStatistics();
            int[][][] confusionMatrices = (int[][][])mapOfDistributions.get(cl.getKey());
            ParameterisedTestResult results = new ParameterisedTestResult(cl.getValue().getParameterName(), decAttr, m_TrainTable.getDecisionDistribution(), confusionMatrices, cl.getValue().getStatistics());
            int optimalParameterValue = 0;
            for (int parVal = 0; parVal < results.getParameterRange(); parVal++)
                if (results.getClassificationResult(parVal).getAccuracy() > results.getClassificationResult(optimalParameterValue).getAccuracy())
                    optimalParameterValue = parVal;
            (cl.getValue()).setProperty(cl.getValue().getParameterName(), Integer.toString(optimalParameterValue));
        }
    }

    /**
     * Classifies a test data set.
     *
     * @param tstTable  Test data set.
     * @param prog      Progress object for classification process.
     * @return          Map of entries: name of a classifier - classification results.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public Map<String,ParameterisedTestResult> classify(DoubleDataTable tstTable, Progress prog) throws InterruptedException
    {
        // classifying test objects
        NominalAttribute decAttr = tstTable.attributes().nominalDecisionAttribute();
        Map<String,int[][][]> mapOfDistributions = new HashMap<String,int[][][]>();
        prog.set("Classifing test table", tstTable.noOfObjects());
        for (DoubleData dObj : tstTable.getDataObjects())
        {
            for (Map.Entry<String,AbstractParameterisedClassifier> cl : m_Classifiers.entrySet())
            {
                try
                {
                    double[] decisions = cl.getValue().classifyWithParameter(dObj);
                    int[][][] confusionMatrices = (int[][][])mapOfDistributions.get(cl.getKey());
                    if (confusionMatrices==null)
                    {
                        confusionMatrices = new int[decisions.length][][];
                        for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
                        {
                            confusionMatrices[parVal] = new int[decAttr.noOfValues()][];
                            for (int i = 0; i < confusionMatrices[parVal].length; i++)
                                confusionMatrices[parVal][i] = new int[decAttr.noOfValues()];
                        }
                        mapOfDistributions.put(cl.getKey(), confusionMatrices);
                    }
                    for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
                        confusionMatrices[parVal][decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())][decAttr.localValueCode(decisions[parVal])]++;
                }
                catch (RuntimeException e)
                {
                    Report.exception(e);
                }
                catch (PropertyConfigurationException e)
                {
                    Report.exception(e);
                }
            }
            prog.step();
        }
        // preparing final classification results
        Map<String,ParameterisedTestResult> resultMap = new HashMap<String,ParameterisedTestResult>();
        for (Map.Entry<String,AbstractParameterisedClassifier> cl : m_Classifiers.entrySet())
        {
            int[][][] confusionMatrices = (int[][][])mapOfDistributions.get(cl.getKey());
            cl.getValue().calculateStatistics();
            ParameterisedTestResult results = new ParameterisedTestResult(cl.getValue().getParameterName(), decAttr, tstTable.getDecisionDistribution(), confusionMatrices, cl.getValue().getStatistics());
            resultMap.put(cl.getKey(), results);
        }
        return resultMap;
    }
}

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.Progress;

/**
 * Abstract class for parameterised classification methods
 * only with the method classifing a single test data object
 * left to be implemented by subclasses. Non-parameterised
 * method returns decision for a fixed parameter value.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class AbstractParameterisedClassifier extends ConfigurationWithStatistics implements ParameterisedClassifier
{
	/** Parameter name. */
    private String m_ParamName;

    /**
     * Constructor assuming that the default value of the parameter
     * is given in the properties.
     *
     * @param prop                   Map between property names and property values.
     * @param paramName              Parameter name.
     */
    public AbstractParameterisedClassifier(Properties prop, String paramName) throws PropertyConfigurationException
    {
        super(prop);
        m_ParamName = paramName;
    }

    /**
     * Constructor used when loadind the object from a file.
     */
    public AbstractParameterisedClassifier()
    {
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void writeAbstractParameterisedClassifier(ObjectOutputStream out) throws IOException
    {
    	writeConfigurationAndStatistics(out);
    	out.writeObject(m_ParamName);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void readAbstractParameterisedClassifier(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readConfigurationAndStatistics(in);
    	m_ParamName = (String)in.readObject();
    }

    /**
     * Return name of the classifier parameter.
     *
     * @return Name of the classifier parameter.
     */
    public String getParameterName()
    {
        return m_ParamName;
    }

    /**
     * Learns the optimal value of the parameter.
     *
     * @param trainTable Training data table.
     * @param prog       Progress object for optimal parameter value search.
     * @return           Optimal value of the parameter.
     * @throws InterruptedException when the user interrupts the execution.
     */
    protected void learnOptimalParameterValue(DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        int[][][] confusionMatrices = null;
        NominalAttribute decAttr = trainTable.attributes().nominalDecisionAttribute();
        prog.set("Learning optimal parameter value", trainTable.noOfObjects());
        for (DoubleData dObj : trainTable.getDataObjects())
        {
            try
            {
                double[] decisions = classifyWithParameter(dObj);
                if (confusionMatrices==null)
                {
                    confusionMatrices = new int[decisions.length][][];
                    for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
                    {
                        confusionMatrices[parVal] = new int[decAttr.noOfValues()][];
                        for (int i = 0; i < confusionMatrices[parVal].length; i++)
                            confusionMatrices[parVal][i] = new int[decAttr.noOfValues()];
                    }
                }
                for (int parVal = 1; parVal < confusionMatrices.length; parVal++)
                    confusionMatrices[parVal][decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())][decAttr.localValueCode(decisions[parVal])]++;
            }
            catch (RuntimeException e)
            {
                Report.exception(e);
            }
            prog.step();
        }
        ParameterisedTestResult results = new ParameterisedTestResult(getParameterName(), decAttr, trainTable.getDecisionDistribution(), confusionMatrices, new Properties());
        int bestParamValue = 0;
        for (int parVal = 1; parVal < results.getParameterRange(); parVal++)
            if (results.getClassificationResult(parVal).getAccuracy() > results.getClassificationResult(bestParamValue).getAccuracy())
                bestParamValue = parVal;
        makePropertyModifiable(m_ParamName);
        setProperty(m_ParamName, Integer.toString(bestParamValue));
    }
}

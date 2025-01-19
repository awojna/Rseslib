/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;

/**
 * Abstract class for classifiers
 * that provide both single and distributed decision.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class AbstractClassifierWithDistributedDecision extends ConfigurationWithStatistics implements ClassifierWithDistributedDecision, Classifier
{
	/** Decision attribute. */
	private NominalAttribute m_DecisionAttribute;
	/** Distribution of decision values in the whole training set. */
	private int[] trainDecDistr;

    /**
     * Constructor.
     *
     * @param prop     Map between property names and property values.
     * @param decAttr  Decision attribute information.
     * @param trainTab Training table.
     * @throws PropertyConfigurationException If an I/O error occurs while reading properties.
     */
    public AbstractClassifierWithDistributedDecision(Properties prop, DoubleDataTable trainTab) throws PropertyConfigurationException
    {
        super(prop);
        m_DecisionAttribute = trainTab.attributes().nominalDecisionAttribute();
        trainDecDistr = trainTab.getDecisionDistribution();
    }

    /**
     * Constructor used when loading the object from a file.
     */
    protected AbstractClassifierWithDistributedDecision()
    {
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occurred.
     */
    protected void writeAbstractClassifier(ObjectOutputStream out) throws IOException
    {
    	writeConfigurationAndStatistics(out);
    	out.writeObject(m_DecisionAttribute);
    	out.writeObject(trainDecDistr);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occurred.
     */
    protected void readAbstractClassifier(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readConfigurationAndStatistics(in);
    	m_DecisionAttribute = (NominalAttribute)in.readObject();
    	trainDecDistr = (int[])in.readObject();
    }

    /**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
     */
    public double classify(DoubleData dObj) throws PropertyConfigurationException
    {
        double[] decDistr = classifyWithDistributedDecision(dObj);
        int bestDec = 0;
        for (int dec = 1; dec < decDistr.length; dec++)
            if (decDistr[dec] > decDistr[bestDec] || (decDistr[dec] == decDistr[bestDec] && trainDecDistr[dec] > trainDecDistr[bestDec]))
            	bestDec = dec;
        return m_DecisionAttribute.globalValueCode(bestDec);
    }
}

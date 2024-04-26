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


package rseslib.processing.classification.bayes;

import java.util.Properties;

import rseslib.processing.classification.Classifier;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.probability.DecisionDependentProbability;
import rseslib.structure.probability.KernelNumericDecisionDependentProbability;
import rseslib.structure.probability.MEstimateNominalDecisionDependentProbability;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Naive bayesian classifier.
 *
 * @author Lukasz Ligowski
 */

public class NaiveBayesClassifier extends ConfigurationWithStatistics implements Classifier
{
	/** Attributes. */
	private Header m_Attributes;
	/** Unconditional probability of decisions. */
	private double[] m_TotalDecProbability;
	/** Array of conditional value probabilities for particular attributes. */
	private DecisionDependentProbability[] m_Provider;

	/**
	 * Constructor learns the naive Bayes classifier.
	 *
	 * @param prop Properties.
	 * @param table Data table for classification.
     * @param prog Progress indicator.
	 */
	public NaiveBayesClassifier(Properties prop, DoubleDataTable table, Progress prog)  throws PropertyConfigurationException, InterruptedException
	{
		super(prop);
		m_Attributes = table.attributes();
		prog.set("Learning naive bayesian classifier", m_Attributes.noOfAttr());
		int[] decDistr = table.getDecisionDistribution();
		m_TotalDecProbability = new double[decDistr.length];
		int sum = 0;
		for (int dec = 0; dec < m_TotalDecProbability.length; dec++)
		{
			m_TotalDecProbability[dec] = decDistr[dec];
			sum += decDistr[dec];
		}
		for (int dec = 0; dec < m_TotalDecProbability.length; dec++)
			m_TotalDecProbability[dec] /= sum;
		m_Provider = new DecisionDependentProbability[m_Attributes.noOfAttr()];
		for (int i=0; i<table.attributes().noOfAttr(); i++)
		{
			if (m_Attributes.isConditional(i))
				if (m_Attributes.isNominal(i))
					m_Provider[i] = new MEstimateNominalDecisionDependentProbability(getProperties(), table, i);
				else if (m_Attributes.isNumeric(i))
					m_Provider[i] = new KernelNumericDecisionDependentProbability(getProperties(), table, i);
			prog.step();
		}
	}

	/**
	 * Classifier.
	 *
	 * @param dObj Data object to classify
	 */
	public double classify(DoubleData dObj)
	{
		// wyliczenie prawdopodobienstw
		double[] prob = m_TotalDecProbability.clone();
		for (int att=0; att<m_Provider.length; att++)
			if (m_Attributes.isConditional(att))
				for (int dec = 0; dec < m_TotalDecProbability.length; dec++)
					prob[dec] *= m_Provider[att].getProbability(dObj.get(att), dec);

		//wybor decyzji o najwiekszym prawdopodobienstwie
		int iMax=0;
		double ArgMax=0;
		for (int dec=0; dec<prob.length; dec++)
		{
			if (ArgMax<prob[dec])
			{
				iMax=dec;
				ArgMax=prob[dec];
			}
		}
		return m_Attributes.nominalDecisionAttribute().globalValueCode(iMax);
	}

	/**
	 *  Calculates statistics.
	 */
	public void calculateStatistics() {
	}

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

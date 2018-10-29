/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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


package rseslib.structure.probability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.histogram.Histogram;
import rseslib.structure.histogram.NominalAttributeHistogram;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;

/**
 * Conditional probability estimation
 * of nominal attribute values
 * given a decision value.
 * It uses value distribution in decision classes
 * to estimate value probability. 
 *
 * @author Lukasz Ligowski
 */
public class MEstimateNominalDecisionDependentProbability extends Configuration implements DecisionDependentProbability
{
	/** Name of property for getting out m value.*/
	private static final String M_ESTIMATION_PARAMETER_PROPERTY_NAME="mEstimateParameter";

	/** The information about the attribute. */
	private NominalAttribute m_Attribute;
	/** Histograms of attribute values in particual decision classes. */
	private Histogram[] m_DecClassHistograms;
	/** m-estimation parameter.*/
	private int m_estimate;

	/**
	 * Computes distribution of values of the attribute among the decisions.
	 *
	 * @param prop Properties.
	 * @param table Set of objects provided for distribution extraction.
	 * @param index Number of attribute for distribution extraction.
	 */
	public MEstimateNominalDecisionDependentProbability(Properties prop, DoubleDataTable table, int index) throws PropertyConfigurationException
    {
		super(prop);
		m_Attribute = (NominalAttribute)table.attributes().attribute(index);
		NominalAttribute decAttr = table.attributes().nominalDecisionAttribute();
		m_DecClassHistograms = new Histogram[decAttr.noOfValues()];
		Collection<DoubleData>[] decClasses = new Collection[decAttr.noOfValues()];
		for (int dec = 0; dec < decClasses.length; dec++)
			decClasses[dec] = new ArrayList<DoubleData>();
		for (DoubleData obj : table.getDataObjects())
			decClasses[decAttr.localValueCode(((DoubleDataWithDecision)obj).getDecision())].add(obj);
		for (int dec = 0; dec < decClasses.length; dec++)
			m_DecClassHistograms[dec] = new NominalAttributeHistogram(decClasses[dec], index, m_Attribute);
		m_estimate=getIntProperty(M_ESTIMATION_PARAMETER_PROPERTY_NAME);
	}

	/**
	 * Estimation of the conditional probability of a value
	 * given a decision.
	 *
	 * @param value 	Value.
	 * @param decision 	Local code of a decision.
	 * @return 			Conditional probability of the value.
	 */
	public double getProbability(double value, int decision)
	{
		if (Double.isNaN(value)) return 0;
		int localValue = m_Attribute.localValueCode(value);
		if (localValue>=0)
			if (localValue < m_DecClassHistograms[decision].size())
				return (m_DecClassHistograms[decision].amount(localValue)+((double)m_estimate)/m_Attribute.noOfValues())/(m_DecClassHistograms[decision].totalAmount()+m_estimate);
			else
				return ((double)m_estimate)/(m_Attribute.noOfValues()*(m_DecClassHistograms[decision].totalAmount()+m_estimate));
		return 0;
	}

	/**
	 * Returns string representation of array of attribute values distribution.
	 *
	 * @return String representation of array of attribute values distribution.
	 */
	public String toString()
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Ilosc decyzji: "+m_DecClassHistograms.length+", Ilosc wartosci atrybutu: "+m_DecClassHistograms[0].size()+Report.lineSeparator);
		for (int i=0;i<m_DecClassHistograms.length;i++)
		{
			for (int j=0;j<m_DecClassHistograms[i].size();j++)
				sbuf.append(m_DecClassHistograms[i].amount(j)+" ");
			sbuf.append(Report.lineSeparator);
		}
		return sbuf.toString();
	}
}

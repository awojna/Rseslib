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


package rseslib.structure.probability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.histogram.Histogram;
import rseslib.structure.histogram.NumericalAttributeHistogram;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;


/**
 * Conditional probability estimation
 * of numerical attribute values
 * given a decision value.
 * Probability is estimated
 * by the application of the kernel-based method
 * in the definition of the probability density function.
 * A set of different kernel function is available.
 *
 * @author Lukasz Ligowski
 */
public class KernelNumericDecisionDependentProbability extends Configuration implements DecisionDependentProbability
{
	/** Types of kernel funcion. */
	private enum KernelType { gaussian, hypercube };
	
	/** Name of property providing smoothness factor. */
	private static final String SMOOTHNESS_PROPERTY_NAME = "smoothness";
	/** Name of the kernel property. */
	private static final String KERNEL_PROPERTY_NAME = "kernel";
	
	/** Histograms of attribute values in particual decision classes. */
	private Histogram[] m_DecClassHistograms;
	/** Kernel used to computations */
	private KernelType kernel;
	/** Value combined from smoothness parameter and some other kernel
	 * function dependent values. */
	private double abs_param_1,abs_param_2;

	/**
	 * Computes distribution of known values of attribute. Reads
	 * necessary values from prop argument.
	 *
	 * @param prop Properties.
	 * @param table Data objects.
	 * @param index Number of attribute to process.
	 */
	public KernelNumericDecisionDependentProbability(Properties prop, DoubleDataTable table, int index) throws PropertyConfigurationException
    {
		super(prop);
		NominalAttribute decAttr = table.attributes().nominalDecisionAttribute();
		m_DecClassHistograms = new Histogram[decAttr.noOfValues()];
		Collection<DoubleData>[] decClasses = new Collection[decAttr.noOfValues()];
		for (int dec = 0; dec < decClasses.length; dec++)
			decClasses[dec] = new ArrayList<DoubleData>();
		for (DoubleData obj : table.getDataObjects())
			decClasses[decAttr.localValueCode(((DoubleDataWithDecision)obj).getDecision())].add(obj);
		for (int dec = 0; dec < decClasses.length; dec++)
			m_DecClassHistograms[dec] = new NumericalAttributeHistogram(decClasses[dec], index);
        try
        {
        	kernel = KernelType.valueOf(getProperty(KERNEL_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown kernel function: "+getProperty(KERNEL_PROPERTY_NAME));
        }
        switch (kernel)
        {
        	case gaussian:
        		abs_param_1 = Math.sqrt(2*Math.PI)*getDoubleProperty(SMOOTHNESS_PROPERTY_NAME);
        		abs_param_2 = 2*getDoubleProperty(SMOOTHNESS_PROPERTY_NAME)*getDoubleProperty(SMOOTHNESS_PROPERTY_NAME);
        		break;
        	case hypercube:
        		abs_param_1 = getDoubleProperty(SMOOTHNESS_PROPERTY_NAME);
        		break;
        }
	}

	/**
	 * Estimation of the conditional probability of a value
	 * given a decision.
	 * Depending on kernel value
	 * it selects a different kernel function
	 * to define the probability density function:
	 * kernel = 0 => radial symmetric multivariate Gaussian kernel;
	 * kernel = 1 => cube
	 *
	 * @param value 	Value.
	 * @param decision 	Local code of a decision.
	 * @return 			Conditional probability of the value.
	 */
	public double getProbability(double value,int decision)
	{
		double sum = 0;
		if (!Double.isNaN(value))
			switch (kernel)
			{
				case gaussian:
					for (int i=0; i<m_DecClassHistograms[decision].size(); i++)
					{
						double diff = value-m_DecClassHistograms[decision].value(i); 
						sum += m_DecClassHistograms[decision].amount(i)*Math.exp(-diff*diff/abs_param_2);
					}
					sum /= m_DecClassHistograms[decision].totalAmount()*abs_param_1;
					break;
				case hypercube:
					for (int i=0; i<m_DecClassHistograms[decision].size(); i++)
						if (Math.abs(value-m_DecClassHistograms[decision].value(i))/abs_param_1<0.5)
							sum++;
					sum /= m_DecClassHistograms[decision].totalAmount()*abs_param_1;
					break;
			}
		return sum;
	}
}

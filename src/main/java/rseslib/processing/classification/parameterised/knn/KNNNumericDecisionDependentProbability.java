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


package rseslib.processing.classification.parameterised.knn;

import java.util.Properties;

import rseslib.processing.metrics.MetricFactory;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.probability.DecisionDependentProbability;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;

/**
 * @author Lukasz Ligowski
 */
public class KNNNumericDecisionDependentProbability extends Configuration implements DecisionDependentProbability
{
	private ArrayHeader newHeader;
	private KNearestNeighbors m_Classifier;

	/**
	 * @param prop
	 * @param table
	 * @param index
	 */
	public KNNNumericDecisionDependentProbability(Properties prop,DoubleDataTable table,int index) throws PropertyConfigurationException, InterruptedException
	{
		super(prop);
		Attribute[] newAttrList = new Attribute[2];
		newAttrList[0]=table.attributes().attribute(index);
		newAttrList[1]=table.attributes().nominalDecisionAttribute();
		newHeader = new ArrayHeader(newAttrList, table.attributes().missing());
		DoubleDataObject container;
		ArrayListDoubleDataTable transformedTable = new ArrayListDoubleDataTable(newHeader);
        for (DoubleData temp : table.getDataObjects())
        {
			container = new DoubleDataObject(newHeader);
			container.set(0,temp.get(index));
			container.setDecision(((DoubleDataWithDecision)temp).getDecision());
			transformedTable.add(container);
		}
        Properties knnProp = Configuration.loadDefaultProperties(KNearestNeighbors.class);
        knnProp.setProperty(MetricFactory.METRIC_PROPERTY_NAME, getProperty(MetricFactory.METRIC_PROPERTY_NAME));
        knnProp.setProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME, getProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME));
        knnProp.setProperty(KNearestNeighbors.WEIGHTING_METHOD_PROPERTY_NAME, getProperty(KNearestNeighbors.WEIGHTING_METHOD_PROPERTY_NAME));
        knnProp.setProperty(KNearestNeighbors.LEARN_OPTIMAL_K_PROPERTY_NAME, getProperty(KNearestNeighbors.LEARN_OPTIMAL_K_PROPERTY_NAME));
        knnProp.setProperty(KNearestNeighbors.MAXIMAL_K_PROPERTY_NAME, getProperty(KNearestNeighbors.MAXIMAL_K_PROPERTY_NAME));
        knnProp.setProperty(KNearestNeighbors.K_PROPERTY_NAME, getProperty(KNearestNeighbors.K_PROPERTY_NAME));
        knnProp.setProperty(KNearestNeighbors.FILTER_NEIGHBOURS_PROPERTY_NAME, getProperty(KNearestNeighbors.FILTER_NEIGHBOURS_PROPERTY_NAME));
        knnProp.setProperty(KNearestNeighbors.VOTING_PROPERTY_NAME, getProperty(KNearestNeighbors.VOTING_PROPERTY_NAME));
		m_Classifier = new KNearestNeighbors(knnProp,transformedTable,new EmptyProgress());
	}

	public double getProbability(double value, int decision)
	{
		DoubleDataObject container = new DoubleDataObject(newHeader);
		container.set(0,value);
		try
		{
			double[] decDistr = m_Classifier.classifyWithDistributedDecision(container);
			double sum = 0;
			for (int dec = 0; dec < decDistr.length; dec++)
				sum += decDistr[dec];
	        return decDistr[decision]/sum;
		}
		catch (PropertyConfigurationException e)
		{
			return 0;
		}
	}

}

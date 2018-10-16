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
 * @author �ukasz Ligowski
 */
public class KnnNumericDecisionDependentProbability extends Configuration implements DecisionDependentProbability
{
	private ArrayHeader newHeader;
	private KnnClassifier m_Classifier;

	/**
	 * @param prop
	 * @param table
	 * @param index
	 */
	public KnnNumericDecisionDependentProbability(Properties prop,DoubleDataTable table,int index) throws PropertyConfigurationException, InterruptedException
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
        Properties knnProp = Configuration.loadDefaultProperties(KnnClassifier.class);
        knnProp.setProperty(MetricFactory.METRIC_PROPERTY_NAME, getProperty(MetricFactory.METRIC_PROPERTY_NAME));
        knnProp.setProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME, getProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME));
        knnProp.setProperty(KnnClassifier.WEIGHTING_METHOD_PROPERTY_NAME, getProperty(KnnClassifier.WEIGHTING_METHOD_PROPERTY_NAME));
        knnProp.setProperty(KnnClassifier.LEARN_OPTIMAL_K_PROPERTY_NAME, getProperty(KnnClassifier.LEARN_OPTIMAL_K_PROPERTY_NAME));
        knnProp.setProperty(KnnClassifier.MAXIMAL_K_PROPERTY_NAME, getProperty(KnnClassifier.MAXIMAL_K_PROPERTY_NAME));
        knnProp.setProperty(KnnClassifier.K_PROPERTY_NAME, getProperty(KnnClassifier.K_PROPERTY_NAME));
        knnProp.setProperty(KnnClassifier.FILTER_NEIGHBOURS_PROPERTY_NAME, getProperty(KnnClassifier.FILTER_NEIGHBOURS_PROPERTY_NAME));
        knnProp.setProperty(KnnClassifier.VOTING_PROPERTY_NAME, getProperty(KnnClassifier.VOTING_PROPERTY_NAME));
		m_Classifier = new KnnClassifier(knnProp,transformedTable,new EmptyProgress());
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

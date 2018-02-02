/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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

package rseslib.processing.classification.meta;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;

import rseslib.processing.classification.Classifier;
import rseslib.processing.classification.ClassifierFactory;
import rseslib.processing.filtering.Sampler;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * 
 * @author Sebastian Stawicki
 *
 */
public class Bagging extends ConfigurationWithStatistics implements Classifier {

	private static final String propertyBaggingWeakClassifiersClass = "baggingWeakClassifiersClass";
	private static final String propertyBaggingNumberOfIterations = "baggingNumberOfIterations";	
	private static final String propertyBaggingUseWeakClassifiersDefaultProperties = "baggingUseWeakClassifiersDefaultProperties";
	
	private ArrayList<Classifier> classifiersEnsemble = new ArrayList<Classifier>();
	
	/** Decision attribute */
	private NominalAttribute nominalDecisionAttribute = null;
	
    //TODO STAWICKI uzupe�ni� opis javadoc, doda� komunikaty dla rzucanych wyj�tk�w
	public Bagging(Properties prop, DoubleDataTable trainTable, Progress prog) 
		throws PropertyConfigurationException, InterruptedException, ClassNotFoundException, 
			IllegalArgumentException, SecurityException, InstantiationException, 
				IllegalAccessException, InvocationTargetException, NoSuchMethodException { 

		super(prop);
		
		if (trainTable.attributes().attribute(trainTable.attributes().decision()).isNominal())
			nominalDecisionAttribute = trainTable.attributes().nominalDecisionAttribute();

		boolean useWeakClassifiersDefaultProperties = getBoolProperty(propertyBaggingUseWeakClassifiersDefaultProperties); 
		Properties classifiersProperties = useWeakClassifiersDefaultProperties ? null : getProperties();
		int numberOfIterations = getIntProperty(propertyBaggingNumberOfIterations);
		if (numberOfIterations <= 0)
			throw new IllegalArgumentException();
		Class weakClassifiersClass = Class.forName(getProperty(propertyBaggingWeakClassifiersClass));
		
		String statement = "Bagging algorithm - creating ensemble of classifiers [";
		statement += weakClassifiersClass.getName();
		statement += "] from training table";
		prog.set(statement, numberOfIterations);
		Progress emptyProgress = new EmptyProgress();
		for (int i=0; i<numberOfIterations; i++) {
			ArrayList<DoubleData> trainSampleArrayList = Sampler.selectWithRepetitions(trainTable.getDataObjects(), trainTable.noOfObjects());
			DoubleDataTable trainSample = new ArrayListDoubleDataTable(trainSampleArrayList);
			Classifier classifier = ClassifierFactory.createClassifier(weakClassifiersClass, classifiersProperties, trainSample, emptyProgress);
			classifiersEnsemble.add(classifier);
			prog.step();
		}
	}
	
	/**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
	 * @throws PropertyConfigurationException 
     */
	public double classify(DoubleData obj) throws PropertyConfigurationException {
		if (nominalDecisionAttribute != null)
			return classifyNominal(obj, nominalDecisionAttribute);
		else 
			return classifyNumeric(obj);
	}
	
	protected double classifyNominal(DoubleData obj, NominalAttribute nominalDecisionAttribute) throws PropertyConfigurationException {
		double[] ensembleDecision = new double[nominalDecisionAttribute.noOfValues()];
		int best = 0;
		for (Classifier classifier : classifiersEnsemble) {
			int dec = nominalDecisionAttribute.localValueCode(classifier.classify(obj));
			if (dec == -1)
				continue;
			ensembleDecision[dec]++;
			if (ensembleDecision[dec] > ensembleDecision[best])
				best = dec;
		}
		return nominalDecisionAttribute.globalValueCode(best);
	}
	
	protected double classifyNumeric(DoubleData dObj) throws PropertyConfigurationException {
		double sum = 0;
		for (Classifier classifier : classifiersEnsemble) 
			sum += classifier.classify(dObj);
		return (sum / classifiersEnsemble.size());
	}
	
	/**
	 * Calculates statistics.
	 */
	public void calculateStatistics() {
	}
	
	/**
	 * Resets statistic.
	 */
	public void resetStatistics() {
	}
}

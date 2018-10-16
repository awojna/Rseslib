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
 * @author Sebastian Stawicki
 *
 */
public class AdaBoost extends ConfigurationWithStatistics implements Classifier {

	private static final String propertyAdaBoostWeakClassifiersClass = "adaBoostWeakClassifiersClass";
	private static final String propertyAdaBoostNumberOfIterations = "adaBoostNumberOfIterations";	
	private static final String propertyAdaBoostUseWeakClassifiersDefaultProperties = "adaBoostUseWeakClassifiersDefaultProperties";

	private ArrayList<Classifier> classifiersEnsemble = new ArrayList<Classifier>(); 
	private ArrayList<Double> classifiersWeights = new ArrayList<Double>();
	
	private ArrayList<DoubleData> trainTableArrayList;

	/** Decision attribute */
	private int decisionAttributeIndex;
	private NominalAttribute nominalDecisionAttribute = null;
	
    //TODO STAWICKI uzupe�ni� opis javadoc, doda� komunikaty dla rzucanych wyj�tk�w, doda� obs�ug� decyzji numerycznych
	public AdaBoost(Properties prop, DoubleDataTable trainTable, Progress prog) 
		throws PropertyConfigurationException, InterruptedException, ClassNotFoundException, 
			IllegalArgumentException, SecurityException, InstantiationException, 
				IllegalAccessException, InvocationTargetException, NoSuchMethodException { 

		super(prop);

		if (!trainTable.attributes().attribute(trainTable.attributes().decision()).isNominal())
			throw new IllegalArgumentException();
		
		trainTableArrayList = trainTable.getDataObjects();
		decisionAttributeIndex = trainTable.attributes().decision();
		if (trainTable.attributes().attribute(decisionAttributeIndex).isNominal())
			nominalDecisionAttribute = trainTable.attributes().nominalDecisionAttribute();
		
		boolean useWeakClassifiersDefaultProperties = getBoolProperty(propertyAdaBoostUseWeakClassifiersDefaultProperties); 
		Properties classifiersProperties = useWeakClassifiersDefaultProperties ? null : getProperties();
		int numberOfIterations = getIntProperty(propertyAdaBoostNumberOfIterations);
		if (numberOfIterations <= 0)
			throw new IllegalArgumentException();
		Class weakClassifiersClass = Class.forName(getProperty(propertyAdaBoostWeakClassifiersClass));
		
		String statement = "AdaBoost algorithm - creating ensemble of classifiers [";
		statement += weakClassifiersClass.getName();
		statement += "] from training table";
		prog.set(statement, numberOfIterations);
		Progress emptyProgress = new EmptyProgress();
		ArrayList<Double> distribution = new ArrayList<Double>();
		for (int i=0; i<trainTable.noOfObjects(); i++)
			distribution.add(1.0/trainTable.noOfObjects());
		
		for (int i=0; i<numberOfIterations; i++) {
			ArrayList<DoubleData> trainSampleArrayList = 
				Sampler.selectWithRepetitionsFromSamplesWithDistribution(trainTableArrayList, distribution, trainTable.noOfObjects());
			DoubleDataTable trainSample = new ArrayListDoubleDataTable(trainSampleArrayList);
			Classifier classifier = ClassifierFactory.createClassifier(weakClassifiersClass, classifiersProperties, trainSample, emptyProgress);
			double epsilon = calculateEpsilon(classifier, distribution);
			if (epsilon >= 0.5) {
				//TODO STAWICKI doda� lepsz� obs�ug� komunikatu
				System.out.println("Error greater than 0.5 - Stop.");
				for (int j=i; j<numberOfIterations; j++)
					prog.step();
				return;
			}			
			classifiersEnsemble.add(classifier);
			double alpha = calculateAlpha(epsilon);
			classifiersWeights.add(alpha);
			distribution = newDistribution(classifier, distribution, alpha);
			prog.step();
		}
		
	}

	private double calculateEpsilon(Classifier classifier, ArrayList<Double> distribution) throws PropertyConfigurationException {
		double error = 0;
		for (int i=0; i<trainTableArrayList.size(); i++) {
			DoubleData obj = trainTableArrayList.get(i);
			if (obj.get(decisionAttributeIndex) != classifier.classify(obj))
				error += distribution.get(i);
		}
		return error;
	}
	

	
	private double calculateAlpha(double epsilon) {
		 return 0.5 * Math.log((1-epsilon)/epsilon);
	}

	
	private ArrayList<Double> newDistribution(Classifier classifier, ArrayList<Double> distribution, double alpha) throws PropertyConfigurationException {
		ArrayList<Double> newDistribution = new ArrayList<Double>();
		double sum = 0;
		for (int i=0; i<distribution.size(); i++) {
			DoubleData obj = trainTableArrayList.get(i);
			double value = distribution.get(i);
			double factor = Math.exp(-alpha);
			if (obj.get(decisionAttributeIndex) == classifier.classify(obj))
				value *= factor;
			else
				value /= factor;
			newDistribution.add(value);
			sum += value;
		}
		for (int i=0; i<newDistribution.size(); i++) {
			double value = newDistribution.get(i);
			newDistribution.set(i, value/sum);
		}
		return newDistribution;
	}
	
	/**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
	 * @throws PropertyConfigurationException 
     */
	public double classify(DoubleData obj) throws PropertyConfigurationException {
		return classifyNominal(obj);
//		//TODO STAWICKI doda� oobs�ug� decyzji numerycznych
//		if (nominalDecisionAttribute != null)
//			return classifyNominal(obj);
//		else 
//			return classifyNumeric(obj);
	}
	
	protected double classifyNominal(DoubleData obj) throws PropertyConfigurationException {
		double[] ensembleDecision = new double[nominalDecisionAttribute.noOfValues()];
		int best = 0;
		for (int i=0; i<classifiersEnsemble.size(); i++) {
			int dec = nominalDecisionAttribute.localValueCode(classifiersEnsemble.get(i).classify(obj));
			if (dec == -1)
				continue;
			ensembleDecision[dec] += classifiersWeights.get(i);
			if (ensembleDecision[dec] > ensembleDecision[best])
				best = dec;
		}
		return nominalDecisionAttribute.globalValueCode(best);
	}
	
//	//TODO STAWICKI doda� oobs�ug� decyzji numerycznych
//	//TODO STAWICKI przypatrze� si� przypadkowi gdy sumOFWeights == 0
//	protected double classifyNumeric(DoubleData obj) throws PropertyConfigurationException {
//		double weightedSum = 0;
//		double sumOfWeights = 0;
//		for (int i=0; i<classifiersEnsemble.size(); i++) {
//			double weight = classifiersWeights.get(i);
//			weightedSum += weight * classifiersEnsemble.get(i).classify(obj);
//			sumOfWeights += weight;
//		}
//		return (weightedSum / sumOfWeights);
//	}
	
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

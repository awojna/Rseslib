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


package rseslib.example;

import java.util.Properties;

import rseslib.processing.classification.ClassifierSet;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * An examplary set of classifiers.
 *
 * @author      Arkadiusz Wojna
 */
public class RseslibClassifiers extends Configuration
{
    /** Name of tree classifier. */
    public static final String TREE_CLASSIFIER_NAME = "treeClassifier";
    /** Name of rule classifier. */
    public static final String RULE_CLASSIFIER_NAME = "ruleClassifier";
    /** Name of rough set classifier. */
    public static final String ROUGH_SET_CLASSIFIER_NAME = "roughSetClassifier";
    /** Name of neural network classifier. */
    public static final String NEURAL_NET_CLASSIFIER_NAME = "neuralNetClassifier";
    /** Name of knn classifier. */
    public static final String KNN_CLASSIFIER_NAME = "knnClassifier";
    /** Name of knn classifier. */
    public static final String LOCAL_KNN_CLASSIFIER_NAME = "localKnnClassifier";
    /** Name of naive bayes classifier. */
    public static final String NAIVE_BAYES_CLASSIFIER_NAME = "naiveBayesClassifier";
    /** Name of svm classifier. */
    public static final String SVM_CLASSIFIER_NAME = "svmClassifier";
    /** Name of pca classifier. */
    public static final String PCA_CLASSIFIER_NAME = "pcaClassifier";
    /** Name of local pca classifier. */
    public static final String LOCAL_PCA_CLASSIFIER_NAME = "localPcaClassifier";

    /** Set of classifier instances to be tested. */
    private ClassifierSet m_MultiClassifier = new ClassifierSet();
    
    /**
     * Constructor.
     *
     * @param prop            Properties of this classifier set.
     */
	public RseslibClassifiers(Properties prop) throws PropertyConfigurationException
	{
		super(prop);
        if (getBoolProperty(TREE_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("C4.5 classifier", rseslib.processing.classification.tree.c45.C45.class);
        if (getBoolProperty(RULE_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("AQ15 classifier", rseslib.processing.classification.rules.AQ15Classifier.class);
        if (getBoolProperty(ROUGH_SET_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Rough set classifier", rseslib.processing.classification.rules.roughset.RoughSetRuleClassifier.class);
        if (getBoolProperty(NEURAL_NET_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Neural network classifier", rseslib.processing.classification.neural.NeuronNetwork.class);
        if (getBoolProperty(KNN_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Knn classifier", rseslib.processing.classification.parameterised.knn.KnnClassifier.class);
        if (getBoolProperty(LOCAL_KNN_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Local knn classifier", rseslib.processing.classification.parameterised.knn.LocalKnnClassifier.class);
        if (getBoolProperty(NAIVE_BAYES_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Naive bayes classifier", rseslib.processing.classification.bayes.NaiveBayesClassifier.class);
        if (getBoolProperty(SVM_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Svm classifier", rseslib.processing.classification.svm.SVM.class);
        if (getBoolProperty(PCA_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Pca classifier", rseslib.processing.classification.parameterised.pca.PcaClassifier.class);
        if (getBoolProperty(LOCAL_PCA_CLASSIFIER_NAME))
        	m_MultiClassifier.addClassifier("Local pca classifier", rseslib.processing.classification.parameterised.pca.LocalPcaClassifier.class);
	}

	/**
	 * Return the set of classifier instances
	 * for all classifier types available in rseslib.
	 * 
	 * @return Set of classifier instances.
	 */
	public ClassifierSet getClassifierSet()
	{
		return m_MultiClassifier;
	}
}

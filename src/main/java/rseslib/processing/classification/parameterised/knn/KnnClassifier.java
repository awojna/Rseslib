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


package rseslib.processing.classification.parameterised.knn;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import rseslib.processing.classification.ClassifierWithDistributedDecision;
import rseslib.processing.classification.parameterised.AbstractParameterisedClassifier;
import rseslib.processing.classification.parameterised.ParameterisedTestResult;
import rseslib.processing.indexing.metric.TreeIndexer;
import rseslib.processing.metrics.MetricFactory;
import rseslib.processing.searching.metric.ArrayVicinityProvider;
import rseslib.processing.searching.metric.IndexingTreeVicinityProvider;
import rseslib.processing.searching.metric.VicinityProvider;
import rseslib.processing.transformation.AttributeTransformer;
import rseslib.processing.transformation.TableTransformer;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.metric.AbstractWeightedMetric;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.MultiProgress;
import rseslib.system.progress.Progress;

/**
 * K nearest neighbors classifier.
 * It induces a metric from a training set
 * and optimizes the attribute weights in this metric
 * and the number of nearest neighbors used for classification.
 * Various methods of voting by nearest neighbors can be used to select the decision.
 * The nearest neighbors can be filtered using rules (RIONA algorithm).
 * This k-nn implementation uses a metric tree with dual search pruning criterion
 * to accelerate searching for nearest neighbors.
 *
 * @author      Arkadiusz Wojna, Grzegorz Gora, Lukasz Ligowski
 */
public class KnnClassifier extends AbstractParameterisedClassifier implements ClassifierWithDistributedDecision, Serializable
{
	/** Attribute weighting methods. */
	public enum Voting { Equal, InverseDistance, InverseSquareDistance; }

    /** Serialization version. */
	private static final long serialVersionUID = 1L;
    /** Parameter name for the used method weighting the attributes in the metric. */
    public static final String WEIGHTING_METHOD_PROPERTY_NAME = "weightingMethod";
    /** Name for the switch indicating whether the classifier uses a metric tree to speed up searching for nearest neighbors. */
    public static final String INDEXING_PROPERTY_NAME = "indexing";
    /** Name for the switch indicating whether the classifier optimizes automatically the number of neighbors. */
    public static final String LEARN_OPTIMAL_K_PROPERTY_NAME = "learnOptimalK";
    /** Name of the parameter defining the maximal number of neighbors while learning the optimal number. */
    public static final String MAXIMAL_K_PROPERTY_NAME = "maxK";
    /** Parameter name for the number of nearest neighbors. */
    public static final String K_PROPERTY_NAME = "k";
    /** Name of the switch indicating whether rules are used to filter the nearest neighbors. */
    public static final String FILTER_NEIGHBOURS_PROPERTY_NAME = "filterNeighboursUsingRules";
    /** Parameter name for the method of voting by the nearest neighbors. */
    public static final String VOTING_PROPERTY_NAME = "voting";

    /** Original training data. */  
    ArrayList<DoubleData> m_OriginalData;
    /** Data transformer speeding up distance calculation. */
    AttributeTransformer m_Transformer;
    /** Transformed training data. */
    DoubleDataTable m_TransformedTrainTable;
    /** Induced metric. */
    Metric m_Metric;
    /** Provider of nearest neighbors. */
    VicinityProvider m_VicinityProvider;
    /** Optional rule filter for nearest neighbors (RIONA algorithm). */
    private CubeBasedNeighboursFilter m_NeighboursFilter;
    /** Switch indicating whether the classified objects come from the training set. */
    private boolean m_bSelfLearning = false;
    /** Maximal number of neighbors while optimizing automatically the number of nearest neighbors. */
    private int m_nMaxK;
    /** Decision attribute. */
    private NominalAttribute m_DecisionAttribute;
    /** The default decision defined by the largest decision class in the training data. */
    private int m_nDefaultDec;
    
    
    /**
     * Constructor required by rseslib tools.
     * It induces a metric from a training set
     * and optimizes the attribute weights in this metric.
     * Next it builds the metric tree with the training objects
     * to accelerate searching for nearest neighbors. 
     * At last it optimizes the number of nearest neighbors used for classification.
     *
     * @param prop                   Parameters of this classifier.
     * @param trainTable             Training data used to induce a metric and to classify test objects.
     * @param prog                   Progress object for reporting training progress.
     * @throws PropertyConfigurationException	when the parameters are incorrect or incomplete.
     * @throws InterruptedException				when a user interrupts execution.
     */
    public KnnClassifier(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
    	// partition progress into three or two stages: metric induction, metric tree construction and optionally k optimization
        super(prop, K_PROPERTY_NAME);
        // prepare progress information
        int[] progressVolumes = null;
        if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME))
        {
            progressVolumes = new int[3];
            progressVolumes[0] = 40;
            progressVolumes[1] = 10;
            progressVolumes[2] = 50;
        }
        else
        {
            progressVolumes = new int[2];
            progressVolumes[0] = 80;
            progressVolumes[1] = 20;
        }
        prog = new MultiProgress("Learning the k-nn classifier", prog, progressVolumes);
        // induce a metric and transform training objects to speed up distance computation
        m_OriginalData =  trainTable.getDataObjects();
        m_Metric = MetricFactory.getMetric(getProperties(), trainTable);
        m_Transformer = m_Metric.transformationOutside();
        m_TransformedTrainTable = trainTable;
        if (m_Transformer!=null)
        	m_TransformedTrainTable = TableTransformer.transform(trainTable, m_Transformer);
        if (m_Metric instanceof AbstractWeightedMetric)
        	MetricFactory.adjustWeights(getProperty(WEIGHTING_METHOD_PROPERTY_NAME), (AbstractWeightedMetric)m_Metric, m_TransformedTrainTable, prog);
        if(getBoolProperty(INDEXING_PROPERTY_NAME))
        {
        	// build the metric tree and index the training objects
        	IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(m_TransformedTrainTable.getDataObjects(), m_Metric, prog);
        	m_VicinityProvider = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);
        } else {
        	// use linear search
            prog.set("Constructing simple vicinity provider", 1);
            m_VicinityProvider = new ArrayVicinityProvider(m_Metric, m_TransformedTrainTable.getDataObjects());
            prog.step();
        }
        // store information required in classification 
        if (m_Metric instanceof AbstractWeightedMetric)
        	m_NeighboursFilter = new CubeBasedNeighboursFilter((AbstractWeightedMetric)m_Metric, m_Transformer!=null);
        m_nMaxK = getIntProperty(MAXIMAL_K_PROPERTY_NAME);
        m_DecisionAttribute = trainTable.attributes().nominalDecisionAttribute();
        m_nDefaultDec = 0;
        for (int dec = 1; dec < trainTable.getDecisionDistribution().length; dec++)
            if (trainTable.getDecisionDistribution()[dec] > trainTable.getDecisionDistribution()[m_nDefaultDec])
            	m_nDefaultDec = dec;
        if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME))
        {
        	// optimize the number of nearest neighbors using the leave-one-out method
            m_bSelfLearning = true;
            learnOptimalParameterValue(trainTable, prog);
            m_bSelfLearning = false;
        }
        makePropertyModifiable(K_PROPERTY_NAME);
        makePropertyModifiable(FILTER_NEIGHBOURS_PROPERTY_NAME);
        makePropertyModifiable(VOTING_PROPERTY_NAME);
    }

    /**
     * Constructor provided with a prepared metric.
     *
     * @param prop                   Parameters of this classifier.
     * @param metric                 Metric used in this classifier.
     * @param trainTable             Training set used to induce a metric and to classify test objects.
     * @param prog                   Progress object for reporting training progress.
     * @throws PropertyConfigurationException	when the parameters are incorrect or incomplete.
     * @throws InterruptedException				when a user interrupts execution.
     */
    public KnnClassifier(Properties prop, Metric metric, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        super(prop, K_PROPERTY_NAME);
        IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(trainTable.getDataObjects(), metric, prog);
        m_VicinityProvider = new IndexingTreeVicinityProvider(null, metric, indexingTree);
        m_nMaxK = getIntProperty(MAXIMAL_K_PROPERTY_NAME);
        if (metric instanceof AbstractWeightedMetric)
        	m_NeighboursFilter = new CubeBasedNeighboursFilter((AbstractWeightedMetric)metric, true);
        m_DecisionAttribute = trainTable.attributes().nominalDecisionAttribute();
        m_nDefaultDec = 0;
        for (int dec = 1; dec < trainTable.getDecisionDistribution().length; dec++)
            if (trainTable.getDecisionDistribution()[dec] > trainTable.getDecisionDistribution()[m_nDefaultDec]) m_nDefaultDec = dec;
        makePropertyModifiable(K_PROPERTY_NAME);
        makePropertyModifiable(FILTER_NEIGHBOURS_PROPERTY_NAME);
        makePropertyModifiable(VOTING_PROPERTY_NAME);
    }

    /**
     * Constructor provided with a provider of nearest neighbors.
     *
     * @param prop            	Parameters of this classifier.
     * @param decAttr         	Decision attribute.
     * @param vicinProv       	Provider of nearest neighbors.
     * @param neighbourFilter	Rule filter for nearest neighbors.
     * @param decDistribution 	Decision distribution in the training set.
     */
    public KnnClassifier(Properties prop, NominalAttribute decAttr, VicinityProvider vicinProv, CubeBasedNeighboursFilter neighbourFilter, int[] decDistribution) throws PropertyConfigurationException
    {
        super(prop, K_PROPERTY_NAME);
        m_VicinityProvider = vicinProv;
        m_nMaxK = getIntProperty(MAXIMAL_K_PROPERTY_NAME);
        m_NeighboursFilter = neighbourFilter;
        m_DecisionAttribute = decAttr;
        m_nDefaultDec = 0;
        for (int dec = 1; dec < decDistribution.length; dec++)
            if (decDistribution[dec] > decDistribution[m_nDefaultDec]) m_nDefaultDec = dec;
        makePropertyModifiable(K_PROPERTY_NAME);
        makePropertyModifiable(FILTER_NEIGHBOURS_PROPERTY_NAME);
        makePropertyModifiable(VOTING_PROPERTY_NAME);
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	writeAbstractParameterisedClassifier(out);
    	out.writeObject(m_OriginalData);
    	out.writeObject(m_Transformer);
    	out.writeObject(m_Metric);
    	out.writeInt(m_nMaxK);
    	out.writeObject(m_DecisionAttribute);
    	out.writeInt(m_nDefaultDec);
    }

    /**
     * Reads this object.
     *
     * @param in			Input for reading.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readAbstractParameterisedClassifier(in);
    	m_OriginalData = (ArrayList<DoubleData>)in.readObject();
    	ArrayList<DoubleData> transformedObjects = m_OriginalData;
    	m_Transformer = (AttributeTransformer)in.readObject();
        if (m_Transformer!=null)
        {
            transformedObjects = new ArrayList<DoubleData>(m_OriginalData.size());
            for (DoubleData dObj : m_OriginalData)
                transformedObjects.add(m_Transformer.transformToNew(dObj));
        }
        m_TransformedTrainTable = new ArrayListDoubleDataTable(transformedObjects);
    	m_Metric = (Metric)in.readObject();
    	try
    	{
            if(getBoolProperty(INDEXING_PROPERTY_NAME))
            {
            	IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(m_TransformedTrainTable.getDataObjects(), m_Metric, new EmptyProgress());
            	m_VicinityProvider = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);                
            } else
                m_VicinityProvider = new ArrayVicinityProvider(m_Metric, m_TransformedTrainTable.getDataObjects());
    	}
    	catch (InterruptedException e)
    	{
    		throw new NotSerializableException(e.getMessage());
    	}
    	catch (PropertyConfigurationException e)
    	{
    		throw new NotSerializableException(e.getMessage());
    	}
    	m_bSelfLearning = false;
    	m_nMaxK = in.readInt();
        if (m_Metric instanceof AbstractWeightedMetric)
        	m_NeighboursFilter = new CubeBasedNeighboursFilter((AbstractWeightedMetric)m_Metric, m_Transformer!=null);
    	m_DecisionAttribute = (NominalAttribute)in.readObject();
    	m_nDefaultDec = in.readInt();
    }

    /**
     * Informs the classifier whether the classified objects come from the training set.
     * 
     * @param selfLearning	The value to be set.
     */
    public void setSelfLearning(boolean selfLearning)
    {
        m_bSelfLearning = selfLearning;
    }
    
    /**
     * Optimizes the number of nearest neighbors using cross-validation.
     *
     * @param trainTable Training data.
     * @param prog		 Progress object for reporting progress.
     * @throws PropertyConfigurationException	when the parameters are incorrect or incomplete.
     * @throws InterruptedException				when a user interrupts execution.
     */
    protected void learnOptimalParameterValueCV(DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        // partition the training set into folds
        Collection<DoubleData>[] parts =  trainTable.randomStratifiedPartition(10);
        int[][][] confusionMatrices = null;
        prog.set("Learning optimal parameter value using cross-validation", trainTable.noOfObjects());
        
        for (int cv = 0; cv < parts.length; cv++)
        {
            // construct the training part and the test part
        	ArrayList<DoubleData> trn = new ArrayList<DoubleData>();
        	ArrayList<DoubleData> tst = new ArrayList<DoubleData>();
            for (int part = 0; part < parts.length; part++)
                if (part==cv)
                	tst.addAll(parts[part]);
                else
                	trn.addAll(parts[part]);
            VicinityProvider vicProv = null;
            if (getBoolProperty(INDEXING_PROPERTY_NAME))
            {
            	IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(trn, m_Metric, new EmptyProgress());
            	vicProv = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);
            } else
            	vicProv = new ArrayVicinityProvider(m_Metric, trn);
            
            // classify single fold
            for (DoubleData dObj : tst)
            {
            	double[] decisions = classifyWithParameter(dObj, vicProv.getVicinity(dObj, m_nMaxK));
            	if (confusionMatrices==null)
            	{
            		confusionMatrices = new int[decisions.length][][];
            		for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
            		{
            			confusionMatrices[parVal] = new int[m_DecisionAttribute.noOfValues()][];
            			for (int i = 0; i < confusionMatrices[parVal].length; i++)
            				confusionMatrices[parVal][i] = new int[m_DecisionAttribute.noOfValues()];
            		}
            	}
            	for (int parVal = 1; parVal < confusionMatrices.length; parVal++)
            		confusionMatrices[parVal][m_DecisionAttribute.localValueCode(((DoubleDataWithDecision)dObj).getDecision())][m_DecisionAttribute.localValueCode(decisions[parVal])]++;
                prog.step();
            }
        }

        // select the best number of nearest neighbors
        ParameterisedTestResult results = new ParameterisedTestResult(getParameterName(), m_DecisionAttribute, trainTable.getDecisionDistribution(), confusionMatrices, new Properties());
        int bestParamValue = 0;
        for (int parVal = 1; parVal < results.getParameterRange(); parVal++)
            if (results.getClassificationResult(parVal).getAccuracy() > results.getClassificationResult(bestParamValue).getAccuracy())
                bestParamValue = parVal;
        makePropertyModifiable(K_PROPERTY_NAME);
        setProperty(K_PROPERTY_NAME, Integer.toString(bestParamValue));
    }

    /**
     * Assigns a decision distribution given a single test object.
     * The method searches for the nearest neighbors, optionally filters them by rules
     * and applies the selected method of voting by the neighbors.
     * The index of each position with a decision weight in the output vector
     * corresponds to the local code of a decision value.
     *
     * @param dObj  Object to be classified.
     * @return      Assigned decision distribution.
     */
    public double[] classifyWithDistributedDecision(DoubleData dObj) throws PropertyConfigurationException
    {
        if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = m_VicinityProvider.getVicinity(dObj, getIntProperty(K_PROPERTY_NAME));
    	boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null)
        	m_NeighboursFilter.markConsistency(dObj, neighbours);
        double[] decDistr = new double[m_DecisionAttribute.noOfValues()];
        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
        if (neighbours[1].dist() == 0.0 && (votingType == Voting.InverseDistance || votingType == Voting.InverseSquareDistance))
        {
        	for (int n = 1; n < neighbours.length && neighbours[n].dist() == 0; n++)
        		if (!checkConsistency || neighbours[n].m_bConsistent)
        			decDistr[m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision())] = 1.0;
        }
        else
        	for (int n = 1; n < neighbours.length; n++)
        	{
        		int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
        		if (!checkConsistency || neighbours[n].m_bConsistent)
        			switch (votingType)
        			{
        			case Equal:
        				decDistr[curDec] += 1.0;
        				break;
        			case InverseDistance:
        				decDistr[curDec] += 1.0 / neighbours[n].dist();
        				break;
        			case InverseSquareDistance:
        				decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        				break;
        			}
        	}
        return decDistr;
    }

    /**
     * Calculates the decision distribution and the voting weights
     * for a given set of neighbors with calculated distances to a test object.
     * The index of each position with a decision weight in the output vector
     * corresponds to the local code of a decision value.
     *
     * @param dObj  			Object already transformed whose neighbors are given to this method.
     * @param neighbours  		Set of neighbors with the position 0 omitted ordered by the distance to dObj.
     * @param neighbourWeights  Array to be filled with the voting weights of the given neighbors, its length must be equal to neighbours.length.
     * @return      			Decision distribution.
     */
    public double[] getDistributedDecisionAndVotingWeights(DoubleData dObj, Neighbour[] neighbours, double[] neighbourWeights) throws PropertyConfigurationException
    {
    	boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null)
        	m_NeighboursFilter.markConsistency(dObj, neighbours);
        double[] decDistr = new double[m_DecisionAttribute.noOfValues()];
        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
        for (int n = 0; n < neighbourWeights.length; ++n)
        	neighbourWeights[n] = 0.0;
        if (neighbours[1].dist() == 0.0 && (votingType == Voting.InverseDistance || votingType == Voting.InverseSquareDistance))
        {
        	for (int n = 1; n < neighbours.length && neighbours[n].dist() == 0; n++)
        		if (!checkConsistency || neighbours[n].m_bConsistent)
        		{
        			decDistr[m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision())] = 1.0;
        			neighbourWeights[n] = Double.POSITIVE_INFINITY;
        		}
        }
        else
        	for (int n = 1; n < neighbours.length; n++)
        	{
        		int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
        		if (!checkConsistency || neighbours[n].m_bConsistent)
        		{
        			switch (votingType)
        			{
        			case Equal:
        				neighbourWeights[n] = 1.0;
        				break;
        			case InverseDistance:
        				neighbourWeights[n] = 1.0 / neighbours[n].dist();
        				break;
        			case InverseSquareDistance:
        				neighbourWeights[n] = 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        				break;
        			}
    				decDistr[curDec] += neighbourWeights[n];
        		}
        	}
        return decDistr;
    }

    /**
     * Assigns a decision to a single test object.
     * The method searches for the nearest neighbors, optionally filters them by rules
     * and applies the selected method of voting by the neighbors.
     *
     * @param dObj  Object to be classified.
     * @return      Assigned decision.
     */
    public double classify(DoubleData dObj) throws PropertyConfigurationException
    {
        double[] decDistr = classifyWithDistributedDecision(dObj);
        int bestDec = 0;
        for (int dec = 1; dec < decDistr.length; dec++)
            if (decDistr[dec] > decDistr[bestDec]) bestDec = dec;
        return m_DecisionAttribute.globalValueCode(bestDec);
    }

    /**
     * Assigns decisions for the range of different numbers of nearest neighbors.
     *
     * @param dObj         Object to be classified.
     * @return             Array of assigned decisions, array indices are the numbers of nearest neighbors.
     */
    public double[] classifyWithParameter(DoubleData dObj) throws PropertyConfigurationException
    {
        if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = null;
        if (m_bSelfLearning)
        {
            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObj, m_nMaxK+1);
            neighbours = new Neighbour[neighboursOneMore.length-1];
            int i = 1;
            for (; i < neighbours.length && !dObj.equals(neighboursOneMore[i].neighbour()); i++)
            	neighbours[i] = neighboursOneMore[i];
           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
        }
        else neighbours = m_VicinityProvider.getVicinity(dObj, m_nMaxK);
        return classifyWithParameter(dObj, neighbours);
    }
    
    /**
     * Assigns decisions for the range of different numbers of nearest neighbors
     * provided the nearest neighbors ordered by the distance to the classified object.
     *
     * @param dObj         Object to be classified.
     * @param neighbors	   Nearest neighbors ordered by the distance to the classified object.
     * @return             Array of assigned decisions, array indices are the numbers of nearest neighbors.
     */
    public double[] classifyWithParameter(DoubleData dObj, Neighbour[] neighbours) throws PropertyConfigurationException
    {
    	boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null)
        	m_NeighboursFilter.markConsistency(dObj, neighbours);
        double[] decisions = new double[m_nMaxK+1];
        double[] decDistr = new double[m_DecisionAttribute.noOfValues()];
        int bestDec = m_nDefaultDec;
        decisions[0] = m_DecisionAttribute.globalValueCode(bestDec);
        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
        int firstNotSet = 1;
        for (int n = 1; n < neighbours.length; n++)
        {
        	int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
        	if (!checkConsistency || neighbours[n].m_bConsistent)
        		switch (votingType)
        		{
        		case Equal:
        			decDistr[curDec] += 1.0;
        			break;
        		case InverseDistance:
        			decDistr[curDec] += 1.0 / neighbours[n].dist();
        			break;
        		case InverseSquareDistance:
        			decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        			break;
        		}
        	if (n == neighbours.length - 1 || neighbours[n].dist() != neighbours[n+1].dist())
        	{
        		if (firstNotSet < n)
        		{
        			for (int d = 0; d < decDistr.length; d++)
        	        	if (decDistr[d] > decDistr[bestDec]) bestDec = d;
        		}
        		else
                	if (decDistr[curDec] > decDistr[bestDec]) bestDec = curDec;
        		for (int i = firstNotSet; i <= n && i < decisions.length; i++)
        			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
        		firstNotSet = n + 1;
        	}
        }
        for (int i = firstNotSet; i < decisions.length; i++)
			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
        return decisions;
    }

    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
        try
        {
            if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME))
                addToStatistics("Optimal "+K_PROPERTY_NAME, getProperty(K_PROPERTY_NAME));
        }
        catch (PropertyConfigurationException e)
        {
        }
        //addToStatistics("Average number of distance calculations", Double.toString(m_VicinityProvider.getAverageNoOfDistCalculations()));
        //addToStatistics("Std. dev. of the number of distance calculations", Double.toString(m_VicinityProvider.getStdDevNoOfDistCalculations()));
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

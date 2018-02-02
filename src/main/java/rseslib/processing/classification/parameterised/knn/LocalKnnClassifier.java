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


package rseslib.processing.classification.parameterised.knn;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import rseslib.processing.classification.parameterised.AbstractParameterisedClassifier;
import rseslib.processing.indexing.metric.TreeIndexer;
import rseslib.processing.metrics.MetricFactory;
import rseslib.processing.searching.metric.IndexingTreeVicinityProvider;
import rseslib.processing.searching.metric.VicinityProvider;
import rseslib.processing.transformation.AttributeTransformer;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObjectWithMemory;
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
 * Two-step k nearest neighbours classifier.
 * For each test object first it selects a number of nearest neighbours
 * and constructs a local metric on the basis of the selected neighbours.
 * Next the classifier selects a few nearest neighbours with respect
 * to this locally induced metric and uses them to vote.
 *
 * @author      Arkadiusz Wojna
 */
public class LocalKnnClassifier extends AbstractParameterisedClassifier implements Serializable
{
	/** Attribute weighting methods. */
	public enum Voting { Equal, InverseDistance, InverseSquareDistance; }

    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	/** Property name for weighting method. */
    public static final String WEIGHTING_METHOD_PROPERTY_NAME = "weightingMethod";
    /** Name of property indicating whether the classifier learns the optimal number k. */
    public static final String LEARN_OPTIMAL_K_PROPERTY_NAME = "learnOptimalK";
    /** Name of property defining the maximal number of k while learning the optimal value. */
    public static final String LOCAL_SET_SIZE_PROPERTY_NAME = "localSetSize";
    /** Parameter name. */
    public static final String K_PROPERTY_NAME = "k";
    /** Name of property indicating whether neighbour voting is weighted with distance. */
    public static final String VOTING_PROPERTY_NAME = "voting";

    /** Name of the statistical value for the minimal number of iterations. */
    public static final String MINIMAL_NUMBER_OF_ITERATIONS_STATISTICS_NAME = "Minimal number of weighting iterations";
    /** Name of the statistical value for the average number of iterations. */
    public static final String AVERAGE_NUMBER_OF_ITERATIONS_STATISTICS_NAME = "Average number of weighting iterations";

    /** Collection of the original training data objects. */  
    private Collection<DoubleData> m_OriginalData;
    /** Data transoformer used in the induced metric. */
    private AttributeTransformer m_Transformer;
    /** The induced metric. */
    private Metric m_Metric;
    /** Provider of vicinity for test data objects. */
    VicinityProvider m_VicinityProvider;
    /** Switch to recognize whether searching for optimal k is going on. */
    private boolean m_bSelfLearning = false;
    /** Size of the local set used to induce a local metric. */
    private int m_nLocalSetSize;
    /** Properties of a local metric. */
    Properties m_LocalMetricProperties = new Properties();
    /** Distances in the global metric to be restored at neighbours. */
    double[] m_DistancesToRestore;
    /** Decision attribute. */
    NominalAttribute m_DecisionAttribute;
    /** The default decision defined by the largest support in a training data set. */
    int m_nDefaultDec;
    /** Empty progress object. */
    Progress m_EmptyProgress = new EmptyProgress();
    /** The minimal number of weighting iterations over all tested objects. */
    //private int m_nMinNoOfIterations = -1;
    /** The sum of weighting iterations over all tested objects. */
    //private int m_nSumOfIterations = 0;
    /** The number of tested objects. */
    //private int m_nNoOfTestObjects = 0;

    /**
     * Constructor that induces a metric
     * from a given training set trainTable
     * and constructs an indexing tree.
     * It transforms data objects inside the constructor.
     *
     * @param prop                   Properties of this knn clasifier.
     * @param trainTable             Table used to build vicinity provider and to learn the optimal value of the classifier parameter.
     * @param prog                   Progress object to report training progress.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public LocalKnnClassifier(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        super(prop, K_PROPERTY_NAME);
        // prepare progress information
        int[] progressVolumes = null;
        if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME))
        {
            progressVolumes = new int[3];
            progressVolumes[0] = 8;
            progressVolumes[1] = 2;
            progressVolumes[2] = 90;
        }
        else
        {
            progressVolumes = new int[2];
            progressVolumes[0] = 80;
            progressVolumes[1] = 20;
        }
        prog = new MultiProgress("Learning the local k-nn classifier", prog, progressVolumes);
        // induce a global metric and transform training objects for optimization of distance computations 
        m_OriginalData =  trainTable.getDataObjects();
        m_Metric = MetricFactory.getMetric(getProperties(), trainTable);
        m_Transformer = m_Metric.transformationOutside();
        ArrayList<DoubleData> transformedObjects = new ArrayList<DoubleData>(m_OriginalData.size());
        for (DoubleData dObj : m_OriginalData)
        {
        	DoubleDataObjectWithMemory newDObj = new DoubleDataObjectWithMemory(dObj);
        	newDObj.saveValues(0);
        	if (m_Transformer!=null) m_Transformer.transform(newDObj);
        	newDObj.saveValues(1);
        	transformedObjects.add(newDObj);
        }
        DoubleDataTable transformedTrainTable = new ArrayListDoubleDataTable(transformedObjects);
        if (m_Metric instanceof AbstractWeightedMetric)
        	MetricFactory.adjustWeights(getProperty(WEIGHTING_METHOD_PROPERTY_NAME), (AbstractWeightedMetric)m_Metric, transformedTrainTable, prog);
        // index the training objects
        IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(transformedTrainTable.getDataObjects(), m_Metric, prog);
        m_VicinityProvider = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);
        // store information required in classification 
        m_nLocalSetSize = getIntProperty(LOCAL_SET_SIZE_PROPERTY_NAME);
        m_LocalMetricProperties.setProperty(MetricFactory.METRIC_PROPERTY_NAME, MetricFactory.MetricType.CityAndSimpleValueDifference.name());
        m_LocalMetricProperties.setProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME, "200");
        m_DecisionAttribute = trainTable.attributes().nominalDecisionAttribute();
        m_nDefaultDec = 0;
        for (int dec = 1; dec < trainTable.getDecisionDistribution().length; dec++)
            if (trainTable.getDecisionDistribution()[dec] > trainTable.getDecisionDistribution()[m_nDefaultDec]) m_nDefaultDec = dec;
        if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME))
        {
            m_bSelfLearning = true;
            learnOptimalParameterValue(trainTable, prog);
            m_bSelfLearning = false;
        }
        makePropertyModifiable(K_PROPERTY_NAME);
        makePropertyModifiable(VOTING_PROPERTY_NAME);
    }

    /**
     * Constructor.
     *
     * @param prop            Map between property names and property values.
     * @param decAttr         Decision attribute.
     * @param vicinProv       Provider of vicninities for test data objects.
     * @param decDistribution Distribution of decision in a training data set.
     */
    public LocalKnnClassifier(Properties prop, NominalAttribute decAttr, VicinityProvider vicinProv, int[] decDistribution) throws PropertyConfigurationException
    {
        super(prop, K_PROPERTY_NAME);
        m_VicinityProvider = vicinProv;
        m_nLocalSetSize = getIntProperty(LOCAL_SET_SIZE_PROPERTY_NAME);
        m_LocalMetricProperties.setProperty(MetricFactory.METRIC_PROPERTY_NAME, MetricFactory.MetricType.CityAndSimpleValueDifference.name());
        m_LocalMetricProperties.setProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME, "200");
        m_DecisionAttribute = decAttr;
        m_nDefaultDec = 0;
        for (int dec = 1; dec < decDistribution.length; dec++)
            if (decDistribution[dec] > decDistribution[m_nDefaultDec]) m_nDefaultDec = dec;
        makePropertyModifiable(K_PROPERTY_NAME);
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
    	out.writeInt(m_nLocalSetSize);
    	out.writeObject(m_LocalMetricProperties);
    	out.writeObject(m_DecisionAttribute);
    	out.writeInt(m_nDefaultDec);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readAbstractParameterisedClassifier(in);
    	m_OriginalData = (Collection<DoubleData>)in.readObject();
    	m_Transformer = (AttributeTransformer)in.readObject();
    	ArrayList<DoubleData> transformedObjects = new ArrayList<DoubleData>(m_OriginalData.size());
    	for (DoubleData dObj : m_OriginalData)
    	{
    		DoubleDataObjectWithMemory newDObj = new DoubleDataObjectWithMemory(dObj);
    		newDObj.saveValues(0);
    		if (m_Transformer!=null) m_Transformer.transform(newDObj);
    		newDObj.saveValues(1);
    		transformedObjects.add(newDObj);
    	}
        DoubleDataTable transformedTrainTable = new ArrayListDoubleDataTable(transformedObjects);
    	m_Metric = (Metric)in.readObject();
    	try
    	{
    		IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(transformedTrainTable.getDataObjects(), m_Metric, new EmptyProgress());
    		m_VicinityProvider = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);
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
    	m_nLocalSetSize = in.readInt();
    	m_LocalMetricProperties = (Properties)in.readObject();
    	m_DecisionAttribute = (NominalAttribute)in.readObject();
    	m_nDefaultDec = in.readInt();
    	m_EmptyProgress = new EmptyProgress();
    }

    /**
     * Sets the self-learning switch, required to set,
     * if k optimization is done outside the classifier.
     * 
     * @param selfLearning	The value to be set.
     */
    public void setSelfLearning(boolean selfLearning)
    {
        m_bSelfLearning = selfLearning;
    }
    
    /**
     * Classifies a test object on the basis of nearest neighbours.
     * If an object with memory is provided, this classifier assumes
     * that the original values are saved in the store 0.
     *
     * @param dObj         Test object.
     * @return             Array of assigned decisions, indices correspond to parameter values.
     */
    public double[] classifyWithParameter(DoubleData dObj) throws PropertyConfigurationException
    {
    	// convert dObj to an object with memory and transform
    	DoubleDataObjectWithMemory dObjMem;
    	if (dObj instanceof DoubleDataObjectWithMemory)
    	{
    		dObjMem = (DoubleDataObjectWithMemory)dObj;
    		dObjMem.saveValues(1);
    	}
    	else
    	{
   			dObjMem = new DoubleDataObjectWithMemory(dObj); 
   			dObjMem.saveValues(0);
        }
		if (m_Transformer!=null) m_Transformer.transform(dObjMem);
    	// extract the local set
        Neighbour[] neighbours = null;
        if (m_bSelfLearning)
        {
            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObjMem, m_nLocalSetSize+1);
            neighbours = new Neighbour[neighboursOneMore.length-1];
            int i = 1;
            for (; i < neighbours.length && !dObjMem.equals(neighboursOneMore[i].neighbour()); i++)
            	neighbours[i] = neighboursOneMore[i];
           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
        }
        else neighbours = m_VicinityProvider.getVicinity(dObjMem, m_nLocalSetSize);
        // induce a local metric
        DoubleData[] dataObjects = new DoubleData[neighbours.length-1];
        for (int n = 1; n < neighbours.length; n++)
        {
            dataObjects[n-1] = neighbours[n].neighbour();
            ((DoubleDataObjectWithMemory)dataObjects[n-1]).restoreSavedValues(0);
        }
        DoubleDataTable localTab = new ArrayListDoubleDataTable(dataObjects);
        Metric localMetr = MetricFactory.getMetric(m_LocalMetricProperties, localTab);
        AttributeTransformer trans = localMetr.transformationOutside();
        for (int obj = 0; obj < dataObjects.length; obj++)
        	trans.transform(dataObjects[obj]);
        try
        {
        	if (localMetr instanceof AbstractWeightedMetric)
        		MetricFactory.adjustWeights(MetricFactory.Weighting.DistanceBased.name(), (AbstractWeightedMetric)localMetr, localTab, m_EmptyProgress);
        }
        catch (InterruptedException e) { }
        /*if (m_nMinNoOfIterations==-1 || ((WeightedMetric)localMetr).getNoOfWeightingIterations() < m_nMinNoOfIterations)
        	m_nMinNoOfIterations = ((WeightedMetric)localMetr).getNoOfWeightingIterations();
        m_nSumOfIterations += ((WeightedMetric)localMetr).getNoOfWeightingIterations();
        m_nNoOfTestObjects++;*/
        // computes the distances according to the local metric 
        dObjMem.restoreSavedValues(0);
        trans.transform(dObjMem);
        Neighbour[] shiftedNeighbours = new Neighbour[neighbours.length-1];
        if (m_DistancesToRestore==null || m_DistancesToRestore.length < shiftedNeighbours.length)
        	m_DistancesToRestore = new double[shiftedNeighbours.length];
        for (int n = 1; n < neighbours.length; n++)
        {
        	m_DistancesToRestore[n-1] = neighbours[n].dist(); 
        	shiftedNeighbours[n-1] = neighbours[n];
        	shiftedNeighbours[n-1].setDist(localMetr.dist(dObjMem, shiftedNeighbours[n-1].neighbour()));
        }
        // sort the objects according to the distances in the local metric
        Arrays.sort(shiftedNeighbours);
        // classify
        double[] decisions = new double[m_nLocalSetSize+1];
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
        for (int n = 0; n < shiftedNeighbours.length; n++)
        {
        	int curDec = m_DecisionAttribute.localValueCode(shiftedNeighbours[n].neighbour().getDecision());
        	switch (votingType)
        	{
        	case Equal:
        		decDistr[curDec] += 1.0;
        		break;
        	case InverseDistance:
        		decDistr[curDec] += 1.0 / shiftedNeighbours[n].dist();
        		break;
        	case InverseSquareDistance:
        		decDistr[curDec] += 1.0 / (shiftedNeighbours[n].dist()*shiftedNeighbours[n].dist());
        		break;
        	}
        	if (n == shiftedNeighbours.length - 1 || shiftedNeighbours[n].dist() != shiftedNeighbours[n+1].dist())
        	{
        		if (firstNotSet < n + 1)
        		{
        			for (int d = 0; d < decDistr.length; d++)
        				if (decDistr[d] > decDistr[bestDec]) bestDec = d;
        		}
        		else
        			if (decDistr[curDec] > decDistr[bestDec]) bestDec = curDec;
        		for (int i = firstNotSet; i <= n + 1 && i < decisions.length; i++)
        			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
        		firstNotSet = n + 2;
        	}
        }
        for (int i = firstNotSet; i < decisions.length; i++)
			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
        // restore the object values and distances for the global metric
        for (int n = 1; n < neighbours.length; n++)
        	neighbours[n].setDist(m_DistancesToRestore[n-1]); 
        for (int obj = 0; obj < dataObjects.length; obj++)
        	((DoubleDataObjectWithMemory)dataObjects[obj]).restoreSavedValues(1);
    	if (dObj instanceof DoubleDataObjectWithMemory) dObjMem.restoreSavedValues(1);
        return decisions;
    }

    /**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
     */
    public double classify(DoubleData dObj) throws PropertyConfigurationException
    {
        return classifyWithParameter(dObj)[getIntProperty(K_PROPERTY_NAME)];
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
        //addToStatistics(MINIMAL_NUMBER_OF_ITERATIONS_STATISTICS_NAME, String.valueOf(m_nMinNoOfIterations));
        //addToStatistics(AVERAGE_NUMBER_OF_ITERATIONS_STATISTICS_NAME, String.valueOf(((double)m_nSumOfIterations)/(double)m_nNoOfTestObjects));
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

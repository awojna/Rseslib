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


package rseslib.processing.indexing.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.TreeSet;

import rseslib.structure.data.DoubleData;
import rseslib.structure.index.metric.IndexingTreeFork;
import rseslib.structure.index.metric.IndexingTreeLeaf;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.index.metric.IndexingTreeNodeComparator;
import rseslib.structure.metric.Metric;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Abstract indexer constructing binary tree
 * that indexes data objects.
 *
 * @author      Arkadiusz Wojna
 */
public class TreeIndexer extends ConfigurationWithStatistics
{
    /** Property name for the ratio of data objects to the number of generated elementary nodes. */
    public static final String OBJECTS_TO_LEAVES_RATIO_PROPERTY_NAME = "objectsToLeavesRatio";

    /** Ratio of data objects to the number of generated leaves. */
    private int m_nObjectsToLeavesRatio = getIntProperty(OBJECTS_TO_LEAVES_RATIO_PROPERTY_NAME);
    /** Clustering procedure used for splitting nodes. */
    private IndexingTreeLeafClusterer m_Clusterer;
    /** Last indexing tree. */
    private IndexingTreeNode m_Tree;
    /** The height of the last indexing tree. */
    private int m_nHeight;
    /** Counter for the number of distance comparisions. */
    private double m_nDistOperCounter = 0;

    /**
     * Constructor.
     *
     * @param prop      Map between property names and property values.
     */
    public TreeIndexer(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
        m_Clusterer = new KApproxCentersIndexingTreeLeafClusterer(new FarthestKSeedsSelector(), 3);
    }

    /**
     * Constructor.
     *
     * @param prop      Map between property names and property values.
     * @param clusterer Clustering procedure used for splitting nodes.
     */
    public TreeIndexer(Properties prop, IndexingTreeLeafClusterer clusterer) throws PropertyConfigurationException
    {
        super(prop);
        m_Clusterer = clusterer;
    }

    /**
     * Constructs a binary tree indexing a data set.
     *
     * @param objects Collection of data objects to be indexed.
     * @param metric  Metric used for indexing.
     * @param prog    Progress object.
     * @return        The root of the indexing tree.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public IndexingTreeNode indexing(Collection<DoubleData> objects, Metric metric, Progress prog) throws InterruptedException
    {
        DoubleData[] objectsArray = objects.toArray(new DoubleData[0]);
        m_Tree = new IndexingTreeLeaf(null, metric, objectsArray, KApproxCentersIndexingTreeLeafClusterer.selectCenter(objectsArray, metric));
        TreeSet<IndexingTreeNode> ts = new TreeSet<IndexingTreeNode>(new IndexingTreeNodeComparator());
        ts.add(m_Tree);
        int noOfLeaves = objects.size()/m_nObjectsToLeavesRatio;
        int percentage = 0;
        prog.set("Indexing training objects", 100);
        boolean cont = true;
        while (cont && ts.size() < noOfLeaves)
        {
            IndexingTreeLeaf cl = (IndexingTreeLeaf)ts.last();
            if (cl.getWeight()== 0) cont = false;
            else
            {
            	ts.pollLast();
            	IndexingTreeFork splitNode;
            	splitNode = m_Clusterer.cluster(cl);
            	m_nDistOperCounter += m_Clusterer.getNoOfDistOper();
            	if (cl.getParent()!=null) cl.getParent().replaceChild(cl, splitNode);
            	else m_Tree = splitNode;
            	for (int child = 0; child < splitNode.noOfChildren(); child++)
            		if (splitNode.getChildNode(child).size()>0) ts.add(splitNode.getChildNode(child));
            	while (percentage < 100 && 100*Math.log(ts.size())/Math.log(noOfLeaves) >= percentage+1)
            	{
            		percentage++;
            		prog.step();
            	}
            }
        }
        while (percentage < 100)
        {
            percentage++;
            prog.step();
        }
        m_nHeight = m_Tree.getHeight();
        m_nDistOperCounter /= (double)objectsArray.length;
        return m_Tree;
    }

    /**
     * Returns the average number of iterations.
     *
     * @return Average number of iterations.
     */
    public double getAverageNoOfIterations()
    {
        return m_Clusterer.getAverageNoOfIterations();
    }

    /**
     * Returns the maximum number of iterations.
     *
     * @return Maximum number of iterations.
     */
    public double getMaximumNoOfIterations()
    {
        return m_Clusterer.getMaximumNoOfIterations();
    }

    /**
     * Returns the set of all leaves in a given indexing tree.
     *
     * @param treeRoot Root of the tree to be scanned for leaves.
     * @return         Set of all leaves in the tree treeRoot.
     */
    public IndexingTreeLeaf[] getLeaves(IndexingTreeNode treeRoot)
    {
        ArrayList<IndexingTreeNode> leavesArray = new ArrayList<IndexingTreeNode>();
        ArrayList<IndexingTreeNode> nodesStack = new ArrayList<IndexingTreeNode>();
        nodesStack.add(treeRoot);
        while (nodesStack.size() > 0)
        {
            IndexingTreeNode cl = (IndexingTreeNode)nodesStack.remove(nodesStack.size()-1);
            if (cl.isElementary()) leavesArray.add(cl);
            else
                for (int child = 0; child < ((IndexingTreeFork)cl).noOfChildren(); child++)
                    nodesStack.add(((IndexingTreeFork)cl).getChildNode(child));
        }
        return (IndexingTreeLeaf[])leavesArray.toArray(new IndexingTreeLeaf[0]);
    }

    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
        addToStatistics("Average number of indexing iterations", Double.toString(getAverageNoOfIterations()));
        addToStatistics("Maximum number of indexing iterations", Double.toString(getMaximumNoOfIterations()));
        addToStatistics("Height of the indexing tree", Double.toString(m_nHeight));
        addToStatistics("Average number of distance comparisions", Double.toString(m_nDistOperCounter));
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

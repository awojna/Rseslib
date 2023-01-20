/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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


package rseslib.processing.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Stack;

import rseslib.processing.indexing.metric.TreeIndexer;
import rseslib.structure.data.DoubleData;
import rseslib.structure.index.metric.IndexingTreeFork;
import rseslib.structure.index.metric.IndexingTreeLeaf;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.metric.Metric;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * The method of partitioning a set of data objects
 * into a number of clusters taken from leaves
 * of the indexing tree generated
 * with TreeIndexer.
 *
 * @author      Arkadiusz Wojna
 */
public class TreeClusterer extends Configuration implements Clusterer
{
    /** Empty progress. */
    private static final Progress EMPTY_PROGRESS = new EmptyProgress();

    /** Metric used to cluster objects. */
    private Metric m_Metric;
    /** Number of parts to be generated. */
    private int m_nNoOfParts;
    /** Indexer. */
    private TreeIndexer m_Indexer = new TreeIndexer(null);

    /**
     * Constructor.
     *
     * @param prop            Map between property names and property values.
     * @param noOfParts       Number of parts to be generated.
     */
    public TreeClusterer(Properties prop, Metric metric, int noOfParts) throws PropertyConfigurationException
    {
        super(prop);
        m_Metric = metric;
        m_nNoOfParts = noOfParts;
    }

    /**
     * Divides a collection of data objects
     * into a number of clusters.
     *
     * @param dataColl Collection of data objects to be clustered.
     * @return         Array of clusters.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public ArrayList<DoubleData>[] cluster(Collection<DoubleData> dataColl) throws InterruptedException
    {
        try
        {
            m_Indexer.setProperty(TreeIndexer.OBJECTS_TO_LEAVES_RATIO_PROPERTY_NAME, String.valueOf(dataColl.size()/m_nNoOfParts));
        }
        catch (PropertyConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        IndexingTreeNode root = m_Indexer.indexing(dataColl, m_Metric, EMPTY_PROGRESS);
        Stack<IndexingTreeNode> stackOfNodes = new Stack<IndexingTreeNode>();
        ArrayList<IndexingTreeNode> listOfLeaves = new ArrayList<IndexingTreeNode>();
        stackOfNodes.push(root);
        while (!stackOfNodes.empty())
        {
            IndexingTreeNode node = (IndexingTreeNode)stackOfNodes.pop();
            if (node.isElementary()) listOfLeaves.add(node);
            else
                for (int subnode = 0; subnode < ((IndexingTreeFork)node).noOfChildren(); subnode++)
                    stackOfNodes.push(((IndexingTreeFork)node).getChildNode(subnode));
        }
        ArrayList<DoubleData>[] arrayOfLeaves = new ArrayList[listOfLeaves.size()];
        for (int cl = 0; cl < arrayOfLeaves.length; cl++)
        {
        	DoubleData[] leafObjects = ((IndexingTreeLeaf)listOfLeaves.get(cl)).getObjects();
            arrayOfLeaves[cl] = new ArrayList<DoubleData>(leafObjects.length);
            for (DoubleData obj : leafObjects) arrayOfLeaves[cl].add(obj);
        }
        return arrayOfLeaves;
    }
}

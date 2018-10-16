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


package rseslib.structure.index.metric;

import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Metric;

/**
 * A node of an indexing tree with data objects
 * split into a number of subnodes.
 *
 * @author      Arkadiusz Wojna
 */
public class IndexingTreeFork extends IndexingTreeNode
{
    /** The size of this node as the sum of data objects from both subnodes. */
    private int m_nSize;
    /** Array of subnodes. */
    private IndexingTreeNode[] m_Subnodes;

    /**
     * Constructor.
     *
     * @param supernode    Supernode of this node in the indexing binary tree.
     * @param metric       Metric used to measure distance between data objects.
     * @param objects      Array of all data objects from this node.
     * @param subnodesData Array of data objects in subnodes.
     */
    public IndexingTreeFork(IndexingTreeFork supernode, Metric metric, DoubleData[] objects, DoubleData center, DoubleData[][] subnodesData, DoubleData[] subnodesCenters)
    {
        super(supernode, metric, objects, center);
        m_nSize = objects.length;
        m_Subnodes = new IndexingTreeNode[subnodesData.length];
        for (int subnode = 0; subnode < m_Subnodes.length; subnode++)
            m_Subnodes[subnode] = new IndexingTreeLeaf(this, metric, subnodesData[subnode], subnodesCenters[subnode]);
    }

    /**
     * Returns the number of children in this node.
     *
     * @return Number of children in this node.
     */
    public int noOfChildren()
    {
        return m_Subnodes.length;
    }

    /**
     * Returns the height of this tree.
     *
     * @return The height of this tree.
     */
    public int getHeight()
    {
        int maxHeight = 1;
        for (int subnode = 0; subnode < m_Subnodes.length; subnode++)
        {
            int subnodeHeight = m_Subnodes[subnode].getHeight()+1;
            if (subnodeHeight > maxHeight) maxHeight = subnodeHeight;
        }
        return maxHeight;
    }

    /**
     * Returns true if this node is elementary, false otherwise.
     *
     * @return True if this node is elementary, false otherwise.
     */
    public boolean isElementary()
    {
	return false;
    }

    /**
     * Returns the number of data objects in this node.
     *
     * @return Number of data objects in this node.
     */
    public int size()
    {
	return m_nSize;
    }

    /**
     * Replaces an old subnode with a new one.
     *
     * @param prevNode The old subnode to be replaced.
     * @param newNode  The new subnode.
     */
    public void replaceChild(IndexingTreeNode prevNode, IndexingTreeNode newNode)
    {
        int subnode = 0;
        while (m_Subnodes[subnode] != prevNode && subnode < m_Subnodes.length) subnode++;
        if (subnode >= m_Subnodes.length) throw new RuntimeException("A child node to be replaced in a parent node of the indexing tree not found in the parent");
        m_Subnodes[subnode] = newNode;
        newNode.setParent(this);
    }

    /**
     * Returns a subnode.
     *
     * @param childIndex Index of a subnode to be returned.
     * @return           Subnode.
     */
    public IndexingTreeNode getChildNode(int childIndex)
    {
	return m_Subnodes[childIndex];
    }
}

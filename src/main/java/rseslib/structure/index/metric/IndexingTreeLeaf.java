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
 * Leaf of an indexing tree contains the list of data objects.
 *
 * @author      Arkadiusz Wojna
 */
public class IndexingTreeLeaf extends IndexingTreeNode
{
    /** Array of data objects of this node. */
    private DoubleData[] m_arrObjects;
    /** Number of children generated while splitting this node with the balanced splitting method. */
    private int m_nSplittingDegree = 0;

    /**
     * Constructor.
     *
     * @param supernode Supernode of this node in the indexing binary tree.
     * @param metric    Metric used to measure distance between data objects.
     * @param objects   Set of data objects of this node.
     */
    public IndexingTreeLeaf(IndexingTreeFork supernode, Metric metric, DoubleData[] objects, DoubleData center)
    {
        super(supernode, metric, objects, center);
        m_arrObjects = objects;
    }

    /**
     * Returns the height of this tree.
     *
     * @return The height of this tree.
     */
    public int getHeight()
    {
        return 0;
    }

    /**
     * Returns true if this node is elementary, false otherwise.
     *
     * @return True if this node is elementary, false otherwise.
     */
    public boolean isElementary()
    {
	return true;
    }

    /**
     * Returns the number of data objects in this node.
     *
     * @return Number of data objects in this node.
     */
    public int size()
    {
	return m_arrObjects.length;
    }

    /**
     * Returns an array of data objects from this node.
     *
     * @return Array of data objects from this node.
     */
    public DoubleData[] getObjects()
    {
	return m_arrObjects;
    }

    /**
     * Returns the splitting degree of this node.
     *
     * @return The splitting degree of this node.
     */
    public int getSplittingDegree()
    {
        return m_nSplittingDegree;
    }
}

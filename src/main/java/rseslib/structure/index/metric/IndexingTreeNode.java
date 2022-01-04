/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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
import rseslib.system.Report;

/**
 * Node with data objects and the computed mean, radius and weight.
 * The weight of this node is the sum over distances
 * between members of this node and the mean.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class IndexingTreeNode
{
    /** Supernode of this node in the indexing binary tree. */
    IndexingTreeFork m_SuperNode;
    /** Metric used to index data objects. */
    Metric m_Metric;
    /** Mean of this node. */
    DoubleData m_Center;
    /** Radius of this node. */
    double m_nRadius;
    /** Weight of this node. */
    double m_nWeight;

    /**
     *
     * Constructor computes the mean, the radius and the weight of this node.
     *
     * @param supercl Supernode of this node in the indexing binary tree.
     * @param metric  Metric used to measure distance between data objects.
     * @param objects Set of data objects of this node.
     */
    public IndexingTreeNode(IndexingTreeFork supercl, Metric metric, DoubleData[] objects, DoubleData center)
    {
        m_SuperNode = supercl;
        m_Metric = metric;
        m_Center = center;
        //m_nMinDistances = minDistances;
        //m_nMaxDistances = maxDistances;
        computeRadiusAndWeight(objects, m_Metric);
    }

    /**
     * Returns the height of this tree.
     *
     * @return The height of this tree.
     */
    public abstract int getHeight();

    /**
     * Returns true if this node is elementary, false otherwise.
     *
     * @return True if this node is elementary, false otherwise.
     */
    public abstract boolean isElementary();

    /**
     * Sets a given node supernode as the supernode for this node.
     *
     * @param supernode Node to be set as the supernode.
     */
    public void setParent(IndexingTreeFork supernode)
    {
    	m_SuperNode = supernode;
    }

    /**
     * Returns the supernode of this node.
     *
     * @return The supernode of this node.
     */
    public IndexingTreeFork getParent()
    {
    	return m_SuperNode;
    }

    /**
     * Returns the local metric induced from this node.
     *
     * @return Local metric induced from this node.
     */
    public Metric getMetric()
    {
        return m_Metric;
    }

    /**
     * Returns the number of data objects in this node.
     *
     * @return Number of data objects in this node.
     */
    public abstract int size();

    /**
     * Returns the mean of this node.
     *
     * @return The mean of this node.
     */
    public DoubleData getCenter()
    {
    	return m_Center;
    }

    /**
     * Computes the radius and the weight of this node.
     *
     * @param objects Set of data objects of this node.
     * @param metric Metric used to measure distance between data objects.
     */
    void computeRadiusAndWeight(DoubleData[] objects, Metric metric)
    {
    	m_nRadius = 0;
    	m_nWeight = 0;
    	for (int obj = 0; obj < objects.length; obj++)
    	{
    		double r = metric.dist(objects[obj], m_Center);
    		if (r > m_nRadius) m_nRadius = r;
    		m_nWeight += r;
    	}
    	if (objects.length <= 1) m_nWeight = 0.0; 
    }

    /**
     * Returns the radius of this node.
     *
     * @return Radius of this node.
     */
    public double getRadius()
    {
    	return m_nRadius;
    }

    /**
     * Returns the weight of this node.
     *
     * @return Weight of this node.
     */
    public double getWeight()
    {
    	return m_nWeight;
    }

    /**
     * String representation of this node.
     *
     * @return String representation of this node.
     */
    public String toString()
    {
    	return "Mean:"+Report.lineSeparator+m_Center.toString()+Report.lineSeparator+"Size: "+size()+Report.lineSeparator+"Radius: "+m_nRadius+Report.lineSeparator+"Weight: "+m_nWeight+Report.lineSeparator;
    }

    /*public double[] getMinDistances()
    {
        return m_nMinDistances;
    }

    public double[] getMaxDistances()
    {
        return m_nMaxDistances;
    }*/
}

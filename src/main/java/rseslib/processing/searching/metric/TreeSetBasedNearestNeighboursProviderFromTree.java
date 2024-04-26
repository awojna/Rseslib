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


package rseslib.processing.searching.metric;

import java.util.ArrayList;
import java.util.TreeSet;

import rseslib.structure.data.DoubleData;
import rseslib.structure.index.metric.IndexingTreeFork;
import rseslib.structure.index.metric.IndexingTreeLeaf;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.metric.NeighbourComparator;

/**
 * The method extracting nearest neighbours of a data object
 * from an indexing binary tree.
 * TreeSet is used as the priority queue for nearest neighbours.
 *
 * @author      Arkadiusz Wojna
 */
public class TreeSetBasedNearestNeighboursProviderFromTree extends NearestNeighboursProvider
{
    /** Counter for the number of getKNearest calls. */
    private int m_nCallsCounter = 0;
    /** Counter for the number of distance calculations. */
    private double m_nDistCalculationsCounter = 0;
    /** Counter for the square number of distance calculations. */
    private double m_nSquareDistCalculationsCounter = 0;
    /** List of ordered nearest neighbours. */
    TreeSet<Neighbour> m_OrderedNearest = new TreeSet<Neighbour>(new NeighbourComparator());
    /** List of stacked tree nodes. */
    ArrayList<IndexingTreeNode> m_NodesStack = new ArrayList<IndexingTreeNode>();
    /** List of stacked distances from centres of tree nodes corresponding to the elements in m_NodesStack. */
    double[] m_DistStack = new double[256];
    /**
     * List of stacked distances, each position corresponds
     * to the centre of the node placed at the same position in the stack of nodes m_NodesStack
     * and represents the distance between the brother of the node from m_NodesStack
     * closest to a query data object and a query object.
     */
    double[][] m_PruningDistStack = new double[256][];

    /**
     * Returns the average number of distance calculations.
     *
     * @return Average number of distance calculations.
     */
    public double getAverageNoOfDistCalculations()
    {
        if (m_nCallsCounter==0) return 0;
        return m_nDistCalculationsCounter/((double)m_nCallsCounter);
    }

    /**
     * Returns the standard deviation of the number of distance calculations.
     *
     * @return Standard deviation of the number of distance calculations.
     */
    public double getStdDevNoOfDistCalculations()
    {
        if (m_nCallsCounter==0) return 0;
        return Math.sqrt(m_nSquareDistCalculationsCounter/((double)m_nCallsCounter)-getAverageNoOfDistCalculations()*getAverageNoOfDistCalculations());
    }

    /**
     * Allocates more positions in the stack of distances.
     */
    private void enlargeDistStack()
    {
        double[] newDistStack = new double[2*m_DistStack.length];
        for (int d = 0; d < m_DistStack.length; d++) newDistStack[d] = m_DistStack[d];
        m_DistStack = newDistStack;
    }

    /**
     * Allocates more positions in the stack of pruning distances.
     */
    private void enlargePruningDistStack()
    {
        double[][] newDistStack = new double[2*m_PruningDistStack.length][];
        for (int d = 0; d < m_PruningDistStack.length; d++) newDistStack[d] = m_PruningDistStack[d];
        m_PruningDistStack = newDistStack;
    }

    /**
     * Returns noOfNearest data objects from the tree hierarchyRoot nearest to dObj.
     * If the noOfNearest-th and a number of next data objects
     * are equally distant to dObj, then all that have the same distance
     * to dObj, are added to the return array.
     *
     * @param metr          Metric used to measure distance between data objects.
     * @param dObj          Data object that is the reference for neighbours.
     * @param hierarchyRoot Binary tree indexing data objects to be searched.
     * @param noOfNearest   Number of nearest neighbours to be returned.
     * @return              Array of nearest neighbours sorted ascending according to distance to dObj.
     */
    public Neighbour[] getKNearest(Metric metr, DoubleData dObj, IndexingTreeNode hierarchyRoot, int noOfNearest)
    {
        m_OrderedNearest.clear();
        m_NodesStack.clear();
        m_DistStack[m_NodesStack.size()] = metr.dist(dObj, hierarchyRoot.getCenter());
        m_PruningDistStack[m_NodesStack.size()] = new double[1];
        m_PruningDistStack[m_NodesStack.size()][0] = m_DistStack[m_NodesStack.size()];
        m_NodesStack.add(hierarchyRoot);
        int distCalculationsCounter = 1;
        while (m_NodesStack.size() > 0)
        {
            IndexingTreeNode cl = m_NodesStack.remove(m_NodesStack.size()-1);
            double lastNearestDist = 0.0;
            boolean check = true;
            if (m_OrderedNearest.size() >= noOfNearest)
            {
                lastNearestDist = m_OrderedNearest.last().dist();
                if (m_DistStack[m_NodesStack.size()] > cl.getRadius()+lastNearestDist) check = false;
                if (check)
                {
                    int nearestSubnode = 0;
                    for (int subnode = 1; subnode < m_PruningDistStack[m_NodesStack.size()].length; subnode++)
                        if (m_PruningDistStack[m_NodesStack.size()][subnode] < m_PruningDistStack[m_NodesStack.size()][nearestSubnode])
                            nearestSubnode = subnode;
                    if (m_DistStack[m_NodesStack.size()]-lastNearestDist
                        > m_PruningDistStack[m_NodesStack.size()][nearestSubnode]+lastNearestDist) check = false;
                }
            }
            if (check)
                if (cl.isElementary())
                {
                    getKNearest(metr, dObj, ((IndexingTreeLeaf)cl).getObjects(), noOfNearest, m_OrderedNearest);
                    distCalculationsCounter += cl.size();
                }
                else
                {
                    IndexingTreeFork forkCl = (IndexingTreeFork)cl;
                    double[] subnodesDistances = new double[forkCl.noOfChildren()];
                    for (int subnode = 0; subnode < subnodesDistances.length; subnode++)
                        subnodesDistances[subnode] = metr.dist(dObj, forkCl.getChildNode(subnode).getCenter());
                    boolean[] stacked = new boolean[forkCl.noOfChildren()];
                    for (int sortedSubnode = 0; sortedSubnode < subnodesDistances.length; sortedSubnode++)
                    {
                        int furthestSubnode = -1;
                        for (int subnode = 0; subnode < subnodesDistances.length; subnode++)
                            if (!stacked[subnode] && (furthestSubnode == -1 || subnodesDistances[subnode] >= subnodesDistances[furthestSubnode]))
                                furthestSubnode = subnode;
                        if (m_NodesStack.size() >= m_DistStack.length) enlargeDistStack();
                        m_DistStack[m_NodesStack.size()] = subnodesDistances[furthestSubnode];
                        if (m_NodesStack.size() >= m_PruningDistStack.length) enlargePruningDistStack();
                        m_PruningDistStack[m_NodesStack.size()] = subnodesDistances;
                        m_NodesStack.add(forkCl.getChildNode(furthestSubnode));
                        stacked[furthestSubnode] = true;
                    }
                    distCalculationsCounter += forkCl.noOfChildren();
                }
        }
        m_nCallsCounter++;
        m_nDistCalculationsCounter += distCalculationsCounter;
        m_nSquareDistCalculationsCounter += distCalculationsCounter*distCalculationsCounter;
        Neighbour[] nearest = m_OrderedNearest.toArray(new Neighbour[0]);
        return nearest;
    }
}

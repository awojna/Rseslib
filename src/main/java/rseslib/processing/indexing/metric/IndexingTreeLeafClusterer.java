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


package rseslib.processing.indexing.metric;

import rseslib.structure.index.metric.IndexingTreeFork;
import rseslib.structure.index.metric.IndexingTreeLeaf;

/**
 * Clusterer divides a set of data objects
 * into a number of clusters.
 *
 * @author      Arkadiusz Wojna
 */
public interface IndexingTreeLeafClusterer
{
    /**
     * Clusters data objects of a leaf node in an indexing tree.
     *
     * @param leaf                  Leaf node to be clustered.
     * @return                      Clustered node.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public abstract IndexingTreeFork cluster(IndexingTreeLeaf leaf) throws InterruptedException;

    /**
     * Returns the average number of iterations.
     *
     * @return Average number of iterations.
     */
    public abstract double getAverageNoOfIterations();

    /**
     * Returns the maximum number of iterations.
     *
     * @return Maximum number of iterations.
     */
    public abstract double getMaximumNoOfIterations();

    /**
     * Returns the number of distance comparisions in the last call of the clustering procedure.
     *
     * @return Number of distance comparisions in the last call of the clustering procedure.
     */
    public abstract int getNoOfDistOper();
}

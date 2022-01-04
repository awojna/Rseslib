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


package rseslib.processing.indexing.metric;

import java.util.ArrayList;
import java.util.Random;

import rseslib.structure.data.DoubleData;
import rseslib.structure.index.metric.IndexingTreeFork;
import rseslib.structure.index.metric.IndexingTreeLeaf;
import rseslib.structure.metric.Metric;

/**
 * K means clusterer with initial seeds selection procedure given as a parameter.
 *
 * @author      Arkadiusz Wojna
 */
public class KApproxCentersIndexingTreeLeafClusterer implements IndexingTreeLeafClusterer
{
    /** Maximal number of tries in splitting procedure. */
    private static final int MAX_NO_OF_ITERATIONS = 1000;
    /** Generator of random numbers. */
    private static final Random RANDOM_NUMBER_GENERATOR = new Random();

    /** Selector of initial seeds for clustering. */
    private SeedsSelectorWithCenters m_SeedsSelector;
    /** Number of seeds used to cluster data. */
    private int m_nNoOfSeeds;
    /** Counter for the number of distance comparisions. */
    private int m_nDistOperCounter = 0;
    /** Counter for the number of this procedure calls. */
    private int m_nCallsCounter = 0;
    /** The total number of performed iterations. */
    private double m_nIterationsCounter = 0;
    /** The maximal number of iterations performed for a single k-means call. */
    private int m_nMaxIterations = 0;

    /**
     * Constructor.
     *
     * @param seedsSelector Selector of initial seeds for clustering algorithm.
     * @param noOfSeeds     Number of seeds used to cluster data.
     */
    public KApproxCentersIndexingTreeLeafClusterer(SeedsSelectorWithCenters seedsSelector, int noOfSeeds)
    {
        m_SeedsSelector = seedsSelector;
        m_nNoOfSeeds = noOfSeeds;
    }

    /**
     * Returns the average number of iterations.
     *
     * @return Average number of iterations.
     */
    public double getAverageNoOfIterations()
    {
        if (m_nCallsCounter==0) return 0;
        return m_nIterationsCounter/((double)m_nCallsCounter);
    }

    /**
     * Returns the maximum number of iterations.
     *
     * @return Maximum number of iterations.
     */
    public double getMaximumNoOfIterations()
    {
        return m_nMaxIterations;
    }

    /**
     * Clusters data objects of a leaf node in an indexing tree
     * using objects as cluster centers instead of means.
     *
     * @param leaf                  Leaf node to be clustered.
     * @return                      Clustered node.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public IndexingTreeFork cluster(IndexingTreeLeaf leaf) throws InterruptedException
    {
        m_nDistOperCounter = 0;
        Metric metric = leaf.getMetric();
        DoubleData[] objects = leaf.getObjects();
        DoubleData[] seeds = m_SeedsSelector.getSeeds(objects, leaf.getCenter(), metric, m_nNoOfSeeds);
        m_nDistOperCounter += m_SeedsSelector.getNoOfDistOper();
        DoubleData[] centers = new DoubleData[seeds.length];
        double[] distances = new double[centers.length];
        ArrayList<DoubleData>[] subnodeObjects = new ArrayList[centers.length];
        DoubleData[] prevCenters = new DoubleData[centers.length];
        DoubleData[][] subnodes = new DoubleData[centers.length][];
        for (int seed = 0; seed < centers.length; seed++)
        {
            centers[seed] = seeds[seed];
            subnodeObjects[seed] = new ArrayList<DoubleData>();
        }
        int iter = 0;
        boolean cont = true;
        while (cont && iter < MAX_NO_OF_ITERATIONS)
        {
            iter++;
            for (int cl = 0; cl < subnodeObjects.length; cl++)
                subnodeObjects[cl].clear();
            for (int obj = 0; obj < objects.length; obj++)
            {
                int nearestMean = -1;
                for (int cl = 0; cl < subnodeObjects.length; cl++)
                {
                    distances[cl] = metric.dist(objects[obj], centers[cl]);
                    m_nDistOperCounter++;
                    if (nearestMean==-1 || distances[cl] < distances[nearestMean]) nearestMean = cl;
                }
                subnodeObjects[nearestMean].add(objects[obj]);
            }
            for (int cl = 0; cl < subnodeObjects.length; cl++)
                if (subnodeObjects[cl].size()==0)
                {
                    int nearestCl = -1;
                    int nearestObjInd = -1;
                    double nearestDist = Double.MAX_VALUE;
                    for (int cl2 = 0; cl2 < subnodeObjects.length; cl2++)
                        if (subnodeObjects[cl2].size()>1)
                            for (int obj = 0; obj < subnodeObjects[cl2].size(); obj++)
                            {
                                double dist;
                                dist = metric.dist((DoubleData)subnodeObjects[cl2].get(obj), centers[cl]);
                                m_nDistOperCounter++;
                                if (dist < nearestDist)
                                {
                                    nearestCl = cl2;
                                    nearestObjInd = obj;
                                    nearestDist = dist;
                                }
                            }
                    subnodeObjects[cl].add(subnodeObjects[nearestCl].remove(nearestObjInd));
                }
            cont = false;
            for (int cl = 0; cl < subnodeObjects.length; cl++)
            {
                prevCenters[cl] = centers[cl];
                subnodes[cl] = (DoubleData[])subnodeObjects[cl].toArray(new DoubleData[0]);
                centers[cl] = selectCenter(subnodes[cl], prevCenters[cl], metric);
                if (!cont)
                {
                	if (metric.dist(centers[cl], prevCenters[cl]) > 0) cont = true;
                	m_nDistOperCounter++;
                }
            }
        }
        m_nCallsCounter++;
        m_nIterationsCounter += iter;
        if (iter > m_nMaxIterations) m_nMaxIterations = iter;
       return new IndexingTreeFork(null, metric, objects, leaf.getCenter(), subnodes, prevCenters);
    }

    /**
     * Selects an approximate center from a set of data objects.
     *
     * @param objects    Set of data objects.
     * @param prevCenter The previously selected center of a cluster
     *                   in an iterative algorithm.
     * @return           Approximate center of the set of data objects.
     */
    private static DoubleData selectCenter(DoubleData[] objects, DoubleData prevCenter, Metric metric)
    {
        double sampleSize = Math.sqrt(objects.length);
        if (sampleSize < 3)
           if (objects.length < 3) sampleSize = 1.0;
           else sampleSize = 3.0;
        else if (sampleSize > (double)(int)sampleSize) sampleSize = (int)sampleSize+1;
        DoubleData[] sample = new DoubleData[(int)sampleSize];
        sample[0] = prevCenter;
        for (int obj = 1; obj < sample.length; obj++)
        {
            boolean occured = true;
            while (occured)
            {
                sample[obj] = objects[RANDOM_NUMBER_GENERATOR.nextInt(objects.length)];
                occured = false;
                for (int prev = 0; !occured && prev < obj; prev++)
                    if (sample[obj]==sample[prev]) occured = true;
            }
        }
        int bestCenter = -1;
        double bestVarianceSum = 0.0;
        for (int obj = 0; obj < sample.length; obj++)
        {
            double varianceSum = 0.0;
            for (int obj2 = 0; obj2 < sample.length; obj2++)
                if (obj != obj2)
                {
                    double dist = metric.dist(sample[obj], sample[obj2]);
                    varianceSum += dist*dist;
                }
            if (bestCenter==-1 || varianceSum < bestVarianceSum)
            {
                bestCenter = obj;
                bestVarianceSum = varianceSum;
            }
        }
        return sample[bestCenter];
    }

    /**
     * Selects an approximate center from a set of data objects.
     *
     * @param objects    Set of data objects.
     * @return           Approximate center of the set of data objects.
     */
    public static DoubleData selectCenter(DoubleData[] objects, Metric metric)
    {
    	return selectCenter(objects, objects[RANDOM_NUMBER_GENERATOR.nextInt(objects.length)], metric);
    }
    
    /**
     * Returns the number of distance comparisions in the last call of the clustering procedure.
     *
     * @return Number of distance comparisions in the last call of the clustering procedure.
     */
    public int getNoOfDistOper()
    {
        return m_nDistOperCounter;
    }
}

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


package rseslib.processing.searching.metric;

import java.util.Properties;

import rseslib.structure.data.DoubleData;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.Neighbour;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Provides vicinity of data objects extracted from a given data collection.
 * Data collection is given in the form of an indexing tree
 * together with a metric defining distances between data objects.
 * In case of subsequent queries for the vicinity of same data object
 * this provider is optimized:
 * it remembers the last result and uses it
 * if the requested number of neighbours is smaller or equal
 * than in the previous query.
 *
 * @author      Arkadiusz Wojna
 */
public class IndexingTreeVicinityProvider extends Configuration implements VicinityProvider
{
    Metric m_Metric;
    /** Data set where neighbours are searched for. */
    IndexingTreeNode m_Tree;
    /** Provider of nearest neighbours. */
    TreeSetBasedNearestNeighboursProviderFromTree m_NeighboursProvider;
    /** Minimum number of neighbours extracted. */
    int m_nMinNumberOfNeighbours = 0;
    /** The data object for that a vicinity was searched last time. */
    DoubleData m_LastData;
    /** The number of nearest for that a vicinity was searched last time. */
    int m_LastNoOfNearest = 0;
    /** Vicinity of the data object for that a vicinity was searched last time. */
    Neighbour[] m_LastVicinity;

    /**
     * Constructor.
     *
     * @param prop          Map between property names and property values.
     * @param metric        Metric used as a measure of distance between data objects.
     * @param indexingTree  Data set where neighbours are searched for.
     */
    public IndexingTreeVicinityProvider(Properties prop, Metric metric, IndexingTreeNode indexingTree) throws PropertyConfigurationException
    {
        super(prop);
        m_Metric = metric;
        m_Tree = indexingTree;
        m_NeighboursProvider = new TreeSetBasedNearestNeighboursProviderFromTree();
    }

    /**
     * Sets the minimum number of neighbors that are extracted.
     * If a user requests less neighbours 
     * then this minimum number of neighbours are extracted
     * but the user obtains the requested number.
     * 
     * @param min	Minimum number of neighbours extracted.
     */
    public void setMinNumberOfNeighbours(int min)
    {
    	m_nMinNumberOfNeighbours = min;
    }
    
    /**
     * Provides nearest neighbours of a given data object
     * and sorts them according to the growing distance.
     * In case when distance between data object is not unique
     * the returned number of neighbours may be larger
     * than the value of the parameter noOfNeighbours.
     *
     * @param ind           Data object index in an array.
     * @param dObj          Data object to be used for searching vicinity.
     * @param noOfNearest   Number of nearest neighbours to be returned.
     * @return              Vicinity of a given data object.
     */
    public Neighbour[] getVicinity(DoubleData dObj, int noOfNearest)
    {
        if (dObj!=m_LastData || noOfNearest>m_LastNoOfNearest)
        {
            m_LastData = dObj;
            if (noOfNearest >= m_nMinNumberOfNeighbours) m_LastNoOfNearest = noOfNearest;
            else m_LastNoOfNearest = m_nMinNumberOfNeighbours;
            Neighbour[] nearest = m_NeighboursProvider.getKNearest(m_Metric, dObj, m_Tree, m_LastNoOfNearest);
            m_LastVicinity = new Neighbour[nearest.length+1];
            for (int n = 0; n < nearest.length; n++) m_LastVicinity[n+1] = nearest[n];
        }
        if (noOfNearest<m_LastNoOfNearest && noOfNearest<m_LastVicinity.length-1)
        {
        	int arrayLength = noOfNearest+1;
        	while (arrayLength < m_LastVicinity.length
        			&& m_LastVicinity[arrayLength].dist()==m_LastVicinity[noOfNearest].dist())
        		arrayLength++;
        	Neighbour[] nearest = new Neighbour[arrayLength];
            for (int n = 1; n < nearest.length; n++)
            	nearest[n] = m_LastVicinity[n];
            return nearest;
        }
        return m_LastVicinity;
    }

    /**
     * Returns the average number of distance calculations.
     *
     * @return Average number of distance calculations.
     */
    public double getAverageNoOfDistCalculations()
    {
        return m_NeighboursProvider.getAverageNoOfDistCalculations();
    }

    /**
     * Returns the standard deviation of the number of distance calculations.
     *
     * @return Standard deviation of the number of distance calculations.
     */
    public double getStdDevNoOfDistCalculations()
    {
        return m_NeighboursProvider.getStdDevNoOfDistCalculations();
    }
}

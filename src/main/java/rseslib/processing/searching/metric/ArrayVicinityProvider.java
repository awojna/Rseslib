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


package rseslib.processing.searching.metric;

import java.util.ArrayList;
import java.util.TreeSet;

import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.metric.NeighbourComparator;

/**
 * Provides vicinity of data objects extracted from a given data collection.
 * Data collection is given in the form of an array list
 * together with a metric defining distances between data objects.
 *
 * @author      Arkadiusz Wojna
 */
public class ArrayVicinityProvider implements VicinityProvider
{
	/** Metric. */
    Metric m_Metric;
    /** Objects to be searched. */
    DoubleData[] m_Objects;
    /** Provider of nearest neighbours. */
    NearestNeighboursProvider m_NeighboursProvider;

    /**
     * Constructor.
     */
    public ArrayVicinityProvider(Metric metric, ArrayList<DoubleData> objects)
    {
        m_Metric = metric;
        m_Objects = objects.toArray(new DoubleData[0]);
        m_NeighboursProvider = new NearestNeighboursProvider();
    }

    /**
     * Provides nearest neighbours of a given data object
     * and sorts them according to the growing distance.
     * In case when distance between data object is not unique
     * the returned number of neighbours may be larger
     * than the value of the parameter noOfNeighbours.
     *
     * @param dObj          Data object to be used for searching vicinity.
     * @param noOfNearest   Number of nearest neighbours to be returned.
     * @return              Vicinity of a given data object.
     */
    public Neighbour[] getVicinity(DoubleData dObj, int noOfNearest)
    {
        TreeSet<Neighbour> nearests = new TreeSet<Neighbour>(new NeighbourComparator());
        m_NeighboursProvider.getKNearest(m_Metric, dObj, m_Objects, noOfNearest, nearests);
        Neighbour[] result = nearests.toArray(new Neighbour[0]);
        Neighbour[] result_shifted = new Neighbour[result.length+1];
        for (int n = 0; n < result.length; n++)
        	result_shifted[n+1] = result[n];        
        return result_shifted;
    }

    /**
     * Returns the average number of distance calculations.
     *
     * @return Average number of distance calculations.
     */
    public double getAverageNoOfDistCalculations()
    {
    	// TODO: implement
        return 0;
    }

    /**
     * Returns the standard deviation of the number of distance calculations.
     *
     * @return Standard deviation of the number of distance calculations.
     */
    public double getStdDevNoOfDistCalculations()
    {
    	// TODO: implement
        return 0;
    }
}

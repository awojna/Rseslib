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


package rseslib.processing.searching.metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;
import java.util.Iterator;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.Neighbour;

/**
 * The method extracting nearest neighbours of a data object
 * from a given array of data objects.
 *
 * @author      Arkadiusz Wojna
 */
public class NearestNeighboursProvider
{
    /** Generator of random numbers. */
    static final Random RANDOM_GENERATOR = new Random();
    /** Neighbours counter. */
    private int m_Counter = 0;

    /**
     * Returns noOfNearest data objects from objectArray nearest to dObj.
     * If the noOfNearest-th and a number of next data objects
     * are equally distant to dObj, then all that have the same distance
     * to dObj, are added to the return array.
     *
     * @param metr        Metric used to measure distance between data objects.
     * @param dObj        Data object that is the reference for neighbours.
     * @param objectArray Array of data objects to be searched.
     * @param noOfNearest Number of nearest neighbours to be returned.
     * @return            Array of nearest neighbours to dObj.
     */
    public DoubleData[] getKNearest(Metric metr, DoubleData dObj, DoubleData[] objectArray, int noOfNearest)
    {
        if (noOfNearest < objectArray.length)
        {
            // podzial tablicy objectArray
            int left = 0, right = objectArray.length - 1;
            while (left < right)
            {
                int p = left + RANDOM_GENERATOR.nextInt(right-left+1);
                DoubleData tmpObj = objectArray[right];
                objectArray[right] = objectArray[p];
                objectArray[p] = tmpObj;
                int l = left, r = right - 1;
                double dist = metr.dist(objectArray[right], dObj);
                while (l < r)
                    if (dist >= metr.dist(objectArray[l], dObj)) l++;
                    else if (dist < metr.dist(objectArray[r], dObj)) r--;
                    else
                    {
                        tmpObj = objectArray[l];
                        objectArray[l] = objectArray[r];
                        objectArray[r] = tmpObj;
                        l++;
                        if (l < r) r--;
                    }
                if (dist < metr.dist(objectArray[l], dObj))
                {
                    tmpObj = objectArray[l];
                    objectArray[l] = objectArray[right];
                    objectArray[right] = tmpObj;
                }
                if (l < noOfNearest) left = l + 1;
                else right = l;
            }
        }
        else noOfNearest = objectArray.length;

        // przepisanie najblizszych do nowej tablicy
        double maxDist = 0;
        for (int i = 0; i < noOfNearest; i++)
        {
            double dist = metr.dist(dObj, objectArray[i]);
            if (dist > maxDist) maxDist = dist;
        }
        int returnNoOfNearest = noOfNearest;
        for (int i = noOfNearest; i < objectArray.length; i++)
            if (metr.dist(dObj, objectArray[i]) == maxDist) returnNoOfNearest++;
        DoubleData[] tableOfNearest = new DoubleData[returnNoOfNearest];
        for (int i = 0; i < noOfNearest; i++) tableOfNearest[i] = objectArray[i];
        int nextIndex = noOfNearest;
        for (int i = noOfNearest; i < objectArray.length && nextIndex < tableOfNearest.length; i++)
            if (metr.dist(dObj, objectArray[i]) == maxDist) tableOfNearest[nextIndex++] = objectArray[i];
        Arrays.sort(tableOfNearest);
        return tableOfNearest;
    }

    /**
     * Returns noOfNearest data objects nearest to dObj
     * from both the array objectArray and the list nearest.
     * It scans the array and updates the list nearest
     * always when a data object near enough is found in the array.
     * If the noOfNearest-th and a number of next data objects
     * are equally distant to dObj, then all that have the same distance
     * to dObj, are added to the return array.
     *
     * @param metr        Metric used to measure distance between data objects.
     * @param dObj        Data object that is the reference for neighbours.
     * @param objectArray Array of data objects to be searched.
     * @param noOfNearest Number of nearest neighbours to be returned.
     * @param nearest     List of nearest neighbours to be updated.
     */
    public void getKNearest(Metric metr, DoubleData dObj, DoubleData[] objectArray, int noOfNearest, ArrayList<Neighbour> nearest)
    {
        for (int obj = 0; obj < objectArray.length; obj++)
        {
            double dist = metr.dist(dObj, objectArray[obj]);
            if (nearest.size() < noOfNearest || dist <= nearest.get(nearest.size()-1).dist())
            {
                Neighbour neighb = new Neighbour((DoubleDataWithDecision)objectArray[obj], dist, m_Counter++);
                nearest.add(neighb);
                int near = nearest.size()-1;
                while (near > 0 && dist < nearest.get(near-1).dist())
                {
                    nearest.set(near, nearest.get(near-1));
                    near--;
                }
                nearest.set(near, neighb);
                if (noOfNearest < nearest.size() && nearest.get(noOfNearest-1).dist() < nearest.get(noOfNearest).dist())
                    while (noOfNearest < nearest.size())
                        nearest.remove(nearest.size()-1);
            }
        }
    }

    /**
     * Returns noOfNearest data objects nearest to dObj
     * from both the array objectArray and the list nearest.
     * It scans the array and updates the list nearest
     * always when a data object near enough is found in the array.
     * If the noOfNearest-th and a number of next data objects
     * are equally distant to dObj, then all that have the same distance
     * to dObj, are added to the return array.
     *
     * @param metr        Metric used to measure distance between data objects.
     * @param dObj        Data object that is the reference for neighbours.
     * @param objectArray Array of data objects to be searched.
     * @param noOfNearest Number of nearest neighbours to be returned.
     * @param nearest     Set of nearest neighbours to be updated.
     */
    public void getKNearest(Metric metr, DoubleData dObj, DoubleData[] objectArray, int noOfNearest, TreeSet<Neighbour> nearest)
    {
        for (int obj = 0; obj < objectArray.length; obj++)
        {
            double dist = metr.dist(dObj, objectArray[obj]);
            Neighbour last = null;
            if (nearest.size() > 0) last = (Neighbour)nearest.last();
            if (nearest.size() < noOfNearest || dist <= last.dist())
            {
                nearest.add(new Neighbour(((DoubleDataWithDecision)objectArray[obj]), dist, m_Counter++));
                if (nearest.size() > noOfNearest)
                {
                	int lastCount = 0;
                	Iterator<Neighbour> it = nearest.descendingIterator();
                	while (it.hasNext() && it.next().dist() == last.dist())
                		lastCount++;
                	if (nearest.size() >= noOfNearest + lastCount)
                		for (int n = 0; n < lastCount; n++)
                			nearest.pollLast();
                }
            }
        }
    }
}

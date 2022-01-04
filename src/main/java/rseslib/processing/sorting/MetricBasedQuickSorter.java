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


package rseslib.processing.sorting;

import java.util.Random;

import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Metric;

/**
 * Implementation of QuickSort for sorting data objects
 * according to distance from a fixed data object
 * measured with a given metric.
 *
 * @author      Arkadiusz Wojna
 */
public class MetricBasedQuickSorter implements MetricBasedSorter
{
    /** Generator of random numbers. */
    static final Random RANDOM_GENERATOR = new Random();

    /**
     * Sorts data objects according to distance
     * from the data object dObj
     * measured with the metric metr.
     * It sorts objects from the array dObject
     * only with indices between left and right inclusively.
     *
     * @param metr     Metric used to measure distance.
     * @param dObj     Data object used as a reference point.
     * @param dObjects Array of data objects to be sorted.
     * @param left     Index of the left end of the sorted range of objects.
     * @param right    Index of the right end of the sorted range of objects.
     */
    private void sort(Metric metr, DoubleData dObj, DoubleData[] dObjects, int left, int right)
    {
        int p = left + RANDOM_GENERATOR.nextInt(right-left+1);
        DoubleData tmpObj = dObjects[right];
        dObjects[right] = dObjects[p];
        dObjects[p] = tmpObj;
        int l = left, r = right - 1;
        double dist = metr.dist(dObjects[right], dObj);
        while (l < r)
            if (dist >= metr.dist(dObjects[l], dObj)) l++;
            else if (dist < metr.dist(dObjects[r], dObj)) r--;
            else
            {
                tmpObj = dObjects[l];
                dObjects[l] = dObjects[r];
                dObjects[r] = tmpObj;
                l++;
                if (l < r) r--;
            }
        if (dist < metr.dist(dObjects[l], dObj))
        {
            tmpObj = dObjects[l];
            dObjects[l] = dObjects[right];
            dObjects[right] = tmpObj;
        }
        if (left < l) sort(metr, dObj, dObjects, left, l);
        if (l + 1 < right) sort(metr, dObj, dObjects, l + 1, right);
    }

    /**
     * Sorts data objects
     * according to distance from the data object dObj
     * measured with the metric metr.
     *
     * @param metr     Metric used to measure distance.
     * @param dObj     Data object used as a reference point.
     * @param dObjects Array of data objects to be sorted.
     */
    public void sort(Metric metr, DoubleData dObj, DoubleData[] dObjects)
    {
        sort(metr, dObj, dObjects, 0, dObjects.length-1);
    }
}
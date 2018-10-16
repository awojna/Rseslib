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


package rseslib.processing.sorting;

import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Metric;

/**
 * Interface for methods sorting data objects
 * according to distance from a fixed data object
 * measured with a given metric.
 *
 * @author      Arkadiusz Wojna
 */
public interface MetricBasedSorter
{
    /**
     * Sorts data objects
     * according to distance from the data object dObj
     * measured with the metric metr.
     *
     * @param metr     Metric used to measure distance.
     * @param dObj     Data object used as a reference point.
     * @param dObjects Array of data objects to be sorted.
     */
    public abstract void sort(Metric metr, DoubleData dObj, DoubleData[] dObjects);
}
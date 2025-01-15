/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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

import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Neighbour;

/**
 * Provides vicinity of data objects
 * extracted from a given data collection.
 *
 * @author      Arkadiusz Wojna
 */
public interface VicinityProvider
{
    /**
     * Returns the average number of distance calculations.
     *
     * @return Average number of distance calculations.
     */
    public abstract double getAverageNoOfDistCalculations();

    /**
     * Returns the standard deviation of the number of distance calculations.
     *
     * @return Standard deviation of the number of distance calculations.
     */
    public abstract double getStdDevNoOfDistCalculations();

    /**
     * Provides nearest neighbours of a given data object
     * and sorts them according to the growing distance.
     *
     * @param dObj          Data object to be used for searching vicinity.
     * @param noOfNearest   Number of nearest neighbours to be returned.
     * @return              Vicinity of a given data object.
     */
    public abstract Neighbour[] getVicinity(DoubleData dObj, int noOfNearest);
}

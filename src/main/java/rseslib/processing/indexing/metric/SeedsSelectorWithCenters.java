/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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

import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Metric;

/**
 * Selector of seeds from a set of data objects that enable to calculate means.
 *
 * @author      Arkadiusz Wojna
 */
public interface SeedsSelectorWithCenters
{
    /**
     * Selects a number of seeds from a collection of data objects.
     *
     * @param objects   Array of data objects to be seeded.
     * @param mean      Mean of the set of data objects from objects.
     * @param metric    Metric used to measure distance among data objects.
     * @param noOfSeeds Number of seeds to be returned.
     * @return          Array of selected seeds.
     */
    public abstract DoubleData[] getSeeds(DoubleData[] objects, DoubleData mean, Metric metric, int noOfSeeds);

    /**
     * Returns the number of distance comparisions in the last call of the seeding procedure.
     *
     * @return Number of distance comparisions in the last call of the seeding procedure.
     */
    public abstract int getNoOfDistOper();
}

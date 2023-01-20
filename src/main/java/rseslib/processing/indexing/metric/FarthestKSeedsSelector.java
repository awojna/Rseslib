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
 * Selector of k seeds from a collection of data objects.
 * The first one is the farthest data object from the mean
 * and each next one is the farthest from all previously selected.
 *
 * @author      Arkadiusz Wojna
 */
public class FarthestKSeedsSelector implements SeedsSelectorWithCenters
{
    /** Counter for the number of distance comparisions. */
    private int m_nDistOperCounter = 0;

    /**
     * Selects a number of seeds from a collection of data objects.
     * The first one is the farthest data object from the mean
     * and each next one is the farthest from all previously selected.
     *
     * @param objects Array of data objects to be seeded.
     * @param mean    Mean of the set of data objects from objects.
     * @param metric  Metric used to measure distance among data objects.
     * @param noOfSeeds Number of seeds to be returned.
     * @return        Array of selected seeds.
     */
    public DoubleData[] getSeeds(DoubleData[] objects, DoubleData mean, Metric metric, int noOfSeeds)
    {
        m_nDistOperCounter = 0;
        DoubleData[] seeds;
        if (noOfSeeds < objects.length)
        {
            seeds = new DoubleData[noOfSeeds];
            int farthestObj = -1;
            double farthestDist = 0.0;
            for (int obj = 0; obj < objects.length; obj++)
            {
                double dist = metric.dist(objects[obj], mean);
                m_nDistOperCounter++;
                if (farthestObj == -1 || dist > farthestDist)
                {
                    farthestObj = obj;
                    farthestDist = dist;
                }
            }
            seeds[0] = objects[farthestObj];
            for (int seed = 1; seed < seeds.length; seed++)
            {
                farthestObj = -1;
                farthestDist = 0.0;
                for (int obj = 0; obj < objects.length; obj++)
                {
                    double minDist = Double.MAX_VALUE;
                    DoubleData dObj = objects[obj];
                    for (int s = 0; s < seed; s++)
                        if (dObj == seeds[s]) minDist = 0.0;
                        else
                        {
                            double dist = metric.dist(seeds[s], dObj);
                            m_nDistOperCounter++;
                            if (dist < minDist) minDist = dist;
                        }
                    if (farthestObj == -1 || minDist > farthestDist)
                    {
                        farthestObj = obj;
                        farthestDist = minDist;
                    }
                }
                seeds[seed] = objects[farthestObj];
            }
        }
        else
        {
            seeds = new DoubleData[objects.length];
            for (int seed = 0; seed < seeds.length; seed++) seeds[seed] = objects[seed];
        }
        return seeds;
    }

    /**
     * Returns the number of distance comparisions in the last call of the seeding procedure.
     *
     * @return Number of distance comparisions in the last call of the seeding procedure.
     */
    public int getNoOfDistOper()
    {
        return m_nDistOperCounter;
    }
}

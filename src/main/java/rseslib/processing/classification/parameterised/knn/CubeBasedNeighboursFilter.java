/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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


package rseslib.processing.classification.parameterised.knn;


import rseslib.structure.data.DoubleData;
import rseslib.structure.function.booleanval.BooleanFunction;
import rseslib.structure.function.booleanval.MixedCityAndMetricCube;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.metric.AbstractWeightedMetric;

/**
 * Filter neighbours of an object using cubes.
 * A neighbour is rejected if the cube
 * spanned by the object and the neighbour
 * contains another neighbour with different decision.
 *
 * @author      Arkadiusz Wojna
 */
public class CubeBasedNeighboursFilter
{
    /** Metric used to measure distance and to span cubes on objects. */
    private AbstractWeightedMetric m_Metric;

    public CubeBasedNeighboursFilter(AbstractWeightedMetric metric)
    {
        m_Metric = metric;
    }

    /**
     * Marks consistency of neighbours with a given object dObj.
     *
     * @param dObj          Given object.
     * @param neighbours    Nearest neighbours of the object dObj.
     */
    public void markConsistency(DoubleData dObj, Neighbour[] neighbours)
    {
        for (int obj1 = 1; obj1 < neighbours.length; obj1++)
        {
            BooleanFunction cube = new MixedCityAndMetricCube(dObj, neighbours[obj1].neighbour(), m_Metric);
            neighbours[obj1].m_bConsistent = true;
            for (int obj2 = 1; neighbours[obj1].m_bConsistent && (obj2 <= obj1 || (obj2 < neighbours.length && neighbours[obj2].dist() == neighbours[obj1].dist())); obj2++)
                if (neighbours[obj1].neighbour().getDecision()!=neighbours[obj2].neighbour().getDecision() && cube.booleanVal(neighbours[obj2].neighbour()))
                    neighbours[obj1].m_bConsistent = false;
        }
    }
}

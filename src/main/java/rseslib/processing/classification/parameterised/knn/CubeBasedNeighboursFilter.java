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


package rseslib.processing.classification.parameterised.knn;


import rseslib.processing.classification.parameterised.SingleParameter;
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
 * @author      Arkadiusz Wojna, Grzegorz Gora
 */
public class CubeBasedNeighboursFilter
{
    /** Metric used to measure distance and to span cubes on objects. */
    private AbstractWeightedMetric m_Metric;
    /** Flag indicating whether objects are transformed or can provide original nominal values. */
    private boolean m_bObjectsTransformed;

    /**
     * Constructor.
     * 
     * @param metric				Metric used to construct cubes.
     * @param objectsTransformed	Flag indicating whether objects are transformed or can provide original nominal values.
     */
    public CubeBasedNeighboursFilter(AbstractWeightedMetric metric, boolean objectsTransformed)
    {
        m_Metric = metric;
        m_bObjectsTransformed = objectsTransformed;
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
            BooleanFunction cube = new MixedCityAndMetricCube(dObj, neighbours[obj1].neighbour(), m_Metric, m_bObjectsTransformed);
            neighbours[obj1].m_bConsistent = true;
            for (int obj2 = 1; neighbours[obj1].m_bConsistent && (obj2 <= obj1 || (obj2 < neighbours.length && neighbours[obj2].dist() == neighbours[obj1].dist())); obj2++)
                if (neighbours[obj1].neighbour().getDecision()!=neighbours[obj2].neighbour().getDecision() && cube.booleanVal(neighbours[obj2].neighbour()))
                    neighbours[obj1].m_bConsistent = false;
        }
    }
    
    /**
     * Marks consistency of neighbours with a given object dObj
     * for different values of the factor transforming the cubes
     * spanned between dObj and neighbours.
     * Consistency on the i-th level corresponds to the i-th value of the factor.
     * The method takes two arrays of transforming values.
     * Selection of the array used to mark consistency for each neighbour
     * depends on whether the neighbour has the same decision
     * as given in the parameter or not.
     *
     * @param dObj				Given object.
     * @param neighbours		Nearest neighbours of the object dObj.
     * @param decision			Decision value.
     * @param valuesForSameDec	Factors transforming the cubes used if the decision of this neighbour is the same as the 'decision' parameter. 
     * @param valuesForOtherDec	Factors transforming the cubes used if the decision of this neighbour is different from the 'decision' parameter.
     */
    public void markConsistencyLevels(DoubleData dObj, Neighbour[] neighbours, double decision, SingleParameter<Double> valuesForSameDec, SingleParameter<Double> valuesForOtherDec)
    {
        for (int obj1 = 1; obj1 < neighbours.length; obj1++)
        {
        	SingleParameter<Double> sValues = (neighbours[obj1].neighbour().getDecision() == decision ? valuesForSameDec : valuesForOtherDec);
            neighbours[obj1].m_bConsistentOnLevel = new boolean[sValues.size()]; // consistency on the i-th level corresponds to the i-th value of the transforming factor
        	for (int index = 0; index < sValues.size(); index++) {
				double sValueForIndex = sValues.getParamValueByIndex(index);
	            neighbours[obj1].m_bConsistentOnLevel[index] = true;
	            if (sValueForIndex < 0.0) continue; // the values of the parameter less than 0 are interpreted as the classical kNN - the cube is always consistent
				// for each sValueForIndex use the corresponding cube
				BooleanFunction cube = new MixedCityAndMetricCube(dObj, neighbours[obj1].neighbour(), m_Metric, sValueForIndex, m_bObjectsTransformed);
	            for (int obj2 = 1; neighbours[obj1].m_bConsistentOnLevel[index] && (obj2 <= obj1 || (obj2 < neighbours.length && neighbours[obj2].dist() == neighbours[obj1].dist())); obj2++)
	                if (neighbours[obj1].neighbour().getDecision()!=neighbours[obj2].neighbour().getDecision() && cube.booleanVal(neighbours[obj2].neighbour()))
	                    neighbours[obj1].m_bConsistentOnLevel[index] = false;
			}
        }
    }
}

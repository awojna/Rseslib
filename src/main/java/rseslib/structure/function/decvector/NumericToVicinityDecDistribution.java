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


package rseslib.structure.function.decvector;

import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.table.IntervalWithDecisionDistribution;
import rseslib.structure.table.VicinityDecisionDistributionsForNumerical;
import rseslib.structure.vector.Vector;

/**
 * The function translating the value of a numeric attribute
 * into decision distribution vector in a vicinity of this value.
 * If the flag m_bConvertToVectDecDifference is off,
 * the function returns the index of the interval
 * containing the attribute value
 * as the representation of the decision vector.
 * If the flag m_bConvertToVectDecDifference is on,
 * it implies that the decision attribute has two values
 * and a decision vector has two coordinates
 * and the function returns the difference between
 * the first and the second coordinates of the decision vector.
 *
 * @author      Arkadiusz Wojna
 */
public class NumericToVicinityDecDistribution extends AttributeDoubleFunction
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/**
     * Array of successive intervals with decision vectors,
     * position 0 contains decision distribution for the null value.
     */
    private IntervalWithDecisionDistribution[] m_DecDistributions = null;
    /**
     * The flag is set to true if the decision attribute has two values
     * and then the function returns the difference between
     * the first and the second coordinates of the decision vector.
     */
    private boolean m_bConvertToVectDecDifference = false;

    /**
     * Constructor extracts decision vectors
     * for succesive value ranges.
     *
     * @param objects 			 Data set that decision vectors for succesive value ranges
     *                			 are calculated from.
     * @param attrIndex          Attribute index.
     * @param minDistrSampleSize The minimal size of the value vicinity taken for computing decision distribution.
     */
    public NumericToVicinityDecDistribution(DoubleDataTable objects, int attrIndex, int minDistrSampleSize)
    {
        super(attrIndex);
        m_DecDistributions = new VicinityDecisionDistributionsForNumerical(objects, attrIndex, minDistrSampleSize).getIntervals();  
        if (objects.attributes().nominalDecisionAttribute().noOfValues()==2) m_bConvertToVectDecDifference = true;
        else m_bConvertToVectDecDifference = false;
    }

    /**
     * Returns decision vectors for succesive attribute value ranges.
     *
     * @return Decision vectors for succesive attribute value ranges.
     */
    public Vector[] getVicinityDecVectors()
    {
    	Vector[] valueDict = new Vector[m_DecDistributions.length];
    	for (int val = 0; val < valueDict.length; val++)
    		valueDict[val] = m_DecDistributions[val].getDecDistribution();
    	return valueDict;
    }

    /**
     * Returns the value of this function for a given attribute value.
     *
     * @param attrVal Attribute value.
     * @return        Value of this function for a given attribute value.
     */
    public double doubleVal(double attrVal)
    {
    	if (Double.isNaN(attrVal)) return Double.NaN;
        int interval = 0;
        if (m_DecDistributions.length > 1)
        {
            int left = 1, right = m_DecDistributions.length-1;
            if (attrVal < m_DecDistributions[left].getRight()) interval = left;
            else if (attrVal > m_DecDistributions[right].getLeft()) interval = right;
            else
            {
                while (left < right)
                {
                    int centre = (left+right)/2;
                    if (m_DecDistributions[centre].compareTo(attrVal)==0) left = right = centre;
                    else if (m_DecDistributions[centre].compareTo(attrVal)<0) left = centre + 1;
                    else right = centre - 1;
                }
                interval = left;
            }
        }
        if (m_bConvertToVectDecDifference)
            return (m_DecDistributions[interval].getDecDistribution().get(0)-m_DecDistributions[interval].getDecDistribution().get(1));
        return interval;
    }
}

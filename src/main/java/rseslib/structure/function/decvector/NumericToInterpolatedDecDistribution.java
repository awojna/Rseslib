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


package rseslib.structure.function.decvector;

import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.DecisionDistributionsForDiscretizedNumerical;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.table.IntervalWithDecisionDistribution;
import rseslib.structure.vector.Vector;

/**
 * The function translating the value of a numeric attribute
 * into decision distribution vector for this value.
 * If the flag m_bConvertToVectDecDifference is off,
 * the function returns the real index of the interval
 * containing the attribute value. The real index corresponds
 * to the interpolation between the interval preceding
 * the attribute value and the interval succeeding
 * the attribute value.
 * If the flag m_bConvertToVectDecDifference is on,
 * it implies that the decision attribute has two values
 * and a decision vector has two coordinates
 * and the function returns the difference between
 * the first and the second coordinates
 * of the interpolated decision vector.
 *
 * @author      Arkadiusz Wojna
 */
public class NumericToInterpolatedDecDistribution extends AttributeDoubleFunction
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/**
     * Array of successive intervals with decision vectors,
     * position 0 contains decision distribution for the null value.
     */
    private IntervalWithDecisionDistribution[] m_DecDistributions = null;
    /** The minimal value of the attribute in a training set. */
    private double m_nMinValue;
    /** Length of a single interval obtained from discretisation. */
    private double m_nIntervalLength;
    /**
     * The flag is set to true if the decision attribute has two values
     * and then the function returns the difference between
     * the first and the second coordinates of the decision vector.
     */
    private boolean m_bConvertToVectDecDifference = false;

    /**
     * Constructor extracts decision vectors
     * for succesive value ranges.
     * The value ranges are obtained by dividing
     * the range of all values into equal intervals.
     *
     * @param objects            Data set that decision vectors for succesive value ranges
     *                           are calculated from.
     * @param attrIndex          Attribute index.
     */
    public NumericToInterpolatedDecDistribution(DoubleDataTable objects, int attrIndex)
    {
        super(attrIndex);
        int noOfIntervals = objects.attributes().nominalDecisionAttribute().noOfValues();
        if (noOfIntervals < 5) noOfIntervals = 5; 
        DecisionDistributionsForDiscretizedNumerical decProbabilities = new DecisionDistributionsForDiscretizedNumerical(objects, attrIndex, noOfIntervals); 
        m_DecDistributions = decProbabilities.getIntervals();
        m_nIntervalLength = decProbabilities.getIntervalLength();
        m_nMinValue = objects.getNumericalStatistics(attrIndex).getMinimum();
        if (objects.attributes().nominalDecisionAttribute().noOfValues()==2)
        	m_bConvertToVectDecDifference = true;
        else m_bConvertToVectDecDifference = false;
    }

    /**
     * Returns decision vectors for succesive intervals.
     *
     * @return Decision vectors for succesive intervals.
     */
    public Vector[] getDiscretisedDecVectors()
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
        double interval = Double.NaN;
        if (m_DecDistributions.length > 1)
            interval = (attrVal - m_nMinValue) / m_nIntervalLength + 0.5;
        if (m_bConvertToVectDecDifference)
        {
            if (Double.isNaN(interval)) interval = 0.0;
            else if (interval <= 0.0 || interval >= (double)m_DecDistributions.length) return 0.0;
            int left = (int)interval;
            if (interval == (double)left)
                return (m_DecDistributions[left].getDecDistribution().get(0)-m_DecDistributions[left].getDecDistribution().get(1));
            double leftWeight = (double)(left + 1) - interval;
            double rightWeight = interval - (double)left;
            if (interval < 1.0)
                return rightWeight*(m_DecDistributions[left+1].getDecDistribution().get(0)-m_DecDistributions[left+1].getDecDistribution().get(1));
            if (interval > (double)(m_DecDistributions.length - 1))
                return leftWeight*(m_DecDistributions[left].getDecDistribution().get(0)-m_DecDistributions[left].getDecDistribution().get(1));
            return (m_DecDistributions[left].getDecDistribution().get(0)*leftWeight+m_DecDistributions[left+1].getDecDistribution().get(0)*rightWeight
                         -m_DecDistributions[left].getDecDistribution().get(1)*leftWeight-m_DecDistributions[left+1].getDecDistribution().get(1)*rightWeight);
        }
        if (Double.isNaN(interval)) interval = 0.0;
        return interval;
    }
}

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


package rseslib.structure.function.booleanval;

import java.io.Serializable;

import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;

/**
 * Interval inclusion test for an attribute.
 *
 * @author      Arkadiusz Wojna, Cezary Tkaczyk
 */
public class AttributeInterval implements ComparableBooleanFunction, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/** Attribute of this test. */
	NumericAttribute m_Attr;
    /** The index of an attribute to be tested. */
    private int m_nAttributeIndex;
    /** Left end of the interval. */
    private double m_nLeft;
    /** Right end of the interval. */
    private double m_nRight;
    /** Flag indicating whether the interval is left-hand closed. */
    private boolean m_bLeftClosed;
    /** Flag indicating whether the interval is right-hand closed. */
    private boolean m_bRightClosed;

    /**
     * Constructor.
     *
     * @param attrIndex   The index of an attribute to be tested.
     * @param left        Left end of the interval.
     * @param right       Right end of the interval.
     * @param leftClosed  Flag indicating whether the interval is left-hand closed.
     * @param rightClosed Flag indicating whether the interval is right-hand closed.
     */
    public AttributeInterval(NumericAttribute attr, int attrIndex, double left, double right, boolean leftClosed, boolean rightClosed)
    {
    	m_Attr = attr;
        m_nAttributeIndex = attrIndex;
        m_nLeft = left;
        m_nRight = right;
        m_bLeftClosed = leftClosed;
        m_bRightClosed = rightClosed;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        double val = dObj.get(m_nAttributeIndex);
        return (!Double.isNaN (val)
                && (m_nLeft < val || m_nLeft==val && m_bLeftClosed)
                && (m_nRight > val || m_nRight==val && m_bRightClosed));
    }

    /**
     * This method compares which boolean function is more general.
     * A function is more general than another function
     * if whenever the second function returns true,
     * the first function also returns true.  
     *
     * @param toCompare	A boolean function to be compared.
     * @return			Information which function is more general.
     */
    public CompareResult compareGenerality(ComparableBooleanFunction toCompare) throws ClassCastException
    {
    	AttributeInterval thatSelector = (AttributeInterval) toCompare;
    	double leftDiff = 0.0, rightDiff = 0.0;
    	if(!Double.isInfinite(m_nLeft) && !Double.isInfinite(thatSelector.m_nLeft))
    		leftDiff = m_nLeft - thatSelector.m_nLeft;
    	if(!Double.isInfinite(m_nRight) && !Double.isInfinite(thatSelector.m_nRight))
    		rightDiff = m_nRight - thatSelector.m_nRight;
    	
    	if (leftDiff == 0 && rightDiff == 0)
    		return CompareResult.EQUAL;
    	if (leftDiff <= 0 && rightDiff >= 0)
    		return CompareResult.THIS_MORE_GENERAL;
    	if (leftDiff >= 0 && rightDiff <= 0)
    		return CompareResult.THAT_MORE_GENERAL;
    	return CompareResult.NOT_COMPARABLE;
    }
    
    /**
     * Shrinks to exclude a given value
     * by a given margin in relation to another value.
     * 
     * @param val			Value to be excluded from this interval.
     * @param relativeTo	Reference value used to calculate how much to shrink.
     * @param margin		Value between 0 and 1 controlling how much the interval boundary is shifted beyond the excluded value.
     * @return				True if the operation succeeded and the updated interval excludes the value, false otherwise.
     */
    public boolean excludeValueWithMargin(double val, double relativeTo, double margin)
    {
    	if(Double.isNaN(val))
    		return true;
    	if(val == relativeTo)
    		return false;
    	double newBoundary = val + (relativeTo - val) * margin;		// equal to:  val - (val - relativeTo) * margin
    	if (newBoundary > m_nLeft && newBoundary < m_nRight)
    	{
    		if (val < relativeTo)
    			m_nLeft = newBoundary;    			
    		else
    			m_nRight = newBoundary;
    	}
    	return true;
    }

    /**
     * Returns text representation.
     * 
     * @return	Text representation.
     */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(m_Attr.name() + " in ");
		if (m_bLeftClosed)
			sb.append("<");
		else sb.append("(");
		sb.append(m_nLeft + ";"+ m_nRight);
		if (m_bRightClosed)
			sb.append(">");
		else sb.append(")");
		return sb.toString();
	}
}

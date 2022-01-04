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


package rseslib.structure.function.booleanval;

import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;

/**
 * Interval inclusion test for an attribute.
 *
 * @author      Arkadiusz Wojna
 */
public class AttributeInterval implements BooleanFunction
{
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

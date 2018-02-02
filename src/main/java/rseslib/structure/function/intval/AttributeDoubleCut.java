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


package rseslib.structure.function.intval;

import rseslib.structure.data.DoubleData;

/**
 * @author Rafa� Latkowski
 */
public class AttributeDoubleCut implements BoundedIntegerFunction
{
	/**
	 * Left value of the cut interval.
	 */
	double m_dLeftCutValue;

	/**
	 * Right value of the cut interval.
	 */
	double m_dRightCutValue;

	/**
	 * Number of attribute for which values are compared.
	 */
	int m_nAttributeNumber;

	/**
	 * Creates cut for attrubute <code>attributeNumber</code>
	 * with cut interval <code>leftCutValue</code> and <code>rightCutValue</code>.
	 * @param <code>attributeNumber</code> number of attribute for which values are compared.
	 * @param <code>cutValue</code> value of the cut.
	 */
	public AttributeDoubleCut(int attributeNumber,double leftCutValue,double rightCutValue)
	{
		if (leftCutValue>rightCutValue) throw new RuntimeException("");
		m_dLeftCutValue=leftCutValue;
		m_dRightCutValue=rightCutValue;
		m_nAttributeNumber=attributeNumber;
	}

	/**
	 * Compares the value of the attribute with cut value.
	 * @param <code>dObj</code> object for which the value is compared
	 * @return 0 if value of compared attribute is equal or below the left cut value
	 * @return 1 if value of compared attribute is equal or above the right cut value
	 * @return -1 if the value of compared attribute is missing or is between left and right cut value
	 * @see rseslib.structure.function.intval.IntegerFunction#intValue(rseslib.structure.data.DoubleData)
	 */
	public int intValue(DoubleData dObj)
	{
		double val=dObj.get(m_nAttributeNumber);
		if (Double.isNaN(val)||Double.isInfinite(val)) return -1;
		else if (val<=m_dLeftCutValue) return 0;
		else if (val>=m_dRightCutValue) return 1;
		else return -1;
	}

	/**
	 * Returns maximal value of this function (1).
	 * @see rseslib.structure.function.intval.BoundedIntegerFunction#maxIntValue()
	 * @return 1.
	 */
	public int maxIntValue()
	{
		return 1;
	}

	/**
	 * Returns minimal value of this function (-1).
	 * @see rseslib.structure.function.intval.BoundedIntegerFunction#minIntValue()
	 * @return -1.
	 */
	public int minIntValue()
	{
		return -1;
	}
}

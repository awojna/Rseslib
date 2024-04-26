/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;

/**
 * Equality test for an attribute.
 *
 * @author      Arkadiusz Wojna
 */
public class AttributeEquality implements BooleanFunction
{
	/** Attribute of this test. */
	Attribute m_Attr;
    /** The index of an attribute to be tested. */
    int m_nAttributeIndex;
    /** The value to be compared. */
    double m_nAttributeValue;

    /**
     * Constructor.
     *
     * @param attrIndex The index of an attribute to be tested.
     * @param attrValue The value to be compared.
     */
    public AttributeEquality(Attribute attr, int attrIndex, double attrValue)
    {
    	m_Attr = attr;
        m_nAttributeIndex = attrIndex;
        m_nAttributeValue = attrValue;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        return (Double.isNaN(dObj.get(m_nAttributeIndex)) && Double.isNaN(m_nAttributeValue)
                || dObj.get(m_nAttributeIndex)==m_nAttributeValue);
    }

    /**
     * Returns text representation.
     * 
     * @return	Text representation.
     */
	public String toString()
	{
		if (m_Attr.isNumeric())
			return m_Attr.name() + " = " + m_nAttributeValue;
		else
			return m_Attr.name() + " = " + NominalAttribute.stringValue(m_nAttributeValue);
	}
}

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


package rseslib.structure.data;

import rseslib.structure.attribute.Header;

/**
 * Data object with object values
 * and different types of attributes.
 *
 * @author      Arkadiusz Wojna
 */
public class ObjectDataObject implements ObjectData
{
    /** Array of attributes. */
    Header m_arrAttributes;
    /** Array of attribute values. */
    private Object[] m_arrAttrValues;

    /**
     * Constructs a new data object
     * with a given attribute types
     * and null attribute values.
     *
     * @param attributes Array of attribute types.
     */
    public ObjectDataObject(Header attributes)
    {
        m_arrAttributes = attributes;
        m_arrAttrValues = new Object[attributes.noOfAttr()];
    }

    /**
     * Returns attribute types for this data object.
     *
     * @return Attribute types for this data object.
     */
    public Header attributes()
    {
        return m_arrAttributes;
    }

    /**
     * Sets the value of a given attribute to a given object value.
     *
     * @param atrNo Index of the attribute to be changed.
     * @param value Object attribute value.
     */
    public void setObjectAttrVal(int atrNo, Object value)
    {
        m_arrAttrValues[atrNo] = value;
    }

    /**
     * Returns the object value of a given attribute.
     *
     * @param atrNo Index of the attribute to be returned.
     * @return      Object attribute value.
     */
    public Object getObjectAttrVal(int atrNo)
    {
        return m_arrAttrValues[atrNo];
    }

    /**
     * Constructs string representation of attribute values.
     *
     * @return String representation of attribute values.
     */
    private String attributesToString()
    {
	StringBuffer strBuf = new StringBuffer();
	for (int i = 0; i < m_arrAttrValues.length; i++)
            if (m_arrAttributes.isConditional(i))
            {
                if (m_arrAttributes.isNumeric(i)) strBuf.append(((Double)m_arrAttrValues[i]).doubleValue());
                else if (m_arrAttributes.isNominal(i)) strBuf.append(m_arrAttrValues[i].toString());
                if (i < m_arrAttrValues.length-1) strBuf.append(", ");
            }
	return strBuf.toString();
    }

    /**
     * Constructs string representation of this data object.
     *
     * @return String representation of this data object.
     */
    public String toString()
    {
        return "<"+attributesToString()+">";
    }
}

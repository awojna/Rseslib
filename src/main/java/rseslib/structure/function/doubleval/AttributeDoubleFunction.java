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


package rseslib.structure.function.doubleval;

import java.io.Serializable;

import rseslib.structure.data.DoubleData;

/**
 * Abstract class for a numeric function on double data
 * dependent only on a single attribute.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class AttributeDoubleFunction implements DoubleFunction, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/** Index of the scaled attribute. */
    private int m_nAttrIndex;

    public AttributeDoubleFunction(int attrIndex)
    {
        m_nAttrIndex = attrIndex;
    }

    /**
     * Returns the value of this function for a given attribute value.
     *
     * @param attrVal Attribute value.
     * @return        Value of this function for a given attribute value.
     */
    public abstract double doubleVal(double attrVal);

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public double doubleVal(DoubleData dObj)
    {
        return doubleVal(dObj.get(m_nAttrIndex));
    }
}

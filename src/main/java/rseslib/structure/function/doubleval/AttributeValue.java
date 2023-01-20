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


package rseslib.structure.function.doubleval;


/**
 * Representation of a numeric attribute
 * as the funtion assigning the attribute value
 * for a given data object.
 *
 * @author      Arkadiusz Wojna
 */
public class AttributeValue extends AttributeDoubleFunction
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/**
     * Constructor.
     *
     * @param ind Index of this attribute.
     */
    public AttributeValue(int ind)
    {
        super(ind);
    }

    /**
     * Returns the value of this function for a given attribute value.
     *
     * @param attrVal Attribute value.
     * @return        Value of this function for a given attribute value.
     */
    public double doubleVal(double attrVal)
    {
        return attrVal;
    }
}
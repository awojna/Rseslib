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


package rseslib.structure.attribute;

/**
 * Numeric attribute information.
 *
 * @author      Arkadiusz Wojna
 */
public class NumericAttribute extends Attribute
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/**
     * Constructor computes a normalisation factor.
     *
     * @param attrType Attribute type.
     * @param name     Name of this attribute.
     */
    public NumericAttribute(Type attrType, String name)
    {
        super(attrType, ValueSet.numeric, name);
    }

    /**
     * Checks whether this attribute is numeric.
     *
     * @return True if this attribute is numeric false otherwise.
     */
    public boolean isNumeric()
    {
        return true;
    }
}

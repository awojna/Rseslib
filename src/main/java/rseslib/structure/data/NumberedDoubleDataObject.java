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


package rseslib.structure.data;

import rseslib.structure.attribute.Header;

/**
 * Double data object that has a number related to an index.
 *
 * @author      Arkadiusz Wojna
 */
public class NumberedDoubleDataObject extends DoubleDataObject
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** The number of this data object. */
    int m_nNumber;

    /**
     * Constructs a new data object
     * with a given attribute types.
     *
     * @param attributes Array of attribute types.
     * @param number     Number of this data object.
     */
    public NumberedDoubleDataObject(Header attributes, int number)
    {
        super(attributes);
        m_nNumber = number;
    }

    /**
     * Returns the number of this data object.
     *
     * @return Number of this data object.
     */
    public int getNumber()
    {
        return m_nNumber;
    }
}

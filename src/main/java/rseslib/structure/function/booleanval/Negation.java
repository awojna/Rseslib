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

import rseslib.structure.data.DoubleData;

/**
 * Negation of a boolean function on double data.
 *
 * @author      Arkadiusz Wojna
 */
public class Negation implements BooleanFunction
{
    /** Function to be negated. */
    BooleanFunction m_BoolFun;

    /**
     * Constructor.
     *
     * @param boolFun Function to be negated.
     */
    public Negation(BooleanFunction boolFun)
    {
        m_BoolFun = boolFun;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        return !m_BoolFun.booleanVal(dObj);
    }

    /**
     * Returns text representation.
     * 
     * @return	Text representation.
     */
	public String toString()
	{
		return "not ( " + m_BoolFun + " )";
	}
}
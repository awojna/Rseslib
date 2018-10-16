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


package rseslib.structure.function.booleanval;

import rseslib.structure.data.DoubleData;

/**
 * Disjunction of boolean functions on double data.
 *
 * @author      Arkadiusz Wojna
 */
public class Disjunction implements BooleanFunction
{
    /** Components of this disjunction. */
    BooleanFunction[] m_arrComponents;

    /**
     * Constructor.
     *
     * @param components Components of this disjunction.
     */
    public Disjunction(BooleanFunction[] components)
    {
        m_arrComponents = components;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        boolean value = false;
        for (int i = 0; i < m_arrComponents.length && !value; i++) value = m_arrComponents[i].booleanVal(dObj);
        return value;
    }

    /**
     * Returns text representation.
     * 
     * @return	Text representation.
     */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		boolean first = true;
        for (int i = 0; i < m_arrComponents.length; i++)
        {
			if (!first)
				sb.append(" | ");
			sb.append("( " + m_arrComponents[i] + " )");
			first = false;
		}
		return sb.toString();
	}
}
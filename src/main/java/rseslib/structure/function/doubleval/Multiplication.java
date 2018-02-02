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


package rseslib.structure.function.doubleval;

import rseslib.structure.data.DoubleData;

/**
 * Representation of the multiplication of double functions.
 *
 * @author      Arkadiusz Wojna
 */
public class Multiplication implements DoubleFunction
{
    /** Array of factor functions. */
    DoubleFunction[] m_arrFactors;

    /**
     * Constructor.
     *
     * @param factors Array of factor functions.
     */
    public Multiplication(DoubleFunction[] factors)
    {
        m_arrFactors = factors;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public double doubleVal(DoubleData dObj)
    {
        double d = 0;
        for (int i = 0; i < m_arrFactors.length; i++) d += m_arrFactors[i].doubleVal(dObj);
        return d;
    }
}
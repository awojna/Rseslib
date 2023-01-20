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


package rseslib.structure.function.booleanval;

import rseslib.structure.data.DoubleData;
import rseslib.structure.function.doubleval.DoubleFunction;

/**
 * Binary cut on values of a numeric function.
 *
 * @author      Arkadiusz Wojna
 */
public class BooleanDiscrimination implements BooleanFunction
{
    /** Numeric function to be discriminated. */
    DoubleFunction m_RealFunction;
    /** Cutting value. */
    double m_nCut;

    /**
     * Constructor.
     *
     * @param realFun Numeric function to be discriminated.
     * @param cut     Cutting value.
     */
    public BooleanDiscrimination(DoubleFunction realFun, double cut)
    {
        m_RealFunction = realFun;
        m_nCut = cut;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        double value = m_RealFunction.doubleVal(dObj);
        if (Double.isNaN(value) || value < m_nCut) return false;
        else return true;
    }
}

/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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


package rseslib.structure.function.intval;

import rseslib.structure.data.DoubleData;
import rseslib.structure.function.doubleval.DoubleFunction;

/**
 * Partition of a range of values of a numeric funtion
 * into a fixed finite number of intervals.
 *
 * @author      Arkadiusz Wojna
 */
public class IntervalDiscrimination implements Discrimination
{
    /** The numeric function to be discriminated. */
    DoubleFunction m_RealFunction;
    /** End points of the intervals. */
    double[] m_arrBorders;

    /**
     * Constructor.
     *
     * @param realFun Numeric function to be discriminated.
     * @param borders End points of the intervals.
     */
    public IntervalDiscrimination(DoubleFunction realFun, double[] borders)
    {
        m_RealFunction = realFun;
        m_arrBorders = borders;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public int intValue(DoubleData dObj)
    {
        double value = m_RealFunction.doubleVal(dObj);
        int i;
        for (i = 0; i < m_arrBorders.length && m_arrBorders[i] < value; i++);
        return i;
    }

    /**
     * Returns the number of branches.
     *
     * @return int Number of discrimination branches.
     */
    public int noOfValues()
    {
		return m_arrBorders.length+1;
    }

	/**
     * Outputs a description of a discrimination for a given branch.
     *
     * @param branch The branch index for which the description is to be returned.
     * @return Description of a discrimination for a given branch.
     */
    public String toString(int branch)
    {
    	if (branch==0) return "(inifinity;"+m_arrBorders[0]+"]";
    	else if (branch==m_arrBorders.length) return "("+m_arrBorders[m_arrBorders.length-1]+";inifinity)";
    	else return "("+m_arrBorders[branch-1]+";"+m_arrBorders[branch]+"]";
    }
}
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

import java.util.Comparator;

import rseslib.structure.data.DoubleData;

/**
 * Comparator defining the order on data objects
 * consistent with the natural order of attribute values.
 *
 * @author      Arkadiusz Wojna
 */
public class NumericAttributeComparator implements Comparator<DoubleData>
{
    /** Attribute used to define the order. */
    private int m_nAttr;

    /**
     * Constructor.
     *
     * @param attr Attribute used to define the order.
     */
    public NumericAttributeComparator(int attr)
    {
        m_nAttr = attr;
    }

    /**
     * Compares attribute values of two data objects.
     *
     * @param o1 First data object to be compared.
     * @param o2 Second data object to be compared.
     * @return   Value 1, 0 or -1 if the first data object o1
     *           is less, equal or greater than
     *           the second data object o2.
     */
    public int compare(DoubleData o1, DoubleData o2)
    {
        if (o1.get(m_nAttr) < o2.get(m_nAttr)) return -1;
        if (o1.get(m_nAttr) > o2.get(m_nAttr)) return 1;
        return 0;
    }
}

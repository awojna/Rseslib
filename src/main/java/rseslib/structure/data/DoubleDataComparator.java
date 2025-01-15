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


package rseslib.structure.data;

import java.util.Comparator;

/**
 * Comparator defining lexicographical order
 * on data objects with double values.
 *
 * @author      Grzegorz Gora, Arkadiusz Wojna
 */
public class DoubleDataComparator implements Comparator<DoubleData>
{
    /**
     * Compares two data objects with double values.
     *
     * @param o1 First data object to be compared.
     * @param o2 Second data object to be compared.
     * @return   Value 1, 0 or -1 as the first data object o1
     *           is lexicographically after, equal to or before
     *           the second data object o2.
     */
    public int compare(DoubleData o1, DoubleData o2)
    {
    	int result = 0;
    	for (int att = 0; att < o1.attributes().noOfAttr() && result==0; att++)
    		if (o1.get(att) < o2.get(att)) result = -1;
    		else if (o1.get(att) > o2.get(att)) result = 1;
    	return result;
    }
}

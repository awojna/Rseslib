/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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


package rseslib.structure.linearorder;

import java.util.Comparator;

import rseslib.structure.data.DoubleData;

/**
 * Comparator sorting objects by the values on a given attribute.
 *
 * @author      Arkadiusz Wojna
 */
public class AttributeComparator implements Comparator<DoubleData>
{
	int m_nAttr;
	
	AttributeComparator(int attr)
	{
		m_nAttr = attr;
	}
	
	public int compare(DoubleData o1, DoubleData o2)
	{
		if (o1.get(m_nAttr) < o2.get(m_nAttr))
			return -1;
		if (o1.get(m_nAttr) > o2.get(m_nAttr))
			return 1;
		return 0;
	}
}

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


package rseslib.processing.roughset;

import java.util.ArrayList;
import java.util.Arrays;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.linearorder.AttributeSubsetComparator;

/**
 * The class computing the positive region of a given set of objects
 * on a given subset of attributes.
 *
 * @author      Arkadiusz Wojna
 */
public class PositiveRegionProvider
{
	public ArrayList<DoubleData> getPositiveRegion(int[] attributes, DoubleData[] objs)
	{
		AttributeSubsetComparator comp = new AttributeSubsetComparator(attributes);
		Arrays.sort(objs, comp);
		ArrayList<DoubleData> posRegion = new ArrayList<DoubleData>();
		int obj = 0;
		while (obj < objs.length)
		{
			int ref = obj++;
			double dec = ((DoubleDataWithDecision)objs[ref]).getDecision();
			boolean sameDec = true;
			for (; obj < objs.length && comp.compare(objs[obj], objs[ref]) == 0; ++obj)
				if (sameDec && ((DoubleDataWithDecision)objs[obj]).getDecision() != dec)
					sameDec = false;
			if (sameDec)
				for (int o = ref; o < obj; ++o)
					posRegion.add(objs[o]);
		}
		return posRegion;
	}
}

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


package rseslib.processing.roughset;

import java.util.ArrayList;
import java.util.Arrays;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.linearorder.AttributeSubsetComparator;

/**
 * The class computing the rough set (lower and upper approximation)
 * of a decision class, and the positive region.
 *
 * @author      Arkadiusz Wojna
 */
public class RoughSet
{
	/**
	 * Computes lower approximation of a given decision class
	 * for a given subset of attributes.
	 * 
	 * @param attributes	Indices of attributes.
	 * @param dec			Decision value.
	 * @param objs			Set of objects.
	 * @return				Lower approximation of a the decision class.
	 */
	public ArrayList<DoubleData> lowerApproximation(int[] attributes, double dec, DoubleData[] objs)
	{
		AttributeSubsetComparator comp = new AttributeSubsetComparator(attributes);
		Arrays.sort(objs, comp);
		ArrayList<DoubleData> lowerApprox = new ArrayList<DoubleData>();
		int obj = 0;
		while (obj < objs.length)
		{
			int ref = obj++;
			boolean sameDec = (((DoubleDataWithDecision)objs[ref]).getDecision() == dec);
			for (; obj < objs.length && comp.compare(objs[obj], objs[ref]) == 0; ++obj)
				if (sameDec && ((DoubleDataWithDecision)objs[obj]).getDecision() != dec)
					sameDec = false;
			if (sameDec)
				for (int o = ref; o < obj; ++o)
					lowerApprox.add(objs[o]);
		}
		return lowerApprox;
	}
	
	/**
	 * Computes upper approximation of a given decision class
	 * for a given subset of attributes.
	 * 
	 * @param attributes	Indices of attributes.
	 * @param dec			Decision value.
	 * @param objs			Set of objects.
	 * @return				Upper approximation of a the decision class.
	 */
	public ArrayList<DoubleData> upperApproximation(int[] attributes, double dec, DoubleData[] objs)
	{
		AttributeSubsetComparator comp = new AttributeSubsetComparator(attributes);
		Arrays.sort(objs, comp);
		ArrayList<DoubleData> upperApprox = new ArrayList<DoubleData>();
		int obj = 0;
		while (obj < objs.length)
		{
			int ref = obj++;
			boolean otherDec = (((DoubleDataWithDecision)objs[ref]).getDecision() != dec);
			for (; obj < objs.length && comp.compare(objs[obj], objs[ref]) == 0; ++obj)
				if (otherDec && ((DoubleDataWithDecision)objs[obj]).getDecision() == dec)
					otherDec = false;
			if (!otherDec)
				for (int o = ref; o < obj; ++o)
					upperApprox.add(objs[o]);
		}
		return upperApprox;
	}
	
	/**
	 * Computes positive region of a given set of objects
	 * for a given subset of attributes.
	 * 
	 * @param attributes	Indices of attributes.
	 * @param objs			Set of objects.
	 * @return				Positive region.
	 */
	public ArrayList<DoubleData> positiveRegion(int[] attributes, DoubleData[] objs)
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

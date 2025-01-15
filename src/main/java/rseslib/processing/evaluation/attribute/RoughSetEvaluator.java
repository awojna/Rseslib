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


package rseslib.processing.evaluation.attribute;

import rseslib.processing.roughset.RoughSet;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;

/**
 * The class computing approximation accuracy (decision dependency)
 * and significance of a given subset of attributes.
 *
 * @author      Arkadiusz Wojna
 */
public class RoughSetEvaluator
{
	/**
	 * Calculates the accuracy of approximation of the decision attribute
	 * by a given set of conditional attributes in a set of objects.
	 * This measure is known also as the dependency degree of the decision attribute
	 * on the given attributes.
	 * 
	 * @param attributes	Indices of attributes.
	 * @param objs			Set of objects.
	 * @return				Accuracy of approximation in the range < 0.0 ; 1.0 >.
	 */
	public double approximationAccuracy(int[] attributes, DoubleData[] objs)
	{
		return ((double)(new RoughSet()).positiveRegion(attributes, objs).size()) / objs.length;
	}

	/**
	 * Calculates the significance of a given set of conditional attributes
	 * for the decision attribute in a set of objects.
	 * 
	 * @param attributes	Indices of attributes.
	 * @param objs			Set of objects.
	 * @return				Significance of the attributes for the decision in the range < 0.0 ; 1.0 >.
	 */
	public double attributeSignificance(int[] attributes, DoubleData[] objs)
	{
		Header hdr = objs[0].attributes();
		int[] allAttrs = new int[hdr.noOfAttr() - 1];
		boolean[] complementMask = new boolean[hdr.noOfAttr()];
		int att = 0;
		for (int i = 0; i < hdr.noOfAttr(); ++i)
			if(hdr.isConditional(i))
			{
				allAttrs[att++] = i;
				complementMask[i] = true;
			} else
				complementMask[i] = false;
		for (int i : attributes)
			complementMask[i] = false;
		int[] complement = new int[allAttrs.length - attributes.length];
		int r = 0;
		for (int i = 0; i < complementMask.length; ++i)
			if(complementMask[i])
				complement[r++] = i;
		return 1 - approximationAccuracy(complement, objs) / approximationAccuracy(allAttrs, objs);
	}
}

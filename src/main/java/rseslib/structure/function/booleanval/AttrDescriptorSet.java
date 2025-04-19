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


package rseslib.structure.function.booleanval;

import java.io.Serializable;
import java.util.Collection;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;

/**
 * Conjunction of attribute descriptors.
 *
 * @author      Cezary Tkaczyk
 */
public class AttrDescriptorSet implements ComparableBooleanFunction, Comparable<AttrDescriptorSet>, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Array of descriptors, some positions can be null, the i-th descriptor is a function on the i-th attribute. */
	private ComparableBooleanFunction[] m_nSelectors;
	/** Performance rate. */
	private int m_nPerformance;
	
	/**
	 * Constructor.
	 * 
	 * @param noOfAttrs
	 */
	AttrDescriptorSet(int noOfAttrs)
	{
		m_nSelectors = new ComparableBooleanFunction[noOfAttrs];
	}
	
	/**
	 * Sets a given descriptor.
	 * 
	 * @param idx
	 * @param selector
	 */
	public void set(int idx, ComparableBooleanFunction selector)
	{
		m_nSelectors[idx] = selector;
	}
	
	/**
	 * Returns a given descriptor.
	 * 
	 * @param idx
	 * @return
	 */
	public ComparableBooleanFunction get(int idx)
	{
		return m_nSelectors[idx];
	}
	
    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
	public boolean booleanVal(DoubleData dObj)
	{
		for (int i=0; i<m_nSelectors.length; i++)
			if (m_nSelectors[i] != null)
				if (!m_nSelectors[i].booleanVal(dObj)) return false;
		return true;
	}
	
    /**
     * This method compares which boolean function is more general.
     * A function is more general than another function
     * if whenever the second function returns true,
     * the first function also returns true.  
     *
     * @param toCompare	A boolean function to be compared.
     * @return			Information which function is more general.
     */
	public CompareResult compareGenerality(ComparableBooleanFunction toCompare) throws ClassCastException
	{
		ComparableBooleanFunction[] thisSelectors = m_nSelectors;
		ComparableBooleanFunction[] thatSelectors = ((AttrDescriptorSet)toCompare).m_nSelectors;
		int thisMinusThat = 0;
		int thatMinusThis = 0;
		boolean possibleThisMoreGeneral = false;
		boolean possibleThatMoreGeneral = false;
		for (int i = 0; i < thisSelectors.length; i++) {
			if ((thisSelectors[i] == null) && (thatSelectors[i] == null)) {
			} else if (thisSelectors[i] == null) {
				thatMinusThis++;
			} else if (thatSelectors[i] == null) {
				thisMinusThat++;
			} else {
				CompareResult comparison = thisSelectors[i].compareGenerality(thatSelectors[i]);
				switch (comparison) {
				case THIS_MORE_GENERAL:
					possibleThisMoreGeneral = true;
					break;
				case THAT_MORE_GENERAL:
					possibleThatMoreGeneral = true;
					break;
				case EQUAL:
					break;
				case NOT_COMPARABLE:
					return CompareResult.NOT_COMPARABLE;
					//possibleThisMoreGeneral = true;						
					//possibleThatMoreGeneral = true;
				}
			}
		}

		if ((thisMinusThat > 0) && (thatMinusThis == 0) 
				&& (!possibleThatMoreGeneral))
			return CompareResult.THIS_MORE_GENERAL;
		else if ((thisMinusThat == 0) && (thatMinusThis > 0)
				&& (!possibleThisMoreGeneral))
			return CompareResult.THAT_MORE_GENERAL;
		else if ((thisMinusThat == 0) && (thatMinusThis == 0))
			if ((!possibleThisMoreGeneral) && (!possibleThatMoreGeneral))
				return CompareResult.EQUAL;
			else if ((!possibleThisMoreGeneral) && (possibleThatMoreGeneral))
				return CompareResult.THAT_MORE_GENERAL;
			else if ((possibleThisMoreGeneral) && (!possibleThatMoreGeneral))
				return CompareResult.THIS_MORE_GENERAL;
			else
				return CompareResult.NOT_COMPARABLE;
		else
			return CompareResult.NOT_COMPARABLE;
	}

	/**
	 * Calculates the performance of this set of descriptors in relation to a given decision   
	 * measured as the number of the objects with this decision matching these descriptors
	 * plus the number of objects with other decisions filtered out by these descriptors.
	 *   
	 * @param examples
	 * @param dec
	 */
	public void calculatePerformance(Collection<DoubleData> examples, double dec)
	{
		// e_pos_included + e_neg_excluded
		int e_posIncluded = 0;
		int e_negExcluded = 0;
		for (DoubleData example : examples) {
			if (this.booleanVal(example)) {
				if (((DoubleDataWithDecision)example).getDecision() == dec)
					e_posIncluded++;
			}
			else {
				if (((DoubleDataWithDecision)example).getDecision() != dec)
					e_negExcluded++;
			}
		}
		m_nPerformance = e_posIncluded + e_negExcluded; 
	}
	
	/**
	 * Returns the performance of this set of descriptors.
	 * 
	 * @return Performance.
	 */
	public int getPerformance()
	{
		return m_nPerformance;
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
		for (int i=0; i<m_nSelectors.length; i++) {
			if (m_nSelectors[i] != null) {
				if (!first)
					sb.append("  &  ");
				sb.append(m_nSelectors[i]);
				first = false;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Compares performance of this set of descriptors with another set.
	 * 
	 *  @param o Set of descriptors to be compared.
	 */
	public int compareTo(AttrDescriptorSet o)
	{
		return m_nPerformance - o.m_nPerformance;
	}
}

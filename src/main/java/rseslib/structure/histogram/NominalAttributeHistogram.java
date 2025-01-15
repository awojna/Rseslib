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


package rseslib.structure.histogram;

import java.util.Collection;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;

/**
 * The value histogram of a nominal attribute in a set of data object.
 *
 * @author      Arkadiusz Wojna
 */
public class NominalAttributeHistogram implements Histogram
{
	/** The information about the attribute. */
	private NominalAttribute m_Attribute;
	/** The histogram values for successive local codes of nominal values. */
	private int[] m_Amounts;
	/** The total number of non-missing values in a set. */
	private int m_nTotal;

	/**
	 * Constructor.
	 * 
	 * @param objects	Set of objects to define this histogram.
	 * @param attrInd	Attibute number.
	 * @param attr		Information about the attribute.
	 */
	public NominalAttributeHistogram(Collection<DoubleData> objects, int attrInd, NominalAttribute attr)
	{
		m_Attribute = attr;
		m_Amounts = new int[attr.noOfValues()];
		for (DoubleData obj : objects)
			if (!Double.isNaN(obj.get(attrInd)))
				m_Amounts[m_Attribute.localValueCode(obj.get(attrInd))]++; 
		for (int pos = 0; pos < m_Amounts.length; pos++)
			m_nTotal += m_Amounts[pos];
	}
	
    /**
     * Returns the number of the histogram positions,
     * i.e. the number of different values
     * in a set used to define this histogram.
     *
     * @return	Number of the histogram positions.
     */
    public int size()
    {
    	return m_Amounts.length;
    }

    /**
     * Returns the value at a given position in this histogram.
     *
     * @param valIndex	Position in this histogram.
     * @return			Value at the given position.
     */
    public double value(int valIndex)
    {
    	return m_Attribute.globalValueCode(valIndex);
    }

    /**
     * Retuns the number of the occurences of the value
     * from a given position in this histogram.
     *
     * @param valIndex	Position in this histogram.
     * @return			Number of the value occurrences.
     */
    public int amount(int valIndex)
    {
    	return m_Amounts[valIndex];
    }

    /**
     * Returns the number of all non-missing values.
     * 
     * @return Number of all non-missing values.
     */
    public int totalAmount()
    {
    	return m_nTotal;
    }
}

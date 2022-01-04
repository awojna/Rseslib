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


package rseslib.structure.histogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import rseslib.processing.filtering.MissingValuesFilter;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.NumericAttributeComparator;

/**
 * The value histogram of a numerical attribute in a set of data object.
 *
 * @author      Arkadiusz Wojna
 */
public class NumericalAttributeHistogram implements Histogram
{
	/** Sorted array of all the values occuring in a data set. */  
	private double[] m_Values;
	/** The histogram values corresponding to the attribute values from the array m_Values. */
	private int[] m_Amounts;
	/** The total number of non-missing values in a set. */
	private int m_nTotal;
	
	/**
	 * Constructor.
	 * 
	 * @param objects	Set of objects to define this histogram.
	 * @param attrInd	Attibute number.
	 */
	public NumericalAttributeHistogram(Collection<DoubleData> objects, int attrInd)
	{
		ArrayList<DoubleData> objectsWthoutNulls = MissingValuesFilter.select(objects, attrInd); 
		DoubleData[] objArray = objectsWthoutNulls.toArray(new DoubleData[0]);
		Arrays.sort(objArray, new NumericAttributeComparator(attrInd));
		int valuePos = 0;
		for (int obj = 0; obj < objArray.length; obj++)
			if (obj==0 || objArray[obj].get(attrInd)!=objArray[obj-1].get(attrInd))
				valuePos++;
		m_Values = new double[valuePos];
		m_Amounts = new int[valuePos];
		valuePos = -1;
		for (int obj = 0; obj < objArray.length; obj++)
		{
			if (obj==0 || objArray[obj].get(attrInd)!=m_Values[valuePos])
				m_Values[++valuePos] = objArray[obj].get(attrInd);
			m_Amounts[valuePos]++;
		}
		m_nTotal = objArray.length;
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
    	return m_Values.length;
    }

    /**
     * Returns the value at a given position in this histogram.
     *
     * @param valIndex	Position in this histogram.
     * @return			Value at the given position.
     */
    public double value(int valIndex)
    {
    	return m_Values[valIndex];
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

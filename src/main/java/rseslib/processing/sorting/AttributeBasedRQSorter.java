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


package rseslib.processing.sorting;

import rseslib.structure.data.DoubleData;
import rseslib.structure.linearorder.LinearOrder;

/**
 * @author Rafa� Latkowski
 */
public class AttributeBasedRQSorter implements AttributeBasedSorting
{
	Sorter sorter;
	/**
	 * 
	 */
	public AttributeBasedRQSorter()
	{
		sorter=new RandomizedQuickSorter();	
	}

	/* (non-Javadoc)
	 * @see rseslib.processing.sorting.AttributeBasedSorting#sort(int, rseslib.structure.data.DoubleData[])
	 */
	public void sort(int attribute, DoubleData[] dObjects)
	{
		sorter.sort(new ArrayAttributeLinearOrder(dObjects,attribute));
	}


	public class ArrayAttributeLinearOrder implements LinearOrder
	{
		DoubleData[] m_data;
		int m_attribute;
		public ArrayAttributeLinearOrder(DoubleData[] data,int attribute)
		{
			m_data=data;
			m_attribute=attribute;
		}
		/**
		 * Compares two DoubleData elements in array on specified attribute.
		 * @return true if n1-st DoubleData object on attribute m_attribute is grater than n2-nd attribute.
		 * @see rseslib.structure.linearorder.LinearOrder#greater(int, int)
		 */
		public boolean greater(int n1, int n2)
		{
			return m_data[n1].get(m_attribute)>m_data[n2].get(m_attribute);
		}

		/**
		 * Swaps two DoubleData elements in array.
		 * @see rseslib.structure.linearorder.LinearOrder#swap(int, int)
		 */
		public void swap(int n1, int n2)
		{
			DoubleData temp;
			temp = m_data[n1];
			m_data[n1]=m_data[n2];
			m_data[n2]=temp;
		}

		/**
		 * Returns zero
		 * @return zero
		 * @see rseslib.structure.linearorder.LinearOrder#getFirst()
		 */
		public int getFirst()
		{
			return 0;
		}

		/**
		 * Returns length of array with DoubleData objects.
		 * @return length of array
		 * @see rseslib.structure.linearorder.LinearOrder#getLast()
		 */
		public int getLast()
		{
			return m_data.length-1;
		}
		
	}
}

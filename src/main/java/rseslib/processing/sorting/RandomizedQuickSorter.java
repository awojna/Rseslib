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


package rseslib.processing.sorting;

import java.util.Comparator;

/**
 * @author Rafal Latkowski
 */
public class RandomizedQuickSorter<T> implements Sorter<T>
{
	private Comparator<T> m_order;
	T[] m_array;
	
	/** 
	 * Sorts data using QuickSort algorithm.
	 * @see rseslib.processing.sorting.Sorter#sort(T[] array, Comparator<T> order)
	 */
	public void sort(T[] array, Comparator<T> order)
	{
		m_array = array;
		m_order = order;
		rqsort(0, m_array.length - 1);
	}
	
	private void rqsort(int l,int r)
	{
		int pr = l+(int)((r-l)*Math.random());
		swap(pr,l);
		int p = l;
		int i = l;
		int j = r+1;
		while (i<j)
		{
			do i++; while (m_order.compare(m_array[p],m_array[i]) > 0 && (i<r));
			do j--; while (m_order.compare(m_array[j],m_array[p]) > 0 && (j>l));
			if (i<j) swap(i,j);
		}
		swap(p,j);
		if (j-1>l) rqsort(l,j-1);
		if (r>j+1) rqsort(j+1,r);		
	}
	
	private void swap(int p1, int p2)
	{
		T tmp = m_array[p1];
		m_array[p1] = m_array[p2];
		m_array[p2] = tmp; 
	}
}

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

import rseslib.structure.linearorder.LinearOrder;

/**
 * @author Rafal Latkowski
 */
public class RandomizedQuickSorter implements Sorter
{
	private LinearOrder m_order;
	/**
	 * 
	 */
	public RandomizedQuickSorter()
	{
	}

	private void rqsort(int l,int r)
	{
		int pr = l+(int)((r-l)*Math.random());
		m_order.swap(pr,l);
		int p = l;
		int i = l;
		int j = r+1;
		while (i<j)
		{
			do i++; while (m_order.greater(p,i)&&(i<r));
			do j--; while (m_order.greater(j,p)&&(j>l));
			if (i<j) m_order.swap(i,j);
		}
		m_order.swap(p,j);
		if (j-1>l) rqsort(l,j-1);
		if (r>j+1) rqsort(j+1,r);		
	}

	/** 
	 * Sorts data accessible by LinearOrder using QuickSort algorithm.
	 * @see rseslib.processing.sorting.Sorter#sort(rseslib.structure.linearorder.LinearOrder)
	 */
	public void sort(LinearOrder order)
	{
		m_order=order;
		rqsort(order.getFirst(),order.getLast());
	}

}

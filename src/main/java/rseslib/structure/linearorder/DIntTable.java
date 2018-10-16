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


package rseslib.structure.linearorder;


/**
 * @author Rafal Latkowski
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DIntTable implements LinearOrder
{

	private int[] m_aTable;
	private int m_nSize;
	private int m_nCapacity;

	public DIntTable()
	{
		this(1024);
	}

	public DIntTable(int capacity)
	{
		m_nCapacity = capacity;
		m_nSize = 0;
		m_aTable = new int[m_nCapacity];
	}

	public int getSize()
	{
		return m_nSize;
	}

	public int getCapacity()
	{
		return m_nCapacity;
	}

	public int[] toArray()
	{
		int new_int[] = new int[m_nSize];
		System.arraycopy(m_aTable, 0, new_int, 0, m_nSize);
		return new_int;
	}

	private void grow()
	{
		int new_capacity = m_nCapacity >= 1024 ? 2 * m_nCapacity : 1024;
		int[] new_aTable = new int[new_capacity];
		System.arraycopy(m_aTable, 0, new_aTable, 0, m_nCapacity);
		m_nCapacity = new_capacity;
		m_aTable = new_aTable;
	}

	public void add(int i)
	{
		if (m_nSize == m_nCapacity)
			grow();
		m_aTable[m_nSize] = i;
		m_nSize++;
	}

	public boolean greater(int n1, int n2)
	{
		return m_aTable[n1] > m_aTable[n2];
	}

	public void swap(int n1, int n2)
	{
		int temp = m_aTable[n1];
		m_aTable[n1] = m_aTable[n2];
		m_aTable[n2] = temp;
	}

	public int getFirst()
	{
		return 0;
	}

	public int getLast()
	{
		return m_nSize - 1;
	}

	public int get(int index)
	{
		if (index >= m_nSize)
			throw new IndexOutOfBoundsException(index + ">=" + m_nSize);
		return m_aTable[index];
	}
}

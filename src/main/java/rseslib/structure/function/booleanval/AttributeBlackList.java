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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;

/**
 * Test against a black list of excluded values.
 *
 * @author      Cezary Tkaczyk, Arkadiusz Wojna
 */
public class AttributeBlackList implements ComparableBooleanFunction, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/** Attribute of this test. */
	Attribute m_Attr;
    /** The index of an attribute to be tested. */
    int m_nAttributeIndex;
    /** Text representing missing value. */
    String m_Missing;
    /** Switch indicating whether the missing value in included in this black list. */
    boolean m_MissingBlack = false;
    /** Black list of values. */
    double[] m_arrBlackList = null;
    /** Number of elements on this black list. */
    int m_nSize = 0;
    
    /**
     * Constructor.
     * 
     * @param attr
     * @param attrIndex
     * @param missing
     */
    AttributeBlackList(Attribute attr, int attrIndex, String missing)
    {
    	m_Attr = attr;
    	m_nAttributeIndex = attrIndex;
    	m_Missing = missing;
    	m_arrBlackList = new double[10];
    }
    
    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(m_Attr);
    	out.writeInt(m_nAttributeIndex);
    	out.writeObject(m_Missing);
    	out.writeBoolean(m_MissingBlack);
    	out.writeInt(m_nSize);
    	if(m_Attr.isNumeric())
    		out.writeObject(m_arrBlackList);
    	else
    		for(int i = 0; i < m_nSize; ++i)
    			out.writeInt(((NominalAttribute)m_Attr).localValueCode(m_arrBlackList[i]));
    }

    /**
     * Reads this object.
     *
     * @param in			Input for reading.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_Attr = (Attribute)in.readObject();
    	m_nAttributeIndex = in.readInt();
    	m_Missing = (String)in.readObject();
    	m_MissingBlack = in.readBoolean();
    	m_nSize = in.readInt();
    	if(m_Attr.isNumeric())
    		m_arrBlackList = (double[])in.readObject();
    	else
    	{
    		m_arrBlackList = new double[m_nSize > 10 ? m_nSize : 10];
    		for(int i = 0; i < m_nSize; ++i)
    			m_arrBlackList[i] = ((NominalAttribute)m_Attr).globalValueCode(in.readInt());
    	}
    }
    
    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        double val = dObj.get(m_nAttributeIndex);
        if(Double.isNaN(val))
        	return !m_MissingBlack;
        for(int i = 0; i < m_nSize; ++i)
        	if(val == m_arrBlackList[i])
        		return false;
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
    	AttributeBlackList thatSelector = (AttributeBlackList) toCompare;
    	
    	if(m_arrBlackList.length == thatSelector.m_arrBlackList.length)
    	{
    		for(int i = 0; i < m_arrBlackList.length; i++)
    			if (m_arrBlackList[i] != thatSelector.m_arrBlackList[i])
    				return CompareResult.NOT_COMPARABLE;
    		if(!m_MissingBlack && thatSelector.m_MissingBlack)
    			return CompareResult.THIS_MORE_GENERAL;
    		if(m_MissingBlack && !thatSelector.m_MissingBlack)
    			return CompareResult.THAT_MORE_GENERAL;
    		return CompareResult.EQUAL;
    	}
    	
    	if(m_arrBlackList.length < thatSelector.m_arrBlackList.length)
    	{
    		if(m_MissingBlack && !thatSelector.m_MissingBlack)
    			return CompareResult.NOT_COMPARABLE;
    		int j = 0;
    		for(int i = 0; i < m_arrBlackList.length; i++)
    		{
    			while(j < thatSelector.m_arrBlackList.length && thatSelector.m_arrBlackList[j] < m_arrBlackList[i])
    				j++;
    			if(j == thatSelector.m_arrBlackList.length || thatSelector.m_arrBlackList[j++] != m_arrBlackList[i])
        			return CompareResult.NOT_COMPARABLE;
    		}
			return CompareResult.THIS_MORE_GENERAL;
    	} else {
    		if(!m_MissingBlack && thatSelector.m_MissingBlack)
    			return CompareResult.NOT_COMPARABLE;
    		int j = 0;
    		for(int i = 0; i < thatSelector.m_arrBlackList.length; i++)
    		{
    			while(j < m_arrBlackList.length && m_arrBlackList[j] < thatSelector.m_arrBlackList[i])
    				j++;
    			if(j == m_arrBlackList.length || m_arrBlackList[j++] != thatSelector.m_arrBlackList[i])
        			return CompareResult.NOT_COMPARABLE;
    		}
			return CompareResult.THAT_MORE_GENERAL;
    	}
    }
    
    /**
     * Adds a value to this black list.
     * 
     * @param val	Value to be added to this black list.
     */
    public void exclude(double val)
    {
        if(Double.isNaN(val))
        	m_MissingBlack = true;
        else
        {
        	if(m_nSize == m_arrBlackList.length)
        	{
        		double[] newList = new double[m_nSize * 2];
                for(int i = 0; i < m_nSize; ++i)
                	newList[i] = m_arrBlackList[i];
                m_arrBlackList = newList;
        	}
        	int l = 0;
        	while(l < m_nSize && val > m_arrBlackList[l])
        		l++;
        	if(l == m_nSize || m_arrBlackList[l] != val)
        	{
        		for(int idx = m_nSize++; idx > l; idx--)
        			m_arrBlackList[idx] = m_arrBlackList[idx - 1];
        		m_arrBlackList[l] = val;
        	}
        }
    }

    /**
     * Returns text representation.
     * 
     * @return	Text representation.
     */
	public String toString()
	{
    	StringBuffer sb = new StringBuffer();
    	sb.append(m_Attr.name() + " <> ");
    	boolean nonfirst = false;
    	for (double d : m_arrBlackList)
    	{
    		if(nonfirst)
    			sb.append(",");
    		else
    			nonfirst = true;
    		if(m_Attr.isNominal())
    			sb.append(NominalAttribute.stringValue(d));
    		else
    			sb.append(d);
    	}
    	if(m_MissingBlack)
    	{
    		if(nonfirst)
    			sb.append(",");
    		sb.append(m_Missing);
    	}
    	return sb.toString();
	}
}

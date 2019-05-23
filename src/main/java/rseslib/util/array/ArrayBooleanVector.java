/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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


package rseslib.util.array;

import java.util.Arrays;

/**
 * Array boolean vector class represents a vector of boolean values. 
 * The implementation is based on array of booleans and
 * frequently uses java.util.Arrays methods.
 * @see java.util.Arrays
 *  
 * @author Rafal Latkowski
 */
public class ArrayBooleanVector implements BooleanVector
{
    /**
     * Serialization universal version identifier 
     */
    private static final long serialVersionUID = 7232578539L;

    /**
     * Boolean vector data
     * @serial
     */
    boolean[] m_aVector;

    /**
     * Constructor creates new boolean vector of a specified witdth.
     * @param width The boolean vector width.
     */
    public ArrayBooleanVector(int width)
    {
        m_aVector = new boolean[width];
    }
    
    /**
     * Constructor creates new boolean vector as an exact copy of template. 
     * Creates new boolean vector of identical width as passed template
     * and copies all values into new vector. 
     * @param template The boolean vector template.
     */
    public ArrayBooleanVector(ArrayBooleanVector template)
    {
        m_aVector = new boolean[template.m_aVector.length];
        System.arraycopy(template.m_aVector.length,0,m_aVector,0,m_aVector.length);
    }

    /**
     * Constructor creates new boolean vector as an exact copy of template. 
     * Creates new boolean vector of identical width as passed template
     * and copies all values into new vector. 
     * @param template The boolean vector template.
     */
    public ArrayBooleanVector(BooleanVector template)
    {
        m_aVector = new boolean[template.getWidth()];
        for (int i=0;i<m_aVector.length;i++)
            m_aVector[i]=template.get(i);
    }

    /**
     * Constructor creates new boolean vector by decompressing bits from long value.
     * Creates new boolean vector of specified width and decompresses
     * bits from long value.
     * @param width The boolean vector width.
     * @param vector The compressed bits of boolean vector.
     */
    public ArrayBooleanVector(int width,long vector)
    {
        m_aVector = new boolean[width];
        long mask=1L;
        for (int i=0;i<width;i++)
        {
            m_aVector[i]=(vector&mask)>0L;
            mask<<=1;
        }
    }

    /**
     * Sets value of boolean vector.
     * @param position The position to be set to a specified value.
     * @param value The value to be set.
     */
    public void set(int position,boolean value)
    {
        m_aVector[position]=value;
    }
    
    /**
     * Sets value of boolean vector to true.
     * @param position The position to be set to true.
     */
    public void set(int position)
    {
        m_aVector[position]=true;
    }
    
    /**
     * Sets value of boolean vector to false.
     * @param position The position to be set to false.
     */
    public void clear(int position)
    {
        m_aVector[position]=false;
    }
    
    /**
     * Returns value of boolean vector.
     * @param position The position to be returned.
     * @return value of boolean at specified position.
     */
    public boolean get(int position)
    {
        return m_aVector[position];
    }
    
    
    /**
     * Returns width of boolean vector.
     * @return width of boolean vector.
     */
    public int getWidth()
    {
        return m_aVector.length;
    }
        
    /**
     * Compress boolean vector into long value.
     * Booleans from vector are converted into bits of long.
     * @return long value with compressed bits of boolean vector
     */
    public long compress()
    {
        if (m_aVector.length>64) throw new ArrayIndexOutOfBoundsException("Compression not available for boolean vectors wider than 64");
        long value=0L;
        long mask=1L;
        for (int i=0;i<m_aVector.length;i++)
        {
            if (m_aVector[i]) value|=mask;
            mask<<=1;
        }
        return value;
    }
    
    
    /**
     * Returns a string representation of the boolean vector.
     * @return string representation of the boolean vector.
     * @see     java.util.Arrays#toString(boolean[])
     */
    public String toString()
    {
        StringBuffer sb=new StringBuffer(m_aVector.length+2);
        sb.append('[');
        for (int i=0;i<m_aVector.length;i++)
            sb.append(m_aVector[i]?'+':'-');
        sb.append(']');
        return sb.toString();
    }
    
    /** 
     * Compares this vector with the specified vector o according to the lexicographical order.
     * Returns -1, 0, or 1 as this vector is less than, equal to, or greater than the specified object.
     * @param o The vector to be compared.
     * @return The value -1, 0, or 1 as this vector is less than, equal to, or greater than the specified object.
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(BooleanVector bv)
    {
        if (m_aVector.length<bv.getWidth()) return -1;
        for (int i=0;i<m_aVector.length;i++)
        {
            if (m_aVector[i]!=bv.get(i))
            {
                if (m_aVector[i]) return 1; else return -1;
            }
        }
        return 0;
    }

    /**
     * Compares object with this BooleanVector.
     * @return true when values of all booleans are the same
     * @see     java.util.Arrays#equals(boolean[], boolean[]) 
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    public boolean equals(Object o)
    {
        if (o==null) return false;
        if (!(o instanceof ArrayBooleanVector)) return false;
        return Arrays.equals(m_aVector,((ArrayBooleanVector)o).m_aVector);
    }
    
    /**
     * Returns hash code for this boolean vector.
     * @return integer hash code for this boolean vector
     * @see     java.util.Arrays#hashCode(boolean[])
     * @see     java.lang.Object#hashCode()
     * @see     java.util.Hashtable
     */
    public int hashCode()
    {
        super.hashCode();
        return Arrays.hashCode(m_aVector);
    }

    public void and(BooleanVector bv)
    {
        for (int i=0;i<m_aVector.length&&i<bv.getWidth();i++)
            m_aVector[i]&=bv.get(i);
    }

    public void or(BooleanVector bv)
    {
        for (int i=0;i<m_aVector.length&&i<bv.getWidth();i++)
            m_aVector[i]|=bv.get(i);
    }

    /**
     * Copies values of boolean array into this boolean vector.
     * If sizes does not mach, only shorter size is used.
     * @param vector The array of boolean values.
     */
    public void set(boolean[] vector)
    {
        System.arraycopy(vector,0,m_aVector,0,
            vector.length>m_aVector.length?m_aVector.length:vector.length);
    }
    
    /**
     * Returns true if all bits in this vector are set to false.
     * @return true if all bits in this vector are set to false.
     */
    public boolean allFalse()
    {
        for (boolean val : m_aVector)
            if (val) return false;
        return true;
    }
}

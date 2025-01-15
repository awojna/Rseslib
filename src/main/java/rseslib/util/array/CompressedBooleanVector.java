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


package rseslib.util.array;

import java.util.Arrays;

/**
 * Compressed boolean vector class represents a vector of boolean values. 
 * The implementation is based on array of integer and
 * uses compression of booleans into integer bits.
 *  
 * @author Rafal Latkowski
 */
public class CompressedBooleanVector implements BooleanVector
{
    /**
     * Serialization universal version identifier 
     */
    private static final long serialVersionUID = 7232578539L;

    /**
     * Bit mask for all bits in compressed integer
     */
    private static final int[] bitmask =
    {
        0x00000001,0x00000002,0x00000004,0x00000008,
        0x00000010,0x00000020,0x00000040,0x00000080,
        0x00000100,0x00000200,0x00000400,0x00000800,
        0x00001000,0x00002000,0x00004000,0x00008000,
        0x00010000,0x00020000,0x00040000,0x00080000,
        0x00100000,0x00200000,0x00400000,0x00800000,
        0x01000000,0x02000000,0x04000000,0x08000000,
        0x10000000,0x20000000,0x40000000,0x80000000
    };
        
    /**
     * Logical width of this boolean vector
     * @serial
     */
    int m_width;

    /**
     * Integer vector for bit data of boolean vector
     * @serial
     */
    int[] m_aVector;

    /**
     * Constructor creates new boolean vector of a specified witdth.
     * @param width The boolean vector width.
     */
    public CompressedBooleanVector(int width)
    {
        m_width=width;
        m_aVector = new int[(width>>5) + ((width&31)!=0?1:0)];
    }
    
    /**
     * Constructor creates new boolean vector as an exact copy of template. 
     * Creates new boolean vector of identical width as passed template
     * and copies all values into new vector. 
     * @param template The boolean vector template.
     */
    public CompressedBooleanVector(CompressedBooleanVector template)
    {
        m_width=template.m_width;
        m_aVector = new int[template.m_aVector.length];
        for (int i=0;i<m_aVector.length;i++)
            m_aVector[i]=template.m_aVector[i];
    }
    

    /**
     * Constructor creates new boolean vector as an exact copy of template. 
     * Creates new boolean vector of identical width as passed template
     * and copies all values into new vector. 
     * @param template The boolean vector template.
     */
    public CompressedBooleanVector(BooleanVector template)
    {
        m_width=template.getWidth();
        m_aVector = new int[(m_width>>5) + ((m_width&31)!=0?1:0)];
        
        for (int i=0;i<m_width;i++)
        {
            if (template.get(i)) m_aVector[i>>5]|=bitmask[i&31];
        }
    }

    /**
     * Sets value of boolean vector.
     * @param position The position to be set to a specified value.
     * @param value The value to be set.
     */
    public void set(int position,boolean value)
    {
        if (value)
            m_aVector[position>>5] |= bitmask[position&31];
        else
            m_aVector[position>>5] &= ~bitmask[position&31];
    }
    
    /**
     * Sets value of boolean vector to true.
     * @param position The position to be set to true.
     */
    public void set(int position)
    {
        m_aVector[position>>5] |= bitmask[position&31];
    }
    
    /**
     * Sets value of boolean vector to false.
     * @param position The position to be set to false.
     */
    public void clear(int position)
    {
        m_aVector[position>>5] &= ~bitmask[position&31];
    }
    
    /**
     * Returns value of boolean vector.
     * @param position The position to be returned.
     * @return value of boolean at specified position.
     */
    public boolean get(int position)
    {
        return (m_aVector[position>>5]&bitmask[position&31])!=0;
    }
    
    
    /**
     * Returns width of boolean vector.
     * @return width of boolean vector.
     */
    public int getWidth()
    {
        return m_width;
    }
            
    /**
     * Returns a string representation of the boolean vector.
     * @return string representation of the boolean vector.
     * @see     java.util.Arrays#toString(boolean[])
     */
    public String toString()
    {
        StringBuffer sb=new StringBuffer(m_width+2);
        sb.append('[');
        for (int i=0;i<m_width;i++)
            sb.append(get(i)?'+':'-');
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
        if (m_width<bv.getWidth()) return -1;
        for (int i=0;i<m_width;i++)
        {
            if (get(i)!=bv.get(i))
            {
                if (get(i)) return 1; else return -1;
            }
        }
        return 0;
    }

    /**
     * Compares object with this BooleanVector.
     * @return true when values of all booleans are the same
     * @see     java.util.Arrays#equals(int[], int[]) 
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    public boolean equals(Object o)
    {
        if (o==null) return false;
        if (!(o instanceof CompressedBooleanVector)) return false;
        return Arrays.equals(m_aVector,((CompressedBooleanVector)o).m_aVector);
    }
    
    /**
     * Returns hash code for this boolean vector.
     * @return integer hash code for this boolean vector
     * @see     java.util.Arrays#hashCode(int[])
     * @see     java.lang.Object#hashCode()
     * @see     java.util.Hashtable
     */
    public int hashCode()
    {
        super.hashCode();
        return Arrays.hashCode(m_aVector);
    }

    /**
     * Modifies this boolean vector by conjuction with parameter boolean vector.
     * @param bv The boolean vector to be conjucted.  
     */
    public void and(CompressedBooleanVector bv)
    {
        for (int i=0;(i<m_aVector.length)&&(i<bv.m_aVector.length);i++)
            m_aVector[i]&=bv.m_aVector[i];
    }

    /**
     * Modifies this boolean vector by disjunction with parameter boolean vector.
     * @param bv The boolean vector to be disjuncted.
     */
    public void or(CompressedBooleanVector bv)
    {
        for (int i=0;(i<m_aVector.length)&&(i<bv.m_aVector.length);i++)
            m_aVector[i]|=bv.m_aVector[i];
    }

    /**
     * Modifies this boolean vector by conjuction with parameter boolean vector.
     * @param bv The boolean vector to be conjucted.  
     */
    public void and(BooleanVector bv)
    {
        if (bv instanceof CompressedBooleanVector) and((CompressedBooleanVector) bv);
        else throw new IllegalArgumentException("ANDing BooleanVector is forbiden due to efficiency issues");
    }

    /**
     * Modifies this boolean vector by disjunction with parameter boolean vector.
     * @param bv The boolean vector to be disjuncted.
     */
    public void or(BooleanVector bv)
    {
        if (bv instanceof CompressedBooleanVector) or((CompressedBooleanVector) bv);
        else throw new IllegalArgumentException("ORing BooleanVector is forbiden due to efficiency issues");
    }

    /**
     * Copies values of boolean array into this boolean vector.
     * If sizes does not mach, only shorter size is used.
     * @param vector The array of boolean values.
     */
    public void set(boolean[] vector)
    { 
        for (int i=0;i<m_width&&i<vector.length;i++)
        {
            if (vector[i]) m_aVector[i>>5]|=bitmask[i&31];
        }
    }

    /**
     * Returns true if all bits in this vector are set to false.
     * @return true if all bits in this vector are set to false.
     */
    public boolean allFalse()
    {
        for (int val : m_aVector)
            if (val!=0) return false;
        return true;
    }
}

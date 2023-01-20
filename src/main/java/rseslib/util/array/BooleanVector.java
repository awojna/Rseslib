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


package rseslib.util.array;

import java.io.Serializable;

/**
 * Boolean vector array class represents a vector of boolean values. 
 * The implementation is based on array of booleans and
 * frequently uses java.util.Arrays methods.
 * @see java.util.Arrays
 *  
 * @author Rafal Latkowski
 */
public interface BooleanVector extends Comparable<BooleanVector>, Serializable
{
    /**
     * Returns width of boolean vector.
     * @return width of boolean vector.
     */
    public int getWidth();
    
    /**
     * Sets value of boolean vector.
     * @param position The position to be set to a specified value.
     * @param value The value to be set.
     */
    public void set(int position,boolean value);
    
    /**
     * Copies values of boolean array into this boolean vector.
     * If sizes does not mach, only shorter size is used.
     * @param vector The array of boolean values.
     */
    public void set(boolean[] vector);
    
    /**
     * Sets value of boolean vector to true.
     * @param position The position to be set to true.
     */
    public void set(int position);
    
    /**
     * Sets value of boolean vector to false.
     * @param position The position to be set to false.
     */
    public void clear(int position);
    
    /**
     * Returns value of boolean vector.
     * @param position The position to be returned.
     * @return value of boolean at specified position.
     */
    public boolean get(int position);
    
    /**
     * Returns a string representation of the boolean vector.
     * @return string representation of the boolean vector.
     */
    public String toString();
    
    /**
     * Modifies this boolean vector by conjuction with parameter boolean vector.
     * @param bv The boolean vector to be conjucted.  
     */
    public void and(BooleanVector bv);
    
    /**
     * Modifies this boolean vector by disjunction with parameter boolean vector.
     * @param bv The boolean vector to be disjuncted.
     */
    public void or(BooleanVector bv);
    
    /**
     * Returns true if all bits in this vector are set to false.
     * @return true if all bits in this vector are set to false.
     */
    public boolean allFalse();
}

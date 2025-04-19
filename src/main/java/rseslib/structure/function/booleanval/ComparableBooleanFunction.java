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

/**
 * Interface for a boolean function
 * with the method for comparing generality.
 *
 * @author      Cezary Tkaczyk
 */
public interface ComparableBooleanFunction extends BooleanFunction
{
	public enum CompareResult { THIS_MORE_GENERAL, THAT_MORE_GENERAL, EQUAL, NOT_COMPARABLE }
	
    /**
     * This method compares which boolean function is more general.
     * A function is more general than another function
     * if whenever the second function returns true,
     * the first function also returns true.  
     *
     * @param toCompare	A boolean function to be compared.
     * @return			Information which function is more general.
     */
	abstract public CompareResult compareGenerality(ComparableBooleanFunction toCompare) throws ClassCastException;
}

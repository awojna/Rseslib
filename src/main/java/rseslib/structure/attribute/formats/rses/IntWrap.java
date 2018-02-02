/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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


package rseslib.structure.attribute.formats.rses;


/**
 * Rough Set Library
 *
 * @author Jan Bazan
 *
 */


public class IntWrap
{

	/**
	 * The variable into which the integer number is stored.
	 */
	int intValue;

	/**
	 * Constructs a newly allocated IntWrap object
	 * with the specified initial values assigned to field: <tt>intValue</tt>.
	 *
	 */

	public IntWrap(int val)
	{
		intValue = val;
	}

	/**
	 * Constructs a newly allocated IntWrap object
	 * with the initial values equals 0.
	 *
	 */

	public IntWrap()
	{
		intValue = 0;
	}

	/**
	 * Returns the value of field: <tt>intValue</tt>.
	 *
	 */

	public int getValue(){ return intValue; }

	/**
	 * Sets a new value of the field: <tt>intValue</tt>.
	 *
	 */

	public void setValue(int val) { intValue = val;}

	public void incValue() { intValue++;}

	public String toString()
	{
	  Integer int_val = new Integer(intValue);
	  return int_val.toString();
	}


}
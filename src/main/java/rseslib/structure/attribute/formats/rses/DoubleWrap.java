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


package rseslib.structure.attribute.formats.rses;

/**
 * Rough Set Library
 *
 * @author Jan Bazan
 *
 */


public class DoubleWrap
{
  	double doubleValue;

	public DoubleWrap(double val)
	{
		doubleValue = val;
	}

	public DoubleWrap()
	{
		doubleValue = 0.0;
	}

	public double getValue(){ return doubleValue; }


	public void setValue(double val) { doubleValue = val;}
	public void incValue() { doubleValue = doubleValue + 1.0;}

	public String toString()
	{
	  Double doubleVal = new Double(doubleValue);
	  return doubleVal.toString();
	}



}
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


package rseslib.structure.indiscernibility;

import rseslib.structure.data.DoubleData;

/**
 * Interface for data indiscernibility.
 * 
 * @author Rafal Latkowski
 */
public interface Indiscernibility
{
	/**
	 * Defines the indiscernibility between two objects.
	 * 
	 * @param object1	First object to be compared.
	 * @param object2	Second object to be compared.
	 * @return			True if the objects are indiscernible false otherwise.
	 */
	public boolean similar(DoubleData object1, DoubleData object2);

	/**
	 * Defines the indiscernibility between two values of an attribute.
	 * 
	 * @param value1	First value to be compared.
	 * @param value2	Second value to be compared.
	 * @param attribute	Attribute index.
	 * @return			True if the values are indiscernible false otherwise.
	 */
	public boolean similar(double value1, double value2, int attribute);
}
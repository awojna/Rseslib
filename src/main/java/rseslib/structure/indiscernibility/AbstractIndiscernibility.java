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


package rseslib.structure.indiscernibility;

import java.io.Serializable;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;

/**
 * Abstract class for data indiscernibility.
 * 
 * @author Rafal Latkowski
 */
public abstract class AbstractIndiscernibility implements Indiscernibility, Serializable
{
	/** Serialization version. */
	private static final long serialVersionUID = 1L;

	/**
	 * Defines the indiscernibility between two objects.
	 * 
	 * @param object1	First object to be compared.
	 * @param object2	Second object to be compared.
	 * @return			True if the objects are indiscernible false otherwise.
	 * @see rseslib.structure.indiscernibility.Indiscernibility#similar(rseslib.structure.data.DoubleData, rseslib.structure.data.DoubleData, int)
	 */
    public boolean similar(DoubleData object1,DoubleData object2)
    {
    	Header hdr = object1.attributes();
        for (int a=0; a<hdr.noOfAttr(); a++)
        	if (hdr.isConditional(a) && !similar(object1.get(a),object2.get(a),a)) return false;
        return true;
    }
}

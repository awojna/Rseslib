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


package rseslib.processing.transformation;

import java.io.Serializable;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.structure.function.doubleval.DoubleFunction;

/**
 * Function based transformer transforms data
 * with a vector of functions.
 *
 * @author      Arkadiusz Wojna
 */
public class FunctionBasedTransformer implements Transformer, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Header of transformed objects. */
	Header m_Header;
    /** Array of functions for new attributes. */
    DoubleFunction[] m_arrFunctions;

    /**
     * Constructor stores an array of attribute transformers.
     *
     * @param attributes	Header of new transformed objects.
     * @param fuctions		Array of functions to be stored.
     */
    public FunctionBasedTransformer(Header attributes, DoubleFunction[] functions)
    {
    	m_Header = attributes;
        m_arrFunctions = functions;
    }

	/**
	 * Constructor to be used only for deserialization.
	 */
	public FunctionBasedTransformer()
	{
	}
	
    /**
     * Transforms double data to new data object and returns the new object.
     * The original data remains unchanged.
     *
     * @param dObj Data object to be transformed.
     * @return     Data object with transformed attribute values.
     */
    public DoubleData transformToNew(DoubleData dObj)
    {
        DoubleData newDObj = new DoubleDataObject(m_Header);
        for (int att = 0; att < m_Header.noOfAttr(); att++)
        	newDObj.set(att, m_arrFunctions[att].doubleVal(dObj));
        return newDObj;
    }
}

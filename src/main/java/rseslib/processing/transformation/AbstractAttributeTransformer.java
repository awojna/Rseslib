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

/**
 * AttributeTransformer converts attribute values
 * for particular attributes in double data.
 * Defines the methods transform()
 * on the base of the methods from the interface DoubleData.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class AbstractAttributeTransformer implements AttributeTransformer, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Header of transformed objects. */
	Header m_Header;

	/**
	 * Constructor.
	 * 
	 * @param attributes Header of new transformed objects.
	 */
	public AbstractAttributeTransformer(Header attributes)
	{
		m_Header = attributes;
	}
	
	/**
	 * Constructor to be used only for deserialization.
	 */
	public AbstractAttributeTransformer()
	{
	}
	
	/**
     * Transforms double data changing the original data object.
     *
     * @param dObj Data object to be transformed.
     */
    public void transform(DoubleData dObj)
    {
        for (int att = 0; att < dObj.attributes().noOfAttr(); att++)
        	dObj.set(att, get(dObj.get(att), att));
    }

    /**
     * Transforms double data to new data object and returns the new object.
     * The orgininal data remains unchanged.
     *
     * @param dObj Data object to be transformed.
     * @return     Data object with transformed attribute values.
     */
    public DoubleData transformToNew(DoubleData dObj)
    {
        DoubleData newDObj = new DoubleDataObject(m_Header);
        for (int att = 0; att < dObj.attributes().noOfAttr(); att++)
        	newDObj.set(att, get(dObj.get(att), att));
        return newDObj;
    }
}

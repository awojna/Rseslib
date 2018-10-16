/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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

import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
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
public abstract class AttributeSelection implements Transformer, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/** Header of transformed objects. */
	private Header m_Header;
	/** Position of the selected attributes in the original header. */ 
	private int[] m_AttributePositions;

	/**
	 * Constructor.
	 * 
	 * @param attributes Header of new transformed objects.
	 */
	public AttributeSelection(Header header, boolean[] selectedAttrs)
	{
		if (selectedAttrs.length!=header.noOfAttr())
			throw new IllegalArgumentException("The length of an attribute mask is different from the header size.");
		int attrCounter = 0;
		for (int att = 0; att < header.noOfAttr(); att++)
			if (selectedAttrs[att]) attrCounter++;
		Attribute[] newAttrs = new Attribute[attrCounter];
		m_AttributePositions = new int[attrCounter];
		attrCounter = 0;
		for (int att = 0; att < header.noOfAttr(); att++)
			if (selectedAttrs[att])
			{
				newAttrs[attrCounter] = header.attribute(att);
				m_AttributePositions[attrCounter++] = att;
			}
		m_Header = new ArrayHeader(newAttrs, header.missing());
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
        for (int att = 0; att < m_AttributePositions.length; att++)
        	newDObj.set(att, dObj.get(m_AttributePositions[att]));
        return newDObj;
    }
}

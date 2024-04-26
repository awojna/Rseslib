/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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

import rseslib.structure.attribute.Header;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;

/**
 * Numeric function based transformer transforms data
 * with a vector of numeric functions.
 * Each numeric function corresponds
 * to a single attribute in the new data object.
 *
 * @author      Arkadiusz Wojna
 */
public class FunctionBasedAttributeTransformer extends AbstractAttributeTransformer
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

    /** Array of functions for particular attributes. */
    AttributeDoubleFunction[] m_arrAttributeTransformers;

    /**
     * Constructor stores an array of attribute transformers.
     *
     * @param attributes       Header of new transformed objects.
     * @param attrTransformers Array of attribute transformers to be stored.
     */
    public FunctionBasedAttributeTransformer(Header attributes, AttributeDoubleFunction[] attrTransformers)
    {
    	super(attributes);
        m_arrAttributeTransformers = attrTransformers;
    }

    /**
     * Returns the transformed value of a given attribute.
     *
     * @param attrVal   Attribute value to be transformed.
     * @param attrIndex Index of the attribute to be returned.
     * @return          The transformed value of a given attribute.
     */
    public double get(double attrVal, int attrIndex)
    {
        if (m_arrAttributeTransformers[attrIndex]!=null) return m_arrAttributeTransformers[attrIndex].doubleVal(attrVal);
        return attrVal;
    }
}

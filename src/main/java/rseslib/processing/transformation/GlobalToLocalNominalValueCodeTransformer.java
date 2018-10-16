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

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;

/**
 * Transformes global codes of nominal values
 * to local codes.
 *
 * @author      Arkadiusz Wojna
 */
public class GlobalToLocalNominalValueCodeTransformer extends AbstractAttributeTransformer
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param attributes       Header of transformed objects.
     */
    public GlobalToLocalNominalValueCodeTransformer(Header attributes)
    {
    	super(attributes);
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
        if (m_Header.isNominal(attrIndex))
            return ((NominalAttribute)m_Header.attribute(attrIndex)).localValueCode(attrVal);
        return attrVal;
    }
}

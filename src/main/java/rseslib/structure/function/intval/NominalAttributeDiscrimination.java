/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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


package rseslib.structure.function.intval;

import java.io.Serializable;

import rseslib.structure.attribute.*;
import rseslib.structure.data.DoubleData;

/**
 * Class implementing the discrimination function based on values
 * of a nominal attribute.
 *
 * @author      Arkadiusz Wojna
 */
public class NominalAttributeDiscrimination implements Discrimination, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
    /** Index of the nominal attribute to discriminate. */
    int m_nAttrIndex;
    /** Information about the nominal attribute to discriminate. */
    NominalAttribute m_AttributeWithLocalValueCoding;

    /**
     * Constructor.
     *
     * @param attr int Index of the nominal attribute to discriminate.
     * @param attrInfo Information about the nominal attribute to discriminate.
     */
    public NominalAttributeDiscrimination(int attr, NominalAttribute attrInfo)
    {
        m_nAttrIndex = attr;
        m_AttributeWithLocalValueCoding = attrInfo;
    }

    /**
     * Returns the number of branches.
     *
     * @return int Number of discrimination branches.
     */
    public int noOfValues()
    {
        return m_AttributeWithLocalValueCoding.noOfValues();
    }

    /**
     * Returns the branch number for a given double data.
     *
     * @param dObj Double data to be discriminated.
     * @return     branch number for a given double data.
     */
    public int intValue(DoubleData dObj)
    {
        return m_AttributeWithLocalValueCoding.localValueCode(dObj.get(m_nAttrIndex));
    }

    /**
     * Outputs a description of a discrimination for a given branch.
     *
     * @param branch The branch index for which the description is to be returned.
     * @return Description of a discrimination for a given branch.
     */
    public String toString(int branch)
    {
        return m_AttributeWithLocalValueCoding.name()+" = "
               +NominalAttribute.stringValue(m_AttributeWithLocalValueCoding.globalValueCode(branch));
    }
}

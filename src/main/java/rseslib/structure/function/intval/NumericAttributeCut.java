/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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

import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;

/**
 * Class implementing the discrimination function based on binary cut
 * of a numeric attribute.
 *
 * @author      Arkadiusz Wojna
 */
public class NumericAttributeCut implements Discrimination, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	/** Index of the numeric attribute to discriminate. */
    int m_nAttrIndex;
    /** The attribute value used for object discrimination. */
    double m_nCut;
    /** Information about the numeric attribute to discriminate. */
    NumericAttribute m_Attribute;

    /**
     * Constructor.
     *
     * @param attr Index of the nominal attribute to discriminate.
     * @param cut  Attribute value used for object discrimination.
     * @param attrInfo Information about the numeric attribute to discriminate.
     */
    public NumericAttributeCut(int attr, double cut, NumericAttribute attrInfo)
    {
        m_nAttrIndex = attr;
        m_nCut = cut;
        m_Attribute = attrInfo;
    }

    /**
     * Returns the number of branches.
     *
     * @return int Number of discrimination branches.
     */
    public int noOfValues()
    {
        return 2;
    }

    /**
     * Returns the branch number for a given double data.
     *
     * @param dObj Double data to be discriminated.
     * @return     branch number for a given double data.
     */
    public int intValue(DoubleData dObj)
    {
        return dObj.get(m_nAttrIndex)<m_nCut?0:1;
    }

    /**
     * Outputs a description of a discrimination for a given branch.
     *
     * @param branch The branch index for which the description is to be returned.
     * @return Description of a discrimination for a given branch.
     */
    public String toString(int branch)
    {
        if (branch==0) return m_Attribute.name()+" < "+m_nCut;
        if (branch==1) return m_Attribute.name()+" >= "+m_nCut;
        return null;
    }
}

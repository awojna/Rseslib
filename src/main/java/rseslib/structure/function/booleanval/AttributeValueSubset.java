/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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


package rseslib.structure.function.booleanval;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;

/**
 * Value subset inclusion test for an attribute.
 *
 * @author      Arkadiusz Wojna
 */
public class AttributeValueSubset implements BooleanFunction
{
	/** Attribute of this test. */
	NominalAttribute m_Attr;
    /** The index of an attribute to be tested. */
    int m_nAttributeIndex;
    /** The subset of values to be tested for inclusion. */
    double[] m_nSubsetOfValues;

    /**
     * Constructor.
     *
     * @param attrIndex         The index of an attribute to be tested.
     * @param subsetOfValues The subset of values to be tested for inclusion.
     */
    public AttributeValueSubset(NominalAttribute attr, int attrIndex, double[] subsetOfValues)
    {
    	m_Attr = attr;
        m_nAttributeIndex = attrIndex;
        m_nSubsetOfValues = subsetOfValues;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        double value = dObj.get(m_nAttributeIndex);
        for (int i = 0; i < m_nSubsetOfValues.length; i++)
            if (Double.isNaN(value) && Double.isNaN(m_nSubsetOfValues[i])
                || value==m_nSubsetOfValues[i]) return true;
        return false;
    }

    /**
     * Returns text representation.
     * 
     * @return	Text representation.
     */
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(m_Attr.name() + " in {");
    	boolean first = true;
    	for (double d : m_nSubsetOfValues) {
    		if (first)
    			sb.append(NominalAttribute.stringValue(d));
    		else
    			sb.append(","+NominalAttribute.stringValue(d));
    		first = false;
    	}
    	sb.append("}");
    	return sb.toString();
    }
}

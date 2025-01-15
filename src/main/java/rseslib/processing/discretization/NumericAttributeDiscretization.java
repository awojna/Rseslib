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


package rseslib.processing.discretization;

import java.util.Arrays;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;

/**
 * NumericAttributeDiscretization object converts
 * original numerical values to discretized values.
 * 
 * @author Rafal Latkowski
 */
public class NumericAttributeDiscretization extends AttributeDoubleFunction
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Information about the discretized attribute. */
    private NominalAttribute m_Attribute;
	/** Array of interval cutting points stored in sorted table. */
    private double[] m_aSortedCuts;

    /**
     * Constructor.
     * 
     * @param attrIndex		Attribute index.
     * @param attr			Original attribute information. 
     * @param cuts			The cuts, don't have to be sorted. 
     */
    public NumericAttributeDiscretization(int attrIndex, NumericAttribute attr, double[] cuts)
    {
        super(attrIndex);
        Attribute.Type type;
        if (attr.isConditional()) type = Attribute.Type.conditional;
        else if (attr.isDecision()) type = Attribute.Type.decision;
        else type = Attribute.Type.text;
        m_aSortedCuts = cuts;
        Arrays.sort(m_aSortedCuts);
        m_Attribute = new NominalAttribute(type, attr.name());
        if (m_aSortedCuts.length==0)
        	m_Attribute.globalValueCode("(-inf-+inf)");
        else
        {
        	m_Attribute.globalValueCode("(-inf - "+m_aSortedCuts[0]+")");
        	for (int interval_id = 1; interval_id < m_aSortedCuts.length; interval_id++)
        		m_Attribute.globalValueCode("["+m_aSortedCuts[interval_id-1]+" - "+m_aSortedCuts[interval_id]+")");
        	m_Attribute.globalValueCode("["+m_aSortedCuts[m_aSortedCuts.length-1]+" - +inf)");
        }
    }
    
    /**
     * Returns the information about the discretized attribute.
     * 
     * @return	Information about the discretized attribute.
     */
    public NominalAttribute getAttribute()
    {
    	return m_Attribute;
    }

    /**
     * Returns interval discretized value of original attribute value.
     * 
     * @param attrVal original attribute value
     * @return interval discretized value of original attribute value
     * @see rseslib.structure.function.doubleval.AttributeDoubleFunction#doubleVal(double)
     */
    public double doubleVal(double attrVal)
    {
        // missing values have to remain missing
        if (Double.isNaN(attrVal)) return Double.NaN;
        // regular discretization
        int interval_id = 0;
        while (interval_id<m_aSortedCuts.length && attrVal>=m_aSortedCuts[interval_id])
        	interval_id++;
        return m_Attribute.globalValueCode(interval_id);
    }
}

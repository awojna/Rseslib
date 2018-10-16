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


package rseslib.structure.data;

import java.util.BitSet;

/**
 * Data object with binary attributes
 * implemented with use of BitSet.
 *
 * @author      Arkadiusz Wojna
 */
public class BooleanDataObject implements BooleanData
{
    /** The vector of binary values for this data object. */
    private BitSet m_AttrValues;
    /** Decision value. */
    private double m_nDecision;

    /**
     * Constructs a data object with given binary values
     * and a given decision.
     *
     * @param values   The vector of binary values for this data object.
     * @param decision Decision.
     */
    public BooleanDataObject(BitSet values, int decision)
    {
	m_AttrValues = values;
	m_nDecision = decision;
    }

    /**
     * Sets the value of a given attribute to a given value.
     *
     * @param atrNo Index of the attribute to be changed.
     * @param value Attribute value.
     */
    public void setBooleanAttrVal(int atrNo, boolean value)
    {
        //sprawdzanie warunkow wejsciowych
        if (atrNo < 0 || atrNo >= m_AttrValues.length()) throw new RuntimeException("Wrong attribute index "+atrNo);
	if (value) m_AttrValues.set(atrNo);
	else m_AttrValues.clear(atrNo);
    }

    /**
     * Returns the value of a given attribute.
     *
     * @param attrN Index of the attribute to be changed.
     * @return      Attribute value.
     */
    public boolean getBooleanAttrVal(int attrN)
    {
        //sprawdzanie warunkow wejsciowych
        if (attrN < 0 && attrN >= m_AttrValues.length()) throw new RuntimeException("Wrong attribute index "+attrN);
	return m_AttrValues.get(attrN);
    }

    /**
     * Sets decision.
     *
     * @param decVal Decision value.
     */
    public void setDecision(double decVal)
    {
        m_nDecision = decVal;
    }

    /**
     * Returns decision.
     *
     * @return Decision value.
     */
    public double getDecision()
    {
        return m_nDecision;
    }

    /**
     * Constructs string representation of this data object.
     *
     * @return String representation of this data object.
     */
    public String toString()
    {
	StringBuffer strBuf = new StringBuffer();
	strBuf.append("BooleanDataObject <");
	for (int i = 0; i < m_AttrValues.length(); i++)
	{
	    if (m_AttrValues.get(i)) strBuf.append("1");
	    else strBuf.append("0");
	}
	strBuf.append(", dec=" + m_nDecision+">");
	return strBuf.toString();
    }
}

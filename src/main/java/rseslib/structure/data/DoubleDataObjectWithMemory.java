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


package rseslib.structure.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;

/**
 * Double data object that can store
 * and restore attribute values.
 *
 * @author      Arkadiusz Wojna
 */
public class DoubleDataObjectWithMemory extends DoubleDataObject
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Array of stored attribute values. */
    double[][] m_arrSavedAttrValues = new double[2][];

    /**
     * Constructs a new data object
     * with a given attribute types.
     *
     * @param attributes Array of attribute types.
     * @param number     Number of this data object.
     */
    public DoubleDataObjectWithMemory(Header attributes)
    {
        super(attributes);
    }

    /**
     * Constructs a new data object
     * with the same attribute values
     * as in a given data object.
     *
     * @param dObj Data object used for construction as a template.
     */
    public DoubleDataObjectWithMemory(DoubleData dObj)
    {
        super(dObj.attributes());
        for (int att = 0; att < m_arrAttrValues.length; att++)
            m_arrAttrValues[att] = dObj.get(att);
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_arrSavedAttrValues = new double[2][];    
    }

    /**
     * Stores the present attribute values.
     *
     * @param storeIndex Index of a store to be used for saving.
     */
    public void saveValues(int storeIndex)
    {
        if (storeIndex >= m_arrSavedAttrValues.length)
        {
            double[][] newSavedAttrValues = new double[storeIndex+1][];
            for (int store = 0; store < m_arrSavedAttrValues.length; store++)
                newSavedAttrValues[store] = m_arrSavedAttrValues[store];
            m_arrSavedAttrValues = newSavedAttrValues;
        }
        if (m_arrSavedAttrValues[storeIndex]==null)
            m_arrSavedAttrValues[storeIndex] = new double[m_arrAttrValues.length];
        for (int att = 0; att < m_arrSavedAttrValues[storeIndex].length; att++)
            m_arrSavedAttrValues[storeIndex][att] = m_arrAttrValues[att];
    }

    /**
     * Restores saved attribute values.
     *
     * @param storeIndex Index of a store with attribute values to be restored.
     */
    public void restoreSavedValues(int storeIndex)
    {
        if (storeIndex >= m_arrSavedAttrValues.length || m_arrSavedAttrValues[storeIndex]==null) throw new RuntimeException("Store "+storeIndex+" is empty");
        for (int att = 0; att < m_arrAttrValues.length; att++)
            m_arrAttrValues[att] = m_arrSavedAttrValues[storeIndex][att];
    }

    /**
     * Constructs string representation of this data object.
     *
     * @return String representation of this data object.
     */
    public String toString()
    {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("<");
        for (int i = 0; i < m_arrAttrValues.length; i++)
            if (m_arrAttributes.isConditional(i))
            {
                if (m_arrAttributes.isNumeric(i)) strBuf.append(m_arrSavedAttrValues[0][i]);
                else if (m_arrAttributes.isNominal(i)) strBuf.append(NominalAttribute.stringValue(m_arrSavedAttrValues[0][i]));
                if (i < m_arrAttrValues.length-1) strBuf.append(", ");
            }
        if (m_arrAttributes.nominalDecisionAttribute().isNominal())
            strBuf.append("dec = " + NominalAttribute.stringValue(m_arrSavedAttrValues[0][m_arrAttributes.decision()])+">");
        else strBuf.append("dec = " + getDecision()+">");
        return strBuf.toString();
    }
}

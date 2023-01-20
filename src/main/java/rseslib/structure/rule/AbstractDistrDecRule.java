/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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


package rseslib.structure.rule;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.vector.Vector;

/**
 * Abstract class for rule with distributed decision.
 * 
 * @author Rafal Latkowski
 */
public abstract class AbstractDistrDecRule implements DistributedDecisionRule, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

    /** Information about the decision attribute. */
    NominalAttribute m_DecAttr = null;
	/** Decision distribution of this rule. */
    Vector m_DecisionVector = null;
    /** Most probable decision. * */
    double m_nBestGlobalDecision = Double.NaN;
    
    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(m_DecAttr);
    	out.writeObject(m_DecisionVector);
    	out.writeInt(m_DecAttr.localValueCode(m_nBestGlobalDecision));
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_DecAttr = (NominalAttribute)in.readObject();
    	m_DecisionVector = (Vector)in.readObject();
    	m_nBestGlobalDecision = m_DecAttr.globalValueCode(in.readInt());
    }

    /**
     * Sets the decision distribution of this rule.
     *
     * @param decVec	Decision distribution to be set.
     * @param decAttr	Information about the decision attribute.
     */
    public void setDecisionVector(Vector decVec, NominalAttribute decAttr)
    {
    	m_DecAttr = decAttr;
        m_DecisionVector = decVec;
        int bestDec = 0;
        for (int dec = 1; dec < m_DecisionVector.dimension(); dec++)
            if (m_DecisionVector.get(dec) > m_DecisionVector.get(bestDec)) bestDec = dec;
        m_nBestGlobalDecision = m_DecAttr.globalValueCode(bestDec);
    }

    /**
     * Returns the decision of this rule.
     *
     * @return Decision of this rule.
     * @see rseslib.structure.rule.Rule#getDecision()
     */
    public double getDecision()
    {
        return m_nBestGlobalDecision;
    }

    /**
     * Returns the decision distribution of this rule.
     *
     * @return Decision distribution of this rule.
     * @see rseslib.structure.rule.DistributedDecisionRule#getDecisionVector()
     */
    public Vector getDecisionVector()
    {
        return m_DecisionVector;
    }
    
    /**
     * Returns the decision attribute of this rule.
     *
     * @return Decision attribute of this rule.
     */
    public NominalAttribute getDecisionAttribute()
    {
    	return m_DecAttr;
    }
}

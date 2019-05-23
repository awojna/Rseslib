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


package rseslib.structure.rule;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.function.booleanval.BooleanFunction;

/**
 * Decision rule with a deterministic decision.
 *
 * @author      Arkadiusz Wojna
 */
public class BooleanFunctionRule implements Rule
{
	/** Decision attrbiute */
	NominalAttribute m_DecisionAttr;
    /** Predecessor of this rule. */
    BooleanFunction m_Predecessor;
    /** Rule decision. */
    double m_nDecision;

    /**
     * Constructor of this rule.
     *
     * @param predecessor Predecessor of this rule.
     * @param decision    Rule decision.
     */
    public BooleanFunctionRule(BooleanFunction predecessor, double decision, NominalAttribute decAttr)
    {
    	m_DecisionAttr = decAttr;
        m_Predecessor = predecessor;
        m_nDecision = decision;
    }

    /**
     * Checks whether this rule matches a given data object.
     *
     * @param dObj Double data to be matched.
     * @return     True if this rule matches to a data object, false otherwise.
     */
    public boolean matches(DoubleData dObj)
    {
        return m_Predecessor.booleanVal(dObj);
    }

    /**
     * Returns the decision of this rule.
     *
     * @return Decision of this rule.
     */
    public double getDecision()
    {
        return m_nDecision;
    }

    /**
     * Provides text representation of this rule.
     * 
     * @return	Text representation of this rule.
     */
    public String toString()
    {
    	return m_Predecessor + " -> ( DEC = " + NominalAttribute.stringValue(m_nDecision) + " )";
    }
}

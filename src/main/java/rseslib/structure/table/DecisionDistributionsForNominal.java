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


package rseslib.structure.table;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.vector.Vector;
import rseslib.system.Report;

/**
 * Nominal attribute information
 * with decision distributions
 * computed for all values.
 *
 * @author      Arkadiusz Wojna
 */
public class DecisionDistributionsForNominal
{
    /** Attribute with global integer code to local integer code mapping. */
    private NominalAttribute m_Attribute;
    /** Decision distribution in a table. */
    private Vector m_TotalDecDistribution;
    /** Map from attribute values into decision vactors. */
    private Vector[] m_ValueDecDistributions;

    /**
     * Constructor computes decision distribution for all nominal values
     * of this attribute.
     *
     * @param objects  The set of data objects
     *                 that decision distributions are computed for.
     * @param index  Attribut index.
     */
    public DecisionDistributionsForNominal(DoubleDataTable objects, int index)
    {
        NominalAttribute decAttr = objects.attributes().nominalDecisionAttribute();
        m_Attribute = (NominalAttribute)objects.attributes().attribute(index);
        m_TotalDecDistribution = new Vector(decAttr.noOfValues());
        for (int d = 0; d < m_TotalDecDistribution.dimension(); d++)
            m_TotalDecDistribution.set(d, objects.getDecisionDistribution()[d]);
        m_TotalDecDistribution.normalizeWithCityNorm();
        m_ValueDecDistributions = new Vector[m_Attribute.noOfValues()];
        for (int val = 0; val < m_ValueDecDistributions.length; val++)
            m_ValueDecDistributions[val] = new Vector(decAttr.noOfValues());
        // wyliczanie wektorow rozkladu decyzji dla poszczegolnych wartosci atrybutu
        for (DoubleData obj : objects.getDataObjects())
        {
            int val = m_Attribute.localValueCode(obj.get(index));
            if (val != -1) m_ValueDecDistributions[val].increment(decAttr.localValueCode(((DoubleDataWithDecision)obj).getDecision()));
        }
        // unormowanie wektorow rozkladu decyzji
        for (int val = 0; val < m_ValueDecDistributions.length; val++)
            m_ValueDecDistributions[val].normalizeWithCityNorm();
    }

    /**
     * Extracts an array of decision distribution for particular nominal values.
     * Array indices correspond to local integer value codes from 0 to n-1.

     *
     * @return Array of decision distribution for particular nominal values.
     */
    public Vector[] getValueDecVectorsForLocalCodes()
    {
        return m_ValueDecDistributions;
    }

    /**
     * Returns the total decision distribution.
     *
     * @return The total decision distribution.
     */
    public Vector getTotalDecVector()
    {
        return m_TotalDecDistribution;
    }

    /**
     * Constructs string representation of this attribute.
     *
     * @return String representation of this attribute.
     */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("Atrybut symboliczny ("+m_ValueDecDistributions.length+" wartosci)"+Report.lineSeparator);
        for (int val = 0; val < m_ValueDecDistributions.length; val++)
            sbuf.append("Wartosc "+NominalAttribute.stringValue(m_Attribute.globalValueCode(val))+": "+m_ValueDecDistributions[val]+Report.lineSeparator);
        return sbuf.toString();
    }
}

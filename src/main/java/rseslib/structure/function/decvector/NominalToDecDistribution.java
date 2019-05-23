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


package rseslib.structure.function.decvector;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.DecisionDistributionsForNominal;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;

/**
 * The function translating the value of a nominal attribute
 * into decision distribution vector for this value.
 * If the flag m_bConvertToVectDecDifference is off,
 * the function returns the original nominal value
 * as the representation of the decision vector
 * or -1 for unknown nominal value.
 * If the flag m_bConvertToVectDecDifference is on,
 * it implies that the decision attribute has two values
 * and a decision vector has two coordinates
 * and the function returns the difference between
 * the first and the second coordinates of the decision vector.
 *
 * @author      Arkadiusz Wojna
 */
public class NominalToDecDistribution extends AttributeDoubleFunction
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Attribute with global integer code to local integer code mapping. */
    private NominalAttribute m_Attribute;
    /** Total decision distribution in a training table. */
    private Vector m_TotalDecDistribution;
    /** Map between nominal values of this attribute and decision distributions for these values. */
    private Vector[] m_ValueDecDistributions;
    /**
     * The flag is set to true if the decision attribute has two values
     * and then the function returns the difference between
     * the first and the second coordinates of the decision vector.
     */
    private boolean m_bConvertToVectDecDifference = false;

    /**
     * Constructor.
     *
     * @param attrInfo  Information about the considered nominal attribute with the map between value indices and decision vectors.
     * @param attrIndex Index of the considered attribute.
     */
    public NominalToDecDistribution(DoubleDataTable objects, int attrIndex)
    {
        super(attrIndex);
        m_Attribute = (NominalAttribute)objects.attributes().attribute(attrIndex);
        DecisionDistributionsForNominal decProbabilities = new DecisionDistributionsForNominal(objects, attrIndex); 
        m_TotalDecDistribution = decProbabilities.getTotalDecVector();
        m_ValueDecDistributions = decProbabilities.getValueDecVectorsForLocalCodes();
        if (m_TotalDecDistribution.dimension()==2) m_bConvertToVectDecDifference = true;
        else m_bConvertToVectDecDifference = false;
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
     *
    public DoubleVector getTotalDecVector()
    {
        return m_TotalDecDistribution;
    }

    /**
     * Returns the value of this function for a given attribute value.
     *
     * @param attrVal Attribute value.
     * @return        Value of this function for a given attribute value.
     */
    public double doubleVal(double attrVal)
    {
    	if (Double.isNaN(attrVal)) return Double.NaN;
        int intVal = m_Attribute.localValueCode(attrVal);
        if (m_bConvertToVectDecDifference)
        {
        	if (intVal<0 || intVal>=m_ValueDecDistributions.length)
        		return Double.NaN;
        	Vector decVect = m_ValueDecDistributions[intVal];
        	return (decVect.get(0)-decVect.get(1));
        }
        return intVal;
    }
}

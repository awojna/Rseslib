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


package rseslib.structure.function.doubleval;

import java.io.Serializable;

import rseslib.structure.table.NumericalStatistics;
import rseslib.system.PropertyConfigurationException;

/**
 * Scales a numeric attribute with a computed factor
 * according to a given scaling type.
 *
 * @author      Arkadiusz Wojna
 */
public class NumericScaler extends AttributeDoubleFunction implements Serializable
{
	/** Normalization types. */
	public enum Normalization { None, StdDev, Range; }

    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Scaling factor. */
    private double m_nScalingFactor;
    /** Maximal distance between two values of the scaled attribute after scaling. */
    private double m_nMaxDistance;
    /** Average value of the scaled attribute after scaling. */
    private double m_nAverage;

    /**
     * Constructor computes scaling factor according
     * to given type of normalisation.
     *
     * @param attrInfo          Information about the considered numeric attribute with statistics
     *                          from the distribution of attribute values in a training table.
     * @param attrIndex         Index of the considered attribute.
     * @param normalisationType Type of normalisation.
     */
    public NumericScaler(NumericalStatistics attrInfo, int attrIndex, Normalization normalisationType) throws PropertyConfigurationException
    {
    	super(attrIndex);
        switch (normalisationType)
        {
            case None:
                if (attrInfo.getMaximum() > attrInfo.getMinimum()) m_nScalingFactor = 1.0;
                else m_nScalingFactor = 0.0;
                break;
            case StdDev:
                if (attrInfo.getStandardDeviation() != 0) m_nScalingFactor = 0.5 / attrInfo.getStandardDeviation();
                else m_nScalingFactor = 0.0;
                break;
            case Range:
                if (attrInfo.getMaximum() > attrInfo.getMinimum()) m_nScalingFactor = 1/(attrInfo.getMaximum()-attrInfo.getMinimum());
                else m_nScalingFactor = 0.0;
                break;
            default:
                throw new PropertyConfigurationException("unknown normalisation type "+normalisationType+" for numeric attribute scaling");
        }
        if (m_nScalingFactor == 0.0) m_nMaxDistance = 1.0;
        else m_nMaxDistance = (attrInfo.getMaximum()-attrInfo.getMinimum())*m_nScalingFactor;
        m_nAverage = attrInfo.getAverage()*m_nScalingFactor;
    }

    /**
     * Returns the scaling factor.
     *
     * @return Scaling factor.
     */
    public double getScalingFactor()
    {
    	return m_nScalingFactor;
    }

    /**
     * Returns maximal distance between two values of the scaled attribute after scaling.
     *
     * @return Maximal distance between two values of the scaled attribute after scaling.
     */
    public double getMaxDistance()
    {
    	return m_nMaxDistance;
    }

    /**
     * Returns the average value of the scaled attribute after scaling.
     *
     * @return Average value of the scaled attribute after scaling.
     */
    public double getAverage()
    {
    	return m_nAverage;
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
    	return attrVal*m_nScalingFactor;
    }
}

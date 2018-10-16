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


package rseslib.structure.function.booleanval;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.AbstractWeightedMetric;

/**
 * Cube stretched on two data points
 * in a space of both numeric and nominal attributes.
 * For numeric attributes the value of a test object is required
 * to fall between the values of two data points.
 * For nominal attributes the value of the first data point
 * is required to be closer or equal to the value of a test object
 * than to the value of the second data point.
 *
 * @author      Grzegorz G�ra, Arkadiusz Wojna
 */
public class MixedCityAndMetricCube implements BooleanFunction
{
    /** Array of attributes. */
    private Header m_attrTypes;
    /** The first data point that defines the centre of this cube for nominal attributes. */
    private DoubleData m_Centre;
    /** Vector of radius for nominal attributes. */
    private double m_attrRadius[];
    /** Beginning points of numeric intervals. */
    private double m_intervalBeg[];
    /** Ending points of numeric intervals. */
    private double m_intervalEnd[];
    /** Metric used to define this cube. */
    private AbstractWeightedMetric m_Metric;

    /**
     * Constructor.
     *
     * @param dObj1  The first data object. It used as an end point of numeric intervals
     *               and the centre for nominal attibutes.
     * @param dObj2  The second data object. It used as an end point of numeric intervals
     *               and defines the radius for nominal attibutes.
     * @param metric Metric used to define this cube.
     */
    public MixedCityAndMetricCube(DoubleData dObj1, DoubleData dObj2, AbstractWeightedMetric metric)
    {
        m_attrTypes = dObj1.attributes();
        m_Metric = metric;
        if (dObj1.attributes().noOfAttr() != dObj2.attributes().noOfAttr()) throw new RuntimeException("The dimensions of data points is different");
        m_Centre = dObj1;
        m_attrRadius = new double[m_attrTypes.noOfAttr()];
        m_intervalBeg = new double[m_attrTypes.noOfAttr()];
        m_intervalEnd = new double[m_attrTypes.noOfAttr()];
        for (int i = 0; i < m_attrTypes.noOfAttr(); i++)
            if (m_attrTypes.isConditional(i))
            {
                if (m_attrTypes.isNominal(i))
                    m_attrRadius[i] = m_Metric.valueDist(dObj1.get(i), dObj2.get(i), i);
                else if (m_attrTypes.isNumeric(i))
                    if (Double.isNaN(dObj1.get(i)) || Double.isNaN(dObj2.get(i)) || dObj1.get(i) < dObj2.get(i))
                    {
                        m_intervalBeg[i] = dObj1.get(i);
                        m_intervalEnd[i] = dObj2.get(i);
                    }
                    else
                    {
                        m_intervalBeg[i] = dObj2.get(i);
                        m_intervalEnd[i] = dObj1.get(i);
                    }
            }
    }

    /**
     * Checks whether a given data point falls into this cube.
     *
     * @param dObj Data point to be checked.
     * @return     True if the data point falls into this cube, false otherwise.
     */
    public boolean booleanVal(DoubleData dObj)
    {
        for (int i = 0; i < m_attrTypes.noOfAttr(); i++)
            if (m_attrTypes.isConditional(i))
            {
                if (m_attrTypes.isNominal(i))
                {
                    // testowanie czy wartosc obiektu na tym atrybucie wpada do kuli
                    if (m_Metric.valueDist(m_Centre.get(i), dObj.get(i), i) > m_attrRadius[i]) return false;
                }
                else if (m_attrTypes.isNumeric(i))
                {
	            // wszystkie wartosci musza sie zawierac w przedzialach kostki
                if (!Double.isNaN(m_intervalBeg[i]) && !Double.isNaN(m_intervalEnd[i])
                    && (Double.isNaN(dObj.get(i)) || (m_intervalBeg[i] > dObj.get(i) || m_intervalEnd[i] < dObj.get(i)))) return false;
            }
        }
    return true;
}

    /**
     * Converts this cube to string.
     * For numeric attributes the end points of the interval are given.
     *
     * @return String representation of this cube.
     */
     public String toString()
     {
        StringBuffer sb = new StringBuffer(1024);
        sb.append("kostka[");
        for (int i = 0; i < m_attrTypes.noOfAttr(); i++)
            if (m_attrTypes.isConditional(i))
            {
                if (m_attrTypes.isNominal(i))
                {
                    if (m_attrRadius[i] == 0) sb.append(m_Centre.get(i));
                    else sb.append("(" + m_Centre.get(i) + ", r=" + m_attrRadius[i] + ")");
                    sb.append(',');
                }
                else if (m_attrTypes.isNumeric(i))
                {
                    if (m_intervalBeg[i] == m_intervalEnd[i])
                    {
                        sb.append(m_intervalBeg[i]);
                    }
                    else
                    {
                        sb.append(m_intervalBeg[i]);
                        sb.append('-');
                        sb.append(m_intervalEnd[i]);
                    }
                    sb.append(',');
                }
            }
        sb.append("]");
        return sb.toString();
     }

    /**
     * Converts this cube to string.
     * For numeric attributes the length of the interval are given.
     *
     * @return String representation of this cube.
     */
     public String toString2()
     {
        StringBuffer sb = new StringBuffer(1024);
        sb.append("kostka[");
        for (int i = 0; i < m_attrTypes.noOfAttr(); i++)
            if (m_attrTypes.isConditional(i))
            {
                if (m_attrTypes.isNominal(i))
                {
                    sb.append("["+i+"]=");
                    if (m_attrRadius[i] == 0) sb.append(m_Centre.get(i));
                    else sb.append("(" + m_Centre.get(i) + ", r=" + m_attrRadius[i] + ")");
                    sb.append(", ");
                }
                else if (m_attrTypes.isNumeric(i))
                {
                    sb.append("["+i+"]=");
                    sb.append(m_intervalEnd[i]-m_intervalBeg[i]);
                    sb.append(", ");
                }
            }
        sb.append("]");
        return sb.toString();
     }
}

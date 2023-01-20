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


package rseslib.structure.function.booleanval;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.AbstractWeightedMetric;

/**
 * Cube stretched on two data points
 * in a space of both numeric and nominal attributes.
 * The value of the first data point is required
 * to be closer or equal to the value of a test object
 * than to the value of the second data point.
 *
 * @author      Grzegorz Gora, Arkadiusz Wojna
 */
public class MetricCube implements BooleanFunction
{
    /** Array of attributes. */
    private Header m_attrTypes;
    /** The first data point that defines the centre of this cube. */
    private DoubleData m_Centre;
    /** Vector of radius for attributes. */
    private double m_attrRadius[];
    /** Metric used to define this cube. */
    private AbstractWeightedMetric m_Metric;

    /**
     * Constructor.
     *
     * @param dObj1  The first data object. It used as the centre.
     * @param dObj2  The second data object. It defines the radius for attibutes.
     * @param metric Metric used to define this cube.
     */
    public MetricCube(DoubleData dObj1, DoubleData dObj2, AbstractWeightedMetric metric)
    {
        m_attrTypes = dObj1.attributes();
        m_Metric = metric;
        if (dObj1.attributes().noOfAttr() != dObj2.attributes().noOfAttr()) throw new RuntimeException("The dimensions of data points is different");
        m_Centre = dObj1;
        m_attrRadius = new double[m_attrTypes.noOfAttr()];
        for (int i = 0; i < m_attrTypes.noOfAttr(); i++)
            if (m_attrTypes.isConditional(i))
                m_attrRadius[i] = m_Metric.valueDist(dObj1.get(i), dObj2.get(i), i);
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
                // testowanie czy wartosc obiektu na tym atrybucie wpada do kuli
                if (m_Metric.valueDist(m_Centre.get(i), dObj.get(i), i) > m_attrRadius[i]) return false;
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
                if (m_attrRadius[i] == 0) sb.append(m_Centre.get(i));
                else sb.append("(" + m_Centre.get(i) + ", r=" + m_attrRadius[i] + ")");
                sb.append(',');
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
                sb.append("["+i+"]=");
                if (m_attrRadius[i] == 0) sb.append(m_Centre.get(i));
                else sb.append("(" + m_Centre.get(i) + ", r=" + m_attrRadius[i] + ")");
                sb.append(", ");
            }
        sb.append("]");
        return sb.toString();
     }
}

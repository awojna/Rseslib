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


package rseslib.structure.metric;

import java.util.Properties;

import rseslib.processing.transformation.AttributeTransformer;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.table.NumericalStatistics;
import rseslib.system.PropertyConfigurationException;

/**
 * Metric combining the Manhattan city metric for numeric attributes
 * and Hamming's metric for nominal attributes.
 * This metric accepts null values. The distance between a null
 * and a non-null value for a single attribute is equal
 * to the maximal possible distance for this attribute
 * and the distance between two null values is 0.
 *
 * @author      Arkadiusz Wojna
 */
public class CityHammingMetric extends AbstractWeightedMetric                                       
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Array of the maximal distances for particular numeric attributes. */
    private double[] m_arrMaxDistances;

    /**
     * Constructor.
     *
     * @param prop       Properties of the metric to be induced.
     * @param tab        The set of data objects used to induce this metric.
     */
    public CityHammingMetric(Properties prop, DoubleDataTable tab) throws PropertyConfigurationException
    {
        super(prop, tab.attributes().noOfAttr());
        m_attrTypes = tab.attributes();
        m_arrMaxDistances = new double[m_attrTypes.noOfAttr()];
        for (int attr = 0; attr < m_arrMaxDistances.length; attr++)
            if (m_attrTypes.isConditional(attr) && m_attrTypes.isNumeric(attr))
            {
                NumericalStatistics attrWithStats = new NumericalStatistics(tab.getDataObjects(), attr);
                m_arrMaxDistances[attr] = attrWithStats.getMaximum() - attrWithStats.getMinimum();
            }
    }

    /**
     * Turns off transformation of values inside metric
     * and passes the transformer out.
	 * From this moment a user must assure
	 * that objects are transformed
	 * before a distance computation function is called.
     * External transformation can be more effective:
     * each object can be transformed only once.
     * With internal transformation the values of an object
     * are transformed each time when it is used in
     * a distance computing function. 
     * 
     * @return    Transformer.
     */
    public AttributeTransformer transformationOutside()
    {
        return null;
    }

    /**
     * Returns the distance between two attribute values for a single numeric attribute.
     *
     * @param val1 The first attribute value.
     * @param val2 The second attribute value.
     * @param attr Index of an attibute to be used for distance measure.
     * @return     Distance between the values val1 and val2.
     */
    private double numDist(double val1, double val2, int attr)
    {
        if (Double.isNaN(val1) || Double.isNaN(val2)) return 1;
        if (m_arrMaxDistances[attr] > 0.0)
            return Math.abs(val2-val1)/m_arrMaxDistances[attr];
        if (val1==val2) return 0;
        else return 1;
    }

    /**
     * Returns the distance between two attribute values for a single attribute.
     *
     * @param val1 The first attribute value.
     * @param val2 The second attribute value.
     * @param attr Index of an attibute to be used for distance measure.
     * @return     Distance between the values val1 and val2.
     */
    public double valueDist(double val1, double val2, int attr)
    {
        if (m_attrTypes.isNumeric(attr)) return numDist(val1, val2, attr);
        if (m_attrTypes.isNominal(attr)
        		&& (Double.isNaN(val1) || Double.isNaN(val2) || val1 != val2))
        	return 1;
        return 0;
    }
}

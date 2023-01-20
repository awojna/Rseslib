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


package rseslib.structure.metric;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Abstract metric enriched with weights
 * assigned to attributes.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class AbstractWeightedMetric extends Configuration implements Metric, Serializable
{
    /** Metric types. */
    enum MetricType implements Serializable { City, Euclidean, Maximum, Indexed; };

    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	/** Parameter name for the type of this metric. */
    private static final String METRIC_TYPE_NAME_PROPERTY_NAME = "metricType";
    /** Parameter name for the index of this metric used only if the type is indexed. */
    private static final String METRIC_INDEX_PROPERTY_NAME = "metricIndex";

    /** Type of this metric. */
    MetricType m_nMetricType;
    /** Index of this metric used only with the indexed metric type. */
    double m_nMetricIndex = 0;
    /** Information about attributes. */
    Header m_attrTypes;
	/** Weights of attributes. */
    double[] m_arrWeights;
    /** Number of weighting iterations. */
    private int m_NoOfIterations = 0;

    /**
     * Constructor.
     *
     * @param prop   Properties of the metric to be induced.
     * @param trans  Transformer of the attribute values.
     * @param noOfAttr     Number of attributes.
     */
    public AbstractWeightedMetric(Properties prop, int noOfAttr) throws PropertyConfigurationException
    {
        super(prop);
    	try
    	{
    		m_nMetricType = MetricType.valueOf(getProperty(METRIC_TYPE_NAME_PROPERTY_NAME));
    	}
    	catch (IllegalArgumentException e)
    	{
			throw new PropertyConfigurationException("Unknown metric type: "+getProperty(METRIC_TYPE_NAME_PROPERTY_NAME));
        }
        if (m_nMetricType == MetricType.Indexed)
            m_nMetricIndex = getDoubleProperty(METRIC_INDEX_PROPERTY_NAME);
        m_arrWeights = new double[noOfAttr];
        for (int att = 0; att < m_arrWeights.length; att++) m_arrWeights[att] = 1;
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	writeConfiguration(out);
    	out.defaultWriteObject();
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readConfiguration(in);
    	in.defaultReadObject();
    }

    /**
     * Returns the distance between two attribute values for a single attribute.
     *
     * @param val1 The first attribute value.
     * @param val2 The second attribute value.
     * @param attr Index of an attibute to be used for distance measure.
     * @return     Distance between the values val1 and val2.
     */
    public abstract double valueDist(double val1, double val2, int attr);

    /**
     * Returns the distance between two data objects.
     *
     * @param datObj1 First data object to be compared.
     * @param datObj2 Second data object to be compared.
     * @return        Distance between the objects datObj1 and datObj2.
     */
    public double dist(DoubleData datObj1, DoubleData datObj2)
    {
        switch (m_nMetricType)
        {
            case Indexed:
                return distIndexed(datObj1, datObj2);
            case City:
                return distCity(datObj1, datObj2);
            case Euclidean:
                return distEuclidean(datObj1, datObj2);
            case Maximum:
                return distMaximum(datObj1, datObj2);
            default:
                throw new RuntimeException("Unknown metric type "+m_nMetricType);
        }
    }

    /**
     * Returns the Manhattan city distance between two data objects with double values.
     *
     * @param datObj1 First data object to be compared.
     * @param datObj2 Second data object to be compared.
     * @return        Manhattan city distance between the objects datObj1 and datObj2.
     */
    double distCity(DoubleData datObj1, DoubleData datObj2)
    {
        double dist = 0;
        for (int att = 0; att < m_attrTypes.noOfAttr(); att++)
            if (m_attrTypes.isConditional(att))
                dist += valueDist(datObj1.get(att), datObj2.get(att), att)*m_arrWeights[att];
        return dist;
    }

    /**
     * Returns the euclidean distance between two data objects with double values.
     *
     * @param datObj1 First data object to be compared.
     * @param datObj2 Second data object to be compared.
     * @return        Euclidean distance between the objects datObj1 and datObj2.
     */
    double distEuclidean(DoubleData datObj1, DoubleData datObj2)
    {
        double dist = 0;
        for (int att = 0; att < m_attrTypes.noOfAttr(); att++)
            if (m_attrTypes.isConditional(att))
            {
                double distVal = valueDist(datObj1.get(att), datObj2.get(att), att);
                dist += distVal*distVal*m_arrWeights[att];
            }
        return Math.sqrt(dist);
    }

    /**
     * Returns the maximum distance between two data objects with double values.
     *
     * @param datObj1 First data object to be compared.
     * @param datObj2 Second data object to be compared.
     * @return        Maximum distance between the objects datObj1 and datObj2.
     */
    double distMaximum(DoubleData datObj1, DoubleData datObj2)
    {
        double dist = 0;
        for (int att = 0; att < m_attrTypes.noOfAttr(); att++)
            if (m_attrTypes.isConditional(att))
            {
                double valDist = valueDist(datObj1.get(att), datObj2.get(att), att)*m_arrWeights[att];
                if (valDist > dist) dist = valDist;
            }
        return dist;
    }

    /**
     * Returns an indexed distance between two data objects with double values.
     *
     * @param datObj1 First data object to be compared.
     * @param datObj2 Second data object to be compared.
     * @return        Indexed distance between the objects datObj1 and datObj2.
     */
    double distIndexed(DoubleData datObj1, DoubleData datObj2)
    {
        double dist = 0;
        for (int att = 0; att < m_attrTypes.noOfAttr(); att++)
            if (m_attrTypes.isConditional(att))
                dist += Math.pow(valueDist(datObj1.get(att), datObj2.get(att), att), m_nMetricIndex)*m_arrWeights[att];
        return dist;
    }

    /**
     * Sets the weight of the attribute attrInd to the new value.
     *
     * @param attrInd Index of the attribute to be changed.
     * @param weight  A new weight value to be set.
     */
    public void setWeight(int attrInd, double weight)
    {
        m_arrWeights[attrInd] = weight;
    }

    /**
     * Returns the value of the weight of the attribute.
     *
     * @param attrInd Index of the attribute to deliver the weight.
     * @return        The value of the weight.
     */
    public double getWeight(int attrInd)
    {
        return m_arrWeights[attrInd];
    }

    /**
     * Sets the number of weighting iterations.
     *
     * @param noOfIterations Number of weighting iterations.
     */
    public void setNoOfWeightingIterations(int noOfIterations)
    {
        m_NoOfIterations = noOfIterations;
    }

    /**
     * Returns the number of weighting iterations.
     *
     * @return Number of weighting iterations.
     */
    public int getNoOfWeightingIterations()
    {
        return m_NoOfIterations;
    }

    /**
     * Returns attribute types for this object.
     *
     * @return Attribute types for this object.
     */
    public Header attributes()
    {
        return m_attrTypes;
    }

    /**
     * Constructs a string containing the list of weight values.
     *
     * @return String containing the list of weight values
     */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<");
        for (int i = 0; i < m_arrWeights.length; i++)
        {
            sbuf.append(i+"="+m_arrWeights[i]);
            if (i < m_arrWeights.length-1) sbuf.append(", ");
            else sbuf.append(">");
        }
        return sbuf.toString();
    }
}

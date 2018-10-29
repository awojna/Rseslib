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


package rseslib.structure.metric;

import java.util.Properties;

import rseslib.processing.transformation.AttributeTransformer;
import rseslib.processing.transformation.FunctionBasedAttributeTransformer;
import rseslib.structure.function.decvector.NominalToDecDistribution;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.function.doubleval.NumericScaler;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;
import rseslib.system.PropertyConfigurationException;

/**
 * Metric combining the Manhattan city metric for numeric attributes
 * and the Simple Value Difference metric for nominal attributes.
 * This metric accepts null values. The distance between a null
 * and a non-null value for a single attribute is equal
 * to the maximal possible distance for this attribute
 * and the distance between two null values is 0.
 *
 * @author      Grzegorz Gora, Arkadiusz Wojna
 */
public class CitySVDMetric extends AbstractWeightedMetric
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

    /**
     * Transformer of the attribute values.
	 * If null then objects must be transformed outside
     * before a distance computation function is called.
     */
    AttributeTransformer m_Transformer = null;
	/** Array of the maximal distances for particular numeric attributes. */
    private double[] m_arrMaxDistances;
    /** Array of the average values for particular numeric attributes. */
    private double[] m_arrAverages;
    /** Number of values for particular attributes. */
    private int[] m_arrNoOfValues;
    /** Distributions of the decision attribute for particular values of particular nominal attributes. */
    private Vector[][] m_arrDecDistr;
    /** Distances computed between nominal values for particular nominal attributes. */
    private double[][][] m_arrDeltaWeights;
    /**
     * True if the decision attribute has two values
     * and each nominal value of a data object
     * is represented as the difference between
     * the first and the second coordinate of the decision vector
     * corresponding to this nominal value.
     * Otherwise it is false.
     */
    private boolean m_bDecisionVectorAsDecisionValueDifference;

    /**
     * Constructor.
     *
     * @param prop       Properties of this metric.
     * @param tab        Training table used to induce transformer.
     */
    public CitySVDMetric(Properties prop, DoubleDataTable tab) throws PropertyConfigurationException
    {
        super(prop, tab.attributes().noOfAttr());
        m_attrTypes = tab.attributes();
        
        // construct transformer
        m_arrMaxDistances = new double[m_attrTypes.noOfAttr()];
        m_arrAverages = new double[m_attrTypes.noOfAttr()];
        m_arrNoOfValues = new int[m_attrTypes.noOfAttr()];
        m_arrDecDistr = new Vector[m_attrTypes.noOfAttr()][];
        AttributeDoubleFunction[] transFunctions = new AttributeDoubleFunction[m_attrTypes.noOfAttr()];
        for (int attr = 0; attr < m_attrTypes.noOfAttr(); attr++)
            if (m_attrTypes.isConditional(attr))
            {
                if (m_attrTypes.isNumeric(attr))
                {
                	NumericScaler scaler = new NumericScaler(tab.getNumericalStatistics(attr), attr, NumericScaler.Normalization.Range);
                	m_arrAverages[attr] = scaler.getAverage();
                	m_arrMaxDistances[attr] = scaler.getMaxDistance();
                	transFunctions[attr] = scaler;
                }
                else if (m_attrTypes.isNominal(attr))
                {
                	NominalToDecDistribution nomFunc = new NominalToDecDistribution(tab, attr);
                	m_arrDecDistr[attr] = nomFunc.getValueDecVectorsForLocalCodes();
                	m_arrNoOfValues[attr] = m_arrDecDistr[attr].length;
                	transFunctions[attr] = nomFunc;
                }
            }
        m_Transformer = new FunctionBasedAttributeTransformer(m_attrTypes, transFunctions);
        
        m_bDecisionVectorAsDecisionValueDifference = (m_attrTypes.nominalDecisionAttribute().noOfValues()==2);
        
        // precompute distances between nominal values
        m_arrDeltaWeights = new double[m_attrTypes.noOfAttr()][][];
        for (int att = 0; att < m_attrTypes.noOfAttr(); att++)
            if (m_attrTypes.isConditional(att) && m_attrTypes.isNominal(att))
            {
                m_arrDeltaWeights[att] = new double[m_arrNoOfValues[att]][m_arrNoOfValues[att]];
                for (int i = 0; i < m_arrDeltaWeights[att].length; i++)
                    for (int j = 0; j < m_arrDeltaWeights[att][i].length; j++)
                        if (i==j) m_arrDeltaWeights[att][i][j] = 0;
                        else if (m_arrDecDistr[att][i]==null || m_arrDecDistr[att][j]==null) m_arrDeltaWeights[att][i][j] = 1;
                        else m_arrDeltaWeights[att][i][j] = Vector.cityDist(m_arrDecDistr[att][i], m_arrDecDistr[att][j]);
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
    	AttributeTransformer trans = m_Transformer;
    	m_Transformer = null;
        return trans;
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
        if (m_Transformer!=null)
        {
            val1 = m_Transformer.get(val1, attr);
            val2 = m_Transformer.get(val2, attr);
        }
        if (m_attrTypes.isNumeric(attr))
        {
            if (Double.isNaN(val1) || Double.isNaN(val2)) return m_arrMaxDistances[attr];
            return Math.abs(val2-val1);
        }
        else
        {
        	if (Double.isNaN(val1) || Double.isNaN(val2)) return 2;
            if (m_bDecisionVectorAsDecisionValueDifference) return Math.abs(val2-val1);
            int intVal1 = (int)val1;
            int intVal2 = (int)val2;
            if (intVal1<0 || intVal2<0 || intVal1>=m_arrNoOfValues[attr] || intVal2>=m_arrNoOfValues[attr])
            	return 2;
            return m_arrDeltaWeights[attr][intVal1][intVal2];
        }
    }
}

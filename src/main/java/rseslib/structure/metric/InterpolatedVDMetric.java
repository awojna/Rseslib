/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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
import rseslib.structure.function.decvector.NumericToInterpolatedDecDistribution;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;
import rseslib.system.PropertyConfigurationException;

/**
 * The Value Difference metric for data objects
 * with both numeric and nominal attributes.
 * The distance for the numeric values is computed
 * as the distance between decision distributions
 * where decision distribution for a numeric value
 * is computed as the interpolation between distributions
 * of the adjecent intervals.
 * This metric accepts null values and treats the null value
 * as a separated nominal value.
 *
 * @author      Arkadiusz Wojna
 */
public class InterpolatedVDMetric extends AbstractWeightedMetric
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

    /**
     * Transformer of the attribute values.
	 * If null then objects must be transformed outside
     * before a distance computation function is called.
     */
    AttributeTransformer m_Transformer = null;
	/** Distributions of the decision attribute for particular values of particular nominal attributes. */
    private Vector[][] m_arrDecDistr;
    /** Distances computed between nominal values for particular attributes. */
    private double[][][] m_arrDeltaWeights;
    /*
     * True if the decision attribute has two values
     * and each nominal value of a data object
     * is represented as the difference between
     * the first and the second coordinate of the decision vector
     * corresponding to this nominal value.
     * Otherwise it is false.
     */
    private boolean m_bDecisionVectorAsDecisionValueDifference;
    /** Decision vector interpolated for the first value. */
    private Vector m_VectorForValue1;
    /** Decision vector interpolated for the second value. */
    private Vector m_VectorForValue2;

    /**
     * Constructor.
     *
     * @param prop       Properties of this metric.
     * @param tab        Training table used to induce transformer.
     */
    public InterpolatedVDMetric(Properties prop, DoubleDataTable tab) throws PropertyConfigurationException
    {
        super(prop, tab.attributes().noOfAttr());
        m_attrTypes = tab.attributes();

        // construct transformer
        m_arrDecDistr = new Vector[m_attrTypes.noOfAttr()][];
        AttributeDoubleFunction[] transFunctions = new AttributeDoubleFunction[m_attrTypes.noOfAttr()];
        for (int attr = 0; attr < m_attrTypes.noOfAttr(); attr++)
            if (m_attrTypes.isConditional(attr))
            {
               if (m_attrTypes.isNumeric(attr))
               {
            	   NumericToInterpolatedDecDistribution numFunc = new NumericToInterpolatedDecDistribution(tab, attr); 
            	   m_arrDecDistr[attr] = numFunc.getDiscretisedDecVectors();
            	   transFunctions[attr] = numFunc; 
               }
               else if (m_attrTypes.isNominal(attr))
               {
            	   NominalToDecDistribution numFunc = new NominalToDecDistribution(tab, attr);
            	   m_arrDecDistr[attr] = numFunc.getValueDecVectorsForLocalCodes();
            	   transFunctions[attr] = numFunc;
               }
            }
        m_Transformer = new FunctionBasedAttributeTransformer(m_attrTypes, transFunctions);
        
        m_bDecisionVectorAsDecisionValueDifference = (m_attrTypes.nominalDecisionAttribute().noOfValues()==2);
        m_VectorForValue1 = new Vector(m_attrTypes.nominalDecisionAttribute().noOfValues());
        m_VectorForValue2 = new Vector(m_attrTypes.nominalDecisionAttribute().noOfValues());

        // precompute distances between nominal values
        m_arrDeltaWeights = new double[m_attrTypes.noOfAttr()][][];
        for (int att = 0; att < m_attrTypes.noOfAttr(); att++)
            if (m_attrTypes.isConditional(att) && m_attrTypes.isNominal(att))
            {
                m_arrDeltaWeights[att] = new double[m_arrDecDistr[att].length][m_arrDecDistr[att].length];
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
     * Returns the distance between two attribute values for a single numeric attribute.
     *
     * @param val1 The first attribute value.
     * @param val2 The second attribute value.
     * @param attr Index of an attibute to be used for distance measure.
     * @return     Distance between the values val1 and val2.
     */
    private double numDist(double val1, double val2, int attr)
    {
        if (Double.isNaN(val1) || Double.isNaN(val2)) return 2;
        if (val1==val2) return 0;
        double val = val1;
        Vector vectorForVal = m_VectorForValue1;
        for (int v = 0; v < 2; v++)
        {
            boolean setZero = false;
            if (val <= 0.0 || val >= (double)m_arrDecDistr[attr].length) setZero = true;
            int left = (int)val;
            if (setZero) vectorForVal.reset();
            else if (val == (double)left) vectorForVal.set(m_arrDecDistr[attr][left]);
            else
            {
                double leftWeight = (double)(left + 1) - val;
                double rightWeight = val - (double)left;
                if (val < 1.0)
                    for (int d = 0; d < vectorForVal.dimension(); d++)
                        vectorForVal.set(d, m_arrDecDistr[attr][left+1].get(d)*rightWeight);
                else if (val > (double)(m_arrDecDistr[attr].length - 1))
                    for (int d = 0; d < vectorForVal.dimension(); d++)
                        vectorForVal.set(d, m_arrDecDistr[attr][left].get(d)*leftWeight);
                else
                    for (int d = 0; d < vectorForVal.dimension(); d++)
                        vectorForVal.set(d, m_arrDecDistr[attr][left].get(d)*leftWeight+m_arrDecDistr[attr][left+1].get(d)*rightWeight);
            }
            val = val2;
            vectorForVal = m_VectorForValue2;
        }
        return Vector.cityDist(m_VectorForValue1, m_VectorForValue2);
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
        if (m_attrTypes.isNumeric(attr)) return numDist(val1, val2, attr);
        if (m_attrTypes.isNominal(attr))
        {
        	if (Double.isNaN(val1) || Double.isNaN(val2)) return 2;
        	if (m_bDecisionVectorAsDecisionValueDifference) return Math.abs(val1 - val2);
            int intVal1 = (int)val1;
            int intVal2 = (int)val2;
            if (intVal1<0 || intVal2<0 || intVal1>=m_arrDeltaWeights[attr].length || intVal2>=m_arrDeltaWeights[attr][intVal1].length)
            	return 2;
            return m_arrDeltaWeights[attr][intVal1][intVal2];
        }
        return 0;
    }
}

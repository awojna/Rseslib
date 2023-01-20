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

import java.util.Properties;

import rseslib.processing.transformation.AttributeTransformer;
import rseslib.processing.transformation.FunctionBasedAttributeTransformer;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.function.decvector.NominalToDecDistribution;
import rseslib.structure.function.decvector.NumericToVicinityDecDistribution;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;
import rseslib.system.PropertyConfigurationException;

/**
 * The Simple Value Difference metric for data objects
 * with both numeric and nominal attributes.
 * The SVD distance for the numeric values is computed
 * as the distance between decision distributions
 * from certain neighbourhoods of this numeric values.
 * This metric accepts null values and treats the null value
 * as the separated nominal values.
 *
 * @author      Arkadiusz Wojna
 */
public class DensityBasedVDMetric extends AbstractWeightedMetric
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	/** Property name for metric type. */
    public static final String VICINITY_SIZE_PROPERTY_NAME = "vicinitySize";

    /**
     * Transformer of the attribute values.
	 * If null then objects must be transformed outside
     * before a distance computation function is called.
     */
    AttributeTransformer m_Transformer = null;
    /** Distributions of the decision for particular values of particular attributes. */
    private Vector[][] m_arrDecDistr;
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
    public DensityBasedVDMetric(Properties prop, DoubleDataTable tab) throws PropertyConfigurationException
    {
        super(prop, tab.attributes().noOfAttr());
        Attribute[] newAttributes = new Attribute[tab.attributes().noOfAttr()];
        for (int att = 0; att < newAttributes.length; att++)
            if (tab.attributes().isConditional(att)) newAttributes[att] = new NominalAttribute(Attribute.Type.conditional, tab.attributes().attribute(att).name());
            else newAttributes[att] = tab.attributes().attribute(att);
        m_attrTypes = new ArrayHeader(newAttributes, tab.attributes().missing());
        
        // construct transformer
        int vicinitySize = getIntProperty(VICINITY_SIZE_PROPERTY_NAME);
        m_arrDecDistr = new Vector[m_attrTypes.noOfAttr()][];
        AttributeDoubleFunction[] transFunctions = new AttributeDoubleFunction[m_attrTypes.noOfAttr()];
        for (int attr = 0; attr < m_attrTypes.noOfAttr(); attr++)
            if (m_attrTypes.isConditional(attr))
            {
               if (tab.attributes().isNumeric(attr))
               {
            	   NumericToVicinityDecDistribution numFunc = new NumericToVicinityDecDistribution(tab, attr, vicinitySize); 
            	   m_arrDecDistr[attr] = numFunc.getVicinityDecVectors();
            	   transFunctions[attr] = numFunc; 
               }
               else if (tab.attributes().isNominal(attr))
               {
            	   NominalToDecDistribution numFunc = new NominalToDecDistribution(tab, attr);
            	   m_arrDecDistr[attr] = numFunc.getValueDecVectorsForLocalCodes();
            	   transFunctions[attr] = numFunc;
               }
            }
        m_Transformer = new FunctionBasedAttributeTransformer(tab.attributes(), transFunctions);
        
        m_bDecisionVectorAsDecisionValueDifference = (m_attrTypes.nominalDecisionAttribute().noOfValues()==2);
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
    	if (Double.isNaN(val1) || Double.isNaN(val2)) return 2;
        if (m_bDecisionVectorAsDecisionValueDifference) return Math.abs(val1 - val2);
        int intVal1 = (int)val1;
        int intVal2 = (int)val2;
        if (intVal1<0 || intVal2<0 || intVal1>=m_arrDecDistr[attr].length || intVal2>=m_arrDecDistr[attr].length)
        	return 2;
        return Vector.cityDist(m_arrDecDistr[attr][intVal1], m_arrDecDistr[attr][intVal2]);
    }
}

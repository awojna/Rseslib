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


package rseslib.processing.discretization;

import java.util.Properties;

import rseslib.processing.transformation.AttributeTransformer;
import rseslib.processing.transformation.FunctionBasedAttributeTransformer;
import rseslib.processing.transformation.TransformationProvider;
import rseslib.processing.transformation.Transformer;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * AbstractDiscretizationProvider can be used as the base class
 * for the methods that discretize each attribute independently.
 * 
 * @author Rafal Latkowski
 */
public abstract class AbstractDiscretizationProvider extends Configuration implements TransformationProvider
{
    /**
     * Constructs initial object for generating discretization.
     * This object do not require any initialization.
     * 
     * @param prop              Map between property names and property values.
     */
    public AbstractDiscretizationProvider(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
    }

    /**
     * Creates discretization cuts for one attribute.
     * Main method of this discretization provider.
     * 
     * @param attribute				Selected attribute for discretization.
     * @param table data			Used for estimating the best cuts.
     * @return Discretization cuts with specified number of intervals
     */
    abstract double[] generateCuts(int attribute, DoubleDataTable table);
    
    /**
     * Method that generate discretization based on data table.
     * Each attribute is discretized independently.
     * 
     * @param table 				Data table to estimate the discretization.
     * @return
     */
    public AttributeTransformer generateDiscretization(DoubleDataTable table)
    {
    	Attribute[] attributes = new Attribute[table.attributes().noOfAttr()];
    	AttributeDoubleFunction discr_table[] = 
            new AttributeDoubleFunction[table.attributes().noOfAttr()];

        for (int i=0; i<attributes.length;i++)
            if (table.attributes().isConditional(i) && table.attributes().isNumeric(i))
            {
            	double[] cuts = generateCuts(i, table);
            	NumericAttributeDiscretization disc = new NumericAttributeDiscretization(i, (NumericAttribute)table.attributes().attribute(i), cuts);
            	attributes[i] = disc.getAttribute();
            	discr_table[i] = disc;
            }
            else attributes[i] = table.attributes().attribute(i);
    
        Header new_header = new ArrayHeader(attributes, table.attributes().missing());

        return new FunctionBasedAttributeTransformer(new_header, discr_table);
    }

    /**
     * Method that generate discretization based on data table.
     * 
     * @param table 		Data table to estimate the discretization.
     * @return				Discretization estimated on data table.
     */
    public Transformer generateTransformer(DoubleDataTable table)
    {
        return generateDiscretization(table);
    }
}

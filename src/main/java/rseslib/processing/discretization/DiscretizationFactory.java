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


package rseslib.processing.discretization;

import java.util.Properties;

import rseslib.processing.transformation.TransformationProvider;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Factory of discretization methods.
 *
 * @author      Arkadiusz Wojna
 */
public class DiscretizationFactory
{
	/** Property name for discretization type. */
    public static final String DISCRETIZATION_PROPERTY_NAME = "Discretization";
	/** Property name for number of intervals. */
    public static final String NUMBER_OF_INTERVALS_PROPERTY_NAME = "DiscrNumberOfIntervals";
	/** Property name for minimal frequency. */
    public static final String MIN_FREQUENCY_PROPERTY_NAME = "DiscrMinimalFrequency";
	/** Property name for confidence level. */
    public static final String CONFIDENCE_LEVEL_PROPERTY_NAME = "DiscrConfidenceLevelForIntervalDifference";
	/** Property name for minimal number of intervals. */
    public static final String MIN_INTERVALS_PROPERTY_NAME = "DiscrMinimalNumberOfIntervals";

    /** Discretization types. */
	public enum DiscretizationType
	{
		None(null),
		EqualWidth(RangeDiscretizationProvider.class),
		EqualFrequency(HistogramDiscretizationProvider.class),
		OneRule(OneRuleDiscretizationProvider.class),
		EntropyMinimizationStatic(EntropyMinStaticDiscretizationProvider.class),
		EntropyMinimizationDynamic(null),
		ChiMerge(ChiMergeDiscretizationProvider.class),
		MaximalDiscernibilityHeuristicGlobal(null),
		MaximalDiscernibilityHeuristicLocal(null);

	    /** The class of this discretization type. */
		private final Class classobj;

		/** Constructor. */
		DiscretizationType(Class classobj)
		{
			this.classobj = classobj;
		}
	}
	
	/**
     * Provides the appropriate discretization method.
	 * @param discrProperties       Discretization properties.
     *
     * @return           			Discretizer.
     */
    public static TransformationProvider getDiscretizationProvider(Properties discrProperties) throws PropertyConfigurationException
    {
    	DiscretizationType discretization;
    	try
    	{
    		discretization = DiscretizationType.valueOf(discrProperties.getProperty(DISCRETIZATION_PROPERTY_NAME));
    	}
    	catch (IllegalArgumentException e)
    	{
			throw new PropertyConfigurationException("Unknown discretization: "+discrProperties.getProperty(DISCRETIZATION_PROPERTY_NAME));
        }
    	Properties prop = null;
    	if(discretization.classobj != null)
    		prop = Configuration.loadDefaultProperties(discretization.classobj);
    	switch (discretization)
    	{
    		case None:
    			return null;
    		case EqualWidth:
    			prop.setProperty(RangeDiscretizationProvider.NUMBER_OF_INTERVALS_PROPERTY_NAME, discrProperties.getProperty(NUMBER_OF_INTERVALS_PROPERTY_NAME));
    			return new RangeDiscretizationProvider(prop);
    		case EqualFrequency:
    			prop.setProperty(HistogramDiscretizationProvider.NUMBER_OF_INTERVALS_PROPERTY_NAME, discrProperties.getProperty(NUMBER_OF_INTERVALS_PROPERTY_NAME));
    			return new HistogramDiscretizationProvider(prop);
    		case OneRule:
    			prop.setProperty(OneRuleDiscretizationProvider.MIN_FREQUENCY_PROPERTY_NAME, discrProperties.getProperty(MIN_FREQUENCY_PROPERTY_NAME));
    			return new OneRuleDiscretizationProvider(prop);
    		case EntropyMinimizationStatic:
    			return new EntropyMinStaticDiscretizationProvider(prop);
    		case EntropyMinimizationDynamic:
    			return new EntropyMinDynamicDiscretizationProvider();
    		case ChiMerge:
    			prop.setProperty(ChiMergeDiscretizationProvider.CONFIDENCE_LEVEL_PROPERTY_NAME, discrProperties.getProperty(CONFIDENCE_LEVEL_PROPERTY_NAME));
    			prop.setProperty(ChiMergeDiscretizationProvider.MIN_INTERVALS_PROPERTY_NAME, discrProperties.getProperty(MIN_INTERVALS_PROPERTY_NAME));
    			return new ChiMergeDiscretizationProvider(prop);
    		case MaximalDiscernibilityHeuristicGlobal:
    			return new MDGlobalDiscretizationProvider();
    		case MaximalDiscernibilityHeuristicLocal:
    			return new MDLocalDiscretizationProvider();
    	}
    	return null;
    }
}

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


package rseslib.processing.metrics;

import java.util.Properties;

import rseslib.structure.metric.CityHammingMetric;
import rseslib.structure.metric.CitySVDMetric;
import rseslib.structure.metric.InterpolatedVDMetric;
import rseslib.structure.metric.DensityBasedVDMetric;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.AbstractWeightedMetric;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Factory of metrics inducing a required metric
 * from a training table.
 *
 * @author      Arkadiusz Wojna
 */
public class MetricFactory
{
	/** Property name for metric type. */
    public static final String METRIC_PROPERTY_NAME = "metric";
	/** Property name for metric type. */
    public static final String VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME = "vicinitySizeForDensityBasedMetric";

    /** Metric types. */
	public enum MetricType
	{
		CityAndHamming(CityHammingMetric.class),
		CityAndSimpleValueDifference(CitySVDMetric.class),
		DensityBasedValueDifference(DensityBasedVDMetric.class),
		InterpolatedValueDifference(InterpolatedVDMetric.class);

	    /** The class of this metric type. */
		private final Class classobj;

		/** Constructor. */
		MetricType(Class classobj)
		{
			this.classobj = classobj;
		}
	}
	
	/** Attribute weighting methods. */
	public enum Weighting { None, Perceptron,  DistanceBased, AccuracyBased; }

	/**
     * Induces the appropriate metric from a given training set of data object.
	 * @param tab                   The set of data objects used to induce this metric.
	 * @param metricName            The name of a metric type.
     *
     * @return           			The induced metric.
     */
    public static Metric getMetric(Properties metricProperties, DoubleDataTable tab) throws PropertyConfigurationException
    {
    	MetricType metric;
    	try
    	{
    		metric = MetricType.valueOf(metricProperties.getProperty(METRIC_PROPERTY_NAME));
    	}
    	catch (IllegalArgumentException e)
    	{
			throw new PropertyConfigurationException("Unknown metric: "+metricProperties.getProperty(METRIC_PROPERTY_NAME));
        }
    	Properties metricProp = Configuration.loadDefaultProperties(metric.classobj);
    	switch (metric)
    	{
    		case CityAndHamming:
    			return new CityHammingMetric(metricProp, tab);
    		case CityAndSimpleValueDifference:
    			return new CitySVDMetric(metricProp, tab);
    		case DensityBasedValueDifference:
    			metricProp.setProperty(DensityBasedVDMetric.VICINITY_SIZE_PROPERTY_NAME, metricProperties.getProperty(VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME));
    			return new DensityBasedVDMetric(metricProp, tab);
    		case InterpolatedValueDifference:
    			return new InterpolatedVDMetric(metricProp, tab);
    	}
    	return null;
    }

    /**
     * Calls the appropriate weighting method.
     *
     * @param prop                Properties with the weighting method.
     * @param metr                Original metric to be weighted.
     * @param tab                 Training set of data objects.
     * @param prog                Progress object.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public static void adjustWeights(String weightingMethodName, AbstractWeightedMetric metr, DoubleDataTable tab, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
    	Weighting weightingMethod;
    	try
    	{
    		weightingMethod = Weighting.valueOf(weightingMethodName);
    	}
    	catch (IllegalArgumentException e)
    	{
			throw new PropertyConfigurationException("Unknown weighting method: "+weightingMethodName);
        }
    	WeightAdjuster weightAdj = null;
    	switch (weightingMethod)
    	{
    	case None:
    		prog.set("Weighting attributes in a metric", 1);
    		prog.step();
    		break;
    	case Perceptron:
    		weightAdj = new PerceptronBasedWeightAdjuster(null);
    		break;
    	case DistanceBased:
    		weightAdj = new DistanceBasedWeightAdjuster(null);
    		break;
    	case AccuracyBased:
    		weightAdj = new AccuracyBasedWeightAdjuster(null);
    		break;
    	}
    	if (weightAdj!=null)
    	{
    		weightAdj.adjustWeights(metr, tab, prog);
    	}
    	return;
    }
}

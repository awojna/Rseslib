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


package rseslib.processing.discretization;

import java.util.Properties;

import rseslib.processing.transformation.TransformationProvider;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.table.NumericalStatistics;
import rseslib.system.PropertyConfigurationException;

/**
 * RangeDiscretizationProvider generates discretization based on attribute range.
 * Range of a numeric and conditional attribute is divided into a specified number
 * of equally wide intervals. First and last interval are opened up to the
 * negative and positive infinity, respectively.
 * 
 * @author Rafal Latkowski
 */
public class RangeDiscretizationProvider extends AbstractDiscretizationProvider implements TransformationProvider
{
	/** Property name for minimal number of interval. */
    public static final String NUMBER_OF_INTERVALS_PROPERTY_NAME = "numberOfIntervals";

    /**
     * Number of intervals.
     */
    private int m_nNumberOfIntervals;

    /**
     * Constructs initial object for generating discretization.
     * This object do not require any initialization. Only a default
     * number of intervals is set.
     * 
     * @param number_of_intervals default number of intervals
     */
    public RangeDiscretizationProvider(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
        m_nNumberOfIntervals = getIntProperty(NUMBER_OF_INTERVALS_PROPERTY_NAME);
    }

    /**
     * Creates discretization cuts for one attribute.
     * Main method of this discretization provider.
     * 
     * @param attribute				Selected attribute for discretization.
     * @param table data			Used for estimating the best cuts.
     * @return Discretization cuts with specified number of intervals
     */
    double[] generateCuts(int attribute, DoubleDataTable table)
    {
    	NumericalStatistics stats = table.getNumericalStatistics(attribute);
        double max = stats.getMaximum();
        double min = stats.getMinimum();
        double[] cuts;
        if (Double.isNaN(max) || Double.isNaN(min) || max-min<1e-10)
        	cuts = new double[0];
        else
        {
        	cuts = new double[m_nNumberOfIntervals-1];
            for (int i=0; i<cuts.length; i++)
                cuts[i] = min+(max-min)*(i+1)/m_nNumberOfIntervals;
        }
        return cuts;
    }
}

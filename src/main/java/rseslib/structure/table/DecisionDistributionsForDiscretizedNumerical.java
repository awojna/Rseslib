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


package rseslib.structure.table;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.vector.Vector;
import rseslib.system.Report;

/**
 * Extended numerical attribute information.
 * It contains decision distributions calculated
 * for intervals of the range of numerical attribute values.
 * The interval are obtained by descretizing
 * the range of values into equal intervals.
 *
 * @author      Arkadiusz Wojna
 */
public class DecisionDistributionsForDiscretizedNumerical
{
    /**
     * Decision vectors for succesive value ranges.
     * Position 0 contains decision vector for the null value.
     */
    private IntervalWithDecisionDistribution[] m_DecDistributions = null;
    /** Length of a single interval obtained from discretisation. */
    private double m_nIntervalLength;

    /**
     * Constructor extracts decision vectors
     * for succesive value ranges.
     *
     * @param objects            Data set that decision vectors for succesive value ranges
     *                           are calculated from.
     * @param index              Attribute index.
     * @param noOfIntervals		 Number of intervals that the range of values is divided into.
     */
    public DecisionDistributionsForDiscretizedNumerical(DoubleDataTable objects, int index, int noOfIntervals)
    {
        NominalAttribute decAttr = objects.attributes().nominalDecisionAttribute();
        NumericalStatistics stats = objects.getNumericalStatistics(index);
        Vector[] decDistributions = new Vector[noOfIntervals+1];
        for (int interval = 0; interval < decDistributions.length; interval++)
            decDistributions[interval] = new Vector(objects.attributes().nominalDecisionAttribute().noOfValues());
        m_nIntervalLength = (stats.getMaximum() - stats.getMinimum()) / (double)noOfIntervals;
        for (DoubleData dObj : objects.getDataObjects())
        {
            double attrVal = dObj.get(index);
            if (Double.isNaN(dObj.get(index))) decDistributions[0].increment(decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision()));
            else
            {
                double dInterval = (attrVal - stats.getMinimum()) / m_nIntervalLength + 1.0;
                int iInterval = (int)dInterval;
                boolean prevInterval = false;
                if (dInterval == (double)iInterval) prevInterval = true;
                if (iInterval < decDistributions.length) decDistributions[iInterval].increment(decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision()));
                if (prevInterval && iInterval > 1) decDistributions[iInterval-1].increment(decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision()));
            }
        }
        for (int interval = 0; interval < decDistributions.length; interval++)
        {
            boolean empty = true;
            for (int d = 0; empty && d < decDistributions[interval].dimension(); d++)
                if (decDistributions[interval].get(d) > 0) empty = false;
            if (empty)
                for (int d = 0; d < decDistributions[interval].dimension(); d++)
                    decDistributions[interval].set(d, objects.getDecisionDistribution()[d]);
            decDistributions[interval].normalizeWithCityNorm();
        }
        m_DecDistributions = new IntervalWithDecisionDistribution[decDistributions.length];
        m_DecDistributions[0] = new IntervalWithDecisionDistribution(Double.NaN, Double.NaN, true, true, decDistributions[0]);
        for (int interval = 1; interval < m_DecDistributions.length; interval++)
            m_DecDistributions[interval] = new IntervalWithDecisionDistribution(stats.getMinimum() + m_nIntervalLength * (double)(interval - 1), stats.getMinimum() + m_nIntervalLength * (double)interval, true, true, decDistributions[interval]);
    }

    /**
     * Returns succesive intervals with the corresponding decision vectors.
     *
     * @return Succesive intervals with the corresponding decision vectors.
     */
    public IntervalWithDecisionDistribution[] getIntervals()
    {
        return m_DecDistributions;
    }

    /**
     * Returns the length of a single interval obtained from discretisation.
     *
     * @return Length of a single interval obtained from discretisation.
     */
    public double getIntervalLength()
    {
        return m_nIntervalLength;
    }


    /**
     * Constructs string representation of this attribute.
     *
     * @return String representation of this attribute.
     */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("Atrybut numeryczny ("+m_DecDistributions.length+" przedzialow)"+Report.lineSeparator);
        for (int interval = 0; interval < m_DecDistributions.length; interval++)
            sbuf.append(m_DecDistributions[interval]+Report.lineSeparator);
        return sbuf.toString();
    }
}

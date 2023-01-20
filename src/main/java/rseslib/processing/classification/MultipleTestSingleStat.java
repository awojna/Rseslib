/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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

package rseslib.processing.classification;

import java.io.Serializable;

/**
 * Aggregated classification statistic from a number of tests.
 *
 * @author      Arkadiusz Wojna, Grzegorz Gora
 */
public class MultipleTestSingleStat implements Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Name of statistic. */
    private String m_statisticName;
	/** Array of single statistic for particular tests. */
    private double[] m_statistic;
    /** Statistic averaged over all tests. */
    private double m_nAverageStatistic;
    /** Standard deviation of statistic. */
    private double m_nStdDev;

    /**
     * Constructor.
     *
     * @param Array of statistics for particular tests.
     */
    public MultipleTestSingleStat(String statisticName, double[] statistic)
    {
    	m_statisticName = statisticName;
    	m_statistic = statistic;
        computeAverage();
        computeStdDev();
    }

    /**
     * Computes statistic averaged over all tests.
     */
    private void computeAverage()
    {
        m_nAverageStatistic = 0.0;
        for (int run = 0; run < m_statistic.length; run++)
            m_nAverageStatistic += m_statistic[run];
        m_nAverageStatistic /= m_statistic.length;
    }

    /**
     * Returns the average statistic.
     *
     * @return Average statistic.
     */
    public double getAverage()
    {
        return m_nAverageStatistic;
    }

    /**
     * Computes the standard deviation of statistic.
     */
    private void computeStdDev()
    {
        m_nStdDev = 0.0;
        for (int run = 0; run < m_statistic.length; run++)
        {
            double diff = m_statistic[run] - m_nAverageStatistic;
            m_nStdDev += (diff*diff);
        }
        m_nStdDev /= m_statistic.length;
        m_nStdDev = Math.sqrt(m_nStdDev);
    }

    /**
     * Returns the standard deviation of statistic.
     *
     * @return Standard deviation of statistic.
     */
    public double getStandardDeviation()
    {
        return m_nStdDev;
    }

    /**
     * Returns string representation of results.
     *
     * @return  String representation of results.
     */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(m_statisticName + ": "+(int)(100*m_nAverageStatistic)+".");
        if (10000*m_nAverageStatistic-100*(int)(100*m_nAverageStatistic) < 10) sbuf.append("0");
        sbuf.append((int)(10000*m_nAverageStatistic-100*(int)(100*m_nAverageStatistic))+"%");
        sbuf.append("   Std.dev.: "+(int)(100*m_nStdDev)+".");
        if (10000*m_nStdDev-100*(int)(100*m_nStdDev) < 10) sbuf.append("0");
        sbuf.append((int)(10000*m_nStdDev-100*(int)(100*m_nStdDev))+"%");
        return sbuf.toString();
    }

    /**
     * Returns string representation of results, both aggregated and from particular tests.
     *
     * @return  String representation of results, both aggregated and from particular tests.
     */
    public String toStringDetails()
    {
    	return toString() + "; partial results: " + toStringResultArray();
    }

    /**
     * Returns string representation of results from particular tests.
     *
     * @return  String representation of results from particular tests.
     */
    public String toStringResultArray()
    {
    	StringBuffer strbuf = new StringBuffer();
    	strbuf.append("[");
    	for (int i = 0; i < m_statistic.length; i++) {
			strbuf.append("" + m_statistic[i]);
			if (i!=m_statistic.length-1) strbuf.append(", "); 
		}
    	strbuf.append("]");
    	return strbuf.toString();
    }
    
}

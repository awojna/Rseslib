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


package rseslib.processing.classification;

import java.io.Serializable;

import rseslib.system.Report;

/**
 * Classification accuracy for a number of tests.
 *
 * @author      Arkadiusz Wojna
 */
public class MultipleTestResult implements Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Array of accuracy for particular tests. */
    private double[] m_Accuracy;
    /** Accuracy averaged over all tests. */
    private double m_nAverageAccuracy;
    /** Standard deviation of accuracy. */
    private double m_nStdDev;

    /**
     * Constructor.
     *
     * @param accuracy Array of accuracy for particular tess.
     */
    public MultipleTestResult(double[] accuracy)
    {
        m_Accuracy = accuracy;
        computeAverage();
        computeStdDev();
    }

    /**
     * Constructor.
     *
     * @param results Array of results for particular tests.
     */
    public MultipleTestResult(TestResult[] results)
    {
        m_Accuracy = new double[results.length];
        for (int run = 0; run < m_Accuracy.length; run++)
            m_Accuracy[run] = results[run].getAccuracy();
        computeAverage();
        computeStdDev();
    }

    /**
     * Computes accuracy avereged over all tests.
     */
    private void computeAverage()
    {
        m_nAverageAccuracy = 0.0;
        for (int run = 0; run < m_Accuracy.length; run++)
            m_nAverageAccuracy += m_Accuracy[run];
        m_nAverageAccuracy /= m_Accuracy.length;
    }

    /**
     * Returns the average accuracy.
     *
     * @return Average accuracy.
     */
    public double getAverage()
    {
        return m_nAverageAccuracy;
    }

    /**
     * Computes the standard deviation of accuracy.
     */
    private void computeStdDev()
    {
        m_nStdDev = 0.0;
        for (int run = 0; run < m_Accuracy.length; run++)
        {
            double diff = m_Accuracy[run] - m_nAverageAccuracy;
            m_nStdDev += (diff*diff);
        }
        m_nStdDev /= m_Accuracy.length;
        m_nStdDev = Math.sqrt(m_nStdDev);
    }

    /**
     * Returns the standard deviation of accuracy.
     *
     * @return Standard deviation of accuracy.
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
        sbuf.append("Accuracy: "+(int)(100*m_nAverageAccuracy)+".");
        if (10000*m_nAverageAccuracy-100*(int)(100*m_nAverageAccuracy) < 10) sbuf.append("0");
        sbuf.append((int)(10000*m_nAverageAccuracy-100*(int)(100*m_nAverageAccuracy))+"%");
        sbuf.append("   Std.dev.: "+(int)(100*m_nStdDev)+".");
        if (10000*m_nStdDev-100*(int)(100*m_nStdDev) < 10) sbuf.append("0");
        sbuf.append((int)(10000*m_nStdDev-100*(int)(100*m_nStdDev))+"%");
        sbuf.append("   Tests: "+m_Accuracy.length+Report.lineSeparator);
        return sbuf.toString();
    }
}

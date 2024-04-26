/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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
 * Classification measures: Accuracy, F-measure, G-mean and Sensitivity
 * for a number of tests.
 *
 * @author      Arkadiusz Wojna, Grzegorz Gora
 */
public class MultipleTestResult implements Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 3L;
	/** Name of accuracy measure. */
	private static final String ACCURACY_NAME = "Accuracy";
	/** Name of F-measure. */
	private static final String F_MEASURE_NAME = "F-measure";
	/** Name of G-mean measure. */
	private static final String G_MEAN_NAME = "G-mean";
	/** Name of sensitivity measure. */
	private static final String SENSITIVITY_NAME = "Sensitivity";
	
	/** Error message. */
	private String m_Error = null;
	/** Number of tests run. */
	private int m_nNoOfTests;
	/** Aggregated accuracy. */
	private MultipleTestSingleStat m_AccuracyStat;
	/** Aggregated F-measure. */
	private MultipleTestSingleStat m_FmeasureStat;
	/** Aggregated G-mean. */
	private MultipleTestSingleStat m_GmeanStat;
	/** Aggregated sensitivity. */
	private MultipleTestSingleStat m_SensitivityStat;

    /**
     * Constructor without measures for imbalanced data.
     *
     * @param accuracy Array of accuracy for particular tests.
     */
    public MultipleTestResult(double[] accuracy)
    {
    	m_nNoOfTests = accuracy.length;
        m_AccuracyStat = new MultipleTestSingleStat(ACCURACY_NAME, accuracy);
    }

    /**
     * Constructor aggregating results from individual tests.
     *
     * @param results Array of results for particular tests.
     */
    public MultipleTestResult(TestResult[] results)
    {
    	m_nNoOfTests = results.length;
    	for (int run = 0; run < m_nNoOfTests; run++)
    		if (!results[run].successfulRun())
    		{
    			m_Error = results[run].getError();
    			return;
    		}
    	double[] arrAccuracy = new double[m_nNoOfTests];
    	for (int run = 0; run < m_nNoOfTests; run++)
    		arrAccuracy[run] = results[run].getAccuracy();
    	m_AccuracyStat = new MultipleTestSingleStat(ACCURACY_NAME, arrAccuracy);
    	if(results[0].hasMeasuresForImbalanced())
    	{
    		double[] arrFmeasure = new double[m_nNoOfTests];
    		double[] arrGmean = new double[m_nNoOfTests];
    		double[] arrSensitivity = new double[m_nNoOfTests];
    		for (int run = 0; run < m_nNoOfTests; run++)
    		{
    			arrFmeasure[run] = results[run].getFmeasure();
    			arrGmean[run] = results[run].getGmean();
    			arrSensitivity[run] = results[run].getSensitivity();
    		}
    		m_FmeasureStat = new MultipleTestSingleStat(F_MEASURE_NAME, arrFmeasure);
    		m_GmeanStat = new MultipleTestSingleStat(G_MEAN_NAME, arrGmean);
    		m_SensitivityStat = new MultipleTestSingleStat(SENSITIVITY_NAME, arrSensitivity);
    	}
    }

    /**
     * Constructor aggregating results one level up. 
     *
     * @param lowerLevelResults Results from tests aggregated on the lower level.
     */
    public MultipleTestResult(MultipleTestResult[] lowerLevelResults)
    {
    	m_nNoOfTests = lowerLevelResults.length;
    	for (int run = 0; run < m_nNoOfTests; run++)
    		if (!lowerLevelResults[run].successfulRun())
    		{
    			m_Error = lowerLevelResults[run].getError();
    			return;
    		}
    	double[] arrAccuracy = new double[m_nNoOfTests];
    	for (int run = 0; run < m_nNoOfTests; run++)
    		arrAccuracy[run] = lowerLevelResults[run].m_AccuracyStat.getAverage();
    	m_AccuracyStat = new MultipleTestSingleStat(ACCURACY_NAME, arrAccuracy);
    	if(lowerLevelResults[0].hasMeasuresForImbalanced())
    	{
    		double[] arrFmeasure = new double[m_nNoOfTests];
    		double[] arrGmean = new double[m_nNoOfTests];
    		double[] arrSensitivity = new double[m_nNoOfTests];
    		for (int run = 0; run < m_nNoOfTests; run++)
    		{
    			arrFmeasure[run] = lowerLevelResults[run].m_FmeasureStat.getAverage();
    			arrGmean[run] = lowerLevelResults[run].m_GmeanStat.getAverage();
    			arrSensitivity[run] = lowerLevelResults[run].m_SensitivityStat.getAverage();
    		}
    		m_FmeasureStat = new MultipleTestSingleStat(F_MEASURE_NAME, arrFmeasure);
    		m_GmeanStat = new MultipleTestSingleStat(G_MEAN_NAME, arrGmean);
    		m_SensitivityStat = new MultipleTestSingleStat(SENSITIVITY_NAME, arrSensitivity);
    	}
    }
    
    /**
     * Returns true if the test completed successfully.
     * 
     * @return	True if the test completed successfully.
     */
    public boolean successfulRun()
    {
    	return (m_Error == null);
    }
    
    /**
     * Returns true if this result has classification measures for imbalanced data.
     * 
     * @return  True if this result has classification measures for imbalanced data.
     */
    public boolean hasMeasuresForImbalanced()
    {
    	return (m_FmeasureStat != null && m_GmeanStat != null && m_SensitivityStat != null);
    }

    /**
     * Returns the average classification accuracy.
     *
     * @return Average accuracy.
     */
    public double getAvgAccuracy()
    {
        return m_AccuracyStat.getAverage();
    }
    
    /**
     * Returns the standard deviation of accuracy.
     *
     * @return Standard deviation of accuracy.
     */
    public double getAccuracyStandardDeviation()
    {
        return m_AccuracyStat.getStandardDeviation();
    }

    /**
     * Returns the average F-measure.
     *
     * @return Average F-measure.
     */
    public double getAvgFmeasure()
    {
    	if(m_FmeasureStat == null)
    		return Double.NaN;
        return m_FmeasureStat.getAverage();
    }
    
    /**
     * Returns the average G-mean.
     *
     * @return Average G-mean.
     */
    public double getAvgGmean()
    {
    	if(m_GmeanStat == null)
    		return Double.NaN;
        return m_GmeanStat.getAverage();
    }
    
    /**
     * Returns the average sensitivity.
     *
     * @return Average sensitivity.
     */
    public double getAvgSensitivity()
    {
    	if(m_SensitivityStat == null)
    		return Double.NaN;
        return m_SensitivityStat.getAverage();
    }
    
    /**
     * Returns the error message if error occurred during the test.
     *
     * @return Error message if error occurred during the test.
     */
    public String getError()
    {
        return m_Error;
    }
    
    /**
     * Returns string representation of results.
     *
     * @return  String representation of results.
     */
    public String toString()
    {
    	if (m_Error != null)
    		return m_Error;
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(m_AccuracyStat.toString()+Report.lineSeparator);
        if (m_FmeasureStat != null)
        	sbuf.append(m_FmeasureStat.toString()+Report.lineSeparator);
        if (m_GmeanStat != null)
        	sbuf.append(m_GmeanStat.toString()+Report.lineSeparator);
        if (m_SensitivityStat != null)
        	sbuf.append(m_SensitivityStat.toString()+Report.lineSeparator);
        sbuf.append("Tests: "+m_nNoOfTests+Report.lineSeparator);
        return sbuf.toString();
    }
    
    /**
     * Returns string representation of results including partial results.
     *
     * @return  String representation of results including partial results.
     */
    public String toStringDetails()
    {
    	if (m_Error != null)
    		return m_Error;
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(m_AccuracyStat.toStringDetails()+Report.lineSeparator);
        if (m_FmeasureStat != null)
        	sbuf.append(m_FmeasureStat.toStringDetails()+Report.lineSeparator);
        if (m_GmeanStat != null)
        	sbuf.append(m_GmeanStat.toStringDetails()+Report.lineSeparator);
        if (m_SensitivityStat != null)
        	sbuf.append(m_SensitivityStat.toStringDetails()+Report.lineSeparator);
        sbuf.append("Tests: "+m_nNoOfTests+Report.lineSeparator);
        return sbuf.toString();
    }

}

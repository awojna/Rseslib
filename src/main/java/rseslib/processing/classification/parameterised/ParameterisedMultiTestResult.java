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


package rseslib.processing.classification.parameterised;

import rseslib.processing.classification.MultipleTestResult;

/**
 * Classification accuracy for a number of tests
 * obtained by parameterized classification method.
 *
 * @author      Arkadiusz Wojna
 */
public class ParameterisedMultiTestResult
{
    /** Parameter name. */
    private String m_ParameterName;
    /** Array of multitest results for succesive parameter values. */
    private MultipleTestResult[] m_arrMultiTestResults = null;

    /**
     * Constructor.
     *
     * @param parameterName         Parameter name.
     * @param parameterisedResults  Array of multitest results for succesive parameter values for particular tests.
     */
    public ParameterisedMultiTestResult(String parameterName, ParameterisedTestResult[] parameterisedResults)
    {
        m_ParameterName = parameterName;
        m_arrMultiTestResults = new MultipleTestResult[parameterisedResults[0].getParameterRange()];
        for (int paramVal = 0; paramVal < m_arrMultiTestResults.length; paramVal++)
        {
            double[] accuracy = new double[parameterisedResults.length];
            for (int test = 0; test < accuracy.length; test++) accuracy[test] = parameterisedResults[test].getClassificationResult(paramVal).getAccuracy();
            m_arrMultiTestResults[paramVal] = new MultipleTestResult(accuracy);
        }
    }

    /**
     * Constructor.
     *
     * @param parameterName                 Parameter name.
     * @param arrayOfParameterisedAccuracy  Array of multitest results for succesive parameter values for particular tests.
     */
    public ParameterisedMultiTestResult(String parameterName, double[][] arrayOfParameterisedAccuracy)
    {
        m_ParameterName = parameterName;
        m_arrMultiTestResults = new MultipleTestResult[arrayOfParameterisedAccuracy[0].length];
        for (int paramVal = 0; paramVal < m_arrMultiTestResults.length; paramVal++)
        {
            double[] accuracy = new double[arrayOfParameterisedAccuracy.length];
            for (int test = 0; test < accuracy.length; test++) accuracy[test] = arrayOfParameterisedAccuracy[test][paramVal];
            m_arrMultiTestResults[paramVal] = new MultipleTestResult(accuracy);
        }
    }

    /**
     * Returns parameter name.
     *
     * @return Parameter name.
     */
    public String getParameterName()
    {
        return m_ParameterName;
    }

    /**
     * Returns the number of parameter values.
     *
     * @return Number of parameter values.
     */
    public int parameterRange()
    {
        return m_arrMultiTestResults.length;
    }

    /**
     * Return the results of this multitest for a given parameter value.
     *
     * @param parameterValue The parameter value of the multitest result to be returned.
     * @return               Results of this multitest for a given parameter value.
     */
    public MultipleTestResult getResultForParameterValue(int parameterValue)
    {
        return m_arrMultiTestResults[parameterValue];
    }

    /**
     * Returns string representation of classification results.
     *
     * @return String representation of classification results.
     */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer();
        for (int paramVal = 0; paramVal < m_arrMultiTestResults.length; paramVal++)
            if (m_arrMultiTestResults[paramVal]!=null)
                sbuf.append(m_ParameterName+" = "+paramVal+"   "+m_arrMultiTestResults[paramVal].toString());
        return sbuf.toString();
    }

}

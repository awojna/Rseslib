/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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

import java.util.Enumeration;
import java.util.Properties;

import rseslib.processing.classification.TestResult;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.system.Report;

/**
 * Classification accuracy for the whole test set
 * and for particular decision classes
 * obtained by parameterized classification method.
 *
 * @author      Arkadiusz Wojna
 */
public class ParameterisedTestResult
{
    /** Parameter name. */
    String m_ParameterName;
    /** Array of classification results for succesive parameter values. */
    TestResult[] m_arrClassificationResults = null;
    /** Dictionary of general statistics. */
    Properties m_Statistics;

    /**
     * Constructor.
     *
     * @param parameterName                  Parameter name.
     * @param decDistr                       Distribution of objects for particular decisions.
     * @param parameterizedConfusionMatrices Confusion matrices obtained in classification
     *                                       fort particular parameter values.
     * @param decAttr                        Information about decision attribute.
     * @param statistics                     Dictionary of general statistics.
     */
    public ParameterisedTestResult(String parameterName, NominalAttribute decAttr, int[] decDistr, int[][][] parameterizedConfusionMatrices, Properties statistics)
    {
        m_ParameterName = parameterName;
        m_arrClassificationResults = new TestResult[parameterizedConfusionMatrices.length];
        for (int paramVal = 0; paramVal < m_arrClassificationResults.length; paramVal++)
            if (parameterizedConfusionMatrices[paramVal]!=null)
                m_arrClassificationResults[paramVal] = new TestResult(decAttr, decDistr, parameterizedConfusionMatrices[paramVal], null);
        m_Statistics = statistics;
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
      * Returns the upper bound of the parameter range.
      *
      * @return Uupper bound of the parameter range.
      */
     public int getParameterRange()
     {
         return m_arrClassificationResults.length;
     }

     /**
      * Returns classification result for a given parameter velue.
      *
      * @param parValue Parameter value.
      * @return         Classification result for a given parameter velue.
      */
     public TestResult getClassificationResult(int parValue)
     {
         return m_arrClassificationResults[parValue];
     }

    /**
     * Returns string representation of classification results.
     *
     * @return String representation of classification results.
     */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer();
        if (m_Statistics!=null)
        {
            Enumeration enumarator = m_Statistics.propertyNames();
            while (enumarator.hasMoreElements())
            {
                String resultName = (String)enumarator.nextElement();
                sbuf.append(resultName+" = "+m_Statistics.getProperty(resultName)+Report.lineSeparator);
            }
        }
        int bestParameter = -1;
        for (int paramVal = 0; paramVal < m_arrClassificationResults.length; paramVal++)
            if (bestParameter==-1 || (m_arrClassificationResults[paramVal]!=null
                && m_arrClassificationResults[paramVal].getAccuracy() > m_arrClassificationResults[bestParameter].getAccuracy()))
                bestParameter = paramVal;
        sbuf.append("Best "+m_ParameterName+" = "+bestParameter+Report.lineSeparator+m_arrClassificationResults[bestParameter].toString());
        for (int paramVal = 0; paramVal < m_arrClassificationResults.length; paramVal++)
            if (m_arrClassificationResults[paramVal]!=null)
                sbuf.append(m_ParameterName+" = "+paramVal+Report.lineSeparator+m_arrClassificationResults[paramVal].toString());
        return sbuf.toString();
    }
}

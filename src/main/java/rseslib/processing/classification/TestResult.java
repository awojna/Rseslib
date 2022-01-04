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

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;


import rseslib.structure.attribute.NominalAttribute;
import rseslib.system.Report;

/**
 * Classification accuracy for the whole test set
 * and for particular decision classes
 * and confusion matrix.
 *
 * @author      Arkadiusz Wojna
 */
public class TestResult implements Serializable
{
	/** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Dictionary of statistics specific to a classifier. */
    private Properties m_Statistics;
    /** Decision attribute with value dictionary. */
    private NominalAttribute m_DecisionAttribute;
    /** Decision distribution in the test set. */
    private int[] m_arrDecDistr;
    /** Confusion matrix obtained in classification. */
    private int[][] m_arrConfusionMatrix;
    /** Number of all objects tested. */
    private int m_nAll = 0;
    /** Number of objects that are assigned with a decision by a classifier. */
    private int m_nCovered = 0;
    /** Number of correctly classfied objects. */
    private int m_nCorrect = 0;

    /**
     * Constructor.
     *
     * @param decAttr         Information about decision attribute.
     * @param decDistr        Distribution of objects for locally coded decisions.
     * @param confusionMatrix Confusion matrix obtained in classification.
     *                        The i-th row represents the distribution of the decisions assigned
     *                        for objects with the real decision coded locally by i.
     * @param statistics      Dictionary of general statistics.
     */
    public TestResult(NominalAttribute decAttr, int[] decDistr, int[][] confusionMatrix, Properties statistics)
    {
        if (decDistr.length!=confusionMatrix.length) throw new RuntimeException("The length of the total decision distribution and the confusion matrix dimension do not match");
        m_Statistics = statistics;
        m_DecisionAttribute = decAttr;
        m_arrDecDistr = decDistr;
        m_arrConfusionMatrix = confusionMatrix;
        for (int dec = 0; dec < m_arrDecDistr.length; dec++)
        {
            m_nAll += m_arrDecDistr[dec];
            m_nCorrect += m_arrConfusionMatrix[dec][dec];
            for (int clDec = 0; clDec < m_arrConfusionMatrix[dec].length; clDec++)
            	m_nCovered += m_arrConfusionMatrix[dec][clDec];
        }
    }

    /**
     * Returns the information about the decision attribute.
     * 
     * @return	Information about the decision attribute.
     */
    public NominalAttribute decisionAttribute()
    {
    	return m_DecisionAttribute;
    }
    
    /**
     * Returns classification accuracy.
     *
     * @return  Classification accuracy.
     */
    public double getAccuracy()
    {
        return (double)m_nCorrect/(double)m_nAll;
    }

    /**
     * Returns classification accuracy for a given decision class.
     *
     * @param dec   Decision.
     * @return      Classification accuracy for a given decision class.
     */
    public double getDecAccuracy(double dec)
    {
    	int decLocCode = m_DecisionAttribute.localValueCode(dec);
        if (m_arrDecDistr[decLocCode]==0) return 0.0;
        else return (double)m_arrConfusionMatrix[decLocCode][decLocCode]/(double)m_arrDecDistr[decLocCode];
    }

    /**
     * Returns the number of objects with the real decision realDec
     * that were classified with the decision assignedDec.
     *
     * @param realDec     Real decision of the objects to be counted.
     * @param assignedDec The decision assigned by the classifier.
     * @return            Number of objects.
     */
    public int getNoOfObjects(double realDec, double assignedDec)
    {
        return m_arrConfusionMatrix[m_DecisionAttribute.localValueCode(realDec)][m_DecisionAttribute.localValueCode(assignedDec)];
    }

    /**
     * Returns string representation of classification results.
     *
     * @return  String representation of classification results.
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
        double accuracy = getAccuracy();
        sbuf.append("  Accuracy: "+(int)(100*accuracy)+".");
        if (10000*accuracy-100*(int)(100*accuracy) < 10) sbuf.append("0");
        sbuf.append((int)(10000*accuracy-100*(int)(100*accuracy))+"%"+Report.lineSeparator);
        for (int dec = 0; dec < m_DecisionAttribute.noOfValues(); dec++)
        {
        	double decCode = m_DecisionAttribute.globalValueCode(dec);
        	double decAccuracy = getDecAccuracy(decCode); 
            sbuf.append("  Decision "+NominalAttribute.stringValue(decCode)+": "+(int)(100*decAccuracy)+".");
            if (10000*decAccuracy-100*(int)(100*decAccuracy) < 10) sbuf.append("0");
            sbuf.append((int)(10000*decAccuracy-100*(int)(100*decAccuracy))+"%"+Report.lineSeparator);
        }
        return sbuf.toString();
    }
    
    /**
     * Returns statistics.
     *
     * @return  Statistics.
     */
    public Properties getStatistics()
    {
    	return m_Statistics;
    }
    
    /**
     * Returns statistics and results.
     *
     * @return  Statistics and results.
     */
    public Properties getStatisticsAndResults()
    {
    	Properties stats = (Properties)m_Statistics.clone();
    	stats.put("all_cnt",Integer.toString(m_nAll));
    	stats.put("cover_cnt",Integer.toString(m_nCovered));
    	stats.put("correct_cnt",Integer.toString(m_nCorrect));
    	stats.put("precision",Double.toString(((double)m_nCorrect)/((double)m_nCovered)));
    	stats.put("coverage",Double.toString(((double)m_nCovered)/((double)m_nAll)));
    	stats.put("accuracy",Double.toString(((double)m_nCorrect)/((double)m_nAll)));
    	return stats;
    }
}

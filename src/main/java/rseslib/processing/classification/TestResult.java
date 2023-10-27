/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.system.Report;

/**
 * Classification accuracy for the whole test set
 * and for particular decision classes
 * and confusion matrix.
 *
 * @author      Arkadiusz Wojna, Grzegorz Gora
 */
public class TestResult implements Serializable
{
	/** Serialization version. */
	private static final long serialVersionUID = 2L;
	/** Definition of statistics formatting. */
	private static final DecimalFormat df = new DecimalFormat("0.0000");

	/** Error message. */
	private String m_Error = null;
	/** Dictionary of statistics specific to a classifier. */
    private Properties m_Statistics;
    /** Decision attribute with value dictionary. */
    private NominalAttribute m_DecisionAttribute;
    /** Decision distribution in the test set. */
    private int[] m_arrDecDistr;
    /** Confusion matrix obtained in classification. */
    private int[][] m_arrConfusionMatrix;
    /** Decision distribution obtained in classification. */
    private int[] m_arrPredictedDecDistr;
    /** Number of all objects tested. */
    private int m_nAll = 0;
    /** Number of objects that are assigned with a decision by a classifier. */
    private int m_nCovered = 0;
    /** Number of correctly classified objects. */
    private int m_nCorrect = 0;
    /** Local code of minority decision. */
    private int m_nLocalMinorityDec = -1;

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
        m_arrPredictedDecDistr = new int[m_DecisionAttribute.noOfValues()];
        for (int predictedDec = 0; predictedDec < m_arrPredictedDecDistr.length; predictedDec++)
        {
        	m_arrPredictedDecDistr[predictedDec] = 0;
            for (int dec = 0; dec < m_arrDecDistr.length; dec++)
            	m_arrPredictedDecDistr[predictedDec] += m_arrConfusionMatrix[dec][predictedDec];
        }
        for (int dec = 0; dec < m_arrDecDistr.length; dec++)
        {
            m_nAll += m_arrDecDistr[dec];
            m_nCorrect += m_arrConfusionMatrix[dec][dec];
            for (int clDec = 0; clDec < m_arrConfusionMatrix[dec].length; clDec++)
            	m_nCovered += m_arrConfusionMatrix[dec][clDec];
        }
        if (m_DecisionAttribute.isMinorityValueSet())
        	m_nLocalMinorityDec = m_DecisionAttribute.localValueCode(m_DecisionAttribute.getMinorityValueGlobalCode());
    }
    
    /**
     * Constructor for unsuccessful test.
     * 
     * @param errorMsg	Error message.
     */
    public TestResult(String errorMsg)
    {
    	m_Error = errorMsg;
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
     * Returns true if this result provides measures for imbalanced data.
     * 
     * @return  True if this result provides measures for imbalanced data.
     */
    public boolean hasMeasuresForImbalanced()
    {
    	return m_DecisionAttribute.isMinorityValueSet();
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
     * Returns sensitivity (recall, true positive rate) counted for minority decision class.
     *
     * @return  Sensitivity for minority decision class.
     */
    public double getSensitivity()
    {
    	if(m_nLocalMinorityDec == -1) throw new RuntimeException("The result does not provide measures for imbalanced data");
    	return getLocalDecAccuracy(m_nLocalMinorityDec);
    }
    
    /**
     * Returns specificity (true negative rate), i.e. accuracy for majority decision class.
     *
     * @return  Specificity for majority decision class.
     */
    public double getSpecificity()
    {
    	if(m_nLocalMinorityDec == -1) throw new RuntimeException("The result does not provide measures for imbalanced data");
    	return getLocalDecAccuracy(1 - m_nLocalMinorityDec);
    }
    
    /**
     * Returns precision (positive predictive value) counted for minority decision class.
     *
     * @return  Precision for minority decision class.
     */
    public double getPrecision()
    {
    	if(m_nLocalMinorityDec == -1) throw new RuntimeException("The result does not provide measures for imbalanced data");
        if (m_arrDecDistr[m_nLocalMinorityDec]==0) return 0.0;
        else return (double)m_arrConfusionMatrix[m_nLocalMinorityDec][m_nLocalMinorityDec]/(double)m_arrPredictedDecDistr[m_nLocalMinorityDec];
    }
  
    /**
     * Returns G-mean classification measure.
     *
     * @return  G-mean classification measure.
     */
    public double getGmean()
    {
    	if (m_DecisionAttribute.noOfValues() != 2) throw new RuntimeException("G-mean is defined only for classification with two decision classes");
    	double productOfValues = 1;
    	for (int dec = 0; dec < m_DecisionAttribute.noOfValues(); dec++)
        	productOfValues *= getLocalDecAccuracy(dec);
    	return Math.sqrt(productOfValues);
    }
    
    /**
     * Returns F-measure counted for minority class.
     * 
     * @return  F-measure counted for minority class.
     */
    public double getFmeasure()
    {
    	if(m_nLocalMinorityDec == -1) throw new RuntimeException("The result does not provide measures for imbalanced data");
    	double recall = getSensitivity();
    	double precision = getPrecision();
    	double Fmeasure = 2 * (precision * recall) / (precision + recall);
    	if (Double.isNaN(Fmeasure)) return 0;
    	return Fmeasure;
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
     * Returns classification accuracy for a decision class with a given local code.
     *
     * @param decLocCode   Local code of a decision class.
     * @return             Classification accuracy for a decision class with a given local code.
     */
    private double getLocalDecAccuracy(int decLocCode)
    {
        if (m_arrDecDistr[decLocCode]==0) return 0.0;
        else return (double)m_arrConfusionMatrix[decLocCode][decLocCode]/(double)m_arrDecDistr[decLocCode];
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
     * Returns string representation of classification results.
     *
     * @return  String representation of classification results.
     */
    public String toString()
    {
    	if (m_Error != null)
    		return m_Error;
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
        sbuf.append("Accuracy: "+df.format(getAccuracy())+Report.lineSeparator);
        for (int dec = 0; dec < m_DecisionAttribute.noOfValues(); dec++)
        {
        	double decCode = m_DecisionAttribute.globalValueCode(dec);
            sbuf.append("  Decision "+NominalAttribute.stringValue(decCode)+": "+df.format(getDecAccuracy(decCode))+Report.lineSeparator);
        }
        if (m_nLocalMinorityDec != -1)
        {
        	sbuf.append("F-measure: " + df.format(getFmeasure()) + Report.lineSeparator);
        	sbuf.append("G-mean: " + df.format(getGmean()) + Report.lineSeparator);
        	sbuf.append("Sensitivity: " + df.format(getSensitivity()) + Report.lineSeparator);
        }
        return sbuf.toString();
    }

    /**
     * Returns string representation of classification measures for imbalanced data. 
     *
     * @return  String representation of classification measures for imbalanced data.
     */
    public String toStringStats()
    {
    	if (m_Error != null)
    		return m_Error;
    	StringBuffer sbuf = new StringBuffer();
    	sbuf.append("true positive rate (sensitivity, recall)=" + getSensitivity() + Report.lineSeparator);
    	sbuf.append("positive predictive value (precision)=" + getPrecision() + Report.lineSeparator);
    	sbuf.append("dec distribution=" + Arrays.toString(m_arrDecDistr) + Report.lineSeparator);
    	sbuf.append("predicted dec distribution=" + Arrays.toString(m_arrPredictedDecDistr) + Report.lineSeparator);
    	sbuf.append("G-mean=" + getGmean() + Report.lineSeparator);
    	sbuf.append("F-measure=" + getFmeasure() + Report.lineSeparator);
    	return sbuf.toString();
    }    

    /**
     * Returns string representation of confusion matrix.
     *
     * @return  String representation of confusion matrix.
     */
    public String toStringConfusionMatrix()
    {
    	if (m_Error != null)
    		return m_Error;
        StringBuffer sbuf = new StringBuffer();
        for (int dec = 0; dec < m_DecisionAttribute.noOfValues(); dec++) {
        	sbuf.append("real dec=" + dec + "\t(" + NominalAttribute.stringValue(m_DecisionAttribute.globalValueCode(dec)) + ")\t");
            for (int decClassified = 0; decClassified < m_DecisionAttribute.noOfValues(); decClassified++) {
            	sbuf.append(m_arrConfusionMatrix[dec][decClassified] + "\t");
            }
            sbuf.append(Report.lineSeparator);
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
    	if (m_Error != null)
    		stats.put("error", m_Error);
    	else
    	{
    		stats.put("all_cnt",Integer.toString(m_nAll));
    		stats.put("cover_cnt",Integer.toString(m_nCovered));
    		stats.put("correct_cnt",Integer.toString(m_nCorrect));
    		stats.put("precision",Double.toString(((double)m_nCorrect)/((double)m_nCovered)));
    		stats.put("coverage",Double.toString(((double)m_nCovered)/((double)m_nAll)));
    		stats.put("accuracy",Double.toString(((double)m_nCorrect)/((double)m_nAll)));
    	}
    	return stats;
    }
}

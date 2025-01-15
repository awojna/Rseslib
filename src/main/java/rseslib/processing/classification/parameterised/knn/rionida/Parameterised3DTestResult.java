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

package rseslib.processing.classification.parameterised.knn.rionida;

import rseslib.util.pair.PairObjObj;
import rseslib.processing.classification.TestResult;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.system.Report;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Classification accuracy for the whole test set
 * and for particular decision classes
 * obtained by parameterized classification method.
 *
 * @author Grzegorz Gora
 */
public class Parameterised3DTestResult
{
//stare
//    /** Parameter name. */
//    String m_ParameterName;
//zmiana ggora 5.08.2016
    MultiDimensionalParameters m_multiDimParams; 
    /** Array of classification results for succesive parameter values. */
    TestResult[] m_arrClassificationResults = null;
    /** Dictionary of general statistics. */
    Properties m_Statistics;

//    /**
//     * Constructor.
//     * Zmiana ggora 5.08.2016
//     *
//     * @param parameterName                  Parameter name.
//     * @param decDistr                       Distribution of objects for particular decisions.
//     * @param parameterizedConfusionMatrices Confusion matrices obtained in classification
//     *                                       fort particular parameter values.
//     * @param decAttr                        Information about decision attribute.
//     * @param statistics                     Dictionary of general statistics.
//     */
//    public Parameterised3DTestResult(MultiDimensionalParameters _multiDimParams, NominalAttribute decAttr, int[] decDistr, int[][][] parameterizedConfusionMatrices, Properties statistics)
//    {
//    	m_multiDimParams = _multiDimParams;
//        m_arrClassificationResults = new TestResult[parameterizedConfusionMatrices.length];
//        for (int paramVal = 0; paramVal < m_arrClassificationResults.length; paramVal++)
//            if (parameterizedConfusionMatrices[paramVal]!=null)
//                m_arrClassificationResults[paramVal] = new TestResult(decAttr, decDistr, parameterizedConfusionMatrices[paramVal], null);
//        m_Statistics = statistics;
//    }

    //dodane: ggora 30.06.2016
    /**
     * Constructor.
     * Zmiana ggora 5.08.2016
     *
     * @param parameterName                  Parameter name.
     * @param decDistr                       Distribution of objects for particular decisions.
     * @param parameterizedConfusionMatrices Confusion matrices obtained in classification
     *                                       fort particular parameter values.
     * @param decAttr                        Information about decision attribute.
     * @param _minorityClass				 global code for minority class 
     * @param statistics                     Dictionary of general statistics.
     */
//    public Parameterised3DTestResult(String parameterName, NominalAttribute decAttr, int[] decDistr, int[][][] parameterizedConfusionMatrices, Properties statistics, double _minorityClass) //stare
//    public Parameterised3DTestResult(MultiDimensionalParameters _multiDimParams, NominalAttribute decAttr, int[] decDistr, int[][][] parameterizedConfusionMatrices, Properties statistics, double _minorityClass)
    public Parameterised3DTestResult(MultiDimensionalParameters _multiDimParams, NominalAttribute decAttr, int[] decDistr, int[][][] parameterizedConfusionMatrices, Properties statistics)
    {
//        m_ParameterName = parameterName;
    	m_multiDimParams = _multiDimParams;
        m_arrClassificationResults = new TestResult[parameterizedConfusionMatrices.length];
        for (int paramVal = 0; paramVal < m_arrClassificationResults.length; paramVal++)
            if (parameterizedConfusionMatrices[paramVal]!=null)
//                m_arrClassificationResults[paramVal] = new TestResult(decAttr, decDistr, parameterizedConfusionMatrices[paramVal], null, _minorityClass);
            	m_arrClassificationResults[paramVal] = new TestResult(decAttr, decDistr, parameterizedConfusionMatrices[paramVal], null);
        m_Statistics = statistics;
    }

    public Parameterised3DTestResult(MultiDimensionalParameters _multiDimParams, NominalAttribute decAttr, int[] decDistr, TestResult[] parameterizedTestResult, Properties statistics)
    {
    	m_multiDimParams = _multiDimParams;
        m_arrClassificationResults = parameterizedTestResult;
        m_Statistics = statistics;
    }
    
    
//zakomentowa�em ggora 5.08.2016
//     /**
//      * Returns parameter name.
//      *
//      * @return Parameter name.
//      */
//     public String getParameterName()
//     {
//         return m_ParameterName;
//     }

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

     /*
      * uwaga z tego korzystam w p�tli do wypisywania wszystkich tych miar do pliku (w klasie AbstractParameterised3DClassifier)
      * Dlatego nie dodawa� tutaj miary, kt�rych tam nie u�ywam np. Accuracy
      */
     public enum PrintMeasureInMatrix { Fmeasure, Gmean, Sensitivity, Specificity, Precision, Accuracy}; 
 	
 	/**
      * Generates String representing matrix containing F-measure values for different parameters from class m_multiDimParams
      * @ggora zmiana 26.10.2017 - nie by�o dostosowane do mo�liwych macierzy 4D
      * Uwaga: Na razie zrobione tylko dla macierzy 3D - chyba nie b�dzie potrzeba dla 4D, bo i tak trudno zwizualizowa�. Po prostu sztucznie ustawiam warto�� paramValues.sMajValue
      *
      * 
      * @return String representing matrix with F-measure for different parameters 
      */
    public String getMatrix(PrintMeasureInMatrix _printMeasureInMatrix) {
    	StringBuffer sbuf = new StringBuffer();
    	 for (int sMinIndex = 0; sMinIndex < m_multiDimParams.m_sMinValues.size(); sMinIndex++) {
    		 double sMinValue = m_multiDimParams.m_sMinValues.getParamValueByIndex(sMinIndex);
        	 sbuf.append(Report.lineSeparator);
        	 sbuf.append("sMin=" + sMinValue + Report.lineSeparator);
        	 //dla ka�dego parametru s wypisz macierz dla r�nych k i p
        	 sbuf.append("p\\k" + "\t");
        	 for (int k = 0; k < m_multiDimParams.m_kValues.size(); k++) {
        		 sbuf.append(m_multiDimParams.m_kValues.getParamValueByIndex(k) + "\t");
        	 }
        	 sbuf.append(Report.lineSeparator);
        	 for (int p = 0; p < m_multiDimParams.m_pValues.size(); p++) {
        		 double pValue = m_multiDimParams.m_pValues.getParamValueByIndex(p);
            	 sbuf.append(pValue + "\t"); //pierwsza kolumna
        		 for (int k = 0; k < m_multiDimParams.m_kValues.size(); k++) {
        			 int kValue = m_multiDimParams.m_kValues.getParamValueByIndex(k);
        			 ValuesForParameters paramValues = new ValuesForParameters();
        			 paramValues.kValue = kValue;
        			 paramValues.pValue = pValue;
        			 paramValues.sMinValue = sMinValue;
        			 paramValues.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue();
        			 int indexInOneDimensionalTable = m_multiDimParams.getIndexForParams(paramValues);
        			 double measureValue = getMeasureValue(_printMeasureInMatrix, indexInOneDimensionalTable);
                	 sbuf.append(measureValue + "\t");
        		 }
            	 sbuf.append(Report.lineSeparator);
        	 }
    	 }
    	 return sbuf.toString();
     }


    
    
    /**
     * Generates String representing differences in matrix containing F-measure values for consequent s parameters values
     * @ggora zmiana 26.10.2017 - nie by�o dostosowane do mo�liwych macierzy 4D
     * Uwaga: Jak wy�ej tzn. Na razie zrobione tylko dla macierzy 3D - chyba nie b�dzie potrzeba dla 4D, bo i tak trudno zwizualizowa�. Po prostu sztucznie ustawiam warto�� paramValues.sMajValue
     * 
     * @return String representing differences in matrix with F-measure (matrix for different p and s values) for different parameters s 
     */
    public String getMatrixDeltas(PrintMeasureInMatrix _printMeasureInMatrix) {
    	StringBuffer sbuf = new StringBuffer();
    	StringBuffer sbuf_forMax = new StringBuffer();
		sbuf.append(Report.lineSeparator);
		sbuf_forMax.append(Report.lineSeparator);
		double maxValue = 0;
    	for (int sMinIndex = 0; sMinIndex < m_multiDimParams.m_sMinValues.size(); sMinIndex++) {
    		double sMinValue = m_multiDimParams.m_sMinValues.getParamValueByIndex(sMinIndex);
    		double sValue_previous = -1;
    		double previous_maxValue = 0;
    		if (sMinIndex > 0) {
    			sValue_previous = m_multiDimParams.m_sMinValues.getParamValueByIndex(sMinIndex-1);
    			previous_maxValue = maxValue;
    		}
    		sbuf.append("sMin=" + sMinValue + Report.lineSeparator);
    		sbuf_forMax.append("sMin=" + sMinValue + Report.lineSeparator);
    		double delta = 0;
    		maxValue = 0;
    		double delta_maxValue = 0;
    		ArrayList<PairObjObj<Double, Integer>> arrValuesForMax = new ArrayList<PairObjObj<Double,Integer>>(); 
    		//dla ka�dego parametru s policz r�nic� na macierzach tej i poprzedniej
    		for (int p = 0; p < m_multiDimParams.m_pValues.size(); p++) {
    			double pValue = m_multiDimParams.m_pValues.getParamValueByIndex(p);
    			for (int k = 0; k < m_multiDimParams.m_kValues.size(); k++) {
    				int kValue = m_multiDimParams.m_kValues.getParamValueByIndex(k);
    				ValuesForParameters paramValues = new ValuesForParameters();
    				paramValues.kValue = kValue;
    				paramValues.pValue = pValue;
    				paramValues.sMinValue = sMinValue;
    				paramValues.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue();
    				int indexInOneDimensionalTable = m_multiDimParams.getIndexForParams(paramValues);
    				double measureValue = getMeasureValue(_printMeasureInMatrix, indexInOneDimensionalTable);
    				if (measureValue > maxValue) {
    					maxValue = measureValue;
    					arrValuesForMax.clear();
    					arrValuesForMax.add(new PairObjObj<Double, Integer>(pValue, kValue));
    				}
    				else if (measureValue == maxValue) {
    					arrValuesForMax.add(new PairObjObj<Double, Integer>(pValue, kValue));
    				}
    				
    				if (sMinIndex > 0) {
        				paramValues.sMinValue = sValue_previous;
        				indexInOneDimensionalTable = m_multiDimParams.getIndexForParams(paramValues);
           			 	double previous_measureValue = getMeasureValue(_printMeasureInMatrix, indexInOneDimensionalTable);
        				delta += Math.abs(measureValue - previous_measureValue);
        				delta_maxValue = maxValue - previous_maxValue; 
    				}
    			} //k
    		} //p
			if (sMinIndex > 0) {
				sbuf.append("delta=\t" + delta);
	    		sbuf.append(Report.lineSeparator);
				sbuf.append("delta_maxValue=\t" + delta_maxValue);
	    		sbuf.append(Report.lineSeparator);
			}
			sbuf_forMax.append("maxValue=\t" + maxValue + "\t" + arrValuesForMax);
    		sbuf_forMax.append(Report.lineSeparator);
    	} //s
		sbuf.append(Report.lineSeparator);
		sbuf_forMax.append(Report.lineSeparator);
    	return sbuf.toString() +sbuf_forMax;
    }
    
    /*
     * Pomocnicza metoda - w 3 miejscach jest u�ywany ten kod (w metodzie getMatrix() i getMatrixDeltas()), zatem da�em to jako metod�. Po prostu wylicza warto�� okre�lonej miary dla policzonego
     * indeksu w tablicy jednowymiarowej
     */
    private double getMeasureValue(PrintMeasureInMatrix _printMeasureInMatrix, int _indexInOneDimensionalTable) {
    	double measureValue;
    	switch (_printMeasureInMatrix) {
    	case Fmeasure:
    		measureValue = m_arrClassificationResults[_indexInOneDimensionalTable].getFmeasure();
    		break;
    	case Gmean:
    		measureValue = m_arrClassificationResults[_indexInOneDimensionalTable].getGmean();
    		break;
    	case Sensitivity:
    		measureValue = m_arrClassificationResults[_indexInOneDimensionalTable].getSensitivity(); //zwane tak�e recall
    		break;
    	case Specificity:
    		measureValue = m_arrClassificationResults[_indexInOneDimensionalTable].getSpecificity();
    		break;
    	case Precision:
    		measureValue = m_arrClassificationResults[_indexInOneDimensionalTable].getPrecision();
    		break;
    	case Accuracy:
    		measureValue = m_arrClassificationResults[_indexInOneDimensionalTable].getAccuracy();
    		break;
    	default:
    		throw new IllegalArgumentException("nie powinien tutaj wej��");
    	}
    	return measureValue;
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
//                && m_arrClassificationResults[paramVal].getAccuracy() > m_arrClassificationResults[bestParameter].getAccuracy())) //stare
                && m_arrClassificationResults[paramVal].getFmeasure() > m_arrClassificationResults[bestParameter].getFmeasure()))
                bestParameter = paramVal;
        String parameterName = m_multiDimParams.m_kValues.getParameterName() + ", " + m_multiDimParams.m_pValues.getParameterName() + ", " + m_multiDimParams.m_sMinValues.getParameterName();
//        sbuf.append("Best "+m_ParameterName+" = "+bestParameter+Report.lineSeparator+m_arrClassificationResults[bestParameter].toString()); //stare
        sbuf.append("Best "+parameterName+" = "+bestParameter+Report.lineSeparator+m_arrClassificationResults[bestParameter].toString()); //zmiana ggora 5.08.2016
        for (int paramVal = 0; paramVal < m_arrClassificationResults.length; paramVal++)
            if (m_arrClassificationResults[paramVal]!=null)
            {
            	ValuesForParameters paramValues = m_multiDimParams.getParamsForIndex(paramVal);
//              sbuf.append(m_ParameterName+" = "+paramVal+Report.lineSeparator+m_arrClassificationResults[paramVal].toString()); //stare
          	sbuf.append(parameterName+" = "+paramVal+"(k="+ paramValues.kValue + ", p=" + paramValues.pValue  + ", s=" + paramValues.sMinValue +")"+Report.lineSeparator+m_arrClassificationResults[paramVal].toString()); //zmiana ggora 5.08.2016
            }
        return sbuf.toString();
    }
}

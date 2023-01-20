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

package rseslib.processing.classification.parameterised.knn.rionida;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import rseslib.processing.classification.parameterised.SingleParameter;
import rseslib.system.Report;
import rseslib.system.output.StandardOutput;
import weka.core.OptionHandler;
import weka.core.pmml.FieldMetaInfo.Value;

/**
 * Class describing many parameters.
 * Each parameter is one dimension of multidimensional space of parameters.
 * 
 * @author Grzegorz Gora
 */
public class MultiDimensionalParameters implements Serializable {

    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	int[] m_arrValuesForParameters;
	
	public SingleParameter<Integer> m_kValues;
	public SingleParameter<Double> m_pValues;
	public SingleParameter<Double> m_sMinValues;
	public SingleParameter<Double> m_sMajValues;
	
	 /* 
	  * jakakolwiek warto�� dla sMaj na przypadek optymalizacji 3-wymiarowej;
	  * wa�ne �eby to by�a jedna warto�� i �eby ci�gle t� warto�ci� si� pos�ugiwa� przy odwo�aniu do tablicy 4-wymiarowej
	  * Uwaga: nie powinna to by� warto�� ujemna, bo wtedy innaczej b�dzie si� zachowywa� klasyfiaktor tak�e w przypadku 3D dla sprzeczno�ci (chodzi o przyspieszenie
	  * w przypadku sprzeczno�ci, a dla warto�ci s ujmnych (oznaczaj�cych metod� kNN) wszystko i tak jest liczone
	  */
	private static final double s_artifical_sMajValue = 1.0;
	
	/**
	 * Coonstructor dla 4-wymiarowej tablicy
	 * @param _kValues - class SingleParameter describing parameter for k values
	 * @param _pValues - class SingleParameter describing parameter for p values
	 * @param _sMinValues - class SingleParameter describing parameter for s values
	 */
	public MultiDimensionalParameters(SingleParameter<Integer> _kValues, SingleParameter<Double> _pValues, SingleParameter<Double> _sMinValues, SingleParameter<Double> _sMajValues) {
		m_kValues = _kValues;
		m_pValues = _pValues;
		m_sMinValues = _sMinValues;
		m_sMajValues = _sMajValues;
	}

	/**
	 * Zwraca sztuczn� warto�� dla sMaj (4 wymiaru). Wa�ne, �eby zawsze pos�ugiwa� si� t� jedn� warto�ci�. A jest potrzebna, �eby jako� dosta� si� do indeksu w tablicy
	 * @return s_artifical_sMajValue
	 */
	public static double getArtifical_sMajValue() {
		return s_artifical_sMajValue;
	}	
	
	/**
	 * Volume of multi dimensional space of parameters.
	 * @return
	 */
	public int getVolume() {
		return m_kValues.size() * m_pValues.size() * m_sMinValues.size() * m_sMajValues.size(); 
	}

	
	/**
	 * Transformation of point in multi dimensional space of parameters into one dimensional array.
	 * It is not possible to use this method outside because it would be easy to mistake order of parameters.  
	 * @param _kValue - k value (value in first dimension)
	 * @param _pValue - p value (value in second dimension)
	 * @param _sMinValue - sMin value (value in third dimension)
	 * @param _sMajValue - sMaj value (value in fourth dimension)
	 * @return index in one dimensional array corresponding to point in multi dimensional space
	 */
	private int getIndexForValues(Integer _kValue, Double _pValue, Double _sMinValue, Double _sMajValue) {
		int sizeFor_kValue = m_kValues.size();
		int sizeFor_pValue = m_pValues.size();
		int sizeFor_sMinValue = m_sMinValues.size();
		int indexFor_kValue = m_kValues.getIndexOfValue(_kValue);
		int indexFor_pValue = m_pValues.getIndexOfValue(_pValue);
		int indexFor_sMinValue = m_sMinValues.getIndexOfValue(_sMinValue);
		int indexFor_sMajValue = m_sMajValues.getIndexOfValue(_sMajValue);
		int index2D = indexFor_kValue + sizeFor_kValue * indexFor_pValue;
		int index3D = index2D + sizeFor_kValue * sizeFor_pValue * indexFor_sMinValue;
		int index4D = index3D + sizeFor_kValue * sizeFor_pValue * sizeFor_sMinValue * indexFor_sMajValue; 
		return index4D;
	}
	
	
	/**
	 * Transformation of point in multi dimensional space of parameters into one dimensional array.
	 * @param _params - parameters describing point in multi dimensional space of parameters
	 * @return index in one dimensional array corresponding to point in multi dimensional space
	 */
	public int getIndexForParams(ValuesForParameters _params) {
		return getIndexForValues(_params.kValue, _params.pValue, _params.sMinValue, _params.sMajValue);
	}

	/**
	 * Transformation of point in one dimensional array into point in multi dimensional space of parameters.
	 * @param _index - index in one dimensional array
	 * @return ValuesForParameters describing point in multi dimensional space of parameters corresponding to point in array with _index
	 */
	public ValuesForParameters getParamsForIndex(int _index) {
		int sizeFor_kValue = m_kValues.size();
		int sizeFor_pValue = m_pValues.size();
		int sizeFor_sMinValue = m_sMinValues.size();
		int index4D = _index;
		int indexFor_sMajValue = index4D / (sizeFor_kValue * sizeFor_pValue * sizeFor_sMinValue);
		double sMajValue = m_sMajValues.getParamValueByIndex(indexFor_sMajValue);
//		int index3D = _index;
		int index3D = index4D % (sizeFor_kValue * sizeFor_pValue * sizeFor_sMinValue);
		int indexFor_sMinValue = index3D / (sizeFor_kValue * sizeFor_pValue);
		double sMinValue = m_sMinValues.getParamValueByIndex(indexFor_sMinValue);
		int index2D = index3D % (sizeFor_kValue * sizeFor_pValue);
		int indexFor_pValue = index2D / sizeFor_kValue;
		double pValue = m_pValues.getParamValueByIndex(indexFor_pValue);
		int indexFor_kValue = index2D % sizeFor_kValue;
		int kValue = m_kValues.getParamValueByIndex(indexFor_kValue);
		ValuesForParameters valuesForParams = new ValuesForParameters();
		valuesForParams.kValue = kValue;
		valuesForParams.pValue = pValue;
		valuesForParams.sMinValue = sMinValue;
		valuesForParams.sMajValue = sMajValue;
		return valuesForParams;
	}
	
    /**
     * Returns a string representation of this object.
     *
     * @return String representation of this object.
     */
	public String toString() {
		return	"volume=" + getVolume() + System.lineSeparator() +
		"m_kValues:" + "size=" + m_kValues.size() + "; " + m_kValues + System.lineSeparator() +
		"m_pValues:" + "size=" + m_pValues.size() + "; " + m_pValues + System.lineSeparator() +
		"m_sMinValues:" + "size=" + m_sMinValues.size() + "; " + m_sMinValues + System.lineSeparator() + 
		"m_sMajValues:" + "size=" + m_sMajValues.size() + "; " + m_sMajValues;
	}

    /**
     * Returns a string representation of this object with details.
     *
     * @return String representation of this object with details.
     */
	public String toStringDetails() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(this.toString() + Report.lineSeparator);
		for (int i = 0; i < this.getVolume(); i++) {
			ValuesForParameters valuesForParams = this.getParamsForIndex(i);
			int index = this.getIndexForParams(valuesForParams);
			sbuf.append("i=" + i + ", params:" + valuesForParams + ", index=" + index + Report.lineSeparator);
			if (index != i) throw new IllegalStateException("co� nie tak: index powinien by� r�wny i");
		}
		return sbuf.toString();
	}
	
    /**
     * Main testing method.
     *
     * @param args Parameters of the method: a path to a training
     *             and a test data file and the data format name.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
        Report.addInfoOutput(new StandardOutput());
        ArrayList<Integer> kValues = SingleParameter.getIntArithmeticSequence(0, 1, 4);
        SingleParameter<Integer> kParamValues = new SingleParameter<Integer>("kParameter", kValues);
//        ArrayList<Double> pValues = ParameterValues.getDoubleArithmeticSequence(0, 0.05, 10);
        ArrayList<Double> pValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0, 0.2, 0.05);
        SingleParameter<Double> pParamValues = new SingleParameter<Double>("pPaarameter", pValues);
//        ArrayList<Double> sMinValues = SingleParameter.getDoubleSingleValue(1.0);
        ArrayList<Double> sMinValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0, 0.2, 0.1);
        SingleParameter<Double> sMinParamValues = new SingleParameter<Double>("sParameter", sMinValues);
        ArrayList<Double> sMajValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0.8, 1.0, 0.1);
        SingleParameter<Double> sMajParamValues = new SingleParameter<Double>("sParameter", sMajValues);
        MultiDimensionalParameters multiDimParams = new MultiDimensionalParameters(kParamValues, pParamValues, sMinParamValues, sMajParamValues);
        Report.displaynl("multiDimParams:\n" + multiDimParams);
        for (int i = 0; i < multiDimParams.getVolume(); i++) {
        	ValuesForParameters valuesForParams = multiDimParams.getParamsForIndex(i);
        	int index = multiDimParams.getIndexForParams(valuesForParams);
        	Report.displaynl("i=" + i + ", params:" + valuesForParams + ", index=" + index);
        	if (index != i) throw new IllegalStateException("co� nie tak: index powinien by� r�wny i");
		}
        Report.close();
    }


	
}

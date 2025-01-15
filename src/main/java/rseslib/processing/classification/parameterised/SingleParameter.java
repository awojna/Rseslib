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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class describing single parameter (one dimension parameter).
 * It contains name of the parameter and lists of possible values for this parameter.
 * 
 * @author     Grzegorz Gora
 */
public class SingleParameter<T extends Number> implements Serializable {

    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	/** Constant used to multiply and divide values in order to round floating-point values properly in arithmetic series. */
	private static int MULTIPLY_TO_ROUND = 100000;	
	/** Name of this parameter */
	protected String m_strParameterName;
	/** Array containing possible values for this parameter. */
	protected ArrayList<T> m_arr_paramValues;
	/** Map of values into index in array m_arr_paramValues. */
	HashMap<T, Integer> m_mapValuesIntoIndex = new HashMap<T, Integer>();

	/**
	 * Constructor
	 * @param _paramName - parameter name
	 * @param _arr_paramValues - list of all possible values for this parameter
	 */
	public SingleParameter(String _paramName, ArrayList<T> _arr_paramValues) {
		m_strParameterName = _paramName;
		m_arr_paramValues = _arr_paramValues;
		checkRepetitionsAndCreateMap(m_arr_paramValues);
	}
	
	/**
	 * It is assumed that values of the parameter are given in the list of increasing values.
	 * This method checks this assumption (checks whether on the list are different, increasing values).
	 * Generally used for ArrayList<Double> or ArrayList<Integer>
	 */
	private void checkRepetitionsAndCreateMap(ArrayList<T> _arrList) {
		int size = _arrList.size();
		if (size == 0) return;
		double lastValue = _arrList.get(0).doubleValue();
		m_mapValuesIntoIndex.put(_arrList.get(0), 0);
		for (int i = 1; i < _arrList.size(); i++) {
			double value = _arrList.get(i).doubleValue();
			if (lastValue >= value) throw new IllegalArgumentException("Values should be strictly increasing");
			m_mapValuesIntoIndex.put(_arrList.get(i), i);
			lastValue = value;
		}
	}
	
	/**
	 * Returns number of values for this parameter
	 * @return size of the list of values of this parameter given in the constructor
	 */
	public int size() {
		return m_arr_paramValues.size();
	}
	
	/**
	 * Returns value of parameter from given index
	 * @param _index - index of value (counting from 0)
	 * @return _index-ith value from the values given in the constructor
	 */
	public T getParamValueByIndex(int _index) {
		return m_arr_paramValues.get(_index);
	}
	
	/**
	 * Returns index corresponding to the given value.
	 * @param _value - any value from the list of values in this parameter
	 * @return index of given value or null if value does not exist in the list of values of this parameter
	 */
	public int getIndexOfValue(T _value) {
		Integer index;
		index = m_mapValuesIntoIndex.get(_value);
		if (index == null) throw new IllegalArgumentException("Value " + _value + " does not exist in the list of values of this parameter");
		return index;
	}
	
	/**
	 * Returns name of this parameter.
	 * @return the m_strParameterName
	 */
	public String getParameterName() {
		return m_strParameterName;
	}
	
    /**
     * Returns a string representation of this object.
     *
     * @return String representation of this object.
     */
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(m_strParameterName + ":");
	    boolean next = false;
	    for(T item : m_arr_paramValues) {
	    	if(next)
	    		sbuf.append(", ");
	    	else
	    		next = true;
	    	sbuf.append(item.toString());
	    }
	    return sbuf.toString();

	}
	
	/**
	 * Returns arithmetic sequence with given initial term, common difference and number of terms
	 * Thus such sequence is created: <_initialTerm, _initialTerm+_commonDifference, _initialTerm+2*_commonDifference, ...>, where number of terms is given
	 * @param _initialTerm - initial term
	 * @param _commonDifference - common difference
	 * @param _numberOfTerms - number of terms
	 * @return
	 */
	public static ArrayList<Integer> getIntArithmeticSequence(int _initialTerm, int _commonDifference, int _numberOfTerms) {
    	ArrayList<Integer> arrIntList = new ArrayList<Integer>();
    	int currentValue = _initialTerm;
		for (int i = 0; i < _numberOfTerms; i++) {
			arrIntList.add(currentValue);
			currentValue += _commonDifference;
		}
		return arrIntList;
	}

	/**
	 * Returns arithmetic sequence given initial term, last term and common difference
	 * Thus such sequence is created: <_initialTerm, _initialTerm+_commonDifference, _initialTerm+2*_commonDifference, ...>, where last term i equal or smaller than _lastTerm 
	 * @param _initialTerm - initial term
	 * @param _lastTerm - last term
	 * @param _commonDifference - common difference
	 * @return
	 */
	public static ArrayList<Integer> getIntArithmeticSequenceFromTo(int _initialTerm, int _lastTerm, int _commonDifference) {
    	ArrayList<Integer> arrIntList = new ArrayList<Integer>();
    	int currentValue = _initialTerm;
    	while (currentValue <= _lastTerm) {
			arrIntList.add(currentValue);
			currentValue += _commonDifference;
		}
		return arrIntList;
	}
	
	/**
	 * Analogical method for double values.
	 * @see getIntArithmeticSequence
	 */
	public static ArrayList<Double> getDoubleArithmeticSequence(double _initialTerm, double _commonDifference, int _numberOfTerms) {
    	ArrayList<Double> arrDoubleList = new ArrayList<Double>();
    	double currentValue = _initialTerm;
		for (int i = 0; i < _numberOfTerms; i++) {
			arrDoubleList.add(currentValue);
			currentValue += _commonDifference;
		}
		return arrDoubleList;
	}

	/**
	 * Analogical method for double values.
	 * @see getIntArithmeticSequenceFromTo
	 */
	public static ArrayList<Double> getDoubleArithmeticSequenceFromTo(double _initialTerm, double _lastTerm, double _commonDifference) {
    	ArrayList<Double> arrDoubleList = new ArrayList<Double>();
    	double currentValue = _initialTerm;
    	while (currentValue <= _lastTerm) {
			arrDoubleList.add(currentValue);
			currentValue += _commonDifference;
			currentValue *= MULTIPLY_TO_ROUND;
			currentValue = Math.round(currentValue);
			currentValue /= MULTIPLY_TO_ROUND;
		}
		return arrDoubleList;
	}
	
	/**
	 * Returns array containing one given value
	 * @param _value - value
	 * @return
	 */
	public static ArrayList<Double> getDoubleSingleValue(double _value) {
    	ArrayList<Double> arrDoubleList = new ArrayList<Double>();
    	arrDoubleList.add(_value);
		return arrDoubleList;
	}

	/**
	 * Returns array containing two given values
	 * @param _value1 - first value
	 * @param _value2 - second value
	 * @return
	 */
	public static ArrayList<Double> getDoubleTwoValues(double _value1, double _value2) {
    	ArrayList<Double> arrDoubleList = new ArrayList<Double>();
    	arrDoubleList.add(_value1);
    	arrDoubleList.add(_value2);
		return arrDoubleList;
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
//    	ArrayList<Integer> arrIntList = new ArrayList<Integer>();
//    	arrIntList.add(0);
//    	arrIntList.add(1);
//    	arrIntList.add(2);
//    	ParameterValues<Integer> paramIntValues = new ParameterValues<Integer>(arrIntList);
//    	Debug.println("paramIntValues=" + paramIntValues);

    	ArrayList<Integer> arrIntList2 = getIntArithmeticSequence(1, 2, 5);
    	SingleParameter<Integer> paramIntValues2 = new SingleParameter<Integer>("paramIntValues2", arrIntList2);
    	System.out.println("" + paramIntValues2);
    	
    	
//    	ArrayList<Double> arrDoubleList = new ArrayList<Double>();
//    	arrDoubleList.add(0.0);
//    	arrDoubleList.add(1.0);
//    	arrDoubleList.add(2.0);
//    	ParameterValues<Double> paramDoubleValues = new ParameterValues<Double>(arrDoubleList);
//    	Debug.println("paramDoubleValues=" + paramDoubleValues);

    	ArrayList<Double> arrDoubleListMinus = getDoubleSingleValue(-1.0);
    	ArrayList<Double> arrDoubleList2 = getDoubleArithmeticSequence(1.0, 0.5, 5);
    	arrDoubleListMinus.addAll(arrDoubleList2);
//    	SingleParameter<Double> paramDoubleValues2 = new SingleParameter<Double>("paramDoubleValues2", arrDoubleList2);
    	SingleParameter<Double> paramDoubleValues2 = new SingleParameter<Double>("paramDoubleValues2", arrDoubleListMinus);
    	System.out.println("" + paramDoubleValues2);
    	for (int i = 0; i < paramDoubleValues2.size(); i++) {
			Double value = paramDoubleValues2.getParamValueByIndex(i);
			System.out.println("index=" + i + ", value=" + value);
		}
    	for (Double double1 : arrDoubleList2) {
			int index = paramDoubleValues2.getIndexOfValue(double1);
			System.out.println(double1 + " has index " + index);
		}

    }	
}

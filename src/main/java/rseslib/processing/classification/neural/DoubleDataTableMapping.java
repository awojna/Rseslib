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


package rseslib.processing.classification.neural;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;

/**
 * @author Jakub Sakowicz
 *
 * Klasa okresla metode mapowania DoubleData w wektor liczb zmiennoprzecinkowych.
 */
public class DoubleDataTableMapping {
	/**
	 * Atrybut numeryczny
	 */
	private final static int TYPE_NUMERIC = 1;
	/**
	 * Atrybut nominalny
	 */
	private final static int TYPE_NOMINAL = 2;
	
	/**
	 * @author Jakub Sakowicz
	 *
	 * Klasa okreslajaca mapowanie na pojedyncze wyjscie.  
	 * 	 
	 */
	private class Output {
		public int index; 		// numer atrybutu z DoubleData 
		public int type;		// typ atrybutu
		public double data1;	// wartosc aktywacji (dla atrybutow nominalnych),
								// wartosc minimalna (dla atrybutow numerycznych)
		public double data2;	// rozpietosc wartosci
	}
	
	/**
	 * Lista (obiektow Output) okreslajaca mapowanie wyjsc
	 */
	private List<Output> mapping;
	
	/**
	 * Konstruktor
	 * @param ddt - tabelka z danymi, dla ktorych nalezy przygotowac mapowanie
	 */
	public DoubleDataTableMapping(DoubleDataTable ddt) {
		mapping = new ArrayList<Output>();
		
		Header header = ddt.attributes();
		
		for (int i = 0; i < header.noOfAttr(); i++ ) {
			if (header.isConditional(i)) {
				if (header.isNumeric(i))
					addNumeric(i, ddt);				
				if (header.isNominal(i))
					addNominal(i, ddt);				
			}
		}
	}
	
	/**
	 * Dodaje wyjscia odpowiadajace atrybutowi numerycznemu
	 * @param index - indeks atrybutu z DoubleData
	 * @param ddt   - tabelka z danymi
	 */
	public void addNumeric(int index, DoubleDataTable ddt) {
		double min =   1000000;
		double max = - 1000000;
		for (DoubleData dd : ddt.getDataObjects()) {
			double data = dd.get(index);
			if (data < min) min = data;
			if (data > max) max = data;
		}
		addOutput(index, TYPE_NUMERIC, min, 1/Math.max(1, max - min));
	}
	
	/**
	 * Dodaje wyjscia odpowiadajace atrybutowi nominalnemu
	 * @param index - indeks atrybutu z DoubleData
	 * @param ddt   - tabelka z danymi
	 */
	public void addNominal(int index, DoubleDataTable ddt) {
		Set<Double> valueSet = new HashSet<Double>();
		for (DoubleData dd : ddt.getDataObjects()) {
			valueSet.add(new Double(dd.get(index)));
		}
		for (Iterator i = valueSet.iterator(); i.hasNext(); )		
			addOutput(index, TYPE_NOMINAL, ((Double) i.next()).doubleValue(), 0);		
	}
	
	/**
	 * Dodaje wyjscie o okreslonych parametrach
	 * @param index - numer atrybutu z DoubleData 
	 * @param type  - typ atrybutu
	 * @param data1 - wartosc aktywacji (dla atrybutow nominalnych),
	 * 				- wartosc minimalna (dla atrybutow numerycznych)
	 * @param data2 - rozpietosc wartosci
	 */
	public void addOutput(int index, int type, double data1, double data2) {
		Output output = new Output();
		output.index = index;
		output.type = type; 
		output.data1 = data1;
		output.data2 = data2;
		
		mapping.add(output);
	}
	
	/**
	 * Tlumaczy DoubleData na wektor wejsc
	 * @param dd - Double Data
	 * @return wektor wartosci zmiennoprzecinkowych
	 */
	public double[] translate(DoubleData dd) {
		double [] result = new double[noOfOutputs()];
		
		for (int j = 0; j < mapping.size(); j++) {
			Output output = mapping.get(j);
			switch (output.type) {
				case TYPE_NUMERIC:
					// normalizacja
					result[j] = (dd.get(output.index) - output.data1) * output.data2;
					break;
				case TYPE_NOMINAL:
					// aktywacja tylko tego co trzeba
					if (output.data1 == dd.get(output.index))
						result[j] = 1;	
					else
						result[j] = 0;					
					break;
				default:
					result[j] = 0;
			}
		}
		return result;
			
	}
	
	/**
	 * Zwraca ilosc wejsc dla sieci
	 * @return ilosc wejsc
	 */
	public int noOfOutputs() {
		return mapping.size();
	}
	
		
	
}

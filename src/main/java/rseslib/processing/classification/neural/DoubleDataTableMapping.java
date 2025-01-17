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


package rseslib.processing.classification.neural;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;

/**
 * @author Jakub Sakowicz
 *
 * Klasa okresla metode mapowania DoubleData w wektor liczb zmiennoprzecinkowych.
 */
public class DoubleDataTableMapping implements Serializable {
    /**
     * Serialization version.
     */
	private static final long serialVersionUID = 1L;
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
	private class Output implements Serializable {
	    /** Serialization version. */
		private static final long serialVersionUID = 1L;

		public int index; 			// numer atrybutu z DoubleData 
		public int type;			// typ atrybutu
		public NominalAttribute na;	// atrybut jesli nominalny 
		public double data1;		// wartosc aktywacji (dla atrybutow nominalnych),
									// wartosc minimalna (dla atrybutow numerycznych)
		public double data2;		// rozpietosc wartosci
		
		/**
		 * Writes this object.
		 *
		 * @param out			Output for writing.
		 * @throws IOException	if an I/O error has occured.
		 */
		private void writeObject(ObjectOutputStream out) throws IOException
		{
			out.writeInt(index);
			out.writeInt(type);
			if(type == TYPE_NOMINAL) {
				out.writeObject(na);
				out.writeInt(na.localValueCode(data1));
			} else
				out.writeDouble(data1);				
			out.writeDouble(data2);
		}

		/**
		 * Reads this object.
		 *
		 * @param out			Output for writing.
		 * @throws IOException	if an I/O error has occured.
		 */
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
		{
			index = in.readInt();
			type = in.readInt();
			if(type == TYPE_NOMINAL) {
				na = (NominalAttribute)in.readObject();
				data1 = na.globalValueCode(in.readInt());
			} else
				data1 = in.readDouble();
			data2 = in.readDouble();
		}
	}
	
	/**
	 * Lista (obiektow Output) okreslajaca mapowanie wyjsc
	 */
	private List<Output> mapping;
	
	/**
	 * Konstruktor
	 * @param header - naglowek danych, dla ktorych nalezy przygotowac mapowanie
	 * @param data   - tabelka z danymi, dla ktorych nalezy przygotowac mapowanie
	 */
	public DoubleDataTableMapping(Header header, ArrayList<DoubleData> data) {
		mapping = new ArrayList<Output>();
		
		for (int i = 0; i < header.noOfAttr(); i++ ) {
			if (header.isConditional(i)) {
				if (header.isNumeric(i))
					addNumeric(i, data);				
				if (header.isNominal(i))
					addNominal(i, (NominalAttribute)header.attribute(i));				
			}
		}
	}
	
	/**
	 * Dodaje wyjscia odpowiadajace atrybutowi numerycznemu
	 * @param index - indeks atrybutu z DoubleData
	 * @param data  - tabelka z danymi
	 */
	public void addNumeric(int index, ArrayList<DoubleData> data) {
		double min =   1000000;
		double max = - 1000000;
		for (DoubleData dd : data) {
			double val = dd.get(index);
			if (val < min) min = val;
			if (val > max) max = val;
		}
		addOutput(index, TYPE_NUMERIC, null, min, 1/Math.max(1, max - min));
	}
	
	/**
	 * Dodaje wyjscia odpowiadajace atrybutowi nominalnemu
	 * @param index - indeks atrybutu z DoubleData
	 * @param na    - atrybut
	 */
	public void addNominal(int index, NominalAttribute na) {
		for (int i = 0; i < na.noOfValues(); ++i)
			addOutput(index, TYPE_NOMINAL, na, na.globalValueCode(i), 0);		
	}
	
	/**
	 * Dodaje wyjscie o okreslonych parametrach
	 * @param index - numer atrybutu z DoubleData 
	 * @param type  - typ atrybutu
	 * @param data1 - wartosc aktywacji (dla atrybutow nominalnych),
	 * 				- wartosc minimalna (dla atrybutow numerycznych)
	 * @param data2 - rozpietosc wartosci
	 */
	public void addOutput(int index, int type, NominalAttribute na, double data1, double data2) {
		Output output = new Output();
		output.index = index;
		output.type = type;
		output.na = na;
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

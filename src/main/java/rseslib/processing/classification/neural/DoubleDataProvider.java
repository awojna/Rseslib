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


package rseslib.processing.classification.neural;

import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;

/**
 * @author Jakub Sakowicz
 *
 * Klasa implementuj?ca interfejs IInputProvider. Przetwarza DoubleData na zbior n wyjsc.
 * Automatycznie obslugiwane sa atrybuty nominalne (po jednym wyjsciu binarnym na kazda 
 * mozliwa wartosc atrybutu). Dane wyjsciowe sa normalizowane do przedzialu [0..1] (oczywiscie 
 * w zakresie wynikajacym z tabelki testowej, dane do klasyfikacji moga produkowac na wyjsciach 
 * wartosci z poza tego zakresu).
 */
public class DoubleDataProvider implements IInputProvider {

	/**
	 * Konstruktor
	 * @param ddt - tabelka z danymi wejsciowymi
	 */
	public DoubleDataProvider (DoubleDataTable ddt) {
		mapping = new DoubleDataTableMapping(ddt);
		
	}
	
	
	/**
	 * Konstruktor
	 * @param mapping - klasa okreslajaca mapowanie DoubleData na wektor liczb
	 */
	public DoubleDataProvider (DoubleDataTableMapping mapping) {
		this.mapping = mapping;
	}

	
	/**
	 * Klasa okreslajaca mapowanie DoubleData na wektor liczb
	 */
	private DoubleDataTableMapping mapping;
	
	/**
	 * Wektor wyjsciowy ;)
	 */
	private double[] input;
	
	
	/**
	 * Ustawia jako wyjscie translacje danego obiekt DoubleData
	 * @param dd - DoubleData
	 */
	public void setDoubleData(DoubleData dd) {
		input = mapping.translate(dd);
	}
	
	/**
	 * Zwraca ilosc wejsc
	 * @see sid2005.sakowicz.IInputProvider#noOfInputs()
	 */
	public int noOfInputs() {
		return mapping.noOfOutputs();
	}
	
	/**
	 * Zwraca wartosc na i-tym wejsciu
	 * @param i - numer wejscia
	 * @see sid2005.sakowicz.IInputProvider#get(int)
	 */
	public double get(int i) {
		if (i == noOfInputs())
			return 1.0;
		else
			return input[i];
	}

}

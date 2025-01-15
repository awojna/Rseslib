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

import java.util.List;


/**
 * @author Jakub Sakowicz
 *
 * Klasa grupujaca wyjscia perceptronow. Implementuje interfejs IInputProvider. 
 * 
 */
public class PerceptronOutputContainerProvider implements IInputProvider {

	/**
	 * lista perceptronow, ktorych wyjscia maja byc zgrupowane 
	 */
	private List perceptrons;
	
	/**
	 * Konstruktor
	 * @param perceptrons lista perceptronow do zgrupowania wyjsc 
	 */
	public PerceptronOutputContainerProvider(List perceptrons) {
		this.perceptrons = perceptrons;
	}
	
	/**
	 * Zwraca ilosc wyjsc
	 * @see sid2005.sakowicz.IInputProvider#noOfInputs()
	 */
	public int noOfInputs() {
		return perceptrons.size();
	}

	/**
	 * Zwraca wartosc i-tego wyjscia
	 * @see sid2005.sakowicz.IInputProvider#get(int)
	 */
	public double get(int i) {
		if (i == noOfInputs())
			return 1.0;
		else
			return ((Perceptron)perceptrons.get(i)).getOutput();
	}

}

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

import java.io.Serializable;
import java.util.ListIterator;

import rseslib.structure.attribute.NominalAttribute;


/**
 * @author Jakub Sakowicz
 *
 * Klasa odpowiadajaca za najwyzsza warstwe sieci - element grupujacy rezultaty perceptronow 
 * i okreslajacy rzeczywisty wynik dzialania sieci.
 * 
 */
public class ResultCombiner implements IDeviationCounter, Serializable {
    /**
     * Serialization version.
     */
	private static final long serialVersionUID = 1L;

	/**
	 * Lista mozliwych wartosci parametru decyzyjnego
	 */
	private NominalAttribute decAttr;
	/**
	 * Wyniki ostatniej warstwy
	 */
	private IInputProvider input;
	/**
	 * Wynik sieci
	 */
	private double result;
	/**
	 * Wynik rozkladowy sieci
	 */
	private double[] distributedResult;
	/**
	 * Oczekiwany wynik sieci - do liczenia bledow
	 */
	private double expectedResult;
	
	/**
	 * Konstruktor
	 * @param availableResults lista mozliwych wartosci parametru decyzyjnego
	 */
	public ResultCombiner(NominalAttribute decAttr) {
		this.decAttr = decAttr;
	}
	
	/**
	 * Uruchamia proces okreslania wyniku na podstawie ostatniej warstwy
	 */
	public void count() {
		double max = 0; 
		int maxIndex = 0;
		for (int i=0; i < input.noOfInputs(); i++) {
			distributedResult[i] = input.get(i);
			if (distributedResult[i] > max) {
				max = distributedResult[i];
				maxIndex = i;
			}				
		}
		result = decAttr.globalValueCode(maxIndex);		
	}
	
	/**
	 * Zwraca wynik dzialania sieci - nalezy pamietac o wczesniejszym wywolaniu count()
	 * @return przewidywana wartosc parametru decyzyjnego
	 */
	public double getResult() {
		return result;
	}
	
	/**
	 * Zwraca rozkladowy wynik dzialania sieci - nalezy pamietac o wczesniejszym wywolaniu count()
	 * @return Rozklad wag decyzji
	 */
	public double[] getDistributedResult() {
		return distributedResult;
	}
	
	/**
	 * Ustawia ostatnia warstwe sieci 
	 * @param input
	 */
	public void setInput(IInputProvider input) {
		this.input = input;
		distributedResult = new double[input.noOfInputs()];
	}	
	
	/**
	 * Ustawia oczekiwany wynik dzialania
	 * @param value
	 */
	public void setExpectedResult(double value) {
		this.expectedResult = value;
	}
	
	/**
	 * Liczy bledy dla danych perceptronow
	 * @param perceptrons iterator po perceptronach, ktorym nalezy policzyc bledy
	 */ 
	public void countDeviations(ListIterator perceptrons) {
		int i = 0;
		while(perceptrons.hasNext()) {
			Perceptron perceptron =(Perceptron)perceptrons.next();
			double output = perceptron.getOutput();
			
			perceptron.setDeviation(
					Global.DIFFERENTIAL.eval(output) * (expectedForInput(i) - output));
			
			i++;
		}
	}
	
	/**
	 * Zwraca wartosc oczekiwana dla wejscia o zadanym numerze
	 * @param i numer wejscia
	 * @return 1 jesli wejscie odpowiada za wynik, 0 wpp
	 */
	private double expectedForInput(int i) {
		if (decAttr.globalValueCode(i) == expectedResult)
			return 1;
		return 0;
	}
	
}

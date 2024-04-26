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


/**
 * @author Jakub Sakowicz
 *
 * Pojedynczy perceptron
 * 
 */
public class Perceptron {
	/**
	 * Wejscie perceptronu
	 */
	private IInputProvider input;
		
	/**
	 * Wagi elementow wejscia
	 */
	private double[] weights;
	
	/**
	 * Blad perceptronu
	 */
	private double deviation;
	
	/**
	 * Wyjscie perceptronu
	 */
	private double output = 0;
		
	/**
	 * Ustawia obiekt reprezentujacy wejscia perceptronu
	 * @param inputProvider - obiekt implementujacy interfejs IInputProvider
	 */
	public void setInput(IInputProvider inputProvider) {
		this.input = inputProvider;
		this.initWeigths();		
	}
	
	/**
	 * Podmienia obiekt reprezentujacy wejscia perceptronu
	 * @param inputProvider
	 * @author damian
	 */
	/*public void updateInput(IInputProvider inputProvider) {
		this.input = inputProvider;	
	}*/
	
	/**
	 * Liczy wyjscie
	 */
	public void count() {
		double sum = 0;
		for (int i=0; i < weights.length; i++)
			sum += weights[i] * input.get(i);
			
		output = Global.FUNCTION.eval(sum);
	}
	
	/**
	 * Zwraca wynik dzialania perceptronu
	 * @return liczba zmiennoprzecinkowa
	 */
	public double getOutput() {
		return output;		
	}
	
	/**
	 * Ustawia blad perceptronu
	 * @param value - liczba zmiennoprzecinkowa
	 */
	public void setDeviation(double value) {
		this.deviation = value;
	}
	
	/**
	 * Zwraca blad perceptronu
	 * @return double
	 */
	public double getDeviation() {
		return deviation;
	}
	
	/**
	 * Zwraca wage i-tego wejscia
	 * @param i - numer wejscia
	 * @return waga - liczba zmiennoprzecinkowa 
	 */
	public double getWeight(int i) {
		return weights[i];
	}
	
	/**
	 * Poprawia wagi perceptronu zgodnie z ustawionym bledem
	 */
	public void improveWeights() {	
		for (int i=0; i < weights.length; i++)
			weights[i] = weights[i] + (Global.CURRENT_ALFA * deviation * input.get(i));
	}
	
	/**
	 * Inicjalizacja tablicy wag
	 */
	private void initWeigths() {
		weights = new double[input.noOfInputs() + 1];
		for (int i = 0; i < input.noOfInputs() + 1; i ++) {
			weights[i] = Misc.getRandomDouble() / (input.noOfInputs() + 1); // !
		}
	}
	
	/**
	 * Serializuje stan perceptronu do obiektu
	 * @return Object
	 */	
	public Object storeData() {
		return weights.clone();
	}
	
	/**
	 * Deserializuje stan perceptronu z obiektu (do ktorego byl wczesniej zserializowany)
	 * @param data - Object
	 */	
	public void restoreData(Object data) {
		this.weights = (double[])data;
	}
	
	/**
	 * Zwraca minimalna wage dla perceptronu
	 * @return
	 */
	public double getMinWeight() {
		if (weights == null) return 0;
		else {
			double min = weights[0];
			for (int i=0; i< input.noOfInputs(); i++) {
				if (weights[i] < min) min = weights[i];
			}
			return min;
		}
	}
	
	/**
	 * Zwraca maksymalna wage dla perceptronu
	 * @return
	 */
	public double getMaxWeight() {
		if (weights == null) return 0;
		else {
			double max = weights[0];
			for (int i=0; i< input.noOfInputs(); i++) {
				if (weights[i] > max) max = weights[i];
			}
			return max;
		}
	}
	
	/** 
	 * Zwraca liczbe wejsc do perceptronu wraz z 'wejsciem' progu aktywacji
	 * 
	 * @return
	 */
	public int getWeightLength() {
		return weights.length;
	}
	
	public double[] getWeights() {
		return weights;
	}
	
	public IInputProvider getInput() {
		return input;
	}
	
	/**
	 * Metoda podmieniania wag i wejscia - zwiazana z dodaniem nowego perceptronu w 
	 * poprzedniej warstwie sieci
	 * 
	 */
	/*public void recombine(IInputProvider newInput) {
		int i;
		input = newInput;
		double[] oldweights = new double[weights.length];
		for (i=0; i<weights.length; i++) {
			oldweights[i] = weights[i];
		}
		weights = new double[weights.length + 1];
		for (i=0; i<(oldweights.length-1); i++) {
			weights[i] = oldweights[i];
		}		
		weights[oldweights.length-1] = Misc.getRandomDouble() / (input.noOfInputs() + 1);
		weights[oldweights.length] = oldweights[oldweights.length - 1];
	}*/
	
}

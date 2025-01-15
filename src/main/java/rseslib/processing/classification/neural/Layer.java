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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * @author Jakub Sakowicz
 * Damian - dodano zmiany
 * Klasa reprezentujaca warstwe perceptronow
 * 
 */
public class Layer implements IDeviationCounter {
	/**
	 * Wynik dzialania warstwy - wejscie dla nastepnej 
	 */
	private IInputProvider output;

	/**
	 * Perceptrony w warstwie - zmiana na public DW
	 */
	public List<Perceptron> perceptrons;
	
	/**
	 * Konstruktor
	 * @param noOfPerceptrons - ilosc perceptronow w warstwie
	 */
	public Layer(int noOfPerceptrons) {
		perceptrons = new ArrayList<Perceptron>();
		for (int i = 0; i < noOfPerceptrons; i++)
			perceptrons.add(new Perceptron());
		output = new PerceptronOutputContainerProvider(perceptrons);
	}
	
	/**
	 * Zwraca iterator po wszystkich perceptronach w tej warstwie
	 * @return iterator po perceptronach warstwy
	 */	
	public ListIterator<Perceptron> perceptrons() {
		return perceptrons.listIterator();
	}
	
	/**
	 * Zwraca obiekt dostarczajacy wyniki dzialania tej warstwy
	 * @return obiekt implementujacy IInputProvider
	 */
	public IInputProvider getOutput() {
		return output;
	}
	
	/**
	 * Ustawia wejscie dla wszystkich perceptronow w tej warstwie
	 * @param inputProvider - obiekt implementujacy IInputProvider
	 */
	public void setInput(IInputProvider inputProvider) {
		for (Iterator i = perceptrons(); i.hasNext(); ) {
			Perceptron perceptron = (Perceptron)i.next();
			perceptron.setInput(inputProvider);
		}
	}
	
	/**
	 * Zmienia wejscia dla wszystkich perceptronow w warstwie
	 * 
	 * @param inputProvider - obiekt implementujacy IInputProvider
	 */
	/*public void updateInput(IInputProvider inputProvider) {
		input = inputProvider; 
		for (Iterator i = perceptrons(); i.hasNext(); ) {
			Perceptron perceptron = (Perceptron)i.next();
			perceptron.updateInput(inputProvider);
		}
	}*/
		
	/**
	 * Nakazuje perceptronom przeliczenie swoich wynikow
	 */
	public void count() {
		for (Iterator i = perceptrons(); i.hasNext(); ) {
			((Perceptron)i.next()).count();
		}
	}
	
	/**
	 * Dodanie jednego perceptronu do warstwy i rozszerzenie wyjscia warstwy o nowy element
	 *
	 */
	/*public void grow() {
		Perceptron newP = new Perceptron();
		perceptrons.add(perceptrons.size(), newP); 
		newP.setInput(input);
		output = new PerceptronOutputContainerProvider(perceptrons);
	}*/
	
	/**
	 * Rozszerzenie liczby wag dla kazdego perceptronu oraz podmiana wejscia
	 * dla kazdego perceptronu
	 * 
	 * @param inputProvider - obiekt implementujacy IInputProvider
	 */
	/*public void recombine(IInputProvider inputProvider){
		input = inputProvider;
		for (Iterator i = perceptrons(); i.hasNext(); ) {
			Perceptron perceptron = (Perceptron)i.next();
			perceptron.recombine(inputProvider);
		}	
	}*/
	
	/**
	 * Liczy bledy dla perceptronow z warstwy nizszej, zgodnie z wzorami 
	 * algorytmu back-prop-update
	 */
	public void countDeviations(ListIterator p) {
		int j = 0;
		while(p.hasNext()) {
			Perceptron perceptron = (Perceptron)p.next();
		
			double sum = 0;
			for (Iterator k = perceptrons(); k.hasNext(); ) {
				Perceptron inner = (Perceptron)k.next();
				sum += inner.getDeviation() * inner.getWeight(j);				
			}
			sum *= Global.DIFFERENTIAL.eval(perceptron.getOutput());
			
			perceptron.setDeviation(sum);
			
			j++;
		}
	}
	
	/**
	 * Poprawia wagi perceptronow w tej warstwie (przy zalozeniu, 
	 * ze perceptrony maja juz policzone bledy)
	 */
	public void improveWeights() {
		for (Iterator i = perceptrons(); i.hasNext(); )
			((Perceptron)i.next()).improveWeights();
	}
	
	/**
	 * Serializuje stan warstwy
	 * @return obiekt ktory pamieta stan wszystkich perceptronow w warstwie 
	 */
	public Object storeData() {
		Object[] data = new Object[perceptrons.size()];
		for (int i = 0; i < perceptrons.size(); i++)
			data[i] = (perceptrons.get(i)).storeData();
		return data;
	}
	
	/**
	 * Deserializuje stan warstwy - przywraca wartosci wszystkim perceptronom
	 * @param data - obiekt zawierajacy zserializowany stan warstwy
	 */
	public void restoreData(Object data) {
		for (int i = 0; i < perceptrons.size(); i++)
			(perceptrons.get(i)).restoreData(((Object[]) data)[i]);
	}
	
	
	/**
	 * Zwraca najmniejsza wage krawedzi w warstwie
	 * 
	 * @return najmniejsza waga
	 */
	public double getMinWeight() {
	    Perceptron p;
	    double min, pom;
	    
	    min = 0;
		ListIterator it = perceptrons.listIterator();
		while (it.hasNext()) {
			p = (Perceptron) it.next();
			pom = p.getMinWeight();
			if (pom < min) min = pom;
		}
		return min;
	}
	
	
	/**
	 * Zwraca najwieksza wage krawedzi w warstwie
	 * 
	 * @return najwieksza waga
	 */
	public double getMaxWeight() {
	    Perceptron p;
	    double max, pom;
	    
	    max = 0;
		ListIterator it = perceptrons.listIterator();
		while (it.hasNext()) {
			p = (Perceptron) it.next();
			pom = p.getMaxWeight();
			if (pom > max) max = pom;
		}
		return max;
	}
	
	
	/**
	 * Zwraca najmniesza wartosc z wyjsc perceptronow
	 * 
	 * @return
	 */
	public double getMinOutput() {
	    Perceptron p;
	    double min, pom;
	    
	    min = 0;
		ListIterator it = perceptrons.listIterator();
		while (it.hasNext()) {
			p = (Perceptron) it.next();
			pom = p.getOutput();
			if (pom < min) min = pom;
		}
		return min;		
	}

	
	/**
	 * Zwraca najwieksza wartosc z wyjsc perceptronow
	 * 
	 * @return
	 */
	public double getMaxOutput() {
	    Perceptron p;
	    double max, pom;
	    
	    max = 0;
		ListIterator it = perceptrons.listIterator();
		while (it.hasNext()) {
			p = (Perceptron) it.next();
			pom = p.getOutput();
			if (pom > max) max = pom;
		}
		return max;		
	}
	
	
	/**
	 * Sprawdzenie czy warstwa zawiera perceptron p
	 * @param p perceptron
	 * @return
	 */
	public boolean includesPercetron(Perceptron p) {	
		for (Perceptron o : perceptrons) if (o == p) return true;
		return false;
	}
}

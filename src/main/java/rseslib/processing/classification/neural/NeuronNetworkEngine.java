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


package rseslib.processing.classification.neural;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Report;

/**
 * @author Jakub Sakowicz
 *
 * Silnik sieci neuronowej
 *
 */
public class NeuronNetworkEngine {
	
	/**
	 * Konstruktor
	 * @param fullTable    - cala tabelka z danymi
	 * @param trainData    - czesc treningowa
	 * @param validateData - czesc walidacyjna
	 */
	public NeuronNetworkEngine(DoubleDataTable fullTable, ArrayList trainData, ArrayList validateData) {
	
		this.fullTable = fullTable;
		this.trainData = (ArrayList)trainData.clone(); // kopia, gdyz pozniej randomizowana jest kolejnosc
		this.validateData = validateData;
		this.setup();
	}
	
	/**
	 * Konstruktor
	 * @param fullTable    - cala tabelka z danymi
	 * @param trainData    - czesc treningowa
	 * @param validateData - czesc walidacyjna
	 * @param hidden_no     - liczba ukrytych warstw
	 * @param structure     - lista licznosci kolejnych warstw
	 */
	public NeuronNetworkEngine(DoubleDataTable fullTable, ArrayList trainData, ArrayList validateData, int hidden_no, int[] structure) {
	
		this.fullTable = fullTable;
		this.trainData = (ArrayList)trainData.clone(); // kopia, gdyz pozniej randomizowana jest kolejnosc
		this.validateData = validateData;
		this.setup(hidden_no, structure);
		//raz to jestem (przychodzac z learn) juz z tablica uwzgledniajaca input'a i zakonczenie ale poprawnym hidden_no
		//czy jestem tu a innym razem?
	}	
	
	/**
	 * Warstwa wejsiowa (bez perceptronow, tylko tlumaczenie wejscia)
	 */
	public DoubleDataProvider input;
	/**
	 * Ilosci perceptronow w warstwach
	 */
	public int[] noOfPerceptronsInLayer;
	/**
	 * Warstwy sieci
	 */
	public List<Layer> layers;
	
	/**
	 * Tabela z danymi 
	 */	
	private DoubleDataTable fullTable;
	/**
	 * Czesc treningowa
	 */
	private ArrayList trainData;
	/**
	 * Czesc walidacyjna
	 */
	private ArrayList validateData;
	/**
	 * Iterator po danych trenignowych
	 */
	private Iterator dataIterator;
	
	/**
	 * Skutecznosc tego silnika
	 */
	private double myRatio = 0;
	/**
	 * Parametr ALFA tego silnika
	 */
	private double ALFA = Global.INITIAL_ALFA;
	
	/**
	 * Lista dostepnych wartosci parametru decyzyknego
	 */
	public List availableResults;
	/**
	 * Obiekt przetwarzajacy wyjscia ostatniej warstwy na wartosci parametru decyzyjnego
	 */
	private ResultCombiner resultCombiner;
	
	/**
	 * Pojedyncza faza uczenia sieci. Wykonanie Global.ITER_COUNT algorytmu 
	 * back-prop-update.
	 */
	public void learn() {		
		if (myRatio > Global.DEST_TARGET_RATIO) return; // po co wiecej sie uczyc...
		if (myRatio > 50) ALFA *= Global.MULT_ALFA;
		if (myRatio > 99) ALFA *= Global.MULT_ALFA;
		shuffle();
		Global.CURRENT_ALFA = Math.max(ALFA, Global.MIN_ALFA);
		for (int i = 0; i < Global.ITER_COUNT; i++) {
			backPropUpdate();						
		}
		
	}

	/**
	 * Przelicza i zwraca wynik dla wejscia
	 * @param dd - rekord z danymi
	 * @return przewidywana wartosc decyzyjna
	 */
	public double classify(DoubleData dd) {
		count(dd);
		return resultCombiner.getResult();
	}
	
	/**
	 * Zwraca celnosc sieci na danych walidacyjnych
	 * @return liczba zmiennoprzecinkowa z zakresu 0..100
	 */
	public double targetRatio() {
		int hit = 0;
		for (Iterator i = validateData.iterator(); i.hasNext(); ) {
			DoubleDataWithDecision dd = (DoubleDataWithDecision)i.next();
			if (this.classify(dd) == dd.getDecision())
				hit++;
		}
		myRatio = (double)hit/(double)validateData.size() * 100; 
		return myRatio;
	}
	
	/**
	 * Inicjalizacja sieci
	 *
	 */
	private void setup() {			
		shuffle(); 								// pomieszanie danych treningowych
		noOfPerceptronsInLayer = new int[Global.NO_OF_LAYERS];
		createInputProvider(); 					// utworzenie obiektu odpowiadajacego za wejscie
		createResultCombiner();					// utworzenie obiektu odpowiadajacego za wyjscie
		// ilosci perceptronow w postepie geometrycznym
		int av = availableResults.size();
		int in = input.noOfInputs();	
		//System.out.print("AV: " + av + ", in: " + in + " \n");
		double mult = Math.pow((double)av / (double)in, 1/(double)(Global.NO_OF_LAYERS));
		double current = in * mult;
		for (int i=0; i < Global.NO_OF_LAYERS - 1; i++) {
			noOfPerceptronsInLayer[i] = (int)current;
			current *= mult;
			Report.debugnl("Warstwa " + i + " perceptronow: " + noOfPerceptronsInLayer[i]);
		}
		noOfPerceptronsInLayer[Global.NO_OF_LAYERS - 1] = av; 
		createLayers();
	}
	
	/**
	 * Inicjalizacja sieci
	 * @param hidden_no     - liczba ukrytych warstw
	 * @param structure     - lista licznosci kolejnych warstw
	 */
	private void setup(int hidden_no, int[] structure) {			
		shuffle(); 								// pomieszanie danych treningowych
		noOfPerceptronsInLayer = new int[hidden_no + 1];
		createInputProvider(); 					// utworzenie obiektu odpowiadajacego za wejscie
		createResultCombiner();					// utworzenie obiektu odpowiadajacego za wyjscie
		int av = availableResults.size();
        int i;
		for (i=0; i<hidden_no; i++) {
			noOfPerceptronsInLayer[i] = structure[i+1];	
			}	
		noOfPerceptronsInLayer[hidden_no] = av; 
		createLayers(hidden_no);
	}
	
	/**
	 * Miesza dane treningowe
	 */
	private void shuffle() {
		Collections.shuffle(trainData);
		dataIterator = trainData.iterator();
	}
	
	/**
	 * Tworzy obiekt zarzadzajacy wejsciem
	 */
	private void createInputProvider() {
		input = new DoubleDataProvider(fullTable);
	}
	
	/**
	 * Oblicza ilosc mozliwych rezultatow i tworzy obiekt odpowiadajacy za wyjscie.
	 */
	private void createResultCombiner() {
		Set<Double> availableResultsSet = new HashSet<Double>();
		for (DoubleData dd : fullTable.getDataObjects()) {
			availableResultsSet.add(new Double(((DoubleDataWithDecision)dd).getDecision()));
		}
		availableResults = new ArrayList<Double>(availableResultsSet);
		resultCombiner = new ResultCombiner(availableResults);
	}
	
	/**
	 * Tworzy poszczegolne warstwy zgodnie z wartosciami w tablicy
	 * noOfPerceptronsInLayer. Podlacza wejscia i wyjscia
	 */
	private void createLayers() {
		layers = new ArrayList<Layer>();
		IInputProvider currentLayerInput = input;
		for (int i = 0; i < Global.NO_OF_LAYERS; i++ ) {
			Layer layer = new Layer(noOfPerceptronsInLayer[i]);
			layers.add(layer);
			layer.setInput(currentLayerInput);
			currentLayerInput = layer.getOutput();
		}
		resultCombiner.setInput(currentLayerInput);
	}
	
	/**
	 * Tworzy poszczegolne warstwy zgodnie z wartosciami w tablicy
	 * noOfPerceptronsInLayer. Podlacza wejscia i wyjscia
	 */
	private void createLayers(int hidden_no) {
		layers = new ArrayList<Layer>();
		IInputProvider currentLayerInput = input;
		for (int i = 0; i < (hidden_no + 1); i++ ) {
			Layer layer = new Layer(noOfPerceptronsInLayer[i]);
			layers.add(layer);
			layer.setInput(currentLayerInput);
			currentLayerInput = layer.getOutput();
		}
		resultCombiner.setInput(currentLayerInput);
	}	
		
	/**
	 * Dodanie jednego perceptronu do sieci i poprawienie wejsc, wyjsc 
	 * 
	 * @param extended_no numer warstwy ukrytej
	 */
	/*public void changeNetwork(int extended_no) {
		Layer layer;
		int i;
		
		Iterator it = layers.iterator();
		IInputProvider currentLayerInput = input;
		i = 0;
		while(it.hasNext()) {
			layer = (Layer) it.next();
			if (i == extended_no) {
				layer.grow();  //zwieksz licznosc warstwy o jeden perceptron
				noOfPerceptronsInLayer[i]++;
			}
			if ((i-1) == extended_no) {
				//podmiana wejscia wraz z powiekszeniem tablicy wag dla perceptronow
				layer.recombine(currentLayerInput); 
			}
			layer.updateInput(currentLayerInput);
			currentLayerInput = layer.getOutput();
			i++;		
		}
		resultCombiner.setInput(currentLayerInput);	
	}*/
	
	/**
	 * Zwraca nastepny obiekt danych treningowych
	 * @return DoubleDataWithDecision
	 */
	private DoubleDataWithDecision nextData() {
		if (! dataIterator.hasNext())
			dataIterator = trainData.iterator();
		return (DoubleDataWithDecision)dataIterator.next();
	}
	
	/**
	 * Algorytm propagacji wstecznej. Zmodyfikowany o limitowanie ilosci trenowanych
	 * rekordow z danymi do Global.MAX_ITER_SIZE (dla lepszego zarzadzania czasem dzialania).
	 *
	 */
	private void backPropUpdate() {
		for (int i =0; i < Math.min(Global.MAX_ITER_SIZE, trainData.size()); i++) {
			DoubleDataWithDecision dd = this.nextData();
			
			count(dd); // liczymy wynik
			
			resultCombiner.setExpectedResult(dd.getDecision()); // okreslamy pozadany rezultat
			
			// kazemy obliczyc bledy poszczegolnym warstwom
			IDeviationCounter dc = resultCombiner;
			for (ListIterator l = layers.listIterator(layers.size()); l.hasPrevious(); ) {
				Layer layer = (Layer)l.previous();
				dc.countDeviations(layer.perceptrons());
				dc = layer;
			}
	
			// kazemy poprawic wagi
			for (ListIterator l = layers.listIterator(layers.size()); l.hasPrevious(); ) {				
				((Layer)l.previous()).improveWeights();
			}
		}
	}

	
	/**
	 * Przelicza wynik dla wejscia
	 * @param dd - rekord z danymi
	 */
	private void count(DoubleData dd) {
		input.setDoubleData(dd);		
		
		for (ListIterator l = layers.listIterator(); l.hasNext(); ) {
			((Layer)l.next()).count();
		}

		resultCombiner.count();		
	}
	

	/**
	 * Serializuje stan sieci do obiektu
	 * @return Object
	 */
	public Object storeData() {
		Object[] data = new Object[layers.size()];
		for (int i=0; i < layers.size(); i++)
			data[i] = (layers.get(i)).storeData();
		return data;
	}

	/**
	 * Deserializuje stan sieci z obiektu (do ktorego byla wczesniej zserializowana)
	 * @param data - Object
	 */
	public void restoreData(Object data) {
		for (int i=0; i < layers.size(); i++)
			(layers.get(i)).restoreData(((Object[])data)[i]);
	}
	
}

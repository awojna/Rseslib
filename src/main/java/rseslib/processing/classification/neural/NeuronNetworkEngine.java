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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.system.Report;

/**
 * @author Jakub Sakowicz
 *
 * Silnik sieci neuronowej
 *
 */
public class NeuronNetworkEngine implements Serializable {
	
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Konstruktor
	 * @param attrs        - naglowek danych
	 * @param data         - cala tabelka z danymi
	 * @param trainData    - czesc treningowa
	 * @param validateData - czesc walidacyjna
	 */
	public NeuronNetworkEngine(Header attrs, ArrayList<DoubleData> data, ArrayList trainData, ArrayList validateData, double initAlpha, double targetRatio) {
	
		this.attributes = attrs;
		this.data = data;
		this.trainData = (ArrayList)trainData.clone(); // kopia, gdyz pozniej randomizowana jest kolejnosc
		this.validateData = validateData;
		this.ALFA = initAlpha;
		this.DEST_TARGET_RATIO = targetRatio;
		this.setup();
	}
	
	/**
	 * Konstruktor
	 * @param attrs        - naglowek danych
	 * @param data         - cala tabelka z danymi
	 * @param trainData    - czesc treningowa
	 * @param validateData - czesc walidacyjna
	 * @param hidden_no     - liczba ukrytych warstw
	 * @param structure     - lista licznosci kolejnych warstw
	 */
	public NeuronNetworkEngine(Header attrs, ArrayList<DoubleData> data, ArrayList trainData, ArrayList validateData, int hidden_no, int[] structure, double initAlpha, double targetRatio) {
	
		this.attributes = attrs;
		this.data = data;
		this.trainData = (ArrayList)trainData.clone(); // kopia, gdyz pozniej randomizowana jest kolejnosc
		this.validateData = validateData;
		this.ALFA = initAlpha;
		this.DEST_TARGET_RATIO = targetRatio;
		this.setup(hidden_no, structure);
		//raz to jestem (przychodzac z learn) juz z tablica uwzgledniajaca input'a i zakonczenie ale poprawnym hidden_no
		//czy jestem tu a innym razem?
	}	
	
	/**
	 * Nagłowek danych 
	 */	
	private Header attributes;
	/**
	 * Dane
	 */	
	private ArrayList<DoubleData> data;
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
	 * Parametr ALFA tego silnika
	 */
	private double ALFA;
	/**
	 * Parametr DEST_TARGET_RATIO tego silnika
	 */
	private double DEST_TARGET_RATIO;
	/**
	 * Skutecznosc tego silnika
	 */
	private double myRatio = 0;
	
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
	 * Obiekt przetwarzajacy wyjscia ostatniej warstwy na wartosci parametru decyzyjnego
	 */
	private ResultCombiner resultCombiner;
	
	/**
	 * Pojedyncza faza uczenia sieci. Wykonanie Global.ITER_COUNT algorytmu 
	 * back-prop-update.
	 */
	public void learn() {		
		if (myRatio > DEST_TARGET_RATIO) return; // po co wiecej sie uczyc...
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
	 * Przelicza i zwraca wynik rozkladowy dla wejscia
	 * @param dd - rekord z danymi
	 * @return rozklad decyzji
	 */
	public double[] classifyWithDistributedDecision(DoubleData dd) {
		count(dd);
		return resultCombiner.getDistributedResult();
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
		int av = attributes.nominalDecisionAttribute().noOfValues();
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
		int av = attributes.nominalDecisionAttribute().noOfValues();
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
		input = new DoubleDataProvider(attributes, data);
	}
	
	/**
	 * Oblicza ilosc mozliwych rezultatow i tworzy obiekt odpowiadajacy za wyjscie.
	 */
	private void createResultCombiner() {
		resultCombiner = new ResultCombiner(attributes.nominalDecisionAttribute());
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
	
    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(attributes);
    	out.writeObject(data);
    	out.writeObject(trainData);
    	out.writeObject(validateData);
    	out.writeDouble(ALFA);
    	out.writeDouble(DEST_TARGET_RATIO);
    	out.writeDouble(myRatio);
    	out.writeObject(input);
    	out.writeObject(noOfPerceptronsInLayer);
    	out.writeObject(layers);
    	out.writeObject(resultCombiner);
    }

    /**
     * Reads this object.
     *
     * @param in			Input for reading.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	attributes = (Header)in.readObject();
    	data = (ArrayList<DoubleData>)in.readObject();
    	trainData = (ArrayList)in.readObject();
    	validateData = (ArrayList)in.readObject();
    	ALFA = in.readDouble();
    	DEST_TARGET_RATIO = in.readDouble();
    	myRatio = in.readDouble();
    	input = (DoubleDataProvider)in.readObject();
    	noOfPerceptronsInLayer = (int[])in.readObject();
    	layers = (List<Layer>)in.readObject();
    	resultCombiner = (ResultCombiner)in.readObject();
		dataIterator = trainData.iterator();
    }
}

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
import java.util.Collection;
import java.util.Properties;

import rseslib.processing.classification.AbstractClassifierWithDistributedDecision;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Neural network implementation.
 * 
 * @author Jakub Sakowicz
 */
public class NeuralNetwork extends AbstractClassifierWithDistributedDecision implements Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	protected long timeLimit;
	protected int[] networkStructure;
	/** Poczatkowa wartosc wspolczynnika ALFA dla back-prop-update */
	protected double INITIAL_ALFA;
	/** Wspolczynnik powyzej ktorego siec nie bedzie juz uczona */
	protected double DEST_TARGET_RATIO;
	/** Naglowek */
	protected Header attributes;
	/** Cala tabelka */
	protected ArrayList<DoubleData> data;
	/** dane treningowe */
	protected ArrayList<DoubleData> trainData;
	/** dane walidacyjne */
	protected ArrayList<DoubleData> validateData;
	/** Czas startu uczenia sieci. */
	protected long startTime;
	/** czas rozpoczecia ostatniej rundy */
	protected long lastRound;
	/** najlepszy sposrod powyzszych */
	protected NeuronNetworkEngine bestEngine;
	protected Progress prog;   // obiekt do raportowania
	protected int currentStep; // aktualny krok
	protected boolean leave;    // czy nikt nie nakazal przerwanai dzialania


	/**
	 * Konstruktor
	 * @param prop			- dodatkowe parametry dla klasyfikatora
	 * @param trainTable	- tabelka treningowa
	 * @param prog			- obiekt do raportowania postepu
	 */
	public NeuralNetwork(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
	{
		super(prop, trainTable);
		timeLimit = ((long)this.getIntProperty(Global.TIME_LIMIT_NAME)) * 1000;
		INITIAL_ALFA = this.getDoubleProperty(Global.INITIAL_ALFA_NAME);
		DEST_TARGET_RATIO = this.getDoubleProperty(Global.DEST_TARGET_RATIO_NAME);
		if (getBoolProperty("showTraining"))
			setupProgress(new EmptyProgress());  
		else
			setupProgress(prog);
		// czas - aby moc przerwac uczenie po okreslonym czasie
		startTime = System.currentTimeMillis();
		// podzial tabelki na czesc treningowa i walidacyjna
		Collection<DoubleData>[] split = trainTable.randomSplit(3,1);
		trainData = new ArrayList<DoubleData>(split[0]);
		validateData = new ArrayList<DoubleData>(split[1]);
		attributes = trainTable.attributes();
		this.data = trainTable.getDataObjects();
		bestEngine = new NeuronNetworkEngine(attributes, data, trainData, validateData, INITIAL_ALFA, DEST_TARGET_RATIO);
		
		/*wczytanie sposobu generowania sieci i jej ewentualnej struktury*/
		if (getBoolProperty("automaticNetworkStructure"))
		{
            /*uzycie sieci juz wygenerowanej przez komputer*/
			networkStructure = new int[Global.NO_OF_LAYERS + 1];
			networkStructure[0] = bestEngine.input.noOfInputs();
			for (int i= 1;  i < Global.NO_OF_LAYERS; i++) {
				networkStructure[i] = bestEngine.noOfPerceptronsInLayer[i-1];
			}
			networkStructure[networkStructure.length - 1] = attributes.nominalDecisionAttribute().noOfValues();
		}
		else
		{
			/*wczytanie definicji sieci uzytkownika*/
			String userStructure = this.getProperty("hiddenLayersSize");
			int pozycjapocz = 0;
			int[] tabpom = new int[userStructure.length()];
			int pozycjakon = userStructure.indexOf(";");
			if (pozycjakon == -1) pozycjakon = userStructure.length();
			int i =0;
			while (pozycjapocz < userStructure.length()) {
				tabpom[i] = Integer.valueOf(userStructure.substring(pozycjapocz, pozycjakon));
				i++;
				pozycjapocz = pozycjakon + 1;
				if (pozycjakon != userStructure.length()) {
					if (userStructure.substring(pozycjakon + 1).indexOf(";") != -1) 
						pozycjakon = userStructure.substring(pozycjakon + 1).indexOf(";") + (pozycjakon + 1);
					else pozycjakon = userStructure.length();
				};			
			}
			networkStructure = new int[i+2];
			for (i = 1; i < (networkStructure.length - 1); i++) networkStructure[i] = tabpom[i-1];
			networkStructure[0] = bestEngine.input.noOfInputs();
			networkStructure[networkStructure.length - 1] = attributes.nominalDecisionAttribute().noOfValues();
			bestEngine = new NeuronNetworkEngine(attributes, data, trainData, validateData, networkStructure.length-2, networkStructure, INITIAL_ALFA, DEST_TARGET_RATIO);
		}
		if (getBoolProperty("showTraining")) prog.step();
		else
		{
			reportStep(System.currentTimeMillis() - startTime);
			learn();
		}
	}
	
    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	writeAbstractClassifier(out);
    	out.writeLong(timeLimit);
    	out.writeObject(networkStructure);
    	out.writeDouble(INITIAL_ALFA);
    	out.writeDouble(DEST_TARGET_RATIO);
    	out.writeObject(attributes);
    	out.writeObject(data);
    	out.writeObject(trainData);
    	out.writeObject(validateData);
    	out.writeObject(bestEngine);
    }

    /**
     * Reads this object.
     *
     * @param in			Input for reading.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readAbstractClassifier(in);
    	timeLimit = in.readLong();
    	networkStructure = (int[])in.readObject();
    	INITIAL_ALFA = in.readDouble();
    	DEST_TARGET_RATIO = in.readDouble();
    	attributes = (Header)in.readObject();
    	data = (ArrayList<DoubleData>)in.readObject();
    	trainData = (ArrayList)in.readObject();
    	validateData = (ArrayList)in.readObject();
    	bestEngine = (NeuronNetworkEngine)in.readObject();
		setupProgress(new EmptyProgress());  
    }

	/**
	 * Przygotowuje Progress do raportowania uczenia
	 * @param prog Progrees wykorzystywany do raportowania
	 */
	protected void setupProgress(Progress prog)
	{
		this.prog = prog;
		currentStep = 0;
		leave = false;
		prog.set("Learning the neural network", 100);
	}

	/**
	 * Raportuje postep uczenia.
	 * @param timeElapsed
	 * @return czy nauka powinna sie zakonczyc
	 */
	protected boolean reportStep(long timeElapsed) throws PropertyConfigurationException
	{
		int destStep = (int)(100 * timeElapsed / this.timeLimit);
		for (;currentStep < destStep; currentStep++)
			try {
				prog.step();
			} catch (InterruptedException e) {
				leave = true;
				break; // nalezy przerwac dzialanie
			}
		return leave;
	}

	/**
	 * Uczy siec
	 * @param trainTable	- tabelka z danymi treningowymi
	 */
	protected void learn() throws PropertyConfigurationException
	{

		// wyniki poszczegolnych silnikow na danych walidacyjnych
		double result = 0;
		// najnajlepszy do tej pory uzyskany wynik
		double max_absolute_result = -1;
		double max_result = -1;
		// zserializowany najlepszy silnik
		Object best_perceptrons_weights = null;
		// numer rundy w ktorej osiagnelismy najlepszy wynik
		int best_round = 0;

		for (int i = 1; i<Global.MAX_REPEAT_COUNT; i++) {
			Report.debugnl("Tura " + i);

			// czas rozpoczecia rundy
			lastRound = System.currentTimeMillis();
			// czy jeszcze jakies silniki powinny byc uczone
			boolean shouldLearnMore = false;

			bestEngine.learn();
				// wynik na danych walidacyjnych
			result = bestEngine.targetRatio();

			Report.debugnl("Wynik enginu : " + result);


			// czy jestesmy najnajlepsi
			if (result > max_absolute_result) {
				max_absolute_result = result;
				best_perceptrons_weights = bestEngine.storeData();
			}

			if (result > max_result) {
				best_round = i;
				max_result = result;
			}

			// czy wymagamy uczenia
			if (result < DEST_TARGET_RATIO)
				shouldLearnMore = true;

			// raport postepu
			reportStep(System.currentTimeMillis() - startTime);

			Report.debugnl("Czas dzialania " + (System.currentTimeMillis() - startTime)/1000 + " sek.");

			// usuwanie silnika, ktorego dzialanie nie przynosi juz efektow, startowanie w zamian nowego
			if (i - best_round > Global.GRACE_LEARN_PERIOD) {
				Report.debugnl("Usunieto bezuzyteczny silnik");
				bestEngine = new NeuronNetworkEngine(attributes, data, trainData, validateData, INITIAL_ALFA, DEST_TARGET_RATIO);
				best_round = i;
				max_result = -1;
			}

			// czas trwania ostatniej rundy
			long lastRoundTime = System.currentTimeMillis() - lastRound;

			// jesli konczy sie czas lub nie ma sie czego uczyc to konczymy
			long timeElapsed = System.currentTimeMillis() - startTime;
			if ( timeElapsed + 1.4 * lastRoundTime > this.timeLimit ||
					! shouldLearnMore || reportStep(timeElapsed))
				break;
		}


		// przywrocenie najlepszych wag
		bestEngine.restoreData(best_perceptrons_weights);
		Report.debugnl("Walidacja najlepszego daje wynik " + bestEngine.targetRatio());
		reportStep(this.timeLimit);
	}

    /**
	 * Klasyfikuje podany rekord z rozkladem decyzji
	 * @param dd - DoubleData do sklasyfikowania
     */
    public double[] classifyWithDistributedDecision(DoubleData dd) {
    	return bestEngine.classifyWithDistributedDecision(dd);
    }
    
    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
	}

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

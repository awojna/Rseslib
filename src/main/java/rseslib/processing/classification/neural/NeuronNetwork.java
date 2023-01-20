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
import java.util.Collection;
import java.util.Properties;

import rseslib.processing.classification.Classifier;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Neural network implementation.
 * 
 * @author Jakub Sakowicz
 */
public class NeuronNetwork extends ConfigurationWithStatistics implements Classifier
{
	protected long timeLimit;
	protected int[] networkStructure;
	/** Cala tabelka */
	protected DoubleDataTable trainTable;
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
	public NeuronNetwork(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
	{
		super(prop);
		timeLimit = ((long)this.getIntProperty(Global.TIME_LIMIT_NAME)) * 1000;
		Global.INITIAL_ALFA = this.getDoubleProperty(Global.INITIAL_ALFA_NAME);
		Global.DEST_TARGET_RATIO = this.getDoubleProperty(Global.DEST_TARGET_RATIO_NAME);
		if (getBoolProperty("showTraining"))
		{
			setupProgress(new EmptyProgress());  
			prog.set("Learning the neural network", 1);
		}
		else setupProgress(prog);
		// czas - aby moc przerwac uczenie po okreslonym czasie
		startTime = System.currentTimeMillis();
		// podzial tabelki na czesc treningowa i walidacyjna
		Collection<DoubleData>[] split = trainTable.randomSplit(3,1);
		trainData = new ArrayList<DoubleData>(split[0]);
		validateData = new ArrayList<DoubleData>(split[1]);
		this.trainTable = trainTable;
		bestEngine = new NeuronNetworkEngine(trainTable, trainData, validateData);
		
		/*wczytanie sposobu generowania sieci i jej ewentualnej struktury*/
		if (getBoolProperty("automaticNetworkStructure"))
		{
            /*uzycie sieci juz wygenerowanej przez komputer*/
			networkStructure = new int[Global.NO_OF_LAYERS + 1];
			networkStructure[0] = bestEngine.input.noOfInputs();
			for (int i= 1;  i < Global.NO_OF_LAYERS; i++) {
				networkStructure[i] = bestEngine.noOfPerceptronsInLayer[i-1];
			}
			networkStructure[networkStructure.length - 1] = bestEngine.availableResults.size();
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
			networkStructure[networkStructure.length - 1] = bestEngine.availableResults.size();
			bestEngine = new NeuronNetworkEngine(trainTable, trainData, validateData, networkStructure.length-2, networkStructure);
		}
		if (getBoolProperty("showTraining")) prog.step();
		else
		{
			reportStep(System.currentTimeMillis() - startTime);
			learn();
		}
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
			if (result < Global.DEST_TARGET_RATIO)
				shouldLearnMore = true;

			// raport postepu
			reportStep(System.currentTimeMillis() - startTime);

			Report.debugnl("Czas dzialania " + (System.currentTimeMillis() - startTime)/1000 + " sek.");

			// usuwanie silnika, ktorego dzialanie nie przynosi juz efektow, startowanie w zamian nowego
			if (i - best_round > Global.GRACE_LEARN_PERIOD) {
				Report.debugnl("Usunieto bezuzyteczny silnik");
				bestEngine = new NeuronNetworkEngine(trainTable, trainData, validateData);
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
	 * Klasyfikuje podany rekord
	 * @param dd - DoubleData do sklasyfikowania
	 * @see rseslib.processing.classification.Classifier#classify(rseslib.structure.data.DoubleData)
	 */
	public double classify(DoubleData dd) {
		return bestEngine.classify(dd);
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

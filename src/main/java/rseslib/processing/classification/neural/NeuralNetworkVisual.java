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

import javax.swing.JPanel;
import javax.swing.BoxLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.swing.*;

import rseslib.processing.classification.VisualClassifier;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.Progress;

/**
 * Wizualizacja implementacji sieci neuronowej w bibliotece rseslib. Wersja dwuwatkowa
 * zostala zakomentowana. Jej odzyskanie polega na odkomentowaniu:
 * w definicjach: private Task task
 * w metodach: zmiana w startTrain()
 * w kalsach" odkomentwanie klasy Task
 * @author Damian Wojcik
 * 
 */
public class NeuralNetworkVisual extends NeuralNetwork implements VisualClassifier,
		ActionListener {
	private static final long serialVersionUID = 1L;
	
	public VNNPanel nnView, nnViewOne;
	
	public AddNodesDialog addNodesView;

	public RetrainDialog retrainView;

    private Task task;
    
    private JPanel canvas = null;

    private JPanel canvasOne = null;
    
    /**
	 * Konstruktor
	 * 
	 * @param prop wlasciwosci klasyfikatora
	 * @param trainTable tablelka treningowa
	 * @param prog postep
	 * @throws PropertyConfigurationException
	 */
	public NeuralNetworkVisual(Properties prop, DoubleDataTable trainTable, Progress prog)
		throws PropertyConfigurationException, InterruptedException
	{
		super(prop, trainTable, prog);
		if (getBoolProperty("showTraining")) startTrain();	/* wersja dwuwatkowa*/
	}
	
	public void startTrain() {
		task = new Task();
		task.execute();
	}
	
	/**
	 * Writes this object.
	 *
	 * @param out			Output for writing.
	 * @throws IOException	if an I/O error has occured.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException
	{
	}

	/**
	 * Reads this object.
	 *
	 * @param out			Output for writing.
	 * @throws IOException	if an I/O error has occured.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {	
		if (nnView != null) {
			if (event.getSource() == nnView.TrainButton) {
				/*rozpoczecie trenowania sieci*/
				nnView.showClassification = false;
				if (retrainView == null) {
					retrainView = new RetrainDialog(this);
					retrainView.setLocationRelativeTo(nnView);
				}
				retrainView.setVisible(true);			
				return;
			}
			if (event.getSource() == nnView.AddNodes) {		
				/*wyswietlenie panelu dodania perceptronow*/
				if (addNodesView == null) {
					addNodesView = new AddNodesDialog(this);
					addNodesView.setLocationRelativeTo(nnView);
				}
				addNodesView.setVisible(true);			
			}
		}
	}

	private void init(JPanel canv) {
		nnView = new VNNPanel(networkStructure, bestEngine.layers, attributes, true);
		nnView.TrainButton.addActionListener(this);
		nnView.AddNodes.addActionListener(this);		
		JScrollPane scrollPane = new JScrollPane(nnView);
		canv.add(scrollPane);
		canv.setLayout(new BoxLayout( canv, BoxLayout.Y_AXIS));
	}
	
	private void initOne(JPanel canv) {
		nnViewOne = new VNNPanel(networkStructure, bestEngine.layers, attributes, false);
		JScrollPane scrollPane = new JScrollPane(nnViewOne);
		canv.add(scrollPane);
		canv.setLayout(new BoxLayout( canv, BoxLayout.Y_AXIS));
	}

	public void draw(JPanel canv) {
		if (! canv.equals(canvas)) {
			canvas = canv;
			init(canvas);
			nnView.setNotShowClassification();
			canvas.revalidate();
			canvas.setVisible(true);
			canvas.repaint();
		}
	}

	public void drawClassify(JPanel canv, DoubleData obj) {
		if (! canv.equals(canvasOne)) {
			canvasOne = canv;
			initOne(canvasOne);
			nnViewOne.setShowClassification();
			canvasOne.revalidate();
			canvasOne.setVisible(true);
			canvasOne.repaint();	
		} else {
			nnViewOne.setShowClassification();
			canvasOne.repaint();
		}
	}
	
	public Header attributes() {
		return attributes;
	}

	/**
	 * 
	 * @param row - numer ukrytej warstwy
	 * @param no - liczba neuronow do dodania
	 */
	public void addNodes(int row, int no) {		
		if ((row > 0) && (row < bestEngine.layers.size())) {
			for (int i=0; i<no; i++) {
				//bestEngine.changeNetwork(row-1);
				networkStructure[row]++;
			}
			bestEngine = new NeuronNetworkEngine(attributes, data, trainData,	validateData, networkStructure.length - 2, networkStructure, INITIAL_ALFA, DEST_TARGET_RATIO);
			if (nnView != null) nnView.updateModel(networkStructure, bestEngine.layers);
			if (nnViewOne != null) nnViewOne.updateModel(networkStructure, bestEngine.layers);			
		}
	}
	
	public void setTimeLimit(int limitSeconds) {
		timeLimit = ((long)limitSeconds) * 1000;
	}
	
	/**
	 * Uczenie sieci
	 * 
	 * @param trainTable -
	 *            tabelka z danymi treningowymi
	 */
	protected void learn() throws PropertyConfigurationException {
		// wyniki poszczegolnych silnikow na danych walidacyjnych
		double result = 0;
		// najnajlepszy do tej pory uzyskany wynik
		double max_absolute_result = -1;
		double max_result = -1;
		// zserializowany najlepszy silnik
		Object best_perceptrons_weights = null;
		// numer rundy w ktorej osiagnelismy najlepszy wynik
		int best_round = 0;

		for (int i = 1; i < Global.MAX_REPEAT_COUNT; i++) {
			Report.debugnl("Tura " + i);
			if (nnView!=null)
			{
				nnView.TrainButton.setEnabled(false);
				nnView.AddNodes.setEnabled(false);
			}

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

			Report.debugnl("Czas dzialania "
						+ (System.currentTimeMillis() - startTime) / 1000
						+ " sek.");

			// usuwanie silnika, ktorego dzialanie nie przynosi juz efektow,
			// startowanie w zamian nowego
			if (i - best_round > Global.GRACE_LEARN_PERIOD) {
				Report.debugnl("Usunieto bezuzyteczny silnik");
				bestEngine = new NeuronNetworkEngine(attributes, data, trainData,	validateData, networkStructure.length - 2, networkStructure, INITIAL_ALFA, DEST_TARGET_RATIO);
				if (nnView != null) {
					nnView.updateModel(networkStructure, bestEngine.layers);
				}
				if (nnViewOne != null) {
					nnViewOne.updateModel(networkStructure, bestEngine.layers);
				}
				best_round = i;
				max_result = -1;
			}
			else
			{
				if (nnView != null) nnView.setNotValidShapes();
				if (nnViewOne != null) nnViewOne.setNotValidShapes();
			}

			// czas trwania ostatniej rundy
			long lastRoundTime = System.currentTimeMillis() - lastRound;

			// jesli konczy sie czas lub nie ma sie czego uczyc to konczymy
			long timeElapsed = System.currentTimeMillis() - startTime;
			if (timeElapsed + 1.4 * lastRoundTime > this.timeLimit
					|| !shouldLearnMore || reportStep(timeElapsed))
				break;
		}

		// przywrocenie najlepszych wag
		bestEngine.restoreData(best_perceptrons_weights); 
		if (nnView != null)
		{
			nnView.TrainButton.setEnabled(true);
			nnView.AddNodes.setEnabled(true);
			nnView.setNotValidShapes();
		}
		if (nnViewOne != null) nnViewOne.setNotValidShapes();
		Report.debugnl("Walidacja najlepszego daje wynik "
					+ bestEngine.targetRatio());
		reportStep(this.timeLimit);
		repaint();
	}

	public void repaint() {
		if (canvas != null) {
			canvas.repaint();
		}
		if (canvasOne != null) {
			canvasOne.repaint();
		}
	}

    /* watek dla klasyfikacji */
	class Task extends SwingWorker<Void, Void> {
		
	    public Void doInBackground() {	
	    	startTime = System.currentTimeMillis();	
			try {
				learn();
			} catch (PropertyConfigurationException e) { }
	        return null;
	    }

	    public void done() {
	        ;
	    }
	}
}

/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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


package rseslib.qmak.dataprocess.classifier;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import rseslib.processing.classification.Classifier;
import rseslib.qmak.dataprocess.FileStoreable;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.progress.Progress;

/**
 * Interfejs dla klasy QClassifier
 * 
 * @author Leszek Tur
 *
 */
public interface iQClassifier extends iQProjectElement , FileStoreable  {

	/**
	 * Czy klasyfikator jest wytrenowany
	 */
	public boolean isTrained();

	/**
	 * 
	 * @param testTable zwykla tabelka z Rsesliba
	 * @return 0 ok; -1 blad???
	 */
	public int trainOnTable(DoubleDataTable testTable);
	
	public int trainOnTableWithProgress(DoubleDataTable dataTable,Progress progres);

	/**
	 * 
	 * @param data
	 * @return wynik klasyfikacji
	 */
	public double classify(DoubleData data);
	
	/**
	 * 
	 * @return wlasciwosci klasyfikatora
	 */
	public Properties getProperties();
	
	/**
	 * ustawia wlasciwosi klasyfikatora
	 * @param prop
	 */
	public void setProperties(Properties prop);
	
	/**
	 * Pobranie klasyfikatora biblioteki Rseslib3
	 * @return klasyfikator
	 */
	public Classifier getClassifier();

	/**
	 * Wczytanie klasyfikatora z pliku wpisanego do instancji klasy
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void load() throws IOException, InterruptedException;
	
	/**
	 * Zapisanie klasyfikatora do podanego pliku
	 * @param outputFile plik do zapisania danych
	 * @param prog obiekt implementujacy Progress
	 */
	public void store(File outputFile,  Progress prog) throws IOException, InterruptedException;
	
	
}

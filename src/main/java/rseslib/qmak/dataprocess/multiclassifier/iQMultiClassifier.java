/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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


package rseslib.qmak.dataprocess.multiclassifier;

import java.io.IOException;
import java.util.Map;

import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.processing.classification.TestResult;
import rseslib.qmak.dataprocess.FileStoreable;
import rseslib.qmak.dataprocess.classifier.*;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Interfejs dla klasy QMultiClassifier
 * 
 * @author Leszek Tur
 *
 */
public interface iQMultiClassifier extends iQProjectElement, FileStoreable {

	public void add(iQClassifier klasyfikator) throws PropertyConfigurationException;
	
	public void remove(String name);

	public boolean areTrained();

	public void trainOn(DoubleDataTable tabelka,Progress prog) throws InterruptedException, PropertyConfigurationException;

	public Map<String, TestResult> classify(DoubleDataTable tabelka,Progress prog) throws InterruptedException;

	public void setTrained();
	
	public void setNotTrained();
	
	/**
	 * Odtworzenie stanu klasyfikatora na podstawie danych z pliku w instancji klasy
	 * 
	 * @throws StoringNotImplementedException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	public void load()throws IOException, InterruptedException, ClassNotFoundException ;
}

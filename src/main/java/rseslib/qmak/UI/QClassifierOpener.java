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


package rseslib.qmak.UI;

import java.io.File;

import javax.swing.*;

import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.classifier.*;
import rseslib.qmak.util.Utils;

/**
 * Klasa wspierajaca graficzne otwarcie klasyfikatora na podstawie 
 * pliku wskazanego przez uztkownika
 * 
 * @author Damian Wojcik
 */
public class QClassifierOpener {
	JFileChooser load_chooser;
	QMainFrame owner;
	
	/**
	 * Konstruktor
	 * @param mainFrame okno gaficzne programu
	 */
	public QClassifierOpener(QMainFrame mainFrame) {
		owner = mainFrame;
		load_chooser = new JFileChooser();
		load_chooser.setFileFilter(Utils.getFileFilterQCLD());
	}
	
	/**
	 * Wczytanie klasyfikatora z pliku wybranego przez uzytkownika
	 * 
	 * @return interfejs instancji klasyfikatora
	 */
	public iQClassifier load() {
		int returnVal = load_chooser.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File classifier_fileXML = load_chooser.getSelectedFile();
			iQClassifier loadedClassifier = new QClassifier(classifier_fileXML);
			return loadedClassifier;
		} else
			return null;		
	}
}

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


package rseslib.qmak.UI;

import java.io.File;

import javax.swing.JFileChooser;

import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.results.QMultipleTestResult;
import rseslib.qmak.util.Utils;

public class QMultipleTestResultOpener {
	JFileChooser load_chooser;
	QMainFrame owner;
	
	/**
	 * Konstruktor
	 * @param mainFrame okno gaficzne programu
	 */
	public QMultipleTestResultOpener(QMainFrame mainFrame) {
		owner = mainFrame;
		load_chooser = new JFileChooser();
		load_chooser.setFileFilter(Utils.getFileFilterQMTRD());
	}
	
	/**
	 * Wczytanie klasyfikatora z pliku wybranego przez uzytkownika
	 * 
	 * @return interfejs instancji klasyfikatora
	 */
	public QMultipleTestResult load() {
		int returnVal = load_chooser.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File classifier_fileXML = load_chooser.getSelectedFile();
			QMultipleTestResult loadedMTestResult = new QMultipleTestResult(classifier_fileXML, owner.getProject());
			return loadedMTestResult ;
		} else
			return null;		
	}
}
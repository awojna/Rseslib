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


package rseslib.qmak.UI;

import java.io.File;

import javax.swing.JFileChooser;

import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.results.QTestResult;
import rseslib.qmak.dataprocess.results.*;
import rseslib.qmak.util.Utils;

public class QTestResultOpener {
	JFileChooser load_chooser;
	QMainFrame owner;
	
	/**
	 * Konstruktor
	 * @param mainFrame okno gaficzne programu
	 */
	public QTestResultOpener(QMainFrame mainFrame) {
		owner = mainFrame;
		load_chooser = new JFileChooser();
		load_chooser.setFileFilter(Utils.getFileFilterQTRD());
	}
	
	/**
	 * Wczytanie klasyfikatora z pliku wybranego przez uzytkownika
	 * 
	 * @return interfejs instancji klasyfikatora
	 */
	public QTestResult load() {
		int returnVal = load_chooser.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File classifier_fileXML = load_chooser.getSelectedFile();
			QTestResult loadedTestResult = new QTestResult(classifier_fileXML, owner.getProject());
			return loadedTestResult ;
		} else
			return null;		
	}
}
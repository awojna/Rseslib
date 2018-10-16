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
import javax.swing.JFrame;

import rseslib.qmak.dataprocess.results.QTestResult;
import rseslib.qmak.dataprocess.results.*;
import rseslib.qmak.util.Utils;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

public class QTestResultSaver {

	QTestResult testresult;
	JFileChooser save_chooser;
	JFrame owner;
	
	/**
	 * Konstruktor
	 * @param tr test result
	 * @param mainFrame okno programu
	 */
	public QTestResultSaver(QTestResult tr, JFrame mainFrame) {
		testresult = tr;
		owner = mainFrame;
		save_chooser = new JFileChooser();
		save_chooser.setFileFilter(Utils.getFileFilterQTRD());
	}
	
	/**
	 * Zapisywanie test resulta do pliku wskazanego przez uzytkownika
	 * 
	 */
	public void store() {
		Progress prog = new EmptyProgress();
		
		int returnVal = save_chooser.showSaveDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File tr_fileXML = save_chooser.getSelectedFile();
			if (! Utils.getExtension(tr_fileXML.getName()).equals(QTestResult.description_extension)) {
				// wybrano nazwe pliku do zapisu			
				String absolute_path = tr_fileXML.getAbsolutePath()
						+  "." + QTestResult.description_extension;
				tr_fileXML = new File(absolute_path);
			} 
			try {
				testresult.store(tr_fileXML, prog);			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Zmiana test resulta, ktorego ma dotyczyc okno
	 * @param tr test result
	 */
	public void setTestResult(QTestResult tr) {
		testresult = tr;
	}
	
}

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import rseslib.qmak.dataprocess.classifier.*;
import rseslib.qmak.dataprocess.project.*;
import rseslib.qmak.util.Utils;
import rseslib.system.progress.*;

import javax.swing.*;

import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;

/**
 * Klasa przeprowadzajaca zapisywanie klasyfikatora do pliku
 * 
 * @author Damian Wojcik
 *
 */
public class QClassifierSaver {

	iQClassifier classifier;
	JFileChooser save_chooser;
	JFrame owner;
	
	/**
	 * Konstruktor
	 * @param clas klasyfikator
	 * @param mainFrame okno programu
	 */
	public QClassifierSaver(iQClassifier clas, JFrame mainFrame) {
		classifier = clas;
		owner = mainFrame;
		save_chooser = new JFileChooser();
		save_chooser.setFileFilter(Utils.getFileFilterQCLD());
	}
	
	/**
	 * Zapisywanie klasyfikatora do pliku wskazanego przez uzytkownika
	 * 
	 */
	public void store() {
		Progress prog = new EmptyProgress();
		
		int returnVal = save_chooser.showSaveDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File classifier_fileXML = save_chooser.getSelectedFile();
			if (! Utils.getExtension(classifier_fileXML.getName()).equals(QClassifier.description_extension)) {
				// wybrano nazwe pliku do zapisu			
				String absolute_path = classifier_fileXML.getAbsolutePath()
						+  "." + QClassifier.description_extension;
				classifier_fileXML = new File(absolute_path);
			} 
			try {
				//zapis "store"
				String data_path = classifier_fileXML.getAbsolutePath().substring(0, classifier_fileXML.getAbsolutePath().lastIndexOf(".")) + ".qcl";			
				classifier.setFileName(data_path);
				classifier.store(new File(data_path), prog);
				
				//zapis "XMLstore"
				BufferedWriter bw = new BufferedWriter(new FileWriter(classifier_fileXML));
				bw.append("<?xml version='1.0' encoding='ISO-8859-2'?>");
				bw.newLine();
				if (classifier.isXMLstoreable()) {
					((iQXMLstoreable)classifier).XMLstore(bw);
				}
				bw.newLine();
				bw.close();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Zmiana klasyfikatora, ktorego ma dotyczyc okno
	 * @param clas klasyfikator
	 */
	public void setClassifier(iQClassifier clas) {
		classifier = clas;
	}
	
}

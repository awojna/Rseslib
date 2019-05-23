/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.multiclassifier.iQMultiClassifier;
import rseslib.qmak.dataprocess.multiclassifier.*;
import rseslib.qmak.util.Utils;
import rseslib.system.progress.*;

/**
 * Klasa do obslugi zapisywania multiklasyfikatora do pliku
 * 
 * @author Damian Wojcik
 */
public class QMultiClassifierSaver {

	iQMultiClassifier mclassifier;
	JFileChooser save_chooser;
	JFrame owner;
	
	/**
	 * Konstruktor
	 * @param clas multiklasyfikator
	 * @param mainFrame glowne okno programu
	 */
	public QMultiClassifierSaver(iQMultiClassifier clas, JFrame mainFrame) {
		mclassifier = clas;
		owner = mainFrame;
		save_chooser = new JFileChooser();
		save_chooser.setFileFilter(Utils.getFileFilterQMCD());
	
	}
	
	/**
	 * Zapisanie multiklasyfikatora pamietanego w klasie
	 *
	 */
	public void store() {
		Progress prog = new EmptyProgress();
		
		int returnVal = save_chooser.showSaveDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File mclassifier_fileXML = save_chooser.getSelectedFile();
			if (! Utils.getExtension(mclassifier_fileXML.getName()).equals(Utils.qmcd)) {
				// wybrano nazwe pliku do zapisu			
				String absolute_path = mclassifier_fileXML.getAbsolutePath()
						+  "." + Utils.qmcd;
				mclassifier_fileXML = new File(absolute_path);
			} 
			try {
				//zapis XML multiklasyfikatora z opisem jego skladnikow
				BufferedWriter bw = new BufferedWriter(new FileWriter(mclassifier_fileXML));
				bw.append("<?xml version='1.0' encoding='ISO-8859-2'?>");
				bw.newLine();				
				if (mclassifier.isXMLstoreable()) {
					((QMultiClassifier)mclassifier).setFileName(mclassifier_fileXML.getAbsolutePath());
					((QMultiClassifier)mclassifier).XMLWidestore(bw);
				}				
				bw.newLine();
				bw.close();
				
				//store dla multiklasyfikatora i jego skladnikow do plikow
				String data_path = mclassifier_fileXML.getAbsolutePath().substring(0, mclassifier_fileXML.getAbsolutePath().lastIndexOf(".")) + ".qmc";			
				mclassifier.setFileName(data_path);
				mclassifier.store(new File(data_path), prog);			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Zmiana multiklasyfikatora pamietanego w klasie
	 * @param clas multiklasyfikator do zapisu
	 */
	public void setMultiClassifier(iQMultiClassifier clas) {
		mclassifier = clas;
	}
}

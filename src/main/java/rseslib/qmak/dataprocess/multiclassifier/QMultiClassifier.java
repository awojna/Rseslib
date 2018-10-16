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

import java.awt.Point;

import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.multiclassifier.iQMultiClassifier;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.project.iQProject;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;
import rseslib.processing.classification.ClassifierSet;
import rseslib.processing.classification.CrossValidationTest;
import rseslib.processing.classification.MultipleCrossValidationTest;
import rseslib.processing.classification.MultipleRandomSplitTest;
import rseslib.processing.classification.MultipleTestResult;
import rseslib.processing.classification.TestResult;
import rseslib.qmak.XmlMulticlassifierOpener;
import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.project.*;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Klasa reprezentujaca multiklasyfikatory w projekcie.
 * 
 * @author Leszek Tur
 */
public class QMultiClassifier implements iQMultiClassifier, iQXMLstoreable {

	private ClassifierSet klasyfikatory;
	private boolean wytrenowane;
	private Properties properties;
	private Map<String,iQClassifier> klasyfi;
	private Point p;
	public static String extension = "qmc";
	public static String description_extension = "qmcd";
	

	public QMultiClassifier(Point newPoint, String newName)
			throws PropertyConfigurationException {
		p = newPoint;
		klasyfi = new HashMap<String, iQClassifier>();
		wytrenowane = false;
		properties = new Properties();
		properties.setProperty("name", newName);
		properties.setProperty("file", newName + "." + description_extension);
	}
	
	public QMultiClassifier(String fileName, iQProject qp) {
		klasyfi = new HashMap<String, iQClassifier>();	
		wytrenowane = false;		
		properties = new Properties();
		properties.setProperty("file", fileName);	
		
		XmlMulticlassifierOpener handler = new XmlMulticlassifierOpener(this, (QProject)qp); 
		SAXParserFactory fact = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = fact.newSAXParser();
			saxParser.parse(new File(fileName), handler);
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
	}
	
	public iQClassifier getQClassifier(String nazwa){
		return klasyfi.get(nazwa);
	}
	
	public Set<String> getClassifierNames(){
		return klasyfi.keySet();
	}

	public int size(){
		return klasyfi.size();
	}
	
	public void add(iQClassifier cl){
		klasyfi.put(cl.getName(), cl);
		wytrenowane = false;
	}
	
	public void remove(String name) {
		klasyfi.remove(name);
		wytrenowane = false;
	}
	
	public void addWithNull(String name){
		klasyfi.put(name, null);
		wytrenowane = false;
	}

	
	public boolean areTrained() {
		return wytrenowane;
	}

	public void setTrained() {
		wytrenowane = true;
	}

	public void setNotTrained() {
		wytrenowane = false;
	}

	public Map<String, TestResult> classify(DoubleDataTable tabelka,Progress prog) throws InterruptedException {
		if (wytrenowane) {
			return klasyfikatory.classify(tabelka, prog);
		}
		return null;
	}

	public void trainOn(DoubleDataTable trainTable, Progress prog) throws InterruptedException, PropertyConfigurationException {
		klasyfikatory = new ClassifierSet();
		for (Iterator it = klasyfi.keySet().iterator(); it.hasNext();) {
			String el = (String) it.next();
			QClassifier cla = (QClassifier) klasyfi.get(el);
			klasyfikatory.addClassifier(cla.getName(), cla.getTyp().getClassifierClass(), cla.getProperties());
		}
		klasyfikatory.train(trainTable, prog);
		wytrenowane = true;
	}
	
	private void budujKlasyfikatory() throws PropertyConfigurationException{
		klasyfikatory = new ClassifierSet();
		for (Iterator it = klasyfi.keySet().iterator(); it.hasNext();) {
			String el = (String) it.next();
			QClassifier cla = (QClassifier) klasyfi.get(el);
			klasyfikatory.addClassifier(cla.getName(), cla.getTyp().getClassifierClass(), cla.getProperties());
		}
	}

	public Map<String, MultipleTestResult> doCrossValidationTest(
			Properties prop, DoubleDataTable table, Progress prog)
			throws InterruptedException, PropertyConfigurationException {
		budujKlasyfikatory();
		CrossValidationTest test = new CrossValidationTest(prop, klasyfikatory);
		return test.test(table, prog);
	}

	public Map<String, MultipleTestResult> doMultipleCrossValidationTest(
			Properties prop, DoubleDataTable table, Progress prog)
			throws InterruptedException, PropertyConfigurationException {
		budujKlasyfikatory();
		MultipleCrossValidationTest test = new MultipleCrossValidationTest(
				prop, klasyfikatory);
		return test.test(table, prog);
	}

	public Map<String, MultipleTestResult> doMultipleRandomSplitTest(
			Properties prop, DoubleDataTable table, Progress prog)
			throws InterruptedException, PropertyConfigurationException {
		budujKlasyfikatory();
		MultipleRandomSplitTest test = new MultipleRandomSplitTest(prop,
				klasyfikatory);
		return test.test(table, prog);
	}


	public boolean isClassifier() {
		return false;
	}

	public boolean isMulticlassifier() {
		return true;
	}

	public boolean isTable() {
		return false;
	}

	public boolean isXMLstoreable() {
		return true;
	}

	public boolean isFileStoreable() {
		return true;
	}

	public String getName() {
		return properties.getProperty("name");
	}

	public void setName(String newName) {
		properties.setProperty("name", newName);
	}

	public String getFileName() {
		return properties.getProperty("file");
	}

	public void setFileName(String newFileName) {
		properties.setProperty("file", newFileName);
	}

	public String getCurrentPath() {
		String pom = properties.getProperty("file");
		return pom.substring(0, pom.lastIndexOf(System.getProperty("file.separator")));
	}

	public void XMLstore(BufferedWriter bw) throws IOException {
		bw.append("<MultiClassifier ");
		bw.append(" name=\"" + getName() + "\"");
		bw.append(" filename=\"" + getFileName().substring(getFileName().lastIndexOf(System.getProperty("file.separator"))) + "\"");
		bw.append(" x=\"" + p.x + "\"");
		bw.append(" y=\"" + p.y + "\"");
		bw.append(" >");
		bw.append(" </MultiClassifier>");	
	}

	public void XMLWidestore(BufferedWriter bw) throws IOException {
		String name;
		String fileName;
		
		bw.append("<MultiClassifier ");
		bw.append(" name=\"" + getName() + "\"");
		bw.append(" x=\"" + p.x + "\"");
		bw.append(" y=\"" + p.y + "\"");
		bw.append(" >");
		Integer i = new Integer(0);
		Iterator it = klasyfi.keySet().iterator();
		while (it.hasNext()) {
			name = (String) it.next();
			iQClassifier next = klasyfi.get(name);
			if (next.isXMLstoreable()) {
				fileName = getCurrentPath() + getFileName().substring(getFileName().lastIndexOf(System.getProperty("file.separator")), getFileName().lastIndexOf(".")) + "_" + i.toString() + "." + QClassifier.extension;
				next.setName(name);
				next.setFileName(fileName.toString());
				((iQXMLstoreable) next).XMLstore(bw);
			}
			i++;
		}
		bw.append(" </MultiClassifier>");
	}
	
	public void store(File outputFile, Progress prog)
			throws IOException,
			InterruptedException {
		String name;
		
		File data_file = new File(outputFile.getAbsolutePath().substring(0, outputFile.getAbsolutePath().lastIndexOf(".")) + ".qmc");
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.append("<?xml version='1.0' encoding='ISO-8859-2'?>");
		bw.newLine();
		XMLWidestore(bw);
		bw.flush();
		bw.close();
		Iterator it = klasyfi.keySet().iterator();
		while (it.hasNext()) {
			name = (String) it.next();
			iQClassifier next = klasyfi.get(name);
			next.store(new File(next.getFileName()), prog);
		}
		if (klasyfikatory != null) {
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(data_file));
			output.writeObject(klasyfikatory);
			output.close();
		}
	}

	public String getBaseFileExtension() {
		return QMultiClassifier.extension;
	}
	
	/**
	 * Wczytanie obiektow klasyfikatorow skladowych zapisanych w pliku zwiazanym z instancja multiklasyfikatora
	 */
	public void load() throws IOException, InterruptedException, ClassNotFoundException {
		//fileName to plik typu .qmcd
		//czytanie pliku zapisujacego zmienna klasyfikatory
		File data_file = new File(getFileName().substring(0, getFileName().lastIndexOf(".")) + "." + extension);
		if (data_file.exists()) {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(data_file));
			klasyfikatory = (ClassifierSet)input.readObject();
			input.close();
		}	
	}

	public void setPosition(Point newP) {
		p = newP;
	}

	public Point getPosition() {
		return p;
	}

	public boolean isMultipleTestResult() {
		return false;
	}

	public boolean isTestResult() {
		return false;
	}
	
	public String getDescriptionFileExtension() {
		return description_extension;
	}
}

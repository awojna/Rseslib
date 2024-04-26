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


package rseslib.qmak.dataprocess.results;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import rseslib.processing.classification.TestResult;
import rseslib.qmak.XmlTestResultOpener;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.FileStoreable;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.project.iQProject;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.system.Report;
import rseslib.system.progress.Progress;

/**
 * Class implementing classification result of multiclassifier
 * and simple classifier. 
 * 
 * @author Leszek Tur & Maciej Zuchniak
 *
 */
public class QTestResult implements iQProjectElement, iQXMLstoreable, FileStoreable {
	
	private Map<String,TestResult> wyniki = null;
	private Properties wlasciwosci;
	private Point p;
	
	private boolean isFromMultiClassifier = false;

	private static NominalAttribute decAttr = null;
	
	public static String extension = "qtr";
	
	public static String description_extension = "qtrd";

	public QTestResult(TestResult tresult, String name, String clN) {
		wyniki = new HashMap<String,TestResult>();		
		wyniki.put(clN, tresult); //TODO: zrobic tak zeby to mialo znaczenie jaki to klasyfikator
		decAttr = tresult.decisionAttribute();
		isFromMultiClassifier = false;
		wlasciwosci = new Properties();
		wlasciwosci.setProperty("name", name);
	}
	

	public QTestResult(Map<String, TestResult> wyniki, String name) {
		super();
		this.wyniki = wyniki;
		isFromMultiClassifier = true;
		wlasciwosci = new Properties();
		wlasciwosci.setProperty("name", name);
	}
	
	public QTestResult(File XMLFile, iQProject qp) {
		wyniki = new HashMap<String,TestResult>();		
		wlasciwosci = new Properties();
		wlasciwosci.setProperty("filename", XMLFile.getAbsolutePath());	
		
		XmlTestResultOpener handler = new XmlTestResultOpener(this, (QProject)qp); 
		SAXParserFactory fact = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = fact.newSAXParser();
			saxParser.parse(XMLFile, handler);
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		if (! isFromMultiClassifier) {
				decAttr = wyniki.values().iterator().next().decisionAttribute();
		}
	}
	
	// Mowi ile jest atrybutow decyzyjnych
	public int noOfDecs() {
		return decAttr.noOfValues();
	}
	
	public int howManyObjects(String nazwa, int iloc, int jloc) {
		double iglob = decAttr.globalValueCode(iloc);
		double jglob = decAttr.globalValueCode(jloc);
		return wyniki.get(nazwa).getNoOfObjects(iglob, jglob);
	}

	public String getNameOfFirst() {
		Object[] namesArray = wyniki.keySet().toArray();
		return (String)	namesArray[0];
	}
	
//	public int howManyObjects(int iloc, int jloc) {
//		double iglob = decAttr.globalValueCode(iloc);
//		double jglob = decAttr.globalValueCode(jloc);
//		TestResult tr = (TestResult) wyniki.values().toArray()[0];
//		return wyniki.getNoOfObjects(iglob, jglob);
//	}
//	
	public static String decisionName(int loc) {
		double glob = decAttr.globalValueCode(loc);
		return NominalAttribute.stringValue(glob);
	}
	
	public double getAccuracy(String name) {
		return wyniki.get(name).getAccuracy();
	}
	
	public double getDecAccuracy(String name, int no) {
		double dec = decAttr.globalValueCode(no);
		return wyniki.get(name).getDecAccuracy(dec);
	}
	
//	public double getAccuracyInPercents(String name) {
//		double accuracy = getAccuracy(name);
//		return (int)(10000*accuracy-100*(int)(100*accuracy));
//	}
//	
	public int getClassificationAccuracy(String name) {
		int counter = 0;
		for (int i = 0; i<noOfDecs(); i++) {
			counter += howManyObjects(name, i, i);
		}
		return counter;
	}
	
	public void showResults(){
		StringBuffer buf = new StringBuffer();
        for (Object par : wyniki.entrySet())
        {
            buf.append(((Map.Entry)par).getKey());
            buf.append(Report.lineSeparator);
            buf.append(((Map.Entry)par).getValue());
            buf.append(Report.lineSeparator);
        }
		JOptionPane.showMessageDialog(QMainFrame.getMainFrame(), buf.toString(), getName(), JOptionPane.PLAIN_MESSAGE);
	}

	
	public void printResult() {
		System.out.println(wyniki.toString());
	}
	
	public void showStatistics(){
		String text = new String();
		for (Iterator it = wyniki.keySet().iterator(); it.hasNext();) {
			String el = (String) it.next();
			String propText = new String();
			Properties prop = wyniki.get(el).getStatistics();
			for (Iterator it2 = prop.keySet().iterator(); it2.hasNext();) {
				String el2 = (String) it2.next();
				propText = propText + el2 + " - " +prop.getProperty(el2) +"\n"; 
			}
			text = text + el+":\n"+propText+"\n";
		}
/*		QSimpleResults okno = new QSimpleResults(QMainFrame.getMainFrame(),text);
		okno.setLocationRelativeTo(QMainFrame.getMainFrame());
		okno.pack();
		okno.setVisible(true);
*/
	}

	public void setFromMulticlassifier(boolean b) {
		isFromMultiClassifier = b;
	}
	
	public boolean isFromMultiClassifier() {
		return isFromMultiClassifier;
	}
	

	public Point getPosition() {
		return p;
	}

	public boolean isClassifier() {
		return false;
	}

	public boolean isFileStoreable() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isMulticlassifier() {
		return false;
	}

	public boolean isMultipleTestResult() {
		return false;
	}

	public boolean isTable() {
		return false;
	}

	public boolean isTestResult() {
		return true;
	}

	public boolean isXMLstoreable() {
		return true;
	}

	public String getBaseFileExtension() {
		return extension;
	}

	public String getFileName() {
		return wlasciwosci.getProperty("filename");
	}

	public void setFileName(String fileName) {
		wlasciwosci.setProperty("filename", fileName);	
		
	}
	
	public String getName() {
		return wlasciwosci.getProperty("name");
	}

	public void setName(String name) {
		wlasciwosci.setProperty("name", name);		
	}

	public void setPosition(Point p) {
		this.p = p;	
	}
	
	public void addResult(String resultName, TestResult result) {
		wyniki.put(resultName, result);
	}
	
	public String getCurrentPath() {
		String pom = wlasciwosci.getProperty("filename");
		return pom.substring(0, pom.lastIndexOf(System.getProperty("file.separator")));
	}
	
	public void XMLstore(BufferedWriter bw) throws IOException {
		bw.append("<QTestResult ");
		bw.append(" name=\"" + getName() + "\"");
		bw.append(" filename=\"" + getFileName().substring(getFileName().lastIndexOf(System.getProperty("file.separator"))) + "\"");
		bw.append(" x=\"" + p.x + "\"");
		bw.append(" y=\"" + p.y + "\"");
		bw.append(" >");
		bw.append(" </QTestResult >");	
	}
	
	public void XMLWidestore(BufferedWriter bw) throws IOException {
		String name;
		String fileName;
		
		bw.append("<QTestResult ");
		bw.append(" name=\"" + getName() + "\"");
		bw.append(" x=\"" + p.x + "\"");
		bw.append(" y=\"" + p.y + "\"");
		if (isFromMultiClassifier) bw.append(" fm=\"" + "true"  + "\"");
			else bw.append(" fm=\"" + "false"  + "\"");	
		bw.append(" >");
		Iterator it = wyniki.keySet().iterator();
		while (it.hasNext()) {
			name = (String) it.next();
			fileName = name + "." + getBaseFileExtension();
			bw.append("<Result ");
			bw.append(" name=\"" + name + "\"");	
			bw.append(" filename=\"" + fileName + "\"");
			bw.append(" >");
			bw.append("</Result>");		
			}
		bw.append(" </QTestResult>");
	}
	
	
	public void store(File outputFile, Progress prog) throws IOException, InterruptedException {
		prog.set("", 1);
		String name;
		//output file to plik typu qtrd
		String baseExt = outputFile.getAbsolutePath().substring(0, outputFile.getAbsolutePath().lastIndexOf(System.getProperty("file.separator")));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.append("<?xml version='1.0' encoding='ISO-8859-2'?>");
		bw.newLine();
		XMLWidestore(bw);
		bw.flush();
		bw.close();
		Iterator it = wyniki.keySet().iterator();
		while (it.hasNext()) {
			name = (String) it.next();
			TestResult next = (TestResult) wyniki.get(name);
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(baseExt +System.getProperty("file.separator") + name + "." + getBaseFileExtension()));
			output.writeObject(next);
			output.close();
		}
		prog.step();
	}
	
	
	public String getDescriptionFileExtension() {
		return description_extension;
	}
}

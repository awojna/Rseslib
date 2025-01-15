/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import rseslib.qmak.dataprocess.results.QMultipleTestResult;
import rseslib.processing.classification.MultipleTestResult;
import rseslib.qmak.XmlMultipleTestResultOpener;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.UI.QSimpleResults;
import rseslib.qmak.dataprocess.FileStoreable;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.project.iQProject;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;
import rseslib.system.progress.Progress;

/**
 * Class implementing multiple test result of multiclassifier
 * 
 * @author Leszek Tur & Maciej Zuchniak
 *
 */
public class QMultipleTestResult implements iQProjectElement, iQXMLstoreable, FileStoreable {
	
	public static String extension = "qmtr";
	
	public static String description_extension = "qmtrd";
	
	private Map<String, MultipleTestResult> wyniki = null;
	
	private String rodzaj;
	
	private Properties wlasciwosci;
	private Point p;
		
	public QMultipleTestResult(Map<String, MultipleTestResult> wyniki, String rodzaj, String name) {
		super();
		this.wyniki = wyniki;
		this.rodzaj = rodzaj;
		wlasciwosci = new Properties();
		wlasciwosci.setProperty("name", name);
		wlasciwosci.setProperty("filename", name);		
	}
	
	public QMultipleTestResult(File XMLFile, iQProject qp) {
		wyniki = new HashMap<String,MultipleTestResult>();		
		wlasciwosci = new Properties();
		wlasciwosci.setProperty("filename", XMLFile.getAbsolutePath());	
		
		XmlMultipleTestResultOpener handler = new XmlMultipleTestResultOpener(this, (QProject)qp); 
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
	}
	
	public Object[][] mapToArray(Map<String, MultipleTestResult> wyniki) {
		boolean imbalanced = false;
		for (Map.Entry<String, MultipleTestResult> wyn : wyniki.entrySet())
			if (wyn.getValue().successfulRun()) {
				imbalanced = wyn.getValue().hasMeasuresForImbalanced();
				break;
			}
		Object[][] data = new Object[wyniki.size()][imbalanced ? 6 : 3];
		
		int j = 0;
		for (Map.Entry<String, MultipleTestResult> wyn : wyniki.entrySet()) {
			data[j][0] = wyn.getKey();
			if (wyn.getValue().successfulRun())
			{
				data[j][1] = Math.round(wyn.getValue().getAvgAccuracy() * 10000) / 10000.0;
				data[j][2] = Math.round(wyn.getValue().getAccuracyStandardDeviation() * 10000) / 10000.0;
				if (imbalanced) {
					data[j][3] = Math.round(wyn.getValue().getAvgFmeasure() * 10000) / 10000.0;
					data[j][4] = Math.round(wyn.getValue().getAvgGmean() * 10000) / 10000.0;
					data[j][5] = Math.round(wyn.getValue().getAvgSensitivity() * 10000) / 10000.0;
				}
			} else {
				data[j][1] = "N/A";
				data[j][2] = "N/A";
				if (imbalanced)
				{
					data[j][3] = "N/A";
					data[j][4] = "N/A";
					data[j][5] = "N/A";
				}
			}
			
			j++;
		}
		
		return data;
	}
	
	public QSimpleResults createView() {
		return new QSimpleResults(QMainFrame.getMainFrame(), rodzaj, getName(), mapToArray(wyniki));
//		okno.setLocationRelativeTo(QMainFrame.getMainFrame());
//		okno.pack();
//		okno.setVisible(true);
	}
	
	public String getFileName() {
		return wlasciwosci.getProperty("filename");
	}

	public String getName() {
		return wlasciwosci.getProperty("name");
	}

	public Point getPosition() {
		// TODO Auto-generated method stub
		return p;
	}

	public boolean isMultipleTestResult() {
		return true;
	}
	
	public boolean isClassifier() {
		return false;
	}

	public boolean isFileStoreable() {
		return true;
	}

	public boolean isMulticlassifier() {
		return false;
	}

	public boolean isTable() {
		return false;
	}

	public boolean isXMLstoreable() {
		
		return true;
	}

	public String getBaseFileExtension() {
		return QMultipleTestResult.extension;
	}
	
	public void setFileName(String fileName) {
		wlasciwosci.setProperty("filename", fileName);
	}

	public void setName(String name) {
		wlasciwosci.setProperty("name", name);
	}

	public void setPosition(Point p) {
		this.p = p;	
	}

	public void setType(String rodzaj) {
		this.rodzaj = rodzaj;
	}
	
	public boolean isTestResult() {
		return false;
	}

	//TODO do wywalenia - nie uzywac
	public Map<String, MultipleTestResult> getWyniki() {
		return wyniki;
	}
	
	public String getCurrentPath() {
		String pom = wlasciwosci.getProperty("filename");
		return pom.substring(0, pom.lastIndexOf(System.getProperty("file.separator")));
	}
	
	public void XMLstore(BufferedWriter bw) throws IOException {
		bw.append("<QMultipleTestResult ");
		bw.append(" name=\"" + getName() + "\"");
		bw.append(" filename=\"" + getFileName().substring(getFileName().lastIndexOf(System.getProperty("file.separator"))) + "\"");
		bw.append(" x=\"" + p.x + "\"");
		bw.append(" y=\"" + p.y + "\"");
		bw.append(" >");
		bw.append(" </QMultipleTestResult >");	
	}
	
	public void XMLWidestore(BufferedWriter bw) throws IOException {
		String name;
		String fileName;
		
		bw.append("<QMultipleTestResult ");
		bw.append(" name=\"" + getName() + "\"");
		bw.append(" x=\"" + p.x + "\"");
		bw.append(" y=\"" + p.y + "\"");
		bw.append(" type=\"" + rodzaj+ "\"");
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
		bw.append(" </QMultipleTestResult>");
	}
	
	
	public void store(File outputFile, Progress prog) throws IOException, InterruptedException {
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
			MultipleTestResult next = (MultipleTestResult) wyniki.get(name);
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(baseExt +System.getProperty("file.separator") + name + "." + getBaseFileExtension()));
			output.writeObject(next);
			output.close();
		}
	}
	
	public void addResult(String resultName, MultipleTestResult result) {
		wyniki.put(resultName, result);
	}
	
	
	public String getDescriptionFileExtension() {
		return description_extension;
	}
	
}

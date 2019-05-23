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


package rseslib.qmak;

import java.awt.Point;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.QClassifierType;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.project.QProjectProperties;
import rseslib.qmak.dataprocess.project.iQProject;
import rseslib.qmak.dataprocess.results.QMultipleTestResult;
import rseslib.qmak.dataprocess.results.QTestResult;
import rseslib.qmak.dataprocess.table.QDataTable;
import rseslib.qmak.dataprocess.table.iQDataTable;

/**
 * Klasa wczytywania parserem danych xml projektu zapisanego w pliku
 * @author Trickster's authors and Damian Wojcik
 * 
 */
public class XmlOpener extends DefaultHandler {
	private iQProject project;

	private boolean inClassifier;

	private boolean inMultiClassifier;

	private boolean inTable;

	private boolean inProject;
	
	private boolean inTestResult;
	
	private boolean inMultipleTestResult;

	private iQClassifier classifier = null;

	private iQDataTable datatable = null;

	private QMultiClassifier multiClassifier = null;
	
	private QTestResult testResult = null;
	
	private QMultipleTestResult mTestResult = null;

	private QProjectProperties prop = new QProjectProperties();

	public XmlOpener(iQProject newProject) {
		super();
		project = newProject;
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {
		/**
		 * retrieving general informations about the project
		 * 
		 */

		if (qName == "Project") {
			if (inTable || inProject || inClassifier || inMultiClassifier || inTestResult || inMultipleTestResult) {
				throw new SAXException("Inproject");
			}

			inProject = true;
			if (attrs.getLength() != 5) {
				throw new SAXException("Bad argument number");
			}
			String author = attrs.getValue("author");
			String name = attrs.getValue("name");
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG,
					Locale.ENGLISH);

			Date creation_date = null;
			Date modification_date = null;
			try {
				creation_date = df.parse(attrs.getValue("creationdate"));
			} catch (ParseException e) {
				throw new SAXException("Bad creation date format: "
						+ attrs.getValue("creationdate"));
			}
			try {
				modification_date = df.parse(attrs.getValue("moddate"));
			} catch (ParseException e) {
				throw new SAXException("Bad modification date format: "
						+ attrs.getValue("moddate"));
			}

			project.getProperties().setAuthor(author);
			project.getProperties().setName(name);	
			project.getProperties().setCreationDate(creation_date);
			project.getProperties().setModificationDate(modification_date);
			project.getProperties().setAccessDate(new Date(System.currentTimeMillis()));
			return;
		}

		if (qName == "DataTable") {
			if (inTable || (!inProject) || inClassifier || inMultiClassifier|| inTestResult || inMultipleTestResult) {
				throw new SAXException("Inproject");
			}
			if (attrs.getLength() != 4) {
				throw new SAXException("Bad Table");
			}

			inTable = true;
			String name = attrs.getValue("name");
			String file = attrs.getValue("filename");
			String x = attrs.getValue("x");
			String y = attrs.getValue("y");
			datatable = new QDataTable(name, (project.getProperties().getCurrentPath() + file), new Point(Integer.valueOf(x), Integer.valueOf(y)));
			try {
			datatable.load(); // To jest wczytanie sie tabelki z pliku podanego przy konstruktorze	
			} catch (Exception ex) {
				throw new SAXException(ex);
			}
			return;
		}

		if (qName == "Classifier") {
			if (inTable || (!inProject) || inClassifier || inMultiClassifier|| inTestResult || inMultipleTestResult) {
				throw new SAXException("Inproject");
			}
			if (attrs.getLength() != 5) {
				throw new SAXException("Bad Classifier");
			}

			inClassifier = true;
			String name = attrs.getValue("name");
			String class_path = attrs.getValue("classpath");
			String file = attrs.getValue("filename");
			String x = attrs.getValue("x");
			String y = attrs.getValue("y");
			QClassifierType c1 = new QClassifierType(name, class_path);
			classifier = new QClassifier(c1, name);
			classifier.setPosition(new Point(Integer.valueOf(x), Integer.valueOf(y)));
			classifier.setFileName(project.getProperties().getCurrentPath() + file);
			try {
				classifier.load();
			} catch (Exception ex) {
				throw new SAXException(ex);
			}
		}

		if (qName == "MultiClassifier") {
			if (inTable || (!inProject) || inClassifier || inMultiClassifier|| inTestResult || inMultipleTestResult) {
				throw new SAXException("Inproject");
			}
			if (attrs.getLength() != 4) {
				throw new SAXException("MultiClassifier");
			}

			inMultiClassifier = true;
			String name = attrs.getValue("name");
			String file = attrs.getValue("filename");
			file = project.getProperties().getCurrentPath() + file;
			int x = Integer.valueOf(attrs.getValue("x"));
			int y = Integer.valueOf(attrs.getValue("y"));
			try {
				multiClassifier = new QMultiClassifier(file, project);
				multiClassifier.setPosition(new Point(x, y));
				multiClassifier.setName(name);
				multiClassifier.setNotTrained();	
				multiClassifier.load(); 		
			} catch (Exception ex) {
				throw new SAXException(ex);
			}
		}
		
		if (qName == "QTestResult") {
			if (inTable || (!inProject) || inClassifier || inMultiClassifier|| inTestResult || inMultipleTestResult) {
				throw new SAXException("Inproject");
			}
			if (attrs.getLength() != 4) {
				throw new SAXException("QTestResult");
			}
			
			inTestResult = true;
			String name = attrs.getValue("name");
			String file = attrs.getValue("filename");
			file = project.getProperties().getCurrentPath() + file;
			int x = Integer.valueOf(attrs.getValue("x"));
			int y = Integer.valueOf(attrs.getValue("y"));
			try {
				testResult = new QTestResult(new File(file), project);
				testResult.setPosition(new Point(x, y));
				testResult.setName(name);			
			} catch (Exception ex) {
				throw new SAXException(ex);
			}	
		}
		
		if (qName == "QMultipleTestResult") {
			if (inTable || (!inProject) || inClassifier || inMultiClassifier|| inTestResult || inMultipleTestResult) {
				throw new SAXException("Inproject");
			}
			if (attrs.getLength() != 4) {
				throw new SAXException("QMultipleTestResult");
			}
			
			inMultipleTestResult = true;
			String name = attrs.getValue("name");
			String file = attrs.getValue("filename");
			file = project.getProperties().getCurrentPath() + file;
			int x = Integer.valueOf(attrs.getValue("x"));
			int y = Integer.valueOf(attrs.getValue("y"));
			try {
				mTestResult = new QMultipleTestResult(new File(file), project);
				mTestResult.setPosition(new Point(x, y));
				mTestResult.setName(name);
			} catch (Exception ex) {
				throw new SAXException(ex);
			}	
		}
	}

	public void endElement(String c, String b, String a)  throws SAXException {
			if (a == "Project") {
				inProject = false;
			}
			if (a == "Classifier") {
				inClassifier = false;
				if (classifier == null)
					throw new SAXException("No classifier");
				project.insertElement(classifier);
			}
			if (a == "DataTable") {
				inTable = false;
				if (datatable == null)
					throw new SAXException("No table");
				project.insertElement(datatable);
			}
			if (a == "MultiClassifier") {
				inMultiClassifier = false;
				if (multiClassifier == null)
					throw new SAXException("No multiClassifier");
				project.insertElement(multiClassifier);
			}
			if (a == "QTestResult") {
				inTestResult = false;
				if (testResult == null)
					throw new SAXException("No QTestResult");
				project.insertElement(testResult);
			}
			if (a == "QMultipleTestResult") {
				inMultipleTestResult = false;
				if (mTestResult == null)
					throw new SAXException("No QMultipleTestResult");
				project.insertElement(mTestResult);
			}			
	}

	public void characters(char[] chars, int off, int length) {
		if (inProject || (!inClassifier) || (!inTable) || (!inMultiClassifier) || (!inTestResult) || (!inMultipleTestResult)) {
			String nap = new String(chars, off, length);
			prop.setDescription(nap);
		}
	}
}

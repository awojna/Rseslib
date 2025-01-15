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


package rseslib.qmak;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import rseslib.processing.classification.TestResult;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.results.QTestResult;

/**
 * Klasa wczytywania parserem danych xml zbioru testow
 * 
 * @author Trickster's authors and Damian Wojcik
 *
 */
public class XmlTestResultOpener extends DefaultHandler {

	private boolean inTestResult;
	
	private boolean inResult;

	private QTestResult testResult = null;
	
	private TestResult result = null;
	
	private QProject qProject;

	public XmlTestResultOpener(QTestResult newQTestResult, QProject qp) {
		super();
		testResult = newQTestResult;
		qProject = qp;
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {
		
		if (qName == "QTestResult") {
			if (inResult || inTestResult) {
				throw new SAXException("Inproject\n");
			}
			if (attrs.getLength() != 4) {
				throw new SAXException("TestResult\n");
			}
			inTestResult = true;
			
			String name = attrs.getValue("name");
			int x = Integer.valueOf(attrs.getValue("x"));
			int y = Integer.valueOf(attrs.getValue("y"));
			testResult.setName(name);
			testResult.setPosition(new Point(Integer.valueOf(x), Integer.valueOf(y)));
			String ifm = attrs.getValue("fm");
			if (ifm.equals("false")) testResult.setFromMulticlassifier(false);
			else testResult.setFromMulticlassifier(true);		
			qProject.registerName(name);
		}
		
		if (qName == "Result") {
			if (inResult || (! inTestResult)) {
				throw new SAXException("inResult\n");
			}
			if (attrs.getLength() != 2) {
				throw new SAXException("Bad Result\n");
			}

			inResult = true;
			String name = attrs.getValue("name");
			String file = attrs.getValue("filename");
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(testResult.getCurrentPath() + System.getProperty("file.separator") + file));
				result = (TestResult)input.readObject();
				input.close();
			}
			catch (Exception ex) {
				throw new SAXException("Wrong result\n");
			}
			testResult.addResult(name, result);		
		}

	}

	public void endElement(String c, String b, String a) throws SAXException {
		if (a == "QTestResult") {
			inTestResult = false;
		}
		if (a == "Result") {
			inResult = false;
			if (result == null)
				throw new SAXException("No result\n");			
		}
	}
}

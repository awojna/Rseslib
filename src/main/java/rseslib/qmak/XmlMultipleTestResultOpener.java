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

import rseslib.processing.classification.MultipleTestResult;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.results.QMultipleTestResult;

public class XmlMultipleTestResultOpener extends DefaultHandler {

	private boolean inMTestResult;
	
	private boolean inResult;

	private QMultipleTestResult mTestResult = null;
	
	private MultipleTestResult result = null;
	
	private QProject qProject;

	public XmlMultipleTestResultOpener(QMultipleTestResult newMTestResult, QProject qp) {
		super();
		mTestResult = newMTestResult;
		qProject = qp;
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {
		
		if (qName == "QMultipleTestResult") {
			if (inResult || inMTestResult) {
				throw new SAXException("inResult or inMTestResult\n");
			}
			if (attrs.getLength() != 4) {
				throw new SAXException("MultipleTestResult\n");
			}
			inMTestResult = true;
			
			String name = attrs.getValue("name");
			int x = Integer.valueOf(attrs.getValue("x"));
			int y = Integer.valueOf(attrs.getValue("y"));
			mTestResult.setName(name);
			mTestResult.setPosition(new Point(Integer.valueOf(x), Integer.valueOf(y)));
			String rodzaj = attrs.getValue("type");	
			mTestResult.setType(rodzaj);
			qProject.registerName(name);
		}
		
		if (qName == "Result") {
			if (inResult || (! inMTestResult)) {
				throw new SAXException("inResult\n");
			}
			if (attrs.getLength() != 2) {
				throw new SAXException("Bad Result\n");
			}

			inResult = true;
			String name = attrs.getValue("name");
			String file = attrs.getValue("filename");
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(mTestResult.getCurrentPath() + System.getProperty("file.separator") + file));
				result = (MultipleTestResult)input.readObject();
				input.close();
			}
			catch (Exception ex) {
				throw new SAXException("Wrong result\n");
			}
			mTestResult.addResult(name, result);		
		}

	}

	public void endElement(String c, String b, String a) throws SAXException {
		if (a == "QTestResult") {
			inMTestResult = false;
		}
		if (a == "Result") {
			inResult = false;
			if (result == null)
				throw new SAXException("No result\n");			
		}
	}
}
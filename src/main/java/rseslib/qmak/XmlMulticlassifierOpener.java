/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.QClassifierType;
import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.project.QProject;

/**
 * Klasa wczytywania parserem danych xml multiklasyfikatora z pliku
 * @author Trickster's authors and Damian Wojcik
 *
 */
public class XmlMulticlassifierOpener extends DefaultHandler {

	private boolean inMultiClassifier;
	
	private boolean inClassifier;

	private QMultiClassifier multiClassifier = null;
	
	private QClassifier classifier = null;
	
	private QProject qProject;

	public XmlMulticlassifierOpener(QMultiClassifier newMultiCl, QProject qp) {
		super();
		multiClassifier = newMultiCl;
		qProject = qp;
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {

		if (qName == "MultiClassifier") {
			if (inClassifier || inMultiClassifier) {
				throw new SAXException("Inproject");
			}
			if (attrs.getLength() != 3) {
				throw new SAXException("MultiClassifier");
			}
			inMultiClassifier = true;
			
			String name = attrs.getValue("name");
			int x = Integer.valueOf(attrs.getValue("x"));
			int y = Integer.valueOf(attrs.getValue("y"));
			multiClassifier.setName(name);
			multiClassifier.setPosition(new Point(Integer.valueOf(x), Integer.valueOf(y)));
			multiClassifier.setNotTrained();
		}
		
		if (qName == "Classifier") {
			if (inClassifier || (! inMultiClassifier)) {
				throw new SAXException("InClassifier");
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
			classifier.setFileName(multiClassifier.getCurrentPath() + file);
			qProject.registerName(name);
			
			try {
				classifier.load();
				multiClassifier.add(classifier);
			} catch (Exception ex) {
				throw new SAXException(
						"Classifier couldn't be loaded from file");
			}
		}

	}

	public void endElement(String c, String b, String a) throws SAXException {
		if (a == "MultiClassifier") {
			inMultiClassifier = false;
		}
		if (a == "Classifier") {
			inClassifier = false;
			if (classifier == null)
				throw new SAXException("No classifier");			
		}
	}
}

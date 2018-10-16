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


package rseslib.qmak;

import java.awt.Point;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.QClassifierType;

/**
 * Klasa wczytywania parserem danych xml klasyfikatora z pliku
 * @author Trickster's authors and Damian Wojcik
 * 
 */
public class XmlClassifierOpener extends DefaultHandler {

	private boolean inClassifier;

	private QClassifier classifier = null;

	public XmlClassifierOpener(QClassifier newClassifier) {
		super();
		classifier = newClassifier;
	}

	public void startElement(String namespaceURI, String lName, String qName,
			Attributes attrs) throws SAXException {

		if (qName == "Classifier") {
			if (inClassifier) {
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

			classifier.setName(name);
			classifier.setFileName(classifier.getCurrentPath() + file);
			classifier.setPosition(new Point(Integer.valueOf(x), Integer.valueOf(y)));
			classifier.setType(c1);

			try {
				classifier.load();
			} catch (Exception ex) {
				throw new SAXException(
						"Classifier couldn't be loaded from file");
			}
		}

	}

	public void endElement(String c, String b, String a) {
		if (a == "Project") {
			inClassifier = false;
		}
	}
}

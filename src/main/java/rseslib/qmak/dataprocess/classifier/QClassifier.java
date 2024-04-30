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


package rseslib.qmak.dataprocess.classifier;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import rseslib.processing.classification.Classifier;
import rseslib.qmak.XmlClassifierOpener;
import rseslib.qmak.UI.Progress.QVisualProgress;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.QClassifierType;
import rseslib.qmak.dataprocess.classifier.iQClassifier;

import java.awt.Point;

/**
 * Klasa reprezentujaca klasyfikatory w projekcie.
 * 
 * @author Leszek Tur
 * 
 */
public class QClassifier implements iQClassifier, iQXMLstoreable {

	protected boolean wytrenowany;

	private Classifier klasyfikator;

	protected Properties wlasciwosci;
	
	private Properties wlasciwosci_pr;

	protected QClassifierType typ;

	private Point p;
	
	public static String extension = "qcl"; 
	
	public static String description_extension = "qcld";
	
	public static String properties_extension = "qclp";

	public QClassifier(QClassifierType typeOfClassifier, String name) {
		typ = typeOfClassifier;
		wytrenowany = false;//nie usuwac tego
		
		try {
			wlasciwosci  = Configuration.loadDefaultProperties(typeOfClassifier.classifierClass);
		} catch (PropertyConfigurationException e) {
			e.printStackTrace();
			wlasciwosci = null;
		}
		wlasciwosci_pr = new Properties();
		wlasciwosci_pr.setProperty("name", name);
		wlasciwosci_pr.setProperty("class", typeOfClassifier.getClassName());
		wlasciwosci_pr.setProperty("file", name);
	} 

    public QClassifier(File XMLFile) {
		wlasciwosci  = new Properties();   
		wlasciwosci_pr = new Properties();  
		wlasciwosci_pr.setProperty("file", XMLFile.getAbsolutePath());	
		XmlClassifierOpener handler = new XmlClassifierOpener(this); 
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
	
	public Class getClassifierClass() {
		return typ.classifierClass;
	}
	
	public int trainOnTable(DoubleDataTable dataTable) {
		return trainOnTableWithProgress(dataTable, new EmptyProgress());
	}
	

	public int trainOnTableWithProgress(DoubleDataTable dataTable,Progress progres) {

		Class class_type = null;

		// class_type = Class.forName(typ.getPathToClass());
		class_type = typ.getClassifierClass();

		Class[] argKlasy = new Class[] { Properties.class, DoubleDataTable.class, Progress.class };
		Object[] argObiekty = new Object[] { wlasciwosci, dataTable, progres };
		
		try {
			klasyfikator = (Classifier) (class_type.getConstructor(argKlasy)).newInstance(argObiekty);
			Configuration conf = (Configuration)klasyfikator;
			for(String key : wlasciwosci.stringPropertyNames())
				wlasciwosci.setProperty(key, conf.getProperty(key));
			wytrenowany = true;
		} catch (InvocationTargetException e) {
			if (progres instanceof QVisualProgress)
				((QVisualProgress)progres).setErrorMessage(e.getTargetException().getMessage());
			else
				e.printStackTrace();
		} catch (Exception e) {
			if (progres instanceof QVisualProgress)
				((QVisualProgress)progres).setErrorMessage(e.getMessage());
			else
				e.printStackTrace();
		}

		return 0;
	}

	public double classify(DoubleData obj) {
		if (!wytrenowany) {
			return 0;
		}
		double retu = 0;
		try {
			retu = klasyfikator.classify(obj);
		} catch (PropertyConfigurationException e) {
			e.printStackTrace();
		} 
		return retu;
	};

	public boolean isTrained() {
		return wytrenowany;
	}

	public Properties getProperties() {
		return wlasciwosci;
	}

	public void setProperties(Properties prop) {
		this.wlasciwosci = prop;
		if(klasyfikator != null) {
			Configuration conf = (Configuration)klasyfikator;
			for(String key : wlasciwosci.stringPropertyNames())
				if(conf.isModifiableProperty(key)) {
					try {
						conf.setProperty(key, wlasciwosci.getProperty(key));
					} catch (PropertyConfigurationException e) {
					}
				}
		}
	}

	public boolean isClassifier() {
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
	
	public boolean isFileStoreable() {
		return Serializable.class.isInstance(klasyfikator);
	}	
	
	public String getName() {
		return wlasciwosci_pr.getProperty("name");
	}
	
	public void setName(String newName) {
		wlasciwosci_pr.setProperty("name", newName);
	}
	
	public void setFileName(String newFileName) {
		wlasciwosci_pr.setProperty("file", newFileName);
	}
	
	/**
	 * Get path to classifier file directory
	 * @return absolute path
	 */
	public String getCurrentPath() {
		String pom = wlasciwosci_pr.getProperty("file");
		return pom.substring(0, pom.lastIndexOf(System.getProperty("file.separator")));
	}
	
	public String getFileName() {
		return wlasciwosci_pr.getProperty("file");
	}

	/**
	 * Zwraca nazwe klasy, ktora reprezentuje klasyfikator
	 */
	public String getClassifierClassName() {
		return wlasciwosci_pr.getProperty("class");
	}
	
	public void XMLstore(BufferedWriter bw) throws IOException {
		bw.append("<Classifier ");
		bw.append(" name=\"" + getName() + "\"");
		bw.append(" classpath=\"" + typ.getPathToClass() + "\"");		
		bw.append(" filename=\"" + getFileName().substring(getFileName().lastIndexOf(System.getProperty("file.separator"))) + "\"");
		if(p != null) {
			bw.append(" x=\"" + p.x + "\"");
			bw.append(" y=\"" + p.y + "\"");
		} else {
			bw.append(" x=\"0\"");
			bw.append(" y=\"0\"");
		}
		bw.append(" >");
		bw.append(" </Classifier>");
	}

	public void store(File outputFile,  Progress prog) throws IOException, InterruptedException {
		prog.set("", 1);
	    if (klasyfikator != null) {
	        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outputFile));
	        out.writeObject(klasyfikator);
	        out.close();
	    }
	    prog.step();
	    
	    File properties_file = new File(outputFile.getAbsolutePath().substring(0, outputFile.getAbsolutePath().lastIndexOf(".")) + ".qclp");
	    properties_file.createNewFile();
	    OutputStream os = new FileOutputStream(properties_file);
	    wlasciwosci.store(os, null);
	    os.flush();
	    os.close();
	}

	public String getBaseFileExtension() {
		return QClassifier.extension;
	}
	
	public Classifier getClassifier() {
		return klasyfikator;
	}

	public QClassifierType getTyp() {
		return typ;
	}
	
	public void setType(QClassifierType newtype) {
		typ = newtype;
	}

	   
	public void load() throws IOException {
	    try {
	      File file = new File(getFileName());
	      if (file.exists()) {
	      	ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
	      	klasyfikator = (Classifier) in.readObject();
	        in.close();
	        
	        wlasciwosci.load(new FileInputStream(getFileName().substring(0, getFileName().lastIndexOf(".")) + ".qclp"));
	        wytrenowany = true;
	      }
	      else {
	    	wlasciwosci.load(new FileInputStream(getFileName().substring(0, getFileName().lastIndexOf(".")) + "." + properties_extension));
	        wytrenowany = false;
	      }
	   }
	    catch (Exception ex) {
	    	throw new IOException();
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

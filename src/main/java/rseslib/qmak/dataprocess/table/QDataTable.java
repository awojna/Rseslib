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


package rseslib.qmak.dataprocess.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


import javax.swing.table.AbstractTableModel;

import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.table.QDataTable;
import rseslib.qmak.dataprocess.table.QDataTableClassified;
import rseslib.qmak.dataprocess.table.QDataTableProperties;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.dataprocess.FileStoreable;
import rseslib.qmak.dataprocess.classifier.*;
import rseslib.structure.attribute.*;
import rseslib.structure.attribute.formats.DataFormatRecognizer;
import rseslib.structure.attribute.formats.HeaderFormatException;
import rseslib.structure.data.*;
import rseslib.structure.data.formats.*;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.table.NumericalStatistics;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;
import rseslib.util.random.RandomSelection;

import java.awt.Point;

/**
 * QDataTable represents data set in Qmak program.
 * 
 * @author Damian Wójcik
 */
public class QDataTable extends AbstractTableModel implements iQDataTable, FileStoreable {
	
    protected static final Random RANDOM_GENERATOR = new Random();
	
	protected Header header;

	protected boolean classified = false;

	protected ArrayList<DoubleData> data;

	protected NominalAttribute nominalDecisionAttribute = null;

	protected QDataTableProperties properties;
	
	protected int[] nDecDistribution = null;

	private Point p;
	
	public static String extension = "qtb"; 
	
	public static String description_extension = "qtbd";
	
	/**
	 * Constructor initializing only properties of the table Doesn't
	 * initializing header of the table and nominal attribute Please don't use,
	 * unless you are really sure what you are doing :)
	 */
	public QDataTable(String name, String fileName, Point newP) {
		properties = new QDataTableProperties(name, fileName);
		header = null;
		data = new ArrayList<DoubleData>();
		properties.set_saved();
		p = newP;  //TODO: dodac to przy innych konstruktorach
	}

	/**
	 * Constructor initializing header and the name of the table
	 */
	public QDataTable(String name, Header newHeader) {
		properties = new QDataTableProperties(name, "");
		header = newHeader;
		try {
			nominalDecisionAttribute = header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
			//no reaction?
		}
		data = new ArrayList<DoubleData>();
		properties.set_saved();
	}
	
	/**
	 * Constructor reading data from a file. Data format is recognized
	 * automatically. The constructor verifies compatibility of data with a
	 * given header.
	 * 
	 * @param dataFile
	 *            Data file to be loaded.
	 * @param hdr
	 *            Header for data in a given file.
	 * @param prog
	 *            Progress object for progress reporting.
	 * @throws IOException
	 *             If error in data has occured.
	 * @throws InterruptedException
	 *             If user has interrupted reading data.
	 */

	public QDataTable(File dataFile, Header hdr, Progress prog, String name,
			String filename) throws IOException, InterruptedException,
			BadHeaderException, HeaderFormatException, DataFormatException {
		DoubleDataInput doi = null;
		DataFormatRecognizer rec = new DataFormatRecognizer();
        switch (rec.recognizeFormat(dataFile))
        {
        case ARFF:
        	doi = new ArffDoubleDataInput(dataFile, hdr, prog);
        	break;
        case RSES:
        	doi = new RsesDoubleDataInput(dataFile, hdr, prog);
        	break;
        case CSV:
        	doi = new RseslibDoubleDataInput(dataFile, hdr, prog);
        	break;
        }
		header = doi.attributes();
		try {
			nominalDecisionAttribute = header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
		}
		data = new ArrayList<DoubleData>();
		while (doi.available()) {
			DoubleData dObject = doi.readDoubleData();
			data.add(dObject);
		}
		properties = new QDataTableProperties(name, filename);
		properties.set_saved();
	}

	/**
	 * Constructor reading data from the file Automatically recognizing format
	 * of the data and header of the table
	 */
	public QDataTable(File dataFile, Progress prog, String name, String filename)
			throws IOException, InterruptedException, HeaderFormatException,
			DataFormatException {
		DoubleDataInput doi = null;
		DataFormatRecognizer rec = new DataFormatRecognizer();
        switch (rec.recognizeFormat(dataFile))
        {
        case ARFF:
        	doi = new ArffDoubleDataInput(dataFile, prog);
        	break;
        case RSES:
        	doi = new RsesDoubleDataInput(dataFile, prog);
        	break;
        case CSV:
        	doi = new RseslibDoubleDataInput(dataFile, prog);
        	break;
        }
		header = doi.attributes();
		try {
			nominalDecisionAttribute = header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
		}
		data = new ArrayList<DoubleData>();
		while (doi.available()) {
			DoubleData dObject = doi.readDoubleData();
			data.add(dObject);
		}
		properties = new QDataTableProperties(name, filename);
		properties.set_saved();
	}

	/**
	 * Constructor initializing table with given header and data in
	 * DoubleDataTable
	 */
	public QDataTable(ArrayHeader newHeader, /* ArrayList */
			DoubleDataTable tab, String name, String fileName) {
		header = newHeader;
		try {
			nominalDecisionAttribute = header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
		}
		data = new ArrayList<DoubleData>();
		for (DoubleData obj : tab.getDataObjects()) {
			data.add(obj);
		}
		properties = new QDataTableProperties(name, fileName);
		properties.set_saved();
	}
	
	public QDataTable(File dataFile, File hdrFile, Progress prog, String name,
			String fileName) throws IOException, InterruptedException,
			HeaderFormatException, DataFormatException, BadHeaderException {
		DoubleDataInput doi = null;
		DataFormatRecognizer rec = new DataFormatRecognizer();
        switch (rec.recognizeFormat(dataFile))
        {
        case ARFF:
        	doi = new ArffDoubleDataInput(dataFile, prog);
        	break;
        case RSES:
        	doi = new RsesDoubleDataInput(dataFile, prog);
        	break;
        case CSV:
        	doi = new RseslibDoubleDataInput(dataFile, new ArrayHeader(hdrFile), prog);
        	break;
        }
		header = doi.attributes();
		try {
			nominalDecisionAttribute = header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
		}
		data = new ArrayList<DoubleData>();
		while (doi.available()) {
			DoubleData dObject = doi.readDoubleData();
			data.add(dObject);
		}
		properties = new QDataTableProperties(name, fileName);
		properties.set_saved();
	}

	public QDataTable(File hdrFile, String name) throws IOException,
			HeaderFormatException {
		header = new ArrayHeader(hdrFile);
		properties = new QDataTableProperties(name, "");
		try {
			nominalDecisionAttribute = header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
		}
		properties.set_saved();
	}
	
	public QDataTable copy() {
		QDataTable cp = new QDataTable();
		
		
		cp.header = this.header;

		cp.classified = this.classified;
		
		{
		cp.data = new ArrayList<DoubleData>();
		Iterator i = this.data.iterator();
		DoubleData dd;
		while (i.hasNext()) {
			dd = (DoubleData) i.next();
			cp.data.add((DoubleData) dd.clone());
		}
		}

		if (this.nominalDecisionAttribute == null) cp.nominalDecisionAttribute = null; else {
			cp.nominalDecisionAttribute = this.nominalDecisionAttribute;
		}

		if (this.properties== null) cp.properties = null; else cp.properties = this.properties.clone();
		
		if (nDecDistribution==null) cp.nDecDistribution = null; else {
			cp.nDecDistribution = new int[this.nDecDistribution.length];
			for (int i= 0;i<this.nDecDistribution.length;i++){
				cp.nDecDistribution[i] = this.nDecDistribution[i];
			}
		}

		return cp;
	}

	/**
	 * Constructor initializing table from classified table
	 */
	public QDataTable(QDataTableClassified table) {
		data = (ArrayList<DoubleData>) table.getDataObjects();
		properties = new QDataTableProperties(table.getProperties()); 
		header = table.attributes();
		nominalDecisionAttribute = header.nominalDecisionAttribute();
	}

	/**
	 * It exists because for compatibility with QDataTableClassified constructors
	 */
	public QDataTable() {		
	}

	/* nominalDecisionAttribute nie zostanie publicznie podany,
	 * to jest jedynie metoda dostepowa do niego
	 */
	public String NameOfResult(double i){
		return NominalAttribute.stringValue(i);
	}


	public QDataTable addRecord(DoubleData obj) {
		data.add(obj);
		properties.set_unsaved();
		return this;
	}

	public QDataTable removeRecord(DoubleData obj) {
		data.remove(obj);
		properties.set_unsaved();
		return this;
	}

	public QDataTable modifyRecord(DoubleData oldone, DoubleData newone) {
		return removeRecord(oldone).addRecord(newone);
	}

	public iQDataTable modifyAttributes(Header h) {
		header = h;
		
		for (int i = 0; i < data.size(); i++) {
			DoubleDataWithDecision dat = (DoubleDataWithDecision) data.get(i);
			DoubleDataObject nw = new DoubleDataObject(h);
			for (int j = 0; j < h.noOfAttr(); j++) {
				nw.set(j, dat.get(j));
				if (!Double.isNaN(dat.getDecision())) {
					nw.attributes().nominalDecisionAttribute().globalValueCode(
							NominalAttribute.stringValue(dat.getDecision()));
				}
			}
			data.set(i, nw);
		}
		nominalDecisionAttribute = h.nominalDecisionAttribute();
		properties.set_unsaved(); //???
		return this;
	}

	public QDataTable readHeader(String headerFile) throws IOException,
			HeaderFormatException {
		header = new ArrayHeader(new File(headerFile));
		return this;
	}

	/**
	 * Save table to the file
	 * 
	 * @throws IOException
	 */
	private void save(String fileName) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File(fileName)));
		header.store(bw);
		bw.newLine();
		Iterator iter = iterator();
		while (iter.hasNext()) {
			((DoubleData) iter.next()).store(bw);
		}
		bw.close();
		properties.set_saved();
	}
	
	public void save() throws IOException {
		this.save(properties.getFileName());
	}

	public void load() throws IOException, InterruptedException,
			DataFormatException {
		try {
			open(properties.getFileName());
		} catch (HeaderFormatException e) {
			// bad header
			throw new IOException("Bad header");
		}
	}
	
	public void restoreOldTable(iQDataTable old){
		this.data = (ArrayList<DoubleData>) old.getDataTable().getDataObjects();
		this.properties = old.getProperties();
		this.nDecDistribution= old.getDecisionDistribution();
	}

	/**
	 * Read data from a file
	 * 
	 * @throws IOException, InterruptedException, DataFormatException, HeaderFormatException
	 */
	private void open(String fileName) throws IOException, InterruptedException,
			DataFormatException, HeaderFormatException {
		DoubleDataInput doi = null;
		DataFormatRecognizer rec = new DataFormatRecognizer();
		File dataFile = new File(fileName);
		Progress prog = new EmptyProgress();

        switch (rec.recognizeFormat(dataFile))
        {
        case ARFF:
        	doi = new ArffDoubleDataInput(dataFile, prog);
        	break;
        case RSES:
        	doi = new RsesDoubleDataInput(dataFile, prog);
        	break;
        case CSV:
        	doi = new RseslibDoubleDataInput(dataFile, prog);
        	break;
        }
		header = doi.attributes();
		try {
			nominalDecisionAttribute = header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
		}
		nDecDistribution = null;
		data = new ArrayList<DoubleData>();
		while (doi.available()) {
			DoubleData dObject = doi.readDoubleData();
			data.add(dObject);
		}
		properties.set_saved();
	}

	public DoubleDataTable getDataTable() {
		if (data.isEmpty()) {
			return new ArrayListDoubleDataTable(header);
		}
		return new ArrayListDoubleDataTable(data);
	}

	public DoubleDataWithDecision getRow(int number) {
		return (DoubleDataWithDecision) data.get(number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return (header.noOfAttr());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int columnIndex) {
		return (header.name(columnIndex));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return new Double(getRow(rowIndex).get(columnIndex));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		boolean pusty = false;
		try {
			getRow(rowIndex).set(columnIndex,
					(new Double((String) aValue).doubleValue()));
		} catch (Exception a) {
			if (a.getMessage() != null) {
				if (a.getMessage().compareTo("empty String") == 0) {
					getRow(rowIndex).set(columnIndex, Double.NaN);
					pusty = true;
				}
			}
		}
		;
		Attribute att = header.attribute(columnIndex);
		if (att.isNominal() && !pusty) {
			getRow(rowIndex).set(columnIndex, ((Double) aValue).doubleValue());
			// ((NominalAttribute) att).globalValueCode(aValue
			// .toString()));
		}
		properties.set_unsaved();
	}

	public int noOfObjects() {
		return getRowCount();
	}

	public void add(DoubleData obj) {
		addRecord(obj);
	}

	public boolean remove(DoubleData obj) {
		properties.set_unsaved();
		return data.remove(obj);
	}

	public ArrayList<DoubleData> getDataObjects() {
		return data;
	}

	/**
	 * DoubleDataTable method
	 */
	public Iterator iterator() {
		return data.iterator();
	}

	/**
	 * DoubleDataTable method
	 */
	public NominalAttribute nominalDecisionAttribute() {
		return nominalDecisionAttribute;
	}

	/**
	 * Returns the basic statistics of a given numerical attribute.
	 * 
	 * @return Statistics of a given numerical attribute.
	 */
	public NumericalStatistics getNumericalStatistics(int attr) {
		return null;
	}

	/**
	 * DoubleDataTable method
	 */
	public int[] getDecisionDistribution() {
		if (nDecDistribution == null) {
			calculateDecisionDistribution();
		}
		return nDecDistribution;
	}

	/**
	 * DoubleDataTable method
	 */
	public int[] getValueDistribution(int attrInd) {
		return null;
	}

		/** Nie wiem czy sie przyda
	 * divide the table into two table of given sizes DoubleDataTable method
	 */
	public ArrayList<DoubleData>[] randomSplit(int noOfPartsForLeft, int noOfPartsForRight) {
		ArrayList<DoubleData>[] parts = new ArrayList[2];
		parts[0] = new ArrayList<DoubleData>();
		parts[1] = new ArrayList<DoubleData>();
		boolean[] assigned = RandomSelection.subset(this.data.size(),
				noOfPartsForLeft, noOfPartsForRight);
		for (int ind = 0; ind < this.data.size(); ind++) {
			if (assigned[ind]) {
				parts[0].add(this.data.get(ind));
			} else {
				parts[1].add(this.data.get(ind));
			}
		}
		return parts;
	}

	/** Nie wiem czy si� przyda
	 * divide the table into given number of tables DoubleDataTable method
	 */
	public ArrayList<DoubleData>[] randomPartition(int noOfParts) {
		ArrayList<DoubleData>[] parts = new ArrayList[noOfParts];
		for (int part = 0; part < parts.length; part++) {
			parts[part] = new ArrayList<DoubleData>();
		}
		boolean[] assigned = new boolean[this.data.size()];
		int noOfAssigned = 0;
		int part = 0;
		while (part < parts.length - 1) {
			int ind = RANDOM_GENERATOR.nextInt(this.data.size());
			while (assigned[ind]) {
				ind = RANDOM_GENERATOR.nextInt(this.data.size());
			}
			parts[part].add(this.data.get(ind));
			assigned[ind] = true;
			noOfAssigned++;
			if (noOfAssigned * parts.length >= this.data.size() * (part + 1)) {
				part++;			}
		}
		for (int ind = 0; ind < this.data.size(); ind++) {
			if (!assigned[ind]) {
				parts[parts.length - 1].add(this.data.get(ind));
			}
		}
		return parts;
	}

    /**
     * Random partition of this table into a given number of parts of equal sizes preserving class distribution.
     *
     * @param noOfParts Number of parts to be generated.
     * @return          Table divided into noOfParts collections.
     */
    public ArrayList<DoubleData>[] randomStratifiedPartition(int noOfParts)
    {
    	NominalAttribute decAttr = header.nominalDecisionAttribute();
    	
    	// separate objects into decision classes
    	ArrayList<DoubleData>[] decClass = new ArrayList[decAttr.noOfValues()];
    	for (DoubleData dObj : data)
    		decClass[decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())].add(dObj);
    	
    	// partition each decision class separately
        ArrayList<DoubleData>[][] parts = new ArrayList[decAttr.noOfValues()][];
        for (int dec = 0; dec < parts.length; dec++)
        {
        	parts[dec] = new ArrayList[noOfParts];
        	for (int part = 0; part < parts.length; part++)
        		parts[dec][part] = new ArrayList<DoubleData>();
            boolean[] assigned = new boolean[decClass[dec].size()];
            int noOfAssigned = 0;
            int part = 0;
            while (part < noOfParts - 1)
            {
                int ind = RANDOM_GENERATOR.nextInt(decClass[dec].size());
                while (assigned[ind])
                	ind = RANDOM_GENERATOR.nextInt(decClass[dec].size());
                parts[dec][part].add(decClass[dec].get(ind));
                assigned[ind] = true;
                noOfAssigned++;
                if (noOfAssigned * noOfParts >= decClass[dec].size() * (part + 1))
                	part++;
            }
            for (int ind = 0; ind < decClass[dec].size(); ind++)
                if (!assigned[ind])
                	parts[dec][parts.length - 1].add(decClass[dec].get(ind));
        }
        
        // merge folds from parts for particular decisions
        ArrayList<DoubleData>[] folds = new ArrayList[noOfParts];
    	for (int fold = 0; fold < folds.length; fold++)
    	{
    		folds[fold] = new ArrayList<DoubleData>();
            for (int dec = 0; dec < parts.length; dec++)
            	folds[fold].addAll(parts[dec][fold]);
    	}
        return folds;
    }

	/**
	 * Return the header of the table DoubleDataTable method
	 */
	public Header attributes() {
		return header;
	}

	/**
	 * Calculates decision distribution in this table.
	 */
	private void calculateDecisionDistribution() {
		Iterator dataIter = iterator();
		nDecDistribution = new int[nominalDecisionAttribute.noOfValues()];
		while (dataIter.hasNext()) {
			DoubleDataWithDecision dObj = (DoubleDataWithDecision) dataIter
					.next();
			nDecDistribution[nominalDecisionAttribute.localValueCode(dObj
					.getDecision())]++;
		}
	}

	/**
	 * Save the table to the file DoubleDataTable method
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void store(File outputFile, Progress prog) throws IOException,
			InterruptedException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		header.store(bw);
		bw.newLine();
		Iterator iter = iterator();
		while (iter.hasNext()) {
			((DoubleData) iter.next()).store(bw);
			prog.step();
		}
		bw.close();
		properties.set_saved();
	}

    /**
     * Saves this object to a file in arff format.
     *
     * @param outputFile File to be used for storing this object.
     * @param prog       Progress object for progress reporting.
     * @throws IOException If an I/O error has occured.
     * @throws InterruptedException If user has interrupted saving object.
     */
    public void storeArff(String name, File outputFile, Progress prog) throws IOException, InterruptedException
    {
    	throw new IOException("Arff format not implemented");
    }
    
	/**
	 * Return properties of the table
	 */
	public QDataTableProperties getProperties() {
		return properties;
	}

	/**
	 * Return table with additional column with results of classification
	 */
	public iQDataTable classify(iQClassifier clasifier) {
		try {
			header.nominalDecisionAttribute();
		} catch (RuntimeException e) {
			return this;
		}
		return new QDataTableClassified(this, clasifier);
	}

	/**
	 * If table have not been yet classified, all row are not badly classified
	 */
	public boolean isBadlyClassified(int rowIndex) {
		return false;
	}

	public boolean isSaved() {
		return properties.saved();
	}

	public void setClassified(boolean bool) {
		classified = bool;
	}

	public void setSaved(boolean b) {
		if (b) {
			properties.set_saved();
		} else {
			properties.set_unsaved();
		}
	}

	public void setName(String name) {
		properties.setName(name);
	}

	public void setFileName(String name) {
		properties.setFileName(name);
	}

	public String getName() {
		return properties.getName();
	}

	public String getFileName() {
		return properties.getFileName();
	}

	public Object clone() {
		return this.copy();
	}
	
	public boolean isClassified() {
		return classified;
	}
	
	public boolean isTable() {
		return true;
	}

	public boolean isClassifier(){
		return false;
	}
	
	
	public boolean isXMLstoreable() {
		return true;
	}
	
	public boolean isFileStoreable() {
		return true;
	}	
	
	public boolean isMulticlassifier() {
		return false;
	}
	
	public void XMLstore (BufferedWriter bw)  throws IOException {
		//If you append there new elements then you must do the same in loader:  rseslib.qmak.XmlOpener
		bw.append("<DataTable");
		bw.append(" name=\"" + properties.getName() + "\"");
		bw.append(" filename=\"" + properties.getFileName().substring(properties.getFileName().lastIndexOf(System.getProperty("file.separator"))) + "\"");
		bw.append(" x=\"" + p.x + "\"");
		bw.append(" y=\"" + p.y + "\"");
		bw.append(" >");		
		bw.append(" </DataTable>");	
		bw.newLine();		
	}
	
	public String getBaseFileExtension() {
		return QDataTable.extension;
	}
	
	public void sort(int col){
		//przez wstawianie ;)
		if (col < 0 || col >= this.getColumnCount()) return;
		int i,j;
		int Max;
		DoubleData temp;
		for (i=0;i<this.getRowCount()-1;i++){
			Max = i;
			for (j=i;j<this.getRowCount();j++){
				if (data.get(Max).get(col) < data.get(j).get(col)) {
					Max = j;
				}
			}
			if (Max != i) {
				temp = data.get(Max);
				data.set(Max, data.get(i));
				data.set(i, temp);
			}
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
		return extension;
	}
}

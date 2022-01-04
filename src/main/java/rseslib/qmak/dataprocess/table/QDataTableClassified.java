/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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

import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.table.QDataTable;
import rseslib.qmak.dataprocess.table.QDataTableClassified;
import rseslib.qmak.dataprocess.table.QDataTableProperties;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.classifier.*;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.system.progress.Progress;

/**
 * Class representing a Classified Table. Within class code there are still many methods
 * which we probably won't need: save, load, Streams, editing
 * 
 * @author Damian Wojcik
 * 
 */
public class QDataTableClassified extends QDataTable {
	protected boolean classified = true;

	private ArrayList<Double> decisions;

	private ArrayHeader new_header;

	public static final String SUFFIX_HEADER = "_clas";

	
	public QDataTableClassified() {
		
	}
	/**
	 * Constructing table with given DataTable and classifier
	 */
	public QDataTableClassified (iQDataTable oldData, iQClassifier classifier) {

		int i = 0;
		
		data = (ArrayList<DoubleData>) oldData.getDataObjects();
		decisions = new ArrayList<Double>();
		properties = new QDataTableProperties(oldData.getProperties());
		header = oldData.attributes();
		nominalDecisionAttribute = header.nominalDecisionAttribute();
		properties.set_unsaved();
		while (i < oldData.noOfObjects()) {
			decisions.add(new Double(classifier.classify(oldData.getRow(i))));
//			decisions.add(new Double(classifier.classifyDataTableRecord(oldData, i)));
			i++;
		}
		myNewHeader();
	}

	//I'm not sure if we'll need it
	/*public QDataTableClassified(iQDataTable oldData) {
		int i = 0;

		data = (ArrayList<DoubleData>) oldData.getDataObjects();
		decisions = new ArrayList<Double>();
		properties = new QDataTableProperties(oldData.getProperties());
		header = oldData.attributes();
		nominalDecisionAttribute = header.nominalDecisionAttribute();
		while (i < oldData.noOfObjects()) {
			decisions.add(new Double(oldData.getRow(1).getDecision()));
			i++;
		}
		properties.set_unsaved();
		myNewHeader();
	}*/
	
	public QDataTableClassified copy(){
		QDataTableClassified cp = new QDataTableClassified();

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

		cp.nominalDecisionAttribute = this.nominalDecisionAttribute;

		if (this.properties== null) cp.properties = null; else cp.properties = this.properties.clone();
		
		if (nDecDistribution==null) cp.nDecDistribution = null; else {
			cp.nDecDistribution = new int[this.nDecDistribution.length];
			for (int i= 0;i<this.nDecDistribution.length;i++){
				cp.nDecDistribution[i] = this.nDecDistribution[i];
			}
		}

		cp.decisions = new ArrayList<Double>();
		{Iterator i = this.decisions.iterator();
		 while (i.hasNext()) cp.decisions.add((Double) i.next());
		}
		
		cp.new_header = this.new_header;

		return cp;
	}
	
	public Object clone() {
		return this.copy();
	}

	public boolean isFileStoreable() {
		return false;
	}	
	
	private void myNewHeader() {
		Attribute[] coll = new Attribute[header.noOfAttr() + 1];
		for (int i = 0; i < header.noOfAttr(); i++) {
			coll[i] = header.attribute(i);
		}
		Attribute dec = new Attribute(Attribute.Type.text,
				Attribute.ValueSet.nominal, header.attribute(
						header.decision()).name()
						+ SUFFIX_HEADER);
		//debugowanie
		coll[header.noOfAttr()] = dec;
		new_header = new ArrayHeader(coll, header.missing());
	}

	/**
	 * Changing classified table is not permitted
	 */
	/*
	 * public TTableClassified addRecord(DoubleData obj) { return this; }
	 * 
	 * public TTableClassified removeRecord(DoubleData obj) { return this; }
	 * 
	 * public TTableClassified modifyRecord(DoubleData oldone, DoubleData
	 * newone) { return this; }
	 * 
	 * public TTableClassified modifyAttributes(Header h) { return this; }
	 */

	public void save(String fileName) throws IOException {

		BufferedWriter output = null;
		output = new BufferedWriter(new FileWriter(new File(fileName)));
		new_header.store(output);
		output.newLine();
		for (int no = 0; no < noOfObjects(); no++) {
			for (int attr = 0; attr < header.noOfAttr(); attr++) {
				if (header.isInterpretable(attr)) {
					if (Double.isNaN(getRow(no).get(attr))) {
						output.write(header.missing());
					} else {
						if (header.isNominal(attr)) {
							output.write(NominalAttribute.stringValue(getRow(no).get(attr)));
						} else {
							if (header.isNumeric(attr)) {
								output.write(Double.toString(getRow(no).get(
										attr)));
							}
						}
					}
				} else {
					if (header.isText(attr)) {
						output.write(NominalAttribute.stringValue(getRow(no).get(attr)));

						// TricksterMain.Log.debug(m_header.attribute(attr));
					}
				}
				output.write(' ');
			}
			output.write(NominalAttribute.stringValue(((Double) decisions.get(no)).doubleValue()));
			output.newLine();
		}
		output.close();
		properties.set_saved();
	}

	public int loadFromStream() {
		return 0;
	}

	/*public void open(String fileName) {
	}*/

	/**
	 * This table have one additional column (when compared with
	 * DoubleDataTable) It's the column with results of classification
	 */
	public int getColumnCount() {
		return ((DoubleData) data.get(0)).attributes().noOfAttr() + 1;
	}

	public String getColumnName(int columnIndex) {
		if (columnIndex < getColumnCount() - 1) {
			return ((DoubleData) data.get(0)).attributes().name(columnIndex);
		}
		return QMainFrame.qmainframe.messages.getString("QTVClassificationResult");
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex < getColumnCount() - 1) {
			return new Double(getRow(rowIndex).get(columnIndex));
		}
		return decisions.get(rowIndex);
	}
	
	private void zamien(int i,int j) {
		DoubleData temp;
		Double temp2;
		if (j != i) {
			temp = data.get(i); temp2 = decisions.get(i);
			data.set(i, data.get(j)); decisions.set(i, decisions.get(j));
			data.set(j, temp); decisions.set(j, temp2);
		}
	}
	
	public void sort(int col){
		//przez wstawianie ;)
		if (col < 0 || col >= this.getColumnCount()) return;
		int i,j;
		int Max;
		if (col < this.getColumnCount()-1) {
			for (i=0;i<this.getRowCount()-1;i++){
				Max = i;
				for (j=i;j<this.getRowCount();j++){
					if (data.get(Max).get(col) < data.get(j).get(col)) {
						Max = j;
					}
				}
				zamien(Max,i);
			}
		}else{
			for (i=0;i<this.getRowCount()-1;i++){
				Max = i;
				for (j=i;j<this.getRowCount();j++){
					if (decisions.get(Max) < decisions.get(j)) {
						Max = j;
					}
				}
				zamien(Max,i);
			}
		}
	}

	/**
	 * @param old - wskaznik do tabeli, w ktorej sa dane do przywrocenia. 
	 * Przywraca do tej tabeli dane z tabeli old
	 */
	public void restoreOldTable(QDataTableClassified old){
		this.data = (ArrayList<DoubleData>) old.getDataTable().getDataObjects();
		this.properties = old.getProperties();
		this.nDecDistribution= old.getDecisionDistribution();

		this.decisions = old.decisions;
		this.new_header = old.new_header;	
	}

	/**
	 * 
	 * @param rowIndex
	 * @return decision of classifier for a specified row
	 */
	public Double getClassifierDecision(int rowIndex) {
		return decisions.get(rowIndex);
	}
	
	/**
	 * Check if the answer is correct
	 */
	public boolean isBadlyClassified(int rowIndex) {
		if (getRow(rowIndex).getDecision() != ((Double) decisions.get(rowIndex))
				.doubleValue()) {
			return true;
		}
		return false;
	}

	public iQDataTable classify(iQClassifier classifier) {
		return new QDataTableClassified(new QDataTable(this), classifier);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	public void add(DoubleData obj) {

	}

	public boolean remove(DoubleData obj) {
		return false;
	}

	public void store(File outputFile, Progress prog) throws IOException,
			InterruptedException {
	}
	
	public boolean isClassified() {
		return classified;
	}

	public int saveToStream() {
		return 0;
	}
}

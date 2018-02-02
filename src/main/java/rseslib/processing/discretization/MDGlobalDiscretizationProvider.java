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


package rseslib.processing.discretization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import rseslib.processing.transformation.FunctionBasedAttributeTransformer;
import rseslib.processing.transformation.TransformationProvider;
import rseslib.processing.transformation.Transformer;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.DoubleDataTable;

/**
 * This class represents a method of discretizing continuous attributes based on
 * a local MD heuristic.
 * 
 * @author Marcin Jałmużna
 */
public class MDGlobalDiscretizationProvider implements TransformationProvider {

	private HashMap<Integer, ArrayList<Double>> cuts;
	private ArrayList<Record> intervals;
	private HashMap<Integer, TreeSet<Double>> possibleCuts;

    /**
     * Creates discretization cuts for one attribute.
     * Main method of this discretization provider.
     * 
     * @param attribute				Selected attribute for discretization.
     * @param number_of_intervals 	Not important
     * @param table 				Data used for estimating the best cuts.
     * @return Discretization cuts 
     */
	private void discretize(DoubleDataTable table) {
		
		int nOfAttr = table.attributes().noOfAttr();
		int decAttr = table.attributes().decision();
		

		possibleCuts=new HashMap<Integer, TreeSet<Double>>();
		for (int i = 0; i < nOfAttr; i++)
			if (table.attributes().isConditional(i)
					&& table.attributes().isNumeric(i)) {
				TreeSet<Double> tmp = new TreeSet<Double>();
				for (DoubleData dd : table.getDataObjects()) {
					tmp.add(dd.get(i));
				}
				possibleCuts.put(i, tmp);
			}

		intervals = new ArrayList<Record>();
		for (int i = 0; i < table.noOfObjects(); i++){
			Double dec1 = table.getDataObjects().get(i).get(decAttr);
			for (int j = i + 1; j < table.noOfObjects(); j++) {				
				Double dec2 = table.getDataObjects().get(j).get(decAttr);				
				if (!dec1.equals(dec2)) {
					Record r = new Record();
					r.setStart(new HashMap<Integer, Double>());
					r.setEnd(new HashMap<Integer, Double>());

					for (int i2 = 0; i2 < nOfAttr; i2++) {
						if (i2 != decAttr) {
							Double val_i = table.getDataObjects().get(i)
									.get(i2);
							Double val_j = table.getDataObjects().get(j)
									.get(i2);
							if (val_i > val_j) {
								Double t = val_i;
								val_i = val_j;
								val_j = t;
							}
							r.getStart().put(i2, val_i);
							r.getEnd().put(i2, val_j);
						}
					}
					intervals.add(r);
				}
			}
		}

		while(intervals.size()>0){
			int maxVal=0;
			int maxAttr=-1;
			Double maxCut=Double.MIN_VALUE;
			for(Integer i: possibleCuts.keySet()){
				for(Double d:possibleCuts.get(i)){
					int sum=0;
					for(Record r:intervals)
						if((r.getStart().get(i)<=d)&&(r.getEnd().get(i)>d))
							sum++;						
					if(sum>maxVal){
						maxVal=sum;
						maxAttr=i;
						maxCut=d;
					}					
				}
			}
			// if exist indiscernible records 
			if(maxVal==0)
				break;
			Double higher =possibleCuts.get(maxAttr).higher(maxCut);
			cuts.get(maxAttr).add((maxCut+higher)/2.0);
			possibleCuts.get(maxAttr).remove(maxCut);
			
			ArrayList<Record> tmp = new ArrayList<Record>();
			for(Record r:intervals)
				if((r.getStart().get(maxAttr)<=maxCut)&&(r.getEnd().get(maxAttr)>maxCut))
					tmp.add(r);
			intervals.removeAll(tmp);
	
		}
	}

	private class Record {
		private HashMap<Integer, Double> start = new HashMap<Integer, Double>();
		private HashMap<Integer, Double> end = new HashMap<Integer, Double>();

		public void setStart(HashMap<Integer, Double> start) {
			this.start = start;
		}

		public HashMap<Integer, Double> getStart() {
			return start;
		}

		public void setEnd(HashMap<Integer, Double> end) {
			this.end = end;
		}

		public HashMap<Integer, Double> getEnd() {
			return end;
		}

	}
	
    /**
     * Method that generate discretization based on data table.
     * 
     * @param table 		Data table to estimate the discretization.
     * @return attribute 	Discretization estimated on data table.
     */
	public Transformer generateTransformer(DoubleDataTable table) {
		Attribute[] attributes = new Attribute[table.attributes().noOfAttr()];
		AttributeDoubleFunction discr_table[] = new AttributeDoubleFunction[table
				.attributes().noOfAttr()];

		cuts = new HashMap<Integer, ArrayList<Double>>();
		for (int i = 0; i < table.attributes().noOfAttr(); i++) {
			if (table.attributes().isConditional(i)
					&& table.attributes().isNumeric(i)) {
				cuts.put(i, new ArrayList<Double>());
			}
		}
		discretize(table);

		for (int i = 0; i < attributes.length; i++)
			if (table.attributes().isConditional(i)
					&& table.attributes().isNumeric(i)) {
				Collections.sort(cuts.get(i));

				double[] c = new double[cuts.get(i).size()];
				for (int j = 0; j < c.length; j++) {
					c[j] = cuts.get(i).get(j);
					//System.out.println("attribute: " + i + " cut: " + c[j]);
				}
				NumericAttributeDiscretization disc = new NumericAttributeDiscretization(
						i, (NumericAttribute) table.attributes().attribute(i),
						c);
				attributes[i] = disc.getAttribute();
				discr_table[i] = disc;
			} else
				attributes[i] = table.attributes().attribute(i);

		Header new_header = new ArrayHeader(attributes, table.attributes()
				.missing());

		return new FunctionBasedAttributeTransformer(new_header, discr_table);
	}
}
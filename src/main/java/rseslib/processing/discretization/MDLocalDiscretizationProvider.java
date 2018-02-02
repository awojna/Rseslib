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
import java.util.SortedMap;
import java.util.TreeMap;
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
 * @author Marcin Jalmuzna
 */
public class MDLocalDiscretizationProvider implements TransformationProvider {

	private TreeSet<Double> decisionVal;
	private HashMap<Integer, ArrayList<Double>> cuts;
	private Integer maxVal;
	private Double maxCut;
	private Integer maxAttr;


    /**
     * Creates discretization cuts for whole data table.
     * 
     * @param table 				Data used for estimating the best cuts.
     */
	private void discretize(DoubleDataTable table) {

		HashMap<Double, Integer> before = new HashMap<Double, Integer>();
		HashMap<Double, Integer> after = new HashMap<Double, Integer>();
		for (Double v : decisionVal) {
			before.put(v, 0);
			after.put(v, 0);
		}

		for (DoubleData dd : table.getDataObjects()) {
			Double dec = Double.valueOf(dd.get(table.attributes().decision()));
			after.put(dec, after.get(dec) + 1);
		}

		maxVal = 0;
		maxCut = Double.MIN_VALUE;
		maxAttr = -1;

		for (int a = 0; a < table.attributes().noOfAttr(); a++) {
			if (table.attributes().isConditional(a)
					&& table.attributes().isNumeric(a)) {

				SortedMap<Double, HashMap<Double, Integer>> tmpDist = getDistribution(
						table, a);
				findMax(tmpDist, before, after, a);
				
				HashMap<Double, Integer> temp;
				temp = after;
				after = before;
				before = temp;
			}
		}
		if (maxVal > 0) {
			cuts.get(maxAttr).add(maxCut);
			DoubleDataTable table1 = (DoubleDataTable) table.clone();
			DoubleDataTable table2 = (DoubleDataTable) table.clone();

			for (DoubleData dd : table.getDataObjects()) {
				if (dd.get(maxAttr) > maxCut)
					table1.remove(dd);
				else
					table2.remove(dd);
			}
			discretize(table1);
			discretize(table2);
		}
	}
	
    /**
     * Creates SortedMap representing distribution of decisions 
     * for every value of given attribute 
     * 
     * @param attr 				Attribute.
     * @return Distribution of decisions for every value of given attribute 
     */
	private SortedMap<Double, HashMap<Double, Integer>> getDistribution(
			DoubleDataTable table, int attribute) {

		SortedMap<Double, HashMap<Double, Integer>> tmpDist = new TreeMap<Double, HashMap<Double, Integer>>();

		for (DoubleData dd : table.getDataObjects()) {

			Double tmp = Double.valueOf(dd.get(attribute));
			Double dec = Double.valueOf(dd.get(table.attributes().decision()));

			if (tmpDist.containsKey(tmp)) {
				HashMap<Double, Integer> map = tmpDist.get(tmp);
				map.put(dec, map.get(dec) + 1);
			} else {
				HashMap<Double, Integer> map = new HashMap<Double, Integer>();
				for (Double v : decisionVal)
					map.put(v, 0);

				map.put(dec, 1);
				tmpDist.put(tmp, map);
			}
		}

		return tmpDist;
	}
    /**
     * Finds the best cut for given attribute.
     * Maximized value is number of differentiated(decision) records.
     * 
     * @param distribution 			Distribution of decisions for every value of given attribute 
     * @param before 				Map, where key set is set of values of decision attribute and values are 0
     * @param after					Distribution of values of decision attribute
     * @param attr					Index of attribute
     */
	private void findMax(
			SortedMap<Double, HashMap<Double, Integer>> distribution,
			HashMap<Double, Integer> before, HashMap<Double, Integer> after,
			Integer attr) {

		Double lastKey = Double.MIN_VALUE;

		for (Double key : distribution.keySet()) {
			int val = 0;
			for (Double i : after.keySet()) {
				for (Double j : before.keySet()) {
					if (i != j)
						val += after.get(i) * before.get(j);
				}
			}
			for (Double i : after.keySet()) {
				after.put(i, after.get(i) - distribution.get(key).get(i));
				before.put(i, before.get(i) + distribution.get(key).get(i));
			}
			if (val > maxVal) {
				maxVal = val;
				maxCut = (key + lastKey) / 2.0;
				maxAttr = attr;
			}
			lastKey = key;
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
		decisionVal = new TreeSet<Double>();
		for (DoubleData dd : table.getDataObjects()) {
			decisionVal.add(Double.valueOf(dd
					.get(table.attributes().decision())));
		}

		discretize(table);

		for (int i = 0; i < attributes.length; i++)
			if (table.attributes().isConditional(i)
					&& table.attributes().isNumeric(i)) {
				Collections.sort(cuts.get(i));

				double[] c = new double[cuts.get(i).size()];
				int no_rep = 0;
				for (int j = 0; j < c.length; j++) {
					double val = cuts.get(i).get(j);
					if(no_rep == 0 || val != c[no_rep - 1])
						c[no_rep++] = val;
					//System.out.println("attribute: " + i + " cut: " + c[j]);
				}
				double[] d = new double[no_rep];
				for(int j = 0; j < no_rep; j++)
					d[j] = c[j];
				NumericAttributeDiscretization disc = new NumericAttributeDiscretization(
						i, (NumericAttribute) table.attributes().attribute(i),
						d);
				attributes[i] = disc.getAttribute();
				discr_table[i] = disc;
			} else
				attributes[i] = table.attributes().attribute(i);

		Header new_header = new ArrayHeader(attributes, table.attributes()
				.missing());

		return new FunctionBasedAttributeTransformer(new_header, discr_table);
	}
}
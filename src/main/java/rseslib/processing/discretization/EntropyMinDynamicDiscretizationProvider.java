/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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
import java.util.Iterator;
import java.util.TreeMap;

import rseslib.processing.transformation.FunctionBasedAttributeTransformer;
import rseslib.processing.transformation.TransformationProvider;
import rseslib.processing.transformation.Transformer;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.function.doubleval.AttributeDoubleFunction;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;

/**
 * Top-down local method discretizing all numerical attribute at once (1993 Fayyad & Irani).
 * It starts with the whole set of objects and splits it into two subsets
 * with the optimal cut selected from all numerical attributes.
 * Then the algorithm splits each subset recursively
 * scanning all possible cuts over all numerical attributes
 * and selecting the cut maximizing information gain, i.e. minimizing entropy
 * at each step.
 * 
 * @author Marcin Jalmuzna
 */
public class EntropyMinDynamicDiscretizationProvider implements
		TransformationProvider {

	/** Key - number of attribute, value - cuts */
	private HashMap<Integer, ArrayList<Double>> cuts;


	/**
	 * Method that generate cuts for given data table;
	 * 
	 * @param tabla data table
	 */
	private void discretize(DoubleDataTable table) {

		double dataSize = table.getDataObjects().size();
		if (dataSize > 1) {
			ArrayList<Double> decisions = new ArrayList<Double>();
			for (DoubleData dd : table.getDataObjects()) {
				Double d = dd.get(dd.attributes().decision());
				if (!decisions.contains(d)) {
					decisions.add(d);
				}

			}

			int minAttr = -1;
			Double minCut = 0.0;
			Double mainMinVal = Double.MAX_VALUE;

			for (int attr = 0; attr < table.attributes().noOfAttr(); attr++) {
				if (table.attributes().isNumeric(attr)) {

					TreeMap<Double, Record> decisionDistribution;
					decisionDistribution = new TreeMap<Double, Record>();
					Iterator<DoubleData> iter = table.getDataObjects()
							.iterator();
					while (iter.hasNext()) {
						DoubleData dd = iter.next();
						Double tmp = Double.valueOf(dd.get(attr));
						Double dec = Double.valueOf(dd.get(table.attributes()
								.decision()));
						if (decisionDistribution.containsKey(tmp)) {
							Record r = decisionDistribution.get(tmp);
							r.setSum(r.getSum() + 1);
							if (r.getDecisions().containsKey(dec)) {
								r.getDecisions().put(dec,
										r.getDecisions().get(dec) + 1);
							} else {
								r.getDecisions().put(dec, 1);
							}
						} else {
							Record r = new Record();
							r.setSum(1);
							r.setDecisions(new HashMap<Double, Integer>());
							r.getDecisions().put(dec, 1);
							decisionDistribution.put(tmp, r);
						}
					}

					double min = decisionDistribution.firstKey();
					double minVal = Double.MAX_VALUE;
					double min_e_lw = 0;
					double min_e_gt = 0;
					int[][] min_tab = null;

					// initialize the arrays
					int[][] tab = new int[decisions.size()][2];
					for (int j = 0; j < decisions.size(); j++) {
						tab[j][0] = 0;
						tab[j][1] = 0;
					}
					double lw = 0;
					double gt = dataSize;
					for (Double tmp : decisionDistribution.keySet()) {
						for (Double d : decisionDistribution.get(tmp)
								.getDecisions().keySet()) {
							tab[decisions.indexOf(d)][1] += decisionDistribution
									.get(tmp).getDecisions().get(d);
						}
					}

					// go over all candidate cuts and select the one with the least entropy
					for (Double tmp : decisionDistribution.keySet()) {
						lw += decisionDistribution.get(tmp).getSum();
						gt -= decisionDistribution.get(tmp).getSum();

						for (Double d : decisionDistribution.get(tmp)
								.getDecisions().keySet()) {
							tab[decisions.indexOf(d)][0] += decisionDistribution
									.get(tmp).getDecisions().get(d);
							tab[decisions.indexOf(d)][1] -= decisionDistribution
									.get(tmp).getDecisions().get(d);
						}

						// calculate entropy for the candidate cut 'tmp'
						double e_lw = 0;
						double e_gt = 0;
						for (int j = 0; j < decisions.size(); j++) {
							if (lw > 0) {
								double v1 = ((double) tab[j][0]) / (double) lw;
								if (v1 != 0)
									e_lw -= v1 * log2(v1);

							}
							if (gt > 0) {
								double v2 = (((double) tab[j][1]) / (double) gt);
								if (v2 != 0)
									e_gt -= v2 * log2(v2);

							}
						}

						double val = (((double) lw / dataSize) * e_lw)
								+ (((double) gt / dataSize) * e_gt);

						// update current best cut
						if ((val < minVal) && (val < mainMinVal)) {
							minVal = val; // entropy of new best cut
							min = tmp; // new best cut
							min_e_lw = e_lw;
							min_e_gt = e_gt;
							min_tab = tab.clone();
						}
					}
					if (min_tab != null) {
						double Dp0 = 0;
						double Dp1 = 0;
						double Dp = decisions.size();
						double ent = 0;
						for (int i = 0; i < Dp; i++) {
							double v = ((double) (min_tab[i][0] + min_tab[i][1]))
									/ dataSize;
							if (v > 0)
								ent -= v * log2(v);
							if (min_tab[i][0] > 0)
								Dp0++;
							if (min_tab[i][1] > 0)
								Dp1++;
						}

						double infoGain = ent - minVal;

						double delta = (log2(Math.pow(3.0, Dp) - 2.0))
								- ((Dp * ent) - (Dp0 * min_e_lw) - (Dp1 * min_e_gt));

						double weight = (log2(dataSize - 1.0) + delta)
								/ dataSize;

						if (infoGain >= weight) {
							minAttr = attr;
							mainMinVal = minVal;
							minCut = (min + decisionDistribution.higherKey(min)) / 2.0;
						}

					}

				}
			}

			if (minAttr >= 0) {
				ArrayList<DoubleData> l1 = new ArrayList<DoubleData>();
				ArrayList<DoubleData> l2 = new ArrayList<DoubleData>();

				for (int i = 0; i < table.getDataObjects().size(); i++) {
					DoubleData dd = table.getDataObjects().get(i);
					if (dd.get(minAttr) <= minCut) {
						l1.add(dd);
					} else {
						l2.add(dd);
					}
				}
				cuts.get(minAttr).add(minCut);

				discretize(new ArrayListDoubleDataTable(l1));
				discretize(new ArrayListDoubleDataTable(l2));
			}
		}
	}

	/**
	 * Method that compute logarithm (base 2) of given param.
	 */
	public static double log2(double d) {
		return Math.log(d) / Math.log(2.0);
	}

	private class Record {
		private int sum = 0;
		private HashMap<Double, Integer> decisions = new HashMap<Double, Integer>();

		public int getSum() {
			return sum;
		}

		public void setSum(int sum) {
			this.sum = sum;
		}

		public HashMap<Double, Integer> getDecisions() {
			return decisions;
		}

		public void setDecisions(HashMap<Double, Integer> decisions) {
			this.decisions = decisions;
		}

	}
	
    /**
     * Method that generate discretization based on data table.
     * 
     * @param table 		Data table to estimate the discretization.
     * @return				Discretization estimated on data table.
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

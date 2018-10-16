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


package rseslib.processing.discretization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;

/**
 * This class represents a method of discretizing continuous attributes based on
 * a minimal enthropy heuristic, static version (1993 Fayyad & Irani).
 * 
 * @author Marcin Jałmużna
 */
public class EntropyMinStaticDiscretizationProvider extends
		AbstractDiscretizationProvider {

	// Dla ka�dej warto�ci dyskretyzowanego atrybutu obliczana jest 
	// ilo�� rekord�w z t� warto�ci� oraz rozk�ad na decyzje
	private TreeMap<Double, Record> decisionDistribution;

	public EntropyMinStaticDiscretizationProvider(Properties prop) throws PropertyConfigurationException {
		super(prop);
	}
    /**
     * Creates discretization cuts for one attribute.
     * Main method of this discretization provider.
     * 
     * @param attribute				Selected attribute for discretization.
     * @param table 				Data used for estimating the best cuts.
     * @return Discretization cuts 
     */
	double[] generateCuts(int attribute, DoubleDataTable table) {
		decisionDistribution = new TreeMap<Double, Record>();
		Iterator<DoubleData> iter = table.getDataObjects().iterator();
		while (iter.hasNext()) {
			DoubleData dd = iter.next();
			Double tmp = Double.valueOf(dd.get(attribute));
			Double dec = Double.valueOf(dd.get(
					table.attributes().decision()));
			if (decisionDistribution.containsKey(tmp)) {
				Record r = decisionDistribution.get(tmp);
				r.setSum(r.getSum() + 1);
				if (r.getDecisions().containsKey(dec)) {
					r.getDecisions().put(dec, r.getDecisions().get(dec) + 1);
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

		double[] cuts = discretize(decisionDistribution.firstKey(), decisionDistribution
				.lastKey());
		if(cuts==null)
			cuts = new double[0];
		
//		for (int i = 0; i < cuts.length; i++) {
//			System.out.println("attribute: " + attribute + " cut: " + cuts[i]);
//		}
		return cuts;
	}

    /**
     * Method that generate cuts from given range;
     * 
     * @param start 	start of range.
     * @param end 		end of range.
     * @return array 	generated cuts.
     */
	private double[] discretize(Double start, Double end) {
		if ((start == null) || (end == null))
			return null;
		if (start >= end)
			return null;
		// przegladamy tabele z danymi i wyciagamy wszystkie wartosci
		// etykiety(decyzji)
		ArrayList<Double> decisions = new ArrayList<Double>();
		double dataSize = 0;
		for (Double tmp = start; (tmp!=null)&&(tmp <= end); tmp = decisionDistribution.higherKey(tmp)) {
			for (Double d : decisionDistribution.get(tmp).getDecisions().keySet()) {
				if (!decisions.contains(d)) {
					decisions.add(d);
				}
			}
			dataSize += decisionDistribution.get(tmp).getSum();
		}
		double min = decisionDistribution.firstKey();
		double min_val = Double.MAX_VALUE;
		double min_e_lw = 0;
		double min_e_gt = 0;
		int[][] min_tab = new int[decisions.size()][2];

		// zerujemy tablice
		int[][] tab = new int[decisions.size()][2];
		for (int j = 0; j < decisions.size(); j++) {
			tab[j][0] = 0;
			tab[j][1] = 0;
		}
		double lw = 0;
		double gt = dataSize;
		for (Double tmp = start; (tmp!=null)&&(tmp <= end); tmp = decisionDistribution.higherKey(tmp)) {
			for (Double d : decisionDistribution.get(tmp).getDecisions().keySet()) {
				tab[decisions.indexOf(d)][1] += decisionDistribution.get(tmp)
						.getDecisions().get(d);
			}
		}

		// przegladamy wszystkie rekordy i wybieramy ten z najmniejsza wartoscia
		// entropii
		for (Double tmp = start; tmp < end; tmp = decisionDistribution.higherKey(tmp)) {
			lw += decisionDistribution.get(tmp).getSum();
			gt -= decisionDistribution.get(tmp).getSum();

			for (Double d : decisionDistribution.get(tmp).getDecisions().keySet()) {
				tab[decisions.indexOf(d)][0] += decisionDistribution.get(tmp)
						.getDecisions().get(d);
				tab[decisions.indexOf(d)][1] -= decisionDistribution.get(tmp)
						.getDecisions().get(d);
			}

			// liczymy entropi� dla danego ciecia
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
			// uaktualniamy minimum
			if (val < min_val) {
				min_val = val; // minimalna entropia
				min = tmp; // minimum val
				min_e_lw = e_lw;
				min_e_gt = e_gt;
				min_tab = tab.clone();
			}
		}

		double Dp0 = 0;
		double Dp1 = 0;
		double Dp = decisions.size();
		double ent = 0;
		for (int i = 0; i < Dp; i++) {
			double v = ((double) (min_tab[i][0] + min_tab[i][1])) / dataSize;
			if (v > 0)
				ent -= v * log2(v);
			if (min_tab[i][0] > 0)
				Dp0++;
			if (min_tab[i][1] > 0)
				Dp1++;
		}

		double infoGain = ent - min_val;

		double delta = (log2(Math.pow(3.0, Dp) - 2.0))
				- ((Dp * ent) - (Dp0 * min_e_lw) - (Dp1 * min_e_gt));

		double weight = (log2(dataSize - 1.0) + delta) / dataSize;		
		
		if (infoGain < weight)
			return null;

		double[] ret1 = discretize(start, min);
		double[] ret2 = discretize(decisionDistribution.higherKey(min), end);

		return concatenate(ret1, ((min + decisionDistribution.higherKey(min)) / 2.0),
				ret2);

	}
    /**
     * Method that concatenate two arrays and one value.
     * 
     * @param t1 		first array to concatenate.
     * @param v 		value to put between t1 and t2.
     * @param t2 		second array to concatenate.
     * @return array 	concatenated array.
     */
	private double[] concatenate(double[] t1, double v, double[] t2) {
		int t1_length = 0;
		int t2_length = 0;
		if (t1 != null)
			t1_length = t1.length;
		if (t2 != null)
			t2_length = t2.length;

		double ret[] = new double[t1_length + t2_length + 1];
		if (t1 != null)
			System.arraycopy(t1, 0, ret, 0, t1_length);

		ret[t1_length] = v;
		if (t2 != null)
			System.arraycopy(t2, 0, ret, t1_length + 1, t2_length);

		return ret;
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
}

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;

/**
 * This class represents an algorithm that uses the chi square statistic to
 * discretize numeric attributes
 * 
 * @author Marcin Jałmużna
 */
public class ChiMergeDiscretizationProvider extends
		AbstractDiscretizationProvider {
	
	/** Property name for confidence level. */
    public static final String CONFIDENCE_LEVEL_PROPERTY_NAME = "confidenceLevelForIntervalDifference";
	/** Property name for minimal number of interval. */
    public static final String MIN_INTERVALS_PROPERTY_NAME = "minimalNumberOfIntervals";

    private int minIntervals;
	private double statisticalSignificance;
	// Dla ka�dej warto�ci dyskretyzowanego atrybutu obliczana jest
	// ilo�� rekord�w z t� warto�ci� oraz rozk�ad na decyzje
	private TreeMap<Double, Record> possibleCuts;
	// Lista warto�ci atrybutu decyzyjnego
	private ArrayList<Double> decisions;

	public ChiMergeDiscretizationProvider(Properties prop) throws PropertyConfigurationException {
		super(prop);
		minIntervals = getIntProperty(MIN_INTERVALS_PROPERTY_NAME);
		statisticalSignificance = getDoubleProperty(CONFIDENCE_LEVEL_PROPERTY_NAME);
	}

	/**
	 * Creates discretization cuts for one attribute. Main method of this
	 * discretization provider.
	 * 
	 * @param attribute
	 *            Selected attribute for discretization.
	 * @param table
	 *            Data used for estimating the best cuts.
	 * @return Discretization cuts
	 */
	double[] generateCuts(int attribute, DoubleDataTable table) {

		if (minIntervals < 2) {
			System.err.println("number_of_intervals(=" + minIntervals
					+ ") is to small.");
			return null;
		}

		decisions = new ArrayList<Double>();
		possibleCuts = new TreeMap<Double, Record>();
		// ustawiamy warto�ci possibleCuts i decisions
		for (DoubleData dd : table.getDataObjects()) {
			Double tmp = Double.valueOf(dd.get(attribute));
			Double dec = Double.valueOf(dd.get(table.attributes().decision()));
			if (possibleCuts.containsKey(tmp)) {
				Record r = possibleCuts.get(tmp);
				r.setSum(r.getSum() + 1);
				if (r.getDecisions().containsKey(dec)) {
					r.getDecisions().put(dec, r.getDecisions().get(dec) + 1);
				} else {
					r.getDecisions().put(dec, 1.0);
				}
			} else {
				Record r = new Record();
				r.setSum(1.0);
				r.setDecisions(new HashMap<Double, Double>());
				r.getDecisions().put(dec, 1.0);
				possibleCuts.put(tmp, r);
			}

			if (!decisions.contains(dec)) {
				decisions.add(dec);
			}
		}

		ArrayList<Double> keys = new ArrayList<Double>(possibleCuts.keySet());

		double min_val = 0;

		ChiSquareDistribution dis = new ChiSquareDistribution();
		double theta = dis.getDist(decisions.size() - 1,
				statisticalSignificance);

		for (Double tmp = possibleCuts.higherKey(possibleCuts.firstKey()); tmp != null; tmp = possibleCuts
				.higherKey(tmp)) {
			possibleCuts.get(tmp).chi2 = chi2(possibleCuts.get(possibleCuts
					.lowerKey(tmp)), possibleCuts.get(tmp));
		}

		// scalamy przedzia�y a� osi�gniemy jedno z ogranicze�
		while ((possibleCuts.size() > minIntervals) && (min_val < theta)) {
			Double min = null;
			min_val = Double.MAX_VALUE;
			// szukamy najlepszej pary przedzia��w do scalenia
			for (Double tmp = possibleCuts.higherKey(possibleCuts.firstKey()); tmp != null; tmp = possibleCuts
					.higherKey(tmp)) {
				double val = possibleCuts.get(tmp).chi2;
				if (val < min_val) {
					min_val = val;
					min = tmp;
				}
			}
			if (min_val < theta) {
				possibleCuts.put(min, sum(possibleCuts.get(possibleCuts
						.lowerKey(min)), possibleCuts.get(min)));
				possibleCuts.remove(possibleCuts.lowerKey(min));
				Double tmp = possibleCuts.lowerKey(min);
				if (tmp != null) {
					possibleCuts.get(min).chi2 = chi2(possibleCuts.get(tmp),
							possibleCuts.get(min));
				}
				tmp = possibleCuts.higherKey(min);
				if (tmp != null) {
					possibleCuts.get(tmp).chi2 = chi2(possibleCuts.get(min),
							possibleCuts.get(tmp));
				}
			}
		}
		/** Creating result table*/
		double[] ret = new double[possibleCuts.size() - 1];

		Iterator<Double> iter2 = possibleCuts.keySet().iterator();
		int i = 0;
		while (iter2.hasNext()) {
			Double tmp = iter2.next();
			if (iter2.hasNext()) {
				ret[i] = (tmp + keys.get(keys.indexOf(tmp) + 1)) / 2.0;
				//System.out.println("attribute: " + attribute + " cut: "
				//		+ ret[i]);
			}
			i++;
		}

		return ret;
	}

	/**
	 * Method that compute chi square statistic for two following intervals;
	 * 
	 * @param interval1
	 *            first interval
	 * @param interval2
	 *            second interval
	 * @return chi square value.
	 */
	private Double chi2(Record interval1, Record interval2) {
		Double sum = 0.0;

		Double PI1 = interval1.getSum();
		Double PI2 = interval2.getSum();
		Double PI1I2 = PI1 + PI2;

		for (Double dec : decisions) {
			Double PdI1 = interval1.getDecisions().get(dec);
			if (PdI1 == null)
				PdI1 = 0.0;

			Double PdI2 = interval2.getDecisions().get(dec);
			if (PdI2 == null)
				PdI2 = 0.0;

			Double Pd = PdI1 + PdI2;

			Double tmp = Pd / PI1I2;
			Double edI1 = PI1 * tmp;
			Double edI2 = PI2 * tmp;
			if (edI1 > 0)
				sum += Math.pow((PdI1 - edI1), 2.0) / edI1;
			if (edI2 > 0)
				sum += Math.pow((PdI2 - edI2), 2.0) / edI2;
		}

		return sum;
	}

	/**
	 * Method that join two following intervals;
	 * 
	 * @param interval1
	 *            first interval
	 * @param interval2
	 *            second interval
	 * @return joined interval.
	 */
	private Record sum(Record x, Record y) {
		y.setSum(y.getSum() + x.getSum());
		for (Double dec : x.getDecisions().keySet()) {
			if (y.getDecisions().containsKey(dec)) {
				y.getDecisions().put(dec,
						x.getDecisions().get(dec) + y.getDecisions().get(dec));
			} else {
				y.getDecisions().put(dec, x.getDecisions().get(dec));
			}
		}
		return y;
	}
	/**
	 * Subsidiary class containing informations about interval.
	 *
	 */
	private class Record {
		private Double sum = 0.0;	
		/** Chi2 value for this and next interval*/
		private Double chi2 = Double.MAX_VALUE;
		/** Decisions distribution */
		private HashMap<Double, Double> decisions = new HashMap<Double, Double>();

		public Double getSum() {
			return sum;
		}

		public void setSum(Double sum) {
			this.sum = sum;
		}

		public HashMap<Double, Double> getDecisions() {
			return decisions;
		}

		public void setDecisions(HashMap<Double, Double> decisions) {
			this.decisions = decisions;
		}

	}
}
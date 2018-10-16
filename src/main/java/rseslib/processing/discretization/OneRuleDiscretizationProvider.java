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
import java.util.Properties;
import java.util.TreeMap;

import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
/**
 * This class represents Holte's One Rule discretization algorithm that
 * discretize numeric attributes.
 * 
 * @author Marcin Jałmużna
 */
public class OneRuleDiscretizationProvider extends
		AbstractDiscretizationProvider {

	/** Property name for minimal frequency. */
    public static final String MIN_FREQUENCY_PROPERTY_NAME = "minimalFrequency";

	private int minFreq;
	
	public OneRuleDiscretizationProvider(Properties prop) throws PropertyConfigurationException {
		super(prop);
		minFreq = getIntProperty(MIN_FREQUENCY_PROPERTY_NAME);
	}
    /**
     * Creates discretization cuts for one attribute.
     * Main method of this discretization provider.
     * 
     * @param attribute				Selected attribute for discretization.
     * @param table 				Data used for estimating the best cuts.
     * @return Discretization cuts 
     */
	@Override
	double[] generateCuts(int attribute, DoubleDataTable table) {
		TreeMap<Double, HashMap<Double, Integer>> possibleCuts=new TreeMap<Double, HashMap<Double, Integer>>();
		for (DoubleData dd : table.getDataObjects()) {
			Double val=dd.get(attribute);
			Double dec = dd.get(dd.attributes().decision());
			if(possibleCuts.containsKey(val)){
				if(possibleCuts.get(val).containsKey(dec)){
					possibleCuts.get(val).put(dec, possibleCuts.get(val).get(dec)+1);
				}
				else{
					possibleCuts.get(val).put(dec, 1);
				}
			}
			else{
				HashMap<Double, Integer> tmp= new HashMap<Double, Integer>();
				tmp.put(dec, 1);
				possibleCuts.put(val, tmp);
			}
		}
		ArrayList<Double> cuts=new ArrayList<Double>();
		
		
		HashMap<Double, Integer> sum = new HashMap<Double, Integer>();
		Double decision=null;
		boolean enough=false;
		for(Double val: possibleCuts.keySet()){
			HashMap<Double, Integer> tmp = possibleCuts.get(val);			
			if(enough){
				if((tmp.keySet().size()!=1)||(!tmp.containsKey(decision))){
					cuts.add((val+possibleCuts.lowerKey(val))/2.0);
					enough=false;
					sum=tmp;	
				}
			}
			else{
				add(tmp, sum);
			}
			if(!enough){
				for(Double dec: sum.keySet()){
					if(sum.get(dec)>=minFreq){
						enough=true;
						decision=dec;
						break;
					}
				}
			}		
		}
		double ret[] = new double[cuts.size()];
		for(int i=0; i<cuts.size(); i++){
			ret[i]=cuts.get(i);
			//System.out.println("attribute: " + attribute + " cut: " + cuts.get(i));
		}
		return ret;
	}
	/** 
	 * Join of two maps.
	 * 
	 * @param x first Map
	 * @param y second Map
	 */
	private void add(HashMap<Double, Integer> x, HashMap<Double, Integer> y)
	{
		for(Double dec: x.keySet()){
			if(y.containsKey(dec)){
				y.put(dec, y.get(dec)+x.get(dec));
			}
			else{
				y.put(dec, x.get(dec));
			}
		}
	}
}

/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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


package rseslib.processing.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import rseslib.structure.data.DoubleData;

/**
 * Selects randomly generated sample of data objects.
 *
 * @author      Sebastian Stawicki, Arkadiusz Wojna
 */
public class Sampler
{
    /** Random number generator. */
    private static final Random RANDOM_GENERATOR = new Random();

    /**
     * Returns a sample of data objects
     * selected from an original collection without repetitions.
     *
     * @param data			Collection of data object to be sampled.
     * @param sampleSize	Size of the sample.
     * @return      		Sample of data objects.
     */
    public static ArrayList<DoubleData> selectWithoutRepetitions(ArrayList<DoubleData> data, int sampleSize)
    {
        ArrayList<DoubleData> sample = new ArrayList<DoubleData>(sampleSize);
        if (sampleSize < data.size())
        {
            boolean[] inSample = new boolean[data.size()];
            while (sample.size() < sampleSize)
            {
                int ind = RANDOM_GENERATOR.nextInt(data.size());
                if (!inSample[ind])
                {
                	sample.add(data.get(ind));
                    inSample[ind] = true;
                }
            }
        }
        else sample.addAll(data);
        return sample;
    }

    /**
     * Returns a sample of data objects
     * selected from an original collection with repetitions.
     *
     * @param data			Collection of data object to be sampled.
     * @param sampleSize	Size of the sample.
     * @return      		Sample of data objects.
     */
    public static ArrayList<DoubleData> selectWithRepetitions(ArrayList<DoubleData> data, int sampleSize)
    {
        ArrayList<DoubleData> sample = new ArrayList<DoubleData>(sampleSize);
        for (int obj = 0; obj < sampleSize; obj++)
        	sample.add(data.get(RANDOM_GENERATOR.nextInt(data.size())));
        return sample;
    }
    

    
    //TODO STAWICKI uzupe�ni� opis javadoc, doda� komunikaty dla rzucanych wyj�tk�w
    public static ArrayList<DoubleData> selectWithRepetitionsFromSamplesWithDistribution(
    		ArrayList<DoubleData> data, ArrayList<Double> dataDistribution, int sampleSize) {

    	if (data.size() != dataDistribution.size())
    		throw new IllegalArgumentException();
   	
    	boolean areTherePositiveValues = false;
    	double sum = 0;
    	for (int i=0; i<data.size(); i++) {
    		double weight = dataDistribution.get(i);
    		if (weight < 0)
    			throw new IllegalArgumentException();
    		if (weight > 0)
    			areTherePositiveValues = true;
    		sum += weight;
    	}

    	if (!areTherePositiveValues)
    		throw new IllegalArgumentException();
    	
        class PairDoubleInteger {
        	double d;
        	int i;
        	public PairDoubleInteger(double d, int i) {
        		this.d = d;
        		this.i = i;
        	}
        }

        Comparator<PairDoubleInteger> comparator = new Comparator<PairDoubleInteger>() {
    		public int compare(PairDoubleInteger o1, PairDoubleInteger o2) {
    			if (o1.d > o2.d)
    				return -1;
    			else if (o1.d < o2.d)
    				return 1;
    			else 
    				return 0;
    		}
        };
    	
    	ArrayList<PairDoubleInteger> weights = new ArrayList<PairDoubleInteger>(data.size());
    	for (int i=0; i<dataDistribution.size(); i++) 
    		weights.add(new PairDoubleInteger(dataDistribution.get(i)/sum, i));
    	Collections.sort(weights, comparator);
    	for (int i=1; i<weights.size(); i++)
    		weights.get(i).d += weights.get(i-1).d;
    	
    	ArrayList<PairDoubleInteger> random = new ArrayList<PairDoubleInteger>(sampleSize);
    	for (int i=0; i<sampleSize; i++)
    		random.add(new PairDoubleInteger(RANDOM_GENERATOR.nextDouble(), i));
    	Collections.sort(random, comparator);
    	ArrayList<DoubleData> result = new ArrayList<DoubleData>(sampleSize);
    	for (int i=0; i<sampleSize; i++)
    		result.add(null);
    	int sortedWeightsIndex=0;
    	for (int sortedRandomIndex=random.size()-1; sortedRandomIndex>=0; sortedRandomIndex--) {
    		while ((sortedWeightsIndex < (weights.size()-1)) && (random.get(sortedRandomIndex).d > weights.get(sortedWeightsIndex).d))
    			sortedWeightsIndex++;
    		result.set(random.get(sortedRandomIndex).i, data.get(weights.get(sortedWeightsIndex).i));
    			
    	}
    	return result;
    }
    
}

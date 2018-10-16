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


package rseslib.processing.logic;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Prime implicants generator by Michal Kurzydlowski.
 * 
 * @author Michal Kurzydlowski
 */
public class KurzydlowskiPrimeImplicantsProvider extends Configuration implements PrimeImplicantsProvider
{

	/**
	 * Class for storing best attribute in the best attribute searching function
	 */
	private class BestAttr
	{
		public int attr;
		public AttrStat stat;
		
		public BestAttr(int attr, AttrStat stat)
		{
			this.attr = attr;
			this.stat = stat;
		}
	}
	
	/**
	 * Class for storing return data from dividing cnf into two clause sets (a new clause set)
	 */
	private class Division
	{
		/** Size of the set without clauses that contained choosen attribute */
		public int newSize;
		/** Set of clauses without clauses that contained choosen attribute */
		public ArrayList<BitSet>[] newSortedCnf;
		/** Attribute statistics for the above set */
		public Map<Integer, AttrStat> newAttrStats;
		
		@SuppressWarnings("unchecked")
		public Division(int width)
		{
			newSize = 0;
    		newSortedCnf = new ArrayList[width+1];
            for (int i=0; i<=width; i++)
            {
            	newSortedCnf[i] = new ArrayList<BitSet>();
            }
    		newAttrStats = new HashMap<Integer,AttrStat>();
		}
	}
	
	/**
	 * Class for storing statistics regarding an attribute with method of comparing them
	 */
    private class AttrStat
    {
        public int oneInClause = 0;
        public int twoInClause = 0;
        public int numOfClauses;
        
        public boolean betterThan(AttrStat that)
        {
        	if (this.oneInClause > that.oneInClause)
        	{
        		return true;
        	}
        	else if (this.oneInClause < that.oneInClause)
        	{
        		return false;
        	}
        	else
        	{
        		if (this.twoInClause > that.twoInClause)
            	{
            		return true;
            	}
            	else if (this.twoInClause < that.twoInClause)
            	{
            		return false;
            	}
            	else
            	{
                	if (this.numOfClauses > that.numOfClauses)
                	{
                		return true;
                	}
                	else
                	{
                		return false;
                	}
            	}
        	}
        }
    }
    
    /**
     * Function returning the worst attribute statistic for the best attribute searching function
     */
    private AttrStat getMinStat()
    {
    	AttrStat min = new AttrStat();
    	min.oneInClause = 0;
    	min.twoInClause = 0;
    	min.numOfClauses = 0;
    	return min;
    }

	/**
     * Creates an empty initial prime implicants provider.
	 * 
	 * @param prop Properties.
	 * @throws PropertyConfigurationException
	 */
    public KurzydlowskiPrimeImplicantsProvider(Properties prop) throws PropertyConfigurationException
    {
    	super(prop);
    }

    /**
     * Generates prime implicants from positive CNF formula.
     * CNF formula is represented as a concjunction of elements stored in collection.
     * Each element of collection represents disjunction of variables stored as booleans in boolean vector.
     * This method generates possible prime implicants by sorting cnf into table grouped by the
     * lenght of the clauses, calculates the initial attribute statistics, calls method
     * generatePossiblePrimeImplicants(...) and than it removes non-prime implicants by
     * calling method removeNonPrimeImplicants(...) and finally transforms sorted cnf into a collection.
     * @param cnf CNF formula
     * @return collection of prime implicants
     * @see rseslib.processing.logic.PrimeImplicantsProvider#generatePrimeImplicants(Collection,int)
     */
    @SuppressWarnings("unchecked")
	public Collection<BitSet> generatePrimeImplicants(Collection<BitSet> cnf, int width)
    {
        ArrayList<BitSet> sortedCnf[] = new ArrayList[width+1];
        for (int i=0; i<=width; i++)
        {
        	sortedCnf[i] = new ArrayList<BitSet>();
        }
        for (BitSet formula : cnf)
        {
        	sortedCnf[formula.cardinality()].add(formula);
        }
    	Map<Integer,AttrStat> attrStats = initializeAttrStats(sortedCnf, width);
        ArrayList<BitSet> sortedPrimeImplicants[] = new ArrayList[width+1];
        for (int i=0; i<=width; i++)
        {
        	sortedPrimeImplicants[i] = new ArrayList<BitSet>();
        }
        generatePossiblePrimeImplicants(sortedCnf, cnf.size(), width, attrStats, new BitSet(), sortedPrimeImplicants);
        removeNonPrimeImplicants(sortedPrimeImplicants, width);
        Collection<BitSet> primeImplicants = new ArrayList<BitSet>();
        for (int i=1; i<=width; i++)
        {
        	primeImplicants.addAll(sortedPrimeImplicants[i]);
        }
        return primeImplicants;
    }

    /**
     * Raises attribute statistics or adds a new attribute statistic to the map if not present
     * @param attrStats map of attribute statistics
     * @param attr attribute to be corrected
     * @param level cardinality of the examined clause
     */
    private void addAttrStat(Map<Integer,AttrStat> attrStats, int attr, int level)
    {
		AttrStat attrStat = attrStats.get(attr);
		if (attrStat == null)
		{
			attrStat = new AttrStat();
			switch (level)
			{
				case 1: attrStat.oneInClause = 1; break;
				case 2: attrStat.twoInClause = 1; break;
			}
			attrStat.numOfClauses = 1;
			attrStats.put(attr, attrStat);
		}
		else
		{
			switch (level)
			{
				case 1: attrStat.oneInClause++; break;
				case 2: attrStat.twoInClause++; break;
			}
			attrStat.numOfClauses++;
		}
    }
    
    /**
     * Calculates the initial attribute statistics based on the CNF formula
     * @param sortedCnf array of sorted clauses
     * @param width number of attributes
     * @return map from attribute to its statistic
     */
    private Map<Integer,AttrStat> initializeAttrStats(ArrayList<BitSet> sortedCnf[], int width)
    {
    	Map<Integer,AttrStat> attrStats = new HashMap<Integer,AttrStat>();
    	for (int level=1; level<=width; level++)
    	{
    		for (BitSet formula : sortedCnf[level])
    		{
    			for (int b=formula.nextSetBit(0); b>=0; b=formula.nextSetBit(b+1))
    			{
    				addAttrStat(attrStats, b, level);
    			}
    		}
    	}
		return attrStats;
	}

    /**
     * Removes absorpted clauses from the sorted CNF formula and correct attribute statistics.
     * @param sortedCnf array of sorted clauses (to be changed)
     * @param width number of attributes
     * @param attrStats attribute statistics (to be corrected)
     * @return size of the corrected CNF formula
     */
    private int absorption(ArrayList<BitSet> sortedCnf[], int size, int width,
    		Map<Integer,AttrStat> attrStats)
    {
        for (int i=1; i<width; i++)
        {
            for (BitSet formula : sortedCnf[i])
            {
                for (int j=i+1; j<=width;j++)
                {
                    for (int pos=0; pos<sortedCnf[j].size(); pos++)
                    {
                        BitSet bv = (BitSet)formula.clone();
                        BitSet lbv = sortedCnf[j].get(pos);
                        bv.and(lbv);
                        if (bv.equals(formula))
                        {
                        	for (int k=lbv.nextSetBit(0); k>=0; k=lbv.nextSetBit(k+1))
                    		{
                        		AttrStat attrStat = attrStats.get(k);
                        		switch (lbv.cardinality())
                				{
                					case 1: attrStat.oneInClause--; break;
                					case 2: attrStat.twoInClause--; break;
                				}
                				attrStat.numOfClauses--;
                    		}
                        	sortedCnf[j].remove(pos);
                        	size--;
                            pos--;
                        }
                    }
                }
            }
        }
        return size;
    }
    
    /**
     * Generates possible prime implicants of the CNF formula adding them to the sorted (almost)
     * prime implicants array. Prefix is the set of attributes choosen in previous steps.
     * @param sortedCnf array of sorted clauses (to be changed)
     * @param size
     * @param width
     * @param attrStats
     * @param prefix
     * @param sortedPrimeImplicants
     */
	private void generatePossiblePrimeImplicants(ArrayList<BitSet> sortedCnf[], int size, int width,
    		Map<Integer,AttrStat> attrStats, BitSet prefix, ArrayList<BitSet> sortedPrimeImplicants[])
    {
    	if (size > 0)
    	{
    		size = absorption(sortedCnf, size, width, attrStats);
    		BestAttr bestAttr = bestAttr(attrStats);
    		attrStats.remove(bestAttr.attr);
    		BitSet newPrefix = (BitSet) prefix.clone();
    		newPrefix.set(bestAttr.attr);
    		if (bestAttr.stat.oneInClause > 0)
    		{
    			size = shortenSortedCnf(bestAttr.attr, size, width, sortedCnf, attrStats);
    			generatePossiblePrimeImplicants(sortedCnf, size, width, attrStats, newPrefix,
        				sortedPrimeImplicants);
    		}
    		else
    		{
    			Division div = divideSortedCnf(bestAttr.attr, width, sortedCnf, attrStats);
    			/** find prime implicants containg best attribute */
    			generatePossiblePrimeImplicants(sortedCnf, size, width, attrStats, prefix,
    					sortedPrimeImplicants);
    			/** find prime implicants that don't contain best attribute */
    			generatePossiblePrimeImplicants(div.newSortedCnf, div.newSize, width,
    					div.newAttrStats, newPrefix, sortedPrimeImplicants);
    		}
    	}
    	else
    	{
    		/** add possible prime implicant */
    		sortedPrimeImplicants[prefix.cardinality()].add(prefix);
    	}
    }

    /**
     * Assume that we have choosen this attribute (it was a only attribute in a clause) and
     * remove this attribute from clauses. Therefor shorten the CNF formula.
     * @param attr choosen attribute
     * @param size size of the CNF formula (to be corrected)
     * @param width number of attributes
     * @param sortedCnf array of sorted clauses (to be changed)
     * @param attrStats attribute statistics (to be corrected)
     * @return size of the shorten CNF formula
     */
    private int shortenSortedCnf(int attr, int size, int width,
			ArrayList<BitSet>[] sortedCnf, Map<Integer, AttrStat> attrStats)
    {
    	for (int level=1; level<=width; level++)
    	{
    		for (int i=0; i<sortedCnf[level].size(); i++)
    		{
    			BitSet formula = sortedCnf[level].get(i);
    			if (formula.get(attr))
    			{
    				sortedCnf[level].remove(formula);
    				size--;
    				i--;
    				formula.set(attr, false);
    				for (int b=formula.nextSetBit(0); b>=0; b=formula.nextSetBit(b+1))
    				{
    					AttrStat attrStat = attrStats.get(b);
						attrStat.numOfClauses--;
    					if (level == 2)
    					{
    						attrStat.twoInClause--;
    					}
    				}
    			}
    		}
    	}
    	return size;
	}

    /**
     * Divided the CNF formula into two sets (one assuming we have choosen this attribute and the
     * other assuming we have rejected it). First set will be stored on the original variables and
     * the second will be returned in the Division class object.
     * @param attr choosen attribute
     * @param width number of attributes
     * @param sortedCnf array of sorted clauses (to be changed)
     * @param attrStats attribute statistics (to be corrected)
     * @return Division representing newly created clause set
     */
	private Division divideSortedCnf(int attr, int width, ArrayList<BitSet>[] sortedCnf,
			Map<Integer, AttrStat> attrStats)
	{
		Division div = new Division(width);
    	for (int level=1; level<=width; level++)
    	{
    		for (int i=0; i<sortedCnf[level].size(); i++)
    		{
    			BitSet formula = sortedCnf[level].get(i);
    			if (formula.get(attr))
    			{
    				/** Remove attribute from the first set and correct statistics */
    				sortedCnf[level].remove(formula);
    				i--;
    				formula.set(attr, false);
    				switch (level)
    				{
    					case 3:
    						for (int b=formula.nextSetBit(0); b>=0; b=formula.nextSetBit(b+1))
    		    			{
    							AttrStat attrStat = attrStats.get(b);
    							attrStat.twoInClause++;
    		    			}
    						break;
    						
    					case 2:
    						AttrStat attrStat = attrStats.get(formula.nextSetBit(0));
    						attrStat.twoInClause--;
    						attrStat.oneInClause++;
    						break;
    				}
    				/**
    				 * level > 1 as shortenSortedCnf wasn't choosen (bestAttr.stat.oneInClause = 0)
    				 */
    				sortedCnf[level-1].add(formula);
    			}
    			else
    			{
    				/** leave the formula in the first set and add it to the second set */
					div.newSize++;
    				div.newSortedCnf[level].add((BitSet) formula.clone());
    				/** calculate attribute statistics for the second set */
    				for (int b=formula.nextSetBit(0); b>=0; b=formula.nextSetBit(b+1))
    				{
    					addAttrStat(div.newAttrStats, b, level);
    				}
    			}
    		}
    	}
    	return div;
	}

	/**
	 * Choose the best attribute using the attribute statistics and their comparison method
	 * @param attrStats attribute statistics
	 * @return best attribute (its number and statistics)
	 */
	private BestAttr bestAttr(Map<Integer, AttrStat> attrStats)
	{
    	int bestAttr = 0;
    	AttrStat bestStat = getMinStat();
    	for (Entry<Integer, AttrStat> entry : attrStats.entrySet())
    	{
    		AttrStat attrStat = entry.getValue();
    		if (attrStat.betterThan(bestStat))
    		{
    			bestStat = attrStat;
    			bestAttr = entry.getKey();
    		}
    	}
		return new BestAttr(bestAttr, bestStat);
	}

    /**
     * Removes non-prime implicants by less than n^2/2 checkings.
     * @param sortedCnf sorted array (by clause lenght) of prime and non-prime implicants
     * @param width number of attributes
     * @return sorted array containing only prime implicants
     */
    private void removeNonPrimeImplicants(ArrayList<BitSet>[] sortedCnf, int width)
    {
        for (int i=width-1; i>1; i--)
        {
            for (int k=0; k<sortedCnf[i].size(); k++)
            {
            	BitSet bv_over = sortedCnf[i].get(k);
                boolean true_implicant = true;
                for (int j=1; j<i && true_implicant; j++)
                {
                    for (BitSet bv_inner : sortedCnf[j])
                    {
                        BitSet new_bv = (BitSet)bv_inner.clone();
                        new_bv.and(bv_over);
                        if (new_bv.equals(bv_inner))
                        {
                        	true_implicant = false;
                        	break;
                        }
                    }
                }
                if (!true_implicant)
                {
                	sortedCnf[i].remove(bv_over);
                    k--;
                }
            }
        }
    }

}

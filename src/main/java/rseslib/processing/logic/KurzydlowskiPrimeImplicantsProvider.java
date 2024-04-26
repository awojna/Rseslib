/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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
import java.util.Map.Entry;

/**
 * Generator of all prime implicants
 * from a conjunction of clauses (CNF),
 * where each clause is a disjunction of positive literals
 * over a set of boolean variables by Michal Kurzydlowski.
 * Clauses are limited to positive literals only,
 * this implementation does not allow negative literals.
 * 
 * @author Michal Kurzydlowski
 */
public class KurzydlowskiPrimeImplicantsProvider implements PrimeImplicantsProvider
{

	/**
	 * Class for storing statistics of a given variable with a comparison function.
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
	 * Class for storing best variable in the best variable searching function.
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
	 * Class for storing the subset of clauses not containing a given variable after splitting a set of clauses.
	 */
	private class Division
	{
		/** Size of the subset of clauses not containing a given variable. */
		public int newSize;
		/** Subset of clauses not containing a given variable. */
		public ArrayList<BitSet>[] newSortedCnf;
		/** Variable statistics for the represented subset of clauses. */
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
     * Creates an instance of prime implicants provider.
	 */
    public KurzydlowskiPrimeImplicantsProvider()
    {
    }

    /**
     * Generates all prime implicants from a positive CNF formula.
     * All clauses of the formula are provided as a collection.
     * Each disjunctive clause is represented by a BitSet object,
     * get(i) returns true if and only if
     * the positive literal of the i-th variable occurs in this clause.
     * The method returns collection of prime implicants of the formula.
     * Each prime implicant is represented by a BitSet object,
     * get(i) returns true if and only if
     * the positive literal of the i-th variable occurs in the implicant.
     * The method groups the clauses by length,
     * calculates the initial statistics of the variables
     * and uses the method generatePossiblePrimeImplicants(...).
     * Next, it removes non-prime implicants using the method removeNonPrimeImplicants(...),
     * and returns the remaining implicants as a collection.
     * 
     * @param cnf 	CNF formula.
     * @param width	Number of boolean variables used in the CNF formula.
     * @return 		Collection of all prime implicants.
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
     * Calculates the initial statistics of the variables occurring in a CNF formula.
     * 
     * @param sortedCnf	Clauses grouped by length.
     * @param width 	Number of boolean variables used in the CNF formula.
     * @return 			Mapping between the variables and their statistics.
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
     * Updates the statistics for a given variable.
     * 
     * @param attrStats Mapping between the variables and their statistics.
     * @param attr		Variable to be updated.
     * @param level		Length of the clause.
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
     * Generates potential prime implicants of a CNF formula
     * and adds them to the array of prime implicant candidates grouped by length.
     * 
     * @param sortedCnf				Clauses grouped by length (to be updated).
     * @param size					Number of input clauses.
     * @param width					Number of boolean variables used in the CNF formula.
     * @param attrStats				Mapping between the variables and their statistics.
     * @param prefix				Variables selected in the previous steps.
     * @param sortedPrimeImplicants	Potential prime implicants grouped by length.
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
     * Removes absorbed clauses from clauses grouped by length and updates variable statistics.
     * 
     * @param sortedCnf	Clauses grouped by length (to be updated).
     * @param size		Number of clauses.
     * @param width		Number of boolean variables used in the CNF formula.
     * @param attrStats	Mapping between the variables and their statistics.
     * @return			Number of the clauses remaining after absorption.
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
	 * Select the best variable based on the statistics of the variables.
	 * 
     * @param attrStats	Mapping between the variables and their statistics.
	 * @return			Best variable (its index and statistics)
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
     * Function returning empty statistics.
     * 
     * @return	Empty statistics.
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
     * Removes clauses containing a given variable from an array of clauses grouped by length.
     * 
     * @param attr		Index of the variable.
     * @param size		Number of all input clauses. 
     * @param width		Number of boolean variables used in the CNF formula.
     * @param sortedCnf	Clauses grouped by length (to be updated).
     * @param attrStats	Mapping between the variables and their statistics.
     * @return			Number of the remaining clauses.
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
     * Separates clauses grouped by length into two subsets:
     * the one with the clauses containing a given variable
     * and the other one with the clauses without the variable.
     * The clauses containing the variable remain in the input array
     * and the clauses without the variable are removed from the input array
     * and returned as a Division object.
     * 
     * @param attr		Index of the variable.
     * @param width		Number of boolean variables used in the CNF formula.
     * @param sortedCnf	Clauses grouped by length, only those containing the variable are left.
     * @param attrStats	Mapping between the variables and their statistics.
     * @return			Clauses without the variable.
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
     * Removes non-prime implicants by less than n^2/2 checks.
     * 
     * @param sortedCnf	Prime and non-prime implicants grouped by length.
     * @param width		Number of boolean variables occurring in the implicants.
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

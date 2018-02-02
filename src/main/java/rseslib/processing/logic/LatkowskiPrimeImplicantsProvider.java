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


package rseslib.processing.logic;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Prime implicants generator by Rafal Latkowski.
 * 
 * @author Rafal Latkowski
 */
public class LatkowskiPrimeImplicantsProvider extends Configuration implements PrimeImplicantsProvider
{
    /**
     * ImplicantContext is an auxiliary class for storing
     * context of possible implicant into a map.
     */
    class ImplicantContext
    {
        public BitSet forbidden;
        public Collection<BitSet> rest_cnfs;
    }

    /** Name of property indicating whether clauses absorption is used for optimization. */
	private static String CLAUSES_ABSORPTION_PROPERTY_NAME = "clausesAbsorption";
    /** Name of property indicating whether one-literal clauses are used for optimization. */
	private static String ONE_LITERAL_CLAUSES_OPT_PROPERTY_NAME = "oneLiteralClausesOptimization";
	
    /** Switch indicating whether clauses absorption is used for optimization at the beginning. */
	private boolean m_bClausesAbsorption; 
    /** Switch indicating whether one-literal clauses are used for optimization. */
	private boolean m_bOneLiteralClausesOptimization; 

	/**
     * Creates an empty initial prime implicants provider.
	 * 
	 * @param prop		Properties.
	 * @throws PropertyConfigurationException
	 */
    public LatkowskiPrimeImplicantsProvider(Properties prop) throws PropertyConfigurationException
    {
    	super(prop);
    	m_bClausesAbsorption = getBoolProperty(CLAUSES_ABSORPTION_PROPERTY_NAME);
    	m_bOneLiteralClausesOptimization = getBoolProperty(ONE_LITERAL_CLAUSES_OPT_PROPERTY_NAME);
    }

    /**
     * Generates prime implicants from positive cnf formula.
     * CNF formula is represented as a concjunction of elements stored in collection.
     * Each element of collection represents disjunction of variables stored as booleans in boolean vector.
     * This method generates possible prime implicants by calling method
     * generatePossiblePrimeImplicants(Collection, int) and than it removes
     * some rare non-prime implicants by calling method removeNonPrimeImplicants(Map, int).
     * @param cnf CNF formula
     * @return collection of prime implicants
     * @see rseslib.processing.logic.PrimeImplicantsProvider#generatePrimeImplicants(Collection,int)
     * @see #generatePossiblePrimeImplicants(Collection, int)
     * @see #removeNonPrimeImplicants(Map, int)
     */
    public Collection<BitSet> generatePrimeImplicants(Collection<BitSet> cnf,int width)
    {
    	if (m_bClausesAbsorption) cnf = absorption(cnf,width);
        Map<BitSet,Integer> possible_prime_implicants 
            = generatePossiblePrimeImplicants(cnf,width);
        Collection<BitSet> prime_implicants 
            = removeNonPrimeImplicants(possible_prime_implicants,width);
        possible_prime_implicants.clear();
        return prime_implicants;
    }

    /**
     * Removes absorpted clauses.
     * 
     * @param cnf
     * @param width
     * @return
     */
    private Collection<BitSet> absorption(Collection<BitSet> cnf,int width)
    {
        ArrayList<BitSet> sorted_cnf[] = new ArrayList[width+1];
        for (int i=0;i<=width;i++)
            sorted_cnf[i]=new ArrayList<BitSet>();
        ArrayList<BitSet> finallist = new ArrayList<BitSet>();
        for (BitSet formula : cnf)
        {
            int level=formula.cardinality();
            sorted_cnf[level].add(formula);
            //if (level==0)
                //System.out.println("level="+level+" formula="+formula.toString());
        }
        //if (newcnf[0].size()>0) throw new RuntimeException("Zerowy cnf? Miala byc decyzja uogolniona.");
        for (int i=0;i<width;i++)
        {
            for (BitSet formula : sorted_cnf[i])
            {
                for (int j=i+1;j<=width;j++)
                {
                    //Iterator<BitSet> iter = sorted_cnf[j].iterator();
                    for (int pos=0;pos<sorted_cnf[j].size();pos++)
                    {
                        BitSet bv = (BitSet)formula.clone();
                        bv.and(sorted_cnf[j].get(pos));
                        if (bv.equals(formula))
                        {
                            sorted_cnf[j].remove(pos);
                            pos--;
                        }
                    }
                }
            }
            finallist.addAll(sorted_cnf[i]);
        }

/*        System.out.println("Input="+cnf.size()+" output="+finallist.size());
        System.out.println("Input");
        for (BitSet f : cnf)
            System.out.println(" "+f.toString());
        System.out.println("Output");
        for (BitSet f : finallist)
            System.out.println(" "+f.toString());        
        System.exit(0);*/
        return finallist;
    }
    
    /**
     * Heuristic method for generating prime implicants with blocking variables.
     * Unfortunatelly some implicants are not prime.
     * 
     * @param cnf CNF formula
     * @param width number of variables in each boolean vector
     * @return collection of possible prime implicants
     */
    private Map<BitSet,Integer> generatePossiblePrimeImplicants(Collection<BitSet> cnf,int width)
    {
        if (cnf==null||cnf.isEmpty()) return null;
        HashMap<BitSet,Integer> final_prime_implicants = new HashMap<BitSet,Integer>();
        HashMap<BitSet,ImplicantContext> primes = new HashMap<BitSet,ImplicantContext>();

        BitSet obligatory = new BitSet();
        if (m_bOneLiteralClausesOptimization)
        	for (BitSet clause : cnf)
        	{
        		if (clause.cardinality()==1)
        			obligatory.or(clause);
        	}

        if (m_bOneLiteralClausesOptimization && !obligatory.isEmpty())
        {
            Collection<BitSet> supported;                
            Collection<BitSet> rest;
            
            supported = new LinkedList<BitSet>();
            rest = new LinkedList<BitSet>();
            
            for (BitSet bv : cnf)
            {
                BitSet test = (BitSet)obligatory.clone();
                test.and(bv);
                if (test.isEmpty()) rest.add(bv);
                else supported.add(bv);
            }
            
            if (!supported.isEmpty())
            {            
                if (rest.isEmpty()) final_prime_implicants.put(obligatory,1);
                else
                {
                    ImplicantContext pc = new ImplicantContext();
                    pc.forbidden = obligatory;
                    pc.rest_cnfs = rest;
                    primes.put(obligatory,pc);
                }
                supported.clear();
            }
        }
        else
        {
            for (int a = 0 ; a < width ; a++)
            {
            
                Collection<BitSet> supported;                
                Collection<BitSet> rest;
                
                supported = new LinkedList<BitSet>();
                rest = new LinkedList<BitSet>();
                
                for (BitSet bv : cnf)
                {
                    BitSet test = new BitSet(width);
                    test.set(a);
                    test.and(bv);
                    if (test.isEmpty()) rest.add(bv);
                    else supported.add(bv);
                }
                
                if (!supported.isEmpty())
                {
                    BitSet prime = new BitSet(width);
                    prime.set(a);
                
                    if (rest.isEmpty()) final_prime_implicants.put(prime,1);
                    else
                    {
                        BitSet forbidden = null;
                        for (BitSet supp_cnf : supported)
                        {
                            if (forbidden==null)
                                forbidden = (BitSet)supp_cnf.clone();
                            else
                                forbidden.and(supp_cnf);
                        }
                        forbidden.set(a);
                        ImplicantContext pc = new ImplicantContext();
                        pc.forbidden = forbidden;
                        pc.rest_cnfs = rest;
                        primes.put(prime,pc);
                    }
                    supported.clear();
                }
            }
        }
                    
        int cnt=1;
        while (!primes.isEmpty())
        {
            cnt++;
            HashMap<BitSet,ImplicantContext> new_primes = new HashMap<BitSet,ImplicantContext>();
            for (Entry<BitSet,ImplicantContext> entry : primes.entrySet())
            {
                BitSet forbidden = entry.getValue().forbidden;
                BitSet prime = entry.getKey();
                for (int a=0 ; a < width ; a++)
                if (!forbidden.get(a))
                {
                    Collection<BitSet> new_supported;                
                    Collection<BitSet> new_rest;
                    new_supported = new LinkedList<BitSet>();
                    new_rest = new LinkedList<BitSet>();
                    for (BitSet bv : entry.getValue().rest_cnfs)
                    {
                        BitSet test = (BitSet)prime.clone();
                        test.set(a);
                        test.and(bv);
                        if (test.isEmpty()) new_rest.add(bv);
                        else new_supported.add(bv);
                    }
                    if (!new_supported.isEmpty())
                    {
                        BitSet new_prime = (BitSet)prime.clone();
                        new_prime.set(a);
                        if (new_rest.isEmpty()) final_prime_implicants.put(new_prime,cnt);
                        else
                        {
                            BitSet new_forbidden = null;
                            for (BitSet supp_cnf : new_supported)
                            {
                                if (new_forbidden==null) 
                                    new_forbidden = (BitSet)supp_cnf.clone();
                                else 
                                    new_forbidden.and(supp_cnf);
                            }
                            new_forbidden.or(forbidden);
                            new_forbidden.set(a);
                            ImplicantContext new_pc = new ImplicantContext();
                            new_pc.forbidden = new_forbidden;
                            new_pc.rest_cnfs = new_rest;
                            new_primes.put(new_prime,new_pc);
                        }
                        new_supported.clear();
                    }
                }
                entry.getValue().rest_cnfs.clear();
            }
            primes.clear();
            primes=new_primes;
        }        
        
        return final_prime_implicants;
    }

    /**
     * Removes non-prime implicants by less than n^2/2 checkings.
     * 
     * @param possible_prime_implicants collection of prime and non-prime implicants
     * @param width number of variables in each boolean vector
     * @return collection of prime implicants
     */
    private Collection<BitSet> removeNonPrimeImplicants(Map<BitSet,Integer> possible_prime_implicants,int width)
    {
        LinkedList<BitSet> verified_prime_implicants = new LinkedList<BitSet>();
        LinkedList<BitSet>[] table = new LinkedList[width];
        for (int i=0;i<width;i++)
            table[i] = new LinkedList<BitSet>();
        for (Entry<BitSet,Integer> entry : possible_prime_implicants.entrySet())
            table[entry.getValue()].add(entry.getKey());
                
        for (int i=width-1;i>1;i--)
        {
            for (BitSet bv_over : table[i])
            {
                boolean true_implicant=true;
                for (int j=1;j<i&&true_implicant;j++)
                {
                    for (BitSet bv_inner : table[j])
                    {
                        BitSet new_bv = (BitSet)bv_inner.clone();
                        new_bv.and(bv_over);
                        if (new_bv.equals(bv_inner)) true_implicant=false;
                    }
                }
                if (true_implicant) verified_prime_implicants.add(bv_over);
            }
            table[i].clear();
        }
        for (BitSet bv : table[1])
            verified_prime_implicants.add(bv);
        return verified_prime_implicants;
    }
}

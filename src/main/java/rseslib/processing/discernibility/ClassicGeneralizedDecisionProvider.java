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


package rseslib.processing.discernibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import rseslib.structure.data.DoubleData;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.indiscernibility.SymmetricSimilarityIndiscernibility;
import rseslib.structure.table.DoubleDataTable;

/**
 * Implementation of the generalized decision resolving inconsistencies in the data tables
 * with objects that have different decisions and equal values on all conditional attributes.
 * The objects having the same values of the conditional attributes
 * get the same generalized decision represented by the set of all decisions
 * among the objects with the same attribute values. 
 *
 * @author Rafal Latkowski
 */
public class ClassicGeneralizedDecisionProvider implements GeneralizedDecisionProvider
{
	/** Data table used to calculate the generalized decision. */
    DoubleDataTable m_data;
    /** Indiscernibility relation type for missing values. */
    Indiscernibility m_indiscernibility;
    /** Mapping between objects and their generalized decisions. */
    HashMap<DoubleData, Integer> m_mapObjectToDecision;
    /** Collection of all generalized decision found in the data table represented as the sets of original decisions. */
    ArrayList<HashSet<Double>> m_arrGeneralizedDecisionDict;
    
    /**
     * Constructor calculates the generalized decisions in a given data table.
     * 
     * @param data				Data table used to calculate the generalized decisions.
     * @param indiscernibility	Indiscernibility relation type for missing values.
     */
    public ClassicGeneralizedDecisionProvider(DoubleDataTable data, Indiscernibility indiscernibility)
    {
        m_data=data;
        m_indiscernibility=indiscernibility;
        generateGeneralizedDecisionMapping();
        //debugMapping();
    }

    /**
     * Constructor calculates the generalized decisions in a given data table.
     * It uses the symmetric similarity as the default indiscernibility relation type for missing values.
     * 
     * @param data				Data table used to calculate the generalized decisions.
     */
    public ClassicGeneralizedDecisionProvider(DoubleDataTable data)
    {
        m_data=data;
        m_indiscernibility=new SymmetricSimilarityIndiscernibility();
        generateGeneralizedDecisionMapping();
        //debugMapping();
    }
    
    /**
     * Calculates the generalized decisions in the table provided to the constructor.
     */
    void generateGeneralizedDecisionMapping()
    {
        m_mapObjectToDecision = new HashMap<DoubleData,Integer>();
        m_arrGeneralizedDecisionDict = new ArrayList<HashSet<Double>>();
        HashMap<HashSet<Double>,Integer> sequence = new HashMap<HashSet<Double>,Integer>();
        int sequence_val=0;
        int decision=m_data.attributes().decision();
        for (DoubleData object1 : m_data.getDataObjects())
        {
            double dec1 = object1.get(decision);
            HashSet<Double> decset = new HashSet<Double>();
            decset.add(dec1);
            for (DoubleData object2 : m_data.getDataObjects())
            {
                if (m_indiscernibility.similar(object1,object2))
                {
                    double dec2=object2.get(decision);
                    if (dec1!=dec2) decset.add(dec2);
                }
            }
            int val;
            if (sequence.containsKey(decset))
            {
                val = sequence.get(decset);
            }
            else
            {
                val = sequence_val;
                sequence.put(decset,val);
                m_arrGeneralizedDecisionDict.add(decset);
                sequence_val++;
            }
            m_mapObjectToDecision.put(object1,val);
        }
    }
    
    /**
     * Prints the objects and their generalized decisions
     * from the table provided to the constructor
     * to the standard output.
     */
    void debugMapping()
    {
        for (DoubleData object1 : m_data.getDataObjects())
        {
            System.out.println(object1.toString()+" has ("+m_mapObjectToDecision.get(object1)+")"+getDecisionForObject(object1));
        }
    }
    
	/**
	 * Returns true if two objects have the same generalized decision.
	 * 
	 * @param object1	First object to be compared.
	 * @param object2	Second object to be compared.
	 * @return			True if two objects have the same generalized decision.
	 */
    public boolean haveTheSameDecision(DoubleData object1, DoubleData object2)
    {
        return m_mapObjectToDecision.get(object1)==m_mapObjectToDecision.get(object2);
    }
    
    /**
     * Returns a string representing the generalized decision for a given object.
     * 
     * @param object	Object for which a generalized decision is calculated.
     * @return			Generalized decision for the object.
     */
    public String getDecisionForObject(DoubleData object)
    {
        int decidx = m_mapObjectToDecision.get(object);
        HashSet<Double> decset = m_arrGeneralizedDecisionDict.get(decidx);
        return decset.toString();
    }
}

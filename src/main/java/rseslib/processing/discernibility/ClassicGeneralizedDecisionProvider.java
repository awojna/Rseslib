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


package rseslib.processing.discernibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import rseslib.structure.data.DoubleData;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.indiscernibility.SymmetricSimilarityIndiscernibility;
import rseslib.structure.table.DoubleDataTable;

/**
 * @author Rafal Latkowski
 *
 */
public class ClassicGeneralizedDecisionProvider implements GeneralizedDecisionProvider
{
    DoubleDataTable m_data;
    Indiscernibility m_indiscernibility;
    HashMap<DoubleData,Integer> m_mapObjectToDecision;
    ArrayList<HashSet<Double>> m_arrGeneralizedDecisionDict;
    /**
     * 
     */
    public ClassicGeneralizedDecisionProvider(DoubleDataTable data,Indiscernibility indiscernibility)
    {
        m_data=data;
        m_indiscernibility=indiscernibility;
        generateGeneralizedDecisionMapping();
        //debugMapping();
    }

    public ClassicGeneralizedDecisionProvider(DoubleDataTable data)
    {
        m_data=data;
        m_indiscernibility=new SymmetricSimilarityIndiscernibility();
        generateGeneralizedDecisionMapping();
        //debugMapping();
    }
    
    void debugMapping()
    {
        for (DoubleData object1 : m_data.getDataObjects())
        {
            System.out.println(object1.toString()+" has ("+m_mapObjectToDecision.get(object1)+")"+getDecisionForObject(object1));
        }
    }
    
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
    
    public boolean haveTheSameDecision(DoubleData object1,DoubleData object2)
    {
        return m_mapObjectToDecision.get(object1)==m_mapObjectToDecision.get(object2);
    }
    
    public String getDecisionForObject(DoubleData object)
    {
        int decidx = m_mapObjectToDecision.get(object);
        HashSet<Double> decset = m_arrGeneralizedDecisionDict.get(decidx);
        return decset.toString();
    }
}

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
public class TransitiveClosureGeneralizedDecisionProvider implements GeneralizedDecisionProvider
{
    DoubleDataTable m_data;
    Indiscernibility m_indiscernibility;
    HashMap<DoubleData,Integer> m_mapObjectToDecision;
    ArrayList<HashSet<Double>> m_arrGeneralizedDecisionDict;

    DoubleData m_dataArray[];
    int m_setStructure[];
    int m_setRank[];
    
    /**
     * 
     */
    public TransitiveClosureGeneralizedDecisionProvider(DoubleDataTable data,Indiscernibility indiscernibility)
    {
        m_data=data;
        m_indiscernibility=indiscernibility;
        generateGeneralizedDecisionMapping();
        //debugMapping();
    }

    public TransitiveClosureGeneralizedDecisionProvider(DoubleDataTable data)
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
        /* prepare cluster structure */
        m_dataArray = new DoubleData[m_data.noOfObjects()];
        sets_Init(m_dataArray.length);
        int pos=0;
        for (DoubleData obj : m_data.getDataObjects())
        {
            m_dataArray[pos]=obj; pos++;
        }

        int decision=m_data.attributes().decision();

        /* do pre-clustering */
        for (int pos1=0;pos1<m_dataArray.length;pos1++)
            for (int pos2=0;pos2<m_dataArray.length;pos2++)
                if (pos1!=pos2)
                if (m_indiscernibility.similar(m_dataArray[pos1],m_dataArray[pos2]))
                {
                    sets_Join(pos1,pos2);
                }

        /* fix clustering structure */
        sets_Finalize();
        //System.out.println("DEBUG: Transitive closure iterations: "+cnt);

        /* calculate number of clusters */
        int max_cluster=0;
        for (int val : m_setStructure)
            if (val>max_cluster) max_cluster=val;
        max_cluster++;
        //System.out.println("DEBUG: Transitive closure clusters: "+max_cluster);
        
        /* create multi-decision sets */
        HashSet<Double> tempDict[] = new HashSet[max_cluster];
        for (pos=0;pos<m_dataArray.length;pos++)
        {
            int val=m_setStructure[pos];
            HashSet<Double> decset = tempDict[val];
            if (decset==null)
            {
                decset = new HashSet<Double>();
                tempDict[val]=decset;
            }
            decset.add(m_dataArray[pos].get(decision));
        }
        /* deduplicate multi-decision sets */
        int sequence_val=0;
        HashMap<HashSet<Double>,Integer> sequence = new HashMap<HashSet<Double>,Integer>();
        m_arrGeneralizedDecisionDict = new ArrayList<HashSet<Double>>();
        for (HashSet<Double> decset : tempDict)
        {
            if (decset!=null && !sequence.containsKey(decset))
            {
                sequence.put(decset, sequence_val);
                m_arrGeneralizedDecisionDict.add(decset);
                sequence_val++;
            }
        }

        /* create standard mappings */
        m_mapObjectToDecision = new HashMap<DoubleData,Integer>();
        for (pos=0;pos<m_dataArray.length;pos++)
        {
            int gen_dec_seq = sequence.get(tempDict[m_setStructure[pos]]);
            m_mapObjectToDecision.put(m_dataArray[pos], gen_dec_seq);
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
    
    
    void sets_Init(int size)
    {
        m_setStructure = new int[size];
        m_setRank = new int[size];
        for (int pos=0;pos<m_setStructure.length;pos++)
        {
            m_setStructure[pos]=pos;
            m_setRank[pos]=0;
        }
    }
    
    void sets_Join(int s1,int s2)
    {
        sets_Link(sets_FindSet(s1),sets_FindSet(s2));
    }
    
    int sets_FindSet(int s)
    {
        if (m_setStructure[s]!=s)
            m_setStructure[s]=sets_FindSet(m_setStructure[s]);
        return m_setStructure[s];
    }
    
    void sets_Link(int s1,int s2)
    {
        if (m_setRank[s1]>m_setRank[s2])
        {
            m_setStructure[s2]=s1;
        }
        else
        {
            m_setStructure[s1]=s2;
            if (m_setRank[s1]==m_setRank[s2])
            {
                m_setRank[s2]++;
            }
        }
    }
    
    void sets_Finalize()
    {
        for (int i=0;i<m_setStructure.length;i++)
            sets_FindSet(i);
    }

}

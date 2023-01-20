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


package rseslib.processing.filtering;

import java.util.ArrayList;
import java.util.Collection;

import rseslib.processing.searching.metric.VicinityProvider;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.metric.Neighbour;

/**
 * The filter selecting all objects that are correctly classified by a case base
 * with majority voting k nearest neighbours classification.
 *
 * @author      Arkadiusz Wojna
 */
public class WilsonEditingFilter implements Filter
{
    /** The provider of nearest neighbours from the given case base. */
    VicinityProvider m_VicinityProvider;
    /** The decision attribute. */
    NominalAttribute m_DecisionAttribute;
    /** The number of neighbours voting in classification process. */
    int m_nNumberOfEditingNeighbours;
    /** The parameter k. */
    private int m_nK;

    /**
     * Constructor.
     *
     * @param vicinProv             Provider of nearest neighbours from the given case base.
     * @param decAttr               Decision attribute.
     * @param noOfEditingNeighbours Number of neighbours voting in classification process.
     */
    public WilsonEditingFilter(VicinityProvider vicinProv, NominalAttribute decAttr, int noOfEditingNeighbours, int k)
    {
        m_VicinityProvider = vicinProv;
        m_DecisionAttribute = decAttr;
        m_nNumberOfEditingNeighbours = noOfEditingNeighbours;
        m_nK = k;
    }

    /**
     * Returns a collection of data objects
     * selected from an original collection.
     *
     * @param dataColl Collection of data object to be filtered.
     * @return         Filtered collection of data objects.
     */
    public Collection<DoubleData> select(Collection<DoubleData> dataColl)
    {
        int[] decDistr = new int[m_DecisionAttribute.noOfValues()];
        ArrayList<DoubleData> editedColl = new ArrayList<DoubleData>();
        for (DoubleData obj : dataColl)
        {
            DoubleDataWithDecision dObj = (DoubleDataWithDecision)obj;
            Neighbour[] neighbours = m_VicinityProvider.getVicinity(dObj, m_nK);
            for (int d = 0; d < decDistr.length; d++) decDistr[d] = 0;
            for (int n = 1; n <= m_nNumberOfEditingNeighbours && n < neighbours.length; n++)
                decDistr[m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision())]++;
            int majorityDec = 0;
            for (int d = 1; d < decDistr.length; d++)
                if (decDistr[d] > decDistr[majorityDec]) majorityDec = d;
            if (m_DecisionAttribute.globalValueCode(majorityDec) == dObj.getDecision()) editedColl.add(dObj);
        }
        return editedColl;
    }
}

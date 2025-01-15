/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.data.NumberedDoubleDataObject;
import rseslib.structure.metric.Neighbour;

/**
 * The filter selecting all objects that have
 * at least as many other data objects classified correctly
 * as  classfied uncorrectly.
 *
 * @author      Arkadiusz Wojna
 */
public class RTEditingFilter implements Filter
{
    /** The provider of nearest neighbours from the given case base. */
    VicinityProvider m_VicinityProvider;
    /** The number of data objects in the original data set. */
    int m_nNoOfObjects;
    /** The parameter k. */
    private int m_nK;

    /**
     * Constructor.
     *
     * @param vicinProv             Provider of nearest neighbours from the given case base.
     * @param noOfObjects           Number of data objects in the original data set.
     */
    public RTEditingFilter(VicinityProvider vicinProv, int noOfObjects, int k)
    {
        m_VicinityProvider = vicinProv;
        m_nNoOfObjects = noOfObjects;
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
        int[] balanceArray = new int[m_nNoOfObjects];
        for (DoubleData obj : dataColl)
        {
            DoubleDataWithDecision dObj = (DoubleDataWithDecision)obj;
            Neighbour[] neighbours = m_VicinityProvider.getVicinity(dObj, m_nK);
            if (neighbours[1].neighbour().getDecision()==dObj.getDecision()
                && neighbours[2].neighbour().getDecision()!=dObj.getDecision())
                balanceArray[((NumberedDoubleDataObject)neighbours[1].neighbour()).getNumber()]++;
            if (neighbours[1].neighbour().getDecision()!=dObj.getDecision()
                && neighbours[2].neighbour().getDecision()==dObj.getDecision())
                balanceArray[((NumberedDoubleDataObject)neighbours[1].neighbour()).getNumber()]--;
        }
        ArrayList<DoubleData> editedColl = new ArrayList<DoubleData>();
        for (DoubleData dObj : dataColl)
            if (balanceArray[((NumberedDoubleDataObject)dObj).getNumber()] >= 0) editedColl.add(dObj);
        return editedColl;
    }
}

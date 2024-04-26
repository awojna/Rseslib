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


package rseslib.structure.table;

import java.util.ArrayList;
import java.util.Arrays;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.NumericAttributeComparator;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.vector.Vector;
import rseslib.system.Report;

/**
 * Extended numeric attribute information.
 * It contains common decision distributions calculated
 * from value vicinities. Vicinities are calculated
 * for the whole range of numeric values found
 * in a data set.
 *
 * @author      Arkadiusz Wojna
 */
public class VicinityDecisionDistributionsForNumerical
{
    /**
     * Decision vectors for succesive value ranges.
     * Position 0 contains decision vector for the null value.
     */
    private IntervalWithDecisionDistribution[] m_DecDistributions = null;

    /**
     * Constructor extracts decision vectors
     * for succesive value ranges.
     *
     * @param objects Data set that decision vectors for succesive value ranges
     *                are calculated from.
     * @param index              Attribute index.
     * @param minDistrSampleSize The minimal size of the value vicinity taken for computing decision distribution.
     */
    public VicinityDecisionDistributionsForNumerical(DoubleDataTable objects, int index, int minDistrSampleSize)
    {
        NominalAttribute decAttr = objects.attributes().nominalDecisionAttribute();
        ArrayList<IntervalWithDecisionDistribution> intervalDistributions = new ArrayList<IntervalWithDecisionDistribution>();
        Vector decDistributionForNull = new Vector(objects.attributes().nominalDecisionAttribute().noOfValues());
        // podzial na obiekty z wartosciami rowna i rozna od null
        ArrayList<DoubleDataWithDecision> nonNullObjects = new ArrayList<DoubleDataWithDecision>();
        for (DoubleData dObj : objects.getDataObjects())
        {
            if (Double.isNaN(dObj.get(index))) decDistributionForNull.increment(decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision()));
            else nonNullObjects.add((DoubleDataWithDecision)dObj);
        }
        if (nonNullObjects.size() == objects.noOfObjects())
            for (int d = 0; d < objects.getDecisionDistribution().length; d++)
                decDistributionForNull.set(d, objects.getDecisionDistribution()[d]);
        decDistributionForNull.normalizeWithCityNorm();
        intervalDistributions.add(new IntervalWithDecisionDistribution(Double.NaN, Double.NaN, true, true, decDistributionForNull));
        // sortowanie obiektow wedlug wartosci atrybutu
        DoubleDataWithDecision[] objectTable = (DoubleDataWithDecision[])nonNullObjects.toArray(new DoubleDataWithDecision[0]);
        Arrays.sort(objectTable, new NumericAttributeComparator(index));
        // wyliczanie wektorow rozkladu decyzji
        IntervalWithDecisionDistribution prevInterval = null;
        Vector decDistr = new Vector(objects.attributes().nominalDecisionAttribute().noOfValues());
        int left = 0, nextLeft = 0, right = 0;
        for (nextLeft++; nextLeft < objectTable.length && objectTable[nextLeft-1].get(index)==objectTable[nextLeft].get(index); nextLeft++);
        while (right < objectTable.length)
        {
            // ustaiwenie skrajnych obiektow dla nowego przedzialu
            boolean stretched = true;
            if (right-nextLeft < minDistrSampleSize)
                do decDistr.increment(decAttr.localValueCode(objectTable[right++].getDecision()));
                while (right < objectTable.length && (right-left<minDistrSampleSize || objectTable[right-1].get(index)==objectTable[right].get(index)));
            else
            {
                for (; left < nextLeft; left++) decDistr.decrement(decAttr.localValueCode(objectTable[left].getDecision()));
                for (nextLeft++; nextLeft < right && objectTable[nextLeft-1].get(index)==objectTable[nextLeft].get(index); nextLeft++);
                stretched = false;
            }
            // wyliczenie prawego konca przedzialu
            double newRight = (objectTable[left].get(index)+objectTable[right-1].get(index))/2.0;
            if (right==objectTable.length) newRight = objectTable[right-1].get(index);
            // wyliczenie nowego rozkladu
            Vector normDecDistr = new Vector(decDistr);
            normDecDistr.normalizeWithCityNorm();
            if (prevInterval!=null && normDecDistr.equals(prevInterval.getDecDistribution())) prevInterval.setRight(newRight);
            else
            {
                double newLeft = newRight;
                if (prevInterval!=null)
                    if (stretched)
                    {
                        // rozciagniecie poprzedniego przedzialu
                        prevInterval.setRight(newLeft);
                        prevInterval.setRightOpen();
                    }
                    else newLeft = prevInterval.getRight();
                else newLeft = objectTable[0].get(index);
                    // utworzenie nowego przedzialu
                prevInterval = new IntervalWithDecisionDistribution(newLeft, newRight, ((newLeft==newRight) || prevInterval==null), true, normDecDistr);
                intervalDistributions.add(prevInterval);
            }
        }
        m_DecDistributions = (IntervalWithDecisionDistribution[])intervalDistributions.toArray(new IntervalWithDecisionDistribution[0]);
    }

    /**
     * Returns succesive attribute value ranges with the corresponding decision vectors.
     *
     * @return Succesive attribute value ranges with the corresponding decision vectors.
     */
    public IntervalWithDecisionDistribution[] getIntervals()
    {
    	return m_DecDistributions;
    }

    /**
     * Constructs string representation of this attribute.
     *
     * @return String representation of this attribute.
     */
    public String toString()
    {
    	StringBuffer sbuf = new StringBuffer();
    	sbuf.append("Atrybut numeryczny ("+m_DecDistributions.length+" przedzialow)"+Report.lineSeparator);
    	for (int interval = 0; interval < m_DecDistributions.length; interval++)
    		sbuf.append(m_DecDistributions[interval]+Report.lineSeparator);
    	return sbuf.toString();
    }
}

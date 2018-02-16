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

package rseslib.processing.reducts;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import rseslib.processing.discernibility.DiscernibilityMatrixProvider;
import rseslib.processing.reducts.GlobalReductsProvider;
import rseslib.structure.attribute.Header;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Greedy Johnson algorithm calculating a reduct.
 * TODO: tests, comments, optimization
 * 
 * @author Wiktor Gromniak
 */
public class JohnsonReductsProvider extends Configuration implements GlobalReductsProvider {

	private enum GenerateMethod { AllJohnson, OneJohnson; };

	private static final String s_sGenerate = "Reducts";

    private GenerateMethod m_Generate;
    private DiscernibilityMatrixProvider m_Discernibility;
    private Header m_Header;

    public JohnsonReductsProvider(Properties prop, DoubleDataTable table) throws PropertyConfigurationException {
        super(prop);
        m_Generate = GenerateMethod.valueOf(getProperty(s_sGenerate));
        m_Discernibility = new DiscernibilityMatrixProvider(getProperties(), table);
        m_Header = table.attributes();
    }

    public Collection<BitSet> getReducts() {
        Collection<BitSet> cnf = m_Discernibility.getDiscernibilityMatrix();
        Vector<BitSet> bs = new Vector<BitSet>();
        bs.addAll(cnf);
        Collection<BitSet> collection = null;
        switch (m_Generate) {
            case AllJohnson:
                collection = getAllCountedReducts(bs);
                break;
            case OneJohnson:
                collection = getOneCountedReducts(bs);
                break;
        }
        return collection;
    }

    /**
     * Generates all possible reducts from indiscernibility matrix. First, the function counts all occurrences of
     * attributes placed in indiscernibility matrix, and later, based on gathered data, it counts all possible reducts.
     * All reducts can be generated because in this heuristic, there are sometimes few reducts with maximum occurrence.
     *
     * @param discern_attrs Indiscirnibility matrix.
     * @return All reducts set.
     */
    private Collection<BitSet> getAllCountedReducts(Vector<BitSet> discern_attrs) {

        if (discern_attrs.isEmpty()) {
            return Collections.emptyList();
        }

        List<BitSet> results = new ArrayList<BitSet>();

        int[] counts = new int[m_Header.noOfAttr() - 1]; // TODO: this may contain decision, if we decide so

        for (BitSet cell : discern_attrs) {
            for (int i = cell.nextSetBit(0); i >= 0; i = cell.nextSetBit(i + 1)) {
                counts[i]++;
            }
        }

        List<Integer> maxIndices = maxIndices(counts);

        for (int maxIndex : maxIndices) {

            @SuppressWarnings("unchecked")
            Vector<BitSet> discMatCopy = (Vector<BitSet>) discern_attrs.clone();
            BitSet result = new BitSet();
            result.set(maxIndex);

            removeCellsWithAttr(maxIndex, discMatCopy);

            while (!discMatCopy.isEmpty()) {

                counts = new int[m_Header.noOfAttr() - 1];

                for (BitSet cell : discMatCopy) {
                    for (int i = cell.nextSetBit(0); i >= 0; i = cell.nextSetBit(i + 1)) {
                        counts[i]++;
                    }
                }

                maxIndex = maxIndex(counts);

                result.set(maxIndex);

                removeCellsWithAttr(maxIndex, discMatCopy);
            }

            results.add(result);
        }

        return results;
    }

    private static List<Integer> maxIndices(int[] counts) {
        List<Integer> result = new ArrayList<Integer>();
        int maxIndex = 0;
        int maxCount = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[maxIndex]) {
                maxIndex = i;
                maxCount = counts[maxIndex];
            }
        }
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == maxCount) {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * Generates one possible reduct from indiscerrnibility matrix. First count of one occurrence of attribute existed in
     * indiscernibility matrix and later based on this value it will counting reduct.
     *
     * @param discern_attrs Indiscirnibility matrix.
     * @return All reducts set.
     */
    private Collection<BitSet> getOneCountedReducts(Vector<BitSet> discern_attrs) {

        if (discern_attrs.isEmpty()) {
            return Collections.emptyList();
        }

        BitSet result = new BitSet();

        while (!discern_attrs.isEmpty()) {

            int[] counts = new int[m_Header.noOfAttr() - 1];

            for (BitSet cell : discern_attrs) {
                for (int i = cell.nextSetBit(0); i >= 0; i = cell.nextSetBit(i + 1)) {
                    counts[i]++;
                }
            }

            int maxIndex = maxIndex(counts);

            result.set(maxIndex);

            removeCellsWithAttr(maxIndex, discern_attrs);
        }

        return Collections.singletonList(result);
    }

    private static int maxIndex(int[] counts) {
        int maxIndex = 0;
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > counts[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private static void removeCellsWithAttr(int attr, Vector<BitSet> discMatCopy) {
        for (Iterator<BitSet> iter = discMatCopy.iterator(); iter.hasNext(); ) {
            BitSet cell = iter.next();

            for (int i = cell.nextSetBit(0); i >= 0; i = cell.nextSetBit(i + 1)) {
                if (i == attr) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    public Indiscernibility getIndiscernibilityForMissing() {
        return m_Discernibility.getIndiscernibilityForMissing();
    }
}


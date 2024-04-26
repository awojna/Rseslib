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


package rseslib.processing.metrics;

import java.util.Properties;
import java.util.Random;

import rseslib.processing.indexing.metric.TreeIndexer;
import rseslib.processing.searching.metric.NearestNeighboursProviderFromTree;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.metric.AbstractWeightedMetric;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * The method adjusting attribute weights in a metric.
 * It increases the weight of an attribute
 * if the 1-nn accuracy based on this attribute is greater
 * than the 1-nn accuracy based on all attrbiutes.
 *
 * @author      Arkadiusz Wojna
 */
public class AccuracyBasedWeightAdjuster extends Configuration implements WeightAdjuster
{
    /** Parameter name for the number of iterations. */
    private static final String NO_OF_ITERATIONS_FOR_WEIGHTING_PARAMETER_NAME = "noOfIterationsForWeighting";
    /** Parameter name for the maximal size of randomly selected training sample used in a single iteration. */
    private static final String TRAINING_SAMPLE_SIZE_FOR_WEIGHTING_PARAMETER_NAME = "trainingSampleSizeForWeighting";
    /** Parameter name for the maximal size of randomly selected test sample used in a single iteration. */
    private static final String TEST_SAMPLE_SIZE_FOR_WEIGHTING_PARAMETER_NAME = "testSampleSizeForWeighting";
    /** Generator of random numbers. */
    private static final Random RANDOM_GENERATOR = new Random();
    /** Empty progress. */
    private static final Progress EMPTY_PROGRESS = new EmptyProgress();

    /** Tree indexer. */
    private TreeIndexer m_Indexer = new TreeIndexer(null);
    /** The number of iterations. */
    private int m_nNoOfIterationsForWeighting = getIntProperty(NO_OF_ITERATIONS_FOR_WEIGHTING_PARAMETER_NAME);
    /** The maximal size of randomly selected training sample used in a single iteration. */
    private int m_nTrainingSampleSizeForWeighting = getIntProperty(TRAINING_SAMPLE_SIZE_FOR_WEIGHTING_PARAMETER_NAME);
    /** The maximal size of randomly selected test sample used in a single iteration. */
    private int m_nTestSampleSizeForWeighting = getIntProperty(TEST_SAMPLE_SIZE_FOR_WEIGHTING_PARAMETER_NAME);
    /** Provider of nearest neighbours from a training data sample. */
    private NearestNeighboursProviderFromTree m_NeighboursProvider = new NearestNeighboursProviderFromTree();

    /**
     * Constructor.
     *
     * @param prop Map between property names and property values.
     */
    public AccuracyBasedWeightAdjuster(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
    }

    /**
     * Applies a method to adjust weights of the metric metr.
     *
     * @param metr Metric used to adjust weights.
     * @param tab  Table of data objects used to adjust weights.
     * @param prog Progress object used to report progress.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public void adjustWeights(AbstractWeightedMetric metr, DoubleDataTable tab, Progress prog) throws InterruptedException
    {
        prog.set("Accuracy based weighting", m_nNoOfIterationsForWeighting);
        NominalAttribute decAttr = tab.attributes().nominalDecisionAttribute();
        DoubleDataWithDecision[] tabObjects = tab.getDataObjects().toArray(new DoubleDataWithDecision[0]);
        double[] weightModifiers = new double[metr.attributes().noOfAttr()];
        boolean[] alwaysAdded = new boolean[metr.attributes().noOfAttr()];
        for (int att = 0; att < weightModifiers.length; att++)
            if (metr.attributes().isConditional(att))
            {
                weightModifiers[att] = metr.getWeight(att);
                alwaysAdded[att] = true;
            }
        int added = alwaysAdded.length;
        int noOfEpochs = 0;
        DoubleDataTable[] sampleTab = new ArrayListDoubleDataTable[tab.attributes().nominalDecisionAttribute().noOfValues()];
        for (int epoch = 0; epoch < m_nNoOfIterationsForWeighting; epoch++)
            if (added==0) prog.step();
            else
            {
                if (tab.noOfObjects() > m_nTrainingSampleSizeForWeighting*1.2)
                {
                    for (int dec = 0; dec < sampleTab.length; dec++) sampleTab[dec] = new ArrayListDoubleDataTable(tab.attributes());
                    int selected = 0;
                    boolean[] inSample = new boolean[tabObjects.length];
                    while (selected < m_nTrainingSampleSizeForWeighting)
                    {
                        int ind = RANDOM_GENERATOR.nextInt(tabObjects.length);
                        if (!inSample[ind])
                        {
                            sampleTab[decAttr.localValueCode(tabObjects[ind].getDecision())].add(tabObjects[ind]);
                            selected++;
                            inSample[ind] = true;
                        }
                    }
                }
                else if (epoch==0)
                {
                    for (int dec = 0; dec < sampleTab.length; dec++) sampleTab[dec] = new ArrayListDoubleDataTable(tab.attributes());
                    for (int obj = 0; obj < tabObjects.length; obj++)
                        sampleTab[decAttr.localValueCode(tabObjects[obj].getDecision())].add(tabObjects[obj]);
                }
                IndexingTreeNode[] indexedObjects = new IndexingTreeNode[tab.attributes().nominalDecisionAttribute().noOfValues()];
                for (int dec = 0; dec < indexedObjects.length; dec++)
                    indexedObjects[dec] = m_Indexer.indexing(sampleTab[dec].getDataObjects(), metr, EMPTY_PROGRESS);
                int good = 0, bad = 0;
                int[] attBad = new int[tab.attributes().noOfAttr()];
                int[] attGood = new int[tab.attributes().noOfAttr()];
                int noOfTests = m_nTestSampleSizeForWeighting;
                if (tabObjects.length < noOfTests*1.2) noOfTests = tabObjects.length;
                for (int tst = 0; tst < noOfTests; tst++)
                {
                    int ind = tst;
                    if (noOfTests < tabObjects.length) ind = RANDOM_GENERATOR.nextInt(tabObjects.length);
                    DoubleDataWithDecision dObj = tabObjects[ind];
                    Neighbour nearestGood = null, nearestBad = null;
                    for (int dec = 0; dec < indexedObjects.length; dec++)
                        if (dObj.getDecision() == decAttr.globalValueCode(dec))
                        {
                            Neighbour[] nearestGoodArray = m_NeighboursProvider.getKNearest(metr, dObj, indexedObjects[dec], 2);
                            int nearest = 0;
                            if (nearestGoodArray.length > 0 && dObj.equals(nearestGoodArray[0].neighbour()))
                            	nearest = 1;
                            if (nearestGoodArray.length > nearest) nearestGood = nearestGoodArray[nearest];
                        }
                        else
                        {
                            Neighbour[] nearestBadArray = m_NeighboursProvider.getKNearest(metr, dObj, indexedObjects[dec], 1);
                            if (nearestBadArray.length > 0 && (nearestBad==null || nearestBadArray[0].dist() < nearestBad.dist()))
                                nearestBad = nearestBadArray[0];
                        }
                    if (nearestBad==null)
                    {
                        good++;
                        for (int att = 0; att < attGood.length; att++)
                            if (dObj.attributes().isConditional(att)) attGood[att]++;
                    }
                    else if (nearestGood==null)
                    {
                        bad++;
                        for (int att = 0; att < attBad.length; att++)
                            if (dObj.attributes().isConditional(att)) attBad[att]++;
                    }
                    else
                    {
                        if (nearestGood.dist() <= nearestBad.dist()) good++;
                        else bad++;
                        for (int att = 0; att < attGood.length; att++)
                            if (dObj.attributes().isConditional(att))
                            {
                                double aGood = metr.valueDist(dObj.get(att), nearestGood.neighbour().get(att), att);
                                double aBad = metr.valueDist(dObj.get(att), nearestBad.neighbour().get(att), att);
                                if (aGood <= aBad) attGood[att]++;
                                else attBad[att]++;
                            }
                    }
                }
                for (int att = 0; att < attGood.length; att++)
                    if (metr.attributes().isConditional(att))
                    {
                        weightModifiers[att] *= (double)(m_nNoOfIterationsForWeighting-2)/(double)m_nNoOfIterationsForWeighting;
                        if (attGood[att]*(good+bad) > (attGood[att]+attBad[att])*good)
                            metr.setWeight(att, metr.getWeight(att)+weightModifiers[att]);
                        else if (alwaysAdded[att])
                        {
                            alwaysAdded[att] = false;
                            added--;
                        }
                    }
                noOfEpochs++;
                prog.step();
            }
        metr.setNoOfWeightingIterations(noOfEpochs);
    }
}

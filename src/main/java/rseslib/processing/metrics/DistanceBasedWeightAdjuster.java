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


package rseslib.processing.metrics;

import java.util.Collection;
import java.util.Properties;
import java.util.Random;

import rseslib.processing.filtering.Sampler;
import rseslib.processing.indexing.metric.TreeIndexer;
import rseslib.processing.searching.metric.NearestNeighboursProviderFromTree;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.metric.AbstractWeightedMetric;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * The method adjusting attribute weights in a metric.
 * It increases the weight of an attribute
 * if the ratio of sumed distances
 * between correctly and incorrectly 1-nn classified objects
 * computed only for the considered attribute
 * is better than the ratio of sumed distances
 * while considering all attributes.
 *
 * @author      Arkadiusz Wojna
 */
public class DistanceBasedWeightAdjuster extends Configuration implements WeightAdjuster
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
    public DistanceBasedWeightAdjuster(Properties prop) throws PropertyConfigurationException
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
        prog.set("Distance based weighting", m_nNoOfIterationsForWeighting);
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
        Header hdr = tab.attributes();
        Collection<DoubleData> sampleTab = tab.getDataObjects();
        for (int epoch = 0; epoch < m_nNoOfIterationsForWeighting; epoch++)
            if (added==0) prog.step();
            else
            {
                if (tab.noOfObjects() > m_nTrainingSampleSizeForWeighting * 1.2)
                    sampleTab = Sampler.selectWithoutRepetitions(tab.getDataObjects(), m_nTrainingSampleSizeForWeighting); 
                IndexingTreeNode indexedObjects = m_Indexer.indexing(sampleTab, metr, EMPTY_PROGRESS);
                double[] attrDistGood = new double[hdr.noOfAttr()];
                double[] attrDistBad = new double[hdr.noOfAttr()];
                int noOfTests = m_nTestSampleSizeForWeighting;
                if (tabObjects.length < noOfTests * 1.2)
                    noOfTests = tabObjects.length;
                for (int tst = 0; tst < noOfTests; tst++) {
                    int ind = tst;
                    if (noOfTests < tabObjects.length)
                        ind = RANDOM_GENERATOR.nextInt(tabObjects.length);
                    DoubleDataWithDecision dObj = tabObjects[ind];
                    Neighbour[] neighbours = m_NeighboursProvider.getKNearest(
                        metr, dObj, indexedObjects, 2);
                    int nearest = 0;
                    if (dObj.equals(neighbours[0].neighbour())) nearest = 1;
                    if (dObj.getDecision() ==
                        neighbours[nearest].neighbour().getDecision()) {
                        for (int att = 0; att < attrDistGood.length; att++)
                            if (dObj.attributes().isConditional(att))
                                attrDistGood[att] +=
                                    metr.valueDist(dObj.get(att),
                                    neighbours[nearest].neighbour().get(
                                    att), att);
                    }
                    else {
                        for (int att = 0; att < attrDistBad.length; att++)
                            if (dObj.attributes().isConditional(att))
                                attrDistBad[att] +=
                                    metr.valueDist(dObj.get(att),
                                    neighbours[nearest].neighbour().get(
                                    att), att);
                    }
                }
                double distGood = 0, distBad = 0;
                for (int att = 0; att < hdr.noOfAttr(); att++)
                    if (hdr.isConditional(att)) {
                        distGood += attrDistGood[att]*metr.getWeight(att);
                        distBad += attrDistBad[att]*metr.getWeight(att);
                    }
                for (int att = 0; att < hdr.noOfAttr(); att++)
                    if (hdr.isConditional(att)) {
                        weightModifiers[att] *=
                            (double) (m_nNoOfIterationsForWeighting - 2) /
                            (double) m_nNoOfIterationsForWeighting;
                        if (attrDistBad[att] * (distGood + distBad) >
                            (attrDistGood[att] + attrDistBad[att]) * distBad)
                            metr.setWeight(att,
                                           metr.getWeight(att) +
                                           weightModifiers[att]);
                        else if (alwaysAdded[att]) {
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

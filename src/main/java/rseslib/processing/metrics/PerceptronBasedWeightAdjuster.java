/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.function.doubleval.Perceptron;
import rseslib.structure.metric.AbstractWeightedMetric;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * The method adjusting attribute weights in a metric.
 * It uses single perceptron learning
 * for generating attribute weights.
 *
 * @author      Arkadiusz Wojna
 */
public class PerceptronBasedWeightAdjuster extends Configuration implements WeightAdjuster
{
    /** Parameter name for the number of pairs of training data objects used for percepton based weighting. */
    private static final String NO_OF_PAIRS_FOR_WEIGHTING_PARAMETER_NAME = "noOfPairsForWeighting";

    /** The number of pairs of training data objects used for percepton based weighting. */
    private int m_nNoOfPairsForWeighting = getIntProperty(NO_OF_PAIRS_FOR_WEIGHTING_PARAMETER_NAME);

    /**
     * Constructor.
     *
     * @param prop Map between property names and property values.
     */
    public PerceptronBasedWeightAdjuster(Properties prop) throws PropertyConfigurationException
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
        Random rnd = new Random();
        ArrayList<DoubleData> tableOfObjects = new ArrayList<DoubleData>();
        for (DoubleData obj : tab.getDataObjects())
        	tableOfObjects.add(obj);
        DoubleDataTable tableOfPairs = new ArrayListDoubleDataTable(tab.attributes());
        for (int i = 0; i < m_nNoOfPairsForWeighting; i++)
        {
            int ind1 = rnd.nextInt(tableOfObjects.size());
            int ind2 = rnd.nextInt(tableOfObjects.size());
            if (ind1!=ind2)
            {
                DoubleDataWithDecision obj1 = (DoubleDataWithDecision)tableOfObjects.get(ind1);
                DoubleDataWithDecision obj2 = (DoubleDataWithDecision)tableOfObjects.get(ind2);
                DoubleDataWithDecision ido = new DoubleDataObject(tab.attributes());
                for (int att = 0; att < ido.attributes().noOfAttr(); att++)
                    if (ido.attributes().isConditional(att))
                        ido.set(att, metr.valueDist(obj1.get(att), obj2.get(att), att));
                if (obj1.getDecision()==obj2.getDecision()) ido.setDecision(0);
                else ido.setDecision(1);
                tableOfPairs.add(ido);
            }
        }
        double convergance = (double)0.1 / (double)m_nNoOfPairsForWeighting;
        Perceptron p = Perceptron.discriminateOneFromRest(tableOfPairs, 1, convergance, null);
        p = Perceptron.discriminateOneFromRest(tableOfPairs, 1, convergance/10, p);
        p = Perceptron.discriminateOneFromRest(tableOfPairs, 1, convergance/100, p);
        for (int i = 0; i < metr.attributes().noOfAttr(); i++) metr.setWeight(i, p.getWeights()[i]);
        double minPositiveWeight = Double.MAX_VALUE;
        for (int i = 0; i < metr.attributes().noOfAttr(); i++)
            if (metr.getWeight(i) > 0 && metr.getWeight(i) < minPositiveWeight)
                minPositiveWeight = metr.getWeight(i);
        minPositiveWeight /= 100;
        for (int i = 0; i < metr.attributes().noOfAttr(); i++)
            if (metr.getWeight(i) < 0) metr.setWeight(i, minPositiveWeight);
    }
}

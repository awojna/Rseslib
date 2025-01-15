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


package rseslib.processing.classification.tree.c45;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import rseslib.processing.filtering.MissingValuesFilter;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.NumericAttributeComparator;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.function.intval.Discrimination;
import rseslib.structure.function.intval.NominalAttributeDiscrimination;
import rseslib.structure.function.intval.NumericAttributeCut;

/**
 * Generator of discrimination functions based on nominal attributes.
 * To discriminate data it uses the attribute with
 * the highest information gain ration based on the entropy.
 *
 * @author      Arkadiusz Wojna
 */
public class BestGainRatioDiscriminationProvider implements DiscriminationProvider
{
    /** The natural logarithm of 2. */
    private static final double LN2 = Math.log(2);

    /**
     * Returns the function discriminating data objects
     * into a number of branches.
     *
     * @param dataSet   Collection of data objects used to select the best discrimination function.
     * @param hdr       The header for the data collection.
     * @return          The function that represents the selected discrimination function.
     *                  The function returns the values in the range from 0 to n-1,
     *                  where n is the number of branches. It returns null, if the data set
     *                  is recognised to constitute a leaf in a decision tree.
     */
    public Discrimination getDiscrimination(Collection<DoubleData> dataSet, Header hdr)
    {
        if (dataSet.isEmpty()) return null;
        NominalAttribute decInfo = hdr.nominalDecisionAttribute();

        // Selects the partition with the maximum information gain
        Discrimination bestDiscr = null;
        double bestGainRatio = 0.0;
        for (int attr = 0; attr < hdr.noOfAttr(); attr++)
            if (hdr.isConditional(attr))
               if (hdr.isNominal(attr))
               {
                   Discrimination discr = new NominalAttributeDiscrimination(attr, (NominalAttribute)hdr.attribute(attr));
                   Collection<DoubleData> dataSetWithoutMissing = MissingValuesFilter.select(dataSet, attr);
                   int[] decisionDistribution = getDecisionDistribution(dataSetWithoutMissing, decInfo);
                   double initialEntropy = entropy(decisionDistribution);
                   Collection<DoubleData>[] branches = splitSet(dataSetWithoutMissing, discr);
                   int different = 0;
                   for (int branch = 0; branch < branches.length; branch++)
                       if (branches[branch].size() > 0) different++;
                   if (different > 1)
                   {
                       int[] branchTotals = getDistribution(branches);
                       int[][] branchDistributions = new int[branches.length][];
                       for (int branch = 0; branch < branchDistributions.length; branch++)
                           branchDistributions[branch] = getDecisionDistribution(branches[branch],decInfo);

                       // Computes the gain ratio
                       double attrGainRatio = infoGain(branchTotals,branchDistributions,initialEntropy)
                                               / entropy(branchTotals);
                       // one may use information gain instead of gain ratio:
                       // double attrGainRatio = infoGain(branchTotals, branchDistributions, initialEntropy);
                       if (attrGainRatio > bestGainRatio)
                       {
                           bestDiscr = discr;
                           bestGainRatio = attrGainRatio;
                       }
                   }
                }
                else if (hdr.isNumeric(attr))
                {
                   Collection<DoubleData> dataSetWithoutMissing = MissingValuesFilter.select(dataSet, attr);
                   int[] decisionDistribution = getDecisionDistribution(dataSetWithoutMissing, decInfo);
                   double initialEntropy = entropy(decisionDistribution);
                   DoubleDataWithDecision[] objects = dataSetWithoutMissing.toArray(new DoubleDataWithDecision[0]);
                   Arrays.sort(objects, new NumericAttributeComparator(attr));
                   int[] branchTotals = new int[2];
                   branchTotals[1] = dataSetWithoutMissing.size();
                   int[][] branchDistributions = new int[2][];
                   branchDistributions[0] = new int[decInfo.noOfValues()];
                   branchDistributions[1] = (int[])decisionDistribution.clone();
                   for (int obj = 0; obj < objects.length; obj++)
                   {
                       if (obj > 0 && objects[obj-1].get(attr)!=objects[obj].get(attr))
                       {
                           double attrGainRatio = infoGain(branchTotals, branchDistributions, initialEntropy)
                                                   / entropy(branchTotals);
                           // one may use information gain instead of gain ratio:
                           // double attrGainRatio = infoGain(branchTotals, branchDistributions, initialEntropy);
                           if (attrGainRatio > bestGainRatio)
                           {
                               bestDiscr = new NumericAttributeCut(attr, (objects[obj-1].get(attr)+objects[obj].get(attr))/2, (NumericAttribute)hdr.attribute(attr));
                               bestGainRatio = attrGainRatio;
                           }
                       }
                       branchTotals[0]++;
                       branchTotals[1]--;
                       branchDistributions[0][decInfo.localValueCode(objects[obj].getDecision())]++;
                       branchDistributions[1][decInfo.localValueCode(objects[obj].getDecision())]--;
                   }
               }
       return bestDiscr;
    }

    /**
     * Computes the information gain for a given partition.
     *
     * @param branches             The partition of data.
     * @param branchDistributions  The decision distributions in particular branches
     *                             of the partition.
     * @param initialEntropy       The entropy in the whole data set.
     * @return                     The information gain for the partition.
     */
    private double infoGain(int[] branches, int[][] branchDistributions, double initialEntropy)
    {
        int total = 0;
        for (int branch = 0; branch < branches.length; branch++)
            total += branches[branch];
        double gain = initialEntropy;
        for (int branch = 0; branch < branches.length; branch++)
                gain -= entropy(branchDistributions[branch])
                         * (double)branches[branch] / (double)total;
        return gain;
    }

    /**
     * Computes the decision distribution of a data set.
     *
     * @param dataSet  The data for which the dicision distribution is to be computed.
     * @param decInfo  The information about the decision attribute.
     * @return         The decision distribution of the data.
     */
    private int[] getDecisionDistribution(Collection<DoubleData> dataSet, NominalAttribute decInfo)
    {
        int[] decisionDistribution = new int[decInfo.noOfValues()];
        for (DoubleData dObj : dataSet)
            decisionDistribution[decInfo.localValueCode(((DoubleDataWithDecision)dObj).getDecision())]++;
        return decisionDistribution;
    }

    /**
     * Splits a dataset on the basis of a discriminating function.
     *
     * @param dataSet   The data to be split.
     * @param discr     Discriminating function.
     * @return          The partition obtained.
     */
    private Collection<DoubleData>[] splitSet(Collection<DoubleData> dataSet, Discrimination discr)
    {
        ArrayList<DoubleData>[] splitData = new ArrayList[discr.noOfValues()];
        for (int branch = 0; branch < splitData.length; branch++)
            splitData[branch] = new ArrayList<DoubleData>();
        for (DoubleData dObj : dataSet)
            splitData[discr.intValue(dObj)].add(dObj);
        return splitData;
     }

     /**
      * Computes the partition distribution.
      *
      * @param partition  The data partition for which the distribution is to be computed.
      * @return           The partition distibution.
      */
     private int[] getDistribution(Collection[] partition)
     {
         int[] distribution = new int[partition.length];
         for (int set = 0; set < distribution.length; set++)
             distribution[set] = partition[set].size();
         return distribution;
      }

   /**
    * Computes the entropy of the data distibution.
    *
    * @param dataDistribution  The data distribution for which the entropy is to be computed.
    * @return                  The entropy.
    */
    private double entropy(int[] dataDistribution)
    {
        int total = 0;
        for (int part = 0; part < dataDistribution.length; part++)
            total += dataDistribution[part];
        if (total==0) return 0;
        double entropy = 0;
        for (int part = 0; part < dataDistribution.length; part++)
            if (dataDistribution[part] > 0)
                entropy -= (double)dataDistribution[part] * log2(dataDistribution[part]);
        entropy /= (double)total;
        return entropy + log2(total);
    }

    /**
     * Returns the logarithm of val for base 2.
     *
     * @param val Double value.
     * @return    Logarithm of val for base 2.
     */
    private double log2(double val)
    {
        return Math.log(val) / LN2;
    }
}

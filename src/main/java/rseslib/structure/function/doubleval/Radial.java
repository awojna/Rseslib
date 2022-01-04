/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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


package rseslib.structure.function.doubleval;

import java.util.Random;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;

/**
 * Euclidean distance between the vector of function values
 * for a given data object and a fixed centre.
 */
public class Radial implements DoubleFunction
{
    /** Energy value obtained in the previous training step. */
    private static double prevEnergy = 0;

     /** Array of functions. */
    DoubleFunction[] m_arrComponents;
   /** Coordinates of the centre of this radial function. */
    double[] m_nCentre;

    /**
     * Constructor.
     *
     * @param components Array of functions.
     * @param centre     Coordinates of the centre of this radial function
     */
    public Radial(DoubleFunction[] components, double[] centre)
    {
        m_arrComponents = components;
        m_nCentre = centre;
    }

    /**
     * Returns the value of this function for a given double data.
     *
     * @param dObj Double data to be evaluated.
     * @return     Value of this function for a given double data.
     */
    public double doubleVal(DoubleData dObj)
    {
        double d = 0;
        for (int i = 0; i < m_arrComponents.length; i++)
            d += (m_nCentre[i] - m_arrComponents[i].doubleVal(dObj))*(m_nCentre[i] - m_arrComponents[i].doubleVal(dObj));
        return Math.sqrt(d);
    }

    /**
     * Returns an array of numeric attributes as the array of functions.
     *
     * @param size Number of attributes.
     * @return     Array of numeric attributes.
     */
    public static DoubleFunction[] initAttributes(int size)
    {
        DoubleFunction[] nf = new DoubleFunction[size];
        for (int i = 0; i < nf.length; i++) nf[i] = new AttributeValue(i);
        return nf;
    }

    /**
     * Constructs a radial function
     * with the mean of a given decision class
     * from a training table as the centre.
     *
     * @param tab Training table.
     * @param dec Decision class providing the mean.
     * @return    Constructed radial function.
     */
    public static Radial mean(DoubleDataTable tab, int dec)
    {
        DoubleFunction[] attributes = null;
        double[] coefficients = null;
        for (DoubleData dObj : tab.getDataObjects())
        {
            if (((DoubleDataWithDecision)dObj).getDecision() == dec)
            {
                if (attributes == null)
                {
                    attributes = initAttributes(dObj.attributes().noOfAttr());
                    if (coefficients == null)
                    {
                        coefficients = new double[dObj.attributes().noOfAttr()];
                        for (int i = 0; i < coefficients.length; i++)
                            coefficients[i] = dObj.get(i);
                    }
                }
                for (int i = 0; i < coefficients.length; i++) coefficients[i] += dObj.get(i);
            }
        }
        for (int i = 0; i < coefficients.length; i++) coefficients[i] /= tab.noOfObjects();
        return new Radial(attributes, coefficients);
    }

    /**
     * Constructs k radial function
     * using the algorithm of k-means.
     * The algorithm stops when the difference in energy
     * between the last and the last but one step
     * is less than 1.
     *
     * @param tab         Training table.
     * @param dec         Decision class used to generate k means.
     * @param noOfCenters Number of means in the algorithm.
     * @return            Array of constructed radial functions.
     */
    public static Radial[] kMeans(DoubleDataTable tab, double dec, int noOfCenters)
    {
        Random rnd = new Random();
        double[] min = null;
        double[] max = null;
        for (DoubleData dObj : tab.getDataObjects())
        {
            if (min==null)
            {
                min = new double[dObj.attributes().noOfAttr()];
                for (int i = 0; i < min.length; i++) min[i] = dObj.get(i);
            }
            if (max==null)
            {
                max = new double[dObj.attributes().noOfAttr()];
                for (int i = 0; i < max.length; i++) max[i] = dObj.get(i);
            }
            for (int i = 0; i < min.length; i++)
                if (dObj.get(i) < min[i]) min[i] = dObj.get(i);
            for (int i = 0; i < max.length; i++)
                if (dObj.get(i) > max[i]) max[i] = dObj.get(i);
        }
        DoubleFunction[] attributes = null;
        double[][] coefficients = null;
        double[][] sum = null;
        double oldEnergy = 0;
        double energy = -100;
        while (energy - oldEnergy > 1 || energy - oldEnergy < -1)
        {
            if (sum!=null)
                for (int c = 0; c < sum.length; c++)
                    for (int i = 0; i < sum[c].length; i++) sum[c][i] = 0;
            oldEnergy = energy;
            energy = 0;
            int number = 0;
            for (DoubleData dObj : tab.getDataObjects())
            {
                if (attributes == null)
                {
                    attributes = initAttributes(dObj.attributes().noOfAttr());
                    if (coefficients == null)
                    {
                        coefficients = new double[noOfCenters][];
                        for (int c = 0; c < coefficients.length; c++)
                        {
                            coefficients[c] = new double[dObj.attributes().noOfAttr()];
                            for (int i = 0; i < coefficients[c].length; i++)
                                coefficients[c][i] = min[i] + rnd.nextDouble()*(max[i] - min[i]);
                        }
                    }
                    if (sum == null)
                    {
                        sum = new double[noOfCenters][];
                        for (int c = 0; c < sum.length; c++)
                        {
                            sum[c] = new double[dObj.attributes().noOfAttr()];
                            for (int i = 0; i < sum[c].length; i++) sum[c][i] = 0;
                        }
                    }
                }
                if (((DoubleDataWithDecision)dObj).getDecision() == dec)
                {
                    number++;
                    int best = -1;
                    double bestDist = 0;
                    for (int c = 0; c < coefficients.length; c++)
                    {
                        double dist = 0;
                        for (int i = 0; i < coefficients[c].length; i++)
                        {
                            double diff = (dObj.get(i)-coefficients[c][i]);
                            dist += (diff*diff);
                        }
                        if (best==-1 || dist < bestDist)
                        {
                            best = c;
                            bestDist = dist;
                        }
                    }
                    for (int i = 0; i < coefficients[best].length; i++)
                    {
                        double diff = dObj.get(i)-coefficients[best][i];
                        energy += (diff*diff);
                        sum[best][i] += dObj.get(i);
                    }
                }
            }
            for (int c = 0; c < coefficients.length; c++)
                for (int i = 0; i < coefficients[c].length; i++)
                    coefficients[c][i] = sum[c][i] / number;
        }
        prevEnergy = energy;
        Radial[] radials = new Radial[coefficients.length];
        for (int i = 0; i < radials.length; i++) radials[i] = new Radial(attributes, coefficients[i]);
        return radials;
    }

    /**
     *
     * Constructs k radial function
     * using the algorithm of k-means with increasing number of means.
     * The algorithm stops when the difference in energy
     * between the last and the last but one iteration
     * is less than 1.
     *
     * @param tab         Training table.
     * @param dec         Decision class used to generate k means.
     * @return            Array of constructed radial functions.
     */
    public static Radial[] iterativeKMeans(DoubleDataTable tab, double dec)
    {
        double energy = -100;
        int noOfCenters = 1;
        Radial[] radials = kMeans(tab, dec, noOfCenters);
        Radial[] oldRadials = null;
        while (energy - prevEnergy > 1 || prevEnergy - energy > 1)
        {
            energy = prevEnergy;
            noOfCenters++;
            oldRadials = radials;
            radials = kMeans(tab, dec, noOfCenters);
        }
        return oldRadials;
    }
}

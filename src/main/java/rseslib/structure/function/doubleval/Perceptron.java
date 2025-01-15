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


package rseslib.structure.function.doubleval;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;

/**
 * Perceptron.
 *
 * @author      Arkadiusz Wojna
 */
public class Perceptron implements DoubleFunction
{
    /** Array of input functions. */
    DoubleFunction[] m_arrComponents;
    /** Weights assigned to input functions. */
    double[] m_arrCoefficients;

    /**
     * Constructor.
     *
     * @param components   Array of input function.
     * @param coefficients Weights assigned to input functions.
     */
    public Perceptron(DoubleFunction[] components, double[] coefficients)
    {
        m_arrComponents = components;
        m_arrCoefficients = coefficients;
    }

    /**
     * Returns weights assigned to input functions.
     *
     * @return Weights assigned to input functions.
     */
    public double[] getWeights()
    {
        return m_arrCoefficients;
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
        for (int i = 0; i < m_arrComponents.length; i++) d += m_arrCoefficients[i]*m_arrComponents[i].doubleVal(dObj);
        return d;
    }

    /**
     * Returns an array of numeric attributes as the array of input functions.
     *
     * @param size Number of attributes.
     * @return     Array of numeric attributes.
     */
    private static DoubleFunction[] initAttributes(int size)
    {
        DoubleFunction[] nf = new DoubleFunction[size];
        for (int i = 0; i < nf.length; i++) nf[i] = new AttributeValue(i);
        return nf;
    }

    /**
     * Returns array of weights to input functions with equal coordinates
     * normalised to the length 1.
     *
     * @param size Number of weights.
     * @return     Array of weights to input functions.
     */
    private static double[] initCoefficients(int size)
    {
        double[] coefficients = new double[size];
        for (int i = 0; i < coefficients.length; i++) coefficients[i] = 1/Math.sqrt(size);
        return coefficients;
    }

    /**
     * Trains a perceptron in this way that it discriminates
     * one decision class from the rest.
     *
     * @param tab         Training table.
     * @param dec         Decision to be discriminated.
     * @param convergence Convergence coefficient.
     * @param per         Perceptron with weights for initialisation,
     *                    if null the method create new perceptron.
     * @return            Trained perceptron.
     */
    public static Perceptron discriminateOneFromRest(DoubleDataTable tab, double dec, double convergence, Perceptron per)
    {
        DoubleFunction[] attributes = null;
        double[] coefficients = null;
        if (per != null) coefficients = per.m_arrCoefficients;
        for (DoubleData dObj : tab.getDataObjects())
        {
            if (attributes == null)
            {
                attributes = initAttributes(dObj.attributes().noOfAttr());
                if (coefficients == null) coefficients = initCoefficients(dObj.attributes().noOfAttr());
            }
            double sk = 0;
            for (int i = 0; i < coefficients.length; i++) sk += coefficients[i]*dObj.get(i);
            double delta = 0;
            if (((DoubleDataWithDecision)dObj).getDecision()==dec)
            {
                if (sk <= 0) delta = 2;
            }
            else
            {
                if (sk > 0) delta = -2;
            }
            for (int i = 0; i < coefficients.length; i++) coefficients[i] += convergence*delta*dObj.get(i);
        }
        return new Perceptron(attributes, coefficients);
    }

    /**
     * Metoda uczaca perceptron, dyskryminujaca dwie klasy decyzyjne.
     *
     * @param tab Tablica treningowa.
     * @param dec1 Wartosc pierwszej decyzji.
     * @param dec2 Wartosc drugiej decyzji.
     * @param convergance Wspolczynnik zbieznosci.
     * @param per Perceptron dostarczajacy poczatkowe wagi wejsc.
     * @return Wyuczony perceptron.
     */
    /**
     * Trains a perceptron in this way that it discriminates
     * one decision class from the rest.
     *
     * @param tab         Training table.
     * @param dec1        First decision to be discriminated.
     * @param dec2        Second decision to be discriminated.
     * @param convergence Convergence coefficient.
     * @param per         Perceptron with weights for initialisation,
     *                    if null the method create new perceptron.
     * @return            Trained perceptron.
     */
    public static Perceptron discriminatePair(DoubleDataTable tab, int dec1, int dec2, double convergence, Perceptron per)
    {
        DoubleFunction[] attributes = null;
        double[] coefficients = null;
        if (per != null) coefficients = per.m_arrCoefficients;
        for (DoubleData dObj : tab.getDataObjects())
        {
            if (attributes == null)
            {
                attributes = initAttributes(dObj.attributes().noOfAttr());
                if (coefficients == null) coefficients = initCoefficients(dObj.attributes().noOfAttr());
            }
            double sk = 0;
            for (int i = 0; i < coefficients.length; i++) sk += coefficients[i]*dObj.get(i);
            double delta = 0;
            double dec  = ((DoubleDataWithDecision)dObj).getDecision();
            if (dec==dec1 || dec==dec2)
            {
                if (sk <= 0) delta = 2;
            }
            else
            {
                if (sk > 0) delta = -2;
            }
            for (int i = 0; i < coefficients.length; i++) coefficients[i] += convergence*delta*dObj.get(i);
        }
        return new Perceptron(attributes, coefficients);
    }
}

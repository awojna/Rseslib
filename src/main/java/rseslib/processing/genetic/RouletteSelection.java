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


package rseslib.processing.genetic;

import java.util.Random;

/**
 * @author Rafal Latkowski
 * 
 */
public class RouletteSelection implements Selection
{
    protected Random m_randomNumberGenerator;

    /**
     * 
     */
    public RouletteSelection(Random rnd)
    {
        m_randomNumberGenerator = rnd;
    }

    void normalizeFitness(double fitness[])
    {
        for (int i = 1; i < fitness.length; i++)
            fitness[i] += fitness[i - 1];
        int last = fitness.length - 1;
        for (int i = 0; i < fitness.length; i++)
            fitness[i] = fitness[i] / fitness[last];
    }

    /**
     * @see rseslib.processing.genetic.Selection#selectNewPopulation(java.util.Collection,
     *      int)
     */
    public GAElement[] selectNewPopulation(GAElement[] prev_population, int size)
    {
        double[] fitness = new double[prev_population.length];
        for (int i = 0; i < prev_population.length; i++)
            fitness[i] = prev_population[i].fitness();
        normalizeFitness(fitness);
        GAElement[] new_population = new GAElement[size];
        double val;
        int j;
        for (int i = 0; i < size; i++)
        {
            val = m_randomNumberGenerator.nextDouble();
            j = 0;
            while (fitness[j] < val && j < fitness.length)
                j++;
            new_population[i] = prev_population[j];
        }
        return new_population;
    }

}

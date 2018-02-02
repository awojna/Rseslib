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
public class TournamentSelection implements Selection
{
    protected Random m_randomNumberGenerator;

    protected int m_nTournamentSize;

    /**
     * 
     */
    public TournamentSelection(Random rnd, int aTournamentSize)
    {
        m_randomNumberGenerator = rnd;
        m_nTournamentSize = aTournamentSize;
    }

    /**
     * 
     * 
     * @see rseslib.processing.genetic.Selection#selectNewPopulation(rseslib.processing.genetic.GAElement[],
     *      int)
     */
    public GAElement[] selectNewPopulation(GAElement[] prev_population, int size)
    {
        GAElement[] new_population = new GAElement[size];
        int individual_counter;
        int tournament_counter;
        int choosen_individual;
        int best_individual;
        double best_fitness;

        for (individual_counter = 0; individual_counter < size; individual_counter++)
        {
            best_individual = m_randomNumberGenerator.nextInt(prev_population.length);
            best_fitness = prev_population[best_individual].fitness();

            for (tournament_counter = 1; tournament_counter < m_nTournamentSize; tournament_counter++)
            {
                choosen_individual = m_randomNumberGenerator.nextInt(prev_population.length);
                if (best_fitness < prev_population[choosen_individual].fitness())
                {
                    best_individual = choosen_individual;
                    best_fitness = prev_population[choosen_individual].fitness();
                }
            }
            new_population[individual_counter] = prev_population[best_individual];
        }
        return new_population;
    }
}

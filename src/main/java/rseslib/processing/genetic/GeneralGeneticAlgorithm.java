/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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

/**
 * @author Rafal Latkowski
 *
 */
public class GeneralGeneticAlgorithm
{
    int m_nSelectedPopulationSize;
    int m_nMutatedPopulationSize;
    GeneticVariabilityProvider m_oMutation;
    Selection m_oSelection;
    
    /**
     * 
     */
    public GeneralGeneticAlgorithm(int aSelectedPopulationSize,int aMutatedPopulationSize,GeneticVariabilityProvider aMutation,Selection aSelection)
    {
        m_nSelectedPopulationSize = aSelectedPopulationSize;
        m_nMutatedPopulationSize = aMutatedPopulationSize;
        m_oMutation = aMutation;
        m_oSelection = aSelection;
    }
    
    public GAElement[] iterate(GAElement[] prev_population)
    {
        return m_oSelection.selectNewPopulation(m_oMutation.mutation(prev_population,m_nMutatedPopulationSize),m_nSelectedPopulationSize);
    }

    public GAElement[] iterate(GAElement[] population,int number_of_iterations)
    {
        for (int i=0;i<number_of_iterations;i++)
            population = iterate(population);
        return population;
    }
}

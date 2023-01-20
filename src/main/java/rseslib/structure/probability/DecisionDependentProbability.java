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


package rseslib.structure.probability;

/**
 * Interface for conditional probability of values given a decision.
 *
 * @author Lukasz Ligowski
 */
public interface DecisionDependentProbability
{
	/**
	 * Estimation of the conditional probability of a value
	 * given a decision.
	 *
	 * @param value 	Value.
	 * @param decision 	Local code of a decision.
	 * @return 			Conditional probability of the value.
	 */
	public double getProbability(double value, int decision);
}

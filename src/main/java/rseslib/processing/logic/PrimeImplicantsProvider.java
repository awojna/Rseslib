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


package rseslib.processing.logic;

import java.util.BitSet;
import java.util.Collection;

/**
 * Interface for generating prime implicants
 * from a conjunction of clauses (CNF),
 * where each clause is a disjunction of positive literals
 * over a set of boolean variables.
 * Clauses are limited to positive literals only,
 * this interface does not allow negative literals.
 * 
 * @author Rafal Latkowski
 */
public interface PrimeImplicantsProvider
{
    /**
     * Generates prime implicants from a positive CNF formula.
     * All clauses are provided as a collection.
     * Each disjunctive clause is represented by a BitSet object,
     * get(i) returns true if and only if
     * the positive literal of the i-th variable occurs in this clause.
     * The method returns collection of prime implicants of the formula.
     * Each prime implicant is represented by a BitSet object,
     * get(i) returns true if and only if
     * the positive literal of the i-th variable occurs in this implicant.
     * 
     * @param cnf 	CNF formula
     * @param width	Number of boolean variables used in the CNF formula.
     * @return 		Collection of prime implicants.
     */
    Collection<BitSet> generatePrimeImplicants(Collection<BitSet> cnf, int width);
}

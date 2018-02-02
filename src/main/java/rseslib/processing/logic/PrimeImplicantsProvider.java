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


package rseslib.processing.logic;

import java.util.BitSet;
import java.util.Collection;

/**
 * Interface for generating prime implicants
 * from a collection of clauses (CNF).
 * 
 * @author Rafal Latkowski
 */
public interface PrimeImplicantsProvider
{
    /**
     * Generates prime implicants from positive cnf formula.
     * CNF formula is represented as a concjunction of elements
     * stored in collection.
     * Each element of collection represents disjunction of variables
     * stored as booleans in boolean vector.
     * 
     * @param cnf 	CNF formula
     * @return 		Collection of prime implicants.
     */
    Collection<BitSet> generatePrimeImplicants(Collection<BitSet> cnf,int width);
}

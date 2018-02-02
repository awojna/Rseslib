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


package rseslib.structure.function.intval;

/**
 * Function that discriminates data objects
 * into a number of categories (branches).
 *
 * @author      Arkadiusz Wojna
 */
public interface Discrimination extends IntegerFunction
{
    /**
     * Returns the number of branches.
     *
     * @return int Number of discrimination branches.
     */
    public abstract int noOfValues();

    /**
     * Outputs a description of a discrimination for a given branch.
     *
     * @param branch The branch index for which the description is to be returned.
     * @return Description of a discrimination for a given branch.
     */
    public abstract String toString(int branch);
}

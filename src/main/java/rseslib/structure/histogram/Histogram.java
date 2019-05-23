/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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


package rseslib.structure.histogram;

/**
 * Value histogram of an attribute in a set of data object.
 *
 * @author      Arkadiusz Wojna
 */
public interface Histogram
{
    /**
     * Returns the number of the histogram positions,
     * i.e. the number of different values
     * in a set used to define this histogram.
     *
     * @return	Number of the histogram positions.
     */
    public int size();

    /**
     * Returns the value at a given position in this histogram.
     *
     * @param valIndex	Position in this histogram.
     * @return			Value at the given position.
     */
    public double value(int valIndex);

    /**
     * Retuns the number of the occurences of the value
     * from a given position in this histogram.
     *
     * @param valIndex	Position in this histogram.
     * @return			Number of the value occurrences.
     */
    public int amount(int valIndex);
    
    /**
     * Returns the number of all non-missing values.
     * 
     * @return Number of all non-missing values.
     */
    public int totalAmount();
}

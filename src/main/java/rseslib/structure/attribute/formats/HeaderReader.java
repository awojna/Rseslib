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


package rseslib.structure.attribute.formats;

import java.util.Collection;

import rseslib.structure.attribute.Attribute;

/**
 * Interface for data header reader.
 *
 * @author      Arkadiusz Wojna
 */
public interface HeaderReader
{
    /**
     * Returns the set of all strings denoting missing value.
     *
     * @return Set of all strings denoting missing value.
     */
    public abstract Collection<String> allMissing();

    /**
     * Returns missing value.
     *
     * @return String that denotes the missing value.
     */
    public abstract String singleMissing();

    /**
     * Returns the bit mask indicating
     * which original attributes are to be read in
     * while loading data from file.
     *
     * @return The bit mask where true at a position i
     *         indicates that attribute i is to be read in
     *         and false indicates that the attribute is to be skipped.
     */
    public abstract boolean[] bitMaskOfLoaded();

    /**
     * Returns the information about attributes (loaded only).
     *
     * @return Array of attributes.
     */
    public abstract Attribute[] attributesForLoading();
}

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


package rseslib.processing.reducts;

import java.util.BitSet;
import java.util.Collection;

import rseslib.structure.data.DoubleData;

/**
 * Interface for algorithms computing local reducts.
 * A data table is provided as the argument of the constructor.
 *
 * @author Rafal Latkowski
 */
public interface LocalReductsProvider
{
    /**
     * Returns a set of local reducts of a given data object.
     * Each reduct is represented by a BitSet object,
     * get(i) returns true if and only if the i-th attribute belongs to the reduct.
     * The attribute indices are defined by the header of the data table.
     *
     * @param object	Data object used to compute local reducts.
     * @return			Set of local reducts.
     */
    public Collection<BitSet> getSingleObjectReducts(DoubleData object);
}

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

/**
 * Interface for algorithms computing global reducts.
 * A data table is provided as the argument of the constructor.
 *
 * @author Rafal Latkowski
 */
public interface GlobalReductsProvider
{
    /**
     * Returns a set of global reducts.
     * Each reduct is represented by a BitSet object,
     * get(i) returns true if and only if the i-th attribute belongs to the reduct.
     * The attribute indices are defined by the header of the data table.
     *
     * @return	Set of global reducts.
     */
    public Collection<BitSet> getReducts();
}

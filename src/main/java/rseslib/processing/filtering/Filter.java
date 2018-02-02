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


package rseslib.processing.filtering;

import java.util.Collection;

import rseslib.structure.data.DoubleData;

/**
 * Filter selects data objects.
 *
 * @author      Arkadiusz Wojna
 */
public interface Filter
{
    /**
     * Returns a collection of data objects
     * selected from an original collection.
     *
     * @param dataColl Collection of data object to be filtered.
     * @return          Filtered collection of data objects.
     */
    public abstract Collection<DoubleData> select(Collection<DoubleData> dataColl);
}
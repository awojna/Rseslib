/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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

import java.util.ArrayList;
import java.util.Collection;

import rseslib.structure.data.DoubleData;
import rseslib.structure.function.booleanval.BooleanFunction;

/**
 * Filter selecting all objects matching given boolean function.
 *
 * @author      Arkadiusz Wojna
 */
public class BooleanFunctionFilter
{
    /**
     * Selects all objects matching given boolean function.
     *
     * @param dataColl	Collection of data object to be filtered.
     * @param function	Boolean function to be matched.
     * @return          Filtered set of data objects.
     */
    public static ArrayList<DoubleData> select(Collection<DoubleData> dataColl, BooleanFunction function)
    {
    	ArrayList<DoubleData> filteredObjects = new ArrayList<DoubleData>();
    	for (DoubleData obj : dataColl)
    		if (function.booleanVal(obj)) filteredObjects.add(obj);
        return filteredObjects;
    }
}

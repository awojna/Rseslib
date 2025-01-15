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


package rseslib.processing.filtering;

import java.util.ArrayList;
import java.util.Collection;

import rseslib.structure.data.DoubleData;

/**
 * Filter selecting all objects
 * that have a defined value on a given attribute.
 *
 * @author      Arkadiusz Wojna
 */
public class MissingValuesFilter
{
    /**
     * Selects all objects that have a defined value on a given attribute.
     *
     * @param dataColl	Collection of data object to be filtered.
     * @param attr		Attribute index to be checked for missing values.
     * @return          Filtered set of data objects.
     */
    public static ArrayList<DoubleData> select(Collection<DoubleData> dataColl, int attr)
    {
    	ArrayList<DoubleData> filteredObjects = new ArrayList<DoubleData>();
    	for (DoubleData obj : dataColl)
    		if (!Double.isNaN(obj.get(attr))) filteredObjects.add(obj);
        return filteredObjects;
    }
}

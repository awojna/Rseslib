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


package rseslib.processing.transformation;

import java.util.ArrayList;

import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;

/**
 * TableTransformer is an utility class that automates
 * process of transforming whole data tables.
 * 
 * @author Rafal Latkowski
 */
public class TableTransformer
{
    /**
     * Performs data table transformation. 
     * Takes a table as an input,
     * and produces a new table
     * with transformed objects.
     * 
     * @param table				Data table to be transformed.
     * @param discretization	Definition of discretization.
     * @return 					Discretized data table.
     */
    public static DoubleDataTable transform(DoubleDataTable table, Transformer transformation)
    {
        ArrayList<DoubleData> transformed_objects = new ArrayList<DoubleData>(table.noOfObjects());
        for (DoubleData object : table.getDataObjects())
        	transformed_objects.add(transformation.transformToNew(object));
        return new ArrayListDoubleDataTable(transformed_objects);
    }
}

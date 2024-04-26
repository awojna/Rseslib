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


package rseslib.processing.classification.tree.c45;

import java.util.Collection;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.function.intval.Discrimination;

/**
 * Interface for a generator of discrimination functions.
 *
 * @author      Arkadiusz Wojna
 */
public interface DiscriminationProvider
{
    /**
     * Returns the function discriminating data objects
     * into a number of branches.
     *
     * @param dataSet   Collection of data objects used to select the best discrimination function.
     * @param hdr       The header for the data collection.
     * @return          The function that represents the selected discrimination function.
     *                  The function returns the values in the range from 0 to n-1,
     *                  where n is the number of branches. It returns null, if the data set
     *                  is recognised to constitute a leaf in a decision tree.
     */
    public abstract Discrimination getDiscrimination(Collection<DoubleData> dataSet, Header hdr);
}

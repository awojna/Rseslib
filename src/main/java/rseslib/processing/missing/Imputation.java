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


package rseslib.processing.missing;

import rseslib.structure.table.DoubleDataTable;

/**
 * Interface for methods imputing some missing values in a data table.
 * Imputation methods does not have to replace all missing values. 
 * 
 * @author Rafal Latkowski
 */
public interface Imputation
{
	/**
	 * Returns a table with imputed missing values.
	 * 
	 * @param aTable	Original data table with missing values.
	 * @return			New data table with imputed missing values.
	 */
    public DoubleDataTable imputation(DoubleDataTable aTable);
}

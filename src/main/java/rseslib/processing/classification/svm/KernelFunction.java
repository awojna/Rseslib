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


package rseslib.processing.classification.svm;

import rseslib.structure.data.DoubleData;

/**
 * Interface for kernel functions
 */
public interface KernelFunction {
    /**
     * Returns the value of kernel function.
     *
     * @param dObj1   The first data object to be compared.
     * @param dObj2   The second data object to be compared.
     * @return        The value of kernel function.
     */
    public abstract double K(DoubleData dObj1, DoubleData dObj2);
    
}

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

package rseslib.processing.classification.parameterised.knn.rionida;

/**
 * Class describing point in multidimensional space of parameters.
 * 
 * @author Grzegorz Gora
 */
public class ValuesForParameters {

	/** value for k (value in first dimension) */
	public int kValue;
	/** value for p  (value in second dimension) */
	public double pValue;
	/** value for s (value in third dimension) */
	public double sMinValue;
	/** value for s (value in fourth dimension) */
	public double sMajValue;

    /**
     * Returns a string representation of this object.
     *
     * @return String representation of this object.
     */
	public String toString() {
		return "k=" +  kValue + ", p=" + pValue + ", s=" + sMinValue;
	}

}

/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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


package rseslib.structure.indiscernibility;

/**
/**
 * In this indiscernibility relation the missing value is similar to any value
 * but the defined values are different from the missing value.
 *   
 * @author Rafal Latkowski
 */
public class NonsymmetricSimilarityIndiscernibility extends AbstractIndiscernibility
{
	/** Serialization version. */
	private static final long serialVersionUID = 1L;

    /**
	 * Defines the indiscernibility between two values of an attribute.
	 * 
	 * @param value1	First value to be compared.
	 * @param value2	Second value to be compared.
	 * @param attribute	Attribute index.
	 * @return			True if the values are indiscernible false otherwise.
     */
    public boolean similar(double value1, double value2, int attribute)
    {
        return (value1==value2) || Double.isNaN(value1);
    }

    public boolean equals(Object o)
    {
        return (o instanceof NonsymmetricSimilarityIndiscernibility);
    }


    public int hashCode()
    {
               //NONSSIMI
        return 0x66677464;
    }
}

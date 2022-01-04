/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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
 * @author Rafal Latkowski
 * 
 */
public class ClassicIndiscernibility extends AbstractIndiscernibility
{
	/** Serialization version. */
	private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public ClassicIndiscernibility()
    {
    }

    /**
     * @see rseslib.structure.indiscernibility.AbstractIndiscernibility#similar(double, double, int)
     */
    public boolean similar(double value1, double value2, int attribute)
    {
        return value1==value2||(Double.isNaN(value1)&&Double.isNaN(value2));
    }

    public boolean equals(Object o)
    {
        return (o instanceof ClassicIndiscernibility);
    }

    public int hashCode()
    {
               //CLASIIND
        return 0x25274463;
    }
}

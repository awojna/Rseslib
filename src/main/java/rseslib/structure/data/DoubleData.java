/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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


package rseslib.structure.data;

import java.io.BufferedWriter;
import java.io.IOException;

import rseslib.structure.Headerable;

/**
 * Interface for objects with double value attributes.
 *
 * @author      Arkadiusz Wojna
 */
public interface DoubleData extends Headerable, Cloneable
{
    /**
     * Sets the value of a given attribute to a given double value.
     *
     * @param atrNo Index of the attribute to be changed.
     * @param value Double attribute value.
     */
    public abstract void set(int atrNo, double value);

    /**
     * Returns the double value of a given attribute.
     *
     * @param atrNo Index of the attribute to be returned.
     * @return      Double attribute value.
     */
    public abstract double get(int atrNo);

    /**
     * Returns filed-by-field copy of this object.
     * @return copy of this object.
     * @see java.lang.Object#clone()
     */
    public abstract Object clone();
    
    /**
     * Returns true for equivallent data object.
     * @param obj - object for comparison 
     * @return true if data object is equivallent.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj);

    /**
     * Writes this object.
     *
     * @param output                          Output for writing.
     * @throws IOException                    if an I/O error has occured.
     */
    public abstract void store(BufferedWriter output) throws IOException;

    /**
     * Writes this object in arff format.
     *
     * @param output Output for writing.
     * @throws IOException If an I/O error has occured.
     */
    public abstract void storeArff(BufferedWriter output) throws IOException;
}

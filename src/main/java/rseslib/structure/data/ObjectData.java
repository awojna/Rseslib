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


package rseslib.structure.data;

import rseslib.structure.Headerable;

/**
 * Interface for data objects with object value attributes.
 *
 * @author      Arkadiusz Wojna
 */
public interface ObjectData extends Headerable
{

    /**
     * Sets the value of a given attribute to a given object value.
     *
     * @param atrNo Index of the attribute to be changed.
     * @param value Object attribute value.
     */
    public abstract void setObjectAttrVal(int atrNo, Object value);

    /**
     * Returns the object value of a given attribute.
     *
     * @param atrNo Index of the attribute to be returned.
     * @return      Object attribute value.
     */
    public abstract Object getObjectAttrVal(int atrNo);

}

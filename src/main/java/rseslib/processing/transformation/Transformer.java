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


package rseslib.processing.transformation;

import rseslib.structure.data.DoubleData;

/**
 * Transformer converts attribute values for double data.
 *
 * @author      Arkadiusz Wojna
 */
public interface Transformer
{
    /**
     * Transforms double data to new data object and returns the new object.
     * The orgininal data remains unchanged.
     *
     * @param dObj Data object to be transformed.
     * @return     Data object with transformed attribute values.
     */
    public abstract DoubleData transformToNew(DoubleData dObj);
}

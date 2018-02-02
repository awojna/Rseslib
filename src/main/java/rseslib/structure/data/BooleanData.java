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


package rseslib.structure.data;

/**
 * Interface for objects with boolean attributes.
 *
 * @author      Arkadiusz Wojna
 */
public interface BooleanData
{
    /**
     * Sets decision.
     *
     * @param decVal Decision value.
     */
    public abstract void setDecision(double decVal);

    /**
     * Returns decision.
     *
     * @return Decision value.
     */
    public abstract double getDecision();

    /**
     * Sets the value of a given attribute to a given boolean value.
     *
     * @param atrNo Index of the attribute to be changed.
     * @param value Boolean attribute value.
     */
    public abstract void setBooleanAttrVal(int atrNo, boolean value);

    /**
     * Returns the boolean value of a given attribute.
     *
     * @param atrNo Index of the attribute to be returned.
     * @return      Boolean attribute value.
     */
    public abstract boolean getBooleanAttrVal(int atrNo);
}

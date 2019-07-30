/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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


package rseslib.structure.rule;

import rseslib.structure.data.DoubleData;

/**
 * Interface for a rule.
 *
 * @author      Arkadiusz Wojna
 */
public interface Rule
{
    /**
     * Checks whether this rule matches a given data object.
     *
     * @param dObj Object to be checked.
     * @return     True if this rule matches the object, false otherwise.
     */
    public abstract boolean matches(DoubleData dObj);
    
    /**
     * Returns the decision of this rule.
     *
     * @return Decision of this rule.
     */
    public abstract double getDecision();
}

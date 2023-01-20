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


package rseslib.processing.discernibility;

import rseslib.structure.data.DoubleData;

/**
 * Interface for implementation of a generalized decision.
 * 
 * @author Rafal Latkowski
 */
public interface GeneralizedDecisionProvider
{
	/**
	 * Returns true if two objects have the same generalized decision.
	 * 
	 * @param object1	First object to be compared.
	 * @param object2	Second object to be compared.
	 * @return			True if two objects have the same generalized decision.
	 */
    public boolean haveTheSameDecision(DoubleData object1, DoubleData object2);
    
    /**
     * Returns a string representing the generalized decision for a given object.
     * 
     * @param object	Object for which a generalized decision is calculated.
     * @return			Generalized decision for the object.
     */
    public String getDecisionForObject(DoubleData object);
}

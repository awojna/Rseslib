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


package rseslib.structure.metric;

import rseslib.structure.data.DoubleDataWithDecision;

/**
 * Information about a neighbour of a data object.
 * It contains the data of this neighbour, the distance
 * to the reference data object and consistency information.
 *
 * @author      Arkadiusz Wojna, Grzegorz Gora
 */
public class Neighbour implements Comparable
{
    /** Data of this neighbour. */
    private DoubleDataWithDecision m_Neighbour;
    /** The distance to the reference data object. */
    private double m_nNeighbourDist;
    /** The neighbour object id. */
    int m_nId;
    /** Flag indicating whether this neighbour is consistent with the reference data object. */
    public boolean m_bConsistent;
    /** Array of flags indicating consistency of this neighbour with the reference data object on different levels. */
    public boolean[] m_bConsistentOnLevel = null;

    /**
     * Constructor.
     *
     * @param neighbour     Data of this neighbour.
     * @param neighbourDist The distance to the reference data object.
     * @param id			Identifier of this neighbour.
     */
    public Neighbour(DoubleDataWithDecision neighbour, double neighbourDist, int id)
    {
        m_Neighbour = neighbour;
        m_nNeighbourDist = neighbourDist;
        m_nId = id;
    }

    /**
     * Returns data of this neighbour.
     *
     * @return Data of this neighbour.
     */
    public DoubleDataWithDecision neighbour()
    {
	return m_Neighbour;
    }

    /**
     * Returns the distance to the reference data object.
     *
     * @return Distance to the reference data object.
     */
    public double dist()
    {
	return m_nNeighbourDist;
    }

    /**
     * Sets the distance to the reference data object.
     *
     * @param neighbourDist Distance to the reference data object.
     */
    public void setDist(double neighbourDist)
    {
        m_nNeighbourDist = neighbourDist;
    }

    /**
     * Checks consistency of this neighbour on the level selected in dependence on
     * whether the decision of the neighbour is the same as indicated by the parameter.
     * 
     * @param decision			Decision value.
     * @param levelForSameDec	Level used if the decision of this neighbour is the same as the 'decision' parameter. 
     * @param levelForOtherDec	Level used if the decision of this neighbour is different from the 'decision' parameter.
     * @return					True if this neighbour is consistent with the reference object on a given level.					
     */
    public boolean isConsistentOnLevel(double decision, int levelForSameDec, int levelForOtherDec) {
    	return m_bConsistentOnLevel[m_Neighbour.getDecision() == decision ? levelForSameDec : levelForOtherDec];
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * 
     * @param obj	Object to be compared.
     * @return 		True if the other object is equal to this one.
     */
    public boolean equals(Object obj)
    {
        boolean result = false;
        if (obj instanceof Neighbour) {
        	Neighbour n = (Neighbour)obj;
            result = (m_nNeighbourDist == n.m_nNeighbourDist && m_nId == n.m_nId);
        }
        return result;
    }

    /**
     * Compares distances from the reference data object
     * between this neighbour and another data object.
     *
     * @param o Another neighbour to be compared.
     * @return  Value 1, 0 or -1 as the distance of this neighbour from the reference data object
     *          is greater, equal to or less than the distance of the given data object o.
     */
    public int compareTo(Object o)
    {
        if (((Neighbour)o).m_nNeighbourDist > m_nNeighbourDist) return -1;
        if (((Neighbour)o).m_nNeighbourDist < m_nNeighbourDist) return 1;
        if (((Neighbour)o).m_nId > m_nId) return -1;
        if (((Neighbour)o).m_nId < m_nId) return 1;
        return 0;
    }
    
    /**
     * Returns string representation of this neighbour.
     *
     * @return  String representation of this neighbour.
     */
    public String toString() {
    	return m_Neighbour.toString() + ", dist=" + m_nNeighbourDist;
    }
}

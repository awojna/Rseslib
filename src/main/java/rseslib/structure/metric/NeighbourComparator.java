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


package rseslib.structure.metric;

import java.util.Comparator;


/**
 * Comparator defining lexicographical order
 * on data objects treated as neighbours with a distance
 * to a given data object.
 *
 * @author      Arkadiusz Wojna
 */
public class NeighbourComparator implements Comparator<Neighbour>
{
    /**
     * Compares distances of two neighbours.
     *
     * @param o1 First neighbour to be compared.
     * @param o2 Second neighbour to be compared.
     * @return   Value 1, 0 or -1 as the first neighbour o1
     *           is lexicographically after, equal to or before
     *           the second neighbour o2.
     */
    public int compare(Neighbour o1, Neighbour o2)
    {
        if (o1.dist() < o2.dist()) return -1;
        if (o1.dist() > o2.dist()) return 1;
        if (o1.m_nId < o2.m_nId) return -1;
        if (o1.m_nId > o2.m_nId) return 1;
        return 0;
    }
}

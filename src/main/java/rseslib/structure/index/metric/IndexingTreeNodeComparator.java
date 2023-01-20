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


package rseslib.structure.index.metric;

import java.util.Comparator;

/**
 * Comparator comparing the weights of two nodes.
 *
 * @author      Arkadiusz Wojna
 */
public class IndexingTreeNodeComparator implements Comparator<IndexingTreeNode>
{
    /**
     * Compares the weights of two nodes.
     *
     * @param o1 The first node to be compared.
     * @param o2 The second node to be compared.
     * @return The value 1, 0 or -1 as the weight of the first node
     *         is greater, equal to or less than the weight of the second node.
     */
    public int compare(IndexingTreeNode o1, IndexingTreeNode o2)
    {
	if (o1.getWeight() < o2.getWeight()) return -1;
	if (o1.getWeight() > o2.getWeight()) return 1;
	if (o1.getRadius() < o2.getRadius()) return -1;
	if (o1.getRadius() > o2.getRadius()) return 1;
	if (o1.size() < o2.size()) return -1;
	if (o1.size() > o2.size()) return 1;
	int result = 0;
	for (int att = 0; att < o1.getCenter().attributes().noOfAttr() && result==0; att++)
		if (o1.getCenter().attributes().isConditional(att))
		{
			if (o1.getCenter().get(att) < o2.getCenter().get(att))
				result = -1;
            else if (o1.getCenter().get(att) > o2.getCenter().get(att))
            	result = 1;
		}
	return result;
    }
}

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


package rseslib.structure.linearorder;

import java.util.Comparator;

import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Metric;

/**
 * Comparator sorting objects by the distance to a reference object.
 *
 * @author      Arkadiusz Wojna
 */
public class DistanceComparator implements Comparator<DoubleData> 
{
	DoubleData m_Reference;
	Metric m_Metric;
	
	DistanceComparator(DoubleData reference, Metric metric)
	{
		m_Reference = reference;
		m_Metric = metric;
	}
	
	public int compare(DoubleData o1, DoubleData o2)
	{
		double dist1 = m_Metric.dist(m_Reference, o1);
		double dist2 = m_Metric.dist(m_Reference, o2);
		if (dist1 < dist2)
			return -1;
		if (dist1 > dist2)
			return 1;
		return 0;
	}
}

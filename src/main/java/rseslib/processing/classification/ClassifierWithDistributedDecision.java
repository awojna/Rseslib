/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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


package rseslib.processing.classification;

import rseslib.structure.data.DoubleData;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Statistics;

/**
 * Interface for classification methods that provide distributed decision.
 *
 * @author      Arkadiusz Wojna
 */
public interface ClassifierWithDistributedDecision extends Statistics
{
    /**
     * Returns a decision distribution vector
     * for a single test object.
     * The weight of each decision value is given
     * at the position of the vector
     * identifed by the local code of this decision value.
     *
     * @param dObj  Test object.
     * @return      Assigned decision distribution.
     */
    public abstract double[] classifyWithDistributedDecision(DoubleData dObj) throws PropertyConfigurationException;
}

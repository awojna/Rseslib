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


package rseslib.processing.metrics;

import rseslib.structure.metric.AbstractWeightedMetric;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.progress.Progress;

/**
 * Interface for methods adjusting
 * attribute weights in a metric.
 *
 * @author      Arkadiusz Wojna
 */
public interface WeightAdjuster
{
    /**
     * Applies a method to adjust weights of the metric metr.
     *
     * @param metr Metric used to adjust weights.
     * @param tab  Table of data objects used to adjust weights.
     * @param prog Progress object used to report progress.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public abstract void adjustWeights(AbstractWeightedMetric metr, DoubleDataTable tab, Progress prog) throws InterruptedException;
}

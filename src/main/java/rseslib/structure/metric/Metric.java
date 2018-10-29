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


package rseslib.structure.metric;

import rseslib.processing.transformation.AttributeTransformer;
import rseslib.structure.Headerable;
import rseslib.structure.data.DoubleData;

/**
 * Interface for metrics defining distance
 * between data objects with double values.
 *
 * @author      Grzegorz Gora, Arkadiusz Wojna
 */
public interface Metric extends Headerable
{
    /**
     * Turns off transformation of values inside metric
     * and passes the transformer out.
	 * From this moment a user must assure
	 * that objects are transformed
	 * before a distance computation function is called.
     * External transformation can be more effective:
     * each object can be transformed only once.
     * With internal transformation the values of an object
     * are transformed each time when it is used in
     * a distance computing function. 
     * 
     * @return    Transformer.
     */
    public abstract AttributeTransformer transformationOutside();

    /**
     * Returns the distance between two data objects.
     *
     * @param datObj1 First data object to be compared.
     * @param datObj2 Second data object to be compared.
     * @return        Distance between the objects datObj1 and datObj2.
     */
    public abstract double dist(DoubleData datObj1, DoubleData datObj2);
}

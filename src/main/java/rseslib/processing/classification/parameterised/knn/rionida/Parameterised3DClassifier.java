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

package rseslib.processing.classification.parameterised.knn.rionida;

import rseslib.processing.classification.Classifier;
import rseslib.structure.data.DoubleData;
import rseslib.system.PropertyConfigurationException;

/**
 * Interface for parameterised classification methods.
 *
 * @author Grzegorz Gora
 */
public interface Parameterised3DClassifier extends Classifier
{
//stare
//    /**
//     * Return name of the classifier parameter.
//     *
//     * @return Name of the classifier parameter.
//     */
//    public abstract String getParameterName();
//zmiana ggora 5.08.2016
    /**
     * Returns multi dimensional parameters
	 * @return class describing many parameters
	 */
	public MultiDimensionalParameters getMultiDimParams();

	
    /**
     * Assign a decision to a single test object
     * for different parameter values, where parameters are 3-dimensional (or in general multi-dimensional)
     *
     * @param dObj  Test object.
     * @return      Array of assigned decisions, indices correspond to parameter values.
     */
//    public abstract double[] classifyWith3DParameter(DoubleData dObj) throws PropertyConfigurationException;
    public abstract double[] classifyWithMultiParameter(DoubleData dObj) throws PropertyConfigurationException;
}

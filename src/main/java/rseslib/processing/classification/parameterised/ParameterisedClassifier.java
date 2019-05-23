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


package rseslib.processing.classification.parameterised;

import rseslib.processing.classification.Classifier;
import rseslib.structure.data.DoubleData;
import rseslib.system.PropertyConfigurationException;

/**
 * Interface for parameterised classification methods.
 *
 * @author      Arkadiusz Wojna
 */
public interface ParameterisedClassifier extends Classifier
{
    /**
     * Return name of the classifier parameter.
     *
     * @return Name of the classifier parameter.
     */
    public abstract String getParameterName();

    /**
     * Assign a decision to a single test object
     * for different parameter values.
     *
     * @param dObj  Test object.
     * @return      Array of assigned decisions, indices correspond to parameter values.
     */
    public abstract double[] classifyWithParameter(DoubleData dObj) throws PropertyConfigurationException;
}

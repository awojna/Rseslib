/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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

import javax.swing.JPanel;

import rseslib.structure.Headerable;
import rseslib.structure.data.DoubleData;

/**
 * Interface for classifiers with visualisation.
 *
 * @author      Maciej Pr�chniak, Arkadiusz Wojna
 */
public interface VisualClassifier extends Classifier, Headerable
{
    /**
     * Draws this classifier on a panel.
     *
     * @param canvas  Panel to draw.
     */
	public void draw(JPanel canvas);

	/**
     * Draws the classification of a single data object on a panel.
     *
     * @param canvas  Panel to draw.
     * @param dObj    Object to be classified.
     */
   public void drawClassify(JPanel canvas, DoubleData obj);
}

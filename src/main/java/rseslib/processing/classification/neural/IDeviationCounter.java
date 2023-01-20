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


package rseslib.processing.classification.neural;

import java.util.ListIterator;

/**
 * @author Jakub Sakowicz
 *
 * Interfejs obiektu, kt?ry potrafi liczyc bledy dla perceptronow
 */
public interface IDeviationCounter {
	/**
	 * Liczy bledy dla danych perceptronow
	 * @param perceptrons iterator po perceptronach, ktorym nalezy policzyc bledy
	 */
	public void countDeviations(ListIterator perceptrons);
}

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


package rseslib.processing.classification.neural;

import java.awt.Color;

/**
 * @author damian
 * 
 */
public class ColorChooser {

	static Color[] COLORS = { Color.blue, Color.gray, Color.green, Color.yellow,
			 Color.orange, Color.magenta, Color.red };

	static int COLOR_NO = 7;

	public static Color getColor(double weight, double min, double max) {
		for (int i = COLOR_NO - 1; i >= 0; i--) {
			if (weight >= (min + (max - min) * i / COLOR_NO))
				return COLORS[i];
		}
		return null;
	}

	public static String getBounds(int pozycja, double min, double max) {
		Double low, high;
		String s;
		int end = 5;
		
		if ((pozycja >= 0) && (pozycja < COLOR_NO)) {
			low = min + (max - min) * pozycja / COLOR_NO;
			if (end > low.toString().length()) end = low.toString().length();
			if (pozycja != (COLOR_NO - 1)) {
				high = min + (max - min) * (pozycja + 1) / COLOR_NO;
				if (end > high.toString().length()) end = high.toString().length();				
				s = low.toString().substring(0, end) + " <= weight < " + high.toString().substring(0, end);
			}
			else {
				s = low.toString().substring(0, end) + " <= weight < Inf";				
			}
			return s;
		} else
			return null;
	}
}

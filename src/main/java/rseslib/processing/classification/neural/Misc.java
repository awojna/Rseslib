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


package rseslib.processing.classification.neural;

import java.util.Random;


/**
 * @author Jakub Sakowicz
 *
 * Klasa oferujaca rozne, nietypowe rzeczy...
 * 
 */
public class Misc {
	/**
	 * Generator liczb losowych
	 */
	private static Random RANDOM_GEN = new Random(); 
	
	/**
	 * Zwraca losowa liczbe zmiennoprzecinkowa z zakresu [0..1]
	 * @return
	 */
	public static double getRandomDouble() {
		return RANDOM_GEN.nextDouble();
	}
	
	/**
	 * 
	 * @author Jakub Sakowicz
	 *
	 * Klasa reprezentujaca funkcje 1/(1+(e^(-z)))
	 * 
	 */
	public static class OneOverOnePlusExpOfMinusZ implements IDoubleFunction {
		public double eval(double z) {			
			return 1.0/(1.0 + (1.0 / Math.exp(z)));
		}
				
		/**
		 * 
		 * @author Jakub Sakowicz
		 *
		 * Klasa reprezentujaca funkcje y * (1 - y)
		 * 
		 */
		private static class MyDifferential implements IDoubleFunction {
			public double eval(double y) {
				return y * (1 - y);
			}
		}
		
		/**
		 * Pochodna tej funkcji, potrafiaca sie liczyc z jej wartosci
		 * @return
		 */
		public static IDoubleFunction valueDifferential() {
			return new MyDifferential();			
		}
	}
	
}

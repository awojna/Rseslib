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


package rseslib.processing.classification.neural;


/**
 * @author Jakub Sakowicz
 *
 * Globalna konfiguracja sieci neuronowej. Parametry zostaly dobrane eksperymentalnie.
 * Jednym z najwazniejszych jest TIME_LIMIT, ktory okresla ograniczenie czasowe dla fazy uczenia.
 * 
 */
public class Global {
	/**
	 * Nazwy wykorzystywanych properties
	 */
	public static String INITIAL_ALFA_NAME = "initialAlpha";
	public static String TIME_LIMIT_NAME = "timeLimit";
	public static String DEST_TARGET_RATIO_NAME = "targetAccuracy";
	
	
	/**
	 * Poczatkowa wartosc wspolczynnika ALFA dla back-prop-update
	 */
	public static double INITIAL_ALFA = 0.9;
	/**
	 * Mnoznik dla wspolczynnika ALFA - powodujacy jego zmniejszanie, 
	 * gdy skutecznosc sieci bedzie dosc duza
	 */
	public static double MULT_ALFA = 0.9;
	/**
	 * Minimalne wartosc ponizej ktorej nie bedzie zmniejszana ALFA
	 */
	public static double MIN_ALFA = 0.005;
	
	/**
	 * Aktualna wartosc wspolczynnika ALFA - uzywane 
	 */
	public static double CURRENT_ALFA;
	
	/**
	 * Funkcja wygladzajaca
	 */
	public static IDoubleFunction FUNCTION = new Misc.OneOverOnePlusExpOfMinusZ();
	/**
	 * Pochodna funkcji wygladzajacej
	 */
	public static IDoubleFunction DIFFERENTIAL = Misc.OneOverOnePlusExpOfMinusZ.valueDifferential();
	
	/**
	 * Ilosc warstw w sieci
	 */
	public static int NO_OF_LAYERS = 3;
	
	/**
	 * Czas po jakim siec zostanie uznana za niezdolna do nauczenia sie czegos nowego, 
	 * jesli nie odnotowuje sie postepu
	 */
	public static int GRACE_LEARN_PERIOD = 6;
	
	/**
	 * Maksymalna ilosc rekordow DoubleData do przeuczenia w jednej iteracji
	 */
	public static int MAX_ITER_SIZE = 1000;	
	/**
	 * Ilosc iteracji na jedna ture
	 */	
	public static int ITER_COUNT = 75; 
	/**
	 * Maksymalna ilosc tur
	 */
	public static int MAX_REPEAT_COUNT = 999999;	
	
			
	/**
	 * Wspolczynnik powyzej ktorego siec nie bedzie juz uczona
	 */
	public static double DEST_TARGET_RATIO = 99.9;
}

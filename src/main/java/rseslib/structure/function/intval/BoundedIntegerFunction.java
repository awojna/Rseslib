/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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


package rseslib.structure.function.intval;

/**
 * @author Rafal Latkowski
 */
public interface BoundedIntegerFunction extends IntegerFunction
{
	/**
	 * Returns the maximal value of this function (upper bound).
	 *
	 * @return Maximal value of this function.
	 */
	public abstract int maxIntValue();

	/**
	 * Returns the minimal value of this function (lower bound).
	 *
	 * @return Minimal value of this function.
	 */
	public abstract int minIntValue();
}

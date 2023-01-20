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


package rseslib.system;

/**
 * This is an exception that is thrown
 * whenever there is a problem
 * with the properties of configurable objects.
 *
 * @author  Arkadiusz Wojna
 */
public class PropertyConfigurationException extends Exception
{
    /** Serial version number */
	private static final long serialVersionUID = 1L;

   /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public PropertyConfigurationException(String message)
    {
        super(message);
    }
}

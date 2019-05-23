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


package rseslib.structure.attribute.formats;

/**
 * This is an exception that is thrown
 * if the format of data header is bad in a file.
 *
 * @author  Arkadiusz Wojna
 */
public class HeaderFormatException extends Exception
{
    /** Serial version number */
	private static final long serialVersionUID = 1L;

   /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The detail message.
     */
    public HeaderFormatException(String message)
    {
        super(message);
    }
}

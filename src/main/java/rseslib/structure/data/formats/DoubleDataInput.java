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


package rseslib.structure.data.formats;

import java.io.IOException;

import rseslib.structure.Headerable;
import rseslib.structure.data.DoubleData;

/**
 * Interface for input stream of double data.
 *
 * @author      Arkadiusz Wojna
 */
public interface DoubleDataInput extends Headerable
{
    /**
     * Returns true if there is more data to be read, false otherwise.
     *
     * @return True if there is more data to be read, false otherwise.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public abstract boolean available() throws IOException, InterruptedException;

    /**
     * Reads a new data from this stream.
     *
     * @return Read data.
     * @throws IOException           If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public abstract DoubleData readDoubleData() throws IOException, DataFormatException, InterruptedException;
}

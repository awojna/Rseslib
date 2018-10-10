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


package rseslib.qmak.dataprocess;

import java.io.File;
import java.io.IOException;

import rseslib.system.progress.Progress;

/**
 * Interface for objects that can be saved in a file.
 * Such objects require also
 * the constructor with the two arguments (File, Progress)
 * that loads the object from a file.
 *
 * @author      Arkadiusz Wojna
 */
public interface FileStoreable
{
    /**
     * Saves this object to a file.
     *
     * @param outputFile File to be used for storing this object.
     * @param prog       Progress object for progress reporting.
     * @throws IOException If an I/O error has occured.
     * @throws InterruptedException If user has interrupted saving object.
     */
    public abstract void store(File outputFile, Progress prog) throws IOException, InterruptedException;
}

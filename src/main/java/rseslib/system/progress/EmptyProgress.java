/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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


package rseslib.system.progress;

/**
 * Progress that accepts progress steps and do nothing.
 *
 * @author      Arkadiusz Wojna
 */
public class EmptyProgress implements Progress
{
    /**
     * Sets the total number of steps to be done.
     *
     * @param name      Name of this progress.
     * @param noOfSteps Number of steps in this progress.
     */
    public void set(String name, int noOfSteps)
    {
    }

    /**
     * Makes a single step.
     *
     * @throws InterruptedException When the process is requested to be stopped.
     */
    public void step() throws InterruptedException
    {
    }
}

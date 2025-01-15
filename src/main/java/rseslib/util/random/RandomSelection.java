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


package rseslib.util.random;

import java.util.Random;

/**
 * The class for performing random operations.
 *
 * @author      Arkadiusz Wojna
 */
public class RandomSelection
{
    /** Random number generator. */
    private static final Random RANDOM_GENERATOR = new Random();

    /**
     * Selects a random fraction of indices from a given range
     * with the ratio noOfPartsToBeSelected to noOfPartsToBeLeft.
     *
     * @param range                  Range of indices for selection.
     * @param noOfPartsToBeSelected  Number of parts to be selected in the given range.
     * @param noOfPartsToBeLeft      Number of parts to be left in the given range.
     * @return                       Boolean array with selected indices set to true.
     */
    public static boolean[] subset(int range, int noOfPartsToBeSelected, int noOfPartsToBeLeft)
    {
        boolean[] assigned = new boolean[range];
        int selected = 0;
        while (selected*(noOfPartsToBeSelected+noOfPartsToBeLeft) < assigned.length*noOfPartsToBeSelected)
        {
            int ind = RANDOM_GENERATOR.nextInt(assigned.length);
            while (assigned[ind]) ind = RANDOM_GENERATOR.nextInt(assigned.length);
            assigned[ind] = true;
            selected++;
        }
        return assigned;

    }

}
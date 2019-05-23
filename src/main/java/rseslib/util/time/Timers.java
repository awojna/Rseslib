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


package rseslib.util.time;

import rseslib.system.Report;

/**
 * Timers.
 *
 * @author      Grzegorz Gora, Arkadiusz Wojna
 */
public class Timers
{
    /** The number of timers. */
    private static final int NUMBER_OF_TIMERS = 100;

    /** Switches indicating whether the corresponding timer is on. */
    private static boolean s_Running[] = new boolean[NUMBER_OF_TIMERS];
    /** Array of last timer starts. */
    private static long s_StartTime[] = new long[NUMBER_OF_TIMERS];
    /** Array of last timer stops. */
    private static long s_StopTime[] = new long[NUMBER_OF_TIMERS];
    /** Array of cumulated time from the last reset until the last stop. */
    private static long s_SumTime[] = new long[NUMBER_OF_TIMERS];

    /**
     * Resets all timers.
     */
    static
    {
        for (int i = 0; i < NUMBER_OF_TIMERS; i++) reset(i);
    }

    /**
     * Resets the n-th timer.
     *
     * @param n Timer index.
     */
    public static void reset(int n)
    {
        s_Running[n] = false;
        s_StartTime[n] = -1;
        s_StopTime[n] = -1;
        s_SumTime[n] = 0;
    }

    /**
     * Starts the n-th timer.
     *
     * @param n Timer index.
     */
    public static void start(int n)
    {
        s_StartTime[n] = System.currentTimeMillis();
        s_Running[n] = true;
    }

    /**
     * Stops the n-th timer and returns time in miliseconds from the last start.
     *
     * @param n Timer index.
     * @return  Time in miliseconds from the last start.
     */
    public static long stop(int n)
    {
        if (!s_Running[n])
        {
            Report.exception(new TimerException("Stop call of the "+n+"-th timer without prior start call"));
            return 0;
        }
        s_StopTime[n] = System.currentTimeMillis();
        s_Running[n] = false;
        s_SumTime[n] += (s_StopTime[n] - s_StartTime[n]);
        return (s_StopTime[n] - s_StartTime[n]);
    }

    /**
     * Returns time of the n-th timer in miliseconds.
     * It is time from the last start until
     * either now if the timer is running
     * or the last stop if the timer is off.
     *
     * @param n Timer index.
     * @return  Time of the n-th timer in miliseconds.
     */
    public static long getTime(int n)
    {
        if (s_StartTime[n] < 0)
        {
            Report.exception(new TimerException("The "+n+"-th timer must be started before reading time."));
            return 0;
        }
        if (s_Running[n]) return (System.currentTimeMillis() - s_StartTime[n]);
        return (s_StopTime[n] - s_StartTime[n]);
    }

    /**
     * Returns cumulated time of the n-th timer in miliseconds.
     * It is time from the last reset until
     * either now if the timer is running
     * or the last stop if the timer is off.
     *
     * @param n Indeks stopera.
     * @return Sumaryczny czas dzialania stopera.
     */
    public static long getCumulatedTime(int n)
    {
        if (s_StartTime[n] < 0)
        {
            Report.exception(new TimerException("The "+n+"-th timer must be started before reading time."));
            return 0;
        }
        if (s_Running[n]) return (s_SumTime[n] + (System.currentTimeMillis() - s_StartTime[n]));
        return s_SumTime[n];
    }

    /**
     * Displays time of the n-th timer.
     *
     * @param n Timer index.
     */
    public static void displayTime(int n)
    {
        Report.displaynl("Timer " + n + ": " + (getTime(n)/1000) + "s " +  (getTime(n)%1000) + "ms");
    }

    /**
     * Displays cumulated time of the n-th timer.
     *
     * @param n Timer index.
     */
    public static void displayCumulatedTime(int n)
    {
        Report.displaynl("Timer " + n + ": " + (getCumulatedTime(n)/1000) + "s " +  (getCumulatedTime(n)%1000) + "ms");
    }

    /**
     * Displays cumulated time of all timers.
     */
    public static void displayAllTimers()
    {
        Report.displaynl("Timers' info:");
        for (int i = 0; i < NUMBER_OF_TIMERS; i++)
            if (s_StartTime[i] > 0) displayCumulatedTime(i);
    }
}

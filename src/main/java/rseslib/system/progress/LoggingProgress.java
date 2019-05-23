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


package rseslib.system.progress;

import rseslib.system.Report;

/**
 * Progress displaying progress information
 * to a log stream.
 *
 * @author      Arkadiusz Wojna
 */
public class LoggingProgress extends AbstractProgress
{
    /** Percentage of this progress. */
    int m_nPercantage = 0;
    /** The current step of this progress. */
    int m_nCurrentPoint = -1;
    /** Time when the progress started. */
    long m_nStartTime;

    /**
     * Sets the total number of steps to be done.
     *
     * @param name      Name of this progress.
     * @param noOfSteps Number of steps in this progress.
     */
    public void set(String name, int noOfSteps)
    {
        super.set(name, noOfSteps);
        m_nCurrentPoint = 0;
        Report.displaynl(m_Name+":");
        Report.display("0");
        m_nStartTime = System.currentTimeMillis();
    }

    /**
     * Makes a single step.
     *
     * @throws InterruptedException When the process is requested to be stopped.
     */
    public void step() throws InterruptedException
    {
        if (m_nCurrentPoint == -1)
        {
            Report.exception(new ProgressException(m_Name+": progress not initialised"));
            return;
        }
        if (m_nCurrentPoint>=m_nEndPoint)
        {
            Report.exception(new ProgressException(m_Name+": more than "+m_nEndPoint+" progress steps reported"));
            return;
        }
        m_nCurrentPoint++;
        while (100*m_nCurrentPoint/m_nEndPoint >= m_nPercantage+1)
        {
            m_nPercantage++;
            Report.display(".."+m_nPercantage);
            if (m_nPercantage%10==0) Report.displaynl("%");
        }
        if (m_nCurrentPoint==m_nEndPoint)
        {
            long durationTime = System.currentTimeMillis() - m_nStartTime;
            Report.displaynl("Time: " + (durationTime/1000) + "s " +  (durationTime%1000) + "ms");
            Report.displaynl();
        }
    }
}

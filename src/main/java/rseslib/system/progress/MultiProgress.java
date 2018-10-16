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


package rseslib.system.progress;

import rseslib.system.Report;

/**
 * Progress composed from a number of subprogresses.
 *
 * @author      Arkadiusz Wojna
 */
public class MultiProgress extends AbstractProgress
{
    /** Progress object where this multiprogress passes information about progress. */
    Progress m_nProgress;
    /** The values correspond to the relative amount of time that particular processes take. */
    int[] m_nVolumes;
    /** The sum of volumes. */
    int[] m_nPartialVolumeSum;
    /** Volume index. */
    int m_nVolumeIndex = -1;
    /** Current point of the current volume progress. */
    int m_nCurrentVolumePoint = Integer.MIN_VALUE;
    /** Current point of the total progress. */
    int m_nTotalPoint = 0;

    /**
     * Constructor.
     *
     * @param name    Name of this progress.
     * @param prog    Progress object where this multiprogress passes information about progress.
     * @param volumes The values correspond to the relative amount of time
     *                that particular processes take.
     */
    public MultiProgress(String name, Progress prog, int[] volumes)
    {
        m_nProgress = prog;
        m_nVolumes = volumes;
        m_nPartialVolumeSum = new int[m_nVolumes.length];
        m_nPartialVolumeSum[0] = volumes[0];
        for (int vol = 1; vol < m_nPartialVolumeSum.length; vol++)
            m_nPartialVolumeSum[vol] = m_nPartialVolumeSum[vol-1] + m_nVolumes[vol];
        m_nProgress.set(name, m_nPartialVolumeSum[m_nPartialVolumeSum.length-1]);
    }

    /**
     * Sets the total number of steps to be done.
     *
     * @param name      Name of this progress.
     * @param noOfSteps Number of steps in this progress.
     */
    public void set(String name, int noOfSteps)
    {
        super.set(name, noOfSteps);
        if (m_nVolumeIndex == m_nVolumes.length-1)
        {
            Report.exception(new ProgressException(m_Name+": too many progress sections started"));
            return;
        }
        if (m_nVolumeIndex >= 0 && m_nTotalPoint != m_nPartialVolumeSum[m_nVolumeIndex])
        {
            Report.exception(new ProgressException(m_Name+": new progress section started when the previous one not completed"));
            while (m_nTotalPoint < m_nPartialVolumeSum[m_nVolumeIndex])
            {
                m_nTotalPoint++;
                try
                {
                    m_nProgress.step();
                }
                catch (InterruptedException e) { }
            }
        }
        m_nVolumeIndex++;
        m_nCurrentVolumePoint = 0;
    }

    /**
     * Makes a single step.
     *
     * @throws InterruptedException When the process is requested to be stopped.
     */
    public void step() throws InterruptedException
    {
        if (m_nVolumeIndex == -1)
        {
            Report.exception(new ProgressException("Progress not initialised"));
            return;
        }
        if (m_nVolumeIndex == m_nVolumes.length)
        {
            Report.exception(new ProgressException(m_Name+": Too many progress sections started"));
            return;
        }
        if (m_nCurrentVolumePoint>=m_nEndPoint)
        {
            Report.exception(new ProgressException(m_Name+": more than "+m_nEndPoint+" progress steps reported"));
            return;
        }
        m_nCurrentVolumePoint++;
        int previousPartialVolumeSum = 0;
        if (m_nVolumeIndex > 0) previousPartialVolumeSum = m_nPartialVolumeSum[m_nVolumeIndex-1];
        while (m_nVolumes[m_nVolumeIndex]*(m_nCurrentVolumePoint)/(m_nEndPoint) >= m_nTotalPoint - previousPartialVolumeSum + 1)
        {
            m_nTotalPoint++;
            m_nProgress.step();
        }
    }
}

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
import rseslib.structure.attribute.formats.rses.RSLibProgress;

/**
 * Connection between rseslib progress and rses progress.
 *
 * @author      Arkadiusz Wojna
 */
public class RsesProgress extends AbstractProgress
{
    /** Progress object from rses library. */
    RSLibProgress m_RSLibProgress;
    /** Current value of this progress. */
    int m_nCurrentPoint = Integer.MIN_VALUE;
    /** Current percantege of this progress. */
    int m_nPercentage = 0;

    /**
     * Constructor.
     *
     * @param rslibPr Progress object from rses library.
     */
    public RsesProgress(RSLibProgress rslibPr)
    {
        m_RSLibProgress = rslibPr;
    }

    /**
     * Makes a single step.
     *
     * @throws InterruptedException When the process is requested to be stopped.
     */
    public void step() throws InterruptedException
    {
        if (m_nCurrentPoint>=m_nEndPoint)
        {
            Report.exception(new ProgressException(m_Name+": more than "+m_nEndPoint+" progress steps reported"));
            return;
        }
        if (m_nCurrentPoint==Integer.MIN_VALUE) m_nCurrentPoint = 0;
        m_nCurrentPoint++;
        int oldPercantege = m_nPercentage;
        while (100*(m_nCurrentPoint)/(m_nEndPoint) >= m_nPercentage + 1) m_nPercentage++;
        if (m_nPercentage > oldPercantege) m_RSLibProgress.progress(m_nPercentage);
    }
}

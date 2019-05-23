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


package rseslib.qmak.UI.Progress;

import rseslib.qmak.UI.Progress.QProgress;
import rseslib.system.Report;
import rseslib.system.progress.*;

/**
 * Class made especially for rseslib.
 * Displays progres information in visual window
 *
 * @author  Krzysztof Mroczek
 */
public class QVisualProgress extends AbstractProgress 
{
    /** Percentage of this progress. */
    int m_nPercantage = 0;
    /** The current step of this progress. */
    int m_nCurrentPoint = -1;
    /** Time when the progress started. */
    long m_nStartTime;
    public boolean Cancel = false;
    QProgress okno;

    public QVisualProgress(){
    	okno = new QProgress(this);
    }
    public void setCancel() {Cancel = true;}
    
    public void show() {
    	okno.pokaz();
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
        if (okno == null) okno = new QProgress(this);
    	okno.setText(name);
    	okno.setMaximum(100);
    	okno.setProgres(0);
    	okno.setFocusable(true);
        m_nCurrentPoint = 0;
        m_nStartTime = System.currentTimeMillis();
    }

    public void HideOkno() {
    	if (okno != null) okno.dispose();
        okno = null;
    }
    
    /**
     * Makes a single step.
     *
     * @throws InterruptedException When the process is requested to be stopped.
     */
    public void step() throws InterruptedException
    {
    	if (Cancel) {
    		this.HideOkno();
    		throw(new InterruptedException());
    	}
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
   		m_nPercantage = (100 * m_nCurrentPoint)/m_nEndPoint;
   		okno.setProgres(m_nPercantage);
    
        if (m_nCurrentPoint==m_nEndPoint)
        {
        	HideOkno();
        }
    }
}

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


package rseslib.simplegrid.node;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * @author Rafal Latkowski
 *
 */
public class WatchDog implements Runnable
{
    public static long m_lDeathTime = -1; 
    
    public WatchDog(String dietime)
    {
        System.out.println("Initializing Shutdown Timer");
        Calendar deathDate = GregorianCalendar.getInstance();
        dietime=dietime.trim();
        String time;
        String dayofweek = null;
        int hour;
        int minute;
        if (dietime.indexOf(" ")>0)
        {
            StringTokenizer strtok = new StringTokenizer(dietime);
            dayofweek=strtok.nextToken();
            time=strtok.nextToken();
        }
        else
            time=dietime;
        StringTokenizer strtok = new StringTokenizer(time,":");
        hour = Integer.parseInt(strtok.nextToken());
        minute = Integer.parseInt(strtok.nextToken());
        deathDate.set(Calendar.HOUR_OF_DAY, hour);
        deathDate.set(Calendar.MINUTE, minute);
        if (dayofweek!=null)
        {
            dayofweek=dayofweek.toUpperCase();
            if (dayofweek.startsWith("MO"))
                deathDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);    
            if (dayofweek.startsWith("TU"))
                deathDate.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);    
            if (dayofweek.startsWith("WE"))
                deathDate.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);    
            if (dayofweek.startsWith("TH"))
                deathDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);    
            if (dayofweek.startsWith("FR"))
                deathDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);                   
            if (dayofweek.startsWith("SA"))
                deathDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);    
            if (dayofweek.startsWith("SU"))
                deathDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);    
        }
        m_lDeathTime = deathDate.getTimeInMillis();
        if (m_lDeathTime<System.currentTimeMillis())
            m_lDeathTime += 7*24*60*60*1000L;
        //System.out.println("m_lDeathTime ="+m_lDeathTime);
    }

    public void run()
    {
        if (m_lDeathTime>0)
        while (true)
        {
            //System.out.println("Petla "+System.currentTimeMillis()+" >= "+m_lDeathTime);
            if (System.currentTimeMillis()>=m_lDeathTime)
                System.exit(0);
            try { Thread.sleep(10000); } catch (Exception e) { }
        }
    }

}

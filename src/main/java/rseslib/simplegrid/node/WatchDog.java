/*
 * $Name:  $
 * $RCSfile: WatchDog.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/07/07 10:20:11 $
 * Created on 2006-11-09
 *
 * Copyright (c) 2006 Uniwersytet Warszawski
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

/*
 * $Name:  $
 * $RCSfile: Communication.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/07/07 10:20:12 $
 * Created on 2006-11-04
 *
 * Copyright (c) 2006 Uniwersytet Warszawski
 */

package rseslib.simplegrid.common;

import java.util.Calendar;
import java.util.GregorianCalendar;




/**
 * @author Rafal Latkowski
 *
 */
public class Communication
{
    public static final int[] s_aUDPPorts = {  1433,  4008,  5022,  2443, 
                                              10008, 12067,  1247,  4443,
                                               9480,  7345,  2123,  1443,
                                               1194,  8080,  8120,  6337 };
    public static final int[] s_aUDPRelay = {  7333,  7334,  7335,  7336 };    
    public static final String s_strHello = "Hello SGM!";
    public static final String s_strWelcome = "Welcome to Simplistic Grid";
    public static final String s_strGetTask = "get task";
    public static final String s_strPutTask = "put task";
    public static final String s_strPutAnswer = "task aquired";
    public static final String s_strErrorUnknownCommand = "SGM ERROR1: Unknown command";
    
    public static final int s_nPacketSize=1500;
    public static final int s_nBufferSize=2000;
    
    public static final String s_strSGMVersion = "1.3.2";
    
    public static String getTimeStamp()
    {
        Calendar cal = GregorianCalendar.getInstance();
        return "["+cal.get(Calendar.YEAR)+"-"
            +cal.get(Calendar.MONTH)+"-"
            +cal.get(Calendar.DAY_OF_MONTH)+" "
            +cal.get(Calendar.HOUR_OF_DAY)+":"+
            +cal.get(Calendar.MINUTE)+"."+
            +cal.get(Calendar.SECOND)+","+
            +cal.get(Calendar.MILLISECOND)+"]";
    }
}

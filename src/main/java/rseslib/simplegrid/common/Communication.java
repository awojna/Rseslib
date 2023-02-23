/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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
    
    public static final String s_strSGMVersion = "3.3.1-SNAPSHOT";
    
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

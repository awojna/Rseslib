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


package rseslib.simplegrid;

/**
 * @author Rafal Latkowski
 *
 */

import java.util.ArrayList;
import java.util.StringTokenizer;

import rseslib.simplegrid.common.Communication;
import rseslib.simplegrid.node.Node;
import rseslib.simplegrid.node.RelayServer;
import rseslib.simplegrid.node.WatchDog;

public class NodeMain
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
    	System.out.println("SGM (SimpleGridManager) Node (Worker) "+rseslib.simplegrid.common.Communication.s_strSGMVersion); 
        try
        {
            boolean forcerelay=false;
            boolean deepdebug=false;
            ArrayList<String> relayServers = new ArrayList<String>();
            if (args.length>0)
            {
                for (int argpos=1;argpos<args.length;argpos++)
                {
                    if (args[argpos].equalsIgnoreCase("-DIE")&& args.length>(argpos+1))
                    {
                        argpos++;
                        WatchDog wd = new WatchDog(args[argpos]);
                        (new Thread(wd)).start();
                    }
                    if (args[argpos].equalsIgnoreCase("-RELAY")&& args.length>(argpos+1))
                    {
                        argpos++;
                        StringTokenizer strtok = new StringTokenizer(args[argpos],",;");
                        while (strtok.hasMoreTokens())
                            relayServers.add(strtok.nextToken());   
                    }
                    if (args[argpos].equalsIgnoreCase("-FORCERELAY"))
                        forcerelay=true;
                    if (args[argpos].equalsIgnoreCase("-DEEPDEBUG"))
                        deepdebug=true;
                    if (args[argpos].equalsIgnoreCase("-FILEDEBUG"))
                        deepdebug=true;                            
                }
                for (RelayServer.s_nToken=0;RelayServer.s_nToken<Communication.s_aUDPRelay.length;RelayServer.s_nToken++)
                {
                    (new Thread(new RelayServer(args[0],relayServers))).start();
                    Thread.sleep(100);
                }
                new Node(args[0],relayServers,deepdebug,forcerelay);
            }
            else
            {
                System.out.println("Server name expected. Please provide server address (e.g. localhost).");
            	System.out.println("Syntax: NodeMain <servername> [-DIE x] [-FILEDEBUG]");
            	System.out.println("-DIE <time> : will shutdown node at specified time/day (e.g. -DIE 15:49 or -DIE \"FR 15:45\").");
            	System.out.println("-FILEDEBUG : writes all error messages from classifiers to sgm_node_log.txt file.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.flush();
        System.err.flush();
    }

}

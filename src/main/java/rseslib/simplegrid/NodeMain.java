/*
 * $Name:  $
 * $RCSfile: NodeMain.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/07/07 10:20:11 $
 * Created on 2006-11-04
 *
 * Copyright (c) 2006 Uniwersytet Warszawski
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

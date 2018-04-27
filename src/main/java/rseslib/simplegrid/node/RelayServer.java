/*
 * $Name:  $
 * $RCSfile: RelayServer.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/07/07 10:20:11 $
 * Created on 2006-11-09
 *
 * Copyright (c) 2006 Uniwersytet Warszawski
 */

package rseslib.simplegrid.node;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import rseslib.simplegrid.common.Communication;

/**
 * @author Rafal Latkowski
 *
 */
public class RelayServer implements Runnable
{
    public static int s_nToken;
    int[] m_aUDPPorts;
    InetAddress m_oManagerAddress = null;
    int m_nTimeoutMillis = 20*1000;
    
    public static int s_nRelayServerIsWorking = 0;
    public static int s_nRelayedDatagramsCounter = 0;

    /**
     * 
     */
    public RelayServer(String aServerName,ArrayList<String> relayServers)
    {
        m_aUDPPorts = new int[Communication.s_aUDPPorts.length];
        System.arraycopy(Communication.s_aUDPPorts, 0, m_aUDPPorts, 0, m_aUDPPorts.length);
        try
        {
            m_oManagerAddress = InetAddress.getByName(aServerName);
        }
        catch (Exception e) { }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        int token = s_nToken;
        s_nRelayServerIsWorking++;
        try
        {
            DatagramSocket relay_soc = new DatagramSocket(Communication.s_aUDPRelay[token]);
            int rotating_channel=(int)(Math.random()*m_aUDPPorts.length);
            //System.out.println("I am relaying on port "+Communication.s_aUDPRelay[token]);
            //System.out.println(Thread.currentThread().getStackTrace().toString());
            
            while (m_oManagerAddress!=null)
            {                
                try
                {
                    byte[] buf = new byte[1024];
                    DatagramPacket dtgrm = new DatagramPacket(buf,buf.length);
                    relay_soc.receive(dtgrm);
                    InetAddress received_addr = dtgrm.getAddress();
                    int received_port = dtgrm.getPort();
                    System.out.println("Received from "+received_addr.toString()+":"+received_port);
                    
                    dtgrm.setAddress(m_oManagerAddress);
                    dtgrm.setPort(m_aUDPPorts[rotating_channel]);
                    DatagramSocket server_soc = new DatagramSocket();
                    try
                    {
                        server_soc.setSoTimeout(m_nTimeoutMillis);                    
                        System.out.println("Sending to server "+m_oManagerAddress.toString()+":"+m_aUDPPorts[rotating_channel]+" (channel "+rotating_channel+")");
                        server_soc.send(dtgrm);
                        server_soc.receive(dtgrm);
                        System.out.println("Received from server "+m_oManagerAddress.toString()+":"+m_aUDPPorts[rotating_channel]+" (channel "+rotating_channel+")");
                        
                        dtgrm.setAddress(received_addr);
                        dtgrm.setPort(received_port);
                        System.out.println("Sending to "+received_addr.toString()+":"+received_port);
                        relay_soc.send(dtgrm);
                    }
                    catch (Exception e3)
                    {
                        rotating_channel++;
                        if (rotating_channel>=m_aUDPPorts.length) rotating_channel=0;
                    }
                    s_nRelayedDatagramsCounter++;
                    server_soc.close();
                }
                catch (Exception e)
                {
                    
                }
                relay_soc.close();                
            }
        }
        catch (Exception e2) { }
        s_nRelayServerIsWorking--;
    }

}

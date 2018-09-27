/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import rseslib.processing.classification.Classifier;
import rseslib.processing.classification.SingleClassifierTest;
import rseslib.processing.classification.TestResult;
import rseslib.simplegrid.common.Communication;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * @author Rafal Latkowski
 *
 */

public class Node
{
    boolean settings_deepdebug = false;
    boolean settings_forcerelay = false;
    int[] m_aUDPPorts;
    int[] m_aPositiveConnections;
    int[] m_aConnectionTrials;
    int m_nConnectionTrialCounter = 0;
    int m_nPositiveConnectionCounter = 0;
    int m_nLastFailure = -1;
    
    int m_nComputedTasks = 0;
    InetAddress m_oManagerAddress[];
    ArrayList<InetAddress> m_aRelays;
    
    int m_nTimeoutMillis = 5*1000;
    boolean m_nChannelFound = false;
    int m_nChannel = 0;
    NodeInfoFrame m_frame;
    LinkedList<String> m_llScheduledTasks = new LinkedList<String>();
    LinkedList<String> m_llDoneTasks = new LinkedList<String>();
    
    public Node(String aServerName,ArrayList<String> alRelayServers,boolean deepdebug,boolean forcerelay)
    {
        settings_deepdebug=deepdebug;
        settings_forcerelay=forcerelay;
        m_frame=null;
        try
        {
            m_frame = new NodeInfoFrame();
            m_frame.setVisible(true);
        }
        catch (Exception e) { }
        m_aUDPPorts = new int[Communication.s_aUDPPorts.length];
        System.arraycopy(Communication.s_aUDPPorts, 0, m_aUDPPorts, 0, m_aUDPPorts.length);
        m_nChannel = (int)(Math.random()*m_aUDPPorts.length);
        m_aConnectionTrials = new int[m_aUDPPorts.length];
        m_aPositiveConnections = new int[m_aUDPPorts.length];

        m_aRelays = new ArrayList<InetAddress>();
        for (int i=0;i<alRelayServers.size();i++)
        {
            try {
              InetAddress addr = InetAddress.getByName(alRelayServers.get(i));
              m_aRelays.add(addr);
            } catch (Exception er) { }
        }

        
        try
        {
            InetAddress server = InetAddress.getByName(aServerName); 
            m_oManagerAddress  = new InetAddress[m_aUDPPorts.length];
            for (int i=0;i<m_oManagerAddress.length;i++) m_oManagerAddress[i]=server; 
            while (true)
            {
                if (!m_llDoneTasks.isEmpty() || m_llScheduledTasks.isEmpty())
                {
                    doCommunication();
                }
                else 
                    doProcessing();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (settings_deepdebug)
            {
                try
                {
                    PrintWriter pw = new PrintWriter(new FileWriter("sgm_node_log.txt",true));
                    pw.println(Communication.getTimeStamp()+" "+e.toString());
                    e.printStackTrace(pw);
                    pw.println();
                    pw.close();
                }
                catch (Exception e2) { }
            }
        }
        try
        {
            m_frame.setVisible(false);
            m_frame.dispose();
        }
        catch (Exception e) { }
    }

    void doCommunication()
    {
        int action = 0;
        
        if (!m_nChannelFound)
        {
            selectCommunicationChannel();
        }
        
        m_nConnectionTrialCounter++;
        m_aConnectionTrials[m_nChannel]++;
        m_nLastFailure=m_nChannel;

        String message;
        
        if (!m_nChannelFound)
        {
            message = Communication.s_strHello;
            action = 1;
            if (m_frame!=null)
                m_frame.updateInfo(m_oManagerAddress[m_nChannel].getHostAddress(),getPortString(), m_nComputedTasks, "Trying UDPChannel "+m_nChannel);
            else
            {
                System.out.print(Communication.getTimeStamp()+" Node trying UDPChannel "+m_nChannel+"... ");
                System.out.flush();
            }
        }
        else if (!m_llDoneTasks.isEmpty())
        {
            message = m_llDoneTasks.remove();
            action = 2;
            if (m_frame!=null)
                m_frame.updateInfo(m_oManagerAddress[m_nChannel].getHostAddress(),getPortString(), m_nComputedTasks, "Sending results ...");
            else
            {
                System.out.print(Communication.getTimeStamp()+" Node sending results on UDPChannel "+m_nChannel+"... ");
                System.out.flush();
            }
        } else
        {
            message = Communication.s_strGetTask;
            action = 3;
            if (m_frame!=null)
                m_frame.updateInfo(m_oManagerAddress[m_nChannel].getHostAddress(),getPortString(), m_nComputedTasks, "Requesting task ...");
            else
            {
                System.out.print(Communication.getTimeStamp()+" Node requesting task on UDPChannel "+m_nChannel+"... ");
                System.out.flush();
            }
        }
        String answer = null;
        DatagramSocket dsoc = null;
        try
        {
            dsoc = new DatagramSocket();
            dsoc.setSoTimeout(m_nTimeoutMillis);
            byte buf[] = message.getBytes();
            DatagramPacket dtgrm = new DatagramPacket(buf,buf.length,m_oManagerAddress[m_nChannel],m_aUDPPorts[m_nChannel]);
            dsoc.send(dtgrm);
            buf=new byte[Communication.s_nBufferSize];
            dtgrm = new DatagramPacket(buf,buf.length);
            dsoc.receive(dtgrm);
            byte buf2[] = new byte[dtgrm.getLength()];
            System.arraycopy(buf,dtgrm.getOffset(),buf2,0,dtgrm.getLength());
            answer = new String(buf2);
        }
        catch (Exception e)
        { 
            try { if (dsoc!=null) dsoc.close(); } catch (Exception e2) {}
            if (settings_deepdebug)
            {
                try
                {
                    PrintWriter pw = new PrintWriter(new FileWriter("sgm_node_log.txt",true));
                    pw.println(Communication.getTimeStamp()+" "+e.toString());
                    e.printStackTrace(pw);
                    pw.println();
                    pw.close();
                }
                catch (Exception e2) { }
            }          
            if (m_frame==null)
                System.out.println();
        }
       
        if (answer == null)
        {
            m_nChannelFound = false;
            if (action == 2)
            {
                m_llDoneTasks.add(message);
            }
        }
        else
        {
            if (m_frame!=null)
                m_frame.updateInfo(m_oManagerAddress[m_nChannel].getHostAddress(),getPortString(), m_nComputedTasks, "ok.");
            else
            {
                System.out.println("ok.");
            }

            m_nPositiveConnectionCounter++;
            m_aPositiveConnections[m_nChannel]++;
            m_nLastFailure=-1;

            if (action == 1)
            {
                if (answer.equalsIgnoreCase(Communication.s_strWelcome))
                    m_nChannelFound=true;
            }
            else if (action == 2)
            {
                if (!answer.equalsIgnoreCase(Communication.s_strPutAnswer)) 
                    m_llDoneTasks.add(message);
            }
            else /* action 3 */
            {
                //System.out.println("Received tasks length="+answer.length());
                StringTokenizer strtok = new StringTokenizer(answer,"|");
                while (strtok.hasMoreTokens())
                    m_llScheduledTasks.add(strtok.nextToken().trim());
            }
        }
    }
    
    void selectCommunicationChannel()
    {
    	//System.out.println("m_nConnectionTrialCounter="+m_nConnectionTrialCounter+";settings_forcerelay="+settings_forcerelay);
        if (m_nConnectionTrialCounter==0&&settings_forcerelay)
        {
            if (m_aRelays.size()>0)
            {
                int p = 0;
                for (int v : m_aPositiveConnections) p+=v;
                if (p==0)
                    for (int i=0;i<m_aUDPPorts.length;i++)
                    {
                        m_aUDPPorts[i]=Communication.s_aUDPRelay[p];
                        m_oManagerAddress[i]=m_aRelays.get((int)(p/Communication.s_aUDPRelay.length));
                        p++;
                        if (p>=m_aRelays.size()*Communication.s_aUDPRelay.length) p=0;
                    }
            }                    
        }
        if ((!settings_forcerelay)&&m_nConnectionTrialCounter==2*m_aUDPPorts.length)
        {
            if (m_aRelays.size()>0)
            {
                int p = 0;
                for (int v : m_aPositiveConnections) p+=v;
                if (p==0)
                    for (int i=0;i<m_aUDPPorts.length;i+=2)
                    {
                        m_aUDPPorts[i]=Communication.s_aUDPRelay[p];
                        m_oManagerAddress[i]=m_aRelays.get((int)(p/Communication.s_aUDPRelay.length));
                        p++;
                        if (p>=m_aRelays.size()*Communication.s_aUDPRelay.length) p=0;
                    }
            }
        }
        
        m_nChannel++;
        if (m_nChannel==m_aUDPPorts.length) m_nChannel=0;        
    }
            
    void doProcessing()
    {
        try
        {
            String task = m_llScheduledTasks.remove();

            if (m_frame!=null)
                m_frame.updateInfo(m_oManagerAddress[m_nChannel].getHostAddress(),getPortString(), m_nComputedTasks, task);
            else
            {
                if (task.length()<63)
                    System.out.println(task);
                else
                    System.out.println(task.substring(0,60)+"...");
            }

            //System.out.println("Task=#"+task+"#");
            StringTokenizer strtok = new StringTokenizer(task);
            String task_class = strtok.nextToken();
            String task_table_trn = strtok.nextToken();
            String task_table_tst = strtok.nextToken();
            String task_properites = strtok.nextToken();
            Properties task_prop = new Properties();
            strtok = new StringTokenizer(task_properites,";");
            while (strtok.hasMoreTokens())
            {
                String param = strtok.nextToken();
                //System.out.println("Token: "+param);
                StringTokenizer strtok3 = new StringTokenizer(param,"=");
                task_prop.put(strtok3.nextToken(), strtok3.nextToken());
            }
            
            Progress emptyProg = new EmptyProgress();
            DoubleDataTable trn = new ArrayListDoubleDataTable(new File(task_table_trn),emptyProg);
            DoubleDataTable tst = new ArrayListDoubleDataTable(new File(task_table_tst),emptyProg);
            
            Class[] cTypes = new Class[3];
            cTypes[0] = task_prop.getClass();
            cTypes[1] = DoubleDataTable.class;
            cTypes[2] = Progress.class;
            
            Object[] oArgs = new Object[3];
            oArgs[0] = task_prop;
            oArgs[1] = trn;
            oArgs[2] = emptyProg;
            
            Object method = Class.forName(task_class).getConstructor(cTypes).newInstance(oArgs);
            
            TestResult results = new SingleClassifierTest().classify((Classifier)method,tst,emptyProg);
            Properties stat = results.getStatisticsAndResults();
            StringBuffer sb = new StringBuffer();
            for (Entry e : stat.entrySet())
                sb.append(e.getKey()+"="+e.getValue()+";");
            m_llDoneTasks.add(Communication.s_strPutTask+" "+task+" | "+sb.toString());
            m_nComputedTasks++;
        }
        catch (Exception e)
        {
            if (settings_deepdebug)
            {
                try
                {
                    PrintWriter pw = new PrintWriter(new FileWriter("sgm_node_log.txt",true));
                    pw.println(Communication.getTimeStamp()+" "+e.toString());
                    e.printStackTrace(pw);
                    pw.println();
                    pw.close();
                }
                catch (Exception e2) { }
            }
        }
    }

    
    String getPortString()
    {
        if (m_aUDPPorts[m_nChannel]>=Communication.s_aUDPRelay[0]&&m_aUDPPorts[m_nChannel]<=Communication.s_aUDPRelay[Communication.s_aUDPRelay.length-1])
            return "Relay Server "+m_nChannel+" ("+m_aUDPPorts[m_nChannel]+")";
        else
            return m_nChannel+" ("+m_aUDPPorts[m_nChannel]+")";
    }
}

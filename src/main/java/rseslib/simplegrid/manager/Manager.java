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


package rseslib.simplegrid.manager;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import rseslib.simplegrid.common.Communication;

/**
 * @author Rafal Latkowski
 *
 */

public class Manager implements Runnable
{
    class Job { public String m_sTask; public int m_nProcessingFile; public Job(String task,int file) { m_sTask=task; m_nProcessingFile=file; } }
    
    int[] m_aUDPPorts;
    DatagramSocket[] m_aUDPSockets;
    int m_nThreadToken;

    int m_nProcessingFile = 0;
    ArrayList<String> m_aScriptFiles;
    ArrayList<String> m_aResultFiles;
    
    HashMap<String,Integer> m_hmScheduledJobs = new HashMap<String,Integer>();
    HashSet<String> m_hmComputedJobs;
    HashMap<String,Long> m_hmActivityStatistics = new HashMap<String,Long>();

    LinkedList<Job> m_lSchedulingQueue = new LinkedList<Job>();
    
    int m_nFinishSleep = 10*60*1000;
    
    public Manager(ArrayList<String> aScriptFiles,ArrayList<String> aResultFiles)
    {
        m_aUDPPorts = new int[Communication.s_aUDPPorts.length];
        System.arraycopy(Communication.s_aUDPPorts, 0, m_aUDPPorts, 0, m_aUDPPorts.length);
        m_aScriptFiles=aScriptFiles;
        m_aResultFiles=aResultFiles;
        
        /* Pre-fetch 32 lines of script file */
        try { for (int i=0;i<32;i++) scheduleNextTask(-1); } catch (Exception e) { e.printStackTrace(); }
        
        m_aUDPSockets = new DatagramSocket[m_aUDPPorts.length];
        for (int i=0;i<m_aUDPPorts.length;i++)
        {
            m_nThreadToken = i;
            (new Thread(this)).start();
            do
            {
               try { Thread.sleep(100); } catch (Exception e) {}
            } while (m_aUDPSockets[i]==null && m_aUDPPorts[i]>0);
        }
    }
    
    BufferedReader m_brScript = null;
    
    void scheduleNextTask(int my_token) throws IOException
    {
        //System.out.println("Scheduling...");
        if (m_brScript!=null)
        {
            //System.out.println("Reading line..");
            boolean readed=false;
            String line;
            while (!readed && null!=(line=m_brScript.readLine()))
            {
                if (!m_hmComputedJobs.contains(line))
                {
                    m_lSchedulingQueue.add(new Job(line,m_nProcessingFile));
                    readed=true;
                }
                
            }
            if (!readed)
            {
                //System.out.println("File was empty");
                m_brScript.close();
                m_brScript=null;
                m_hmComputedJobs.clear();
                m_nProcessingFile++;
                //fillListWithScheduled();
            }           
        }
        else
        {        
            if (m_nProcessingFile==m_aScriptFiles.size())
            {
                //System.out.println("No more files, filling with scheduled");
                fillListWithScheduled();
            }
            else
            {
                //System.out.println("Opening new files");
                readNewScripts();
            }
        }
    }
    
    void fillListWithScheduled()
    {
        if (!m_hmScheduledJobs.isEmpty())
        {
            for (Entry<String,Integer> e : m_hmScheduledJobs.entrySet())
            {
            	if (!m_hmComputedJobs.contains(e.getKey()))
            	{
            		m_lSchedulingQueue.add(new Job(e.getKey(),e.getValue()));
            	}
            }
        }        
    }

    void readNewScripts() throws IOException
    {
        m_brScript =  new BufferedReader(new FileReader(m_aScriptFiles.get(m_nProcessingFile)));
        try
        {
            if (m_hmComputedJobs==null) m_hmComputedJobs = new HashSet<String>();
            else m_hmComputedJobs.clear();
            BufferedReader br = new BufferedReader(new FileReader(m_aResultFiles.get(m_nProcessingFile)));
            String line;
            while (null!=(line=br.readLine()))
            {
                StringTokenizer strtok = new StringTokenizer(line,"|");
                m_hmComputedJobs.add(strtok.nextToken().trim());
            }
            br.close();
        }
        catch (Exception e) { }
    }
    
    synchronized String scheduleNextLine(int my_token) throws IOException
    {
        int tries=0;
        int length=0;
        String line = null;
        
        while (tries<10&&length<(Communication.s_nPacketSize*0.9))
        {
            tries++;
            if (m_lSchedulingQueue.isEmpty())
                scheduleNextTask(my_token);
            
            if (!m_lSchedulingQueue.isEmpty())
            {
                Job j = m_lSchedulingQueue.remove();
        
                if (line==null)
                    line=j.m_sTask;
                else
                    line=line+"|"+j.m_sTask;
                
                length=line.length();

                if (length>Communication.s_nPacketSize)
                    m_lSchedulingQueue.add(j);
                else
                    m_hmScheduledJobs.put(j.m_sTask, j.m_nProcessingFile);
            }
        }
        //System.out.println("Prepared("+tries+") line is "+line);
        return line;
    }
    
    synchronized void returnNextTask(String line) throws IOException
    {
        StringTokenizer strtok = new StringTokenizer(line,"|");
        String task = strtok.nextToken().trim();
        if (m_hmScheduledJobs.containsKey(task))
        {
            int file = m_hmScheduledJobs.get(task);
            m_hmScheduledJobs.remove(task);
            if (!m_hmComputedJobs.contains(task))
            {
                PrintWriter pw = new PrintWriter(new FileWriter(m_aResultFiles.get(file),true));
                pw.println(line);
                pw.flush();
                pw.close();
            }
            m_hmComputedJobs.add(task);
        }
    }
    
    public void run()
    {
        int my_token = m_nThreadToken;
        try
        {
            DatagramSocket dsoc = new DatagramSocket(m_aUDPPorts[my_token]);
            m_aUDPSockets[my_token]=dsoc;
            System.out.println(Communication.getTimeStamp()+" UThread"+my_token+" initialized (on UDP Port "+m_aUDPPorts[my_token]+").");
            while (true)
            {
                System.out.println(Communication.getTimeStamp()+" "+getActivityStatistics());
                byte[] buf = new byte[Communication.s_nBufferSize];
                DatagramPacket dtgrm = new DatagramPacket(buf,buf.length);
                //System.out.println("UThread"+my_token+" awaiting:");
                dsoc.receive(dtgrm);
                m_hmActivityStatistics.put(dtgrm.getAddress().getHostAddress(), System.currentTimeMillis());
                byte buf2[] = new byte[dtgrm.getLength()];
                System.arraycopy(buf,dtgrm.getOffset(),buf2,0,dtgrm.getLength());
                String line=new String(buf2);
                if (line.length()>22)
                    System.out.println(Communication.getTimeStamp()+" UThread"+my_token+" received "+line.substring(0,20)+"... "+dtgrm.getAddress().getHostAddress());
                else
                    System.out.println(Communication.getTimeStamp()+" UThread"+my_token+" received "+line+" "+dtgrm.getAddress().getHostAddress());
                if (line.equalsIgnoreCase(Communication.s_strHello))
                {
                    line = Communication.s_strWelcome;
                }
                else if (line.equalsIgnoreCase(Communication.s_strGetTask))
                {
                    line = scheduleNextLine(my_token);
                    
                }
                else if (line.startsWith(Communication.s_strPutTask))
                {
                    returnNextTask(line.substring(Communication.s_strPutTask.length()).trim());
                    line = Communication.s_strPutAnswer;
                }
                else 
                {
                    line = Communication.s_strErrorUnknownCommand;
                }
                if (line!=null)
                {
                    buf = line.getBytes();
                    dtgrm = new DatagramPacket(buf,buf.length,dtgrm.getAddress(),dtgrm.getPort());
                    dsoc.send(dtgrm);
                    if (line.length()>26)
                        System.out.println(Communication.getTimeStamp()+" UThread"+my_token+" sent "+line.substring(0,24)+"...");
                    else
                        System.out.println(Communication.getTimeStamp()+" UThread"+my_token+" sent "+line);
                }
                else
                {
                    if (m_hmScheduledJobs.size()==0)
                    {                        
                        System.out.println("UThread"+my_token+" no more scheduled job - terminating thread. Other threads wait for jobs in execution. If you sure all jobs are done you can kill the server now.");
                        break;
                    }
                    else
                    {
                        Thread.sleep(1000);
                        //System.out.println("UThread"+my_token+" i co ja mam teraz zrobi�?");
                    }
                }
            }
            dsoc.close();
            System.out.println(Communication.getTimeStamp()+" UThread"+my_token+" run() terminated.");
        }
        catch (Exception e)
        {
            System.out.println(Communication.getTimeStamp()+" UThread"+my_token+" failed (on UDP Port "+m_aUDPPorts[my_token]+").");
            m_aUDPPorts[my_token] = -1; 
            e.printStackTrace();
        }
        
    }
    
    public static final long s_lOneMinute=1000*60;
    public static final long s_lFiveMinutes=s_lOneMinute*5;
    public static final long s_lFifteenMinutes=s_lOneMinute*15;
    public static final long s_lOneHour=s_lOneMinute*60;
    
    public String getActivityStatistics()
    {
        long time = System.currentTimeMillis();
        int cnt_1m=0;
        int cnt_5m=0;
        int cnt_15m=0;
        int cnt_1h=0;
        int cnt_total=0;
        
        for (Entry<String,Long> e :  m_hmActivityStatistics.entrySet())
        {
            cnt_total++;
            long timediff = time-e.getValue();
            if (timediff<=s_lOneHour)
            {
                cnt_1h++;
                if (timediff<=s_lFifteenMinutes)
                {
                    cnt_15m++;
                    if (timediff<=s_lFiveMinutes)
                    {
                        cnt_5m++;
                        if (timediff<=s_lOneMinute) cnt_1m++;
                    }
                }
            }
        }
        return "Node activity: "+cnt_1m+" (1m), "+cnt_15m+" (15m), "+cnt_1h+" (1h), "+cnt_total+" (tot) Queue Size: "+m_lSchedulingQueue.size()+" In Execution: "+m_hmScheduledJobs.size();
    }
}

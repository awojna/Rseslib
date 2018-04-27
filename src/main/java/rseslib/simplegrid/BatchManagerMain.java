/*
 * $Name:  $
 * $RCSfile: BatchManagerMain.java,v $
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import rseslib.simplegrid.manager.Manager;

public class BatchManagerMain
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
    	System.out.println("SGM (SimpleGridManager) Batch Manager "+rseslib.simplegrid.common.Communication.s_strSGMVersion); 
        try
        {
            if (args.length>0)
            {
                BufferedReader br = new BufferedReader(new FileReader(args[0]));
                String line;
                ArrayList<String> scripts = new ArrayList<String>();
                ArrayList<String> results = new ArrayList<String>();;
                while (null!=(line=br.readLine()))
                {
                    StringTokenizer strtok = new StringTokenizer(line);
                    scripts.add(strtok.nextToken());
                    results.add(strtok.nextToken());
                }
                new Manager(scripts,results);
                br.close();
            }
            else
            {
                System.out.println("Error: File name expected.");
                System.out.println("Batch SGM Manager need to be executed with script file parameter that contains pairs of filenames in the same line.");
                System.out.println("First filename describes task file (where it reads taks to execute) and the second filename describes result file (where it stores the results).");
                System.out.println("Example: data/experiments.txt file contains:");
                System.out.println("data/tasks.txt data/results.txt");
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

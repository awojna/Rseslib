/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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

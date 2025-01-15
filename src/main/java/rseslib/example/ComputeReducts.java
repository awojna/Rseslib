/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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


package rseslib.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import rseslib.processing.discretization.*;
import rseslib.processing.discretization.DiscretizationFactory.DiscretizationType;
import rseslib.processing.reducts.*;
import rseslib.processing.rules.ReductRuleGenerator.ReductsMethod;
import rseslib.processing.transformation.TableTransformer;
import rseslib.processing.transformation.TransformationProvider;
import rseslib.processing.transformation.Transformer;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.Report;
import rseslib.system.output.StandardErrorOutput;
import rseslib.system.output.StandardOutput;
import rseslib.system.progress.EmptyProgress;

/**
 * This program computes reducts for a given data table
 * and writes them to a file.
 * 
 * Usage:
 *     java ... rseslib.example.ComputeReducts [-d <discretization>] [-dcfg <discr. config file>] [-r <reducts>] [-rcfg <reducts config file>] <data file> [<output file>]
 * 
 * Run the class without arguments to see possible values of the program options.
 * Each discretization method and each algorithm computing reducts has its own set of parameters. Check out:
 * src/main/resources/rseslib/processing/discretization/ for the config files with parameters of the discretization methods,
 * src/main/resources/rseslib/processing/reducts/ for the config files with parameters of the algorithms computing reducts.
 * 
 * @author      Arkadiusz Wojna
 */
public class ComputeReducts
{
    /**
     * The program discretizes the input table, computes reducts and writes them to a file. 
     * 
     * @param args As above.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
    	// parse the options, print help
        DiscretizationType discretization = DiscretizationType.MaximalDiscernibilityHeuristicLocal;
    	ReductsMethod reductsType = ReductsMethod.AllGlobal;
        String discrCfgFile = null;
        String reductsCfgFile = null;
    	int requiredArgs = 1;
    	if (args.length > requiredArgs && args[requiredArgs - 1].equals("-d"))
    	{
    		try
    		{
       			discretization = DiscretizationType.valueOf(args[requiredArgs]);
    		}
    		catch (IllegalArgumentException e)
    		{
        		System.out.println("Unknown discretization: " + args[requiredArgs]);
        		System.out.println("Use one of:");
        		for(DiscretizationType discr : DiscretizationType.values())
        			System.out.println("    " + discr);
        		System.exit(0);
    		}
    		requiredArgs += 2;
    	}
		if(args.length > requiredArgs && args[requiredArgs - 1].equals("-dcfg"))
		{
			discrCfgFile = args[requiredArgs];
    		requiredArgs += 2;
		}
    	if (args.length > requiredArgs && args[requiredArgs - 1].equals("-r"))
    	{
    		try
    		{
    			reductsType = ReductsMethod.valueOf(args[requiredArgs]);
    		}
    		catch (IllegalArgumentException e)
    		{
        		System.out.println("Unknown reducts: " + args[requiredArgs]);
        		System.out.println("Use one of:");
        		for(ReductsMethod red : ReductsMethod.values())
        			System.out.println("    " + red);
        		System.exit(0);
    		}
    		requiredArgs += 2;
    	}
		if(args.length > requiredArgs && args[requiredArgs - 1].equals("-rcfg"))
		{
			reductsCfgFile = args[requiredArgs];
    		requiredArgs += 2;
		}
    	if (args.length != requiredArgs && args.length != requiredArgs + 1)
    	{
    		System.out.println("Program computes reducts from a dataset and writes them to a file.");
    		System.out.println("Usage:");
    		System.out.println("    java ... rseslib.example.ComputeReducts [-d <discretization>] [-dcfg <discr. config file>] [-r <reducts>] [-rcfg <reducts config file>] <data file> [<output file>]");
    		System.out.println();
    		System.out.print("Discretizations: ");
    		for(DiscretizationType discr : DiscretizationType.values())
    			System.out.print(discr + ", ");
    		System.out.println();
    		System.out.println("Check out src/main/resources/rseslib/processing/discretization/ in Rseslib source for the config files with parameters of the discretizations");
    		System.out.println();
    		System.out.print("Reducts: ");
    		for(ReductsMethod red : ReductsMethod.values())
    			System.out.print(red + ", ");
    		System.out.println();
    		System.out.println("Check out src/main/resources/rseslib/processing/reducts/ in Rseslib source for the config files with parameters of the reduct types");
    		System.exit(0);
    	}
    	File dataFile = new File(args[requiredArgs - 1]);
    	if(!dataFile.exists())
    	{
    		System.out.println("File " + args[requiredArgs - 1] + " not found");
    		System.exit(0);
    	}
    	String reductFileName = args.length == requiredArgs ? "reducts.txt" : args[requiredArgs];
    	File reductFile = new File(reductFileName);

    	// set the output to standard output
        Report.addErrorOutput(new StandardErrorOutput());
        Report.addInfoOutput(new StandardOutput());

        // load data and print the table info
        DoubleDataTable table = new ArrayListDoubleDataTable(dataFile, new EmptyProgress());
        Report.displaynl(table);

        // discretize the table
        Properties discrParams = null;
        if (discrCfgFile != null)
        {
        	discrParams = new Properties();
        	discrParams.load(new FileInputStream(discrCfgFile));
        }
        TransformationProvider discrProv = null;
    	switch (discretization)
    	{
    		case None:
    			break;
    		case EqualWidth:
    			discrProv = new RangeDiscretizationProvider(discrParams);
    			break;
    		case EqualFrequency:
    			discrProv = new HistogramDiscretizationProvider(discrParams);
    			break;
    		case OneRule:
    			discrProv = new OneRuleDiscretizationProvider(discrParams);
    			break;
    		case EntropyMinimizationStatic:
    			discrProv = new EntropyMinStaticDiscretizationProvider(discrParams);
    			break;
    		case EntropyMinimizationDynamic:
    			discrProv = new EntropyMinDynamicDiscretizationProvider();
    			break;
    		case ChiMerge:
    			discrProv = new ChiMergeDiscretizationProvider(discrParams);
    			break;
    		case MaximalDiscernibilityHeuristicGlobal:
    			discrProv = new MDGlobalDiscretizationProvider();
    			break;
    		case MaximalDiscernibilityHeuristicLocal:
    			discrProv = new MDLocalDiscretizationProvider();
    			break;
    	}
        if (discrProv != null)
        {
            Report.display("Discretizing...");
        	Transformer discretizer = discrProv.generateTransformer(table);
        	if (discretizer != null)
        		table = TableTransformer.transform(table, discretizer);
            Report.displaynl("done");
        }
        
        // compute reducts
        Report.display("Computing reducts...");
        LocalReductsProvider localProv = null;
        GlobalReductsProvider globalProv = null;
        Properties reductsParams = null;
        if (reductsCfgFile != null)
        {
        	reductsParams = new Properties();
        	reductsParams.load(new FileInputStream(reductsCfgFile));
        }
    	switch (reductsType)
    	{
    	case AllLocal:
    		localProv = new AllLocalReductsProvider(reductsParams, table);
    		break;
    	case AllGlobal:
    		globalProv = new AllGlobalReductsProvider(reductsParams, table);
    		break;
    	case OneJohnson:
    		globalProv = new JohnsonReductsProvider(reductsParams, table);
    		break;
    	case AllJohnson:
    		if(reductsParams == null)
    			reductsParams = Configuration.loadDefaultProperties(JohnsonReductsProvider.class);
    		reductsParams.setProperty("Reducts", "AllJohnson");
    		globalProv = new JohnsonReductsProvider(reductsParams, table);
    		break;
    	case PartialLocal:
    		localProv = new PartialReductsProvider(reductsParams, table);
    		break;
    	case PartialGlobal:
    		globalProv = new PartialReductsProvider(reductsParams, table);
    		break;
    	}
    	Collection<BitSet> reducts = null;
    	if(globalProv != null)
    		reducts = globalProv.getReducts();
    	else
    	{
    		reducts = new HashSet<BitSet>();
            for (DoubleData object : table.getDataObjects())
                reducts.addAll(localProv.getSingleObjectReducts(object));
    	}
        Report.displaynl("done");

    	// write reducts
    	Header hdr = table.attributes();
    	BufferedWriter output = new BufferedWriter(new FileWriter(reductFile));
    	for(BitSet reduct : reducts)
    	{
    		int i = reduct.nextSetBit(0);
    		output.write(hdr.name(i));
    		i = reduct.nextSetBit(i+1);
    		while(i >= 0)
    		{
    			output.write(", " + hdr.name(i));
        		i = reduct.nextSetBit(i+1);
    		}
    		output.newLine();
    	}
    	output.close();

    	Report.displaynl("Reducts saved in " + reductFileName);
    	Report.close();
    }
}

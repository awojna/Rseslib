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
import java.util.Collection;
import java.util.Properties;

import rseslib.processing.discretization.*;
import rseslib.processing.discretization.DiscretizationFactory.DiscretizationType;
import rseslib.processing.rules.AccurateRuleGenerator;
import rseslib.processing.rules.CoveringRuleGenerator;
import rseslib.processing.rules.ReductRuleGenerator;
import rseslib.processing.rules.RuleGenerator;
import rseslib.processing.rules.ReductRuleGenerator.ReductsMethod;
import rseslib.processing.transformation.TableTransformer;
import rseslib.processing.transformation.TransformationProvider;
import rseslib.processing.transformation.Transformer;
import rseslib.structure.rule.Rule;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.Report;
import rseslib.system.output.StandardErrorOutput;
import rseslib.system.output.StandardOutput;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.StdOutProgress;

/**
 * This program computes rules for a given data table and writes them to a file.
 * The possible rule types are reduct, AQ15 or accurate rules.
 *  
 * Usage:
 *     java ... rseslib.example.ComputeRules [-d <discretization>] [-dcfg <discr. config file>] [-r <rules>] [-rcfg <rules config file>] <data file> [<output file>]
 * 
 * Run the class without arguments to see possible values of the program options.
 * Each discretization method and each algorithm computing rules has its own set of parameters. Check out:
 * src/main/resources/rseslib/processing/discretization/ for the config files with parameters of the discretization methods,
 * src/main/resources/rseslib/processing/rules/ for the config files with parameters of the algorithms computing rules.
 * 
 * @author      Arkadiusz Wojna
 */
public class ComputeRules
{
    /**
     * The program discretizes the input table, computes rules and writes them to a file. 
     * 
     * @param args As above.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
    	// parse the options, print help
        DiscretizationType discretization = DiscretizationType.MaximalDiscernibilityHeuristicLocal;
    	ReductsMethod reductsType = ReductsMethod.AllGlobal;
    	boolean aq15 = false, accurate = false;
        String discrCfgFile = null;
        String rulesCfgFile = null;
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
    		String ruleType = args[requiredArgs];
    		if (ruleType.equals("AQ15"))
    			aq15 = true;
    		else if (ruleType.equals("Accurate"))
    			accurate = true;
    		else
    		{
    			try
    			{
    				reductsType = ReductsMethod.valueOf(ruleType);
    			}
    			catch (IllegalArgumentException e)
    			{
    				System.out.println("Unknown rules: " + ruleType);
    				System.out.println("Use one of:");
    				for(ReductsMethod red : ReductsMethod.values())
    					System.out.println("    " + red);
					System.out.println("    AQ15");
					System.out.println("    Accurate");
    				System.exit(0);
    			}
    		}
    		if (aq15)
    		{
    			if (requiredArgs == 3)
    			{
    				System.out.println("Do not use discretization if rules are generated with AQ15 algorithm");
    				System.exit(0);
    			}
    			discretization = DiscretizationType.None;
    		}
    		requiredArgs += 2;
    	}
		if(args.length > requiredArgs && args[requiredArgs - 1].equals("-rcfg"))
		{
			rulesCfgFile = args[requiredArgs];
    		requiredArgs += 2;
		}
    	if (args.length != requiredArgs && args.length != requiredArgs + 1)
    	{
    		System.out.println("Program computes rules from a dataset and writes them to a file.");
    		System.out.println("Usage:");
    		System.out.println("    java ... rseslib.example.ComputeRules [-d <discretization>] [-dcfg <discr. config file>] [-r <rules>] [-rcfg <rules config file>] <data file> [<output file>]");
    		System.out.print("Discretizations: ");
    		for(DiscretizationType discr : DiscretizationType.values())
    			System.out.print(discr + ", ");
    		System.out.println();
    		System.out.println("Check out src/main/resources/rseslib/processing/discretization/ in Rseslib source for the config files with parameters of the discretizations");
    		System.out.println();
    		System.out.print("Rules: ");
    		for(ReductsMethod red : ReductsMethod.values())
    			System.out.print(red + ", ");
			System.out.print("AQ15, ");
			System.out.print("Accurate, ");
    		System.out.println();
    		System.out.println("Check out src/main/resources/rseslib/processing/rules/ in Rseslib source for the config files with parameters of the rule types");
    		System.exit(0);
    	}
    	File dataFile = new File(args[requiredArgs - 1]);
    	if(!dataFile.exists())
    	{
    		System.out.println("File " + args[requiredArgs - 1] + " not found");
    		System.exit(0);
    	}
    	String ruleFileName = args.length == requiredArgs ? "rules.txt" : args[requiredArgs];
    	File ruleFile = new File(ruleFileName);

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
        
        // compute rules
        RuleGenerator ruleGen;
        Properties rulesParams = null;
        if (rulesCfgFile != null)
        {
        	rulesParams = new Properties();
        	rulesParams.load(new FileInputStream(rulesCfgFile));
        }
        if (aq15)
        	ruleGen = new CoveringRuleGenerator(rulesParams);
        else if (accurate)
        	ruleGen = new AccurateRuleGenerator(rulesParams);
        else
        {
        	if (rulesParams == null)
        		rulesParams = Configuration.loadDefaultProperties(ReductRuleGenerator.class);
        	rulesParams.setProperty("Reducts", reductsType.name());
    		ruleGen = new ReductRuleGenerator(rulesParams);
        }
        Collection<Rule> rules = ruleGen.generate(table, new StdOutProgress());

    	// write rules
    	BufferedWriter output = new BufferedWriter(new FileWriter(ruleFile));
    	for(Rule rule : rules)
    	{
    		output.write(((Object)rule).toString());
    		output.newLine();
    	}
    	output.close();

    	Report.displaynl("Rules saved in " + ruleFileName);
    	Report.close();
    }
}

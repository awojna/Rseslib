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
import java.text.DecimalFormat;
import java.util.Properties;

import rseslib.processing.discretization.*;
import rseslib.processing.discretization.DiscretizationFactory.DiscretizationType;
import rseslib.processing.evaluation.attribute.RoughSetEvaluator;
import rseslib.processing.transformation.TableTransformer;
import rseslib.processing.transformation.TransformationProvider;
import rseslib.processing.transformation.Transformer;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Report;
import rseslib.system.output.StandardErrorOutput;
import rseslib.system.output.StandardOutput;
import rseslib.system.progress.EmptyProgress;

/**
 * This program calculates the significance of the conditional attributes
 * for the decision in a given data table using the concept of rough set.
 * It calculates the significance factors for all subsets of attributes
 * of the size less than or equal to a given maximal size.
 * 
 * Usage:
 *     java ... rseslib.example.AttributeSignificance [-d <discretization>] [-dcfg <discr. config file>] [-m <max no of attributes>] <data file> [<output file>]
 * 
 * Run the class without arguments to see possible values of the program options.
 * Each discretization method has its own set of parameters. Check out:
 * src/main/resources/rseslib/processing/discretization/ for the config files with parameters of the discretization methods.
 * 
 * @author      Arkadiusz Wojna
 */
public class AttributeSignificance
{
    /**
     * The program discretizes the input table, and calculates and writes the significance factors to a file. 
     * 
     * @param args As above.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
    	// parse the options, print help
        DiscretizationType discretization = DiscretizationType.ChiMerge;
        String discrCfgFile = null;
        int maxAttrs = 1;
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
    	if (args.length > requiredArgs && args[requiredArgs - 1].equals("-m"))
    	{
    		try
    		{
        		maxAttrs = Integer.parseInt(args[requiredArgs]);
    		}
    		catch (NumberFormatException e)
    		{
        		System.out.println("Provide maximal number of attributes after the option -m");
        		System.exit(0);
    		}
    		requiredArgs += 2;
    	}
    	if (args.length != requiredArgs && args.length != requiredArgs + 1)
    	{
    		System.out.println("Program calculates the significance of conditional attributes for the decision in a dataset and writes the results to a file.");
    		System.out.println("Usage:");
    		System.out.println("    java ... rseslib.example.AttributeSignificance [-d <discretization>] [-dcfg <discr. config file>] [-m <max no of attributes>] <data file> [<output file>]");
    		System.out.println();
    		System.out.print("Discretizations: ");
    		for(DiscretizationType discr : DiscretizationType.values())
    			System.out.print(discr + ", ");
    		System.out.println();
    		System.out.println("Check out src/main/resources/rseslib/processing/discretization/ in Rseslib source for the config files with parameters of the discretizations");
    		System.exit(0);
    	}
    	File dataFile = new File(args[requiredArgs - 1]);
    	if(!dataFile.exists())
    	{
    		System.out.println("File " + args[requiredArgs - 1] + " not found");
    		System.exit(0);
    	}
    	String outputFileName = args.length == requiredArgs ? "attribute_significance.txt" : args[requiredArgs];
    	File outputFile = new File(outputFileName);

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
        
        // calculate significance factors for all subsets of attributes
        // of the size less than or equal to the given parameter
    	Header hdr = table.attributes();
        DoubleData[] objects = new DoubleData[table.noOfObjects()];
        table.getDataObjects().toArray(objects);
        int[] allIndices = new int[hdr.noOfAttr() - 1];
        int att = 0;
        for (int i = 0; i < hdr.noOfAttr(); ++i)
        	if(hdr.isConditional(i))
        		allIndices[att++] = i;
        RoughSetEvaluator roughSet = new RoughSetEvaluator();
    	BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
    	output.write("Table: " + dataFile);
    	output.newLine();
    	output.write("Objects: " + table.noOfObjects());
    	output.newLine();
    	output.write("Conditional attributes: " + allIndices.length);
    	output.newLine();
    	output.write("Discretization: " + discretization);
    	output.newLine();
        double approxAccTotal = roughSet.approximationAccuracy(allIndices, objects);
    	DecimalFormat df = new DecimalFormat("0.000");
        output.write("Approximation by all attributes: " + df.format(approxAccTotal));
		output.newLine();
        output.write("Maximal attribute set size: " + maxAttrs);
		output.newLine();
		output.newLine();
		output.newLine();
		output.write("Significance\tAttribute set");
		output.newLine();
        for (int noOfAttr = 1; noOfAttr <= maxAttrs; ++noOfAttr)
        {
            Report.display("Calculating attribute significance for " + noOfAttr + "-combinations ...");
    		output.newLine();
        	int[] selected = new int[noOfAttr];
        	for (int i = 0; i < selected.length; ++i)
        		selected[i] = i;
        	boolean cont = true;
        	while (cont)
        	{
        		// prepare the complement of the current combination of attributes
        		int[] remaining = new int[allIndices.length - noOfAttr];
        		int a = 0, o = 0;
        		for (int i = 0; i < allIndices.length; ++i)
        			if(o < selected.length && selected[o] == i)
        				++o;
        			else
        				remaining[a++] = allIndices[i];
        		// calculate and write the significance coefficient of the current combination of attributes to the file
        		output.write(df.format(1 - roughSet.approximationAccuracy(remaining, objects) / approxAccTotal));
            	for (int i = 0; i < selected.length; ++i)
           			output.write("\t" + hdr.name(allIndices[selected[i]]));
        		output.newLine();
        		// find the next combination of attributes
        		int lowestChanged = selected.length - 1;
        		while (lowestChanged >= 0 && allIndices.length - selected[lowestChanged] == selected.length - lowestChanged)
        			--lowestChanged;
        		if (lowestChanged >= 0)
        		{
        			++selected[lowestChanged];
        			for(int i = lowestChanged + 1; i < selected.length; ++i)
        				selected[i] = selected[i - 1] + 1;
        		}
        		else
        			cont = false;
        	}
            Report.displaynl(" done");
        }
        output.close();
    	Report.displaynl("Significance coefficients saved in " + outputFileName);
    	Report.close();
    }
}

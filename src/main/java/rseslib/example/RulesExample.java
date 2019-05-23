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


package rseslib.example;

import java.io.File;
import java.util.ArrayList;

import rseslib.processing.classification.Classifier;
import rseslib.processing.classification.SingleClassifierTest;
import rseslib.processing.classification.TestResult;
import rseslib.processing.classification.rules.MajorityClassifierWithRules;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Report;
import rseslib.system.output.StandardErrorOutput;
import rseslib.system.output.StandardOutput;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.StdOutProgress;

/**
 * A single test of a rule based classifier on data split into a training and a test table.
 * The class is called with one of the commands:
 * java rseslib.example.RulesExample <data file>
 * or
 * java rseslib.example.RulesExample <training file> <test file>,
 * e.g. java rseslib.example.RulesExample data/heart.dat
 * If only one data file is provided, the method splits data randomly
 * with the ratio 2:1 of the training data size to the test data size.
 * If a test data is provided too, the data given as the first argument
 * are used to build the classifier and the data given as the second argument
 * are used to test the classifier.
 *
 * @author      Arkadiusz Wojna
 */
public class RulesExample
{
    /**
     * Main testing method.
     *
     * @param args Parameters of the method: a path to a training
     *             and a test data file and the data format name.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
    	// check the number of program arguments and print help
    	if (args.length!=1 && args.length!=2)
    	{
    		System.out.println("Usage:");
    		System.out.println("    java ... rseslib.example.RulesExample <data file>");
    		System.out.println("    java ... rseslib.example.RulesExample <training file> <test file>");
    		System.out.println();
    		System.out.println("Program tests an exemplary rule-based classifier on a dataset.");
    		System.out.println("If two data files are provided");
    		System.out.println("the first one is used as the training data");
    		System.out.println("and the second one as the test data.");
    		System.out.println("If one data file is provided");
    		System.out.println("it is randomly divided into the training and the test data.");
    		System.exit(0);
    	}

    	// set the output to standard output
        Report.addErrorOutput(new StandardErrorOutput());
        Report.addInfoOutput(new StandardOutput());

        // load data and split optionally
        DoubleDataTable trainTable = new ArrayListDoubleDataTable(new File(args[0]), new EmptyProgress());
        DoubleDataTable testTable;
        if (args.length==1)
        {
            ArrayList<DoubleData>[] parts = trainTable.randomSplit(2, 1);
            trainTable = new ArrayListDoubleDataTable(parts[0]);
            testTable = new ArrayListDoubleDataTable(parts[1]);
        }
        else
            testTable = new ArrayListDoubleDataTable(new File(args[1]), new EmptyProgress());

        // print the training table info
        if (args.length==1)
        	Report.displaynl(args[0]+" (training part)");
        else Report.displaynl(args[0]);
        Report.displaynl(trainTable);
        
        // train the rule-based classifier
        Classifier ruleBased = new MajorityClassifierWithRules(null, trainTable, new StdOutProgress());

        // print the test table info
        if (args.length==1)
        	Report.displaynl(args[0]+" (testing part)");
        else Report.displaynl(args[1]);
        Report.displaynl(testTable);
        
        // test the rule-based classifier
        TestResult results = new SingleClassifierTest().classify(ruleBased, testTable, new StdOutProgress());

        // print the results
        Report.displaynl(results);
        Report.close();
    }
}

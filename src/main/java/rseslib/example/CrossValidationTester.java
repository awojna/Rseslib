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


package rseslib.example;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import rseslib.processing.classification.*;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.Report;
import rseslib.system.output.StandardErrorOutput;
import rseslib.system.output.StandardOutput;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.StdOutProgress;

/**
 * Cross-validation test for a set of classifiers.
 * It is easily convertable to multitest
 * and cross-validation multitest (see the comments in the main class method).
 *
 * @author      Arkadiusz Wojna
 */
public class CrossValidationTester
{
    /**
     * The main method creates the subdirectory results/
     * in the directory with a data file and writes the cross-validation results
     * to the file with the name corresponding to the data file name.
     *
     * @param args Two arguments: the path to data file and the path to header file.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
    	// check the number of program arguments and print help
    	if (args.length!=2)
    	{
    		System.out.println("Usage:");
    		System.out.println("    java ... rseslib.example.CrossValidationTester  <number of folds>  <data file>");
    		System.out.println("Program runs n-cross-validation on a dataset.");
    		System.exit(0);
    	}

    	// set the output to standard output
        Report.addErrorOutput(new StandardErrorOutput());
        Report.addInfoOutput(new StandardOutput());

        // load data and print the table info
        DoubleDataTable table = new ArrayListDoubleDataTable(new File(args[1]), new EmptyProgress());
        Report.displaynl(table);

        // set the cross-validation parameter
        Properties params = Configuration.loadDefaultProperties(CrossValidationTest.class);
        params.setProperty(CrossValidationTest.NO_OF_FOLDS_PROPERTY_NAME, args[0]);

        // run cross-validation
        CrossValidationTest crossValid = new CrossValidationTest(params, new RseslibClassifiers(null).getClassifierSet());
        Map<String,MultipleTestResult> results = crossValid.test(table, new StdOutProgress());
        Report.displaynl();
        Report.displaynl();

        // uncomment to run test with multiple random split
//        MultipleRandomSplitTest multiTst = new MultipleRandomSplitTest(null, new RseslibClassifiers(null).getClassifierSet());
//        Map<String,MultipleTestResult> results = multiTst.test(table, new StdOutProgress());

        // uncomment to run multiple cross-validation
//        MultipleCrossValidationTest cvMultiTst = new MultipleCrossValidationTest(null, new RseslibClassifiers(null).getClassifierSet());
//        Map<String,MultipleTestResult> results = cvMultiTst.test(table, new StdOutProgress());

        // print the results
        Report.displayMapWithMultiLines("Classification results", results);
        Report.close();
    }
}

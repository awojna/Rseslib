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
 * Cross-validation test of Rseslib classifiers.
 * It is easily convertible to multitest
 * and cross-validation multitest (see the comments in the main class method).
 *
 * @author      Arkadiusz Wojna
 */
public class CrossValidation
{
    /**
     * The main method runs the cross-validation test for Rseslib classifiers
     * and prints the results to the standard output.
     *
     * @param args Two arguments: the number of folds and the path to a data file.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
    	// check the number of program arguments and print help
    	if (args.length!=2)
    	{
    		System.out.println("Program runs n-cross-validation on a dataset.");
    		System.out.println("Usage:");
    		System.out.println("    java ... rseslib.example.CrossValidationTester  <number of folds>  <data file>");
    		System.exit(0);
    	}

    	// set the output to standard output
        Report.addErrorOutput(new StandardErrorOutput());
        Report.addInfoOutput(new StandardOutput());

        // load data and print the table info
        DoubleDataTable table = new ArrayListDoubleDataTable(new File(args[1]), new EmptyProgress());
        Report.displaynl(table);

        // define classifiers to be tested
        // delete the lines with the classifiers not to be tested
        ClassifierSet classifiers = new ClassifierSet();
        classifiers.addClassifier("Rough Set", rseslib.processing.classification.rules.roughset.RoughSetRules.class);
        classifiers.addClassifier("KNN", rseslib.processing.classification.parameterised.knn.KNearestNeighbors.class);
        classifiers.addClassifier("Local KNN", rseslib.processing.classification.parameterised.knn.LocalKNearestNeighbors.class);
        if (table.attributes().nominalDecisionAttribute().noOfValues()==2)
        	classifiers.addClassifier("RIONIDA", rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.class);
        classifiers.addClassifier("C4.5", rseslib.processing.classification.tree.c45.C45DecisionTree.class);
        classifiers.addClassifier("AQ15", rseslib.processing.classification.rules.AQ15.class);
        classifiers.addClassifier("Neural Network", rseslib.processing.classification.neural.NeuralNetwork.class);
        classifiers.addClassifier("Naive Bayes", rseslib.processing.classification.bayes.NaiveBayes.class);
        classifiers.addClassifier("SVM", rseslib.processing.classification.svm.SupportVectorMachine.class);
        classifiers.addClassifier("PCN", rseslib.processing.classification.parameterised.pca.PrincipalComponentNetwork.class);
        classifiers.addClassifier("Local PCN", rseslib.processing.classification.parameterised.pca.LocalPrincipalComponentNetwork.class);

        // define an exemplary classifier with non-default parameter values
        Properties nonDefaultParams = Configuration.loadDefaultProperties(rseslib.processing.classification.tree.c45.C45DecisionTree.class);
        nonDefaultParams.setProperty("pruning", "TRUE");
        classifiers.addClassifier("C4.5 Pruned", rseslib.processing.classification.tree.c45.C45DecisionTree.class, nonDefaultParams);
        
        // run cross-validation
        Properties cvParams = Configuration.loadDefaultProperties(CrossValidationTest.class);
        cvParams.setProperty(CrossValidationTest.NO_OF_FOLDS_PROPERTY_NAME, args[0]);
        CrossValidationTest crossValid = new CrossValidationTest(cvParams, classifiers);
        Map<String,MultipleTestResult> results = crossValid.test(table, new StdOutProgress());
        Report.displaynl();
        Report.displaynl();

        // uncomment to run test with multiple random split
//        MultipleRandomSplitTest multiTst = new MultipleRandomSplitTest(null, classifiers);
//        Map<String,MultipleTestResult> results = multiTst.test(table, new StdOutProgress());

        // uncomment to run multiple cross-validation
//        MultipleCrossValidationTest cvMultiTst = new MultipleCrossValidationTest(null, classifiers);
//        Map<String,MultipleTestResult> results = cvMultiTst.test(table, new StdOutProgress());

        // print the results
        Report.displayMapWithMultiLines("Classification results", results);
        Report.close();
    }
}

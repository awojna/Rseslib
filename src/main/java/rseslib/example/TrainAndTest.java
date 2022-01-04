/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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
import java.util.Map;
import java.util.Properties;

import rseslib.processing.classification.ClassifierSet;
import rseslib.processing.classification.TestResult;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.Report;
import rseslib.system.output.StandardErrorOutput;
import rseslib.system.output.StandardOutput;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.StdOutProgress;

/**
 * Single test of Rseslib classifiers
 * on data split into a training and a test table.
 * If only one data file is provided,
 * the method splits data randomly with the ratio 2:1
 * in the training and the test part.
 * The results are displayed on the standard output.
 *
 * @author      Arkadiusz Wojna
 */
public class TrainAndTest
{
	/**
     * Main testing method.
     *
     * @param args Parameters of the method: one or two data files.
     * @throws Exception when an error occurs.
     */
    public static void main(String[] args) throws Exception
    {
    	// check the number of program arguments and print help
    	if (args.length!=1 && args.length!=2)
    	{
    		System.out.println("Program tests the rseslib classifiers on a dataset.");
    		System.out.println("If two data files are provided");
    		System.out.println("the first one is used as the training data");
    		System.out.println("and the second one as the test data.");
    		System.out.println("If one data file is provided");
    		System.out.println("it is randomly divided into the training and the test data.");
    		System.out.println();
    		System.out.println("Usage:");
    		System.out.println("    java ... rseslib.example.TrainAndTest <data file>");
    		System.out.println("    java ... rseslib.example.TrainAndTest <training file> <test file>");
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

        // define classifiers to be tested
        // delete the lines with the classifiers not to be tested
        ClassifierSet classifiers = new ClassifierSet();
        classifiers.addClassifier("Rough Set", rseslib.processing.classification.rules.roughset.RoughSetRuleClassifier.class);
        classifiers.addClassifier("KNN", rseslib.processing.classification.parameterised.knn.KnnClassifier.class);
        classifiers.addClassifier("Local KNN", rseslib.processing.classification.parameterised.knn.LocalKnnClassifier.class);
        classifiers.addClassifier("C4.5", rseslib.processing.classification.tree.c45.C45.class);
        classifiers.addClassifier("AQ15", rseslib.processing.classification.rules.AQ15Classifier.class);
        classifiers.addClassifier("Neural Network", rseslib.processing.classification.neural.NeuronNetwork.class);
        classifiers.addClassifier("Naive Bayes", rseslib.processing.classification.bayes.NaiveBayesClassifier.class);
        classifiers.addClassifier("SVM", rseslib.processing.classification.svm.SVM.class);
        classifiers.addClassifier("PCA", rseslib.processing.classification.parameterised.pca.PcaClassifier.class);
        classifiers.addClassifier("Local PCA", rseslib.processing.classification.parameterised.pca.LocalPcaClassifier.class);

        // define an exemplary classifier with non-default parameter values
        Properties nonDefaultParams = Configuration.loadDefaultProperties(rseslib.processing.classification.tree.c45.C45.class);
        nonDefaultParams.setProperty("pruning", "TRUE");
        classifiers.addClassifier("C4.5 Pruned", rseslib.processing.classification.tree.c45.C45.class, nonDefaultParams);
        
        // print the training table info
        if (args.length==1)
        	Report.displaynl(args[0]+" (training part)");
        else Report.displaynl(args[0]);
        Report.displaynl(trainTable);
        
        // train the classifiers
        classifiers.train(trainTable, new StdOutProgress());
        Report.displaynl();

        // print the test table info
        if (args.length==1)
        	Report.displaynl(args[0]+" (testing part)");
        else Report.displaynl(args[1]);
        Report.displaynl(testTable);
        
        // test the classifiers
        Map<String,TestResult> results = classifiers.classify(testTable, new StdOutProgress());
        Report.displaynl();

        // print the results
        Report.displayMapWithMultiLines("Classification results for test table", results);
        Report.close();
    }
}

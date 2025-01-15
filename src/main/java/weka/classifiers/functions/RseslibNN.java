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


package weka.classifiers.functions;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import rseslib.processing.classification.neural.Global;
import rseslib.processing.classification.neural.NeuralNetwork;
import weka.classifiers.AbstractRseslibClassifierWrapper;
import weka.core.Option;
import weka.core.Utils;

public class RseslibNN extends AbstractRseslibClassifierWrapper
{
	/** for serialization */
	static final long serialVersionUID = 1L;
	
	public RseslibNN() throws Exception
	{
		super(NeuralNetwork.class);
	}
	
	/**
	 * Returns a string describing classifier
	 * @return a description suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {

		return  "Neural network with automated network structure selection. "
				+ "It uses the classical back-propagation algorithm and sigmoid activation function for all neurons. "
				+ "For more information see\n\n"
				+ getTechnicalInformation().toString();
	}
	
	// methods required for option 'timeLimit'
	public void setTimeLimit(int value)
	{
		getProperties().setProperty(Global.TIME_LIMIT_NAME, String.valueOf(value));
	}
	
	public int getTimeLimit()
	{
		return Integer.parseInt(getProperties().getProperty(Global.TIME_LIMIT_NAME));
	}
	
	public String timeLimitTipText()
	{
		return "Time limit on searching for the optimal network (in seconds)";
	}

	// methods required for option 'initialAlpha'
	public void setInitialAlpha(double value)
	{
		getProperties().setProperty(Global.INITIAL_ALFA_NAME, String.valueOf(value));
	}
	
	public double getInitialAlpha()
	{
		return Double.parseDouble(getProperties().getProperty(Global.INITIAL_ALFA_NAME));
	}
	
	public String initialAlphaTipText()
	{
		return "Initial value of the learning speed coeficient alpha in the back-propagation algorithm, the coeficient decreases over time";
	}

	// methods required for option 'targetAccuracy'
	public void setTargetAccuracy(double value)
	{
		getProperties().setProperty(Global.DEST_TARGET_RATIO_NAME, String.valueOf(value));
	}
	
	public double getTargetAccuracy()
	{
		return Double.parseDouble(getProperties().getProperty(Global.DEST_TARGET_RATIO_NAME));
	}
	
	public String targetAccuracyTipText()
	{
		return "Target accuracy of classification (%), when it is achieved on the validation set the learning process stops";
	}
	
	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {

		Vector<Option> result = new Vector<Option>();

		Enumeration enm = super.listOptions();
		while (enm.hasMoreElements())
			result.addElement((Option)enm.nextElement());

		result.addElement(new Option(
				"\tTime limit on searching for the optimal network (in seconds).\n"
						+ "\t(default: 120)",
						"L", 1, "-L <time limit>"));

		result.addElement(new Option(
				"\tInitial value of the learning speed coefficient alpha.\n"
						+ "\t(default: 0.9)",
						"A", 1, "-A <initial alpha>"));

		result.addElement(new Option(
				"\tTarget accuracy of classification on the validation set (%).\n"
						+ "\t(default: 99.99)",
						"T", 1, "-T <target accuracy>"));

		return result.elements();
	}

	/**
	 * Parses a given list of options.
	 *
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported 
	 */
	public void setOptions(String[] options) throws Exception
	{
		String	tmpStr;

		resetToDefaults();
		
		tmpStr = Utils.getOption('L', options);
		if (tmpStr.length() != 0)
			setTimeLimit(Integer.parseInt(tmpStr));

		tmpStr = Utils.getOption('A', options);
		if (tmpStr.length() != 0)
			setInitialAlpha(Double.parseDouble(tmpStr));

		tmpStr = Utils.getOption('T', options);
		if (tmpStr.length() != 0)
			setTargetAccuracy(Double.parseDouble(tmpStr));

		super.setOptions(options);
	}
	  
	/**
	 * Gets the current settings of the classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions()
	{
		Vector<String> result = new Vector<String>();

		result.add("-L");
		result.add("" + getTimeLimit());

		result.add("-A");
		result.add("" + getInitialAlpha());

		result.add("-T");
		result.add("" + getTargetAccuracy());

		result.addAll(Arrays.asList(super.getOptions())); // superclass
		return result.toArray(new String[result.size()]);
	}

	/**
	* Main method for executing this classifier.
	*
	* @param args the options, use "-h" to display options
	*/
	public static void main(String[] args) throws Exception
	{
		runClassifier(new RseslibNN(), args);
	}
}

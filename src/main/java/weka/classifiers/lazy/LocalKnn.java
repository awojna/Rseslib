/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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


package weka.classifiers.lazy;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import rseslib.processing.classification.parameterised.knn.LocalKnnClassifier;
import rseslib.processing.metrics.MetricFactory;
import weka.classifiers.AbstractRseslibClassifierWrapper;
import weka.core.Option;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

/**
 * Weka wrapper for K-NN classifier with local metric induction.
 * @see rseslib.processing.classification.parameterised.knn.LocalKnnClassifier
 *
 * @author      Arkadiusz Wojna
 */
public class LocalKnn extends AbstractRseslibClassifierWrapper
{
	/** for serialization */
	static final long serialVersionUID = 1L;
	
	  /** Tags for 'metric' option */
	  private static final Tag[] TAGS_METRIC = {
		    new Tag(MetricFactory.MetricType.CityAndHamming.ordinal(), MetricFactory.MetricType.CityAndHamming.name()),
		    new Tag(MetricFactory.MetricType.CityAndSimpleValueDifference.ordinal(), MetricFactory.MetricType.CityAndSimpleValueDifference.name()),
		    new Tag(MetricFactory.MetricType.DensityBasedValueDifference.ordinal(), MetricFactory.MetricType.DensityBasedValueDifference.name()),
		    new Tag(MetricFactory.MetricType.InterpolatedValueDifference.ordinal(), MetricFactory.MetricType.InterpolatedValueDifference.name()),
	  };

	  /** Tags for 'weightingMethod' option */
	  private static final Tag[] TAGS_WEIGHTING = {
		    new Tag(MetricFactory.Weighting.None.ordinal(), MetricFactory.Weighting.None.name()),
		    new Tag(MetricFactory.Weighting.Perceptron.ordinal(), MetricFactory.Weighting.Perceptron.name()),
		    new Tag(MetricFactory.Weighting.DistanceBased.ordinal(), MetricFactory.Weighting.DistanceBased.name()),
		    new Tag(MetricFactory.Weighting.AccuracyBased.ordinal(), MetricFactory.Weighting.AccuracyBased.name()),
	  };
	
	  /** Tags for 'weightingMethod' option */
	  private static final Tag[] TAGS_VOTING = {
		    new Tag(LocalKnnClassifier.Voting.Equal.ordinal(), LocalKnnClassifier.Voting.Equal.name()),
		    new Tag(LocalKnnClassifier.Voting.InverseDistance.ordinal(), LocalKnnClassifier.Voting.InverseDistance.name()),
		    new Tag(LocalKnnClassifier.Voting.InverseSquareDistance.ordinal(), LocalKnnClassifier.Voting.InverseSquareDistance.name()),
	  };

	public LocalKnn() throws Exception
	{
		super(LocalKnnClassifier.class);
	}
	
	/**
	 * Returns a string describing classifier
	 * @return a description suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {

		return  "K nearest neighours classifier with local metric induction. "
				+ "Intendend to improve accuracy in relation to the standard k-nn, particularly in case of data with nominal attributes. "
				+ "Works reasonably with 2000+ training instances. "
				+ "For more information see\n\n"
				+ getTechnicalInformation().toString();
	}

	// methods required for option 'metric'
	String enumarateMetricToString()
	{
		StringBuilder sb = new StringBuilder();
		for (MetricFactory.MetricType metric : MetricFactory.MetricType.values())
			sb.append("\t\t"+metric.ordinal()+"="+metric.name()+"\n");
		return sb.toString();
	}
	
	int getMetricOrdinal()
	{
		return MetricFactory.MetricType.valueOf(getProperties().getProperty(MetricFactory.METRIC_PROPERTY_NAME)).ordinal();
	}
	
	public void setMetric(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_METRIC) {
	    	MetricFactory.MetricType value = MetricFactory.MetricType.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(MetricFactory.METRIC_PROPERTY_NAME, value.name());
	    }
	}

	public SelectedTag getMetric()
	{
		return new SelectedTag(getMetricOrdinal(), TAGS_METRIC);
	}

	public String metricTipText()
	{
	    return "Type of a distance measure used for particular attributes";
	}

	// methods required for option 'vicinitySizeForDensityBasedMetric'
	public void setVicinitySizeForDensityBasedMetric(int value)
	{
		getProperties().setProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getVicinitySizeForDensityBasedMetric()
	{
		return Integer.parseInt(getProperties().getProperty(MetricFactory.VICINITY_SIZE_FOR_DBVDM_PROPERTY_NAME));
	}
	
	public String vicinitySizeForDensityBasedMetricTipText()
	{
		return "Vicinity size in density based metric (used only if metric = DensityBasedValueDifference)";
	}

	// methods required for option 'weightingMethod'
	String enumarateWeightingToString()
	{
		StringBuilder sb = new StringBuilder();
		for (MetricFactory.Weighting weighting : MetricFactory.Weighting.values())
			sb.append("\t\t"+weighting.ordinal()+"="+weighting.name()+"\n");
		return sb.toString();
	}

	int getWeigthingOrdinal()
	{
		return MetricFactory.Weighting.valueOf(getProperties().getProperty(LocalKnnClassifier.WEIGHTING_METHOD_PROPERTY_NAME)).ordinal();
	}

	public void setWeightingMethod(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_WEIGHTING) {
	    	MetricFactory.Weighting value = MetricFactory.Weighting.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(LocalKnnClassifier.WEIGHTING_METHOD_PROPERTY_NAME, value.name());
	    }
	}

	public SelectedTag getWeightingMethod()
	{
		return new SelectedTag(getWeigthingOrdinal(), TAGS_WEIGHTING);
	}

	public String weightingMethodTipText()
	{
	    return "Attribute weighting method";
	}

	// methods required for option 'learnOptimalK'
	public void setLearnOptimalK(boolean value)
	{
		getProperties().setProperty(LocalKnnClassifier.LEARN_OPTIMAL_K_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getLearnOptimalK()
	{
		return Boolean.parseBoolean(getProperties().getProperty(LocalKnnClassifier.LEARN_OPTIMAL_K_PROPERTY_NAME));
	}
	
	public String learnOptimalKTipText()
	{
		return "Whether the classifier learns the optimal number of nearest neighbors";
	}

	// methods required for option 'localSetSize'
	public void setLocalSetSize(int value)
	{
		getProperties().setProperty(LocalKnnClassifier.LOCAL_SET_SIZE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getLocalSetSize()
	{
		return Integer.parseInt(getProperties().getProperty(LocalKnnClassifier.LOCAL_SET_SIZE_PROPERTY_NAME));
	}
	
	public String localSetSizeTipText()
	{
		return "Size of the local set used to induce local metric";
	}

	// methods required for option 'k'
	public void setK(int value)
	{
		getProperties().setProperty(LocalKnnClassifier.K_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getK()
	{
		return Integer.parseInt(getProperties().getProperty(LocalKnnClassifier.K_PROPERTY_NAME));
	}
	
	public String kTipText()
	{
		return "Number of nearest neighbours used to vote for decision (set automatically if learnOptimalK = TRUE)";
	}

	// methods required for option 'voting'
	String enumarateVotingToString()
	{
		StringBuilder sb = new StringBuilder();
		for (LocalKnnClassifier.Voting voting : LocalKnnClassifier.Voting.values())
			sb.append("\t\t"+voting.ordinal()+"="+voting.name()+"\n");
		return sb.toString();
	}

	int getVotingOrdinal()
	{
		return LocalKnnClassifier.Voting.valueOf(getProperties().getProperty(LocalKnnClassifier.VOTING_PROPERTY_NAME)).ordinal();
	}

	public void setVoting(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_VOTING) {
	    	LocalKnnClassifier.Voting value = LocalKnnClassifier.Voting.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(LocalKnnClassifier.VOTING_PROPERTY_NAME, value.name());
	    }
	}

	public SelectedTag getVoting()
	{
		return new SelectedTag(getVotingOrdinal(), TAGS_VOTING);
	}

	public String votingTipText()
	{
	    return "Type of voting for the decision by nearest neighbours";
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
				"\tDistance measure:\n"
						+ enumarateMetricToString()
						+ "\t(default: "+MetricFactory.MetricType.CityAndSimpleValueDifference.ordinal()+"="+MetricFactory.MetricType.CityAndSimpleValueDifference.name()+")",
						"M", 1, "-M"));

		result.addElement(new Option(
				"\tVicinity size in " + MetricFactory.MetricType.DensityBasedValueDifference.name() + " metric.\n"
						+ "\t(default: 200)",
						"S", 1, "-S <vicinity size>"));

		result.addElement(new Option(
				"\tAttribute weighting method:\n"
						+ enumarateWeightingToString()
						+ "\t(default: "+MetricFactory.Weighting.DistanceBased.ordinal()+"="+MetricFactory.Weighting.DistanceBased.name()+")",
						"W", 1, "-W"));

	    result.addElement(new Option("\tDo not optimize the number of the nearest neighbours automatically.",
			      "U", 0, "-U"));

		result.addElement(new Option(
				"\tSize of the local set used to induce local metric.\n"
						+ "\t(default: 100)",
						"L", 1, "-L <local set size>"));

		result.addElement(new Option(
				"\tNumber of nearest neighbours if not set automatically.\n"
						+ "\t(default: 1)",
						"K", 1, "-K <number of neighbours>"));

		result.addElement(new Option(
				"\tVoting method:\n"
						+ enumarateVotingToString()
						+ "\t(default: "+LocalKnnClassifier.Voting.InverseSquareDistance.ordinal()+"="+LocalKnnClassifier.Voting.InverseSquareDistance.name()+")",
						"V", 1, "-V"));

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
		
		tmpStr = Utils.getOption('M', options);
		if (tmpStr.length() != 0)
			setMetric(new SelectedTag(Integer.parseInt(tmpStr), TAGS_METRIC));

		tmpStr = Utils.getOption('S', options);
		if (tmpStr.length() != 0) {
			String metricName = getProperties().getProperty(MetricFactory.METRIC_PROPERTY_NAME);
			if (MetricFactory.MetricType.valueOf(metricName) != MetricFactory.MetricType.DensityBasedValueDifference)
				throw new Exception("Vicinity size makes sense only for " + MetricFactory.MetricType.DensityBasedValueDifference + " metric");
			setVicinitySizeForDensityBasedMetric(Integer.parseInt(tmpStr));
		}

		tmpStr = Utils.getOption('W', options);
		if (tmpStr.length() != 0)
			setWeightingMethod(new SelectedTag(Integer.parseInt(tmpStr), TAGS_WEIGHTING));
		
	    boolean learn = !Utils.getFlag('U', options);
	    setLearnOptimalK(learn);

		tmpStr = Utils.getOption('L', options);
		if (tmpStr.length() != 0)
			setLocalSetSize(Integer.parseInt(tmpStr));

	    if (learn) {
			tmpStr = Utils.getOption('K', options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting K makes sense only if automatic optimization is not used");
	    } else {
			tmpStr = Utils.getOption('K', options);
			if (tmpStr.length() != 0)
				setK(Integer.parseInt(tmpStr));
	    }

		tmpStr = Utils.getOption('V', options);
		if (tmpStr.length() != 0)
			setVoting(new SelectedTag(Integer.parseInt(tmpStr), TAGS_VOTING));

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

		result.add("-M");
		result.add("" + getMetricOrdinal());

		if (getMetricOrdinal() == MetricFactory.MetricType.DensityBasedValueDifference.ordinal()) {
			result.add("-S");
			result.add("" + getVicinitySizeForDensityBasedMetric());
		}

		result.add("-W");
		result.add("" + getWeigthingOrdinal());

		result.add("-L");
		result.add("" + getLocalSetSize());

		if(!getLearnOptimalK()) {
			result.add("-U");
			result.add("-K");
			result.add("" + getK());
		}

		result.add("-V");
		result.add("" + getVotingOrdinal());

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
		runClassifier(new LocalKnn(), args);
	}
}

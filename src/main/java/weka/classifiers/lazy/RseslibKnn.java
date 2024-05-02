/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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

import rseslib.processing.classification.parameterised.knn.KNearestNeighbors;
import rseslib.processing.metrics.MetricFactory;
import weka.classifiers.AbstractRseslibClassifierWrapper;
import weka.core.Option;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

/**
 * Weka wrapper for rseslib K-NN classifier.
 * @see rseslib.processing.classification.parameterised.knn.KNearestNeighbors
 *
 * @author      Arkadiusz Wojna
 */
public class RseslibKnn extends AbstractRseslibClassifierWrapper
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
	
	  /** Tags for 'voting' option */
	  private static final Tag[] TAGS_VOTING = {
		    new Tag(KNearestNeighbors.Voting.Equal.ordinal(), KNearestNeighbors.Voting.Equal.name()),
		    new Tag(KNearestNeighbors.Voting.InverseDistance.ordinal(), KNearestNeighbors.Voting.InverseDistance.name()),
		    new Tag(KNearestNeighbors.Voting.InverseSquareDistance.ordinal(), KNearestNeighbors.Voting.InverseSquareDistance.name()),
	  };

	public RseslibKnn() throws Exception
	{
		super(KNearestNeighbors.class);
	}
	
	/**
	 * Returns a string describing classifier
	 * @return a description suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {

		return  "K nearest neighours classifier "
				+ "with various distance measures applicable also to data with both numeric and nominal attributes. "
				+ "It implements fast neighour search in large data sets and has the mode to work as RIONA algorithm. "
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
		return MetricFactory.Weighting.valueOf(getProperties().getProperty(KNearestNeighbors.WEIGHTING_METHOD_PROPERTY_NAME)).ordinal();
	}

	public void setWeightingMethod(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_WEIGHTING) {
	    	MetricFactory.Weighting value = MetricFactory.Weighting.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(KNearestNeighbors.WEIGHTING_METHOD_PROPERTY_NAME, value.name());
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

	// methods required for option 'indexing'
	public void setIndexing(boolean value)
	{
		getProperties().setProperty(KNearestNeighbors.INDEXING_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getIndexing()
	{
		return Boolean.parseBoolean(getProperties().getProperty(KNearestNeighbors.INDEXING_PROPERTY_NAME));
	}
	
	public String indexingTipText()
	{
		return "Whether the classifier uses indexing to accelerate search of nearest neighbours";
	}

	// methods required for option 'learnOptimalK'
	public void setLearnOptimalK(boolean value)
	{
		getProperties().setProperty(KNearestNeighbors.LEARN_OPTIMAL_K_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getLearnOptimalK()
	{
		return Boolean.parseBoolean(getProperties().getProperty(KNearestNeighbors.LEARN_OPTIMAL_K_PROPERTY_NAME));
	}
	
	public String learnOptimalKTipText()
	{
		return "Whether the classifier learns the optimal number of nearest neighbors";
	}

	// methods required for option 'maxK'
	public void setMaxK(int value)
	{
		getProperties().setProperty(KNearestNeighbors.MAXIMAL_K_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getMaxK()
	{
		return Integer.parseInt(getProperties().getProperty(KNearestNeighbors.MAXIMAL_K_PROPERTY_NAME));
	}
	
	public String maxKTipText()
	{
		return "Maximal possible k while learning the optimum (used only if learnOptimalK = TRUE)";
	}

	// methods required for option 'k'
	public void setK(int value)
	{
		getProperties().setProperty(KNearestNeighbors.K_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getK()
	{
		return Integer.parseInt(getProperties().getProperty(KNearestNeighbors.K_PROPERTY_NAME));
	}
	
	public String kTipText()
	{
		return "Number of nearest neighbours used to vote for decision (set automatically if learnOptimalK = TRUE)";
	}

	// methods required for option 'filterNeighboursUsingRules'
	public void setFilterNeighboursUsingRules(boolean value)
	{
		getProperties().setProperty(KNearestNeighbors.FILTER_NEIGHBOURS_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getFilterNeighboursUsingRules()
	{
		return Boolean.parseBoolean(getProperties().getProperty(KNearestNeighbors.FILTER_NEIGHBOURS_PROPERTY_NAME));
	}
	
	public String filterNeighboursUsingRulesTipText()
	{
		return "Whether nearest neighbours are filtered by rules (RIONA)";
	}

	// methods required for option 'voting'
	String enumarateVotingToString()
	{
		StringBuilder sb = new StringBuilder();
		for (KNearestNeighbors.Voting voting : KNearestNeighbors.Voting.values())
			sb.append("\t\t"+voting.ordinal()+"="+voting.name()+"\n");
		return sb.toString();
	}

	int getVotingOrdinal()
	{
		return KNearestNeighbors.Voting.valueOf(getProperties().getProperty(KNearestNeighbors.VOTING_PROPERTY_NAME)).ordinal();
	}

	public void setVoting(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_VOTING) {
	    	KNearestNeighbors.Voting value = KNearestNeighbors.Voting.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(KNearestNeighbors.VOTING_PROPERTY_NAME, value.name());
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

	    result.addElement(new Option("\tDo not use indexing tree (linear search for nearest neighbours is used).",
			      "L", 0, "-L"));

	    result.addElement(new Option("\tDo not optimize the number of the nearest neighbours automatically.",
			      "U", 0, "-U"));

		result.addElement(new Option(
				"\tMaximum number of neighbours while optimizing automatically.\n"
						+ "\t(default: 100)",
						"N", 1, "-N <maximum number>"));

		result.addElement(new Option(
				"\tNumber of nearest neighbours if not set automatically.\n"
						+ "\t(default: 1)",
						"K", 1, "-K <number of neighbours>"));

	    result.addElement(new Option("\tUse rules to filter nearest neighbours (RIONA).",
			      "R", 0, "-R"));

		result.addElement(new Option(
				"\tVoting method:\n"
						+ enumarateVotingToString()
						+ "\t(default: "+KNearestNeighbors.Voting.InverseSquareDistance.ordinal()+"="+KNearestNeighbors.Voting.InverseSquareDistance.name()+")",
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
		
	    setIndexing(!Utils.getFlag('L', options));

	    boolean learn = !Utils.getFlag('U', options);
	    setLearnOptimalK(learn);
	    
	    if (learn) {
			tmpStr = Utils.getOption('N', options);
			if (tmpStr.length() != 0)
				setMaxK(Integer.parseInt(tmpStr));
			tmpStr = Utils.getOption('K', options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting K makes sense only if automatic optimization is not used");
	    } else {
			tmpStr = Utils.getOption('K', options);
			if (tmpStr.length() != 0)
				setK(Integer.parseInt(tmpStr));
			tmpStr = Utils.getOption('N', options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting maximum number of neighbours makes sense only if automatic optimization is used");
	    }

	    setFilterNeighboursUsingRules(Utils.getFlag('R', options));

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

		if(!getIndexing())
			result.add("-L");

		if(getLearnOptimalK()) {
			result.add("-N");
			result.add("" + getMaxK());
		} else {
			result.add("-U");
			result.add("-K");
			result.add("" + getK());
		}

		if(getFilterNeighboursUsingRules())
			result.add("-R");

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
		runClassifier(new RseslibKnn(), args);
	}
}

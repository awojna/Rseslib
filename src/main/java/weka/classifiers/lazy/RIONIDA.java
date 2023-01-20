/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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

import rseslib.processing.classification.parameterised.knn.rionida.AbstractParameterised3DClassifier;
import rseslib.processing.metrics.MetricFactory;
import weka.classifiers.AbstractRseslibClassifierWrapper;
import weka.core.Option;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

/**
 * Weka wrapper for rseslib RIONIDA classifier.
 * @see rseslib.processing.classification.parameterised.knn.rionida.RIONIDA
 *
 * @author      Arkadiusz Wojna
 */
public class RIONIDA extends AbstractRseslibClassifierWrapper
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
		    new Tag(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.Equal.ordinal(), rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.Equal.name()),
		    new Tag(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.InverseDistance.ordinal(), rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.InverseDistance.name()),
		    new Tag(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.InverseSquareDistance.ordinal(), rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.InverseSquareDistance.name()),
	  };

	  /** Tags for 'optimisationMeasure' option */
	  private static final Tag[] TAGS_OPTIMISATION_MEASURE = {
		    new Tag(AbstractParameterised3DClassifier.OptimisationMeasure.Fmeasure.ordinal(), AbstractParameterised3DClassifier.OptimisationMeasure.Fmeasure.name()),
		    new Tag(AbstractParameterised3DClassifier.OptimisationMeasure.Gmean.ordinal(), AbstractParameterised3DClassifier.OptimisationMeasure.Gmean.name()),
		    new Tag(AbstractParameterised3DClassifier.OptimisationMeasure.Accuracy.ordinal(), AbstractParameterised3DClassifier.OptimisationMeasure.Accuracy.name()),
	  };

	public RIONIDA() throws Exception
	{
		super(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.class);
	}
	
	/**
	 * Returns true if the classifier supports binary decision only.
	 * 
	 * @return	True if the classifier supports binary decision only.
	 */
	protected boolean binaryDecisionOnly()
	{
		return true;
	}

	/**
	 * Returns a string describing classifier
	 * @return a description suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {

		return  "Classifier for imbalanced data combining k nearest neighbours with rule induction "
				+ "applicable to data with both numeric and nominal attributes. "
				+ "It implements advanced multidimensional optimization of parameters. "
				+ "For more information see\n\n"
				+ getTechnicalInformation().toString();
	}

	// methods required for option 'useMajorityDecAsMinorityDec'
	public void setUseMajorityDecAsMinorityDec(boolean value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.MAJORITY_AS_MINORITY_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getUseMajorityDecAsMinorityDec()
	{
		return Boolean.parseBoolean(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.MAJORITY_AS_MINORITY_PROPERTY_NAME));
	}
	
	public String useMajorityDecAsMinorityDecTipText()
	{
		return "Whether the classifier treats the majority decision as the minority decision";
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
		return MetricFactory.Weighting.valueOf(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.WEIGHTING_METHOD_PROPERTY_NAME)).ordinal();
	}

	public void setWeightingMethod(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_WEIGHTING) {
	    	MetricFactory.Weighting value = MetricFactory.Weighting.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.WEIGHTING_METHOD_PROPERTY_NAME, value.name());
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
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.INDEXING_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getIndexing()
	{
		return Boolean.parseBoolean(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.INDEXING_PROPERTY_NAME));
	}
	
	public String indexingTipText()
	{
		return "Whether the classifier uses indexing to accelerate search of nearest neighbours";
	}

	// methods required for option 'learnOptimalParameters'
	public void setLearnOptimalParameters(boolean value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.LEARN_OPTIMAL_PARAMETERS_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getLearnOptimalParameters()
	{
		return Boolean.parseBoolean(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.LEARN_OPTIMAL_PARAMETERS_PROPERTY_NAME));
	}
	
	public String learnOptimalParametersTipText()
	{
		return "Whether the classifier learns the optimal values of parameters";
	}

	// methods required for option 'optimisation4D'
	public void setOptimisation4D(boolean value)
	{
		getProperties().setProperty(AbstractParameterised3DClassifier.OPTIMISATION_4D_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getOptimisation4D()
	{
		return Boolean.parseBoolean(getProperties().getProperty(AbstractParameterised3DClassifier.OPTIMISATION_4D_PROPERTY_NAME));
	}
	
	public String optimisation4DTipText()
	{
		return "Whether to use 3-dimensional or 4-dimensional optimisation";
	}

	// methods required for option 'k'
	public void setK(int value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.K_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getK()
	{
		return Integer.parseInt(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.K_PROPERTY_NAME));
	}
	
	public String kTipText()
	{
		return "Number of nearest neighbours used to vote for decision (set automatically if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'maxK'
	public void setMaxK(int value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.MAXIMAL_K_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getMaxK()
	{
		return Integer.parseInt(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.MAXIMAL_K_PROPERTY_NAME));
	}
	
	public String maxKTipText()
	{
		return "Maximal possible k while learning the optimum (used only if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'pThreshold'
	public void setPThreshold(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getPThreshold()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_PROPERTY_NAME));
	}
	
	public String PThresholdTipText()
	{
		return "It assigns minority class if weight of minority class divided by sum of weights is greater than or equal to this threshold and majority class otherwise";
	}

	// methods required for option 'pThresholdMin'
	public void setPThresholdMin(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_MIN_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getPThresholdMin()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_MIN_PROPERTY_NAME));
	}
	
	public String PThresholdMinTipText()
	{
		return "Minimal possible value while learning the optimal PThreshold (used only if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'pThresholdMax'
	public void setPThresholdMax(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_MAX_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getPThresholdMax()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_MAX_PROPERTY_NAME));
	}
	
	public String PThresholdMaxTipText()
	{
		return "Maximal possible value while learning the optimal PThreshold (used only if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'pThresholdStep'
	public void setPThresholdStep(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_STEP_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getPThresholdStep()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.P_VALUE_THRESHOLD_STEP_PROPERTY_NAME));
	}
	
	public String PThresholdStepTipText()
	{
		return "Density of values between PThresholdMin and PThresholdMax considered while learning the optimal PThreshold (used only if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'sMinority'
	public void setSMinority(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMinority()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_VALUE_PROPERTY_NAME));
	}
	
	public String SMinorityTipText()
	{
		return "Consistency level for minority class, set automatically if learnOptimalParameters = TRUE";
	}

	// methods required for option 'sMinorityMin'
	public void setSMinorityMin(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_MIN_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMinorityMin()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_MIN_VALUE_PROPERTY_NAME));
	}
	
	public String SMinorityMinTipText()
	{
		return "Minimal possible value while learning the optimal SMinority value (used only if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'sMinorityMax'
	public void setSMinorityMax(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_MAX_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMinorityMax()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_MAX_VALUE_PROPERTY_NAME));
	}
	
	public String SMinorityMaxTipText()
	{
		return "Maximal possible value while learning the optimal SMinority value (used only if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'sMinorityStep'
	public void setSMinorityStep(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_STEP_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMinorityStep()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MINORITY_STEP_VALUE_PROPERTY_NAME));
	}
	
	public String SMinorityStepTipText()
	{
		return "Density of values between SMinorityMin and SMinorityMax considered while learning the optimal SMinority value (used only if learnOptimalParameters = TRUE)";
	}

	// methods required for option 'sMajority'
	public void setSMajority(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMajority()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_VALUE_PROPERTY_NAME));
	}
	
	public String SMajorityTipText()
	{
		return "Consistency level for majority class, set automatically if learnOptimalParameters = TRUE";
	}

	// methods required for option 'sMajorityMin'
	public void setSMajorityMin(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_MIN_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMajorityMin()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_MIN_VALUE_PROPERTY_NAME));
	}
	
	public String SMajorityMinTipText()
	{
		return "Minimal possible value while learning the optimal SMajority value (used only if learnOptimalParameters = TRUE and optimisation4D = TRUE)";
	}

	// methods required for option 'sMajorityMax'
	public void setSMajorityMax(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_MAX_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMajorityMax()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_MAX_VALUE_PROPERTY_NAME));
	}
	
	public String SMajorityMaxTipText()
	{
		return "Maximal possible value while learning the optimal SMajority value (used only if learnOptimalParameters = TRUE and optimisation4D = TRUE)";
	}

	// methods required for option 'sMajorityStep'
	public void setSMajorityStep(double value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_STEP_VALUE_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getSMajorityStep()
	{
		return Double.parseDouble(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_STEP_VALUE_PROPERTY_NAME));
	}
	
	public String SMajorityStepTipText()
	{
		return "Density of values between SMajorityMin and SMajorityMax considered while learning the optimal SMinority value (used only if learnOptimalParameters = TRUE and optimisation4D = TRUE)";
	}

	// methods required for option 'optimisationMeasure'
	String enumarateOptimisationMeasureToString()
	{
		StringBuilder sb = new StringBuilder();
		for (AbstractParameterised3DClassifier.OptimisationMeasure measure : AbstractParameterised3DClassifier.OptimisationMeasure.values())
			sb.append("\t\t"+measure.ordinal()+"="+measure.name()+"\n");
		return sb.toString();
	}

	int getOptimisationMeasureOrdinal()
	{
		return AbstractParameterised3DClassifier.OptimisationMeasure.valueOf(getProperties().getProperty(AbstractParameterised3DClassifier.OPTIMISATION_MEASURE_PROPERTY_NAME)).ordinal();
	}

	public void setOptimisationMeasure(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_OPTIMISATION_MEASURE) {
	    	AbstractParameterised3DClassifier.OptimisationMeasure value = AbstractParameterised3DClassifier.OptimisationMeasure.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(AbstractParameterised3DClassifier.OPTIMISATION_MEASURE_PROPERTY_NAME, value.name());
	    }
	}

	public SelectedTag getOptimisationMeasure()
	{
		return new SelectedTag(getOptimisationMeasureOrdinal(), TAGS_OPTIMISATION_MEASURE);
	}

	public String optimisationMeasureTipText()
	{
	    return "Measure for optimization of parameters";
	}
	
	// methods required for option 'filterNeighboursUsingRules'
	public void setFilterNeighboursUsingRules(boolean value)
	{
		getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.FILTER_NEIGHBOURS_PROPERTY_NAME, String.valueOf(value));
	}
	
	public boolean getFilterNeighboursUsingRules()
	{
		return Boolean.parseBoolean(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.FILTER_NEIGHBOURS_PROPERTY_NAME));
	}
	
	public String filterNeighboursUsingRulesTipText()
	{
		return "Whether nearest neighbours are filtered by rules";
	}

	// methods required for option 'voting'
	String enumarateVotingToString()
	{
		StringBuilder sb = new StringBuilder();
		for (rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting voting : rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.values())
			sb.append("\t\t"+voting.ordinal()+"="+voting.name()+"\n");
		return sb.toString();
	}

	int getVotingOrdinal()
	{
		return rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.valueOf(getProperties().getProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.VOTING_PROPERTY_NAME)).ordinal();
	}

	public void setVoting(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_VOTING) {
	    	rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting value = rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.VOTING_PROPERTY_NAME, value.name());
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

	    result.addElement(new Option("\tUse the majority decision in a training data set as the minority decision.",
			      "F", 0, "-F"));

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
						+ "\t(default: "+MetricFactory.Weighting.None.ordinal()+"="+MetricFactory.Weighting.None.name()+")",
						"W", 1, "-W"));

	    result.addElement(new Option("\tDo not use indexing tree (linear search for nearest neighbours is used).",
			      "L", 0, "-L"));

	    result.addElement(new Option("\tDo not optimize the values of parameters automatically.",
			      "U", 0, "-U"));

	    result.addElement(new Option("\tUse 4-dimensional optimisation including the consistency level for majority class.",
			      "E", 0, "-E"));

		result.addElement(new Option(
				"\tNumber of nearest neighbours if not set automatically.\n"
						+ "\t(default: 1)",
						"K", 1, "-K <number of neighbours>"));

		result.addElement(new Option(
				"\tMaximum number of neighbours while optimizing automatically.\n"
						+ "\t(default: 100)",
						"N", 1, "-N <maximum number>"));

		result.addElement(new Option(
				"\tIt assigns minority class if weight of minority class divided by sum of weights is greater than or equal to this threshold and majority class otherwise.\n"
						+ "\t(default: 0.39)",
						"P", 1, "-P <threshold>"));

		result.addElement(new Option(
				"\tMinimum possible P threshold value while optimizing automatically.\n"
						+ "\t(default: 0.0)",
						"PMin", 1, "-PMin <minimum value>"));

		result.addElement(new Option(
				"\tMaximum possible P threshold value while optimizing automatically.\n"
						+ "\t(default: 0.5)",
						"PMax", 1, "-PMax <maximum value>"));

		result.addElement(new Option(
				"\tDensity of tested P threshold values while optimizing automatically.\n"
						+ "\t(default: 0.01)",
						"PStep", 1, "-PStep <step>"));

		result.addElement(new Option(
				"\tConsistency level for minority class.\n"
						+ "\t(default: 1.0)",
						"SMinor", 1, "-SMinor <consistency level>"));

		result.addElement(new Option(
				"\tMinimum consistency level for minority class while optimizing automatically.\n"
						+ "\t(default: 0.0)",
						"SMinorMin", 1, "-SMinorMin <minimum value>"));

		result.addElement(new Option(
				"\tMaximum consistency level for minority class while optimizing automatically.\n"
						+ "\t(default: 1.0)",
						"SMinorMax", 1, "-SMinorMax <maximum value>"));

		result.addElement(new Option(
				"\tDensity of tested consistency levels for minority class while optimizing automatically.\n"
						+ "\t(default: 0.1)",
						"SMinorStep", 1, "-SMinorStep <step>"));

		result.addElement(new Option(
				"\tConsistency level for majority class.\n"
						+ "\t(default: 1.0)",
						"SMajor", 1, "-SMajor <consistency level>"));

		result.addElement(new Option(
				"\tMinimum consistency level for majority class while optimizing automatically.\n"
						+ "\t(default: 0.0)",
						"SMajorMin", 1, "-SMajorMin <minimum value>"));

		result.addElement(new Option(
				"\tMaximum consistency level for majority class while optimizing automatically.\n"
						+ "\t(default: 1.0)",
						"SMajorMax", 1, "-SMajorMax <maximum value>"));

		result.addElement(new Option(
				"\tDensity of tested consistency levels for majority class while optimizing automatically.\n"
						+ "\t(default: 0.1)",
						"SMajorStep", 1, "-SMajorStep <step>"));

		result.addElement(new Option(
				"\tMeasure for optimization of parameters:\n"
						+ enumarateOptimisationMeasureToString()
						+ "\t(default: "+AbstractParameterised3DClassifier.OptimisationMeasure.Gmean.ordinal()+"="+AbstractParameterised3DClassifier.OptimisationMeasure.Gmean.name()+")",
						"O", 1, "-O"));

	    result.addElement(new Option("\tDo not use rules to filter nearest neighbours.",
			      "A", 0, "-A"));

		result.addElement(new Option(
				"\tVoting method:\n"
						+ enumarateVotingToString()
						+ "\t(default: "+rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.Equal.ordinal()+"="+rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.Voting.Equal.name()+")",
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
		
		setUseMajorityDecAsMinorityDec(Utils.getFlag('F', options));
		
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
	    setLearnOptimalParameters(learn);
	    
	    if (learn) {
		    boolean optimisation4D = Utils.getFlag('E', options);
		    setOptimisation4D(optimisation4D);
			tmpStr = Utils.getOption('K', options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting K makes sense only if automatic optimization is not used");
			tmpStr = Utils.getOption('N', options);
			if (tmpStr.length() != 0)
				setMaxK(Integer.parseInt(tmpStr));
			tmpStr = Utils.getOption('P', options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting P threshold makes sense only if automatic optimization is not used");
			tmpStr = Utils.getOption("PMin", options);
			if (tmpStr.length() != 0)
				setPThresholdMin(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("PMax", options);
			if (tmpStr.length() != 0)
				setPThresholdMax(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("PStep", options);
			if (tmpStr.length() != 0)
				setPThresholdStep(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("SMinor", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMinor value makes sense only if automatic optimization is not used");
			tmpStr = Utils.getOption("SMinorMin", options);
			if (tmpStr.length() != 0)
				setSMinorityMin(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("SMinorMax", options);
			if (tmpStr.length() != 0)
				setSMinorityMax(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("SMinorStep", options);
			if (tmpStr.length() != 0)
				setSMinorityStep(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("SMajor", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMajor value makes sense only if automatic optimization is not used");
			if(optimisation4D) {
				tmpStr = Utils.getOption("SMajorMin", options);
				if (tmpStr.length() != 0)
					setSMajorityMin(Double.parseDouble(tmpStr));
				tmpStr = Utils.getOption("SMajorMax", options);
				if (tmpStr.length() != 0)
					setSMajorityMax(Double.parseDouble(tmpStr));
				tmpStr = Utils.getOption("SMajorStep", options);
				if (tmpStr.length() != 0)
					setSMajorityStep(Double.parseDouble(tmpStr));
			} else {
				tmpStr = Utils.getOption("SMajorMin", options);
				if (tmpStr.length() != 0)
					throw new Exception("Setting SMajorMin value makes sense only if 4-dimensional optimization is used");
				tmpStr = Utils.getOption("SMajorMax", options);
				if (tmpStr.length() != 0)
					throw new Exception("Setting SMajorMax value makes sense only if 4-dimensional optimization is used");
				tmpStr = Utils.getOption("SMajorStep", options);
				if (tmpStr.length() != 0)
					throw new Exception("Setting SMajorStep value makes sense only if 4-dimensional optimization is used");
			}
	    } else {
			if (Utils.getFlag('E', options))
				throw new Exception("Setting 4-dimensional optimisation makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption('K', options);
			if (tmpStr.length() != 0)
				setK(Integer.parseInt(tmpStr));
			tmpStr = Utils.getOption('N', options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting maximum number of neighbours makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption('P', options);
			if (tmpStr.length() != 0)
				setPThreshold(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("PMin", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting PMin value makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption("PMax", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting PMax value makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption("PStep", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting PStep value makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption("SMinor", options);
			if (tmpStr.length() != 0)
				setSMinority(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("SMinorMin", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMinorMin value makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption("SMinorMax", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMinorMax value makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption("SMinorStep", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMinorStep value makes sense only if automatic optimization is used");
			tmpStr = Utils.getOption("SMajor", options);
			if (tmpStr.length() != 0)
				setSMajority(Double.parseDouble(tmpStr));
			tmpStr = Utils.getOption("SMajorMin", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMajorMin value makes sense only if automatic 4-dimensional optimization is used");
			tmpStr = Utils.getOption("SMajorMax", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMajorMax value makes sense only if automatic 4-dimensional optimization is used");
			tmpStr = Utils.getOption("SMajorStep", options);
			if (tmpStr.length() != 0)
				throw new Exception("Setting SMajorStep value makes sense only if automatic 4-dimensional optimization is used");
	    }

		tmpStr = Utils.getOption('O', options);
		if (tmpStr.length() != 0)
			setOptimisationMeasure(new SelectedTag(Integer.parseInt(tmpStr), TAGS_OPTIMISATION_MEASURE));

	    setFilterNeighboursUsingRules(!Utils.getFlag('A', options));

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
		
		if(getUseMajorityDecAsMinorityDec())
			result.add("-F");

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

		if(getLearnOptimalParameters()) {
			result.add("-N");
			result.add("" + getMaxK());
			result.add("-PMin");
			result.add("" + getPThresholdMin());
			result.add("-PMax");
			result.add("" + getPThresholdMax());
			result.add("-PStep");
			result.add("" + getPThresholdStep());
			result.add("-SMinorMin");
			result.add("" + getSMinorityMin());
			result.add("-SMinorMax");
			result.add("" + getSMinorityMax());
			result.add("-SMinorStep");
			result.add("" + getSMinorityStep());
			if(getOptimisation4D()) {
				result.add("-E");
				result.add("-SMajorMin");
				result.add("" + getSMajorityMin());
				result.add("-SMajorMax");
				result.add("" + getSMajorityMax());
				result.add("-SMajorStep");
				result.add("" + getSMajorityStep());
			}
		} else {
			result.add("-U");
			result.add("-K");
			result.add("" + getK());
			result.add("-P");
			result.add("" + getPThreshold());
			result.add("-SMinor");
			result.add("" + getSMinority());
			result.add("-SMajor");
			result.add("" + getSMajority());
		}

		result.add("-O");
		result.add("" + getOptimisationMeasureOrdinal());

		if(!getFilterNeighboursUsingRules())
			result.add("-A");

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
		runClassifier(new RIONIDA(), args);
	}
}

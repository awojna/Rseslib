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


package weka.classifiers.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;


import rseslib.processing.classification.rules.roughset.RoughSetRules;
import rseslib.processing.discernibility.DiscernibilityMatrixProvider;
import rseslib.processing.discretization.DiscretizationFactory;
import rseslib.processing.reducts.PartialReductsProvider;
import rseslib.processing.rules.ReductRuleGenerator;
import rseslib.structure.rule.Rule;
import weka.classifiers.AbstractRseslibClassifierWrapper;
import weka.core.Option;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

/**
 * Weka wrapper for rough set classifier.
 * @see rseslib.processing.classification.rules.roughset.RoughSetRules
 *
 * @author      Arkadiusz Wojna
 */
public class RoughSet extends AbstractRseslibClassifierWrapper
{
	/** for serialization */
	static final long serialVersionUID = 1L;

	/** Tags for 'Discretization' option */
	private static final Tag[] TAGS_DISCRETIZATION = {
			new Tag(DiscretizationFactory.DiscretizationType.None.ordinal(), DiscretizationFactory.DiscretizationType.None.name()),
			new Tag(DiscretizationFactory.DiscretizationType.EqualWidth.ordinal(), DiscretizationFactory.DiscretizationType.EqualWidth.name()),
			new Tag(DiscretizationFactory.DiscretizationType.EqualFrequency.ordinal(), DiscretizationFactory.DiscretizationType.EqualFrequency.name()),
			new Tag(DiscretizationFactory.DiscretizationType.OneRule.ordinal(), DiscretizationFactory.DiscretizationType.OneRule.name()),
			new Tag(DiscretizationFactory.DiscretizationType.EntropyMinimizationStatic.ordinal(), DiscretizationFactory.DiscretizationType.EntropyMinimizationStatic.name()),
			new Tag(DiscretizationFactory.DiscretizationType.EntropyMinimizationDynamic.ordinal(), DiscretizationFactory.DiscretizationType.EntropyMinimizationDynamic.name()),
			new Tag(DiscretizationFactory.DiscretizationType.ChiMerge.ordinal(), DiscretizationFactory.DiscretizationType.ChiMerge.name()),
			new Tag(DiscretizationFactory.DiscretizationType.MaximalDiscernibilityHeuristicGlobal.ordinal(), DiscretizationFactory.DiscretizationType.MaximalDiscernibilityHeuristicGlobal.name()),
			new Tag(DiscretizationFactory.DiscretizationType.MaximalDiscernibilityHeuristicLocal.ordinal(), DiscretizationFactory.DiscretizationType.MaximalDiscernibilityHeuristicLocal.name()),
	};

	/** Tags for 'Reducts' option */
	private static final Tag[] TAGS_REDUCTS = {
			new Tag(ReductRuleGenerator.ReductsMethod.AllLocal.ordinal(), ReductRuleGenerator.ReductsMethod.AllLocal.name()),
			new Tag(ReductRuleGenerator.ReductsMethod.AllGlobal.ordinal(), ReductRuleGenerator.ReductsMethod.AllGlobal.name()),
			new Tag(ReductRuleGenerator.ReductsMethod.OneJohnson.ordinal(), ReductRuleGenerator.ReductsMethod.OneJohnson.name()),
			new Tag(ReductRuleGenerator.ReductsMethod.AllJohnson.ordinal(), ReductRuleGenerator.ReductsMethod.AllJohnson.name()),
			new Tag(ReductRuleGenerator.ReductsMethod.PartialLocal.ordinal(), ReductRuleGenerator.ReductsMethod.PartialLocal.name()),
			new Tag(ReductRuleGenerator.ReductsMethod.PartialGlobal.ordinal(), ReductRuleGenerator.ReductsMethod.PartialGlobal.name()),
	};

	/** Tags for 'IndiscernibilityForMissing' option */
	private static final Tag[] TAGS_INDISCERNIBILITY_FOR_MISSING = {
			new Tag(DiscernibilityMatrixProvider.IndiscernibilityRelation.DiscernFromValue.ordinal(), DiscernibilityMatrixProvider.IndiscernibilityRelation.DiscernFromValue.name()),
			new Tag(DiscernibilityMatrixProvider.IndiscernibilityRelation.DiscernFromValueOneWay.ordinal(), DiscernibilityMatrixProvider.IndiscernibilityRelation.DiscernFromValueOneWay.name()),
			new Tag(DiscernibilityMatrixProvider.IndiscernibilityRelation.DontDiscernFromValue.ordinal(), DiscernibilityMatrixProvider.IndiscernibilityRelation.DontDiscernFromValue.name()),
	};

	/** Tags for 'DiscernibilityMethod' option */
	private static final Tag[] TAGS_DISCERNIBILITY = {
			new Tag(DiscernibilityMatrixProvider.DiscernibilityMethod.All.ordinal(), DiscernibilityMatrixProvider.DiscernibilityMethod.All.name()),
			new Tag(DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecision.ordinal(), DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecision.name()),
			new Tag(DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecisionAndOrdinaryChecked.ordinal(), DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecisionAndOrdinaryChecked.name()),
			new Tag(DiscernibilityMatrixProvider.DiscernibilityMethod.OrdinaryDecisionAndInconsistenciesOmitted.ordinal(), DiscernibilityMatrixProvider.DiscernibilityMethod.OrdinaryDecisionAndInconsistenciesOmitted.name()),
	};

	public RoughSet() throws Exception
	{
		super(RoughSetRules.class);
	}
	
	/**
	 * Returns a string describing classifier
	 * @return a description suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {

		return  "Classifier with rule induction based on rough sets. "
				+ "For more information see\n\n"
				+ getTechnicalInformation().toString();
	}

	// methods required for option 'Discretization'
	String enumarateDiscretizationToString()
	{
		StringBuilder sb = new StringBuilder();
		for (DiscretizationFactory.DiscretizationType discretizationType : DiscretizationFactory.DiscretizationType.values())
			sb.append("\t\t"+discretizationType.ordinal()+"="+discretizationType.name()+"\n");
		return sb.toString();
	}
	
	int getDiscretizationOrdinal()
	{
		return DiscretizationFactory.DiscretizationType.valueOf(getProperties().getProperty(DiscretizationFactory.DISCRETIZATION_PROPERTY_NAME)).ordinal();
	}
	
	public void setDiscretization(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_DISCRETIZATION) {
	    	DiscretizationFactory.DiscretizationType value = DiscretizationFactory.DiscretizationType.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(DiscretizationFactory.DISCRETIZATION_PROPERTY_NAME, value.name());
	    }
	}

	public SelectedTag getDiscretization()
	{
		return new SelectedTag(getDiscretizationOrdinal(), TAGS_DISCRETIZATION);
	}

	public String discretizationTipText()
	{
	    return "Type of discretization used to discretize numerical attributes";
	}

	// methods required for option 'DiscrNumberOfIntervals'
	public void setDiscrNumberOfIntervals(int value)
	{
		getProperties().setProperty(DiscretizationFactory.NUMBER_OF_INTERVALS_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getDiscrNumberOfIntervals()
	{
		return Integer.parseInt(getProperties().getProperty(DiscretizationFactory.NUMBER_OF_INTERVALS_PROPERTY_NAME));
	}
	
	public String discrNumberOfIntervalsTipText()
	{
		return "Number of intervals for each attribute (used only if Discretization is EqualWidth or EqualFrequency)";
	}

	// methods required for option 'DiscrMinimalFrequency'
	public void setDiscrMinimalFrequency(int value)
	{
		getProperties().setProperty(DiscretizationFactory.MIN_FREQUENCY_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getDiscrMinimalFrequency()
	{
		return Integer.parseInt(getProperties().getProperty(DiscretizationFactory.MIN_FREQUENCY_PROPERTY_NAME));
	}
	
	public String discrMinimalFrequencyTipText()
	{
		return "Minimal frequency of decision class in each interval (used only if Discretization = OneRule)";
	}

	// methods required for option 'DiscrConfidenceLevelForIntervalDifference'
	public void setDiscrConfidenceLevelForIntervalDifference(double value)
	{
		getProperties().setProperty(DiscretizationFactory.CONFIDENCE_LEVEL_PROPERTY_NAME, String.valueOf(value));
	}
	
	public double getDiscrConfidenceLevelForIntervalDifference()
	{
		return Double.parseDouble(getProperties().getProperty(DiscretizationFactory.CONFIDENCE_LEVEL_PROPERTY_NAME));
	}
	
	public String discrConfidenceLevelForIntervalDifferenceTipText()
	{
		return "Confidence level required to consider two neighbouring intervals as different and not to merge them (used only if Discretization = ChiMerge)";
	}

	// methods required for option 'DiscrMinimalFrequency'
	public void setDiscrMinimalNumberOfIntervals(int value)
	{
		getProperties().setProperty(DiscretizationFactory.MIN_INTERVALS_PROPERTY_NAME, String.valueOf(value));
	}
	
	public int getDiscrMinimalNumberOfIntervals()
	{
		return Integer.parseInt(getProperties().getProperty(DiscretizationFactory.MIN_INTERVALS_PROPERTY_NAME));
	}
	
	public String discrMinimalNumberOfIntervalsTipText()
	{
		return "Minimal number of intervals for each attribute (used only if Discretization = ChiMerge)";
	}

	// methods required for option 'Reducts'
	String enumarateReductsToString()
	{
		StringBuilder sb = new StringBuilder();
		for (ReductRuleGenerator.ReductsMethod reductsType : ReductRuleGenerator.ReductsMethod.values())
			sb.append("\t\t"+reductsType.ordinal()+"="+reductsType.name()+"\n");
		return sb.toString();
	}
	
	int getReductsOrdinal()
	{
		return ReductRuleGenerator.ReductsMethod.valueOf(getProperties().getProperty(ReductRuleGenerator.s_sReductsMethod)).ordinal();
	}
	
	public void setReducts(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_REDUCTS) {
	    	ReductRuleGenerator.ReductsMethod value = ReductRuleGenerator.ReductsMethod.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(ReductRuleGenerator.s_sReductsMethod, value.name());
	    }
	}

	public SelectedTag getReducts()
	{
		return new SelectedTag(getReductsOrdinal(), TAGS_REDUCTS);
	}

	public String reductsTipText()
	{
	    return "Reducts generating method";
	}

	// methods required for option 'IndiscernibilityForMissing'
	String enumarateIndiscernibilityForMissingToString()
	{
		StringBuilder sb = new StringBuilder();
		for (DiscernibilityMatrixProvider.IndiscernibilityRelation indiscerniblity : DiscernibilityMatrixProvider.IndiscernibilityRelation.values())
			sb.append("\t\t"+indiscerniblity.ordinal()+"="+indiscerniblity.name()+"\n");
		return sb.toString();
	}
	
	int getIndiscernibilityForMissingOrdinal()
	{
		return DiscernibilityMatrixProvider.IndiscernibilityRelation.valueOf(getProperties().getProperty(DiscernibilityMatrixProvider.s_sIndiscernibilityRelation)).ordinal();
	}
	
	public void setIndiscernibilityForMissing(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_INDISCERNIBILITY_FOR_MISSING) {
	    	DiscernibilityMatrixProvider.IndiscernibilityRelation value = DiscernibilityMatrixProvider.IndiscernibilityRelation.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(DiscernibilityMatrixProvider.s_sIndiscernibilityRelation, value.name());
	    }
	}

	public SelectedTag getIndiscernibilityForMissing()
	{
		return new SelectedTag(getIndiscernibilityForMissingOrdinal(), TAGS_INDISCERNIBILITY_FOR_MISSING);
	}

	public String indiscernibilityForMissingTipText()
	{
	    return "Method of discerning missing values from others";
	}

	// methods required for option 'DiscernibilityMethod'
	String enumarateDiscernibilityMethodToString()
	{
		StringBuilder sb = new StringBuilder();
		for (DiscernibilityMatrixProvider.DiscernibilityMethod discernibility : DiscernibilityMatrixProvider.DiscernibilityMethod.values())
			sb.append("\t\t"+discernibility.ordinal()+"="+discernibility.name()+"\n");
		return sb.toString();
	}
	
	int getDiscernibilityMethodOrdinal()
	{
		return DiscernibilityMatrixProvider.DiscernibilityMethod.valueOf(getProperties().getProperty(DiscernibilityMatrixProvider.s_sDiscernibilityMethod)).ordinal();
	}
	
	public void setDiscernibilityMethod(SelectedTag newType)
	{
	    if (newType.getTags() == TAGS_DISCERNIBILITY) {
	    	DiscernibilityMatrixProvider.DiscernibilityMethod value = DiscernibilityMatrixProvider.DiscernibilityMethod.values()[newType.getSelectedTag().getID()];
	    	getProperties().setProperty(DiscernibilityMatrixProvider.s_sDiscernibilityMethod, value.name());
	    }
	}

	public SelectedTag getDiscernibilityMethod()
	{
		return new SelectedTag(getDiscernibilityMethodOrdinal(), TAGS_DISCERNIBILITY);
	}

	public String discernibilityMethodTipText()
	{
	    return "Discernibility matrix build method, defines what is discerned";
	}

	// methods required for option 'GeneralizedDecisionTransitiveClosure'
	public void setGeneralizedDecisionTransitiveClosure(boolean value)
	{
		getProperties().setProperty(DiscernibilityMatrixProvider.s_sGeneralizedDecisionTransitiveClosure, String.valueOf(value));
	}
	
	public boolean getGeneralizedDecisionTransitiveClosure()
	{
		return Boolean.parseBoolean(getProperties().getProperty(DiscernibilityMatrixProvider.s_sGeneralizedDecisionTransitiveClosure));
	}
	
	public String generalizedDecisionTransitiveClosureTipText()
	{
		return "Whether the generalized decision is transitively closed before it is used to build discernibility matrix (used only if DiscernibilityMethod is one of GeneralizedDecision or GeneralizedDecisionAndOrdinaryChecked)";
	}

	// methods required for option 'AlphaForPartialReducts'
	public void setAlphaForPartialReducts(double value)
	{
		getProperties().setProperty(PartialReductsProvider.s_sAlpha, String.valueOf(value));
	}
	
	public double getAlphaForPartialReducts()
	{
		return Double.parseDouble(getProperties().getProperty(PartialReductsProvider.s_sAlpha));
	}
	
	public String alphaForPartialReductsTipText()
	{
		return "Alpha value in alpha-cover of partial reducts (0 <= AlphaForPartialReducts < 1), used only if Reducts set to PartialLocal or PartialGlobal";
	}

	// methods required for option 'MissingValueDescriptorsInRules'
	public void setMissingValueDescriptorsInRules(boolean value)
	{
		getProperties().setProperty(ReductRuleGenerator.s_sAllowComparingMissingValues, String.valueOf(value));
	}
	
	public boolean getMissingValueDescriptorsInRules()
	{
		return Boolean.parseBoolean(getProperties().getProperty(ReductRuleGenerator.s_sAllowComparingMissingValues));
	}
	
	public String missingValueDescriptorsInRulesTipText()
	{
		return "Whether descriptors with missing values are enabled in rules";
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
				"\tDiscretization method:\n"
						+ enumarateDiscretizationToString()
						+ "\t(default: "+DiscretizationFactory.DiscretizationType.MaximalDiscernibilityHeuristicLocal.ordinal()+"="+DiscretizationFactory.DiscretizationType.MaximalDiscernibilityHeuristicLocal.name()+")",
						"D", 1, "-D"));
		
		result.addElement(new Option(
				"\tNumber of intervals ("+DiscretizationFactory.DiscretizationType.EqualWidth+" and "+DiscretizationFactory.DiscretizationType.EqualFrequency+") or minimal number of intervals ("+DiscretizationFactory.DiscretizationType.ChiMerge+") for each attribute.\n"
						+ "\t(default: 5 for "+DiscretizationFactory.DiscretizationType.EqualWidth+" and "+DiscretizationFactory.DiscretizationType.EqualFrequency+", 3 for "+DiscretizationFactory.DiscretizationType.ChiMerge+")",
						"N", 1, "-N <number of intervals>"));

		result.addElement(new Option(
				"\tConfidence level required to consider two neighbouring intervals as different and not to merge them in "+DiscretizationFactory.DiscretizationType.ChiMerge+" discretization.\n"
						+ "\t(default: 0.9)",
						"C", 1, "-C <confidence level>"));

		result.addElement(new Option(
				"\tMinimal frequency of decision class in each interval in "+DiscretizationFactory.DiscretizationType.OneRule+" discretization.\n"
						+ "\t(default: 6)",
						"F", 1, "-F <minimal frequency>"));

		result.addElement(new Option(
				"\tReducts generating method:\n"
						+ enumarateReductsToString()
						+ "\t(default: "+ReductRuleGenerator.ReductsMethod.AllLocal.ordinal()+"="+ReductRuleGenerator.ReductsMethod.AllLocal.name()+")",
						"R", 1, "-R"));
		
		result.addElement(new Option(
				"\tMissing value discerning:\n"
						+ enumarateIndiscernibilityForMissingToString()
						+ "\t(default: "+DiscernibilityMatrixProvider.IndiscernibilityRelation.DiscernFromValue.ordinal()+"="+DiscernibilityMatrixProvider.IndiscernibilityRelation.DiscernFromValue.name()+")",
						"I", 1, "-I"));

		result.addElement(new Option(
				"\tDiscernibility matrix build method:\n"
						+ enumarateDiscernibilityMethodToString()
						+ "\t(default: "+DiscernibilityMatrixProvider.DiscernibilityMethod.OrdinaryDecisionAndInconsistenciesOmitted.ordinal()+"="+DiscernibilityMatrixProvider.DiscernibilityMethod.OrdinaryDecisionAndInconsistenciesOmitted.name()+")",
						"X", 1, "-X"));

	    result.addElement(new Option("\tGeneralized decision is transitively closed before it is used to build discernibility matrix.",
			      "T", 0, "-T"));

		result.addElement(new Option(
				"\tAlpha value in alpha-cover of partial reducts.\n"
						+ "\t(default: 0.5)",
						"A", 1, "-A <alpha value>"));

	    result.addElement(new Option("\tDisable missing values in descriptors of rules.",
			      "M", 0, "-M"));

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
		
		tmpStr = Utils.getOption('D', options);
		if (tmpStr.length() != 0)
			setDiscretization(new SelectedTag(Integer.parseInt(tmpStr), TAGS_DISCRETIZATION));
		
		tmpStr = Utils.getOption('N', options);
		if (tmpStr.length() != 0) {
			String discrName = getProperties().getProperty(DiscretizationFactory.DISCRETIZATION_PROPERTY_NAME);
			DiscretizationFactory.DiscretizationType discrType = DiscretizationFactory.DiscretizationType.valueOf(discrName);
			if (discrType != DiscretizationFactory.DiscretizationType.EqualWidth && discrType != DiscretizationFactory.DiscretizationType.EqualFrequency && discrType != DiscretizationFactory.DiscretizationType.ChiMerge)
				throw new Exception("Number of intervals makes sense only for discretization " + DiscretizationFactory.DiscretizationType.EqualWidth + ", " + DiscretizationFactory.DiscretizationType.EqualFrequency + " or " + DiscretizationFactory.DiscretizationType.ChiMerge);
			int value = Integer.parseInt(tmpStr);
			setDiscrNumberOfIntervals(value);
			setDiscrMinimalNumberOfIntervals(value);
		}

		tmpStr = Utils.getOption('F', options);
		if (tmpStr.length() != 0) {
			String discrName = getProperties().getProperty(DiscretizationFactory.DISCRETIZATION_PROPERTY_NAME);
			DiscretizationFactory.DiscretizationType discrType = DiscretizationFactory.DiscretizationType.valueOf(discrName);
			if (discrType != DiscretizationFactory.DiscretizationType.OneRule)
				throw new Exception("Minimal frequency of decision class makes sense only for discretization " + DiscretizationFactory.DiscretizationType.OneRule);
			setDiscrMinimalFrequency(Integer.parseInt(tmpStr));
		}

		tmpStr = Utils.getOption('C', options);
		if (tmpStr.length() != 0) {
			String discrName = getProperties().getProperty(DiscretizationFactory.DISCRETIZATION_PROPERTY_NAME);
			DiscretizationFactory.DiscretizationType discrType = DiscretizationFactory.DiscretizationType.valueOf(discrName);
			if (discrType != DiscretizationFactory.DiscretizationType.ChiMerge)
				throw new Exception("Confidence level makes sense only for discretization " + DiscretizationFactory.DiscretizationType.ChiMerge);
			setDiscrConfidenceLevelForIntervalDifference(Double.parseDouble(tmpStr));
		}
		
		tmpStr = Utils.getOption('R', options);
		if (tmpStr.length() != 0)
			setReducts(new SelectedTag(Integer.parseInt(tmpStr), TAGS_REDUCTS));
		
		tmpStr = Utils.getOption('I', options);
		if (tmpStr.length() != 0)
			setIndiscernibilityForMissing(new SelectedTag(Integer.parseInt(tmpStr), TAGS_INDISCERNIBILITY_FOR_MISSING));

		tmpStr = Utils.getOption('X', options);
		if (tmpStr.length() != 0)
			setDiscernibilityMethod(new SelectedTag(Integer.parseInt(tmpStr), TAGS_DISCERNIBILITY));

	    boolean transClosure = Utils.getFlag('T', options);
		if (transClosure) {
			String discernName = getProperties().getProperty(DiscernibilityMatrixProvider.s_sDiscernibilityMethod);
			DiscernibilityMatrixProvider.DiscernibilityMethod discernibility = DiscernibilityMatrixProvider.DiscernibilityMethod.valueOf(discernName);
			if (discernibility != DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecision && discernibility != DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecisionAndOrdinaryChecked)
				throw new Exception("Transitive closure makes sense only for discernibility methods: " + DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecision + " and " + DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecisionAndOrdinaryChecked);
		}
		setGeneralizedDecisionTransitiveClosure(transClosure);

		tmpStr = Utils.getOption('A', options);
		if (tmpStr.length() != 0) {
			String reductsName = getProperties().getProperty(ReductRuleGenerator.s_sReductsMethod);
			ReductRuleGenerator.ReductsMethod reductsType = ReductRuleGenerator.ReductsMethod.valueOf(reductsName);
			if (reductsType != ReductRuleGenerator.ReductsMethod.PartialGlobal && reductsType != ReductRuleGenerator.ReductsMethod.PartialLocal)
				throw new Exception("Alpha value makes sense only for " + ReductRuleGenerator.ReductsMethod.PartialGlobal + " and " + ReductRuleGenerator.ReductsMethod.PartialLocal + " reducts");
			setAlphaForPartialReducts(Double.parseDouble(tmpStr));
		}

	    setMissingValueDescriptorsInRules(!Utils.getFlag('M', options));

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

		int discrOrdinal = getDiscretizationOrdinal();

		result.add("-D");
		result.add("" + discrOrdinal);
		
		if (discrOrdinal == DiscretizationFactory.DiscretizationType.EqualWidth.ordinal() || discrOrdinal == DiscretizationFactory.DiscretizationType.EqualFrequency.ordinal())
		{
			result.add("-N");
			result.add("" + getDiscrNumberOfIntervals());
		}
		else if (discrOrdinal == DiscretizationFactory.DiscretizationType.OneRule.ordinal())
		{
			result.add("-F");
			result.add("" + getDiscrMinimalFrequency());
		}
		else if (discrOrdinal == DiscretizationFactory.DiscretizationType.ChiMerge.ordinal())
		{
			result.add("-N");
			result.add("" + getDiscrMinimalNumberOfIntervals());
			result.add("-C");
			result.add("" + getDiscrConfidenceLevelForIntervalDifference());
		}

		result.add("-R");
		result.add("" + getReductsOrdinal());

		result.add("-I");
		result.add("" + getIndiscernibilityForMissingOrdinal());

		result.add("-X");
		result.add("" + getDiscernibilityMethodOrdinal());

		if (getDiscernibilityMethodOrdinal() == DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecision.ordinal()
				|| getDiscernibilityMethodOrdinal() == DiscernibilityMatrixProvider.DiscernibilityMethod.GeneralizedDecisionAndOrdinaryChecked.ordinal())
		{
			if (getGeneralizedDecisionTransitiveClosure())
				result.add("-T");
		}
		
		if(getReductsOrdinal() == ReductRuleGenerator.ReductsMethod.PartialGlobal.ordinal() || getReductsOrdinal() == ReductRuleGenerator.ReductsMethod.PartialLocal.ordinal())
		{
			result.add("-A");
			result.add("" + getAlphaForPartialReducts());
		}

		if(!getMissingValueDescriptorsInRules())
			result.add("-M");

		result.addAll(Arrays.asList(super.getOptions())); // superclass
		return result.toArray(new String[result.size()]);
	}

    /**
     * Returns collection of rules induced by this classifier.
     * 
     * @return collection of rules induced by this classifier.
     */
    public Collection<Rule> getRules()
    {
        return ((RoughSetRules)getRseslibClassifier()).getRules();
    }

	/**
	* Main method for executing this classifier.
	*
	* @param args the options, use "-h" to display options
	*/
	public static void main(String[] args) throws Exception
	{
		runClassifier(new RoughSet(), args);
	}
}

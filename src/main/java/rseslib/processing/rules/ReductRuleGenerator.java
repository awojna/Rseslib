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


package rseslib.processing.rules;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import rseslib.processing.reducts.AllGlobalReductsProvider;
import rseslib.processing.reducts.AllLocalReductsProvider;
import rseslib.processing.reducts.GlobalReductsProvider;
import rseslib.processing.reducts.JohnsonReductsProvider;
import rseslib.processing.reducts.LocalReductsProvider;
import rseslib.processing.reducts.PartialReductsProvider;
import rseslib.structure.data.DoubleData;
import rseslib.structure.indiscernibility.ClassicIndiscernibility;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.indiscernibility.NonsymmetricSimilarityIndiscernibility;
import rseslib.structure.indiscernibility.SymmetricSimilarityIndiscernibility;
import rseslib.structure.rule.Rule;
import rseslib.structure.rule.EqualityDescriptorsRule;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Universal generator of rules from reducts.
 * It generates rules from all types of reducts.
 * 
 * @author Rafal Latkowski
 */
public class ReductRuleGenerator extends Configuration implements RuleGenerator
{
	/** Types of reducts. */
	public enum ReductsMethod { AllLocal, AllGlobal, OneJohnson, AllJohnson, PartialLocal, PartialGlobal; };
	/** Types of indiscernibility relations related to missing values. */
	public enum IndiscernibilityRelation { DiscernFromValue, DiscernFromValueOneWay, DontDiscernFromValue; };

	/** Parameter name for type of reducts. */
    public static final String s_sReductsMethod = "Reducts";
	/** Parameter name for type of indiscernibility relation. */
	public static final String s_sIndiscernibilityRelation = "IndiscernibilityForMissing";
	/** Parameter name for the switch enabling or disabling descriptors with missing values in generated rules. */
    public static final String s_sAllowComparingMissingValues = "MissingValueDescriptorsInRules";

    /** Type of used reducts. */
    private ReductsMethod m_ReductsMethod;
    /** Type of used indiscernibiliy relation for missing values. */
    private Indiscernibility m_indiscernibility = null;
    /** Switch enabling or disabling descriptors with missing values in generated rules. */
    boolean m_bAllowComparingMissingValues = true;
        
    /**
     * Constructor preparing this rule generator depending on the parameter values.
     * 
     * @param prop	Parameters defining the rules to be generated.
     * @throws PropertyConfigurationException when the parameters are incorrect or incomplete.
     */
    public ReductRuleGenerator(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
        try {
        	m_ReductsMethod = ReductsMethod.valueOf(getProperty(s_sReductsMethod));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown reducts type: "+getProperty(s_sReductsMethod));
        }
    	try
    	{
        	switch (IndiscernibilityRelation.valueOf(getProperty(s_sIndiscernibilityRelation)))
        	{
        	case DiscernFromValue:
        		m_indiscernibility = new ClassicIndiscernibility();
        		break;
        	case DontDiscernFromValue:
                m_indiscernibility = new SymmetricSimilarityIndiscernibility();
                break;
        	case DiscernFromValueOneWay:
                m_indiscernibility = new NonsymmetricSimilarityIndiscernibility();
        	}
    	}
    	catch (IllegalArgumentException e)
    	{
			throw new PropertyConfigurationException("Unknown indiscernibility relation for mising values: "+getProperty(s_sIndiscernibilityRelation));
        }
        m_bAllowComparingMissingValues = getBoolProperty(s_sAllowComparingMissingValues);

    }

    /**
     * This method generates global reducts given a reduct provider
     * and rules from the generated reducts. 
     * 
     * @param reductsProv            Provider of global reducts, defines the type of global reducts to be used.
     * @param tab		             Table used to generate reducts and rules.
     * @param prog                   Progress object for reporting progress.
     * @throws InterruptedException  when a user interrupts execution.
     */
    public Collection<Rule> generateGlobal(GlobalReductsProvider reductsProv, DoubleDataTable tab, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
		prog.set("Generating reducts and rules", tab.getDataObjects().size());
        Collection<BitSet> reducts = reductsProv.getReducts();
        HashSet<Rule> decisionRules = new HashSet<Rule>();
        for (DoubleData object : tab.getDataObjects())
        {
            for (BitSet reduct : reducts)
            {
            	EqualityDescriptorsRule rule = new EqualityDescriptorsRule(reduct,object,m_indiscernibility);
                if (m_bAllowComparingMissingValues || !rule.hasDescriptorWithMissingValue())
                	decisionRules.add(rule);
            }
            prog.step();
        }
        new RuleStatisticsProvider().calculateStatistics(decisionRules,tab);
        return decisionRules;
    }

    /**
     * This method generates local reducts given a reduct provider
     * and rules from the generated reducts.
     * 
     * @param reductsProv            Provider of local reducts, defines the type of local reducts to be used.
     * @param tab		             Table used to generate reducts and rules.
     * @param prog                   Progress object for reporting progress.
     * @throws InterruptedException  when a user interrupts execution.
     */
    public Collection<Rule> generateLocal(LocalReductsProvider reductsProv, DoubleDataTable tab, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        prog.set("Generating reducts and rules", tab.getDataObjects().size());
        HashSet<Rule> decisionRules = new HashSet<Rule>();
        for (DoubleData object : tab.getDataObjects())
        {
            Collection<BitSet> reducts = reductsProv.getSingleObjectReducts(object);
            for (BitSet reduct : reducts)
            {
            	EqualityDescriptorsRule rule = new EqualityDescriptorsRule(reduct,object,m_indiscernibility);
                if (m_bAllowComparingMissingValues || !rule.hasDescriptorWithMissingValue())
                	decisionRules.add(rule);
            }
            prog.step();
        }
        new RuleStatisticsProvider().calculateStatistics(decisionRules,tab);
        return decisionRules;
    }

    /**
     * Computes reducts from the data table
     * and generates rules from these reducts
     * as defined by the parameters.
     *
     * @param tab  Data set used to generate reducts and rules.
     * @param prog Progress object for reporting progress.
     * @return     Collection of generated rules.
     * @throws PropertyConfigurationException	when the parameters are incorrect or incomplete.
     * @throws InterruptedException				when a user interrupts execution.
     * @see rseslib.processing.rules.RuleGenerator#generate(rseslib.structure.table.DoubleDataTable, Progress)
     */
    public Collection<Rule> generate(DoubleDataTable tab, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
    	Collection<Rule> rules = null;
    	switch (m_ReductsMethod)
    	{
    	case AllLocal:
    		rules = generateLocal(new AllLocalReductsProvider(getProperties(), tab), tab, prog);
    		break;
    	case AllGlobal:
    		rules = generateGlobal(new AllGlobalReductsProvider(getProperties(), tab), tab, prog);
    		break;
    	case OneJohnson:
    	case AllJohnson:
    		rules = generateGlobal(new JohnsonReductsProvider(getProperties(), tab), tab, prog);
    		break;
    	case PartialLocal:
    		rules = generateLocal(new PartialReductsProvider(getProperties(), tab), tab, prog);
    		break;
    	case PartialGlobal:
    		rules = generateGlobal(new PartialReductsProvider(getProperties(), tab), tab, prog);
    		break;
    	}
    	return rules;
    }
}

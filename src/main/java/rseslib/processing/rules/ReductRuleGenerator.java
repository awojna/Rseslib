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
 * @author Rafal Latkowski
 *
 */
public class ReductRuleGenerator extends Configuration implements RuleGenerator
{
	public enum ReductsMethod { AllLocal, AllGlobal, OneJohnson, AllJohnson, PartialLocal, PartialGlobal; };
	public enum IndiscernibilityRelation { DiscernFromValue, DiscernFromValueOneWay, DontDiscernFromValue; };

    public static final String s_sReductsMethod = "Reducts";
	public static final String s_sIndiscernibilityRelation = "IndiscernibilityForMissing";
    public static final String s_sAllowComparingMissingValues = "MissingValueDescriptorsInRules";

    private ReductsMethod m_ReductsMethod;
    private Indiscernibility m_indiscernibility = null;
    boolean m_bAllowComparingMissingValues = true;
        
    /**
     * @throws PropertyConfigurationException 
     * 
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
     */
    private Collection<Rule> generateGlobal(GlobalReductsProvider reductsProv, DoubleDataTable tab, Progress prog) throws PropertyConfigurationException, InterruptedException
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

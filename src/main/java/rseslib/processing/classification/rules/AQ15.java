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


package rseslib.processing.classification.rules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import rseslib.processing.classification.AbstractClassifierWithDistributedDecision;
import rseslib.processing.rules.CoveringRuleGenerator;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.structure.rule.Rule;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Classifier uses AQ15 algorithm.
 * - with complete rules generation
 * - nominal selectors are inequality selectors
 * - numeric selectors are cuts between positive and 
 *   negative examples with margin, defined by property "margin"
 * - rule performance = positive examples + negative examples
 * - classification is by rules voting, or by rule with max weight, 
 * 	 according to property "vote"
 *  
 * @author	Cezary Tkaczyk
 */
public class AQ15 extends AbstractClassifierWithDistributedDecision implements Serializable {

    /** Serialization version. */
	private static final long serialVersionUID = 1L;	
	/** Name of the switch indicating whether test objects are classified by rule voting or by one best rule. */
	public static final String RULE_VOTING_NAME = "ruleVoting";
	
	/** Decision attribute. */
    NominalAttribute m_DecisionAttribute;
    /** Majority decision computed from a training data set. */
    private int m_nMajorityDecision;
    /** Number of test objects that match a rule. */
    private int m_nNoOfMatchesWithRules = 0; 
    /** Number of <code>classify()</code> method invocation */
    private int m_nNoOfClassifiedObjects = 0;
    /** The set of induced decision rules. */
    private Rule[]     m_Rules;
    /** Weights of corresponding rules 
     * (matched examples with same decision) */
    private double[]   m_RulesWeight;
    
    private int[]      m_narrayOfDescriptors;
    private Header     m_header;
    
	public AQ15(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
	{
		super(prop, trainTable);

		DoubleDataTable preparedTrainTable = 
			prepareAndGetArrayOfDescriptors(trainTable);
		
		m_DecisionAttribute = preparedTrainTable.attributes().nominalDecisionAttribute();
		
		// counting the majority decision, setting memebers
		int[] decDistr = preparedTrainTable.getDecisionDistribution();
        m_nMajorityDecision = 0;
        for (int dec = 1; dec < decDistr.length; dec++)
            if (decDistr[dec] > decDistr[m_nMajorityDecision]) m_nMajorityDecision = dec;
        
        Collection<Rule> rules = (new CoveringRuleGenerator(getProperties())).generate(preparedTrainTable, prog);
        countWeights(rules, preparedTrainTable);
        makePropertyModifiable(RULE_VOTING_NAME);
	}
	
	private DoubleDataTable prepareAndGetArrayOfDescriptors(DoubleDataTable trainTable)
	{
		Attribute[] attrs = new Attribute[trainTable.attributes().noOfAttr()];
		int i,j,attr;

        m_narrayOfDescriptors = new int[attrs.length];
        for (attr = 0, i = 0, j = attrs.length-1; attr < attrs.length; attr++)
        	if (trainTable.attributes().isConditional(attr)) {
        		m_narrayOfDescriptors[i] = attr;
        		attrs[i++] = trainTable.attributes().attribute(attr);
        	}
        	else if (trainTable.attributes().isDecision(attr)) {
        		m_narrayOfDescriptors[j]   = attr;
        		attrs[j--] = trainTable.attributes().attribute(attr);
        	}
        
        m_header = new ArrayHeader(attrs,null);
        DoubleDataTable newTable = new ArrayListDoubleDataTable(m_header);
        for (DoubleData dobj : trainTable.getDataObjects()) {
        	newTable.add(prepare(dobj));
        }
        return newTable;
	}

	private DoubleData prepare(DoubleData dobj)
	{
		DoubleData newDobj = new DoubleDataObject(m_header);
		for(int i=0; i<dobj.attributes().noOfAttr(); i++) {
			newDobj.set(i,dobj.get(m_narrayOfDescriptors[i]));			
		}
		return newDobj;
	}

	private void countWeights(Collection<Rule> rules, DoubleDataTable trainTable)
	{
		m_Rules          = new Rule[rules.size()];
		m_RulesWeight    = new double[rules.size()];
		int decAttr = trainTable.attributes().decision();
		Iterator<Rule> ruleIter = rules.iterator();
        for (int i=0; ruleIter.hasNext(); i++)
        {
            Rule r = (Rule)ruleIter.next();
            m_Rules[i] = r;
        }
        for(int i=0; i<rules.size(); i++) {
	        for (DoubleData dObj : trainTable.getDataObjects()) {
        		if ((m_Rules[i].matches(dObj)) 
        			&& (dObj.get(decAttr) == m_Rules[i].getDecision()))
        			m_RulesWeight[i]++;
        	}
        }
        /* Debug */
        /*
        for(int i=0; i<rules.size(); i++) {
        	System.out.println("Regula " + i + " weight: "+ m_RulesWeight[i] 
        	                  + " negweight: " + m_RulesNegWeight[i]);
        }
        */
	}

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	writeAbstractClassifier(out);
    	out.writeObject(m_DecisionAttribute);
    	out.writeInt(m_nMajorityDecision);
    	out.writeObject(m_Rules);
    	out.writeObject(m_RulesWeight);
    	out.writeObject(m_narrayOfDescriptors);
    	out.writeObject(m_header);
    }

    /**
     * Reads this object.
     *
     * @param in			Input for reading.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readAbstractClassifier(in);
    	m_DecisionAttribute = (NominalAttribute)in.readObject();
    	m_nMajorityDecision = in.readInt();
    	m_Rules = (Rule[])in.readObject();
    	m_RulesWeight = (double[])in.readObject();
    	m_narrayOfDescriptors = (int[])in.readObject();
    	m_header = (Header)in.readObject();
    }


    public double[] classifyWithDistributedDecision(DoubleData dObj) throws PropertyConfigurationException
	{
		if (getBoolProperty(RULE_VOTING_NAME))
			return classifyByWeightVoting(dObj);
		else
			return classifyByMaxWeight(dObj);
	}
	
	private double[] classifyByMaxWeight(DoubleData dObj)
	{
		dObj = prepare(dObj);
		double dec = m_DecisionAttribute.globalValueCode(m_nMajorityDecision);
		double maxWeight = 0;
		for(int i=0; i<m_Rules.length; i++) {
			if ((m_Rules[i].matches(dObj))
				&& (m_RulesWeight[i] > maxWeight))
            {
				maxWeight = m_RulesWeight[i];
				dec       = m_Rules[i].getDecision();
            }
		}
		if (maxWeight > 0) m_nNoOfMatchesWithRules++;
		m_nNoOfClassifiedObjects++;
		
		/* Debug */
		//System.out.println("MaxWeight: " + maxWeight);
		
		double[] voteTable = new double[m_DecisionAttribute.noOfValues()];
		voteTable[m_DecisionAttribute.localValueCode(dec)] = 1.0;
		return voteTable;
	}
	
	private double[] classifyByWeightVoting(DoubleData dObj)
	{
		dObj = prepare(dObj);
		double[] voteTable = new double[m_DecisionAttribute.noOfValues()];
		
		for(int i=0; i<m_Rules.length; i++) {
			if (m_Rules[i].matches(dObj)) {
				int dec = m_DecisionAttribute.localValueCode(m_Rules[i].getDecision());
				voteTable[dec] += m_RulesWeight[i]; 
            }
		}
		
		boolean matched = false;
		for(int i=0; i<voteTable.length; i++)
			if (voteTable[i] > 0) {
				matched = true;
				break;
			}
		if(matched)
			m_nNoOfMatchesWithRules++;
		else
			voteTable[m_nMajorityDecision] = 1.0;
		m_nNoOfClassifiedObjects++;
		
		return voteTable;
	}

	/**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
        addToStatistics("Number of matches with rules", 
        		" " + m_nNoOfMatchesWithRules + "/" + m_nNoOfClassifiedObjects);
    }	

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    	m_nNoOfMatchesWithRules=0;
    	m_nNoOfClassifiedObjects=0;
    }
}

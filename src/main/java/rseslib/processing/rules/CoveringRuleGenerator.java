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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;


import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.rule.Rule;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * @author ct201224
 *
 */
public class CoveringRuleGenerator extends Configuration implements RuleGenerator{

	private OneRuleGenerator m_oneRuleGenerator;
	private int              m_k;
	private double           m_prooning;
	
	/**
     * Constructor.
     *
     * @param prop                Parameters of this rule generator. If null,
     *                            parameters are loaded from the configuration directory.
     */
    public CoveringRuleGenerator(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
        m_oneRuleGenerator = new AQ15OneRuleGenerator(getProperties());
        m_k                = getIntProperty("searchWidth");
        m_prooning         = 1-getDoubleProperty("coverage");
    }
    
    /**
     * Returns a collection of rules generated from data table.
     *
     * @param tab  Data objects to be used for rule generation.
     * @return     Collection of generated rules.
     */
	public Collection<Rule> generate(DoubleDataTable tab, Progress prog) throws InterruptedException {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		ArrayList<DoubleDataWithDecision> uncovered = 
			new ArrayList<DoubleDataWithDecision>();
		
		int 			 decIdx  = tab.attributes().decision();
		NominalAttribute decAttr = (NominalAttribute)tab.attributes().attribute(decIdx);
		int              noOfVal = decAttr.noOfValues();
		
		prog.set("Generating AQ15 rules", noOfVal);
		for (int i = 0; i < noOfVal; i++) {
			double dec = decAttr.globalValueCode(i);
			//System.out.println("Generating rules for dec : " + dec);
			uncovered.clear();
			findUncoveredWithDecision(uncovered, tab, dec);

			int prooningObjts = uncovered.size();
			
			//usually - while (uncovered.size() > 0) 
			while (uncovered.size() > m_prooning * prooningObjts) {
				rules.add(m_oneRuleGenerator.generate(tab, uncovered, m_k, dec));
				uncovered = removeCovered(uncovered, rules);
			}
			
			/* Debug */
			/*
			 System.out.println(
					" Rules.size() = " + rules.size());
			*/
			prog.step();
		}
		return rules;
		
	}
	
	private ArrayList<DoubleDataWithDecision> removeCovered(
			ArrayList<DoubleDataWithDecision> uncovered,
			ArrayList<Rule> rules) {
		
		ArrayList<DoubleDataWithDecision> uncoveredTmp =
			new ArrayList<DoubleDataWithDecision>();
		Iterator uncoveredIt = uncovered.iterator();
		
		while (uncoveredIt.hasNext()) {
			DoubleDataWithDecision example = 
				(DoubleDataWithDecision)uncoveredIt.next();
			boolean  notMatched            = true;
			Iterator rulesIt               = rules.iterator();
 			while (rulesIt.hasNext()) {
 				Rule rule = (Rule) rulesIt.next();
 				if (rule.matches(example))
 					notMatched = false;
 			}
 			if (notMatched) uncoveredTmp.add(example);
		}
		
		return uncoveredTmp;
	}
			
	private void findUncoveredWithDecision(
			ArrayList<DoubleDataWithDecision> uncovered,
			DoubleDataTable 				  tab,
			double							  dec) {

		for (DoubleData example : tab.getDataObjects()) {
			if (((DoubleDataWithDecision)example).getDecision() == dec) {
				uncovered.add((DoubleDataWithDecision)example);
			}
		}
	}
	
}

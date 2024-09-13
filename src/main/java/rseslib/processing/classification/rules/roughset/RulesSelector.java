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


package rseslib.processing.classification.rules.roughset;

import rseslib.structure.data.DoubleData;
import rseslib.structure.rule.*;

/**
 * Interface for objects which are passed as selectors to the table model. 
 * Only thing they can do is to say which rules are to be shown and which do 
 * not
 * @author Krzysztof Niemkiewicz
 */
public interface RulesSelector {
	boolean isChoosen(EqualityDescriptorsRule r);
}
/**
 * This selector is used as default selector if we want to show rules which
 * match given object from test table. Just gives value of rule.match() on 
 * given item
 * 
 */
class RuleMatchSelector implements RulesSelector{
	DoubleData dd;
	
	public RuleMatchSelector(DoubleData dd) {

		this.dd = dd;
	}

	public boolean isChoosen(EqualityDescriptorsRule r) {
		return r.matches(dd);
		
	}
	
}


/**
 * Most common selector, which select given value of one of 
 * descriptors of a rule
 *
 */
class OneValueTester implements RulesSelector{

	int attr;
	Double val;	
	public OneValueTester(int attr, Double val) {
		
		this.attr = attr;
		this.val = val;
	}

	public boolean isChoosen(EqualityDescriptorsRule r) {
		if (!(r.hasDescriptor(attr))){
			return false;
		};
		return (r.getDescriptor(attr)==val);
		
	}
	public String toString(){
		return "OneValueTester:v"+attr+"="+val;
	}
}
/**
 * Selector which selects rules of given decision
 *
 */
class RuleDecisionTester implements RulesSelector{

	Double dec;
	public RuleDecisionTester(Double dec) {
	
		this.dec = dec;
	}
	public boolean isChoosen(EqualityDescriptorsRule r) {
		return r.getDecision()==dec;
	}
	public String toString(){
		return "RuleDecisionTester:"+dec;
	}
}
/**
 * Selector which checks if given descriptors exists in a rule
 *
 */
class NotNullValueTester implements RulesSelector{

	int attr;
		
	public NotNullValueTester(int attr) {
		
		this.attr = attr;		
	}

	public boolean isChoosen(EqualityDescriptorsRule r) {
		return r.hasDescriptor(attr);
	}
	public String toString(){
		return "NotNullValueTester:v"+attr;
	}
	
}
/**
 * Selector which selects rules of given length
 *
 */
class RuleLengthTester implements RulesSelector{

	int len;
	
	
	public RuleLengthTester(int len) {
	
		this.len = len;
	}
	public boolean isChoosen(EqualityDescriptorsRule r) {
		return r.getRuleLength()==len;
	}
	public String toString(){
		return "RuleLengthTester:"+len;
	}
	
}
/**
 * Selector which selects rules with given support
 *
 */
class RuleSupportTester implements RulesSelector{

	double dd;
	
	
	public RuleSupportTester(Double d) {
	
		this.dd = d;
	}
	public boolean isChoosen(EqualityDescriptorsRule r) {
		double diff=(r.getSupport()-dd);
		return (diff>-0.001)&&(diff<0.001);
	}
	public String toString(){
		return "RuleSupportTester:"+dd;
	}
	
}

/**
 * Selector which selects rules with given accuracy
 *
 */
class RuleAccuracyTester implements RulesSelector{

	double dd;
	
	
	public RuleAccuracyTester(Double d) {
	
		this.dd = d;
	}
	public boolean isChoosen(EqualityDescriptorsRule r) {
		double diff=(r.getAccuracy()-dd);
		return (diff>-0.001)&&(diff<0.001);
	}
	public String toString(){
		return "RuleAccuracytTester:"+dd;
	}
	
}
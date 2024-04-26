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

import java.util.*;

import javax.swing.table.AbstractTableModel;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.rule.EqualityDescriptorsRule;
import rseslib.structure.rule.Rule;



/**
 * Main model of whole classifier
 * This class handles all data in the main table and does and sorting 
 * and selecting basing on comparators ans selectors passed by respective 
 * models. Also all other table related functionality is handled here
 * @author Krzysztof Niemkiewicz
 */
public class RulesTableModel extends AbstractTableModel {


	//private 
	VisualRoughSetClassifier vrs;
	private Vector<EqualityDescriptorsRule> rules;
	private Vector<EqualityDescriptorsRule> defaultRules;	
	private Comparator<EqualityDescriptorsRule>lastComparator;
	private RulesSelector defSelector;
	private static final long serialVersionUID = 6968820149519008089L;


    /**
     * Main constructor
     * @param vrs2 classifier which will be visualized
     * @param defSelector default selector, normally null, used to create visualization of classification of single table element
     */

	RulesTableModel(VisualRoughSetClassifier vrs2,RulesSelector defSelector){
		vrs=vrs2;		
		rules=new Vector<EqualityDescriptorsRule>();
		Iterator<Rule> i=vrs.getRules().iterator();
		this.defSelector=defSelector;
		if (defSelector==null){
		while (i.hasNext()){
			rules.add((EqualityDescriptorsRule)i.next());
		};}else
		{
			while (i.hasNext()){
				EqualityDescriptorsRule edr=(EqualityDescriptorsRule)i.next();
				if (defSelector.isChoosen(edr)){
				rules.add(edr);};
			};
		}
		
		defaultRules=new Vector<EqualityDescriptorsRule>(rules);
	
		
		
	}
    /**
     * Returns number of rules visible when default selector is applied,
     * used only when classification is shown
     */
	public int getDefaultRulesCount() {
		return defaultRules.size();
	}
   
    /**
     * Returns vector of attributes from the header of trainTable
     */
	public Vector<Attribute> getAttributes(){
		Vector<Attribute> res=new Vector<Attribute>(); 
		for (int i=0;i<vrs.getHeader().noOfAttr();i++){
			res.add(vrs.getHeader().attribute(i));
		}
		return res;
	}
    /**
     * Sorts rules in model according to given comparator
     */
	public void reSort(Comparator<EqualityDescriptorsRule> c){
		Collections.sort(rules,c);
		lastComparator=c;
		this.fireTableDataChanged();
	}
    /**
     * Selects rules which will be visible in the table
     * according to given selector
     */
	
	public void reSelect(VectorMultipleRulesSelector s){
		s.add(defSelector);
		rules=new Vector<EqualityDescriptorsRule>();
		Iterator<EqualityDescriptorsRule> i=defaultRules.iterator();
		while (i.hasNext()){
			EqualityDescriptorsRule edr=i.next();
			if (s.isChoosen(edr)){
			rules.add(edr);};
		};
		if (lastComparator!=null){this.reSort(lastComparator);};
		this.fireTableDataChanged();
	}	
    /**
     * Returns number of columns
     */
	
	public int getColumnCount() {
		return 4;
	}

    /**
     * Returns number of rows equal to the number of rules which
     * will be visible
     */
	
	public int getRowCount() {
		return rules.size();
	}

    /**
     * Returns objects which will be shown in the table
     * From the left, columns contain
     * rule length
     * rule support
     * rule accuracy
     * description of the rule
     */

	public Object getValueAt(int arg0, int arg1) {
		switch (arg1){
		case 0:return rules.get(arg0).getRuleLength();
		case 1:return rules.get(arg0).getSupport();
		case 2:return rules.get(arg0).getAccuracy();
		case 3:return rules.get(arg0);		
		} 
	 return "";

	}
    /**
     * Returns column names 
     *
     */
	 public String  getColumnName(int column) {		 		 
		 switch (column){
			case 0:return "Length";
			case 1:return "Support";
			case 2:return "Accuracy";
			case 3:return "Rules";		
			} 
		 return "";
	 }
    /**
     * Gives all the rules as a vector, used in some valueCreators
     * do not change the vector
     */

	public  Vector<EqualityDescriptorsRule> getAllRules() {
			return defaultRules;
	}
	
    /**
     * Gives all the visible rules as a vector
     */
	
	public  Vector<EqualityDescriptorsRule> getRules() {
		return rules;
	}

} 

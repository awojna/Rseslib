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


package rseslib.processing.classification.rules.roughset;
import javax.swing.*;


import rseslib.structure.attribute.Attribute;
import rseslib.structure.rule.*;

import java.util.*;


/**
 * This class controls whole process of sorting rules in rules table.
 * All SortModels register here and if changed, they force resorting whole table. SortMainModel creates proper {@link VectorMultipleRulesComparator} basing on partial Comparators given by SortModels. Full comparator is then passed to {@link RulesTableModel}. 
 @author Krzysztof Niemkiewicz
 @see RulesTableModel
 */

public class SortMainModel extends AbstractListModel {

	private static final long serialVersionUID = 3742258943204860091L;
	Vector<Attribute> free,attribs;
	Vector<SortModel> sortModels; 
	MainSortPanel sortPane;
	private int totalSize;
	RulesTableModel rtm;

    /**
     * Special, additional atribute which can be choosen by user to sort or select by it's values. This particular one is empty, and makes no change in sorting.
     */

	public final static Attribute ATRIB_NONE=new Attribute(Attribute.Type.conditional,Attribute.ValueSet.numeric,"none");

    /**
     * Special, additional atribute which can be choosen by user to sort or select by it's values. 
     This particular one is connected to length of rules.
     */

	public final static Attribute ATRIB_LENGTH=new Attribute(Attribute.Type.conditional,Attribute.ValueSet.numeric,"rule length");

    /**
     * Special, additional atribute which can be choosen by user to sort or select by it's values. 
     This particular one is connected to the support of rules.
     */
	
	public final static Attribute ATRIB_SUPPORT=new Attribute(Attribute.Type.conditional,Attribute.ValueSet.numeric,"rule support");
    
    /**
     * Special, additional atribute which can be choosen by user to sort or select by it's values. 
     This particular one is connected to accuracy of rules.
     */
	
	public final static Attribute ATRIB_ACC=new Attribute(Attribute.Type.conditional,Attribute.ValueSet.numeric,"rule accuracy");

    /**
     * Main constructor
     * @param msp panel which stores all sorting related components
     * @param rtm2 table with rules to which sorting will be applied
     */
	SortMainModel(MainSortPanel msp,RulesTableModel rtm2){
		rtm=rtm2;
		sortPane=msp;
		attribs=(Vector<Attribute>) rtm.getAttributes();
		free=new Vector<Attribute>();
		Iterator<Attribute> i=attribs.iterator();
		while (i.hasNext()){
			Attribute a=i.next();
			if (!(a.isDecision())){
				free.add(a);
			}
		};
//		attribs=(Vector<Attribute>)free.clone();
		sortModels=new Vector<SortModel>();
		totalSize=free.size();
		
		free.add(ATRIB_LENGTH);
		free.add(ATRIB_SUPPORT);
		free.add(ATRIB_ACC);
		Collections.sort(free,new AComparator());
		
	}
    /**
     * This model stores, controls and gives list of free attributes - attributes which haven't been choosen for sorting yet.
     * @return one of free attributes
     */
	public Object getElementAt(int index) {				
		return free.get(index);
	}
    /**
     * this method maps attributes to their numbers in standard ordering which I 
     *used
     * 
     * @return one of free attributes
     */

	public int indexOfAtrr(Attribute a){
		//int i=-1;
		
//		return rtm.vrs.getHeader().
		return attribs.indexOf(a);
	}
    /**
     * 
     * @return number of all attributes, without special
     */

	public int getTotalSize(){
		return totalSize+3;
	}

    /**
     * This model stores, controls and gives list of free attributes - attributes which haven't been choosen for sorting yet.
     * @return number of free attributes
     */

	public int getSize() {
		return free.size();
	}
/**
     * Adds deselected item to free ones
     * @param a deselected attribute
     */
    
	public void deselectSort(Attribute a){
		if (a!=ATRIB_NONE)free.add(a);
		Collections.sort(free,new AComparator());		
	}
/**
     * Removes newly selected item from free ones
     * @param a selected attribute
     */

	public void selectSort(Attribute a){
		free.remove(a);
		Collections.sort(free,new AComparator());
	};	
/**
     * All new SortModels have to register via this method
     * @param nr number of sortModel to be add
     * @param sm the new SortModel 
     */

	public void registerSort(int nr,SortModel sm){
		this.addListDataListener(sm);
		sortModels.ensureCapacity(nr+1);
		if (sortModels.size()<nr){
		sortModels.set(nr,sm);}else{
			sortModels.add(nr,sm);
		}
	}
    /**
     * Value changed, resorting whole table
     */
	public void valueChanged(){
	this.fireIntervalAdded(this, 0,this.getSize() );
		rtm.reSort(getComparator());
	}
	public void sizeChanged(){
		sortPane.valueChanged();
	}
    /**
     * Creates complex comparator from simpler ones which are retrieved from SortModels
     */

	private Comparator<EqualityDescriptorsRule> getComparator() {
		VectorMultipleRulesComparator res=new VectorMultipleRulesComparator();
		Iterator<SortModel> i=sortModels.iterator();
		while (i.hasNext()){
			res.add(i.next().getComparator());
		};
		return res; 
	}

	public Vector<Attribute> getAttribs() {
		return attribs;
	}

	
	}	


/**
 * Class which implements special comparator made from smaller ones.
 * This comparator compares items by using vector of comaprators.
 * Next comparator form vector is used only when previous says that items are equal.
 */
class AComparator implements Comparator<Attribute>{

	public int compare(Attribute arg0,
			Attribute arg1) {
		return arg0.name().compareTo(arg1.name());
	}
	
}
class VectorMultipleRulesComparator implements Comparator<EqualityDescriptorsRule> {

	private Vector<Comparator<EqualityDescriptorsRule>> clist;
	VectorMultipleRulesComparator(){
		clist=new Vector<Comparator<EqualityDescriptorsRule>>();
	};
	void add(Comparator<EqualityDescriptorsRule> c){
	
		if (c!=null)clist.add(c);
	}
	public int compare(EqualityDescriptorsRule arg0, EqualityDescriptorsRule arg1){
		Iterator<Comparator<EqualityDescriptorsRule>> i=clist.iterator();		
		while (i.hasNext()){
			int choice=i.next().compare(arg0, arg1);
			if (choice!=0)return choice;
		}
		return 0;
		
	}
	public String toString(){
		return clist.toString();
	}
}

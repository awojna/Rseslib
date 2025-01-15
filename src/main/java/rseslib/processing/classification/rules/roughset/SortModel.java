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


package rseslib.processing.classification.rules.roughset;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.rule.EqualityDescriptorsRule;

/**
 *  Model for combo box used to change the way of sorting items in the table
 *  few such models connect themselfes to the MainSortModel which controls 
 *  whole process 
 *  @see SortMainModel
 * @author Krzysztof Niemkiewicz
 */

public class SortModel extends AbstractListModel implements ComboBoxModel,ListDataListener {


	Attribute selected;
	SortMainModel main;
	int nr;
	boolean used;
	private static final long serialVersionUID = 4933519999646303913L;
    /**
     * Main constructor
     * @param mainMod {@link SortMainModel} in which this object will register
     * @param which which number this model has(eg. first,second of all similar models)
     */
	SortModel(SortMainModel mainMod,int which){
		nr=which;
		
		main=mainMod;
		main.registerSort(nr,this);
		selected=SortMainModel.ATRIB_NONE;
	};

    /**
     * This model not only shows all elements which are shown as free by the SortMainModel. It also adds special {@link SortMainModel#ATRIB_NONE} option.
     * return proper option for the combo box
     */
	public Object getElementAt(int index) {	
		if (selected==SortMainModel.ATRIB_NONE)return (Attribute)main.getElementAt(index);
		if (index==0){return SortMainModel.ATRIB_NONE;};
		return (Attribute)main.getElementAt(index-1);
		
	}
    /**
     * we add one because of the special {@link SortMainModel#ATRIB_NONE} option.
     */
	public int getSize() {
		if (selected==SortMainModel.ATRIB_NONE)return main.getSize();
		return (main.getSize()+1);
	}
	 public Object 	getSelectedItem(){
		 return selected;
	 }
     /*
     public void setSelectedItem(Object anItem)
     {
    	 if (!selected.equals(SortMainModel.ATRIB_NONE)){main.deselectSort(selected);};
    	 selected=(Attribute)anItem;
    	 if (!selected.equals(SortMainModel.ATRIB_NONE)){main.selectSort(selected);};
    	 main.valueChanged(nr);
    	 this.fireContentsChanged(this, 0,this.getSize() );
     }
    */
	 public void setSelectedItem(Object anItem)
     {
		 
    	main.deselectSort(selected);
    	 selected=(Attribute)anItem;
    	main.selectSort(selected);
    	 
    	 if (!used){
    		 main.sizeChanged();
    	 };
    	 used=true;
    	 main.valueChanged();

    	 
     } 
	 
    /**
     * Returns partial comparator, depending on what item is selected
     * @see SortMainModel#getComparator()
     */
     public Comparator<EqualityDescriptorsRule> getComparator(){
    	 int i=main.indexOfAtrr(selected);

    	 if (i>=0)
    	 	if (main.attribs.get(i).isDecision()){return new DecisionComparator(main);}
    	 	else{return new OneValueComparator(i,main.attribs.get(i).isNominal());};
    	 
    	 if (selected==SortMainModel.ATRIB_LENGTH){
    		 return new LengthComparator(main);
    	 }
    	 if (selected==SortMainModel.ATRIB_SUPPORT){
    		 return new SupportComparator(main);
    	 }
    	 if (selected==SortMainModel.ATRIB_ACC){
    		 return new AccComparator(main);
    	 }
    	  
 		return null;
 	 	
 	}

	public void contentsChanged(ListDataEvent arg0) {
		
	}

	public void intervalAdded(ListDataEvent arg0) {this.fireContentsChanged(this, 0,this.getSize() );	}

	public void intervalRemoved(ListDataEvent arg0) {	}	
}
/**
 *  Comparator which compares two EqualityDescriptorsRules basing on one their
 *  desciptors, given by it's number in internal EDR ordering
 */
class OneValueComparator implements Comparator<EqualityDescriptorsRule>{
	int attr;
	boolean nominal;
	
	public OneValueComparator(int attr,boolean nominal) {
		
		this.attr = attr;
		this.nominal = nominal;
//		if (nominal){this.attr++;};

	}

	public int compare(EqualityDescriptorsRule arg0,
			EqualityDescriptorsRule arg1) {
			
			if (!(arg1.hasDescriptor(attr)))return -1;
			if (!(arg0.hasDescriptor(attr)))return 1;			
			if (nominal){
				return NominalAttribute.stringValue(arg0.getDescriptor(attr)).compareTo(NominalAttribute.stringValue(arg1.getDescriptor(attr)));
			}else
			{
				if (arg0.getDescriptor(attr)<arg1.getDescriptor(attr))return -1;
				if (arg0.getDescriptor(attr)>arg1.getDescriptor(attr))return 1;
				return 0;
			}
		
	}
	
}
/**
 *  Comparator which compares two EqualityDescriptorsRules basing on their decision
 */

class DecisionComparator implements Comparator<EqualityDescriptorsRule>{
	
	SortMainModel smm;
	
	
	public DecisionComparator(SortMainModel smm) {

		this.smm = smm;
	}


	public int compare(EqualityDescriptorsRule arg0,
			EqualityDescriptorsRule arg1) {

		return NominalAttribute.stringValue(arg0.getDecision()).compareTo(NominalAttribute.stringValue(arg1.getDecision()));
	}
	
}
/**
 *  Comparator which compares two EqualityDescriptorsRules basing on their length
 */

class LengthComparator implements Comparator<EqualityDescriptorsRule>{
	
	SortMainModel smm;
	
	
	public LengthComparator(SortMainModel smm) {

		this.smm = smm;
	}


	public int compare(EqualityDescriptorsRule arg0,
			EqualityDescriptorsRule arg1) {

		return arg0.getRuleLength()-arg1.getRuleLength();
	}
	
}
/**
 *  Comparator which compares two EqualityDescriptorsRules basing on their accuracy
 */


class AccComparator implements Comparator<EqualityDescriptorsRule>{
	
	SortMainModel smm;
	
	
	public AccComparator(SortMainModel smm) {

		this.smm = smm;
	}


	public int compare(EqualityDescriptorsRule arg0,
			EqualityDescriptorsRule arg1) {
		if (arg0.getAccuracy()==arg1.getAccuracy()){
			return 0;
		}		
		return arg0.getAccuracy()<arg1.getAccuracy()?1:-1;
	}
	
}


/**
 *  Comparator which compares two EqualityDescriptorsRules basing on their support
 */

class SupportComparator implements Comparator<EqualityDescriptorsRule>{
	
	SortMainModel smm;
	
	
	public SupportComparator(SortMainModel smm) {

		this.smm = smm;
	}


	public int compare(EqualityDescriptorsRule arg0,
			EqualityDescriptorsRule arg1) {
		if (arg0.getSupport()==arg1.getSupport()){
			return 0;
		}		
		return arg0.getSupport()<arg1.getSupport()?1:-1;
	}
	
}

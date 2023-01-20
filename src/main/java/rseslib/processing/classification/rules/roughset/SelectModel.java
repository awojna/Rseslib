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



import java.util.HashMap;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;

/**
 * This class is similar to SortModel and implements selecting part of 
 * all rules to be shown in a table. It is implemented as model of second 
 * combobox in SelectPanel.Values for the combobox are retrieved form the 
 * SelectMainModel and some special ones are added like 'any' and 'not empty' 
 * To know which attribute is selected(not which value, this is known directly)
 * we are listening to changes in SortModel which controls the 
 * first combobox on mentioned
 * SelectPanel. On any changes, SelectMainModel is notified.
 * @author Krzysztof Niemkiewicz
 */

public class SelectModel extends AbstractListModel implements ComboBoxModel,ListDataListener {


	private static final long serialVersionUID = -8668375636718909706L;
	private static final String NAME_NONE="any";
	private static final String NAME_NOT_NULL="not empty";	
	HashMap<String,Double> coding; 
	Double sel;
	Attribute actualAttr;
	SelectMainModel main;
	int nr;
    /**
     * Main constructor
     * @param mainMod SelectMainModel for this model
     * @param which which one is this sortModel form all of them
     */
	SelectModel(SelectMainModel mainMod,int which){
		coding=new HashMap<String,Double>();
		actualAttr=SortMainModel.ATRIB_NONE;
		sel=SelectMainModel.VAL_NONE;
		main=mainMod;
		nr=which;
		main.registerSelect(this);
		this.addListDataListener(main);
	}

	private String getValueName(double d){
	
		String s;
		if (actualAttr.isNominal()){s=NominalAttribute.stringValue(d);}else
				s=String.valueOf(d);
		coding.put(s, d);
		return s;
	}
	private double getValueByName(String s){		
		return coding.get(s);
	}
	
    /**
     * translates selection to string understood by the combobox
     * especially for the special text options 
     */
	public Object getSelectedItem() {
		if (sel==SelectMainModel.VAL_NONE){
			return NAME_NONE;
		}
		if (sel==SelectMainModel.VAL_NOT_NULL){
			return NAME_NOT_NULL;
		}
		
		return getValueName(sel);
	}

    /**
     * translates selection from the combobox 
     * into internal double representation 
     */
	
	public void setSelectedItem(Object anItem) {
		if (anItem==NAME_NONE){
			sel=SelectMainModel.VAL_NONE;
		}else
		if (anItem==NAME_NOT_NULL){
			sel=SelectMainModel.VAL_NOT_NULL;
		}else			
		sel=getValueByName((String)anItem);
		
		this.fireContentsChanged(this, 0,this.getSize() );
		
	}
    /**
     * Checking if selected value and atrribute are real attribures or 
     * special cases
     *
     */
	private boolean normalValueSelected(){
		
		return SelectMainModel.normalValueSelected(sel)&&(!SelectMainModel.specialValueCreators.containsKey(actualAttr));
	}
    /**
     * Shows values from generated by SelectMainModel for given 
     *  attribute plus some special ones
     
     */
	
	public Object getElementAt(int index) {
		if (!SelectMainModel.specialValueCreators.containsKey(actualAttr)){
		if (index==0)return NAME_NONE;
		if (index==1)return NAME_NOT_NULL;		
		return getValueName(main.valuesOf(actualAttr).get(index-2));}else
		{
			if (index==0)return NAME_NONE;			
			return getValueName(main.valuesOf(actualAttr).get(index-1));
				
		}
	}

    /**
     * Shows values from generated by SelectMainModel for given 
     *  attribute plus some special ones, giving their total size
     
     */
	public int getSize() {
		if (!SelectMainModel.specialValueCreators.containsKey(actualAttr)){
		return main.valuesOf(actualAttr).size()+2;}else{
			return main.valuesOf(actualAttr).size()+1;
		}
	}
	
    /**
     * This model listens to the SortModel of the same SelectPanel
     * this method retrieves and changes select attributes and therefore 
     * all other values given by this model
     */
	public void contentsChanged(ListDataEvent arg0) {
		Attribute newAtr=(Attribute)((SortModel)arg0.getSource()).getSelectedItem();
		
		if (actualAttr!=newAtr ){
		actualAttr=newAtr;
		sel=SelectMainModel.VAL_NONE;
		 this.fireContentsChanged(this, 0,this.getSize() );};
	};
	public void intervalAdded(ListDataEvent arg0) {};
	public void intervalRemoved(ListDataEvent arg0) {};
    /**
     * Gives partial selector which chooses only those values which match 
     * selection stored by this model
     */
	public RulesSelector getSelector(){
		
		int i;		
		if (SelectMainModel.normalValueSelected(sel)){
			i=main.indexOfAtrr(actualAttr);}else{i=-1;};
			
		if (i>=0){return new OneValueTester(i,sel);};
		
		if (sel==SelectMainModel.VAL_NONE)
			return null;	
		
		if (actualAttr==SortMainModel.ATRIB_LENGTH){			
			return new RuleLengthTester(
				sel.intValue());
			};
		if (actualAttr==SortMainModel.ATRIB_SUPPORT){			
				return new RuleSupportTester(sel);
				};
		if (actualAttr==SortMainModel.ATRIB_ACC){					
			return new RuleAccuracyTester(sel);
			};	
				
		if (sel==SelectMainModel.VAL_NOT_NULL)
			return new NotNullValueTester(main.indexOfAtrr(actualAttr));	
		
		return null;
	}	
}


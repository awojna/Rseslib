/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


import rseslib.structure.attribute.Attribute;
import rseslib.structure.rule.EqualityDescriptorsRule;


/**
 * This class extends {@link SortMainModel} and handles the selecting process.
 * Multiple {@link SortModel} objects register here and 
 * their selectors are used to create
 * complex selector which is passed to {@link RulesTableModel}. Also 
 * generating values which can be choosed is handled here and results are 
 * passed to SortModels
 *
 * @author Krzysztof Niemkiewicz
 */
public class SelectMainModel extends SortMainModel implements ListDataListener{

    /**
     * This objects maps special attributes to special objects which create 
     * list of possible values of those attributes
     * this map is set once when creating SelectMainModel and never 
     * changed later
     */
	public static HashMap<Attribute,ValueListCreator> specialValueCreators;
    /**
     * special numeric value for "none" selection as all selections 
     * are stored as Double 
     */
	public static Double VAL_NONE=Double.NaN;
/**
     * special numeric value for "not empty" selection as all selections 
     * are stored as Double 
     */
	
	public static Double VAL_NOT_NULL=Double.POSITIVE_INFINITY;

	private static final long serialVersionUID = 4306543321549983069L;
	Vector<SelectModel> selMod;
	Vector<Vector<Double>> values=new Vector<Vector<Double>>();
    /**
     * Main constructor 
     * @param msp MainSortPanel which will contain all SelectPanels and stuff inside them
     * @param rtm2 model for the table which will be affected by selection
     */
	SelectMainModel(MainSortPanel msp, RulesTableModel rtm2) {
		super(msp, rtm2);		
		selMod=new Vector<SelectModel>();
		Vector<TreeSet<Double>> treeValues=new Vector<TreeSet<Double>>();
		for (int i=0;i<rtm.getAttributes().size();i++){
			treeValues.add(new TreeSet<Double>());
			values.add(new Vector<Double>());
		};
		Iterator<EqualityDescriptorsRule> i=rtm.getRules().iterator();
		EqualityDescriptorsRule edr;
		
		while(i.hasNext()){
			edr=i.next();
			for (int j=0;j<treeValues.size();j++){
				if (edr.hasDescriptor(j)){
					treeValues.get(j).add(edr.getDescriptor(j));
				};
			};
		};
		for (int j=0;j<rtm.getAttributes().size();j++){
			values.get(j).addAll(treeValues.get(j));
		};
		
		specialValueCreators=new HashMap<Attribute,ValueListCreator>();
	
		// Here we create all special creators mentioned above

		specialValueCreators.put(
			SortMainModel.ATRIB_NONE,
			new ValueListCreator(){
				public Vector<Double> getValues(){
					return new Vector<Double>();
				}

				public void update(){};
			});
		specialValueCreators.put(
				SortMainModel.ATRIB_LENGTH,
				new ValueListCreator(){
					void update() {
						HashSet<Double> resS=new HashSet<Double>();
						
						 Vector<EqualityDescriptorsRule> edr=rtm.getAllRules();
						
						for (int i=1;i< edr.size();i++){
							resS.add((Double)(double)(edr.get(i).getRuleLength()));
						}
						
						res=new Vector<Double>(resS);
						Collections.sort(res);
												
					}
				});
		specialValueCreators.put(
				SortMainModel.ATRIB_ACC,
				new ValueListCreator(){
					void update() {
						HashSet<Double> resS=new HashSet<Double>();
						
						 Vector<EqualityDescriptorsRule> edr=rtm.getAllRules();
						
						for (int i=1;i< edr.size();i++){
							resS.add(edr.get(i).getAccuracy());
						}
						res=new Vector<Double>(resS);
						Collections.sort(res);
												
					}

				
				});
		specialValueCreators.put(
				SortMainModel.ATRIB_SUPPORT,
				new ValueListCreator(){
				
					void update() {
						HashSet<Double> resS=new HashSet<Double>();
						
						 Vector<EqualityDescriptorsRule> edr=rtm.getAllRules();
						
						for (int i=1;i< edr.size();i++){
							resS.add(edr.get(i).getSupport());
						}
						res=new Vector<Double>(resS);
						Collections.sort(res);
												
					}
				});		
			Iterator<ValueListCreator> j=specialValueCreators.values().iterator();
			while (j.hasNext()){
				ValueListCreator vlc=j.next();
				vlc.update();
	//			rtm.addTableModelListener(vlc);
			}

	}
	

    /**
     * All SelectModels register here
     */
	public void registerSelect(SelectModel sm){
		selMod.add(sm);
		
	}
    /**
     *  This method generates all possible values of given 
     *  attribute so that SelectModel can pass them to the combobox
     */
	public Vector<Double> valuesOf(Attribute a){
		
		
	
		if (specialValueCreators.containsKey(a)){
			return specialValueCreators.get(a).getValues();
		};		
		return values.get(rtm.getAttributes().indexOf(a));
		
	}
    /**
     * Checks if the selected value is 'normal' which means numeric, not 
     * 'any' or 'not empty'
     */
	static public boolean normalValueSelected(Double d){
		if (d==SelectMainModel.VAL_NONE)return false;
		if (d==SelectMainModel.VAL_NOT_NULL)return false;
				
		return true;
	}
    /**
     * contents changed, we need to reSelect table model
     *
     */
	public void contentsChanged(ListDataEvent arg0) {
	
		rtm.reSelect(getSelector());	
	}
	public void valueChanged(){
		this.fireIntervalAdded(this, 0,this.getSize() );

}


	public void intervalAdded(ListDataEvent arg0) {}

	public void intervalRemoved(ListDataEvent arg0) {}
	
    /**
     * We create complex Selector form simpler ones retireved from
     * SelectModel objects registered here
     */
	VectorMultipleRulesSelector getSelector(){
		VectorMultipleRulesSelector res=new VectorMultipleRulesSelector();
		Iterator<SelectModel> i=selMod.iterator();
		while (i.hasNext()){
			res.add(i.next().getSelector());
		};
		return res; 
	}
}

/**
 * Complex selector which chooses rule only if all it's subselectors choose 
 * the rule. Implements one big 'and' operator over the selectors. 
 *
 */
class VectorMultipleRulesSelector implements RulesSelector {

	private Vector<RulesSelector> rlist;
	VectorMultipleRulesSelector(){
		rlist=new Vector<RulesSelector>();
	};
	void add(RulesSelector rs){
	
		if (rs!=null)rlist.add(rs);
	}
	public boolean isChoosen(EqualityDescriptorsRule r) {
		Iterator<RulesSelector> i=rlist.iterator();		
		while (i.hasNext()){
			if (!(i.next().isChoosen(r)))return false;
		}
		return true;
		
	}
	public String toString(){
		return rlist.toString();
	}
}
/**
 * Simple abstract class for the special values creators, it is important that 
 * in fact those creators never update themselves even if table model changes as
 * possible values are shown for all possible rules not only the visible ones
 */
abstract class  ValueListCreator{	
	protected Vector<Double> res;
	Vector<Double> getValues(){return res;};

	/**
	 * 	
	 *called at the construction but as constructor cannot be written 
	 * for local classes, we have to call it separately
	*/
	abstract void update();
	
};



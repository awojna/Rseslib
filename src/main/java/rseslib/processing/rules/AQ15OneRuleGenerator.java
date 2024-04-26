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


package rseslib.processing.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.function.booleanval.BooleanFunction;
//import rseslib.structure.rule.BooleanFunctionRule;
import rseslib.structure.rule.BooleanFunctionRule;
import rseslib.structure.rule.Rule;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;


/**
 * AQ15RuleGenerator generates a rule.
 * Based on the AQ15 algorithm.
 *
 * @author	Cezary Tkaczyk
 */

public class AQ15OneRuleGenerator extends Configuration implements OneRuleGenerator{
	
	//TODO: Generowa� zbi�r ci�� przy urzyciu np. miary entropii. 
	//		Potem zamiast g�upio generowa� ci�cie, to korzysta� z tego zbioru.
	//		rseslib.processing.classification.tree.c45.BestGainRatioDiscriminationProvider
	//TODO: Petli sie przy sprzecznych przykladach
	//TODO: Optymalizacja jakas?
	
	int    m_nNoOfDescriptors;
	double m_nMargin;
	
	/**
     * Constructor.
     *
     * @param prop                Parameters of this rule generator. If null,
     *                            parameters are loaded from the configuration directory.
     */
	public AQ15OneRuleGenerator(Properties prop) throws PropertyConfigurationException {
		super(prop);
		m_nMargin    = getDoubleProperty("margin");
	}

	public Rule generate(
			DoubleDataTable examples, 
			ArrayList<DoubleDataWithDecision> uncovered, 
			int k, 
			double d) {
        
		m_nNoOfDescriptors = 0;
		for(int i=0; i<examples.attributes().noOfAttr(); i++) {
			if (examples.attributes().attribute(i).isConditional())
				m_nNoOfDescriptors++;
		}
		
        DoubleDataWithDecision e_pos =
        	e_posFind(uncovered, d);
        
        DoubleDataWithDecision e_neg;
        ArrayList<Candidate> candidates = new ArrayList<Candidate>();
        candidates.add(new Candidate());
        
        while (candidatesCoverNoExamples(e_pos, examples, candidates)) {
        //while (whileCondition) {
        	// e_neg = ...
    		e_neg = e_negFind(e_pos, examples, candidates);

    		// selectors
    		Selector[] selectors = 
    			getSelectorsPosConsientNegExcluding(e_pos, e_neg);
    		
    		// update candidates with selectors
    		candidates = updateCandidatesWithSelectors(candidates, selectors);
    		
    		// removing less general
    		candidates = removeLessGeneral(candidates);

    		// retain only k best candidates
    		countPerformance(candidates, e_pos.getDecision(), examples);
    		Collections.sort(candidates);
    		
    		for (int i=candidates.size()-1; i>=k; i--)
    			candidates.remove(i);
    		
        }
        
        //testSelectorsAndCandidates();
        
        Rule rule = null;
        if (candidates.size() > 0) {
        	rule = new BooleanFunctionRule(candidates.get(0), e_pos.getDecision(), examples.attributes().nominalDecisionAttribute());
        	//System.out.println("Rule" + candidates.get(0));
        }
        return rule;
	}

	private void countPerformance(
			ArrayList<Candidate> candidates, 
			double               dec, 
			DoubleDataTable      examples) {
		
		for (int i=0; i<candidates.size(); i++) {
			candidates.get(i).performance(dec, examples);
		}
	}
	
	private boolean candidatesCoverNoExamples(
			DoubleDataWithDecision e_pos, 
			DoubleDataTable        examples,
			ArrayList<Candidate>   candidates) {			
		
		for (DoubleData e_neg : examples.getDataObjects()) {
			if (((DoubleDataWithDecision)e_neg).getDecision() != e_pos.getDecision()) {
				for (int i = 0; i < candidates.size(); i++) {
					if (candidates.get(i).booleanVal(e_neg)) {
						return true;
					}
				}

			}
		}
		return false;
	}
	
	private ArrayList<Candidate> updateCandidatesWithSelectors(
			ArrayList<Candidate> candidates, Selector[] selectors) {
		
		ArrayList<Candidate> newCandidates = 
			new ArrayList<Candidate>(candidates.size());
		
		for(int i=0; i<selectors.length; i++) {
			Iterator<Candidate> it = candidates.iterator();
			for(;it.hasNext();) {
				Candidate candidate = it.next();
				try {
					Candidate newCandidate = candidate.deeperCopy();
					newCandidate.add(selectors[i].deeperCopy());
					if (!existsEqual(newCandidates, newCandidate))
						newCandidates.add(newCandidate);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return newCandidates;
	}
	
	private boolean existsEqual(ArrayList<Candidate> candidates, Candidate candidate) {
		Iterator<Candidate> it = candidates.iterator();
		Candidate next;
		for(;it.hasNext();) {
			next = it.next();
			int comparison = candidate.compareGenerality(next);
			if ((candidate != next) && (comparison == Selector.EQUAL))
				return true;
		}
		return false;
	}
	
	/** Retains only the most general candidates */
	private ArrayList<Candidate> removeLessGeneral(ArrayList<Candidate> candidates) {
		ArrayList<Candidate> newCandidates = new ArrayList<Candidate>();
		int ln = candidates.size();
		for(int i=0; i<ln; i++) {
			if (!existsMoreGeneral(candidates, candidates.get(i)))
				newCandidates.add(candidates.get(i));
		}
		return newCandidates;
	}
	
	/** Returns true if there exists more or equal general candidate 
	 *  in <code>candidates</code>. Considers only elements from idx to size().
	 */
	private boolean existsMoreGeneral(ArrayList<Candidate> candidates, int idx, Candidate candidate) {
		for(int i=idx; i < candidates.size(); i++) {
			int comparison = candidate.compareGenerality(candidates.get(i));
			if ((comparison == Selector.EQUAL) 
					|| (comparison == Selector.THAT_MORE_GENERAL)) {
				return true;
			}
		}
		return false;
	}
	
	/** Returns true if there exists more general candidate 
	 *  in <code>candidates</code>
	 */
	private boolean existsMoreGeneral(ArrayList<Candidate> candidates, Candidate candidate) {
		Iterator<Candidate> it = candidates.iterator();
		Candidate next;
		for(;it.hasNext();) {
			next = it.next();
			int comparison = candidate.compareGenerality(next);
			if ((candidate != next) && 
				(comparison == Selector.THAT_MORE_GENERAL)) {
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns array of Selectors consistent with e_pos excluding e_neg
	 * @param e_pos
	 * @param e_neg
	 * @return
	 */
	private Selector[] getSelectorsPosConsientNegExcluding(
			DoubleDataWithDecision e_pos, 
			DoubleDataWithDecision e_neg) {
		
		int dissimilarity = m_nNoOfDescriptors - similarity(e_pos, e_neg);
		Selector[] selectors = new Selector[dissimilarity];

		for (int i=0, j=0; i<m_nNoOfDescriptors; i++) {
			if (e_pos.get(i) != e_neg.get(i)) {
				if (e_pos.attributes().attribute(i).isNominal()) {
					selectors[j++] = new InequalitySelector((NominalAttribute)e_pos.attributes().attribute(i),i,e_neg.get(i));
				}
				if (e_pos.attributes().attribute(i).isNumeric()) {
					IntervalSelector selector =
						new IntervalSelector((NumericAttribute)e_pos.attributes().attribute(i), i,e_pos.get(i));
					selector.addValue(e_neg.get(i));
					selectors[j++] = selector;
				}
			}
		}
		return selectors;
	}

	private DoubleDataWithDecision e_posFind(
			ArrayList<DoubleDataWithDecision> examples,
			double	 						  dec) {
		int count  = examples.size();
		
		int random = (int)Math.round(Math.random()*count - 0.5d);
		if (random <0) random = 0;
		if (random > count-1) random = count-1;
		
		if (examples.size() == 0) return null;
		else return examples.get(random);
	}
	/**
	 * Finds random example with decision <code>dec</code>
	 */
	private DoubleDataWithDecision e_posFind(
			DoubleDataTable        examples,
			double				   dec) {
		
		int count  = 0;
		int random = 0;
		//ArrayList<DoubleDataWithDecision> candidates = new ArrayList<DoubleDataWithDecision>();
		for (DoubleData example : examples.getDataObjects()) {
			if (((DoubleDataWithDecision)example).getDecision() == dec)
				count++;
		}
		random = (int)Math.round(Math.random()*count - 0.5d);
		if (random <0) random = 0;
		if (random > count-1) random = count-1;
		
		count = -1;
		for (DoubleData example : examples.getDataObjects()) {
			if (((DoubleDataWithDecision)example).getDecision() == dec)
				count++;
			if (count == random)
				return (DoubleDataWithDecision)example;
		}
		return null;
	}
	private DoubleDataWithDecision e_negFind(
				DoubleDataWithDecision e_pos, 
				DoubleDataTable        examples,
				ArrayList<Candidate>   candidates) {
		
    	ArrayList<DoubleDataWithDecision> e_negCandidates = 
    		new ArrayList<DoubleDataWithDecision>(100);
		for (DoubleData e_neg : examples.getDataObjects()) {
    		if (((DoubleDataWithDecision)e_neg).getDecision() != e_pos.getDecision()) {
    			for (int i=0; i<candidates.size(); i++) {
    				if (candidates.get(i).booleanVal(e_neg)) {
    					e_negCandidates.add((DoubleDataWithDecision)e_neg);
    					break;
    				}
    			}
    			
    		}
    	}
    	
    	//find e_neg the most similar to e_pos
		DoubleDataWithDecision e_neg;
    	Iterator e_negIterator = e_negCandidates.iterator();
    	DoubleDataWithDecision e_negBest = e_negCandidates.get(0);
    	int e_negBestSimilarity = similarity(e_negBest, e_pos);
    	while   (e_negIterator.hasNext()) {
    		e_neg = (DoubleDataWithDecision) e_negIterator.next();

			int similarity = similarity(e_neg, e_pos);

    		if (e_negBestSimilarity < similarity) {
    			e_negBestSimilarity = similarity;
    			e_negBest           = e_neg;
    		}
    	}
		return e_negBest;
	}
	
	private int similarity (DoubleDataWithDecision e_pos,
							 DoubleDataWithDecision e_neg) {
		int similarity = 0;
		for(int i = 0; i < m_nNoOfDescriptors; i++) {
			if (e_neg.get(i) == e_pos.get(i)) similarity++; 
		}
		return similarity;
	}
	
	
	class Candidate implements BooleanFunction, Comparable {
		private Selector[] m_nSelectors;
		private int        m_nPerformance;
		
		Candidate() {
			m_nSelectors = new Selector[m_nNoOfDescriptors];
		}
		
		public void set(Selector selector) {
			set(selector.getAttrIndex(), selector);
		}
		
		private void set(int idx, Selector selector) {
			m_nSelectors[idx] = selector;
		}
		
		public void add(Selector selector) {
			add(selector.getAttrIndex(),selector);
		}
		
		private void add(int idx, Selector selector) {
			if (m_nSelectors[idx] == null)
				set(idx,selector);
			m_nSelectors[idx].addAll(selector);
		}
			
		public int compareGenerality(Candidate candidate) {
			Selector[] thisSelectors = m_nSelectors;
			Selector[] thatSelectors = candidate.m_nSelectors;
			int thisMinusThat = 0;
			int thatMinusThis = 0;
			boolean possibleThisMoreGeneral = false;
			boolean possibleThatMoreGeneral = false;
			for (int i = 0; i < thisSelectors.length; i++) {
				if ((thisSelectors[i] == null) && (thatSelectors[i] == null)) {
				} else if (thisSelectors[i] == null) {
					thatMinusThis++;
				} else if (thatSelectors[i] == null) {
					thisMinusThat++;
				} else {
					int comparison = thisSelectors[i].compareGenerality(thatSelectors[i]);
					switch (comparison) {
					case Selector.THIS_MORE_GENERAL:
						possibleThisMoreGeneral = true;
						break;
					case Selector.THAT_MORE_GENERAL:
						possibleThatMoreGeneral = true;
						break;
					case Selector.EQUAL:
						break;
					case Selector.NOT_COMPARABLE:
						return Selector.NOT_COMPARABLE;
						//possibleThisMoreGeneral = true;						
						//possibleThatMoreGeneral = true;
					}
				}
			}

			if ((thisMinusThat > 0) && (thatMinusThis == 0) 
					&& (!possibleThatMoreGeneral))
				return Selector.THIS_MORE_GENERAL;
			else if ((thisMinusThat == 0) && (thatMinusThis > 0)
					&& (!possibleThisMoreGeneral))
				return Selector.THAT_MORE_GENERAL;
			else if ((thisMinusThat == 0) && (thatMinusThis == 0))
				if ((!possibleThisMoreGeneral) && (!possibleThatMoreGeneral))
					return Selector.EQUAL;
				else if ((!possibleThisMoreGeneral) && (possibleThatMoreGeneral))
					return Selector.THAT_MORE_GENERAL;
				else if ((possibleThisMoreGeneral) && (!possibleThatMoreGeneral))
					return Selector.THIS_MORE_GENERAL;
				else
					return Selector.NOT_COMPARABLE;
			else
				return Selector.NOT_COMPARABLE;
		}

		public int compareGenerality2(Candidate candidate) {
			Selector[] thisSelectors = m_nSelectors;
			Selector[] thatSelectors = candidate.m_nSelectors;
			int thisMinusThat = 0;
			int thatMinusThis = 0;			
			for (int i = 0; i < thisSelectors.length; i++) {
				if ((thisSelectors[i] == null) && (thatSelectors[i] == null)) {
				} else if (thisSelectors[i] == null) {
					thatMinusThis++;
				} else if (thatSelectors[i] == null) {
					thisMinusThat++;
				} else {
					int comparison = thisSelectors[i].compareGenerality(thatSelectors[i]);
					switch (comparison) {
					case Selector.THIS_MORE_GENERAL:
						thisMinusThat++;
						break;
					case Selector.THAT_MORE_GENERAL:
						thatMinusThis++;
						break;
					case Selector.EQUAL:
						break;
					case Selector.NOT_COMPARABLE:
						return Selector.NOT_COMPARABLE;
					}
				}
			} //for
			
			if ((thisMinusThat > 0) && (thatMinusThis == 0))
				return Selector.THIS_MORE_GENERAL;
			else if ((thisMinusThat == 0) && (thatMinusThis > 0))
				return Selector.THAT_MORE_GENERAL;
			else if ((thisMinusThat == 0) && (thatMinusThis == 0))
				return Selector.EQUAL;
			else
				return Selector.NOT_COMPARABLE;
		}
		
		public boolean booleanVal(DoubleData dObj) {
			for (int i=0; i<m_nSelectors.length; i++) {
				if (m_nSelectors[i] != null) {
					if (!m_nSelectors[i].booleanVal(dObj)) return false;
				}
			}
			return true;
		}
		
		private int performance(double dec, DoubleDataTable examples) {
			// e_pos_included + e_neg_excluded
			int e_posIncluded = 0;
			int e_negExcluded = 0;
			for (DoubleData example : examples.getDataObjects()) {
				if (this.booleanVal(example)) {
					if (((DoubleDataWithDecision)example).getDecision() == dec)
						e_posIncluded++;
				}
				else {
					if (((DoubleDataWithDecision)example).getDecision() != dec)
						e_negExcluded++;
				}
			}
			
			m_nPerformance = e_posIncluded + e_negExcluded; 
			return m_nPerformance;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for (int i=0; i<m_nSelectors.length; i++) {
				if (m_nSelectors[i] != null) {
					if (!first)
						sb.append("  &  ");
					sb.append(m_nSelectors[i]);
					first = false;
				}
			}
			return sb.toString();
		}
		
		/** Makes deeper copy than clone(). It doesn't deep copy arrayOfDescriptors */
		public Candidate deeperCopy() {
			Candidate nw = new Candidate();
			for(int i=0;i<m_nSelectors.length; i++) {
				if (m_nSelectors[i] != null)
					nw.m_nSelectors[i] = m_nSelectors[i].deeperCopy();
			}
			return nw;
		}

		public int compareTo(Object o) throws ClassCastException {
			Candidate that = (Candidate) o;
			return -(this.m_nPerformance - that.m_nPerformance); //TODO: by�o bez -
		}
	}//class Candidate
	
	
	abstract class Selector implements BooleanFunction, Comparable {
		
		public static final int THIS_MORE_GENERAL = 0;
		public static final int THAT_MORE_GENERAL = 1;
		public static final int EQUAL             = 2;
		public static final int NOT_COMPARABLE    = 3;
		
		/** The index of an attribute to be tested. */
	    int m_nAttributeIndex;

	    Selector(int attrIndex) {
	    	setAttrIndex(attrIndex);
	    }
	    
		public void setAttrIndex(int attrIndex) {
			this.m_nAttributeIndex = attrIndex;
		}

		public int  getAttrIndex() {
			return m_nAttributeIndex;
		}
	    
		public int compareTo(Object o) throws ClassCastException {
			Selector selector = (Selector) o; //this may throw excption
			return (m_nAttributeIndex - selector.m_nAttributeIndex);
		}		
		
		abstract public int compareGenerality(Selector selector);
		
		abstract public void addValue(double val);
		
		abstract public void addAll(Selector selector);
		
		abstract public boolean booleanVal(DoubleData dObj);
		
		/** Makes deeper copy than clone(). Actually, now it is deep copy. */
		abstract public Selector deeperCopy();
	}
	
	class InequalitySelector extends Selector {
		
		/** Attribute of this selector */
		NominalAttribute m_Attr;
	    /** The value to be compared. */
	    ArrayList<Double> m_nAttributeValues;
	    
	    InequalitySelector(NominalAttribute attr, int attrIndex) {
	    	super(attrIndex);
	    	m_Attr = attr;
	    	m_nAttributeValues = new ArrayList<Double>();
	    }
	    
	    InequalitySelector(NominalAttribute attr, int attrIndex, double attrValue) {
	    	super(attrIndex);	    	
	    	m_Attr = attr;
	    	m_nAttributeValues = new ArrayList<Double>();
	    	addValue(attrValue);
	    }
	    
		public boolean booleanVal(DoubleData dObj) {
			boolean  match   = true;
			Iterator it      = m_nAttributeValues.iterator();
			Double   dObjVal = new Double(dObj.get(m_nAttributeIndex));
			for(;it.hasNext();) {
				if (dObjVal.equals(it.next()))	{
					match = false;
					break;
				}
			}
			return match;
		}
		
		
	    public void addValue(double val) {
	    	m_nAttributeValues.add(val);
	    	Collections.sort(m_nAttributeValues);
	    	makeUniqueArrayListDouble(m_nAttributeValues);
	    }

	    public void addAll(Selector sel) throws ClassCastException {
	    	InequalitySelector selector = (InequalitySelector) sel;
	    	m_nAttributeValues.addAll(selector.m_nAttributeValues);
	    	Collections.sort(m_nAttributeValues);
	    	makeUniqueArrayListDouble(m_nAttributeValues);	    	
	    }
	    
	    /** Removes not unique elements. Assumes, that list is sorted. */
	    public void makeUniqueArrayListDouble(ArrayList<Double> list) {
	    	if (list.size() != 1) {
	    		Double last  = list.get(0);
				Double[]tmp  = list.toArray(new Double[0]);
				list.clear();

				list.add(last);
				for (int i = 1; i < tmp.length; i++) {
					if (!last.equals(tmp[i])) {
						last = tmp[i];
						list.add(last);
					}
				}
	    	}
	    }
	    
	    public int compareGenerality(Selector selector) throws ClassCastException{
	    	InequalitySelector thatSelector = (InequalitySelector) selector;
	    	if (m_nAttributeIndex != thatSelector.m_nAttributeIndex) {
	    		return NOT_COMPARABLE;
	    	} else {
	    		double[] thisValues = new double[m_nAttributeValues.size()+1];
	    		for (int i=0; i<m_nAttributeValues.size(); i++) {
	    			thisValues[i] = m_nAttributeValues.get(i).doubleValue();
	    		}
	    		thisValues[m_nAttributeValues.size()] = Double.MAX_VALUE;
	    		
	    		double[] thatValues = new double[thatSelector.m_nAttributeValues.size()+1]; 
	    		for (int i=0; i<thatSelector.m_nAttributeValues.size(); i++) {
	    			thatValues[i] = thatSelector.m_nAttributeValues.get(i).doubleValue();
	    		}
	    		thatValues[thatSelector.m_nAttributeValues.size()] = Double.MAX_VALUE;
	    		
	    		int thisMinusThat = 0;
	    		int thatMinusThis = 0;
	    		for(int i=0, j=0; (i<thisValues.length-1) || (j<thatValues.length-1);) {
	    			if (thisValues[i] < thatValues[j]) {
	    				i++;
	    				thisMinusThat++;
	    			}
	    			else if (thisValues[i] > thatValues[j]) {
	    				j++;
	    				thatMinusThis++;
	    			}
	    			else {
	    				i++; j++;
	    			}
	    		}

	    		if ((thisMinusThat> 0) && (thatMinusThis == 0)) 
	    			return THIS_MORE_GENERAL;
	    		else if ((thisMinusThat == 0) && (thatMinusThis > 0))
	    			return THAT_MORE_GENERAL;
	    		else if ((thisMinusThat == 0) && (thatMinusThis == 0))
	    			return EQUAL;
	    		else 
	    			return NOT_COMPARABLE;
	    	}
	    }


		
		
	    /** Makes deeper copy than clone(). Actually, now it is deep copy. */
	    @Override
		public InequalitySelector deeperCopy() {
			InequalitySelector nw = new InequalitySelector(m_Attr, m_nAttributeIndex);
			for(int i=0; i<m_nAttributeValues.size();i++) {
				nw.m_nAttributeValues.add(new Double(m_nAttributeValues.get(i)));
			}
			return nw;
		}
		
	    public String toString() {
	    	StringBuffer sb = new StringBuffer();
	    	sb.append(m_Attr.name() + " <> ");
	    	boolean first = true;
	    	for (double d : m_nAttributeValues) {
	    		if (first)
	    			sb.append(NominalAttribute.stringValue(d));
	    		else
	    			sb.append(","+NominalAttribute.stringValue(d));
	    		first = false;
	    	}
	    	return sb.toString();
	    }

	}//class InequalitySelector
	
	class IntervalSelector extends Selector {
	    
		NumericAttribute m_Attr;
	    Double m_nLeftBound;
	    Double m_nRightBound;
	    Double m_nPositiveExample;
	    
	    	    
	    IntervalSelector(NumericAttribute attr, int attrIndex, double positiveExample) {
	    	super(attrIndex);
	    	m_Attr = attr;
	    	m_nLeftBound  = Double.NEGATIVE_INFINITY;
	    	m_nRightBound = Double.POSITIVE_INFINITY;
	    	m_nPositiveExample = positiveExample;
	    }

	    public void addValue(double val) {
	    	//addValueWithoutMargin(val);
	    	addValueWithMargin(val, m_nMargin);
	    }
	    
	    private void addValueWithoutMargin(double val) {
	    	if ((val > m_nLeftBound) && (val < m_nRightBound)) {
	    		if (val < m_nPositiveExample)
	    			m_nLeftBound = val;
	    			
	    		if (val > m_nPositiveExample)
	    			m_nRightBound = val;
	    	}
	    }
	    
	    /**
	     * @param part is in [0;1], the percent size of margin
	     */
	    private void addValueWithMargin(double val, double part) {
	    	if ((val + (m_nPositiveExample - val)*part > m_nLeftBound) 
	    	&&  (val - (val - m_nPositiveExample)*part < m_nRightBound)) {
	    		if (val < m_nPositiveExample)
	    			m_nLeftBound = val + (m_nPositiveExample - val)*part;
	    			
	    		if (val > m_nPositiveExample)
	    			m_nRightBound = val - (val - m_nPositiveExample)*part;
	    	}
	    }
	    
	    public void addAll(Selector sel) throws ClassCastException {
	    	IntervalSelector selector = (IntervalSelector) sel;
	    	this.addValue(selector.m_nLeftBound);
	    	this.addValue(selector.m_nRightBound);
	    }

	    public int compareGenerality(Selector selector) throws ClassCastException {
	    	IntervalSelector thatSelector = (IntervalSelector) selector;
	    	
	    	if ((compareDoubles(this.m_nLeftBound, thatSelector.m_nLeftBound) == 0)
	    		&&
	    		(compareDoubles(this.m_nRightBound, thatSelector.m_nRightBound) == 0))
	    		return EQUAL;
	    	
	    	if ((compareDoubles(this.m_nLeftBound, thatSelector.m_nLeftBound) <= 0)
	    		&&
	    		(compareDoubles(this.m_nRightBound, thatSelector.m_nRightBound) >= 0))
	    		return THAT_MORE_GENERAL;
	    	

	    	if ((compareDoubles(this.m_nLeftBound, thatSelector.m_nLeftBound) >= 0)
	    		&&
	    		(compareDoubles(this.m_nRightBound, thatSelector.m_nRightBound) <= 0))
	    		return THIS_MORE_GENERAL;

	    	return NOT_COMPARABLE;
	    }
	    
	    private Double compareDoubles(Double a, Double b) {
	    	if (a.isInfinite() && b.isInfinite()) return new Double(0.0);
	    	else return a-b;
	    }
	    
		public boolean booleanVal(DoubleData dObj) {
			// maybe change Interval:
			// [m_nLeftBound + (positiveExample - m_nLeftBound)/2, etc.]
			Double   dObjVal = new Double(dObj.get(m_nAttributeIndex));
			if ((dObjVal > m_nLeftBound) && (dObjVal < m_nRightBound))
				return true;
			//if ((dObjVal > m_nLeftBound + Math.abs(m_nPositiveExample - m_nLeftBound)/2)
			//	&&	(dObjVal < m_nRightBound - Math.abs(m_nPositiveExample - m_nRightBound)/2))
			//	return true;
			return false;
		}
		
		@Override
		public String toString() {
	    	return m_Attr.name() + " in (" + m_nLeftBound + ";" + m_nRightBound + ")";
	    }

		@Override
		public IntervalSelector deeperCopy() {
			IntervalSelector nw = new IntervalSelector(m_Attr, m_nAttributeIndex, m_nPositiveExample);
			nw.m_nLeftBound     = this.m_nLeftBound;
			nw.m_nRightBound    = this.m_nRightBound;
			return nw;			
		}
	}//class IntervalSelector
}

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


package rseslib.processing.discernibility;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import rseslib.processing.discernibility.ClassicGeneralizedDecisionProvider;
import rseslib.processing.discernibility.GeneralizedDecisionProvider;
import rseslib.processing.discernibility.TransitiveClosureGeneralizedDecisionProvider;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.indiscernibility.ClassicIndiscernibility;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.indiscernibility.NonsymmetricSimilarityIndiscernibility;
import rseslib.structure.indiscernibility.SymmetricSimilarityIndiscernibility;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Provider of a discernibility matrix for a given data table.
 * It can compute the discernibility matrix discerning
 * either all pairs of data objects in the table
 * or the objects with different decisions only.
 * In case of a decision-related discernibility matrix
 * it can discern ordinary decisions, generalized decisions
 * or the objects having both decision types different.
 * The provider can compute the whole discernibility matrix
 * or the discernibility rows related to particular objects.  
 * 
 * @author Rafal Latkowski
 */
public class DiscernibilityMatrixProvider extends Configuration
{
	/** Indiscernibility relation types for missing values. */
	public enum IndiscernibilityRelation { DiscernFromValue, DiscernFromValueOneWay, DontDiscernFromValue; };
	/** Types of discernibility matrix. */
	public enum DiscernibilityMethod { All, GeneralizedDecision, GeneralizedDecisionAndOrdinaryChecked, OrdinaryDecisionAndInconsistenciesOmitted; };
	
    /** Parameter name for the indiscernibility relation used for missing values. */
	public static final String s_sIndiscernibilityRelation = "IndiscernibilityForMissing";
	/** Parameter name for the type of discernibility matrix. */
    public static final String s_sDiscernibilityMethod = "DiscernibilityMethod";
	/** Parameter name for the switch controlling whether transitive closure of the generalized decision is used. */
    public static final String s_sGeneralizedDecisionTransitiveClosure = "GeneralizedDecisionTransitiveClosure";

    /** Indiscernibility relation type for missing values. */
    Indiscernibility m_indiscernibility;
    /** Type of discernibility matrix defining which pairs of objects are discerned. */
    DiscernibilityMethod m_nDiscernibilityMethod;
    /** Checker used only with generalized decision to check whether a given pair of objects have the same decision. */
    GeneralizedDecisionProvider m_nGeneralizedDecisionProvider;
    /** Header of data. */
    Header m_Header;
    /** Objects for which the discernibility matrix is computed. */
    Collection<DoubleData> m_Objects;
    
    /**
     * Constructor takes the data table for which the discernibility matrix is computed.  
     *
     * @param prop		Parameters of the discernibility matrix.
     * @param table		Data table for which the discernibility matrix is computed.
     * @throws PropertyConfigurationException	when the parameters are incorrect or incomplete.
     */
    public DiscernibilityMatrixProvider(Properties prop, DoubleDataTable table) throws PropertyConfigurationException
    {
        super(prop);
        setIndiscernibilityRelation(getProperty(s_sIndiscernibilityRelation));
        setDiscernibilityMethod(getProperty(s_sDiscernibilityMethod));
        if (m_nDiscernibilityMethod==DiscernibilityMethod.GeneralizedDecision
        	|| m_nDiscernibilityMethod==DiscernibilityMethod.GeneralizedDecisionAndOrdinaryChecked)
        	setGeneralizedDecisionProvider(getBoolProperty(s_sGeneralizedDecisionTransitiveClosure), table);
        m_Header = table.attributes();
        m_Objects = table.getDataObjects();
    }

    /**
     * Creates the indiscernibility relation for missing values.
     *
     * @param aIndiscernibilityRelation		Name of the indiscernibility relation type to be created.
     * @throws PropertyConfigurationException	when the name of the indiscernibility relation type is invalid.
     */
    private void setIndiscernibilityRelation(String aIndiscernibilityRelation) throws PropertyConfigurationException
    {
    	try
    	{
        	switch (IndiscernibilityRelation.valueOf(aIndiscernibilityRelation))
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
			throw new PropertyConfigurationException("Unknown indiscernibility relation for mising values: "+aIndiscernibilityRelation);
        }
    }

    /**
     * Sets the type of the discernibility matrix defining which pairs of objects are discerned.
     *
     * @param aDiscernibilityMethod		Name of the type of the discernibility matrix to be computed.
     * @throws PropertyConfigurationException	when the name of the type of the discernibility matrix is invalid.
     */
    private void setDiscernibilityMethod(String aDiscernibilityMethod) throws PropertyConfigurationException
    {
    	try
    	{
    		m_nDiscernibilityMethod = DiscernibilityMethod.valueOf(aDiscernibilityMethod);
    	}
    	catch (IllegalArgumentException e)
    	{
			throw new PropertyConfigurationException("Unknown discernibility method: "+aDiscernibilityMethod);
        }
    }
    
    /**
     * Creates the generalized decision checker.
     *
     * @param transitiveClosure		Switch controlling whether transitive closure of the generalized decision is used.
     */
    private void setGeneralizedDecisionProvider(boolean transitiveClosure, DoubleDataTable table)
    {
    	if (transitiveClosure)
    		m_nGeneralizedDecisionProvider = new TransitiveClosureGeneralizedDecisionProvider(table, m_indiscernibility);
    	else
    		m_nGeneralizedDecisionProvider = new ClassicGeneralizedDecisionProvider(table, m_indiscernibility);
    }

    /**
     * Returns the discernibility matrix of the data table passed to the constructor.
     * Each element of the result is the set of attributes
     * discerning a certain pair (or pairs) of objects from the table,
     * represented by a BitSet object.
     * get(i) returns true if and only if the i-th attribute discerns the pair(s) of objects.
     * The attribute indices are defined by the header of the table.
     *
     * @return	Discernibility matrix.
     */
    public Collection<BitSet> getDiscernibilityMatrix()
    {
        HashSet<BitSet> discern_attrs = new HashSet<BitSet>();
        for (DoubleData object : m_Objects)
        	addDiscernibility(discern_attrs, object);
        return discern_attrs;
    }
    
    /**
     * Returns the discernibility row related to a given data object.
     * Each element of the result is the set of attributes discerning the provided object
     * from a certain object (or objects) from the data table passed to the constructor,
     * represented by a BitSet object.
     * get(i) returns true if and only if the i-th attribute discerns the provided object
     * from the object(s) from the table.
     * The attribute indices are defined by the header of the table.
     *
     * @param object		Object which the discernibility row is computed for.
     * @return	Discernibility row related to a given data object.
     */
    public Collection<BitSet> getLocalDiscernibility(DoubleData object)
    {
        HashSet<BitSet> discern_attrs = new HashSet<BitSet>();
       	addDiscernibility(discern_attrs, object);
        return discern_attrs;
    }

    /**
     * Adds the sets of attributes discerning a given object
     * from the objects from the data table passed to the constructor
     * to an existing collection.
     *    
     * @param discern_attrs		Collection which the sets of attributes discerning a given object are added to.
     * @param object			Object which the added sets of attributes are discerning from the objects from the table.  
     */
    private void addDiscernibility(Collection<BitSet> discern_attrs, DoubleData object)
    {
    	DoubleDataWithDecision objectWithDec = null;
        if (m_nDiscernibilityMethod==DiscernibilityMethod.GeneralizedDecisionAndOrdinaryChecked
            	|| m_nDiscernibilityMethod==DiscernibilityMethod.OrdinaryDecisionAndInconsistenciesOmitted)
        	objectWithDec = (DoubleDataWithDecision)object;
    	BitSet bv;
        for (DoubleData dd : m_Objects)
        {
        	switch (m_nDiscernibilityMethod)
        	{
        	case All:
        		bv = new BitSet(m_Header.noOfAttr());
        		for (int a=0; a<m_Header.noOfAttr(); a++)
        			if (m_Header.isConditional(a) &&
        					!m_indiscernibility.similar(object.get(a),dd.get(a),a))
        				bv.set(a);
        		if (!bv.isEmpty()) discern_attrs.add(bv);
        		break;
        	case GeneralizedDecision:
        		if (!m_nGeneralizedDecisionProvider.haveTheSameDecision(object,dd))
        		{
        			bv = new BitSet(m_Header.noOfAttr());
        			for (int a=0; a<m_Header.noOfAttr(); a++)
        				if (m_Header.isConditional(a) &&
        						!m_indiscernibility.similar(object.get(a),dd.get(a),a))
        					bv.set(a);
        			if (!bv.isEmpty()) discern_attrs.add(bv);
        		}
        		break;
        	case GeneralizedDecisionAndOrdinaryChecked:
        		if (objectWithDec.getDecision()!=((DoubleDataWithDecision)dd).getDecision()
        				&& !m_nGeneralizedDecisionProvider.haveTheSameDecision(object,dd))
        		{
        			bv = new BitSet(m_Header.noOfAttr());
        			for (int a=0; a<m_Header.noOfAttr(); a++)
        				if (m_Header.isConditional(a) &&
        						!m_indiscernibility.similar(object.get(a),dd.get(a),a))
        					bv.set(a);
        			if (!bv.isEmpty()) discern_attrs.add(bv);
        		}
        		break;
        	case OrdinaryDecisionAndInconsistenciesOmitted:
        		if (objectWithDec.getDecision()!=((DoubleDataWithDecision)dd).getDecision())
        		{
        			bv = new BitSet(m_Header.noOfAttr());
        			for (int a=0; a<m_Header.noOfAttr(); a++)
        				if (m_Header.isConditional(a) &&
        						!m_indiscernibility.similar(object.get(a),dd.get(a),a))
        					bv.set(a);
        			if (!bv.isEmpty()) discern_attrs.add(bv);
        		}
        		break;
        	}
        }
    }

    /**
     * Returns the indiscernibility relation used for missing values.
     *
     * @return	Indiscernibility relation.
     */
    public Indiscernibility getIndiscernibilityForMissing()
    {
    	return m_indiscernibility;
    }
}

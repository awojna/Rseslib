/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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
 * @author Rafal Latkowski
 *
 */
public class DiscernibilityMatrixProvider extends Configuration
{
	public enum IndiscernibilityRelation { DiscernFromValue, DiscernFromValueOneWay, DontDiscernFromValue; };
	public enum DiscernibilityMethod { All, GeneralizedDecision, GeneralizedDecisionAndOrdinaryChecked, OrdinaryDecisionAndInconsistenciesOmitted; };
	
	public static final String s_sIndiscernibilityRelation = "IndiscernibilityForMissing";
    public static final String s_sDiscernibilityMethod = "DiscernibilityMethod";
    public static final String s_sGeneralizedDecisionTransitiveClosure = "GeneralizedDecisionTransitiveClosure";

    Indiscernibility m_indiscernibility;
    /**
     * select used discernibility method
     */
    DiscernibilityMethod m_nDiscernibilityMethod;
    GeneralizedDecisionProvider m_nGeneralizedDecisionProvider;
    Header m_Header;
    Collection<DoubleData> m_Objects;
    
    /**
     * @throws PropertyConfigurationException 
     * 
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
        //System.out.println("Prop: ");
        //prop.list(System.out);
    }

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
        //System.out.println("Setting "+s_sIndiscernibilityRelation+" = "+aIndiscernibilityRelation);
    }

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
        //System.out.println("Setting "+s_sGeneralizedDecisionMethod+" = "+aGeneralizedDecisionMethod);
    }
    
    private void setGeneralizedDecisionProvider(boolean transitiveClosure, DoubleDataTable table) throws PropertyConfigurationException
    {
    	if (transitiveClosure)
    		m_nGeneralizedDecisionProvider = new TransitiveClosureGeneralizedDecisionProvider(table, m_indiscernibility);
    	else
    		m_nGeneralizedDecisionProvider = new ClassicGeneralizedDecisionProvider(table, m_indiscernibility);
    }

    public Collection<BitSet> getDiscernibilityMatrix()
    {
        HashSet<BitSet> discern_attrs = new HashSet<BitSet>();
        for (DoubleData object : m_Objects)
        	addDiscernibility(discern_attrs, object);
        return discern_attrs;
    }
    
    public Collection<BitSet> getLocalDiscernibility(DoubleData object)
    {
        HashSet<BitSet> discern_attrs = new HashSet<BitSet>();
       	addDiscernibility(discern_attrs, object);
        return discern_attrs;
    }
    
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

    public Indiscernibility getIndiscernibilityForMissing()
    {
    	return m_indiscernibility;
    }
}

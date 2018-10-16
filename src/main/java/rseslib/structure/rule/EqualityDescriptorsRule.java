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

package rseslib.structure.rule;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.BitSet;

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.indiscernibility.Indiscernibility;

/**
 * A rule in the form of equality descriptors conjuction.
 * It enables to define different modes of missing values indiscernibility.
 * 
 * @author Rafal Latkowski
 */
public class EqualityDescriptorsRule extends AbstractDistrDecRuleWithStatistics implements PartialMatchingRule
{
	/** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/** Rule header. */
    Header m_Header;
	/** Mask of attributes that have descriptors in this rule. */
    boolean[] m_bPresenceOfDescriptor = null;
    /** Attribute values in the rule descriptors. */ 
    double[] m_nValueOfDescriptor = null;
    /** Indiscernibility relation between attribute values. */
    Indiscernibility m_indiscernibility = null;
    /** Number of descriptors in this rule. */
    int m_nRuleLength;
    
    /**
     * Constructor.
     * 
     * @param reduct	Reduct defining attributes to be used in descriptors.
     * @param object	Objects providing attribute values in descriptors.
     */
    public EqualityDescriptorsRule(BitSet reduct, DoubleData object)
    {
        m_Header = object.attributes();
        m_bPresenceOfDescriptor = new boolean[m_Header.noOfAttr()];
        m_nValueOfDescriptor = new double[m_Header.noOfAttr()];
        m_nRuleLength=0;
        for (int i=0; i<m_Header.noOfAttr(); i++)
        	if (m_Header.isConditional(i) && reduct.get(i))
        	{
        		m_bPresenceOfDescriptor[i]=true;
        		m_nValueOfDescriptor[i]=object.get(i);
        		m_nRuleLength++;
        	}
        	else
        		m_bPresenceOfDescriptor[i]=false;
    }

    /**
     * Constructor.
     * 
     * @param reduct			Reduct defining attributes to be used in descriptors.
     * @param object			Objects providing attribute values in descriptors.
     * @param indiscernibility	Indiscernibility mode for missing values.
     */
    public EqualityDescriptorsRule(BitSet reduct, DoubleData object, Indiscernibility indiscernibility)
    {
    	this(reduct, object);
        m_indiscernibility = indiscernibility;
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(m_Header);
    	out.writeObject(m_bPresenceOfDescriptor);
    	for (int att = 0; att < m_bPresenceOfDescriptor.length; att++)
    		if (m_bPresenceOfDescriptor[att])
    		{
    			if (m_Header.isNominal(att))
    				out.writeInt(((NominalAttribute)m_Header.attribute(att)).localValueCode(m_nValueOfDescriptor[att]));
    			else
    				out.writeDouble(m_nValueOfDescriptor[att]);
    		}
    	out.writeObject(m_indiscernibility);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_Header = (Header)in.readObject();
    	m_bPresenceOfDescriptor = (boolean[])in.readObject();
        m_nValueOfDescriptor = new double[m_bPresenceOfDescriptor.length];
        m_nRuleLength = 0;
    	for (int att = 0; att < m_bPresenceOfDescriptor.length; att++)
    		if (m_bPresenceOfDescriptor[att])
    		{
    			if (m_Header.isNominal(att))
    				m_nValueOfDescriptor[att] = ((NominalAttribute)m_Header.attribute(att)).globalValueCode(in.readInt());
        		else
        			m_nValueOfDescriptor[att] = in.readDouble();
    			m_nRuleLength++;
    		}
    	m_indiscernibility = (Indiscernibility)in.readObject();
    }

    /**
     * Checks whether this rule matches a given data object.
     *
     * @param dObj Double data to be matched.
     * @return     True if this rule matches to a data object, false otherwise.
     * @see rseslib.structure.rule.AbstractDistrDecRuleWithStatistics#matches(rseslib.structure.data.DoubleData)
     */
    public boolean matches(DoubleData dObj)
    {
    	if (m_indiscernibility==null)
    	{
    		for (int i=0;i<m_bPresenceOfDescriptor.length;i++)
    			if (m_bPresenceOfDescriptor[i]
    			    && !(Double.isNaN(m_nValueOfDescriptor[i]) && Double.isNaN(dObj.get(i)))
    			    && !(m_nValueOfDescriptor[i]==dObj.get(i)))
    					return false;                
    		return true;
    	}
    	else
    	{
    		for (int i=0;i<m_bPresenceOfDescriptor.length;i++)
    			if (m_bPresenceOfDescriptor[i] && !m_indiscernibility.similar(m_nValueOfDescriptor[i],dObj.get(i),i))
    				return false;                
    		return true;
    	}
    }

    /**
     * Defines how this rule and a given object match.
     *
     * @param dObj Double data to be matched.
     * @return     The value between 0 and 1.
     *             The values near 1 means that
     *             the object dObj matches this rule quite well.
     * @see rseslib.structure.rule.PartialMatchingRule#matchesPartial(rseslib.structure.data.DoubleData)
     */
    public double matchesPartial(DoubleData dObj)
    {
        double count=0;
        double satisfied=0;
    	if (m_indiscernibility==null)
    		for (int i=0; i<m_bPresenceOfDescriptor.length; i++)
    			if (m_bPresenceOfDescriptor[i])
    			{
    				count++;
    				if ((Double.isNaN(m_nValueOfDescriptor[i]) && Double.isNaN(dObj.get(i)))
    					|| m_nValueOfDescriptor[i]==dObj.get(i))
    						satisfied++;
    			}
    	else
    		for (i=0;i<m_bPresenceOfDescriptor.length;i++)
    			if (m_bPresenceOfDescriptor[i])
    			{
    				count++;
    				if (m_indiscernibility.similar(m_nValueOfDescriptor[i],dObj.get(i),i))
    					satisfied++;
    			}
        return satisfied/count;
    }

    /**
     * Returns the number of descriptors in this rule.
     * 
     * @return Number of descriptors in this rule.
     */
    public int getRuleLength()
    {
    	return m_nRuleLength; 
    }

    /**
     * Return true if this rule contains a descriptor for a given attribute.
     * 
     * @param i		Attribute index.
     * @return		True if this rule contains a descriptor for the attribute i
     * 				false otherwise.
     */
    public boolean hasDescriptor(int i)
    {
        return m_bPresenceOfDescriptor[i];
    }

    /**
     * Returns the value from the descriptor of a given attribute.
     * 
     * @param i		Attribute index.
     * @return		Value from the descriptor of a given attribute.
     */
    public double getDescriptor(int i)
    {
        if (m_bPresenceOfDescriptor[i])
            return m_nValueOfDescriptor[i];
        else
            return Double.NaN;
    }

    /**
     * True if this rule contains a descriptor with the missing value.
     * 
     * @return	True if this rule contains a descriptor with the missing value
     * 			false otherwise.
     */
    public boolean hasDescriptorWithMissingValue()
    {
        for (int i=0; i<m_bPresenceOfDescriptor.length; i++)
            if (m_bPresenceOfDescriptor[i] && Double.isNaN(m_nValueOfDescriptor[i]))
            	return true;
        return false;
    }

    /**
     * Returns true if parameter object is an equivalent simple decision rule.
     * Warning: only descriptor presence and values are compared. Other parts
     * remain unchecked.
     * 
     * @return true if parameter object is an equivalent simple decision rule
     */
    public boolean equals(Object o)
    {
        if (o instanceof EqualityDescriptorsRule)
        {
            EqualityDescriptorsRule r = (EqualityDescriptorsRule)o;
            if (!Arrays.equals(m_bPresenceOfDescriptor,r.m_bPresenceOfDescriptor))
                return false;
            if (!Arrays.equals(m_nValueOfDescriptor,r.m_nValueOfDescriptor))
                return false;
            return true;
        }
        else return false;
    }

    /**
     * Returns hash code based on array of descriptor values
     * 
     * @return hash code based on array of descriptor values
     * @see java.util.Arrays#hashCode(double[])
     */
    public int hashCode()
    {
        return Arrays.hashCode(m_nValueOfDescriptor);
    }

    /**
     * Provides text representation of this rule.
     * 
     * @return	Text representation of this rule.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        if (m_bPresenceOfDescriptor!=null)
        {
            boolean notfirst = false;
            for (int i=0;i<m_bPresenceOfDescriptor.length;i++)
            if (m_bPresenceOfDescriptor[i])
            {
                if (notfirst) sb.append(" & ");
                else notfirst=true;
                sb.append("( ");
                sb.append(m_Header.attribute(i).name());
               
                sb.append(" = ");
                if (m_Header.isNominal(i)) sb.append(NominalAttribute.stringValue(m_nValueOfDescriptor[i]));
                else sb.append(m_nValueOfDescriptor[i]);
                sb.append(" )");
            }
        }
        else sb.append("(Conditional part is null)");
        sb.append(" -> ");
        if (m_DecAttr!=null)
        {            
            sb.append("( DEC = { ");
            boolean notfirst = false;
            for (int i=0;i<m_DecisionVector.dimension();i++)
            if (m_DecisionVector.get(i)>1e-10)
            {
                if (notfirst) sb.append(", ");
                else notfirst=true;
                sb.append(NominalAttribute.stringValue(m_DecAttr.globalValueCode(i)));
           /*     sb.append('[');
                sb.append(m_DecisionVector.get(i));
                sb.append(']');*/
            }
            sb.append(" } )");
        }
        else sb.append("( Decisional part is null )");
        return sb.toString();
    }
}

/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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


package rseslib.structure.attribute;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Nominal attribute information.
 *
 * @author      Arkadiusz Wojna, Grzegorz Gora
 */
public class NominalAttribute extends Attribute
{
    /** Serialization version. */
	private static final long serialVersionUID = 2L;
	/** Map between string and global double code of nominal values. */
    private static Map<String,Integer> s_StringValueToGlobalValueCodeMap = new HashMap<String,Integer>();
    /** Map between global double codes and string representation of nominal values. */
    private static ArrayList<String> s_StringValueList = new ArrayList<String>();

    /** The number of values of this attribute. */
    private int m_nNoOfValues = 0;
    /** Map between local integer and global double codes of nominal values. */
    private int[] m_LocalToGlobalValueCodeMap = new int[2];
    /** Global code of the least frequent value of this attribute. Can be used only in the attributes with 2 values, -1 in the attributes with more than 2 values. */
    private double m_dMinorityValueGlobalCode = -1;

    /**
     * Constructor initialises mapping
     * between string and int representation of nominal values.
     *
     * @param attrType Attribute type.
     * @param name     Attribute name.
     */
    public NominalAttribute(Type attrType, String name)
    {
        super(attrType, (attrType!=Type.text)?ValueSet.nominal:ValueSet.nonapplicable, name);
    }

    /**
     * Constructor initialises mapping
     * between string and int representation of nominal values
     * based on another attribute object.
     *
     * @param attrType               Attribute type.
     * @param attr                   Original attribute information.
     */
    public NominalAttribute(Type attrType, NominalAttribute attr)
    {
        super(attrType, (attrType!=Type.text)?ValueSet.nominal:ValueSet.nonapplicable, attr.name());
        m_nNoOfValues = attr.m_nNoOfValues;
        m_LocalToGlobalValueCodeMap = attr.m_LocalToGlobalValueCodeMap;
        m_dMinorityValueGlobalCode = attr.m_dMinorityValueGlobalCode;
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeInt(m_nNoOfValues);
    	for (int v = 0; v < m_nNoOfValues; v++)
    		out.writeObject(stringValue(m_LocalToGlobalValueCodeMap[v]));
    	if(m_dMinorityValueGlobalCode == -1)
    		out.writeObject((String)null);
    	else
    		out.writeObject(stringValue(m_dMinorityValueGlobalCode));
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_nNoOfValues = 0;
    	m_LocalToGlobalValueCodeMap = new int[2];
    	int noOfValues = in.readInt();
    	for (int v = 0; v < noOfValues; v++)
    		globalValueCode((String)in.readObject());
    	String minorityValue = (String)in.readObject();
    	if(minorityValue==null)
    		m_dMinorityValueGlobalCode = -1;
    	else
    		m_dMinorityValueGlobalCode = globalValueCode(minorityValue);
    }

    /**
     * Returns the number of values of this attribute.
     *
     * @return Number of values of this attribute.
     */
    public int noOfValues()
    {
        return m_nNoOfValues;
    }

    /**
     * Converts the string representation of a nominal value to a double value
     * that is unique for the whole system.
     * Global codes are successive integer numbers.
     *
     * @param valueName String representation of a nominal value.
     * @return          Global double code of the nominal value.
     */
    public double globalValueCode(String valueName)
    {
        Integer value = (Integer)s_StringValueToGlobalValueCodeMap.get(valueName);
        if (value==null)
        {
            value = new Integer(s_StringValueList.size());
            s_StringValueToGlobalValueCodeMap.put(valueName, value);
            s_StringValueList.add(valueName);
        }
        boolean newValue = true;
        for (int v = 0; newValue && v < m_nNoOfValues; v++)
            if (m_LocalToGlobalValueCodeMap[v]==value.intValue()) newValue = false;
        if (newValue)
        {
            if (m_nNoOfValues == m_LocalToGlobalValueCodeMap.length)
            {
                int[] doubleLocalIntToGlobalIntValueMap = new int[2*m_LocalToGlobalValueCodeMap.length];
                for (int v = 0; v < m_LocalToGlobalValueCodeMap.length; v++)
                    doubleLocalIntToGlobalIntValueMap[v] = m_LocalToGlobalValueCodeMap[v];
                m_LocalToGlobalValueCodeMap = doubleLocalIntToGlobalIntValueMap;
            }
            m_LocalToGlobalValueCodeMap[m_nNoOfValues++] = value.intValue();
        }
        return value.intValue();
    }

    /**
     * Converts the double global code of a nominal value to a string.
     *
     * @param globalValueCode Global double code of a nominal value.
     * @return                String representation of the nominal value.
     */
    public static String stringValue(double globalValueCode)
    {
        if (Double.isNaN(globalValueCode)) throw new RuntimeException("Nominal value global integer code is NaN");
        int intGlobalValue = (int)globalValueCode;
        if (intGlobalValue < 0 || intGlobalValue >= s_StringValueList.size()) throw new RuntimeException("Nominal value global integer code is out of range");
        return (String)s_StringValueList.get(intGlobalValue);
    }

    
    /**
     * Checks whether a value occurs on this nominal attribute.
     *
     * @param valueName Name of a value to be examined.
     * @return          True if the value occurs on this attribute.
     */
    public boolean valueOccurs(String valueName) {
    	Integer value = (Integer)s_StringValueToGlobalValueCodeMap.get(valueName);
    	if (value==null)
    		return false;
    	for (int v = 0; v < m_nNoOfValues; v++)
    		if (m_LocalToGlobalValueCodeMap[v]==value.intValue())
    			return true;
    	return false;
    }

    /**
     * Converts the global double code of a nominal value
     * to a local integer code that is unique only
     * in the range of values of this nominal attribute.
     * The local codes are the succesive numbers
     * from 0 to noOfValues()-1.
     *
     * @param globalValueCode Global double code of a nominal value.
     * @return                Local integer code.
     *                        Returns -1 if the global code is not found in the map
     *                        between the global and the local codes.
     */
    public int localValueCode(double globalValueCode)
    {
        if (Double.isNaN(globalValueCode)) return -1;
        for (int localVal = 0; localVal < m_nNoOfValues; localVal++)
            if (m_LocalToGlobalValueCodeMap[localVal]==globalValueCode) return localVal;
        return -1;
    }

    /**
     * Converts the local integer code of a nominal value
     * to a global double code.
     *
     * @param localValueCode Local integer code of a nominal value.
     * @return               Global double code.
     */
    public double globalValueCode(int localValueCode)
    {
        if (localValueCode==-1) return Double.NaN;
        if (localValueCode < 0 || localValueCode >= m_nNoOfValues) throw new RuntimeException("Nominal value local integer code is out of range");
        return m_LocalToGlobalValueCodeMap[localValueCode];
    }

    /**
     * Sets the least frequent value in this attribute.
     * 
     * @param minorityValueGlobalCode Global code of the least frequent value.
     */
    public void setMinorityValueGlobalCode(double minorityValueGlobalCode)
    {
    	m_dMinorityValueGlobalCode = minorityValueGlobalCode;
    }

    /**
     * Returns true if the attribute provides information about the least frequent value.
     * 
     * @return True if the attribute provides information about the least frequent value.
     */
    public boolean isMinorityValueSet()
    {
    	return (m_dMinorityValueGlobalCode != -1);
    }

    /**
     * Returns the least frequent value in this attribute.
     * 
     * @return Global code of the least frequent value.
     */
    public double getMinorityValueGlobalCode() {
    	if (m_dMinorityValueGlobalCode == -1) throw new RuntimeException("An attempt to get the minority value when it is not set in the attribute");
    	return m_dMinorityValueGlobalCode;
    }
    
     /**
      * Constructs string representation of this attribute.
      *
      * @return String representation of this attribute.
      */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer(super.toString());
        for (int val = 0; val < noOfValues(); val++) sbuf.append(" '"+stringValue(globalValueCode(val))+"'");
        return sbuf.toString();
    }
 }

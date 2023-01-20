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


package rseslib.structure.data;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.formats.DataFormatException;

/**
 * Data object with double values
 * and different types of attributes.
 *
 * @author      Arkadiusz Wojna
 */
public class DoubleDataObject implements DoubleDataWithDecision, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Array of attributes. */
    Header m_arrAttributes;
    /** Array of attribute values. */
    double[] m_arrAttrValues;

    /**
     * Reads in a new data object.
     *
     * @param input      Input for reading.
     * @param attributes Array of attribute types.
     * @throws IOException If error in data has occured.
     */
    public DoubleDataObject(LineNumberReader input, Header attributes) throws IOException, DataFormatException
    {
        m_arrAttributes = attributes;
        m_arrAttrValues = new double[attributes.noOfAttr()];
        if (!input.ready()) throw new IOException("Reading double data object: can not read from input");
        String line = input.readLine();
        int pos = 0;
        for (int attr = 0; attr < m_arrAttrValues.length; attr++)
        {
            if (pos>=line.length()) throw new DataFormatException("Reading double data object: incorrect format");
            int sep = line.indexOf(' ', pos);
            if (attr==m_arrAttrValues.length-1)
            {
                if (sep!=-1) throw new DataFormatException("Reading double data object: incorrect format");
                sep = line.length();
            }
            else if (sep<=0) throw new DataFormatException("Reading double data object: incorrect format");
            String value = line.substring(pos, sep);
            if (m_arrAttributes.isInterpretable(attr))
            {
                if (m_arrAttributes.isMissing(value))
                    set(attr, Double.NaN);
                else if (m_arrAttributes.isNominal(attr))
                    set(attr, ((NominalAttribute)m_arrAttributes.attribute(attr)).globalValueCode(value));
                else if (m_arrAttributes.isNumeric(attr))
                    set(attr, Double.parseDouble(value));
            }
            else if (m_arrAttributes.isText(attr))
                set(attr, ((NominalAttribute)m_arrAttributes.attribute(attr)).globalValueCode(value));
            pos = sep+1;
        }
    }

    /**
     * Constructs a new data object
     * with a given attribute types.
     *
     * @param attributes Array of attribute types.
     */
    public DoubleDataObject(Header attributes)
    {
        m_arrAttributes = attributes;
        m_arrAttrValues = new double[attributes.noOfAttr()];
    }

    /**
     * Constructs a new data object as a filed-by-field copy
     * of template object.
     * @param template object to be copied
     */
    public DoubleDataObject(DoubleDataObject template)
    {
        /*
         * This constructor is neccessary while there are no
         * other way to create a copy without useless 
         * creating and discarding an array of doubles. 
         */
        m_arrAttributes=template.m_arrAttributes;
        /* 
         * Warning: clone works only for arrays of primitives ! 
         */
        m_arrAttrValues=template.m_arrAttrValues.clone();
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(m_arrAttributes);
    	for (int att = 0; att < m_arrAttributes.noOfAttr(); att++)
    		if (m_arrAttributes.isNominal(att))
    			out.writeInt(((NominalAttribute)m_arrAttributes.attribute(att)).localValueCode(m_arrAttrValues[att]));
    		else out.writeDouble(m_arrAttrValues[att]);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_arrAttributes = (Header)in.readObject();
    	m_arrAttrValues = new double[m_arrAttributes.noOfAttr()];
    	for (int att = 0; att < m_arrAttributes.noOfAttr(); att++)
    		if (m_arrAttributes.isNominal(att))
    			m_arrAttrValues[att] = ((NominalAttribute)m_arrAttributes.attribute(att)).globalValueCode(in.readInt());
    		else m_arrAttrValues[att] = in.readDouble();
    }

    /**
     * Returns attribute types for this data object.
     *
     * @return Attribute types for this data object.
     */
    public Header attributes()
    {
        return m_arrAttributes;
    }

    /**
     * Writes this object.
     *
     * @param output Output for writing.
     * @throws IOException If an I/O error has occured.
     */
    public void store(BufferedWriter output) throws IOException
    {
        for (int attr = 0; attr < m_arrAttrValues.length; attr++)
        {
            if (m_arrAttributes.isInterpretable(attr))
            {
                if (Double.isNaN(m_arrAttrValues[attr]))
                    output.write(m_arrAttributes.missing());
                else if (m_arrAttributes.isNominal(attr))
                    output.write(NominalAttribute.stringValue(m_arrAttrValues[attr]));
                else if (m_arrAttributes.isNumeric(attr))
                    output.write(Double.toString(m_arrAttrValues[attr]));
            }
            else if (m_arrAttributes.isText(attr))
                output.write(NominalAttribute.stringValue(m_arrAttrValues[attr]));
            if (attr < m_arrAttrValues.length-1) output.write(' ');
        }
        output.newLine();
    }

    /**
     * Writes this object in arff format.
     *
     * @param output Output for writing.
     * @throws IOException If an I/O error has occured.
     */
    public void storeArff(BufferedWriter output) throws IOException
    {
        for (int attr = 0; attr < m_arrAttrValues.length; attr++)
        {
            if (m_arrAttributes.isInterpretable(attr))
            {
                if (Double.isNaN(m_arrAttrValues[attr]))
                    output.write("?");
                else if (m_arrAttributes.isNominal(attr))
                    output.write(NominalAttribute.stringValue(m_arrAttrValues[attr]));
                else if (m_arrAttributes.isNumeric(attr))
                    output.write(Double.toString(m_arrAttrValues[attr]));
            }
            else if (m_arrAttributes.isText(attr))
                output.write(NominalAttribute.stringValue(m_arrAttrValues[attr]));
            if (attr < m_arrAttrValues.length-1) output.write(',');
        }
        output.newLine();
    }

    /**
     * Sets the value of a given attribute to a given double value.
     *
     * @param atrNo Index of the attribute to be changed.
     * @param value Double attribute value.
     */
    public void set(int atrNo, double value)
    {
        m_arrAttrValues[atrNo] = value;
    }

    /**
     * Returns the double value of a given attribute.
     *
     * @param atrNo Index of the attribute to be returned.
     * @return      Double attribute value.
     */
    public double get(int atrNo)
    {
        return m_arrAttrValues[atrNo];
    }

    /**
     * Sets decision.
     *
     * @param decVal Decision value.
     */
    public void setDecision(double decVal)
    {
        m_arrAttrValues[m_arrAttributes.decision()] = decVal;
    }

    /**
     * Returns decision.
     *
     * @return Decision value.
     */
    public double getDecision()
    {
        return m_arrAttrValues[m_arrAttributes.decision()];
    }

    /**
     * Compares values between this and a given data objects.
     *
     * @param obj Data object to be compared.
     * @return    True, if data objects are equal, false otherwise.
     */
    public boolean equals(DoubleDataObject obj)
    {
        if (m_arrAttrValues.length != obj.m_arrAttrValues.length) return false;
        for (int i = 0; i < m_arrAttrValues.length ; i++)
            if (m_arrAttrValues[i] != obj.m_arrAttrValues[i]) return false;
        return true;
    }

    /**
     * Constructs string representation of this data object.
     *
     * @return String representation of this data object.
     */
    public String toString()
    {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("<");
        boolean notFirst = false;
        for (int i = 0; i < m_arrAttrValues.length; i++)
            if (m_arrAttributes.isConditional(i))
            {
                if (notFirst) strBuf.append(", ");
                if (Double.isNaN(m_arrAttrValues[i])) strBuf.append(m_arrAttributes.missing());
                else if (m_arrAttributes.isNumeric(i)) strBuf.append(m_arrAttrValues[i]);
                else if (m_arrAttributes.isNominal(i)) strBuf.append(NominalAttribute.stringValue(m_arrAttrValues[i]));
                notFirst = true;
            }
        notFirst = false;
        for (int i = 0; i < m_arrAttrValues.length; i++)
            if (m_arrAttributes.isDecision(i))
            {
                if (notFirst) strBuf.append(", ");
                else strBuf.append(", dec = ");
                if (Double.isNaN(m_arrAttrValues[i])) strBuf.append(m_arrAttributes.missing());
                else if (m_arrAttributes.isNumeric(i)) strBuf.append(m_arrAttrValues[i]);
                else if (m_arrAttributes.isNominal(i)) strBuf.append(NominalAttribute.stringValue(m_arrAttrValues[i]));
                notFirst = true;
            }
        strBuf.append(">");
        return strBuf.toString();
    }
    
    /**
     * Returns filed-by-field copy of this data object.
     * @return copy of this data object.
     * @see java.lang.Object#clone()
     * @see rseslib.structure.data.DoubleData#clone()
     */
    public Object clone()
    {
        return new DoubleDataObject(this);
    }

    /**
     * Returns true for equivallent data object.
     * In this implementation assumes that Header is exactly the same. 
     * (Physically the same object)
     * @param obj - object for comparison 
     * @return true if data object is equivallent.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof DoubleDataObject)
        {
            DoubleDataObject dd = (DoubleDataObject)obj;
            if (m_arrAttributes!=dd.m_arrAttributes) return false;
            else return Arrays.equals(m_arrAttrValues,dd.m_arrAttrValues);
        }
        else return false;
    }

    /**
     * Returns hash code generated only from attribute values.
     * @return hash code generated only from attribute values
     * @see java.util.Arrays#hashCode(double[])
     */
    public int hashCode()
    {
        return Arrays.hashCode(m_arrAttrValues);
    }
}

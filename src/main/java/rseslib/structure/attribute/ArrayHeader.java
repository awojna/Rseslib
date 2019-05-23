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


package rseslib.structure.attribute;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import rseslib.structure.attribute.formats.ArffHeaderReader;
import rseslib.structure.attribute.formats.DataFormatRecognizer;
import rseslib.structure.attribute.formats.HeaderFormatException;
import rseslib.structure.attribute.formats.HeaderReader;
import rseslib.structure.attribute.formats.RsesHeaderReader;
import rseslib.structure.attribute.formats.RseslibHeaderReader;
import rseslib.system.Report;

/**
 * Header with information about attributes.
 *
 * @author      Arkadiusz Wojna
 */
public class ArrayHeader implements Header
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	/** The set of strings that denote missing values. */
    private Collection<String> m_MissingValues = new ArrayList<String>();
    /** The first missing value enumerated in header file. */
    private String m_Missing = null;
    /**
     * The table indicating which attributes
     * are to be read in while loading data from file.
     */
    private boolean[] m_AttrLoaded;
    /** Array of attributes (read in only). */
    private Attribute[] m_arrAttributes = null;
    /**
     * Index of the decision attribute.
     * -1 indicates there is no decision attribute in this header,
     * -2 indicates there are two or more decision attributes in this header.
     */

    private int m_nDecIndex = -1;

    /**
     * This constructor reads header information from input
     * only in rseslib format.
     *
     * @param input        Input for reading header information.
     * @throws IOException if an I/O error has occured.
     */
    public ArrayHeader(LineNumberReader input) throws IOException, HeaderFormatException
    {
        initialize(new RseslibHeaderReader(input));
    }

    /**
     * Constructor that reads header information from file.
     * Data format is recognized automatically.
     * Rseslib format requires header file to be given as the argument.
     * RSES2 format requires data file to be given as the argument.
     *
     * @param headerFile   File containing header information.
     * @throws IOException if an I/O error has occured.
     */
    public ArrayHeader(File headerFile) throws IOException, HeaderFormatException
    {
        HeaderReader hr = null;
        DataFormatRecognizer rec = new DataFormatRecognizer();
        switch (rec.recognizeFormat(headerFile))
        {
        case ARFF:
            hr = new ArffHeaderReader(headerFile);
            break;
        case RSES:
            hr = new RsesHeaderReader(headerFile);
            break;
        case CSV:
            LineNumberReader br = new LineNumberReader(new FileReader(headerFile));
            hr = new RseslibHeaderReader(br);
            br.close();
            break;
        }
        initialize(hr);
    }

    /**
     * Constructs the header using header information reader.
     *
     * @param hr           Header information reader.
     */
    public ArrayHeader(HeaderReader hr)
    {
        initialize(hr);
    }

    /**
     * Initializes header information.
     *
     * @param hr           Header information reader.
     */
    private void initialize(HeaderReader hr)
    {
        m_MissingValues = hr.allMissing();
        m_Missing = hr.singleMissing();
        m_AttrLoaded = hr.bitMaskOfLoaded();
        m_arrAttributes = hr.attributesForLoading();
        // searching for the decision attribute
        int dec = 0;
        for (; dec < m_arrAttributes.length && !m_arrAttributes[dec].isDecision(); dec++);
        if (dec < m_arrAttributes.length)
        {
            m_nDecIndex = dec;
            dec++;
        }
        else m_nDecIndex = -1;
        for (; dec < m_arrAttributes.length && !m_arrAttributes[dec].isDecision(); dec++);
        if (dec < m_arrAttributes.length) m_nDecIndex = -2;
    }

    /**
     * Constructor.
     *
     * @param attributes Array of attributes.
     * @param missing    Sting denoting missing value.
     */
    public ArrayHeader(Attribute[] attributes, String missing)
    {
        if (missing!=null)
        {
            m_Missing = missing;
            m_MissingValues.add(missing);
        }
        m_arrAttributes = attributes;
        m_AttrLoaded = new boolean[m_arrAttributes.length];
        for (int at = 0; at < m_AttrLoaded.length; at++) m_AttrLoaded[at] = true;
        // searching for the decision attribute
        int dec = 0;
        for (; dec < m_arrAttributes.length && !m_arrAttributes[dec].isDecision(); dec++);
        if (dec < m_arrAttributes.length)
        {
            m_nDecIndex = dec;
            dec++;
        }
        else m_nDecIndex = -1;
        for (; dec < m_arrAttributes.length && !m_arrAttributes[dec].isDecision(); dec++);
        if (dec < m_arrAttributes.length) m_nDecIndex = -2;
    }

    /**
     * Writes this header.
     * The header storage keeps local codes of nominal values.
     * After reloading the local codes are the same
     * as before storing.
     * The global codes of nominal values can change.
     *
     * @param output Output for writing.
     * @throws IOException if an I/O error has occured.
     */
    public void store(BufferedWriter output) throws IOException
    {
        output.write("\\"+RseslibHeaderReader.HEADER_START_KEYWORD);
        output.newLine();
        if (m_Missing!=null)
        {
            output.write(RseslibHeaderReader.MISSING_VALUE_KEYWORD+"\t\t");
            boolean first = true;
            for (String val : m_MissingValues)
            	if (first)
            	{
            		output.write(val);
            		first = false;
            	}
            	else output.write(", "+val);
            output.newLine();
        }
        for (int attr = 0; attr < m_arrAttributes.length; attr++)
        {
            output.write(m_arrAttributes[attr].name()+"\t\t");
            if (m_arrAttributes[attr].isText())
                output.write(Attribute.Type.text.name());
            if (m_arrAttributes[attr].isInterpretable())
            {
                if (m_arrAttributes[attr].isNominal())
                    output.write(Attribute.ValueSet.nominal.name());
                else if (m_arrAttributes[attr].isNumeric())
                    output.write(Attribute.ValueSet.numeric.name());
                if (m_arrAttributes[attr].isDecision())
                output.write(", "+Attribute.Type.decision.name());
            }
            output.newLine();
        }
        output.write("\\"+RseslibHeaderReader.HEADER_END_KEYWORD);
        output.newLine();
    }

    /**
     * Writes this header in arff format.
     *
     * @param name		Name of the relation.
     * @param output	Output for writing.
     * @throws IOException if an I/O error has occured.
     */
    public void storeArff(String name, BufferedWriter output) throws IOException
    {
    	output.write("@RELATION "+name);
        output.newLine();
        output.newLine();
        for (int attr = 0; attr < m_arrAttributes.length; attr++)
        {
            output.write("@ATTRIBUTE "+m_arrAttributes[attr].name()+"\t");
            if (m_arrAttributes[attr].isText())
                output.write("STRING");
            if (m_arrAttributes[attr].isInterpretable())
            {
                if (m_arrAttributes[attr].isNominal())
                {
                    NominalAttribute na = (NominalAttribute)m_arrAttributes[attr];
                	output.write("{");
                    for (int v=0; v<na.noOfValues(); v++)
                    {
                    	if (v>0) output.write(", ");
                    	output.write(NominalAttribute.stringValue(na.globalValueCode(v)));
                    }
                    output.write("}");
                }
                else if (m_arrAttributes[attr].isNumeric())
                    output.write("NUMERIC");
            }
            output.newLine();
        }
    }

    /**
     * Returns the number of attributes.
     *
     * @return Number of attributes.
     */
    public int noOfAttr()
    {
        return m_arrAttributes.length;
    }

    /**
     * Returns an attribute.
     *
     * @param attrInd Index of the attribute.
     * @return        Attribute.
     */
    public Attribute attribute(int attrInd)
    {
        return m_arrAttributes[attrInd];
    }

    /**
     * Returns the name of an attribute.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        Name of an attribute.
     */
    public String name(int attrInd)
    {
        return m_arrAttributes[attrInd].name();
    }

    /**
     * Checks whether this attribute is conditional or decision.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if this attribute is conditional or decision false otherwise.
     */
    public boolean isInterpretable(int attrInd)
    {
        return m_arrAttributes[attrInd].isInterpretable();
    }

    /**
     * Checks whether an attribute is a text.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if an attribute is a text false otherwise.
     */
    public boolean isText(int attrInd)
    {
        return m_arrAttributes[attrInd].isText();
    }

    /**
     * Checks whether an attribute is numeric.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if an attribute is numeric false otherwise.
     */
    public boolean isNumeric(int attrInd)
    {
        return m_arrAttributes[attrInd].isNumeric();
    }

    /**
     * Checks whether an attribute is nominal.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if an attribute is nominal false otherwise.
     */
    public boolean isNominal(int attrInd)
    {
        return m_arrAttributes[attrInd].isNominal();
    }

    /**
     * Checks whether a given string denotes missing value.
     *
     * @param value String to be checked.
     * @return      True if value denotes missing value false otherwise.
     */
    public boolean isMissing(String value)
    {
        return m_MissingValues.contains(value);
    }

    /**
     * Returns missing value.
     * @return String that dentoes the missing value.
     */
    public String missing()
    {
        return m_Missing;
    }

    /**
     * Checks whether this attribute is conditional.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if this attribute is conditional false otherwise.
     */
    public boolean isConditional(int attrInd)
    {
        return m_arrAttributes[attrInd].isConditional();
    }

    /**
     * Checks whether this attribute is decision.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if this attribute is decision false otherwise.
     */
    public boolean isDecision(int attrInd)
    {
        return m_arrAttributes[attrInd].isDecision();
    }

    /**
     * Returns the index of the decision attribute.
     *
     * @return Index of the decision attribute.
     */
    public int decision()
    {
        if (m_nDecIndex < 0) throw new RuntimeException("There is no decision attributes in header or more than one");
        return m_nDecIndex;
    }

    /**
     * Returns the information about the decision attribute
     * if the decision is nominal.
     *
     * @return Decision attribute as a nominal attribute.
     */
    public NominalAttribute nominalDecisionAttribute()
    {
        if (m_nDecIndex < 0)
            throw new RuntimeException("There is no decision attributes in header or more than one");
        if (!m_arrAttributes[m_nDecIndex].isNominal())
            throw new RuntimeException("Non-nominal decision attribute requested as nominal attribute");
        return (NominalAttribute)m_arrAttributes[m_nDecIndex];
    }

    /**
     * Returns all missing values.
     *
     * @return All missing values.
     */
    public Collection<String> missingValues()
    {
        return m_MissingValues;
    }

    /**
     * Returns the number of original attributes in header file.
     *
     * @return Number of attributes in header file.
     */
    public int noOfAttrInFile()
    {
        return m_AttrLoaded.length;
    }

    /**
     * Returns whether the i-th attribute
     * in header file was not to be skipped.
     *
     * @param i Attribute index.
     * @return False if the i-th attribute in header file was to be skipped,
     *         true otherwise.
     */
    public boolean attrInFileLoaded(int i)
    {
        return m_AttrLoaded[i];
    }

    /**
     * Returns a string representation of this object.
     *
     * @return String representation of this object.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer(256);
        buf.append("Number of attributes = " + noOfAttr()+Report.lineSeparator);
        buf.append("Attribute types:");
        for (int i = 0; i < noOfAttr(); i++)
        {
            if (i%10==0) buf.append(Report.lineSeparator+"  ");
            String attrTypeName = null;
            if (isText(i)) attrTypeName = Attribute.Type.text.name();
            else if (isDecision(i)) attrTypeName = Attribute.Type.decision.name();
            else if (isNominal(i)) attrTypeName = Attribute.ValueSet.nominal.name();
            else if (isNumeric(i)) attrTypeName = Attribute.ValueSet.numeric.name();
            else attrTypeName = Attribute.ValueSet.nonapplicable.name();
            buf.append(" "+attrTypeName.substring(0, 3));
        }
        buf.append(Report.lineSeparator);
        return buf.toString();
    }
    
    /**
     * Returns true for equivallent header object.
     * @param obj - object for comparison 
     * @return true if header object is equivallent.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see rseslib.structure.attribute.Header#equals(Object)
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof ArrayHeader)
        {
            ArrayHeader ah = (ArrayHeader) obj;
            return Arrays.equals(m_arrAttributes,ah.m_arrAttributes);
        }
        return false;
    }
}

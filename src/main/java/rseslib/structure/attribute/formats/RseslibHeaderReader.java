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


package rseslib.structure.attribute.formats;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;

/**
 * Reader for data header in rseslib format.
 *
 * @author      Arkadiusz Wojna
 */
public class RseslibHeaderReader implements HeaderReader
{
    /** The header start keyword. */
    public static final String HEADER_START_KEYWORD = "beginheader";
    /** The keyword in the missing value line in a data header. */
    public static final String MISSING_VALUE_KEYWORD = "missing_value";
    /** The header end keyword. */
    public static final String HEADER_END_KEYWORD = "endheader";

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
    private Attribute[] m_arrAttributes;

    /**
     * Constructor.
     *
     * @param input        Input for reading header information.
     * @throws IOException if an I/O error has occured.
     */
    public RseslibHeaderReader(Reader input) throws IOException, HeaderFormatException
    {
        StreamTokenizer st = new StreamTokenizer(input);
        st.lowerCaseMode(false);
        st.eolIsSignificant(true);
        st.whitespaceChars(',', ',');
        st.wordChars('_', '_');
        st.wordChars('-', '-');
        st.wordChars('?', '?');
        st.wordChars('\'', '\'');
        st.quoteChar('"');
        ArrayList<Attribute> attrTypes = new ArrayList<Attribute>();
        int noOfAttr = 0;
        boolean endExpected = false;
        boolean beginning = true;
        boolean cont = true;
        while (cont && st.nextToken() != StreamTokenizer.TT_EOF)
        {
            if (st.ttype!=StreamTokenizer.TT_EOL)
                if (st.ttype=='#') while (st.nextToken()!=StreamTokenizer.TT_EOF && st.ttype!=StreamTokenizer.TT_EOL);
                else if (st.ttype=='\\')
                {
                    if (beginning)
                    {
                        if (st.nextToken()!=StreamTokenizer.TT_WORD
                                 || HEADER_START_KEYWORD.indexOf(st.sval)!=0)
                                 throw new HeaderFormatException("Wrong command found at the beginning of header file");
                        endExpected = true;
                        if (st.nextToken() != StreamTokenizer.TT_EOL) throw new HeaderFormatException("Header start command not in a seperated line");
                    }
                    else
                    {
                        if (!endExpected) throw new HeaderFormatException("\\ found in wrong place");
                        if (st.nextToken() != StreamTokenizer.TT_WORD
                                 || HEADER_END_KEYWORD.indexOf(st.sval) != 0)
                                 throw new HeaderFormatException("Wrong command found in header file");
                        if (st.nextToken() != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF) throw new HeaderFormatException("Header end command not in a seperated line");
                        cont = false;
                    }
                }
                else
                {
                    if (st.ttype != StreamTokenizer.TT_WORD) throw new HeaderFormatException("The beginning of the line "+st.lineno()+" is neither an attribute name nor the comment character '#' nor the '"+MISSING_VALUE_KEYWORD+"' keyword");
                    if (st.sval.equalsIgnoreCase(MISSING_VALUE_KEYWORD))
                        while (st.nextToken()!=StreamTokenizer.TT_EOF && st.ttype!=StreamTokenizer.TT_EOL)
                        {
                            String missing = null;
                            if (st.ttype == StreamTokenizer.TT_WORD) missing = st.sval;
                            else
                            {
                                char[] missingString = new char[1];
                                missingString[0] = (char)st.ttype;
                                missing = new String(missingString);
                            }
                            if (m_Missing == null) m_Missing = missing;
                            if (!m_MissingValues.contains(missing)) m_MissingValues.add(missing);
                        }
                    else
                    {
                        String name = st.sval;
                        Attribute.Type attrType = null;
                        Attribute.ValueSet valueSetType = Attribute.ValueSet.nonapplicable;
                        boolean skip = false;
                        while (st.nextToken()!=StreamTokenizer.TT_EOF && st.ttype!=StreamTokenizer.TT_EOL && st.ttype!='{')
                        {
                            if (st.ttype != StreamTokenizer.TT_WORD) throw new HeaderFormatException("Unknown attribute feature in the line "+st.lineno());
                            String keyword = st.sval.toLowerCase();
                            boolean recognised = false;
                            if (Attribute.Type.conditional.name().indexOf(keyword)==0)
                            {
                                if (attrType != null) throw new HeaderFormatException("Attribute type in the line "+st.lineno()+" defined ambiguously");
                                attrType = Attribute.Type.conditional;
                                recognised = true;
                            }
                            if (Attribute.Type.decision.name().indexOf(keyword)==0)
                            {
                                if (attrType != null) throw new HeaderFormatException("Attribute type in the line "+st.lineno()+" defined ambiguously");
                                attrType = Attribute.Type.decision;
                                recognised = true;
                            }
                            if (Attribute.Type.text.name().indexOf(keyword)==0)
                            {
                                if (attrType != null) throw new HeaderFormatException("Attribute type in the line "+st.lineno()+" defined ambiguously");
                                attrType = Attribute.Type.text;
                                recognised = true;
                            }
                            if (Attribute.ValueSet.numeric.name().indexOf(keyword)==0)
                            {
                                if (valueSetType != Attribute.ValueSet.nonapplicable) throw new HeaderFormatException("Value set type in the line "+st.lineno()+" defined ambiguously");
                                valueSetType = Attribute.ValueSet.numeric;
                                recognised = true;
                            }
                            if (Attribute.ValueSet.nominal.name().indexOf(keyword)==0)
                            {
                                if (valueSetType != Attribute.ValueSet.nonapplicable) throw new HeaderFormatException("Value set type in the line "+st.lineno()+" defined ambiguously");
                                valueSetType = Attribute.ValueSet.nominal;
                                recognised = true;
                            }
                            if ("skip".indexOf(keyword)==0)
                            {
                                skip = true;
                                recognised = true;
                            }
                            if (!recognised) throw new HeaderFormatException("Unknown attribute feature "+st.sval+" in the line "+st.lineno());
                        }
                        if (skip) attrTypes.add(null);
                        else
                        {
                            if (attrType == null) attrType = Attribute.Type.conditional;
                            if (attrType==Attribute.Type.text)
                                valueSetType = Attribute.ValueSet.nonapplicable;
                            else
                                if (valueSetType==Attribute.ValueSet.nonapplicable) throw new HeaderFormatException("Value set type required for the attribute in the line "+st.lineno());
                            switch (valueSetType)
                            {
                                case nonapplicable:
                                case nominal:
                                    attrTypes.add(new NominalAttribute(attrType, name));
                                    break;
                                case numeric:
                                   attrTypes.add(new NumericAttribute(attrType, name));
                                    break;
                                default:
                                    throw new HeaderFormatException("Unused attribute type in the line "+st.lineno());
                            }
                            noOfAttr++;
                        }
                        if (st.ttype=='{')
                        {
                        	if (valueSetType!=Attribute.ValueSet.nominal && valueSetType!=Attribute.ValueSet.nonapplicable)
                                throw new HeaderFormatException("Value enumaration for non-nominal attibute in the line "+st.lineno());
                        	NominalAttribute nom = null;
                        	if (!skip) nom = (NominalAttribute)attrTypes.get(attrTypes.size()-1);
                            while (st.nextToken()!='}')
                            {
                                if (st.ttype!='"') throw new HeaderFormatException("Unknown attribute feature in the line "+st.lineno());
                                if (!skip) nom.globalValueCode(st.sval);
                            }
                            if (st.nextToken()!=StreamTokenizer.TT_EOF && st.ttype!=StreamTokenizer.TT_EOL)
                            	throw new HeaderFormatException("Unexpected information after value enumeration in the line "+st.lineno());
                        }
                    }
                }
            beginning = false;
        }
        if (noOfAttr <= 0) throw new HeaderFormatException("No attributes were found in the header file");
        m_AttrLoaded = new boolean[attrTypes.size()];
        m_arrAttributes = new Attribute[noOfAttr];
        int origAttr = 0;
        for (; origAttr < attrTypes.size() && attrTypes.get(origAttr)==null; origAttr++) m_AttrLoaded[origAttr] = false;
        for (int attr = 0; attr < m_arrAttributes.length; attr++)
        {
            if (origAttr >= attrTypes.size()) throw new RuntimeException("Internal error of header loader");
            m_AttrLoaded[origAttr] = true;
            m_arrAttributes[attr] = (Attribute)attrTypes.get(origAttr);
            origAttr++;
            for (; origAttr < attrTypes.size() && attrTypes.get(origAttr)==null; origAttr++) m_AttrLoaded[origAttr] = false;
        }
        if (origAttr != attrTypes.size()) throw new RuntimeException("Internal error of header loader");
    }

    /**
     * Returns the set of all strings denoting missing value.
     *
     * @return Set of all strings denoting missing value.
     */
    public Collection<String> allMissing()
    {
        return m_MissingValues;
    }

    /**
     * Returns missing value.
     *
     * @return String that denotes the missing value.
     */
    public String singleMissing()
    {
        return m_Missing;
    }

    /**
     * Returns the bit mask indicating
     * which original attributes are to be read in
     * while loading data from file.
     *
     * @return The bit mask where true at a position i
     *         indicates that attribute i is to be read in
     *         and false indicates that the attribute is to be skipped.
     */
    public boolean[] bitMaskOfLoaded()
    {
        return m_AttrLoaded;
    }

    /**
     * Returns the information about attributes (loaded only).
     *
     * @return Array of attributes.
     */
    public Attribute[] attributesForLoading()
    {
        return m_arrAttributes;
    }
}

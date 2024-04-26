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


package rseslib.structure.attribute.formats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;

/**
 * Reader for data header in RSES2.x format.
 *
 * @author      Arkadiusz Wojna
 */
public class ArffHeaderReader implements HeaderReader
{
    /** The set of strings that denote missing values. */
    private Collection<String> m_MissingValues = new ArrayList<String>();
    /** The first missing value enumerated in header file. */
    private String m_Missing = "?";
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
     * @param dataFile   Data file in RSES2 format.
     * @throws IOException if an I/O error has occured.
     */
    public ArffHeaderReader(File dataFile) throws IOException
    {
    	weka.core.converters.ArffLoader loader = new weka.core.converters.ArffLoader();
    	loader.setSource(dataFile);
    	weka.core.Instances hdr = loader.getStructure();
    	if (hdr.classIndex()==-1)
    	{
    		if (!hdr.attribute(hdr.numAttributes()-1).isNominal())
    			throw new IOException("Unknown decision attribute in "+dataFile.getPath()+" (the last attribute is not nominal)");
    		hdr.setClassIndex(hdr.numAttributes()-1);
    	}
        extractHeaderInfo(hdr);
    }

    /**
     * Constructor.
     *
     * @param rsesTab   Data table structure from RSES2.
     * @throws IOException if an I/O error has occured.
     */
    public ArffHeaderReader(weka.core.Instances arffTab)
    {
        extractHeaderInfo(arffTab);
    }

    /**
     * Extracts header information
     * from data table structure from RSES2.
     *
     * @param rsesTab   Data table structure from RSES2.
     * @throws IOException if an I/O error has occured.
     */
    private void extractHeaderInfo(weka.core.Instances arffTab)
    {
        m_MissingValues.add(m_Missing);
        m_AttrLoaded = new boolean[arffTab.numAttributes()];
        m_arrAttributes = new Attribute[arffTab.numAttributes()];
        for (int attr = 0; attr < m_arrAttributes.length; attr++)
        {
            m_AttrLoaded[attr] = true;
            if (arffTab.attribute(attr).isNominal())
            {
                if (attr!=arffTab.classIndex())
                    m_arrAttributes[attr] = new NominalAttribute(Attribute.Type.conditional, arffTab.attribute(attr).name());
                else {
                	weka.core.Attribute wekaDec = arffTab.attribute(attr);
                    m_arrAttributes[attr] = new NominalAttribute(Attribute.Type.decision, arffTab.attribute(attr).name());
                    for(int val = 0; val < wekaDec.numValues(); ++val)
                    	((NominalAttribute)m_arrAttributes[attr]).globalValueCode(wekaDec.value(val));
                }
            }
            else if (arffTab.attribute(attr).isNumeric())
                    m_arrAttributes[attr] = new NumericAttribute(Attribute.Type.conditional, arffTab.attribute(attr).name());
            else
                m_arrAttributes[attr] = new NominalAttribute(Attribute.Type.text, arffTab.attribute(attr).name());
        }
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

/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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


package rseslib.structure.data.formats;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.formats.ArffHeaderReader;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Input stream for data in the ARFF format.
 *
 * @author      Arkadiusz Wojna
 */
public class ArffDoubleDataInput implements DoubleDataInput, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
    /** Arff table. */
    private weka.core.Instances m_ArffTab;
    /** Array of attributes. */
    private Header m_Header;
    /** Progress object for progress reporting. */
    private Progress m_Progress;
    /** Index of the next data object to be read. */
    private int m_nObjNumber = 0;

    /**
     * Constructs this input stream
     * and initialises information about attributes
     * from rses data file.
     *
     * @param dataFile       Data file to be loaded.
     * @param prog           Progress object for progress reporting.
     * @throws IOException   If an I/O error occurs.
     */
    public ArffDoubleDataInput(File dataFile, Progress prog) throws IOException
    {
    	weka.core.converters.ArffLoader loader = new weka.core.converters.ArffLoader();
    	loader.setSource(dataFile);
    	m_ArffTab = loader.getDataSet();
    	if (m_ArffTab.classIndex()==-1)
    	{
    		if (!m_ArffTab.attribute(m_ArffTab.numAttributes()-1).isNominal())
    			throw new IOException("Unknown decision attribute in "+dataFile.getPath()+" (the last attribute is not nominal)");
    		m_ArffTab.setClassIndex(m_ArffTab.numAttributes()-1);
    	}
        m_Header = new ArrayHeader(new ArffHeaderReader(m_ArffTab));
        m_Progress = prog;
        m_Progress.set("Loading data from "+dataFile.getPath(), m_ArffTab.numInstances());
    }

    /**
     * Constructs this input stream
     * and verifies information about attributes
     * in rses data file.
     *
     * @param dataFile       Data file to be loaded.
     * @param hdr            Header to be verified with data specification in a file.
     * @param prog           Progress object for progress reporting.
     * @throws IOException   If an I/O error occurs.
     */
    public ArffDoubleDataInput(File dataFile, Header hdr, Progress prog) throws IOException, BadHeaderException
    {
    	weka.core.converters.ArffLoader loader = new weka.core.converters.ArffLoader();
    	loader.setSource(dataFile);
    	m_ArffTab = loader.getDataSet();
    	if (m_ArffTab.classIndex()==-1)
    	{
    		if (!m_ArffTab.attribute(m_ArffTab.numAttributes()-1).isNominal())
    			throw new IOException("Unknown decision attribute in "+dataFile.getPath()+" (the last attribute is not nominal)");
    		m_ArffTab.setClassIndex(m_ArffTab.numAttributes()-1);
    	}
        verifyAttributeTypes(m_ArffTab, hdr);
        m_Header = hdr;
        m_Progress = prog;
        m_Progress.set("Loading data from "+dataFile.getPath(), m_ArffTab.numInstances());
    }

    /**
     * Constructs this input stream based on weka data table.
     *
     * @param wekaTab        Weka table.
     * @throws IOException   If an I/O error occurs.
     */
    public ArffDoubleDataInput(weka.core.Instances wekaTab) throws IOException
    {
    	m_ArffTab = wekaTab;
    	if (m_ArffTab.classIndex()==-1)
    	{
    		if (!m_ArffTab.attribute(m_ArffTab.numAttributes()-1).isNominal())
    			throw new IOException("Unknown decision attribute in weka table (the last attribute is not nominal)");
    		m_ArffTab.setClassIndex(m_ArffTab.numAttributes()-1);
    	}
        m_Header = new ArrayHeader(new ArffHeaderReader(m_ArffTab));
        m_Progress = new EmptyProgress();
        m_Progress.set("Loading data from weka table", m_ArffTab.numInstances());
    }

    /**
     * Verifies compatibility of an rses table
     * with a given header.
     *
     * @param rsesTab  Rses table with attributes to be verified.
     * @param hdr      Header to be verified.
     */
    private static void verifyAttributeTypes(weka.core.Instances arffTab, Header hdr) throws BadHeaderException
    {
        if (hdr.noOfAttr()!=arffTab.numAttributes()) throw new BadHeaderException("Different numbers of attributes");
        for (int attr = 0; attr < arffTab.numAttributes(); attr++)
            if (arffTab.attribute(attr).isNominal())
            {
                if (attr!=arffTab.classIndex())
                {
                    if (!hdr.isConditional(attr) || !hdr.isNominal(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
                }
                else
                    if (!hdr.isDecision(attr) || !hdr.isNominal(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
            }
            else if (arffTab.attribute(attr).isNumeric())
            {
            	if (!hdr.isConditional(attr) || !hdr.isNumeric(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
            }
            else
                if (!hdr.isText(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
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
    	m_ArffTab = null;
    	m_Progress = null;
    	m_nObjNumber = 0;
    }

    /**
     * Returns the array of attributes.
     *
     * @return Array of attributes.
     */
    public Header attributes()
    {
        return m_Header;
    }

    /**
     * Returns true if there is more data to be read, false otherwise.
     *
     * @return True if there is more data to be read, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public boolean available() throws IOException
    {
        return (m_nObjNumber < m_ArffTab.numInstances());
    }

    /**
     * Reads a new data from this stream.
     *
     * @return Read data.
     * @throws IOException           If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public DoubleData convertToDoubleData(weka.core.Instance inst)
    {
        DoubleData ido = new DoubleDataObject(m_Header);
        for (int attr = 0; attr < m_Header.noOfAttr(); attr++)
        	if (inst.isMissing(attr))
        		ido.set(attr, Double.NaN);
        	else if (m_Header.isNumeric(attr))
        		ido.set(attr, inst.value(attr));
        	else
        		ido.set(attr, ((NominalAttribute)m_Header.attribute(attr)).globalValueCode(inst.stringValue(attr)));
        return ido;
    }

    /**
     * Reads a new data from this stream.
     *
     * @return Read data.
     * @throws IOException           If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public DoubleData readDoubleData() throws IOException, InterruptedException
    {
        if (m_nObjNumber >= m_ArffTab.numInstances())
        	throw new IOException("An atempt of reading data from an empty data input");
        weka.core.Instance inst = m_ArffTab.instance(m_nObjNumber);
        DoubleData ido = convertToDoubleData(inst);
        m_nObjNumber++;
        m_Progress.step();
        return ido;
    }
}

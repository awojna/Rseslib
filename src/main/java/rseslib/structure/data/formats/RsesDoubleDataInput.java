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


package rseslib.structure.data.formats;

import java.io.File;
import java.io.IOException;

import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.formats.RsesHeaderReader;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.system.progress.Progress;

/**
 * Input stream for data in the rses format.
 *
 * @author      Arkadiusz Wojna
 */
public class RsesDoubleDataInput implements DoubleDataInput
{
    /** Rses table. */
    private rseslib.structure.attribute.formats.rses.Table m_RsesTab = new rseslib.structure.attribute.formats.rses.Table();
    /** Denominators for attribute values. */
    private int[] m_Denominators;
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
    public RsesDoubleDataInput(File dataFile, Progress prog) throws IOException
    {
        try
        {
            m_RsesTab.loadTable(dataFile.getPath());
        }
        catch (InterruptedException e)
        {
            throw new IOException(e.getMessage());
        }
        m_Header = new ArrayHeader(new RsesHeaderReader(m_RsesTab));
        m_Denominators = new int[m_Header.noOfAttr()];
        for (int attr = 0; attr < m_Header.noOfAttr(); attr++)
            if (m_Header.isNumeric(attr))
            {
            	m_Denominators[attr] = 1;
            	for (int power = 0; power < m_RsesTab.getAttrPrec(attr); power++)
            		m_Denominators[attr] *= 10;
            }
        m_Progress = prog;
        m_Progress.set("Loading data from "+dataFile.getPath(), m_RsesTab.getNoObj());
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
    public RsesDoubleDataInput(File dataFile, Header hdr, Progress prog) throws IOException, BadHeaderException
    {
        try
        {
            m_RsesTab.loadTable(dataFile.getPath());
        }
        catch (InterruptedException e)
        {
            throw new IOException(e.getMessage());
        }
        verifyAttributeTypes(m_RsesTab, hdr);
        m_Header = hdr;
        m_Denominators = new int[m_Header.noOfAttr()];
        for (int attr = 0; attr < m_Header.noOfAttr(); attr++)
            if (m_Header.isNumeric(attr))
            {
            	m_Denominators[attr] = 1;
            	for (int power = 0; power < m_RsesTab.getAttrPrec(attr); power++)
            		m_Denominators[attr] *= 10;
            }
        m_Progress = prog;
        m_Progress.set("Loading data from "+dataFile.getPath(), m_RsesTab.getNoObj());
    }

    /**
     * Verifies compatibility of an rses table
     * with a given header.
     *
     * @param rsesTab  Rses table with attributes to be verified.
     * @param hdr      Header to be verified.
     */
    private static void verifyAttributeTypes(rseslib.structure.attribute.formats.rses.Table rsesTab, Header hdr) throws BadHeaderException
    {
        if (hdr.noOfAttr()!=rsesTab.getNoAttr()) throw new BadHeaderException("Different numbers of attributes");
        for (int attr = 0; attr < rsesTab.getNoAttr(); attr++)
            if (rsesTab.getAttrType(attr))
            {
                if (attr < rsesTab.getNoAttr()-1)
                {
                    if (!hdr.isConditional(attr) || !hdr.isNominal(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
                }
                else
                    if (!hdr.isDecision(attr) || !hdr.isNominal(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
            }
            else
            {
                if (attr < rsesTab.getNoAttr()-1)
                {
                    if (!hdr.isConditional(attr) || !hdr.isNumeric(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
                }
                else
                    if (!hdr.isDecision(attr) || !hdr.isNominal(attr)) throw new BadHeaderException("Wrong type of the attribute "+attr);
            }
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
        return m_nObjNumber < m_RsesTab.getNoObj();
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
        if (m_nObjNumber >= m_RsesTab.getNoObj()) throw new IOException("An atempt of reading data from an empty data input");
        DoubleData ido;
        ido = new DoubleDataObject(m_Header);
        for (int attr = 0; attr < m_Header.noOfAttr(); attr++)
            if (m_Header.isNumeric(attr))
                if (m_RsesTab.getTable(m_nObjNumber, attr)==Integer.MAX_VALUE) ido.set(attr, Double.NaN);
                else ido.set(attr, ((double)m_RsesTab.getTable(m_nObjNumber, attr))/m_Denominators[attr]);
            else
            {
                double doubleValue = Double.NaN;
                if (!m_RsesTab.getTableString(m_nObjNumber, attr).equals("NULL"))
                    doubleValue = ((NominalAttribute)m_Header.attribute(attr)).globalValueCode(m_RsesTab.getTableString(m_nObjNumber, attr));
                ido.set(attr, doubleValue);
            }
        m_nObjNumber++;
        m_Progress.step();
        return ido;
    }
}

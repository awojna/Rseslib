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


package rseslib.structure.data.formats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.formats.HeaderFormatException;
import rseslib.structure.attribute.formats.RseslibHeaderReader;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.system.progress.Progress;

/**
 * Input stream for data in the format with a separated header file.
 *
 * @author      Arkadiusz Wojna
 */
public class RseslibDoubleDataInput implements DoubleDataInput
{

    /** Reader from data file. */
    private LineNumberReader m_DataReader;
    /** Header. */
    private ArrayHeader m_Header;
    /** Switch indicating whether a comma is the separator between attribute values. */
    private boolean m_CommaAsValueSeparator = true;
    /** Separator at the end of data line. */
    private String m_LineEnding = null;
    /** Line with the next data object to be read. */
    String m_Line = null;
    /** Progress object for progress reporting. */
    private Progress m_Progress;
    /** Size of data file */
    private long m_FileSize;
    /** Approximate number of bytes read. */
    private int m_BytesRead = 0;
    /** Approximate percentage of data loaded. */
    private int m_Percentage = 0;

    /**
     * Constructs this input stream
     * and reads attribute information
     * from data file.
     *
     * @param dataFile       Data file to be loaded.
     * @param prog           Progress object for progress reporting.
     * @throws IOException   If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public RseslibDoubleDataInput(File dataFile, Progress prog) throws IOException, HeaderFormatException, DataFormatException, InterruptedException
    {
        if (!containsRseslibHeader(dataFile)) throw new DataFormatException("Unknown data header for file "+dataFile.getPath());
        m_DataReader = new LineNumberReader(new FileReader(dataFile));
        m_Header = new ArrayHeader(m_DataReader);
        detectDataSeparators(dataFile);
        m_Progress = prog;
        m_Progress.set("Loading data from "+dataFile.getPath(), 100);
        m_FileSize = dataFile.length();
        if (m_DataReader.ready()) m_Line = m_DataReader.readLine();
        else close();
    }

    /**
     * Constructs this input stream
     * and verifies information about attributes
     * from data file.
     *
     * @param dataFile       Data file to be loaded.
     * @param hdr            Header to be verified with data specification in a file.
     * @param prog           Progress object for progress reporting.
     * @throws IOException   If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public RseslibDoubleDataInput(File dataFile, Header hdr, Progress prog) throws IOException, HeaderFormatException, BadHeaderException, InterruptedException
    {
        if (hdr.getClass()!=ArrayHeader.class) throw new BadHeaderException("Only ArrayHeader can be used for loading data from "+dataFile.getPath());
        m_DataReader = new LineNumberReader(new FileReader(dataFile));
        if (containsRseslibHeader(dataFile))
        {
            if (!verifyAttributeTypes(m_DataReader, hdr)) throw new BadHeaderException("Incompatible data header in file "+dataFile.getPath());
        }
        m_Header = (ArrayHeader)hdr;
        detectDataSeparators(dataFile);
        m_Progress = prog;
        m_Progress.set("Loading data from "+dataFile.getPath(), 100);
        m_FileSize = dataFile.length();
        if (m_DataReader.ready()) m_Line = m_DataReader.readLine();
        else close();
    }

    /**
     * Checks whether the file contains rseslib data header at the beginning.
     *
     * @param dataFile Data file to be checked.
     * @return         True if data header is contained, false otherwise.
     * @throws IOException if an I/O error has occured.
     */
    private boolean containsRseslibHeader(File dataFile) throws IOException
    {
        BufferedReader bw = new BufferedReader(new FileReader(dataFile));
        boolean isHeader = false;
        if (bw.ready() && bw.readLine().indexOf("\\"+RseslibHeaderReader.HEADER_START_KEYWORD)==0)
            isHeader = true;
        bw.close();
        return isHeader;
    }

    /**
     * Verifies compatibility of an rses table
     * with a given header.
     *
     * @param input    Input for header information to be verified.
     * @param hdr      Header to be verified.
     * @return         True if headers are compatible, false otherwise.
     * @throws IOException   If an I/O error occurs.
     */
    private static boolean verifyAttributeTypes(LineNumberReader input, Header hdr) throws BadHeaderException, HeaderFormatException, IOException
    {
        if (hdr.getClass()!=ArrayHeader.class) throw new BadHeaderException("Only ArrayHeader can be used for data reading");
        ArrayHeader new_hdr = new ArrayHeader(input);
        if (new_hdr.noOfAttr()!=hdr.noOfAttr()) return false;
        for (String val : new_hdr.missingValues())
            if (!hdr.isMissing(val)) return false;
        for (int attr = 0; attr < new_hdr.noOfAttr(); attr++)
        {
            if (new_hdr.isNominal(attr) && !hdr.isNominal(attr)) return false;
            if (new_hdr.isNumeric(attr) && !hdr.isNumeric(attr)) return false;
            if (new_hdr.isText(attr) && !hdr.isText(attr)) return false;
            if (new_hdr.isConditional(attr) && !hdr.isConditional(attr)) return false;
            if (new_hdr.isDecision(attr) && !hdr.isDecision(attr)) return false;
        }
        return true;
    }

    /**
     * Detects type of attribute value separator and line ending.
     *
     * @param dataFile     Data file.
     * @throws IOException If an I/O error occurs.
     */
    private void detectDataSeparators(File dataFile) throws IOException
    {
        BufferedReader dataReader = new BufferedReader(new FileReader(dataFile));
        String lines[] = new String[2];
        if (dataReader.ready())
        {
            lines[0] = dataReader.readLine();
            if (lines[0].indexOf("\\"+RseslibHeaderReader.HEADER_START_KEYWORD)==0)
            {
                while (dataReader.ready() &&
                       dataReader.readLine().indexOf("\\"+RseslibHeaderReader.HEADER_END_KEYWORD)!=0);
                if (dataReader.ready()) lines[0] = dataReader.readLine();
                else lines[0] = null;
            }
            if (lines[0] != null)
                while ((lines[0].length()==0 || lines[0].charAt(0)=='#') && dataReader.ready())
                    lines[0] = dataReader.readLine();
            if (lines[0].length()==0 || lines[0].charAt(0)=='#') lines[0] = null;
        }
        if (dataReader.ready())
        {
            lines[1] = dataReader.readLine();
            while ((lines[1].length()==0 || lines[1].charAt(0)=='#') && dataReader.ready())
                lines[1] = dataReader.readLine();
            if (lines[1].length()==0 || lines[1].charAt(0)=='#') lines[1] = null;
        }
        boolean dotEnding = true;
        for (int l = 0; m_CommaAsValueSeparator && l < lines.length; l++)
            if (lines[l]!=null)
            {
                int commaIndex = lines[l].indexOf(',');
                int commas = 0;
                while (commaIndex != -1)
                {
                    commas++;
                    commaIndex = lines[l].indexOf(',', commaIndex+1);
                }
                m_CommaAsValueSeparator &= (commas==m_Header.noOfAttrInFile()-1);
                if (lines[l].charAt(lines[l].length()-1)!='.') dotEnding = false;
            }
        if (dotEnding) m_LineEnding = ".";
        dataReader.close();
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
      * @throws InterruptedException  If user has interrupted reading data.
      */
    public boolean available() throws IOException, InterruptedException
    {
        if (m_Line!=null)
        {
            while ((m_Line.length()==0 || m_Line.charAt(0)=='#') && m_DataReader.ready())
            {
                m_BytesRead += m_Line.length()+1;
                m_Line = m_DataReader.readLine();
            }
            if (m_Line.length()==0 || m_Line.charAt(0)=='#') close();
        }
        return (m_Line!=null);
    }

    /**
     * Reads a new data from this stream.
     *
     * @return Read data.
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public DoubleData readDoubleData() throws IOException, DataFormatException, InterruptedException
    {
        if (!available()) throw new IOException("An atempt of reading data from an empty data input");
        int attrIndex = 0;
        int valueIndex = 0;
        DoubleData dObject = new DoubleDataObject(m_Header);
        while (valueIndex < m_Line.length() && Character.isWhitespace(m_Line.charAt(valueIndex))) valueIndex++;
        for (int att = 0; att < m_Header.noOfAttrInFile(); att++)
        {
            if (valueIndex>=m_Line.length()) throw new DataFormatException("Too few attributes in line "+m_DataReader.getLineNumber());
            int sepIndex = valueIndex;
            if (m_CommaAsValueSeparator)
            {
                sepIndex = m_Line.indexOf(',', valueIndex);
                if (att==m_Header.noOfAttrInFile()-1)
                {
                    if (sepIndex!=-1) throw new DataFormatException("Too many attributes in the data line "+m_DataReader.getLineNumber());
                    if (m_LineEnding==null) sepIndex = m_Line.length();
                    else
                    {
                        sepIndex = m_Line.indexOf(m_LineEnding, valueIndex);
                        if (sepIndex==-1) throw new DataFormatException("Line ending not found at the end of the data line "+m_DataReader.getLineNumber());
                    }
                }
                else if (sepIndex==-1) throw new DataFormatException("Too few attributes in the data line "+m_DataReader.getLineNumber());
            }
            else while (sepIndex < m_Line.length() && !Character.isWhitespace(m_Line.charAt(sepIndex))) sepIndex++;
            if (m_Header.attrInFileLoaded(att))
            {
                String value = m_Line.substring(valueIndex, sepIndex);
                if (m_Header.isInterpretable(attrIndex))
                {
                    if (m_Header.isMissing(value))
                        dObject.set(attrIndex, Double.NaN);
                    else if (m_Header.isNominal(attrIndex))
                    {
                        double doubleValue = ((NominalAttribute)m_Header.attribute(attrIndex)).globalValueCode(value);
                        dObject.set(attrIndex, doubleValue);
                    }
                    else if (m_Header.isNumeric(attrIndex))
                        dObject.set(attrIndex, Double.parseDouble(value));
                }
                else if (m_Header.isText(attrIndex))
                    dObject.set(attrIndex, ((NominalAttribute)m_Header.attribute(attrIndex)).globalValueCode(value));
                attrIndex++;
            }
            if (m_CommaAsValueSeparator && att < m_Header.noOfAttrInFile()-1) valueIndex = sepIndex + 1;
            else valueIndex = sepIndex;
            while (valueIndex < m_Line.length() && Character.isWhitespace(m_Line.charAt(valueIndex))) valueIndex++;
            if (att==m_Header.noOfAttrInFile()-1)
                if (m_LineEnding==null)
                {
                    if (valueIndex!=m_Line.length()) throw new DataFormatException("The data line "+m_DataReader.getLineNumber()+" too long");
                }
                else if (valueIndex != m_Line.length()-m_LineEnding.length()) throw new DataFormatException("The data line "+m_DataReader.getLineNumber()+" too long");
        }
        m_BytesRead += m_Line.length()+1;
        while (100*m_BytesRead >= (m_Percentage+1)*m_FileSize)
        {
            m_Percentage++;
            m_Progress.step();
        }
        if (m_DataReader.ready()) m_Line = m_DataReader.readLine();
        else close();
        return dObject;
    }

    /**
     * Closes data input.
     *
     * @throws IOException If an I/O error occurs.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    private void close() throws IOException, InterruptedException
    {
        m_Line = null;
        m_DataReader.close();
        while (m_Percentage < 100)
        {
            m_Percentage++;
            m_Progress.step();
        }
    }
}

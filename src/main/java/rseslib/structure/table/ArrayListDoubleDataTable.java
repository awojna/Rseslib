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


package rseslib.structure.table;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.formats.DataFormatRecognizer;
import rseslib.structure.attribute.formats.HeaderFormatException;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.data.formats.ArffDoubleDataInput;
import rseslib.structure.data.formats.DataFormatException;
import rseslib.structure.data.formats.DoubleDataInput;
import rseslib.structure.data.formats.RsesDoubleDataInput;
import rseslib.structure.data.formats.RseslibDoubleDataInput;
import rseslib.system.Report;
import rseslib.system.progress.Progress;
import rseslib.util.random.RandomSelection;

/**
 * Table of data objects with double values
 * implemented with use of java.lang.ArrayList.
 *
 * @author      Arkadiusz Wojna
 */
public class ArrayListDoubleDataTable implements DoubleDataTable
{
    /** Random number generator. */
    private static final Random RANDOM_GENERATOR = new Random();

    /** Array of attribute types. */
    private Header m_arrAttributes;
    /** Array of data objects in this set. */
    private ArrayList<DoubleData> m_DataObjects = new ArrayList<DoubleData>();
    /** Array of numerical statistics for numerical attributes. */
    private NumericalStatistics[] m_NumStats = null;
    /**
     * Array of sizes of particular decision classes.
     * Array indices correspond to local decision codes from this data header.
     * It is null if the decision distribution has not been requested yet.
     */
    private int[][] m_ValueDistribution = null;


    /**
     * Constructor reading data from a file.
     * Data format is recognized automatically.
     *
     * @param dataFile Data file to be loaded.
     * @param prog     Progress object for progress reporting.
     * @throws IOException If error in data has occured.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public ArrayListDoubleDataTable(File dataFile, Progress prog) throws IOException, HeaderFormatException, DataFormatException, InterruptedException
    {
        DoubleDataInput doi = null;
        DataFormatRecognizer rec = new DataFormatRecognizer();
        switch (rec.recognizeFormat(dataFile))
        {
        case ARFF:
        	doi = new ArffDoubleDataInput(dataFile, prog);
        	break;
        case RSES:
        	doi = new RsesDoubleDataInput(dataFile, prog);
        	break;
        case CSV:
        	doi = new RseslibDoubleDataInput(dataFile, prog);
        	break;
        }
        m_arrAttributes = doi.attributes();
        while (doi.available())
        {
            DoubleData dObject = doi.readDoubleData();
            m_DataObjects.add(dObject);
        }
    }

    /**
     * Constructor reading data from a file.
     * Data format is recognized automatically.
     * The constructor verifies compatibility of data
     * with a given header.
     *
     * @param dataFile Data file to be loaded.
     * @param hdr      Header for data in a given file.
     * @param prog     Progress object for progress reporting.
     * @throws IOException If error in data has occured.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public ArrayListDoubleDataTable(File dataFile, Header hdr, Progress prog) throws IOException, HeaderFormatException, DataFormatException, BadHeaderException, InterruptedException
    {
        DoubleDataInput doi = null;
        DataFormatRecognizer rec = new DataFormatRecognizer();
        switch (rec.recognizeFormat(dataFile))
        {
        case ARFF:
        	doi = new ArffDoubleDataInput(dataFile, hdr, prog);
        	break;
        case RSES:
        	doi = new RsesDoubleDataInput(dataFile, hdr, prog);
        	break;
        case CSV:
        	doi = new RseslibDoubleDataInput(dataFile, hdr, prog);
        	break;
        }
        m_arrAttributes = doi.attributes();
        while (doi.available())
        {
            DoubleData dObject = doi.readDoubleData();
            m_DataObjects.add(dObject);
        }
    }

    /**
     * Constructor filling this table with data from weka table.
     *
     * @param wekaTab  Weka table used to fill this table.
     * @throws IOException If error in data has occured.
     * @throws InterruptedException  If user has interrupted reading data.
     */
    public ArrayListDoubleDataTable(weka.core.Instances wekaTab) throws IOException, DataFormatException, InterruptedException
    {
        DoubleDataInput doi = new ArffDoubleDataInput(wekaTab);
        m_arrAttributes = doi.attributes();
        while (doi.available())
        {
            DoubleData dObject = doi.readDoubleData();
            m_DataObjects.add(dObject);
        }
    }

    /**
     * Constructs an empty table with attributes from a template table.
     *
     * @param attributes Header for the table.
     */
    public ArrayListDoubleDataTable(Header attributes)
    {
        m_arrAttributes = attributes;
        m_ValueDistribution = new int[m_arrAttributes.noOfAttr()][];
    }

    /**
     * Constructs a table with attributes from an array of data objects.
     *
     * @param objects Array of data objects used to construct this table.
     */
    public ArrayListDoubleDataTable(DoubleData[] objects)
    {
        if (objects.length <= 0) throw new RuntimeException("Data table initialized with empty set of objects");
        m_arrAttributes = objects[0].attributes();
        for (int obj = 0; obj < objects.length; obj++) m_DataObjects.add(objects[obj]);
    }

    /**
     * Constructs a table with attributes from a collection of data objects.
     *
     * @param objects Collection of data objects used to construct this table.
     */
    public ArrayListDoubleDataTable(ArrayList<DoubleData> objects)
    {
        if (objects.size() <= 0) throw new RuntimeException("Data table initialized with empty set of objects");
        m_arrAttributes = objects.get(0).attributes();
        m_DataObjects = objects;
    }

    /**
     * Saves this object to a file.
     *
     * @param outputFile File to be used for storing this object.
     * @param prog       Progress object for progress reporting.
     * @throws IOException If an I/O error has occured.
     * @throws InterruptedException If user has interrupted saving object.
     */
    public void store(File outputFile, Progress prog) throws IOException, InterruptedException
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        prog.set("Saving data table to "+outputFile.getPath(), m_DataObjects.size());
        m_arrAttributes.store(bw);
        bw.newLine();
        for (DoubleData obj : m_DataObjects)
        {
            obj.store(bw);
            prog.step();
        }
        bw.close();
    }

    /**
     * Saves this object to a file in arff format.
     *
     * @param outputFile File to be used for storing this object.
     * @param prog       Progress object for progress reporting.
     * @throws IOException If an I/O error has occured.
     * @throws InterruptedException If user has interrupted saving object.
     */
    public void storeArff(String name, File outputFile, Progress prog) throws IOException, InterruptedException
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        prog.set("Saving data table to "+outputFile.getPath(), m_DataObjects.size());
        m_arrAttributes.storeArff(name, bw);
        bw.newLine();
        bw.write("@DATA");
        bw.newLine();
        for (DoubleData obj : m_DataObjects)
        {
            obj.storeArff(bw);
            prog.step();
        }
        bw.close();
    }

    /**
     * Returns an array of attributes.
     *
     * @return Array of attributes.
     */
    public Header attributes()
    {
        return m_arrAttributes;
    }

    /**
     * Returns the number of objects.
     *
     * @return Number of objects.
     */
    public int noOfObjects()
    {
        return m_DataObjects.size();
    }

    /**
     * Adds a data object to this table.
     *
     * @param obj The object to be added.
     */
    public void add(DoubleData obj)
    {
        m_DataObjects.add(obj);
        m_NumStats = null;
        if (m_ValueDistribution!=null)
        	for (int att = 0; att < m_ValueDistribution.length; att++)
        		if (m_ValueDistribution[att]!=null)
        			m_ValueDistribution[att][((NominalAttribute)m_arrAttributes.attribute(att)).localValueCode(obj.get(att))]++;
    }

    /**
     * Removes a data object from this table.
     *
     * @param obj  The object to be removed.
     * @return     True, if the object was found and removed from this table,
     *             false otherwise.
     */
    public boolean remove(DoubleData obj)
    {
    	for (DoubleData iterObj : m_DataObjects)
            if (iterObj.equals(obj))
            {
                m_DataObjects.remove(iterObj);
                if (m_ValueDistribution!=null)
                	for (int att = 0; att < m_ValueDistribution.length; att++)
                		if (m_ValueDistribution[att]!=null)
                			m_ValueDistribution[att][((NominalAttribute)m_arrAttributes.attribute(att)).localValueCode(obj.get(att))]--;
                return true;
            }
        return false;
    }

    /**
     * Returns collection of all objects from this table.
     *
     * @return Collection of all objects from this table.
     */
    public ArrayList<DoubleData> getDataObjects()
    {
        return m_DataObjects;
    }

    /**
     * Returns the basic statistics of a given numerical attribute.
     *
     * @return Statistics of a given numerical attribute.
     */
    public NumericalStatistics getNumericalStatistics(int attr)
    {
    	if (!m_arrAttributes.isNumeric(attr)) return null;
    	if (m_NumStats==null)
            m_NumStats = new NumericalStatistics[m_arrAttributes.noOfAttr()];
    	if (m_NumStats[attr]==null)
    		m_NumStats[attr] = new NumericalStatistics(m_DataObjects,attr);
    	return m_NumStats[attr];
    }

    /**
     * Returns the distribution of decision values in this table if the decision is nominal.
     * Array indices correspond to local decision codes from this data header.
     *
     * @return Distribution of decisions in this table.
     */
    public int[] getDecisionDistribution()
    {
    	return getValueDistribution(m_arrAttributes.decision());
    }

    /**
     * Returns the distribution of values in this table for a nominal attribute.
     * Array indices correspond to local value codes for a given attibute.
     *
     * @param attrInd	Index of the attribute.
     * @return Distribution of values in this table.
     */
    public int[] getValueDistribution(int attrInd)
    {
    	if (!m_arrAttributes.isNominal(attrInd)) return null;
    	if (m_ValueDistribution==null)
    		m_ValueDistribution = new int[m_arrAttributes.noOfAttr()][];
        if (m_ValueDistribution[attrInd]==null)
        {
            NominalAttribute attr = (NominalAttribute)m_arrAttributes.attribute(attrInd);
            m_ValueDistribution[attrInd] = new int[attr.noOfValues()];
            for (DoubleData dObj : m_DataObjects)
            	m_ValueDistribution[attrInd][attr.localValueCode(dObj.get(attrInd))]++;
        }
        return m_ValueDistribution[attrInd];
    }


    /**
     * Random split of this table into 2 data collections
     * with the splitting ratio noOfPartsForLeft to noOfPartsForRight.
     *
     * @param noOfPartsForLeft  Number of parts for the table returned at the position 0.
     * @param noOfPartsForRight Number of parts for the table returned at the position 1.
     * @return                  Table splitted into 2 data collections.
     */
    public ArrayList<DoubleData>[] randomSplit(int noOfPartsForLeft, int noOfPartsForRight)
    {
        ArrayList<DoubleData>[] parts = new ArrayList[2];
        parts[0] = new ArrayList<DoubleData>();
        parts[1] = new ArrayList<DoubleData>();
        boolean[] assigned = RandomSelection.subset(m_DataObjects.size(), noOfPartsForLeft, noOfPartsForRight);
        for (int ind = 0; ind < m_DataObjects.size(); ind++)
            if (assigned[ind]) parts[0].add(m_DataObjects.get(ind));
            else parts[1].add(m_DataObjects.get(ind));
        return parts;

    }

    /**
     * Random partition of this table into a given number of parts of equal sizes.
     *
     * @param noOfParts Number of parts to be generated.
     * @return          Table divided into noOfParts collections.
     */
    public ArrayList<DoubleData>[] randomPartition(int noOfParts)
    {
        ArrayList<DoubleData>[] parts = new ArrayList[noOfParts];
        for (int part = 0; part < parts.length; part++) parts[part] = new ArrayList<DoubleData>();
        boolean[] assigned = new boolean[m_DataObjects.size()];
        int noOfAssigned = 0;
        int part = 0;
        while (part < parts.length - 1)
        {
            int ind = RANDOM_GENERATOR.nextInt(m_DataObjects.size());
            while (assigned[ind]) ind = RANDOM_GENERATOR.nextInt(m_DataObjects.size());
            parts[part].add(m_DataObjects.get(ind));
            assigned[ind] = true;
            noOfAssigned++;
            if (noOfAssigned*parts.length >= m_DataObjects.size()*(part+1)) part++;
        }
        for (int ind = 0; ind < m_DataObjects.size(); ind++)
            if (!assigned[ind]) parts[parts.length-1].add(m_DataObjects.get(ind));
        return parts;
    }

    /**
     * Random partition of this table into a given number of parts of equal sizes preserving class distribution.
     *
     * @param noOfParts Number of parts to be generated.
     * @return          Table divided into noOfParts collections.
     */
    public ArrayList<DoubleData>[] randomStratifiedPartition(int noOfParts)
    {
    	NominalAttribute decAttr = m_arrAttributes.nominalDecisionAttribute();
    	
    	// separate objects into decision classes
    	ArrayList<DoubleData>[] decClass = new ArrayList[decAttr.noOfValues()];
        for (int dec = 0; dec < decClass.length; dec++)
        	decClass[dec] = new ArrayList<DoubleData>();
    	for (DoubleData dObj : m_DataObjects)
    		decClass[decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())].add(dObj);
    	
    	// partition each decision class separately
        ArrayList<DoubleData>[][] parts = new ArrayList[decAttr.noOfValues()][];
        for (int dec = 0; dec < parts.length; dec++)
        {
        	parts[dec] = new ArrayList[noOfParts];
        	for (int part = 0; part < parts[dec].length; part++)
        		parts[dec][part] = new ArrayList<DoubleData>();
            boolean[] assigned = new boolean[decClass[dec].size()];
            int noOfAssigned = 0;
            int part = 0;
            while (part < noOfParts - 1)
            {
                int ind = RANDOM_GENERATOR.nextInt(decClass[dec].size());
                while (assigned[ind])
                	ind = RANDOM_GENERATOR.nextInt(decClass[dec].size());
                parts[dec][part].add(decClass[dec].get(ind));
                assigned[ind] = true;
                noOfAssigned++;
                if (noOfAssigned * noOfParts >= decClass[dec].size() * (part + 1))
                	part++;
            }
            for (int ind = 0; ind < decClass[dec].size(); ind++)
                if (!assigned[ind])
                	parts[dec][noOfParts - 1].add(decClass[dec].get(ind));
        }
        
        // merge folds from parts for particular decisions
        ArrayList<DoubleData>[] folds = new ArrayList[noOfParts];
    	for (int fold = 0; fold < folds.length; fold++)
    	{
    		folds[fold] = new ArrayList<DoubleData>();
            for (int dec = 0; dec < parts.length; dec++)
            	folds[fold].addAll(parts[dec][fold]);
    	}
        return folds;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return String representation of this object.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer(1024);
        buf.append("Number of objects = " + m_DataObjects.size()+Report.lineSeparator);
        buf.append(m_arrAttributes);
        int dec = -1;
        for (int i = 0; dec!=-2 && i < m_arrAttributes.noOfAttr(); i++)
            if (m_arrAttributes.isDecision(i))
                if (dec==-1) dec = i;
                else dec = -2;
        if (dec >= 0 && m_arrAttributes.isNominal(dec))
        {
            int[] decDistr = getDecisionDistribution();
            NominalAttribute decAttr = m_arrAttributes.nominalDecisionAttribute();
            buf.append("Decisions:"+Report.lineSeparator);
            for (int i = 0; i < decDistr.length; i++)
                if (decDistr[i] > 0)
                    buf.append("   number of objects with the decision " + NominalAttribute.stringValue(decAttr.globalValueCode(i)) + " is " + decDistr[i]+Report.lineSeparator);
        }
        return buf.toString();
    }
    
    /**
     * Create and return a copy of this object.
     * 
     * @return Copy of this object.
     */
    public Object clone()
    {
        ArrayListDoubleDataTable tab = new ArrayListDoubleDataTable(m_arrAttributes);
        if (m_DataObjects!=null)
        {
            tab.m_DataObjects = new ArrayList<DoubleData>(m_DataObjects.size());
            for (DoubleData object : m_DataObjects)
                tab.m_DataObjects.add((DoubleData)object.clone());
        }
        if (m_ValueDistribution!=null)
        {
        	tab.m_ValueDistribution = new int[m_ValueDistribution.length][];
        	for (int att = 0; att < m_ValueDistribution.length; att++)
        		if (m_ValueDistribution[att]!=null)
        			tab.m_ValueDistribution[att] = m_ValueDistribution[att].clone();
        }
        if (m_NumStats!=null)
        {
        	tab.m_NumStats = new NumericalStatistics[m_NumStats.length];
        	for (int att = 0; att < m_NumStats.length; att++)
        		if (m_NumStats[att]!=null)
        			tab.m_NumStats[att] = (NumericalStatistics)m_NumStats[att].clone();
        }
        return tab;
    }
}

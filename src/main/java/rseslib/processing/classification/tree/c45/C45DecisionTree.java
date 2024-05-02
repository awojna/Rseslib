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


package rseslib.processing.classification.tree.c45;

import java.io.*;
import java.util.*;

import rseslib.processing.classification.Classifier;
import rseslib.structure.attribute.*;
import rseslib.structure.data.*;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;


/**
 * C4.5 decision tree classifier.
 *
 * @author      Arkadiusz Wojna
 */
public class C45DecisionTree extends ConfigurationWithStatistics implements Classifier, Serializable
{
   /** Serialization version. */
	private static final long serialVersionUID = 1L;
    /** Name of property indicating whether the tree is pruned. */
    public static final String PRUNING_PROPERTY_NAME = "pruning";
    /** Name of property defining the number of parts used for tree construction. */
    public static final String NO_OF_BULIDING_PARTS_PROPERTY_NAME = "noOfPartsForBuilding";
    /** Name of property defining the number of parts used for tree pruning. */
    public static final String NO_OF_PRUNING_PARTS_PROPERTY_NAME = "noOfPartsForPruning";

    /** Root node of this decision tree */
    protected DecisionTreeNode m_Root;
    /** Decision attribute. */
    NominalAttribute m_DecisionAttribute;
    /** Header of data. */
    Header m_Header;

    /**
     * Constructor.
     *
     * @param prop            Properties of this clasifier.
     * @param tab             Table used to build a decision tree.
     * @param prog            Progress object to report training progress.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public C45DecisionTree(Properties prop, DoubleDataTable tab, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        super(prop);
        m_Header = tab.attributes();
        m_DecisionAttribute = m_Header.nominalDecisionAttribute();
        Collection<DoubleData> trainSet = null, validSet = null;
        int buildingSteps = 1; 
        int pruningSteps = 0; 
        if (getBoolProperty(PRUNING_PROPERTY_NAME))
        {
            buildingSteps = getIntProperty(NO_OF_BULIDING_PARTS_PROPERTY_NAME); 
            pruningSteps = getIntProperty(NO_OF_PRUNING_PARTS_PROPERTY_NAME); 
        	Collection<DoubleData>[] parts = tab.randomSplit(buildingSteps, pruningSteps);
        	trainSet = parts[0];
        	validSet = parts[1];
        }
        else trainSet = tab.getDataObjects();
        prog.set("Learning C4.5 classifier", buildingSteps+pruningSteps);
        m_Root = new DecisionTreeNode(trainSet, tab.attributes(), new BestGainRatioDiscriminationProvider(), m_DecisionAttribute.globalValueCode(0));
        for (int i = 0; i < buildingSteps; i++) prog.step();
        if (validSet!=null)
        {
        	m_Root.prune(validSet);
            for (int i = 0; i < pruningSteps; i++) prog.step();
        }
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	writeConfigurationAndStatistics(out);
    	out.defaultWriteObject();
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readConfigurationAndStatistics(in);
    	in.defaultReadObject();
    }

    /**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
     */
    public double classify(DoubleData dObj)
    {
    	return m_Root.classify(dObj);
        /*Vector decDistr = classifyWithDecDistribution(dObj);
        int bestDec = 0;
        for (int dec = 1; dec < decDistr.dimension(); dec++)
            if (decDistr.get(dec) > decDistr.get(bestDec)) bestDec = dec;
        return m_DecisionAttribute.globalValueCode(bestDec);*/
    }

    /**
     * Assigns a decision vector to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
     */
    public Vector classifyWithDecDistribution(DoubleData dObj)
    {
        return m_Root.classifyWithDecDistribution(dObj);
    }

    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }

    /**
     * Returns the data header.
     *
     * @return The data header.
     */
    public Header attributes()
    {
        return m_Header;
    }

    /**
     * Outputs the tree.
     *
     * @return The description of the tree.
     */
    public String toString()
    {
        return m_Root.toString(0);
    }
}

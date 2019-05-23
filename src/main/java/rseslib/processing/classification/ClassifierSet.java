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


package rseslib.processing.classification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.MultiProgress;
import rseslib.system.progress.Progress;

/**
 * This class enables to test a number of classifiers
 * at once. It can be used by different testing methods.
 *
 * @author Arkadiusz Wojna
 *
 */
public class ClassifierSet implements Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Map between classifier names and their classes. */
	private Map<String,Class> m_ClassifierTypes = new HashMap<String,Class>();
    /** Map between classifier names and their arguments. */
	private Map<String,Properties> m_ClassifierProperties = new HashMap<String,Properties>();
    /** Map between classifier names and classifiers. */
    private Map<String,Classifier> m_Classifiers = new HashMap<String,Classifier>();

    /**
     * Add a classifier to this set of classifiers.
     *
     * @param name 				Name of a classifier to be added.
     * @param classifierType	Class of a classifier to be added.
     * @param prop				Properties of a classifier to be added.
     * 							If null, default properties are loaded.
     */
	public void addClassifier(String name, Class classifierType, Properties prop) throws PropertyConfigurationException
	{
		if (m_ClassifierTypes.containsKey(name) || m_Classifiers.containsKey(name))
			throw new IllegalArgumentException("A classifier named "+name+" already added.");
		m_ClassifierTypes.put(name, classifierType);
		m_ClassifierProperties.put(name, prop);
	}

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(m_ClassifierTypes);
    	out.writeObject(m_ClassifierProperties);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_ClassifierTypes = (Map<String,Class>)in.readObject();
    	m_ClassifierProperties = (Map<String,Properties>)in.readObject();
    	m_Classifiers = new HashMap<String,Classifier>();
    }

    /**
     * Add a classifier with default properties to this set of classifiers.
     *
     * @param name 				Name of a classifier to be added.
     * @param classifierType	Class of a classifier to be added.
     * @param prog				Progress object to report training progress.
     */
	public void addClassifier(String name, Class classifierType) throws PropertyConfigurationException
	{
		addClassifier(name, classifierType, null);
	}

    /**
     * Add a classifier to this set of classifiers.
     *
     * @param name Name of a classifier to be added.
     * @param cl   Classifier to be added.
     */
    public void addClassifier(String name, Classifier cl)
    {
		if (m_ClassifierTypes.containsKey(name) || m_Classifiers.containsKey(name))
			throw new IllegalArgumentException("A classifier named "+name+" already added.");
        m_Classifiers.put(name, cl);
    }

    /**
     * Constructs classifiers to be tested.
     *
     * @param trainTable Training data set.
     */
    public void train(DoubleDataTable trainTable, Progress prog) throws InterruptedException
	{
    	if (m_ClassifierTypes.size()==0)
    	{
    		prog.set("Training classifiers", 1);
    		prog.step();
    		return;
    	}
    	int[] progressVolumes = new int[m_ClassifierTypes.size()];
    	progressVolumes[0] = 100/progressVolumes.length;
    	for (int i = 1; i < progressVolumes.length; i++)
    		progressVolumes[i] = 100*(i+1)/progressVolumes.length-progressVolumes[i-1];
    	prog = new MultiProgress("Training classifiers", prog, progressVolumes);
		for (Map.Entry<String,Class> cl : m_ClassifierTypes.entrySet())
		{
			m_Classifiers.remove(cl.getKey());
			try
			{
				Class classifierClass = cl.getValue();
				Properties prop = m_ClassifierProperties.get(cl.getKey());
				Classifier classifier = ClassifierFactory.createClassifier(classifierClass, prop, trainTable, prog);
				m_Classifiers.put(cl.getKey(), classifier);
			}
			catch (InvocationTargetException e)
			{
				if (e.getTargetException() instanceof BadHeaderException)
					Report.displaynl(cl.getKey()+" not trained: "+e.getTargetException().getMessage());
				else Report.exception((Exception)e.getTargetException());
			}
			catch (Exception e)
			{
				Report.exception(e);
			}
		}
	}

    /**
     * Classifies a test data set.
     *
     * @param tstTable  Test data set.
     * @param prog      Progress object for classification process.
     * @return          Map of entries: name of a classifier
     *                  and the object TestResult with a classification result.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public Map<String,TestResult> classify(DoubleDataTable tstTable, Progress prog) throws InterruptedException
    {
        // klasyfikacja tabeli testowej
        if (tstTable.noOfObjects()<=0) throw new RuntimeException("Classification of an empty table");
        NominalAttribute decAttr = tstTable.attributes().nominalDecisionAttribute();
        Map<String,int[][]> mapOfConfusionMatrices = new HashMap<String,int[][]>();
        prog.set("Classifing test table", tstTable.noOfObjects());
        for (DoubleData dObj : tstTable.getDataObjects())
        {
            int objDecLocalCode = decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision());
            for (Map.Entry<String,Classifier> cl : m_Classifiers.entrySet())
            {
                int[][] confusionMatrix = (int[][])mapOfConfusionMatrices.get(cl.getKey());
                if (confusionMatrix==null)
                {
                    confusionMatrix = new int[decAttr.noOfValues()][];
                    for (int i = 0; i < confusionMatrix.length; i++)
                        confusionMatrix[i] = new int[decAttr.noOfValues()];
                    mapOfConfusionMatrices.put(cl.getKey(), confusionMatrix);
                }
                try
                {
                    double dec = cl.getValue().classify(dObj);
                    if (!Double.isNaN(dec))
                    	confusionMatrix[objDecLocalCode][decAttr.localValueCode(dec)]++;
                }
                catch (RuntimeException e)
                {
                    Report.exception(e);
                }
                catch (PropertyConfigurationException e)
                {
                    Report.exception(e);
                }
            }
            prog.step();
        }
        // przygotowanie wynikow klasyfikacji
        Map<String,TestResult> resultMap = new HashMap<String,TestResult>();
        for (Map.Entry<String,Classifier> cl : m_Classifiers.entrySet())
        {
            int[][] confusionMatrix = (int[][])mapOfConfusionMatrices.get(cl.getKey());
            cl.getValue().calculateStatistics();
            TestResult results = new TestResult(decAttr, tstTable.getDecisionDistribution(), confusionMatrix, cl.getValue().getStatistics());
            resultMap.put(cl.getKey(), results);
        }
        return resultMap;
    }
}

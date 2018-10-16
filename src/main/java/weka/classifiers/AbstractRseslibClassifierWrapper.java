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


package weka.classifiers;

import java.util.ArrayList;
import java.util.Properties;

import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import rseslib.processing.classification.Classifier;
import rseslib.processing.classification.ClassifierFactory;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.formats.ArffDoubleDataInput;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;
import rseslib.system.progress.StdOutProgress;

/**
 * Abstract wrapper to provide rseslib classifiers as Weka classifiers.
 *
 * @author      Arkadiusz Wojna
 */
public abstract class AbstractRseslibClassifierWrapper extends weka.classifiers.AbstractClassifier
{
	/** for serialization */
	static final long serialVersionUID = 1L;

	private weka.core.Attribute m_WekaDecAttribute;
	
	private ArffDoubleDataInput m_WekaInstancesConverter;
	
	private Properties m_DefaultOptions;

	private Properties m_Options;

	private Class<? extends Classifier> m_ClassifierClass;
	
	private Classifier m_RseslibClassifier;
	
	/**
	 * Constructor.
	 * 
	 * @param classifierClass	Class of rseslib classifier
	 * @throws Exception
	 */
	public AbstractRseslibClassifierWrapper(Class<? extends Classifier> classifierClass) throws Exception
	{
		m_ClassifierClass = classifierClass;
		m_DefaultOptions = Configuration.loadDefaultProperties(m_ClassifierClass);
		resetToDefaults();
	}

	/**
	 * Reset options to default values. 
	 */
	protected void resetToDefaults() throws Exception
	{
		m_Options = (Properties)m_DefaultOptions.clone();
	}

	/**
	 * Properties used to configure the wrapped classifier. 
	 * 
	 * @return	Properties used to configure the wrapped classifier
	 */
	protected Properties getProperties()
	{
		return m_Options;
	}
	
	/**
	 * Returns an instance of a TechnicalInformation object, containing 
	 * detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation()
	{
		TechnicalInformation 	result;

		result = new TechnicalInformation(Type.MANUAL);
		result.setValue(Field.AUTHOR, "Arkadiusz Wojna, Rafal Latkowski, Lukasz Kowalski");
		result.setValue(Field.TITLE, "Rseslib: User Guide");
		result.setValue(Field.URL, "http://rseslib.mimuw.edu.pl/rseslib.pdf");

		return result;
	}

	/**
	 * Returns default capabilities of the classifier.
	 *
	 * @return      the capabilities of this classifier
	 */
	public Capabilities getCapabilities()
	{
		Capabilities result = new Capabilities(this);
		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);
		// class
		result.enable(Capability.NOMINAL_CLASS);
		return result;
	}
	
	/**
	 * Generates the classifier.
	 *
	 * @param instances the data to train the classifier with
	 * @throws Exception if classifier can't be built successfully
	 */
    public void buildClassifier(Instances data) throws Exception
    {
    	m_WekaDecAttribute = data.classAttribute();
    	m_WekaInstancesConverter = new ArffDoubleDataInput(data);
    	ArrayList<DoubleData> objects = new ArrayList<DoubleData>();
        while (m_WekaInstancesConverter.available())
        {
            DoubleData dObject = m_WekaInstancesConverter.readDoubleData();
            objects.add(dObject);
        }
        DoubleDataTable rseslib_tab = new ArrayListDoubleDataTable(objects);
        Progress prog = null;
        if (getDebug())
        	prog = new StdOutProgress();
        else
        	prog = new EmptyProgress();
        m_RseslibClassifier = ClassifierFactory.createClassifier(m_ClassifierClass, m_Options, rseslib_tab, prog);
    }

    /**
     * Classifies an instance.
     *
     * @param instance the instance to classify
     * @return the classification for the instance
     * @throws Exception if instance can't be classified successfully
     */
    public double classifyInstance(Instance instance) throws Exception
    {
    	DoubleData dObj = m_WekaInstancesConverter.convertToDoubleData(instance);
    	double rseslibResult = m_RseslibClassifier.classify(dObj);
    	if(Double.isNaN(rseslibResult))
    		return Double.NaN;
		String result = NominalAttribute.stringValue(rseslibResult);
		return m_WekaDecAttribute.indexOfValue(result);
    }
    
    /**
     * Returns native Rseslib classifier.
     * 
     * @return native Rseslib classifier.
     */
    public Classifier getRseslibClassifier()
    {
    	return m_RseslibClassifier;
    }
    
	/**
	 * Returns a description of the classifier.
	 * 
	 * @return a description of the classifier
	 */
	public String toString()
	{
		if (m_RseslibClassifier != null) {
			m_RseslibClassifier.calculateStatistics();
			return m_RseslibClassifier.getStatistics().toString();
		}
		return new String();
	}
}

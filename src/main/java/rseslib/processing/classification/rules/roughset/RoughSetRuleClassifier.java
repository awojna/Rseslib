/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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


package rseslib.processing.classification.rules.roughset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import rseslib.system.*; 
import rseslib.system.progress.Progress;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.*;
import rseslib.structure.rule.*;
import rseslib.processing.classification.Classifier;
import rseslib.processing.classification.ClassifierWithDistributedDecision;
import rseslib.processing.discretization.DiscretizationFactory;
import rseslib.processing.rules.ReductRuleGenerator;
import rseslib.processing.transformation.TableTransformer;
import rseslib.processing.transformation.TransformationProvider;
import rseslib.processing.transformation.Transformer;

/**
 * Rough set based classifier.
 * It discretizes the training table,
 * computes reducts and generates rules from the reducts.
 * While classifying a test object,
 * it sums the supports of all rules matching the object for each decision class
 * and assigns the decision with the greatest sum.
 * 
 * @author Rafal Latkowski
 */
public class RoughSetRuleClassifier extends ConfigurationWithStatistics implements Classifier, ClassifierWithDistributedDecision, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Discretizer used to discretize numerical attributes. */
	Transformer m_cDiscretizer = null;
	/** Generated rules used for classification. */
    Collection<Rule> m_cDecisionRules = null;
    /** Data attributes. */
    Header m_DiscrHeader;
    /** Decision attribute. */
    NominalAttribute m_DecAttr;
    
    /**
     * Constructor required by rseslib tools.
     * It discretizes the training table,
     * computes reducts and generates rules from the reducts. 
     *
     * @param prop					 Parameters of this classifier.
     * @param trainTable             Table used to generate reducts and rules.
     * @param prog                   Progress object for reporting training progress.
     * @throws PropertyConfigurationException	when the parameters are incorrect or incomplete.
     * @throws InterruptedException				when a user interrupts execution.
     */
    public RoughSetRuleClassifier(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        super(prop);
        TransformationProvider discrProv = DiscretizationFactory.getDiscretizationProvider(getProperties());
        if (discrProv != null)
        	m_cDiscretizer = discrProv.generateTransformer(trainTable);
    	if (m_cDiscretizer != null)
    		trainTable = TableTransformer.transform(trainTable, m_cDiscretizer);
    	m_DiscrHeader = trainTable.attributes();
        m_DecAttr = m_DiscrHeader.nominalDecisionAttribute();
        m_cDecisionRules = new ReductRuleGenerator(getProperties()).generate(trainTable, prog);
    }

    /**
     * Constructor based on a prepared set of rules.
     * 
     * @param	Prepared set of rules.
     * @param	Decision attribute.
     */
    public RoughSetRuleClassifier(Collection<Rule> rules, NominalAttribute decAttr)
    {
        m_cDecisionRules=rules;
        m_DecAttr = decAttr;
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
     * @param in			Input for reading.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readConfigurationAndStatistics(in);
    	in.defaultReadObject();
    }

    /**
     * Assigns a decision distribution to a single test object.
     * The method sums the supports of all rules matching the object for each decision class
     * and assigns the decision with the greatest sum.
     *
     * @param object  Test object to be classified.
     * @return        Decision assigned.
     */
    public double[] classifyWithDistributedDecision(DoubleData object)
    {
    	if (m_cDiscretizer != null)
    		object = m_cDiscretizer.transformToNew(object);
        Vector dv = new Vector(m_DecAttr.noOfValues());
        for (Rule rule : m_cDecisionRules)
            if (rule.matches(object)) 
                dv.add(((DistributedDecisionRule)rule).getDecisionVector()); 
        double[] result = new double[dv.dimension()];
       	for (int i=0; i<dv.dimension(); i++)
       		result[i] = dv.get(i);
       	return result;
    }
    
    /**
     * Assigns a decision to a single test object.
     * The method sums the supports of all rules matching the object for each decision class
     * and assigns the decision with the greatest sum.
     *
     * @param object  Test object to be classified.
     * @return        Decision assigned.
     */
    public double classify(DoubleData object)
    {
       	double[] decDistr = classifyWithDistributedDecision(object);
       	int best = 0;
       	for (int i=1; i<decDistr.length; i++)
       		if (decDistr[i] > decDistr[best]) 
                best=i;
       	if (decDistr[best] == 0.0) return Double.NaN;
       	return m_DecAttr.globalValueCode(best);
    }
    
    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
    	addToStatistics("number_of_rules",Integer.toString(m_cDecisionRules.size()));
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
    
    /**
     * Returns the collection of rules induced by this classifier.
     * 
     * @return Collection of rules induced by this classifier.
     */
    public Collection<Rule> getRules()
    {
        return m_cDecisionRules;
    }
}

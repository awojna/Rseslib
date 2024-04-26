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


package rseslib.processing.classification.rules;

import java.util.Collection;
import java.util.Properties;

import rseslib.processing.classification.Classifier;
import rseslib.processing.rules.AccurateRuleGenerator;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.rule.Rule;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Classifier that constructs the accurate rules from training objects
 * and assigns the decision from the first rule that matches a test object
 * or the decision from the class with the largest number of members
 * in a training set if no rule matches a test object.
 *
 * @author      Arkadiusz Wojna
 */
public class MajorityClassifierWithRules extends ConfigurationWithStatistics implements Classifier
{
    /** Decision attribute. */
    NominalAttribute m_DecisionAttribute;
    /** Majority decision computed from a training data set. */
    private int m_nMajorityDecision;
    /** Number of objects with majority decision in a training set. */
    private int m_nNoOfMajorityObjects;
    /** Number of all objects in a training set. */
    private int m_nNoOfAllObjects;
    /** Number of test objects that match a rule. */
    private int m_nNoOfMatchesWithRules = 0;
    /** The set of induced decision rules. */
    private Collection<Rule> m_Rules;

    /**
     * Constructor computes majority decision and generates accurate rules.
     *
     * @param prop                Parameters of this clasifier. If null,
     *                            parameters are loaded from the configuration directory.
     * @param trainTable          Table used to train classifier.
     * @param prog                Progress object for training process.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public MajorityClassifierWithRules(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        super(prop);
        m_DecisionAttribute = trainTable.attributes().nominalDecisionAttribute();
        // liczenie najczestszej decyzji
        int[] decDistr = trainTable.getDecisionDistribution();
        m_nMajorityDecision = 0;
        for (int dec = 1; dec < decDistr.length; dec++)
            if (decDistr[dec] > decDistr[m_nMajorityDecision]) m_nMajorityDecision = dec;
        m_nNoOfMajorityObjects = decDistr[m_nMajorityDecision];
        m_nNoOfAllObjects = trainTable.noOfObjects();
        // generowanie regul
        m_Rules = (new AccurateRuleGenerator(null)).generate(trainTable, prog);
    }

    /**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
     */
    public double classify(DoubleData dObj)
    {
       	for (Rule r : m_Rules)
        {
            if (r.matches(dObj))
            {
                m_nNoOfMatchesWithRules++;
                return r.getDecision();
            }
        }
        return m_DecisionAttribute.globalValueCode(m_nMajorityDecision);
    }

    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
        addToStatistics("Majority class in a training set", ""+m_nNoOfMajorityObjects+"/"+m_nNoOfAllObjects);
        addToStatistics("Number of matches with rules", Integer.toString(m_nNoOfMatchesWithRules));
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    	m_nNoOfMatchesWithRules = 0;
    }
}

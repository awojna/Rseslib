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


package rseslib.processing.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.function.booleanval.AttributeEquality;
import rseslib.structure.function.booleanval.BooleanFunction;
import rseslib.structure.function.booleanval.Conjunction;
import rseslib.structure.rule.*;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * AccurateRuleGenerator generates a collection
 * that contains one accurate rule for each object.
 *
 * @author      Arkadiusz Wojna
 */
public class AccurateRuleGenerator extends Configuration implements RuleGenerator
{
    /** Name of the property defining the maximal number of generated rules. */
    public static final String MAX_NO_OF_RULES_PROPERTY_NAME = "maxNumberOfRules";

    /** The maximal number of generated rules. */
    private int m_nMaximalNoOfRules = getIntProperty(MAX_NO_OF_RULES_PROPERTY_NAME);

    /**
     * Constructor.
     *
     * @param prop                Parameters of this rule generator. If null,
     *                            parameters are loaded from the configuration directory.
     */
    public AccurateRuleGenerator(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
    }

    /**
     * Returns a collection of rules generated from data table.
     *
     * @param tab  Data objects to be used for rule generation.
     * @return     Collection of generated rules.
     */
    public Collection<Rule> generate(DoubleDataTable tab, Progress prog) throws InterruptedException
    {
        prog.set("Generating accurate rules", 1);
        int noOfDescriptors = 0;
        for (int attr = 0; attr < tab.attributes().noOfAttr(); attr++)
            if (tab.attributes().isConditional(attr)) noOfDescriptors++;
        Collection<Rule> rules;
        if (m_nMaximalNoOfRules < tab.noOfObjects()) rules = new ArrayList<Rule>(m_nMaximalNoOfRules);
        else rules = new ArrayList<Rule>(tab.noOfObjects());
        Iterator<DoubleData> objIter = tab.getDataObjects().iterator();
        while (objIter.hasNext() && rules.size() < m_nMaximalNoOfRules)
        {
            DoubleDataWithDecision obj = (DoubleDataWithDecision)objIter.next();
            BooleanFunction[] descriptors = new BooleanFunction[noOfDescriptors];
            int descr = 0;
            for (int attr = 0; attr < tab.attributes().noOfAttr(); attr++)
                if (tab.attributes().isConditional(attr))
                    descriptors[descr++] = new AttributeEquality(tab.attributes().attribute(attr), attr, obj.get(attr));
            rules.add(new BooleanFunctionRule(new Conjunction(descriptors), obj.getDecision(), tab.attributes().nominalDecisionAttribute()));
        }
        prog.step();
        return rules;
    }
}

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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.rule.*;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;


/**
 * @author Rafal Latkowski
 *
 */
public class RuleStatisticsProvider
{
    public RuleStatisticsProvider()
    {
    }

    public void calculateStatistics(Collection<Rule> rules,DoubleDataTable table)
    {
        int dec_attr = table.attributes().decision();
        NominalAttribute decAttr = table.attributes().nominalDecisionAttribute();
        HashMap<Integer,Integer> dec_mapping = new HashMap<Integer,Integer>();
        HashSet<Rule> removed_rules = new HashSet<Rule>();
        for (int i=0;i<decAttr.noOfValues();i++)
        {
            dec_mapping.put((int)decAttr.globalValueCode(i),i);
        }
        for (Rule rule : rules)
        {
            int support = 0;
            int[] decs = new int[decAttr.noOfValues()];
            Arrays.fill(decs,0);
            //System.out.println("Rule "+rule.toString());
            for (DoubleData object : table.getDataObjects())
            {
                if (rule.matches(object))
                {
                    support++;
                    int dec_idx=dec_mapping.get((int)object.get(dec_attr));
                    decs[dec_idx]++;
                    //System.out.println("matches object "+object.toString());
                }
            }
            if (support==0) removed_rules.add(rule);
            ((AbstractDistrDecRuleWithStatistics)rule).setSupport(support);
            Vector dv = new Vector(decs.length);
            for (int i=0;i<decs.length;i++) dv.set(i,decs[i]);
            ((AbstractDistrDecRuleWithStatistics)rule).setDecisionVector(dv,decAttr);
        }
        rules.removeAll(removed_rules);
    }
}

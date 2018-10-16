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

import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.rule.Rule;
import rseslib.structure.table.DoubleDataTable;

/**
 * OneRuleGenerator generates one rule.
 * 
 * @author	Cezary Tkaczyk
 */
public interface OneRuleGenerator
{
    /**
     * Returns one rule generated upon two data tables.
     *
     * @param examples   Data objects to be used for whole 
     * 					 rule generation process.
     * @param uncovered  Data objects not covered in process
     * @param k			 The width of searching. The greater it is, 
     * 					 the longest time it consumes, but the better 
     * 					 results it gives.
     * @param d          The decision for rule to search. Some algorithm
     * 					 ignores that.
     * @return           One rule.
     */
	public abstract Rule generate(DoubleDataTable examples, 
								  ArrayList<DoubleDataWithDecision> uncovered,
								  int k, double d);
	
}

/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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

import java.util.Properties;

import javax.swing.JPanel;

import rseslib.processing.classification.VisualClassifier;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * Visual rough set based classifier.
 * 
 * @author Krzysztof Niemkiewicz
 */

public class RoughSetRulesVisual extends RoughSetRules implements VisualClassifier
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
    
	/** Attributes of the original training table. */
    private Header trainHeader;
    
    /**
     * Constructor required by rseslib tools.
     *
     * @param prop                   Settings of this clasifier.
     * @param trainTable             Table used to generate rules.
     * @param prog                   Progress object to report training progress.
     * @throws InterruptedException 			when a user interrupts the execution.
     * @throws PropertyConfigurationExcpetion 	when the properties are incorrect.
     */

    public RoughSetRulesVisual(Properties prop, DoubleDataTable trainTable, Progress prog)
	throws PropertyConfigurationException, InterruptedException {	
    	super(prop, trainTable, prog);
    	trainHeader=trainTable.attributes();	
    }

    /**
     * This methods draws visualization of whole classifier(all rules) on
     * given JPanel
     * @param canvas JPanel on which this method draws 
     */

    public void draw(JPanel canvas) { 
	    canvas.add(new VisualRoughSetPanel(this,null));
    }
    
    /**
     * This methods draws visualization of classification of given object(show only matching rules)
     * @param canvas JPanel on which this method draws 
     * @param obj object which classification will be shown
     */
    
    public void drawClassify(JPanel canvas, DoubleData obj) {
    	if (m_cDiscretizer != null)
    		obj = m_cDiscretizer.transformToNew(obj);
    	canvas.removeAll();
    	canvas.add(new VisualRoughSetPanel(this,
					   new RuleMatchSelector(obj)));
    }

    /**
     * This method returns the attributes of the original training table,
     * required to implement VisualClassifer.
     * @return Header of the original training table
     */
    
    public Header attributes() {
    	return trainHeader;
    };

    /**
     * This method returns the discretized attributes.
     * @return Header with the discretized attributes
     */
    public Header getDiscretizedHeader() {
    	return m_DiscrHeader;
    }
};

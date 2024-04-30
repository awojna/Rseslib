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


package rseslib.qmak.UI;


import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import rseslib.qmak.QmakMain;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.system.Configuration;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;

/**
 * 
 * @author Leszek Tur
 * na podstawie TClassifierPropertiesManipulator z trickstera
 */
public class QClassifierPropertiesManipulator {
    private Properties prop;

    private JLabel[] labels;
    private JTextField[] textFields;
    private JComboBox[] comboBoxes;

    public QClassifierPropertiesManipulator(Properties pr, iQClassifier cl) {
    	HashMap<String, String[]> possibleVals;
    	try {
    		possibleVals = Configuration.possibleValues(cl.getClassifierClass());
    	} catch (PropertyConfigurationException e) {
			e.printStackTrace();
			possibleVals = new HashMap<String, String[]>();
		}
      if (pr == null)
    	  prop = new Properties();
      else
    	  prop=(Properties)pr.clone();

      labels = new JLabel[prop.size()];
      textFields = new JTextField[prop.size()];
      comboBoxes = new JComboBox[prop.size()];
      
        Enumeration enum_names = prop.propertyNames();

        int el = 0;
        while(enum_names.hasMoreElements())
        {
        	String s=(String)enum_names.nextElement();
        	JLabel label = new JLabel(s,SwingConstants.RIGHT);
        	labels[el] = label;
        	JComponent comp;
        	if(possibleVals.containsKey(s)) {
        		comboBoxes[el] = new JComboBox(possibleVals.get(s));
        		comboBoxes[el].setSelectedItem(prop.getProperty(s));
        		comp = comboBoxes[el];
        	} else {
            	textFields[el] = new JTextField(prop.getProperty(s));
            	comp = textFields[el];
        	}

        	if ((cl.isTrained()) && 
        			(!((ConfigurationWithStatistics) cl.getClassifier()).isModifiableProperty(s))) {
//        		textField.setBackground(Color.pink);
    			comp.setEnabled(false);
        	} else {
        		comp.setEnabled(true);
        	}
        	
        	++el;
        }

        if(prop.isEmpty())
        {
        	labels = new JLabel[1];
        	labels[0] = new JLabel(QmakMain.getMainFrame().messages.getString("NotConfigurable"));
        }
    }
    
    public void draw(JPanel panel)
    {
    	if(prop.isEmpty())
    		panel.add(labels[0], new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER,
                    GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
    	else {
    		GridLayout layGrid =new GridLayout(labels.length,2);
    		layGrid.setHgap(7);
    		layGrid.setVgap(3);
    		panel.setLayout(layGrid);
    		Border bo = BorderFactory.createEmptyBorder(10, 10, 10, 10);//BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    		panel.setBorder(bo);
    		for(int i=0;i<labels.length;i++)
    		{
    			panel.add(labels[i]);
    			if(textFields[i] != null)
    				panel.add(textFields[i]);
    			else
    				panel.add(comboBoxes[i]);
    		}
    	}
    }

    public Properties getProperties()
    {
    	for(int i=0;i<labels.length;i++) {
    		String s;
    		if(textFields[i] != null) {
    			s=textFields[i].getText();
    		} else {
    			s=(String)comboBoxes[i].getSelectedItem();
    		}
//   		if(s.equals(""))throw new BadAttributeValue();
			prop.setProperty(labels[i].getText(),s);
    	}
    	return prop;
    }
    
    
}

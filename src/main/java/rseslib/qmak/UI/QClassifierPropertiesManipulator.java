/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import rseslib.qmak.QmakMain;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.system.ConfigurationWithStatistics;

/**
 * 
 * @author Leszek Tur
 * na podstawie TClassifierPropertiesManipulator z trickstera
 */
public class QClassifierPropertiesManipulator {
    private Properties prop;

    private Vector<JLabel> labels=new Vector<JLabel>();
    private Vector<JTextField> textFields=new Vector<JTextField>();

    public QClassifierPropertiesManipulator(Properties pr, iQClassifier cl) {
      if (pr == null)
        prop = new Properties();
      else
      prop=(Properties)pr.clone();

        Enumeration enum_names = prop.propertyNames();
        JLabel label;
        JTextField textField;

        while(enum_names.hasMoreElements())
        {
        	String s=(String)enum_names.nextElement();
        	label=new JLabel(s,SwingConstants.RIGHT);
        	textField=new JTextField(prop.getProperty(s));

        	if ((cl.isTrained()) && 
        	(!((ConfigurationWithStatistics) cl.getClassifier()).isModifiableProperty(s))){
//        		textField.setBackground(Color.pink);
    			textField.setEnabled(false);
        	} else {
        		textField.setEnabled(true);
        	}


        	labels.add(label);
        	textFields.add(textField);
        }

        if(prop.isEmpty())
        {
        	label=new JLabel(QmakMain.getMainFrame().messages.getString("NotConfigurable"));
        	labels.add(label);
        }
    }
    
    public void draw(JPanel panel)
    {
    	if(prop.isEmpty())
    		panel.add(labels.elementAt(0), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER,
                    GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
    	else {
    		GridLayout layGrid =new GridLayout(labels.size(),2);
    		layGrid.setHgap(7);
    		layGrid.setVgap(3);
    		panel.setLayout(layGrid);
    		Border bo = BorderFactory.createEmptyBorder(10, 10, 10, 10);//BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    		panel.setBorder(bo);
    		for(int i=0;i<labels.size();i++)
    		{
    		panel.add(labels.elementAt(i));
      		panel.add(textFields.elementAt(i));
    		}
    	}
    }

    public Properties getProperties()
    {
    	for(int i=0;i<textFields.size();i++)
    	{
    		String s=(textFields.elementAt(i)).getText();
//    		if(s.equals(""))throw new BadAttributeValue();
    		prop.setProperty((labels.elementAt(i)).getText(),s);
    	}
    	return prop;
    }
    
    
}

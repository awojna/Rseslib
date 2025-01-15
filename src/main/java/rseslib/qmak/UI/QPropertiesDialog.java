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


package rseslib.qmak.UI;


import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import rseslib.qmak.QmakMain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;


/**
 * Klasa odpowiedzialna za wyswietlanie okienka z wlasciwosciami przy testach wielokrotnych
 * na multiklasyfikatorze.
 * 
 * @author Leszek Tur
 *
 */
public class QPropertiesDialog extends JDialog {
    private Properties prop_orig;
    private Properties prop_ret;//te ktore zwracamy
    
    private Vector<JLabel> labels=new Vector<JLabel>();
    private Vector<JTextField> textFields=new Vector<JTextField>();
    private boolean ustaw = false;

    // obiekty interfejsu
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton buttonOk = new JButton();
    JButton buttonCancel = new JButton();
    JPanel jPanel2 = new JPanel();
    
    public QPropertiesDialog(Frame owner, boolean modal, Properties pr) {
        super(owner, modal);
        try {
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            jbInit();
            initialize(pr);  
            drawProp(jPanel2);
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    
    private void initialize(Properties pr){
        if (pr == null)
            prop_orig = new Properties();
        else
        	prop_orig=(Properties)pr.clone();
        Enumeration enum_names = prop_orig.propertyNames();
        JLabel label;
        JTextField textField;

        while(enum_names.hasMoreElements())
        {
        	String s=(String)enum_names.nextElement();
        	label=new JLabel(s,SwingConstants.RIGHT);
        	textField=new JTextField(prop_orig.getProperty(s));
        	labels.add(label);
        	textFields.add(textField);
        }

        if(prop_orig.isEmpty())
        {
        	label=new JLabel(QmakMain.getMainFrame().messages.getString("NotConfigurable"));
        	labels.add(label);
        }
    }
    


    private void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        this.setModal(false);
        this.setTitle(QmakMain.getMainFrame().messages.getString("ChangeProperties"));
        buttonOk.setAction(null);
        buttonOk.setText("OK");
        buttonCancel.setText("Cancel");
        
        
        buttonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonOk_actionPerformed(e);
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttonCancel_actionPerformed(e);
            }
        });
             
        this.getContentPane().add(panel1, java.awt.BorderLayout.CENTER);
        
        jPanel1.add(buttonOk);
        jPanel1.add(buttonCancel);
        
        panel1.add(jPanel1, java.awt.BorderLayout.SOUTH);
        panel1.add(jPanel2, java.awt.BorderLayout.CENTER);
    }

   
    private void drawProp(JPanel panel)
    {
    	if(prop_orig.isEmpty())
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
    
    private void setPropRet()
    {
    	prop_ret = (Properties) prop_orig.clone();
    	for(int i=0;i<textFields.size();i++)
    	{
    		String s=(textFields.elementAt(i)).getText();
    		prop_ret.setProperty((labels.elementAt(i)).getText(),s);
    	}
    }
    
    public Properties getProperties(){
    	return prop_ret;
    }


    public void buttonOk_actionPerformed(ActionEvent e) {
    	setPropRet();
    	ustaw = true;
    	setVisible(false);
    }

    public void buttonCancel_actionPerformed(ActionEvent e) {
    	prop_ret = (Properties) prop_orig.clone();
    	ustaw = false;
        setVisible(false);
    }
    
    public boolean czyOK(){
    	return ustaw;
    }
}



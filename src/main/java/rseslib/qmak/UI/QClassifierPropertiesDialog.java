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


package rseslib.qmak.UI;


import java.awt.*;
import javax.swing.*;

import rseslib.qmak.UI.QClassifierPropertiesManipulator;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.classifier.iQClassifier;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;


/**
 * 
 * @author Leszek Tur
 * na podstawie TClassifierPropertiesDialog z trickstera
 *
 */
public class QClassifierPropertiesDialog extends JDialog {
    private Properties prop_orig;
    private iQClassifier klasyfikator;

    private QClassifierPropertiesManipulator cpm;
    private boolean ustaw = false;

    // obiekty interfejsu
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton buttonOk = new JButton();
    JButton buttonCancel = new JButton();
    JPanel jPanel2 = new JPanel();
    
    public QClassifierPropertiesDialog(Frame owner, boolean modal) {
        super(owner, modal);
        try {
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            jbInit();
            pack();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    private void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        this.setModal(false);
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


    void assignData(iQClassifier cl) {
        klasyfikator = cl;
        this.setTitle(cl.getName() + " " + QMainFrame.getMainFrame().messages.getString("ClassifierProperties"));
        prop_orig = cl.getProperties();
        cpm = new QClassifierPropertiesManipulator(prop_orig,cl);
        cpm.draw(jPanel2);
        pack();
    }

    public void buttonOk_actionPerformed(ActionEvent e) {
    	  klasyfikator.setProperties(cpm.getProperties());
    	  ustaw = true;
    	  setVisible(false);
    }

    public void buttonCancel_actionPerformed(ActionEvent e) {
    	ustaw = false;
        setVisible(false);
    }
    
    public boolean czyOK(){
    	return ustaw;
    }
}

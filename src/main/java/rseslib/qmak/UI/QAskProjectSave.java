/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.Dimension;
import javax.swing.JPanel;

import rseslib.qmak.UI.QMainFrame;

import javax.swing.BoxLayout;
import java.awt.ComponentOrientation;
import javax.swing.BorderFactory;
import java.awt.Font;
import java.awt.FlowLayout;

/**
 * Klasa pytajaca czy uzytkownik chce zapisac projekt przed jego zamknieciem
 * @author Damin Wojcik
 *
 */
public class QAskProjectSave extends JDialog implements ActionListener {

	private JPanel jContentPane = null;
	private JPanel jPanel1 = null;
	private JLabel jClose_label = null;
	private JPanel jPanel = null;
	private JButton jYes_Button = null;
	private JButton jNo_Button = null;
	private JButton jResign_Button = null;
	
	private QMainFrame owner;
	
	
	/**
	 * This method initializes 
	 * 
	 */
	public QAskProjectSave(QMainFrame my_owner) {
		super();
		owner = my_owner;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(300, 100));
        this.setPreferredSize(new Dimension(300, 100));
        this.setName("dialog_close");
        this.setMaximumSize(new Dimension(300, 100));
        this.setMinimumSize(new Dimension(210, 100));
        this.setContentPane(getJContentPane());
			
	}

	/**
	 * This method sets the dialog wisible for user
	 *
	 */
	public void showDialog() {
		this.setVisible(true);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == jResign_Button) { 
			this.setVisible(false);
			};
		if (event.getSource() == jNo_Button) {
			this.setVisible(false);
			owner.closeProject();
		}
		if (event.getSource() == jYes_Button) {
			this.setVisible(false);
			owner.closeAndSaveProject();
		}		
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BoxLayout(getJContentPane(), BoxLayout.Y_AXIS));
			jContentPane.setComponentOrientation(ComponentOrientation.UNKNOWN);
			jContentPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			jContentPane.setName("jClosePane");
			jContentPane.setPreferredSize(new Dimension(240, 100));
			jContentPane.add(getJPanel1(), null);
			jContentPane.add(getJPanel(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jClose_label = new JLabel();
			jClose_label.setFont(new Font("Dialog", Font.BOLD, 14));
			jClose_label.setText(owner.messages.getString("QAskProjectSave"));
			//jClose_label.setPreferredSize(new Dimension(200, 40));
			jPanel1 = new JPanel();
			jPanel1.setLayout(new FlowLayout());
			jPanel1.add(jClose_label, null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
			jPanel.setPreferredSize(new Dimension(240, 40));
			jPanel.add(getJYes_Button(), null);
			jPanel.add(getJNo_Button(), null);
			jPanel.add(getJResign_Button(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jYes_Button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJYes_Button() {
		if (jYes_Button == null) {
			jYes_Button = new JButton();
			jYes_Button.setName("jYes_button");
			jYes_Button.setText(owner.messages.getString("jYes"));
			jYes_Button.setPreferredSize(new Dimension(80, 20));
			jYes_Button.addActionListener(this);
		}
		return jYes_Button;
	}

	/**
	 * This method initializes jNo_Button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJNo_Button() {
		if (jNo_Button == null) {
			jNo_Button = new JButton();
			jNo_Button.setName("jNo_Button");
			jNo_Button.setText(owner.messages.getString("jNo"));
			jNo_Button.setPreferredSize(new Dimension(80, 20));
			jNo_Button.addActionListener(this);
		}
		return jNo_Button;
	}

	/**
	 * This method initializes jResign_Button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJResign_Button() {
		if (jResign_Button == null) {
			jResign_Button = new JButton();
			jResign_Button.setPreferredSize(new Dimension(80, 20));
			jResign_Button.setText(owner.messages.getString("jCancel"));
			jResign_Button.setName("jResgn_Button");
			jResign_Button.addActionListener(this);			
		}
		return jResign_Button;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

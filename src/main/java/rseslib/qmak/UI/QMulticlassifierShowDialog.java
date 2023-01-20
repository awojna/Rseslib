/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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

import javax.swing.JPanel;

import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import java.awt.GridBagLayout;
import java.awt.Dimension;
import javax.swing.JList;
import java.awt.GridBagConstraints;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.JPopupMenu;

import rseslib.qmak.UI.QClassifierPropertiesDialog;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.QmakMain;
import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;

import javax.swing.JMenuItem;

/**
 * Buduje okienko pokazujace zbior klasyfikatorow w multiklasyfikatorze.
 * 
 * @author Leszek Tur
 *
 */
public class QMulticlassifierShowDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	QMultiClassifier multiClassifier;
	DefaultListModel listaMcl;
	private JList jList = null;
	private JPopupMenu jPopupMenuShowClassfierProperties = null;
	private JMenuItem jMenuItemConfigure = null;
	private JMenuItem jMenuItemRemove = null;
	
	/**
	 * @param owner
	 */
	public QMulticlassifierShowDialog(Frame owner, QMultiClassifier mcl) {
		super(owner);
		setTitle(mcl.getName());
		multiClassifier = mcl;
		initialize();
		this.pack();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(329, 234);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setPreferredSize(new Dimension(200, 200));
			jPanel.add(getJList(), gridBagConstraints);
		}
		return jPanel;
	}

	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJList() {

		listaMcl = new DefaultListModel();
		
		for (Iterator it = multiClassifier.getClassifierNames().iterator(); it.hasNext();) {
			String el = (String) it.next();
			listaMcl.addElement(el);
		}

		jList = new JList(listaMcl);
		jList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					int x = e.getX();
					int y = e.getY();
					getJPopupMenuShowClassfierProperties().show(jList, x, y);
				}
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getClickCount() == 2) {
					int index = jList.getSelectedIndex();
					if (index>=0 && index <= multiClassifier.size()){
						QClassifierPropertiesDialog dlg = new QClassifierPropertiesDialog(QMainFrame.getMainFrame(), true);
						dlg.assignData(multiClassifier.getQClassifier((String) jList.getSelectedValue()));
						dlg.setLocationRelativeTo(QMainFrame.getMainFrame());
						dlg.pack();
						dlg.setModal(true);//wazna linijka - bez tego program dzialal dalej i trenowal klasyfikator
						dlg.setVisible(true);
					}
				}
			}
		});
		return jList;
	}

	/**
	 * This method initializes jPopupMenuShowClassfierProperties	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getJPopupMenuShowClassfierProperties() {
		if (jPopupMenuShowClassfierProperties == null) {
			jPopupMenuShowClassfierProperties = new JPopupMenu();
			jPopupMenuShowClassfierProperties.add(getJMenuItemConfigure());
			jPopupMenuShowClassfierProperties.add(getJMenuItemRemove());
		}
		return jPopupMenuShowClassfierProperties;
	}

	/**
	 * This method initializes jMenuItemConfigure	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItemConfigure() {
		if (jMenuItemConfigure == null) {
			jMenuItemConfigure = new JMenuItem();
			jMenuItemConfigure.setText(QmakMain.getMainFrame().messages.getString("Configure"));
			jMenuItemConfigure.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = jList.getSelectedIndex();
					if (index>=0 && index <= multiClassifier.size()){
						QClassifierPropertiesDialog dlg = new QClassifierPropertiesDialog(QMainFrame.getMainFrame(), true);
						dlg.assignData(multiClassifier.getQClassifier((String) jList.getSelectedValue()));
						dlg.setLocationRelativeTo(QMainFrame.getMainFrame());
						dlg.pack();
						dlg.setModal(true);//wazna linijka - bez tego program dzialal dalej i trenowal klasyfikator
						dlg.setVisible(true);
					}
				}
			});
		}
		return jMenuItemConfigure;
	}

	/**
	 * This method initializes jMenuItemRemove	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItemRemove() {
		if (jMenuItemRemove == null) {
			jMenuItemRemove = new JMenuItem();
			jMenuItemRemove.setText(QmakMain.getMainFrame().messages.getString("RemoveFromSet"));
			jMenuItemRemove.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int index = jList.getSelectedIndex();
					if (index>=0 && index <= multiClassifier.size()){
						multiClassifier.remove((String) listaMcl.get(index));
						listaMcl.removeElementAt(index);
					}
				}
			});
		}
		return jMenuItemRemove;
	}

	public void addItem(String el) {
		listaMcl.addElement(el);
	}
}  //  @jve:decl-index=0:visual-constraint="10,9"

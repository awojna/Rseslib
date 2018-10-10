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

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.ComponentOrientation;
import javax.swing.BoxLayout;
import java.awt.Dimension;
import javax.swing.SwingConstants;

import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.project.*;

import java.awt.event.KeyEvent;
import java.awt.Rectangle;

/**
 * @author damian
 *
 */
public class QNewProjectUI extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JPanel Qnp_contentpane = null;

	private JLabel jLabel = null;

	private JTextField jText_project = null;

	private JLabel jLabel1 = null;

	private JTextField jText_file = null;

	private JPanel jPanel = null;

	private JButton Qok_button = null;

	private JButton Qresign_button = null;

	private JLabel jLabel2 = null;

	private JTextField jText_author = null;
	
	private QMainFrame my_owner = null;

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == Qok_button) {
			this.setVisible(false);
			String project_name = jText_project.getText();
			String project_file = jText_file.getText();
			String project_author = jText_author.getText();
			if ((project_name.compareTo("")==0) || (project_file.compareTo("")==0) || (project_author.compareTo("")==0)) {
				my_owner.writeInfo("All fields must be filled");
				}
			else {
				String absolute_path;
				absolute_path = System.getProperty("user.dir") + System.getProperty("file.separator") + project_file + System.getProperty("file.separator") + project_file;
				my_owner.setProject(new QProject(project_name, absolute_path, project_author));
				my_owner.set_isProjectOpened(true);
				my_owner.setTitle(project_name);
				my_owner.jMainWindow.setEnabled(true);
			}

		}
		else {
			if (e.getSource() == Qresign_button) {
				this.setVisible(false);
			}
		}
	}
 

	/**
	 * @param owner
	 */
	public QNewProjectUI(QMainFrame owner) {
		super(owner);
		initialize();
	}

	/**
	 * Show dialog and remembers owner
	 *  @param owner
	 */
	public void showNewDialog (QMainFrame owner) {
		String user;
		
		my_owner = owner; 
		jText_project.setText("default");
		jText_file.setText("default");
		if ((user=System.getProperty("user.name")) == null) {
			user = "Anonymous";
		 }
		jText_author.setText(user);
		this.setVisible(true);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setPreferredSize(new Dimension(360, 280));
		this.setBounds(new Rectangle(0, 0, 360, 180));
		this.setName("Qnp_dialog");
		this.setMaximumSize(new Dimension(2000, 2000));
		this.setMinimumSize(new Dimension(360, 180));
		this.setTitle("New Project");
		this.setContentPane(getQnp_contentpane());
		this.setForeground(new Color(252, 255, 255));
		this.setVisible(false);
	}

	/**
	 * This method initializes Qnp_contentpane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getQnp_contentpane() {
		if (Qnp_contentpane == null) {
			jLabel2 = new JLabel();
			jLabel2.setText("Project author");
			jLabel2.setPreferredSize(new Dimension(140, 15));
			jLabel1 = new JLabel();
			jLabel1.setText("Project name");
			jLabel1.setPreferredSize(new Dimension(150, 15));
			jLabel1.setName("jLabel1");
			jLabel = new JLabel();
			jLabel.setText("Directory name");
			jLabel.setHorizontalTextPosition(SwingConstants.TRAILING);
			jLabel.setName("jLabel");
			Qnp_contentpane = new JPanel();
			Qnp_contentpane.setLayout(new BoxLayout(getQnp_contentpane(), BoxLayout.Y_AXIS));
			Qnp_contentpane.setBorder(BorderFactory.createCompoundBorder(null, null));
			Qnp_contentpane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			Qnp_contentpane.setEnabled(false);
			Qnp_contentpane.setName("Qnp_main");
			Qnp_contentpane.setBackground(Color.white);
			Qnp_contentpane.setForeground(Color.white);
			Qnp_contentpane.setPreferredSize(new Dimension(360, 240));
			Qnp_contentpane.add(jLabel1, null);
			Qnp_contentpane.add(getJText_project(), null);
			Qnp_contentpane.add(jLabel, null);
			Qnp_contentpane.add(getJText_file(), null);
			Qnp_contentpane.add(jLabel2, null);
			Qnp_contentpane.add(getJText_author(), null);
			Qnp_contentpane.add(getJPanel(), null);
		}
		return Qnp_contentpane;
	}

	/**
	 * This method initializes jText_project	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJText_project() {
		if (jText_project == null) {
			jText_project = new JTextField();
			jText_project.setName("jText_project");
			jText_project.setPreferredSize(new Dimension(100, 15));
			jText_project.setColumns(1);
		}
		return jText_project;
	}

	/**
	 * This method initializes jText_file	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJText_file() {
		if (jText_file == null) {
			jText_file = new JTextField();
			jText_file.setColumns(1);
			jText_file.setName("jText_file");
			jText_file.setPreferredSize(new Dimension(100, 15));
		}
		return jText_file;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.X_AXIS));
			jPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			jPanel.setPreferredSize(new Dimension(240, 20));
			jPanel.setForeground(Color.white);
			jPanel.setBackground(Color.white);
			jPanel.add(getQok_button(), null);
			jPanel.add(getQresign_button(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes Qok_button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getQok_button() {
		if (Qok_button == null) {
			Qok_button = new JButton();
			Qok_button.setPreferredSize(new Dimension(80, 20));
			Qok_button.setMnemonic(KeyEvent.VK_UNDEFINED);
			Qok_button.setText("OK");
			Qok_button.addActionListener(this);
		}
		return Qok_button;
	}

	/**
	 * This method initializes Qresign_button	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getQresign_button() {
		if (Qresign_button == null) {
			Qresign_button = new JButton();
			Qresign_button.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			Qresign_button.setText("Cancel");
			Qresign_button.setPreferredSize(new Dimension(80, 20));
			Qresign_button.addActionListener(this);
		}
		return Qresign_button;
	}

	/**
	 * This method initializes jText_author	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJText_author() {
		if (jText_author == null) {
			jText_author = new JTextField();
			jText_author.setPreferredSize(new Dimension(100, 15));
			jText_author.setName("jText_author");
		}
		return jText_author;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

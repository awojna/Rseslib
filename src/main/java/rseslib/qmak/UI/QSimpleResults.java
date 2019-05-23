/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JTable;
import java.awt.Color;
import java.awt.ComponentOrientation;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import rseslib.qmak.dataprocess.results.QMultipleTestResult;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;


/**
 * Class implementing window showing multiple test result for a table
 * 
 * @author Maciej Zuchniak
 *
 */
public class QSimpleResults extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	
	private JTable qSimpleResultTable = null;
	
	private String testType;

	private String testName;

	private String[] columnNames = {"Classifier name", "Average", "Standard Deviation"};

/*    String[] columnNames = {"First Name",
            "Last Name",
            "Sport",
            "# of Years",
            "Vegetarian"};
*/
    Object[][] data = {
    		{"Mary", 	new Integer(5),  new Boolean(false)},
   			{"Alison", 	new Integer(3),  new Boolean(true)},
			{"Kathy", 	new Integer(2),  new Boolean(false)},
    		{"Sharon",  new Integer(20), new Boolean(true)},
    		{"Philip",  new Integer(10), new Boolean(false)},
    };

	private JPanel closeButtPanel = null;

	private JPanel simpleResTabPanel = null;

	private JButton closeButt = null;
	

	
	/**
	 * @param owner
	 */
/*	public QSimpleResults(Frame owner, String text) {
		super(owner);
		this.text = text;
		initialize();
		this.pack();
	}
	
	public QSimpleResults() {
		initialize();
		this.pack();
	}
*/	
	public QSimpleResults(Frame owner, QMultipleTestResult qmtr) {
		super(owner);
		initialize(owner);
	}
	
	public QSimpleResults(Frame owner, String type, String name, Object[][] dataArray) {
		super(owner);
		testType = type;
		testName = name;
		data = dataArray;
		initialize(owner);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(Frame owner) {
		this.setSize(500, 200);
		this.setPreferredSize(new Dimension(500, 200));
		this.setMinimumSize(new Dimension(300, 100));
		this.setTitle(testName);
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(owner);
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
		}
		jContentPane.setBackground(SystemColor.control);
		jContentPane.setOpaque(true);
		jContentPane.setPreferredSize(new Dimension(264, 40));
		jContentPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jContentPane.add(getSimpleResTabPanel(), null);
		jContentPane.add(getCloseButtPanel(), null);
		return jContentPane;
	}

	private void close() {
		this.dispose();
	}

	/**
	 * This method initializes qSimpleResultTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getQSimpleResultTable() {
		if (qSimpleResultTable == null) {
			qSimpleResultTable = new JTable(data, columnNames);
			qSimpleResultTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
			qSimpleResultTable.setFillsViewportHeight(true);
			qSimpleResultTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			qSimpleResultTable.setForeground(Color.black);
			qSimpleResultTable.setBackground(new Color(202, 251, 202));
			qSimpleResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			qSimpleResultTable.setShowHorizontalLines(true);
			qSimpleResultTable.setCellSelectionEnabled(false);
			qSimpleResultTable.add(qSimpleResultTable.getTableHeader(), BorderLayout.PAGE_START);

			// Zmiana szerokosci kolumn
			TableColumn column = null;
			for (int i = 0; i < 3; i++) {
			    column = qSimpleResultTable.getColumnModel().getColumn(i);
			    if (i == 0) {
			        column.setPreferredWidth(200); //first column is bigger
			    } else {
			        column.setPreferredWidth(120);
			    }
			}
			
			//Create the scroll pane and add the table to it.
	        //Add the scroll pane to this panel.
		}
		return qSimpleResultTable;
	}
	
	/**
	 * This method initializes closeButtPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCloseButtPanel() {
		if (closeButtPanel == null) {
			closeButtPanel = new JPanel();
			closeButtPanel.setLayout(new BoxLayout(getCloseButtPanel(), BoxLayout.X_AXIS));
			closeButtPanel.setPreferredSize(new Dimension(100, 30));
			closeButtPanel.add(getCloseButt(), null);
		}
		return closeButtPanel;
	}

	/**
	 * This method initializes simpleResTabPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSimpleResTabPanel() {
		if (simpleResTabPanel == null) {
			simpleResTabPanel = new JPanel();
			simpleResTabPanel.setLayout(new BorderLayout());
			simpleResTabPanel.setBorder(BorderFactory.createTitledBorder(null, "Test type: " + testType, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			getQSimpleResultTable();
			JTableHeader tabheader = qSimpleResultTable.getTableHeader();
//			tabheader.setBackground(new Color());
			tabheader.setFont(new Font("Dialog", Font.BOLD, 12));
//			tabheader.set
			simpleResTabPanel.add(tabheader, BorderLayout.PAGE_START);
			simpleResTabPanel.add(qSimpleResultTable, BorderLayout.CENTER);
		}
		return simpleResTabPanel;
	}

	/**
	 * This method initializes closeButt	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCloseButt() {
		if (closeButt == null) {
			closeButt = new JButton();
			closeButt.setText("Close");
			closeButt.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		            button_actionPerformed(e);
		        }
		    });

		}
		return closeButt;
	}

	private void button_actionPerformed(ActionEvent e) {
		this.dispose();
	}

	
/*	public void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
*/

}  //  @jve:decl-index=0:visual-constraint="10,10"

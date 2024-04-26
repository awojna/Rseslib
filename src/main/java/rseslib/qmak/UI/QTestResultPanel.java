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


import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import rseslib.qmak.dataprocess.results.QTestResult;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

public class QTestResultPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 4962026205502786468L;

	QTestResult qtr = null;

	JPanel qConfMatrixPanel = null;
	JPanel qStatisticsPanel = null;
	
	private JTable table = null;



	private String clacc = "Classification accuracy";

	private JLabel claccLabel = null;

	
    public QTestResultPanel(QTestResult qtresult) {
    	qtr = qtresult;
    	initialize();
    }

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			this.add(getQConfMatrixPanel(), null);
			this.add(getQStatisticsPanel(), null);
	}

	
	private int getNoOfDecs() {
//		return 5;
		return qtr.noOfDecs();
	}
	
	private int howManyObjects(String name, int row, int col) {
//		return row*col;
		return qtr.howManyObjects(name, row, col);
	}
	
	private String decisionName(int nr) {
//		return "Decision name";
		return QTestResult.decisionName(nr);
	}
	
	private String getNameOfFirst() {
//		return "Name of first";
		return qtr.getNameOfFirst();
	}
	
	private String getAccuracy() {
//		return 6;
		return toPercents(qtr.getAccuracy(getNameOfFirst()));
	}
	
	private double getDecAccuracy(int no) {
		return qtr.getDecAccuracy(getNameOfFirst(), no);
	}
	
	private String toPercents(double x) {
		String s = "";
		int i = 0;
		int j = 0;
		i = (int) (10000*x);
		j = i%100;
		i = i/100;
		s = (Integer.toString(i)) + "." + (Integer.toString(j)) + "%";
		return s;
	}
	

	
	class ConfMatrixTableModel extends AbstractTableModel {
		
        /**
		 * 
		 */
		private static final long serialVersionUID = 6294422472688236323L;
		private String[] columnNames;
        private Object[][] data;
        
        ConfMatrixTableModel() {
        	int colCnt = getNoOfDecs() + 2;
        	int rowCnt = getNoOfDecs() + 1;
        	int ip = 1;
        	int jp = 1;
//        	qtr.printResult();
        	data        = new Object[rowCnt][colCnt];
        	columnNames = new String[colCnt];
        	String name = getNameOfFirst();
        	data[0][0] = "";
        	data[0][colCnt-1] = "Decision accuracy";
        	for (int i = 0; i < getNoOfDecs(); i++) {
//        		columnNames[i+ip] = "a";
        		data[i+ip][0] = data[0][i+jp] = decisionName(i);
        		data[i+ip][colCnt-1] = toPercents(getDecAccuracy(i));
        		for (int j = 0; j < getNoOfDecs(); j++) {
        			data[i+ip][j+jp] = howManyObjects(name, i, j);
//        			data[i+ip][j+jp] = (new Integer(howManyObjects(name, i, j)).toString());
//        			if (data[i+ip][j+jp] == "0") data[i+ip][j+jp] = "";
        		}
        	}
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        
//         * Don't need to implement this method unless your table's
//         * editable.
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
        	return false;
        }

//         * Don't need to implement this method unless your table's
//         * data can change.
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }

	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}
	
	private JPanel getQStatisticsPanel() {
		if (qStatisticsPanel == null) {
			claccLabel = new JLabel();
			claccLabel.setText(clacc + ": " + getAccuracy());
			qStatisticsPanel = new JPanel();
			qStatisticsPanel.setLayout(new BorderLayout());
		}
		qStatisticsPanel.setOpaque(true);
		qStatisticsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
		qStatisticsPanel.setPreferredSize(new Dimension(0, 50));
		qStatisticsPanel.add(claccLabel, BorderLayout.WEST);
		return qStatisticsPanel;
	}
	
	private JPanel getQConfMatrixPanel() {
		if (qConfMatrixPanel == null) {
			qConfMatrixPanel = new JPanel();
			qConfMatrixPanel.setLayout(new BorderLayout());
		}
		qConfMatrixPanel.setOpaque(true);
		qConfMatrixPanel.setBorder(BorderFactory.createTitledBorder(null, "Confusion matrix", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
		qConfMatrixPanel.add(getTable(), BorderLayout.NORTH);
		return qConfMatrixPanel;
	}
	
	
	/**
	 * This method initializes table	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getTable() {
		if (table == null) {
//			table = new JTable(getData(), getColumnNames());
			table = new JTable(new ConfMatrixTableModel());
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			table.setCellSelectionEnabled(false);
			table.setColumnSelectionAllowed(false);
			table.setEnabled(false);
			table.setComponentOrientation(ComponentOrientation.UNKNOWN);
			table.setBackground(new Color(204, 204, 255));
			
			// Zmiana szerokosci kolumn
			TableColumn column = null;
			int columnCount = table.getColumnModel().getColumnCount();
			for (int i = 0; i < columnCount; i++) {
			    column = table.getColumnModel().getColumn(i);
			    if (i == columnCount - 1) {
			        column.setPreferredWidth(120); //last column is bigger
			    } else {
			        column.setPreferredWidth(80);
			    }
			}
			

		}
		return table;
	}

	
    public JDialog createAndShowGUI(JFrame owner) {
        //Create and set up the window.
    	JDialog frame = new JDialog();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        frame.setTitle(qtr.getName());
        frame.setContentPane(this);

        //Display the window.
        frame.setLocationRelativeTo(owner);
        frame.pack();
        frame.setVisible(true);
        return frame;
    }


	
}

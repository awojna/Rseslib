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

import java.awt.*;
import javax.swing.*;

import java.awt.event.*;

import javax.swing.border.Border;

import rseslib.qmak.UI.QIcon;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.UI.QTableView;
import rseslib.qmak.UI.QTryb;
import rseslib.qmak.dataprocess.table.QDataTableClassified;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.QmakMain;
import rseslib.qmak.dataprocess.table.*;

import java.awt.Dimension;
import java.awt.BorderLayout;

/**
 * Okno, ktore zawiera klase tabelki (klasa QTableView). Taka
 * 'klasa otoczka' dla wlasciwej reprezentacji wizualnej tabelki
 * @author Krzysiek && Trickster's autors
 */
public class iQTables extends JDialog {

	// obiekty interfejsu
	private JPanel panel1 = new JPanel(); // @jve:decl-index=0:visual-constraint="10,54"

	private BorderLayout borderLayout1 = new BorderLayout();

	private JPanel buttonsPanel = new JPanel();

	private JButton buttonOk = new JButton();

	private FlowLayout flowLayout1 = new FlowLayout();

	private JButton buttonCancel = new JButton();

	Border border = BorderFactory.createCompoundBorder(BorderFactory
			.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder());

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	private QTableView table;

	private iQDataTable dataTable = null; // @jve:decl-index=0:

	private iQDataTable dataTableElem = null; // @jve:decl-index=0:

	private QIcon ikona; // po zamknieciu okna koniecznie ustawic pole
							// ikona.okno na null

	public iQDataTable getTable() {
		return dataTable;
	}

	public iQTables(Frame frame, boolean modal) {
		super(frame, modal);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public iQTables() {
		this(null, false);
		try {
			QmakMain.Log.error("don't use this constructor!");
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public iQTables(QIcon el) {
		this(null, false);
		try {
			ikona = el;
			dataTable = (iQDataTable) el.getElem();
			dataTableElem = (iQDataTable) ((iQDataTable) el.getElem()).clone();
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		panel1.setSize(new Dimension(486, 285));
		panel1.setLayout(borderLayout1);

		if (dataTable == null)
			return;
		table = new QTableView(dataTable,QTryb.tabelkOnly,this);
		panel1.add(table, BorderLayout.CENTER);
		table.invalidateProject();
		this.setSize(new Dimension(500, 600));

		buttonOk.setText(QmakMain.getMainFrame().messages.getString("jOK"));
		buttonOk.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonOk_actionPerformed(e);
			}
		});

		if (!dataTable.isClassified()) {
			buttonsPanel.setLayout(flowLayout1);
			buttonCancel.setText(QmakMain.getMainFrame().messages.getString("jCancel"));
			buttonCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					buttonCancel_actionPerformed(e);
				}
			});
		}

		this.setModal(false);
		this.setTitle(dataTable.getName());
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				this_componentResized(e);
			}
		});
		borderLayout1.setHgap(0);
		borderLayout1.setVgap(0);
		getContentPane().add(panel1);
		buttonsPanel.add(buttonOk, null);
		if (!dataTable.isClassified())
			buttonsPanel.add(buttonCancel, null);
		panel1.add(buttonsPanel, java.awt.BorderLayout.SOUTH);

	}

	public void invalidateProject() {
		table.invalidateProject();
	}

	void buttonOk_actionPerformed(ActionEvent e) {
		// QmakMain.getMainFrame().getProject().GetProjectElements().remove(OrigElem);
		// QmakMain.getMainFrame().getProject().GetProjectElements().add(Elem);
		QMainFrame.getMainFrame().jMainWindow.repaint();
		ikona.okno = null;
		dispose();
	}

	void buttonCancel_actionPerformed(ActionEvent e) {
		dataTable.restoreOldTable(dataTableElem);
		ikona.okno = null;
		dispose();
	}

	public void this_componentResized(ComponentEvent e) {
		int h;
		int w;
		boolean res = false;
		if (getSize().height < getMinimumSize().height) {
			h = getMinimumSize().height;
			res = true;
		} else
			h = getSize().height;
		if (getSize().width < getMinimumSize().width) {
			w = getMinimumSize().width;
			res = true;
		} else
			w = getSize().width;

		if (res)
			setSize(w, h);
	}

	public void sort(int kol) {
		if (dataTable.isClassified())
			((QDataTableClassified) dataTable).sort(kol);
		else
			dataTable.sort(kol);
		panel1.remove(table);
		table = new QTableView(dataTable,QTryb.tabelkOnly,this);
		panel1.add(table, BorderLayout.CENTER);

		table.invalidateProject();
		panel1.revalidate();
	}
}

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


package rseslib.qmak.UI.chart;

import javax.swing.JDialog;

//import ButtonHtmlDemo;

import javax.swing.WindowConstants;

import rseslib.qmak.UI.chart.GraphSetupPanel;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.table.*;

import java.awt.Dimension;

/**
 * Class implementing frame showing charts. 
 * 
 * @author Maciej Zuchniak
 *
 */

public class QChartFrame extends JDialog {

//	@jve:decl-index=0:visual-constraint="375,10"

	public QChartFrame(iQDataTable tab) {
        setSize(new Dimension(222, 105));
        setTitle(QmakMain.getMainFrame().messages.getString("QCFChartGenerator")+ " for " + tab.getName());
        //Create and set up the content pane.
        GraphSetupPanel newContentPane = new GraphSetupPanel(tab);
        newContentPane.setOpaque(true); //content panes must be opaque
        setContentPane(newContentPane);
        pack();
	}
	
	public void changeTableName(String newName) {
        setTitle(QmakMain.getMainFrame().messages.getString("QCFChartGenerator")+ " for " + newName);
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"

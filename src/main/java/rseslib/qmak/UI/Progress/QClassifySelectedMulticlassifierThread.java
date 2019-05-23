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


package rseslib.qmak.UI.Progress;

import java.util.Map;


import javax.swing.JOptionPane;

import rseslib.qmak.UI.QIcon;
import rseslib.processing.classification.TestResult;
import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.*;
import rseslib.qmak.UI.Progress.QVisualProgress;
import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.table.QDataTable;

/**
 * Klasa sluzy do obslugi progres baru.
 * @author Krzysztof Mroczek
 */
public class QClassifySelectedMulticlassifierThread extends Thread {
	public QMultiClassifier multiclassifier;
	public QDataTable tab;
	public QIcon table, multiclass;
	

	public QClassifySelectedMulticlassifierThread(QDataTable t,QMultiClassifier mult,QIcon multi, QIcon TableIc) {
		tab = t;
		multiclassifier = mult;
		table = TableIc;
		multiclass = multi;
	}
	
	public void run () {

		
		QVisualProgress progres = new QVisualProgress();
		progres.show();
		Map<String, TestResult> re;
		
		try {
			re = multiclassifier.classify(tab.getDataTable(), progres);
			if (!progres.Cancel)
				QmakMain.getMainFrame().jMainWindow.
					classifySelectedMulticlassifierEndThred(re,multiclass,table);
		} catch (InterruptedException e) {
			JOptionPane.showMessageDialog(QmakMain.getMainFrame().jMainWindow,
					"Error in classificaton", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}

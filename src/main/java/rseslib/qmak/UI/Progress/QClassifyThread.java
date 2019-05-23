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

import java.awt.Point;

import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.Progress.QVisualProgress;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.table.QDataTable;

/**
 * 
 * @author Krzysztof Mroczek
 *
 */
public class QClassifyThread extends Thread {
	public iQClassifier Classifier;
	public QDataTable tab;
	

	public QClassifyThread(iQClassifier k, QDataTable t) {
		Classifier = k;
		tab = t;
	}
	
	public void run () {
		QVisualProgress progres = new QVisualProgress();
		progres.show();
		Classifier.trainOnTableWithProgress(tab.getDataTable(), progres);
		if (Classifier != null && !progres.Cancel)
			QmakMain.getMainFrame().jMainWindow.wstawSklasyfikowanyElementDoProjektu(Classifier);
	}
}

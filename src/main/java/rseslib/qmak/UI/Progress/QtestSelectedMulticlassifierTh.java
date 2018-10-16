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


package rseslib.qmak.UI.Progress;

import java.util.Map;
import java.util.Properties;
import java.lang.InterruptedException;

import rseslib.processing.classification.MultipleTestResult;
import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.UI.Progress.QVisualProgress;
import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.results.QMultipleTestResult;
import rseslib.qmak.dataprocess.table.QDataTable;

/**
 * Klasa obslugujaca wizualny progres przy testach multiklasyfikatora.
 * 
 * @author Leszek Tur
 *
 */
public class QtestSelectedMulticlassifierTh extends Thread {
	public QMultiClassifier multiclassifier;
	public Properties prop;
	public QDataTable tab;
	Map<String, MultipleTestResult> re;
	String typ;
	QMultipleTestResult wyniki;
	

	public QtestSelectedMulticlassifierTh(Properties pr, QDataTable t,QMultiClassifier mult,String ty) {
		prop = pr;
		tab = t;
		multiclassifier = mult;
		typ = ty;
	}
	
	public void run () {	
		QVisualProgress progres = new QVisualProgress();
		progres.show();
		try {
			if (typ.equals("CrossValidationTest")){
				re = multiclassifier.doCrossValidationTest(prop, tab.getDataTable(), progres);
				wyniki = new QMultipleTestResult(re,"CrossValidationTest",QMainFrame.getMainFrame().getProject().CreateUniqeName("MultipleTestResult", false));			
			}
			if (typ.equals("MultipleCrossValidationTest")){
				re = multiclassifier.doMultipleCrossValidationTest(prop, tab.getDataTable(), progres);
				wyniki = new QMultipleTestResult(re,"CrossValidationTest",QMainFrame.getMainFrame().getProject().CreateUniqeName("MultipleTestResult", false));			
			}
			if (typ.equals("MultipleRandomSplitTest")){
				re = multiclassifier.doMultipleRandomSplitTest(prop, tab.getDataTable(), progres);
				wyniki = new QMultipleTestResult(re,"CrossValidationTest",QMainFrame.getMainFrame().getProject().CreateUniqeName("MultipleTestResult", false));			
			}	
		//	QMainFrame.getMainFrame().getProject().insertElement(wyniki);
			if (!progres.Cancel)
				QmakMain.getMainFrame().jMainWindow.testSelectedMulticlassifierEndThred(wyniki);
		} catch (InterruptedException e) {
			progres.HideOkno();
		} catch (Exception e) {
			e.printStackTrace();//TODO do debugowania
//			JOptionPane.showMessageDialog(QmakMain.getMainFrame().jMainWindow, "Error in test",
//					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
}

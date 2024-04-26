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

import java.awt.*;

import java.util.*;

import javax.swing.*;

import rseslib.qmak.UI.QClassifierOpener;
import rseslib.qmak.UI.QClassifierPropertiesDialog;
import rseslib.qmak.UI.QClassifierSaver;
import rseslib.qmak.UI.QIcon;
import rseslib.qmak.UI.QKlasyfikator;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.UI.QMultiClassifierOpener;
import rseslib.qmak.UI.QMultiClassifierSaver;
import rseslib.qmak.UI.QMulticlassifierShowDialog;
import rseslib.qmak.UI.QMultipleTestResultOpener;
import rseslib.qmak.UI.QMultipleTestResultSaver;
import rseslib.qmak.UI.QPropertiesDialog;
import rseslib.qmak.UI.QTestResultOpener;
import rseslib.qmak.UI.QTestResultPanel;
import rseslib.qmak.UI.QTestResultSaver;
import rseslib.qmak.UI.QVisClassifierView;
import rseslib.qmak.UI.Progress.QClassifySelectedMulticlassifierThread;
import rseslib.qmak.UI.Progress.QClassifyThread;
import rseslib.qmak.UI.Progress.QVisualProgress;
import rseslib.qmak.UI.Progress.QtestSelectedMulticlassifierTh;
import rseslib.qmak.UI.Progress.QtrainSelectedMulticlassifierThread;
import rseslib.qmak.UI.chart.QChartFrame;
import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.QClassifierType;
import rseslib.qmak.dataprocess.classifier.QClassifierTypes;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.multiclassifier.iQMultiClassifier;

import java.awt.event.*;
import java.io.File;
import java.lang.Math;

import rseslib.structure.data.DoubleData;
import rseslib.processing.classification.CrossValidationTest;
import rseslib.processing.classification.MultipleCrossValidationTest;
import rseslib.processing.classification.MultipleRandomSplitTest;
import rseslib.processing.classification.SingleClassifierTest;
import rseslib.processing.classification.TestResult;
import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.iQTables;
import rseslib.qmak.UI.Progress.*;
import rseslib.qmak.UI.chart.*;
import rseslib.qmak.dataprocess.classifier.*;
import rseslib.qmak.dataprocess.multiclassifier.*;
import rseslib.qmak.dataprocess.project.QPara;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.dataprocess.results.QMultipleTestResult;
import rseslib.qmak.dataprocess.results.QTestResult;
import rseslib.qmak.dataprocess.table.QDataTable;
import rseslib.qmak.dataprocess.table.QDataTableClassified;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.util.Utils;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

class QKlasyfikator extends JMenuItem {
	public QClassifierType Classifier;

	QKlasyfikator(QClassifierType typ) {
		Classifier = typ;
	}

	public QClassifierType getClassifierType() {
		return Classifier;
	}
}

/**
 * Glowna klasa odpowiedzialna za wyswietlanie ikonek w projekcie 
 * i oprogramowujaca akcje wykonywane na tych ikonkach. 
 * 
 * @author Krzysztof Mroczek
 * @author Leszek Tur
 * @author Damian Wojcik
 * @author Maciej Zuchniak
 * 
 */
public class QProjectView extends JPanel {
	Set<QIcon> elements = new HashSet<QIcon>();

	QMainFrame mainFrame = null;

	QClassifierSaver qsaver;
	
	QTestResultSaver trsaver;
	
	QTestResultOpener tropener;
	
	QMultipleTestResultSaver mtrsaver;
	
	QMultipleTestResultOpener mtropener;	
	
	QClassifierOpener qopener;
	
	QMultiClassifierOpener qMultiClassifierOpener;
	
	QMultiClassifierSaver qMultiClassifierSaver;

	QIcon SelectedTable = null;
	QIcon SelectedClassifier = null;
	QIcon SelectedMulticlassifier = null;
	QIcon SelectedMultipleTestResult = null;
	QIcon SelectedTestResult = null;

	JPopupMenu popup_QMainView = new JPopupMenu();
	JMenuItem MainViewNewTable = new JMenuItem();
	JMenuItem MainViewOpenTableWithSeparetedHeader = new JMenuItem();
	JMenuItem MainViewOpenTable = new JMenuItem();
	JMenuItem MainViewAddMultiCl = new JMenuItem();
	JMenuItem MainViewHelp = new JMenuItem();
	JMenuItem MainViewOpenClassifier = new JMenuItem();	
	JMenuItem MainViewOpenMultiClassifier = new JMenuItem();	
	JMenuItem MainViewOpenTestResult = new JMenuItem();
	JMenuItem MainViewOpenMultipleTestResult = new JMenuItem();	
	
	JPopupMenu popup_QMulticlassifierIcon = new JPopupMenu();
	JMenuItem MulticlassifierIconDeleteJMenuItem = new JMenuItem();
	JMenuItem MulticlassifierShowJMenuItem = new JMenuItem();
	JMenuItem MulticlassifierHelpJMenuItem = new JMenuItem();
	JMenuItem MulticlassifierTrainJMenuItem = new JMenuItem();
	JMenu MulticlassifierIconAddJMenu = new JMenu();
	JMenu MulticlassifierIconTestJMenu = new JMenu();
	JMenuItem MulticlassifierTest1JMenuItem = new JMenuItem();
	JMenuItem MulticlassifierTest2JMenuItem = new JMenuItem();
	JMenuItem MulticlassifierTest3JMenuItem = new JMenuItem();
	JMenuItem MulticlassifierClassifyTableJMenuItem = new JMenuItem();
	JMenuItem MulticlassifierIconRename = new JMenuItem();
	JMenuItem MulticlassifierIconSave = new JMenuItem();

	JPopupMenu popup_QClassifierIcon = new JPopupMenu();
	JMenuItem ClassifierIconTrainTable = new JMenuItem();
	JMenuItem ClassifierIconTest = new JMenuItem();
	JMenuItem ClassifierIconDelete = new JMenuItem();
	JMenuItem ClassifierIconChangeProperties = new JMenuItem();
	JMenuItem ClassifierIconRename = new JMenuItem();
	JMenuItem ClassifierIconSave = new JMenuItem();
	JMenuItem ClassifierHelpJMenuItem = new JMenuItem();

	JPopupMenu popup_QVisClassifierIcon = new JPopupMenu();
	JMenuItem VisClassifierIconTrainTable = new JMenuItem();
	JMenuItem VisClassifierIconVisualise = new JMenuItem();
	JMenuItem VisClassifierIconTest = new JMenuItem();	
	JMenuItem VisClassifierIconDelete = new JMenuItem();
	JMenuItem VisClassifierIconChangeProperties = new JMenuItem();
	JMenuItem VisClassifierIconRename = new JMenuItem();
	JMenuItem VisClassifierIconSave = new JMenuItem();
	JMenuItem VisClassifierHelpJMenuItem = new JMenuItem();
	
	JPopupMenu popup_QTableIcon = new JPopupMenu();
	JMenu TableIconClassifiersMenu = new JMenu();
	JMenu TableIconTrainedClassifiersMenu = new JMenu();
	JMenuItem TableIconShow = new JMenuItem();
	JMenuItem TableIconDelete = new JMenuItem();
	JMenuItem TableIconGenerateChartOption = new JMenuItem();
	JMenuItem TableIconCopy = new JMenuItem();
	JMenuItem TableIconRename = new JMenuItem();
	JMenuItem TableIconHelp = new JMenuItem();
	JMenuItem TableIconSplit = new JMenuItem();
	JMenuItem TableIconSave = new JMenuItem();
	

	JPopupMenu popup_QMultipleTestResultIcon = new JPopupMenu();
	JMenuItem MultipleTestResultIconDelete = new JMenuItem();
	JMenuItem MultipleTestResultIconShow = new JMenuItem();
	JMenuItem MultipleTestResultIconRename = new JMenuItem();
	JMenuItem MultipleTestResultHelpJMenuItem = new JMenuItem();
	JMenuItem MultipleTestResultSaveJMenuItem = new JMenuItem();	


	JPopupMenu popup_QTestResultIcon = new JPopupMenu();
	JMenuItem TestResultIconDelete = new JMenuItem();
	JMenuItem TestResultIconShow = new JMenuItem();
	JMenuItem TestResultIconShowStatistics = new JMenuItem();
	JMenuItem TestResultIconRename = new JMenuItem();
	JMenuItem TestResultHelpJMenuItem = new JMenuItem();
	JMenuItem TestResultSave = new JMenuItem();


	// INICJALIZACJA I BUDOWANIE INTERFEJSU
	// ======================================================

	QProjectView(QMainFrame mF) {
		this.setLayout(null);
		mainFrame = mF;
		try {
			jbInit();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setBorder(BorderFactory.createLineBorder(Color.black, 2));
		// MAIN POPUP
		popup_QMainView.add(MainViewOpenTable);
		MainViewOpenTable.setText(mainFrame.messages.getString("QPVopentable"));
		MainViewOpenTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iQProjectElement El;
				El = QMainFrame.getMainFrame()
						.jMenuFileAddRseslibTable_actionPerformed(
								new ActionEvent(this, 0, null));
				if (El != null)
					insertIcon(El, null);
			};
		});

		popup_QMainView.add(MainViewOpenTableWithSeparetedHeader);
		MainViewOpenTableWithSeparetedHeader.setText(mainFrame.messages.getString("QPVopenTableWithSeparetedHeader"));
		MainViewOpenTableWithSeparetedHeader.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iQProjectElement El;
				El = QMainFrame.getMainFrame()
				.jMenuFileAddRseslibTableAndHeader_actionPerformed(
						new ActionEvent(this, 0, null));
				if (El != null)
					insertIcon(El, null);
			};
		});
		
		popup_QMainView.add(MainViewOpenClassifier);
		MainViewOpenClassifier.setText(mainFrame.messages.getString("QPVopenclassifier"));
		MainViewOpenClassifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addFileClassifier();
			};
		});	
	
		popup_QMainView.add(MainViewOpenTestResult);
		MainViewOpenTestResult.setText(mainFrame.messages.getString("QPVopentestresult"));
		MainViewOpenTestResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTestResult();
			};
		});	
		
		popup_QMainView.add(MainViewOpenMultipleTestResult);
		MainViewOpenMultipleTestResult.setText(mainFrame.messages.getString("QPVopenmultipletestresult"));
		MainViewOpenMultipleTestResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addMultipleTestResult();
			};
		});			
		
		popup_QMainView.add(MainViewOpenMultiClassifier);
		MainViewOpenMultiClassifier.setText(mainFrame.messages.getString("QPVopenMulticlassifier"));
		MainViewOpenMultiClassifier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addFileMultiClassifier();
			};
		});	
		
		popup_QMainView.add(MainViewNewTable);
		MainViewNewTable.setText(mainFrame.messages.getString("QPVcreateTable"));
		MainViewNewTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iQProjectElement El;
				El = QMainFrame.getMainFrame().jMenuTableNew_actionPerformed(
						new ActionEvent(this, 0, null));
				if (El != null) {
					insertIcon(El, null);
				}
			};
		});		
		
		popup_QMainView.add(MainViewAddMultiCl);
		MainViewAddMultiCl.setText(mainFrame.messages.getString("QPVcreateMulticlassifier"));
		MainViewAddMultiCl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iQProjectElement El;
				try {
					El = new QMultiClassifier(new Point(rnd(50,200), rnd(50,200)),mainFrame.getProject().CreateUniqeName("Experiment", false));	
	        		insertIcon(El, null);
	        		mainFrame.getProject().insertElement(El);
				} catch (PropertyConfigurationException e1) {
					e1.printStackTrace();
					QmakMain.Log.error(mainFrame.messages.getString("ErrMulticlassifierNotMade"));
				}
			};
		});
		
		popup_QMainView.add(MainViewHelp);
		MainViewHelp.setText(mainFrame.messages.getString("jMenuHelp")); 
		MainViewHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.getHelp().pokaz("QProjectView_popup_menu");
			};
		});

		// TABLE
		popup_QTableIcon.add(TableIconShow);
		TableIconShow.setText(mainFrame.messages.getString("jMenuShow"));
		TableIconShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedTable();
			};
		});	

		popup_QTableIcon.add(TableIconGenerateChartOption);
		TableIconGenerateChartOption.setText(mainFrame.messages.getString("QPVGenerateChart"));
		TableIconGenerateChartOption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generateChart();
			};
		});
		
		popup_QTableIcon.add(TableIconClassifiersMenu);
		TableIconClassifiersMenu.setText(mainFrame.messages.getString("jMenuTrainClassifier"));
		
		popup_QTableIcon.add(TableIconSplit);
		TableIconSplit.setText(mainFrame.messages.getString("jMenuSplit"));
		TableIconSplit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				splitTableIcon();
			};
		});
		
		popup_QTableIcon.add(TableIconCopy);
		TableIconCopy.setText(mainFrame.messages.getString("jMenuCopyTable"));
		TableIconCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addCopyOfSelectedTable();
			};
		});
		
		popup_QTableIcon.add(TableIconRename);
		TableIconRename.setText(mainFrame.messages.getString("jMenuRename"));
		TableIconRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameTableIcon();
			};
		});
		
		popup_QTableIcon.add(TableIconSave);
		TableIconSave.setText(mainFrame.messages.getString("jMenuSave"));
		TableIconSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SaveTableIcon_actionPerformed();
			};
		});

		popup_QTableIcon.add(TableIconDelete);
		TableIconDelete.setText(mainFrame.messages.getString("jMenuDelete"));
		TableIconDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeObjectUnderIcon(SelectedTable);
			};
		});
		
		popup_QTableIcon.add(TableIconHelp);
		TableIconHelp.setText(mainFrame.messages.getString("jMenuHelp"));
		TableIconHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.getHelp().pokaz("QProjectView_TableIcon");
			};
		});

		// CLASSIFIER
		popup_QClassifierIcon.add(ClassifierIconChangeProperties);
		ClassifierIconChangeProperties.setText(mainFrame.messages.getString("jMenuProperties"));
		ClassifierIconChangeProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showClassifierProperties();
			};
		});

		popup_QClassifierIcon.add(ClassifierIconTrainTable);
		ClassifierIconTrainTable.setText(mainFrame.messages.getString("QPVClassifySelectedTable"));
		ClassifierIconTrainTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				classifySelectedTable();
			};
		});
		
		popup_QClassifierIcon.add(ClassifierIconTest);
		ClassifierIconTest.setText(mainFrame.messages.getString("QPVTestClassifier"));
		ClassifierIconTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SingleClassifierTest sct = new SingleClassifierTest();
				QClassifier qclassifier = ((QClassifier) SelectedClassifier.getElem());
				QDataTable  qtesttab    = ((QDataTable)  SelectedTable.getElem());
				try {
					QVisualProgress progres = new QVisualProgress();
					progres.show();
					TestResult tres = sct.classify(qclassifier.getClassifier(), 
					                                  qtesttab.getDataTable(), 
					                                  progres);
					iQProjectElement prelt = new QTestResult(
							tres, mainFrame.getProject().CreateUniqeName("TestResult", false), qclassifier.getName());
					mainFrame.getProject().insertElement(prelt);
					insertIcon(prelt, null);
					makeArrowFromTo(qclassifier, prelt);
					makeArrowFromTo(qtesttab, prelt);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			};
		});
		
		popup_QClassifierIcon.add(ClassifierIconRename);
		ClassifierIconRename.setText(mainFrame.messages.getString("jMenuRename"));
		ClassifierIconRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameClassifier();
			};
		});
				
		popup_QClassifierIcon.add(ClassifierIconSave);
		ClassifierIconSave.setText(mainFrame.messages.getString("QPVsaveClassifier"));
		ClassifierIconSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveClassifier();
			};
		});

		popup_QClassifierIcon.add(ClassifierIconDelete);
		ClassifierIconDelete.setText(mainFrame.messages.getString("jMenuDelete"));
		ClassifierIconDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeObjectUnderIcon(SelectedClassifier);
			};
		});
		
		popup_QClassifierIcon.add(ClassifierHelpJMenuItem);
		ClassifierHelpJMenuItem.setText(mainFrame.messages.getString("jMenuHelp"));
		ClassifierHelpJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.getHelp().pokaz("QProjectView_ClassifierIcon");
			};
		});
		

		// VisCLASSIFIER
		popup_QVisClassifierIcon.add(VisClassifierIconVisualise);
		VisClassifierIconVisualise.setText(mainFrame.messages.getString("QPVvisualize"));
		VisClassifierIconVisualise.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionPerformed_pokazWizualizacje(e);
			};
		});
		
		popup_QVisClassifierIcon.add(VisClassifierIconChangeProperties);
		VisClassifierIconChangeProperties.setText(mainFrame.messages.getString("jMenuProperties"));
		VisClassifierIconChangeProperties
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showClassifierProperties();
					};
				});

		popup_QVisClassifierIcon.add(VisClassifierIconTrainTable);
		VisClassifierIconTrainTable.setText(mainFrame.messages.getString("QPVClassifySelectedTable"));
		VisClassifierIconTrainTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				classifySelectedTable();
			};
		});

		popup_QVisClassifierIcon.add(VisClassifierIconTest);
		VisClassifierIconTest.setText(mainFrame.messages.getString("QPVTestClassifier"));
		VisClassifierIconTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SingleClassifierTest sct = new SingleClassifierTest();
				QClassifier qclassifier = ((QClassifier) SelectedClassifier.getElem());
				QDataTable  qtesttab    = ((QDataTable)  SelectedTable.getElem());
				try {
					QVisualProgress progres = new QVisualProgress();
					progres.show();
					TestResult tres = sct.classify(qclassifier.getClassifier(), 
					                                  qtesttab.getDataTable(), 
					                                  progres);
					iQProjectElement prelt = new QTestResult(
							tres, mainFrame.getProject().CreateUniqeName("TestResult", false), qclassifier.getName());
					mainFrame.getProject().insertElement(prelt);
					insertIcon(prelt, null);
					makeArrowFromTo(qclassifier, prelt);
					makeArrowFromTo(qtesttab, prelt);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			};
		});
		
		popup_QVisClassifierIcon.add(VisClassifierIconRename);
		VisClassifierIconRename.setText(mainFrame.messages.getString("jMenuRename"));
		VisClassifierIconRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameClassifier();
			};
		});
				
		popup_QVisClassifierIcon.add(VisClassifierIconSave);
		VisClassifierIconSave.setText(mainFrame.messages.getString("QPVsaveClassifier"));
		VisClassifierIconSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveClassifier();
			};
		});
		
		popup_QVisClassifierIcon.add(VisClassifierIconDelete);
		VisClassifierIconDelete.setText(mainFrame.messages.getString("jMenuDelete"));
		VisClassifierIconDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeObjectUnderIcon(SelectedClassifier);
			};
		});

		popup_QVisClassifierIcon.add(VisClassifierHelpJMenuItem);
		VisClassifierHelpJMenuItem.setText(mainFrame.messages.getString("jMenuHelp"));
		VisClassifierHelpJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.getHelp().pokaz("QProjectView_ClassifierIcon");
			};
		});
		
		// MULTICLASSIFIER
		popup_QMulticlassifierIcon.add(MulticlassifierShowJMenuItem);
		MulticlassifierShowJMenuItem.setText(mainFrame.messages.getString("jMenuShow"));
		MulticlassifierShowJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedMulticlassifier();
			};
		});
		
		popup_QMulticlassifierIcon.add(MulticlassifierTrainJMenuItem);
		MulticlassifierTrainJMenuItem.setText(mainFrame.messages.getString("jMenuTrainOnSelectedTable"));
		MulticlassifierTrainJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				trainSelectedMulticlassifier();
			};
		});

		popup_QMulticlassifierIcon.add(MulticlassifierClassifyTableJMenuItem);
		MulticlassifierClassifyTableJMenuItem
				.setText(mainFrame.messages.getString("jMenuClassifySelectedTable"));
		MulticlassifierClassifyTableJMenuItem
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						classifySelectedMulticlassifier();
					};
				});

		popup_QMulticlassifierIcon.add(MulticlassifierIconTestJMenu);
		MulticlassifierIconTestJMenu.setText(mainFrame.messages.getString("jMenuTestOnSelectedTable"));
		MulticlassifierIconTestJMenu.add(MulticlassifierTest1JMenuItem);
		MulticlassifierIconTestJMenu.add(MulticlassifierTest2JMenuItem);
		MulticlassifierIconTestJMenu.add(MulticlassifierTest3JMenuItem);
		MulticlassifierTest1JMenuItem.setText(mainFrame.messages.getString("QPVDoCrossValidationTest"));
		MulticlassifierTest2JMenuItem.setText(mainFrame.messages.getString("QPVDoMultipleCrossValidationTest"));
		MulticlassifierTest3JMenuItem.setText(mainFrame.messages.getString("QPVDoMultipleRandomSplitTest"));
		MulticlassifierTest1JMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testSelectedMulticlassifier("CrossValidationTest");
			};
		});
		MulticlassifierTest2JMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testSelectedMulticlassifier("MultipleCrossValidationTest");
			};
		});
		MulticlassifierTest3JMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testSelectedMulticlassifier("MultipleRandomSplitTest");
			};
		});

		popup_QMulticlassifierIcon.add(MulticlassifierIconRename);
		MulticlassifierIconRename.setText(mainFrame.messages.getString("jMenuRename"));
		MulticlassifierIconRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameMultiClassifier();
			};
		});
				
		popup_QMulticlassifierIcon.add(MulticlassifierIconSave);
		MulticlassifierIconSave.setText(mainFrame.messages.getString("QPVsaveMulticlassifier"));
		MulticlassifierIconSave.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveMultiClassifier();
					};
				});	

		popup_QMulticlassifierIcon.add(MulticlassifierIconDeleteJMenuItem);
		MulticlassifierIconDeleteJMenuItem.setText(mainFrame.messages.getString("jMenuDelete"));
		MulticlassifierIconDeleteJMenuItem
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						removeObjectUnderIcon(SelectedMulticlassifier);
					};
				});

		popup_QMulticlassifierIcon.add(MulticlassifierHelpJMenuItem);
		MulticlassifierHelpJMenuItem.setText(mainFrame.messages.getString("jMenuHelp"));
		MulticlassifierHelpJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.getHelp().pokaz("QProjectView_MultiClassifierIcon");
			};
		});
		
			

		// MULTIPLE TEST RESULT
		popup_QMultipleTestResultIcon.add(MultipleTestResultIconShow);
		popup_QMultipleTestResultIcon.add(MultipleTestResultIconRename);
		popup_QMultipleTestResultIcon.add(MultipleTestResultSaveJMenuItem);		
		popup_QMultipleTestResultIcon.add(MultipleTestResultIconDelete);
		popup_QMultipleTestResultIcon.add(MultipleTestResultHelpJMenuItem);
		MultipleTestResultIconDelete.setText(mainFrame.messages.getString("jMenuDelete"));
		MultipleTestResultIconShow.setText(mainFrame.messages.getString("jMenuShow"));
		MultipleTestResultIconRename.setText(mainFrame.messages.getString("jMenuRename"));
		MultipleTestResultHelpJMenuItem.setText(mainFrame.messages.getString("jMenuHelp"));
		MultipleTestResultSaveJMenuItem.setText(mainFrame.messages.getString("jMenuMultipleTestResultSave"));
		MultipleTestResultIconDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeObjectUnderIcon(SelectedMultipleTestResult);
			};
		});
		MultipleTestResultIconShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedMultipleTestResult();
//				showClassifierTestResult();
			};
		});
		MultipleTestResultHelpJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.getHelp().pokaz("QProjectView_MultipleTestResultIcon");
			};
		});
		
		MultipleTestResultIconRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameMultipleTestResult();
			};
		});
				
		MultipleTestResultSaveJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveMultipleTestResult();
			};
		});
		// TEST RESULT
		popup_QTestResultIcon.add(TestResultIconShow);
		popup_QTestResultIcon.add(TestResultIconShowStatistics);
		popup_QTestResultIcon.add(TestResultIconRename);
		popup_QTestResultIcon.add(TestResultSave);		
		popup_QTestResultIcon.add(TestResultIconDelete);
		popup_QTestResultIcon.add(TestResultHelpJMenuItem);
		TestResultIconDelete.setText(mainFrame.messages.getString("jMenuDelete"));
		TestResultIconShow.setText(mainFrame.messages.getString("jMenuShow"));
		TestResultIconShowStatistics.setText(mainFrame.messages.getString("jMenuShowStatistics"));
		TestResultIconRename.setText(mainFrame.messages.getString("jMenuRename"));
		TestResultHelpJMenuItem.setText(mainFrame.messages.getString("jMenuHelp"));
		TestResultSave.setText(mainFrame.messages.getString("jMenuTestResultSave"));
		TestResultIconDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeObjectUnderIcon(SelectedTestResult);
			};
		});
		
		
		
		
		TestResultIconShow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedClassifierTestResult();
			};
		});
		TestResultIconShowStatistics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSelectedTestResultStatistics();
			};
		});
		TestResultIconRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameTestResult();
			};
		});
		TestResultHelpJMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mainFrame.getHelp().pokaz("QProjectView_TestResultIcon");
			};
		});
		TestResultSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveTestResult();
			};
		});		

	}

	// =====================================================================================================
	// METODY WYKONUJACE ROZNE AKCJE NA OBIEKTACH PROJEKTU (NP. KLASYFIKATORACH)
	// - ZWIAZANE Z OPCJAMI MENU
	// =====================================================================================================

	// MULTICLASSIFIER
	private void testSelectedMulticlassifier(String typ){
		if (SelectedTable != null && SelectedMulticlassifier != null) {
			QDataTable tab = (QDataTable) SelectedTable.getElem();
			QMultiClassifier mcl = (QMultiClassifier) SelectedMulticlassifier.getElem();
			
			try {
				Properties prop = null;
				if (typ.equals("CrossValidationTest")){
					prop = Configuration.loadDefaultProperties(CrossValidationTest.class);
				}
				if (typ.equals("MultipleCrossValidationTest")){
					prop = Configuration.loadDefaultProperties(MultipleCrossValidationTest.class);
				}
				if (typ.equals("MultipleRandomSplitTest")){
					prop = Configuration.loadDefaultProperties(MultipleRandomSplitTest.class);
				}								
				
				QPropertiesDialog qpd = new QPropertiesDialog(QMainFrame.getMainFrame(),true,prop);
				qpd.setLocationRelativeTo(QMainFrame.getMainFrame());
				qpd.pack();
				qpd.setModal(true);
				qpd.setVisible(true);
				
				if (!qpd.czyOK()){
					qpd.dispose();
					return;
				}
				qpd.dispose();
				
				
				if (mcl.size()>0) {
				
				if (typ.equals("CrossValidationTest")){
					QtestSelectedMulticlassifierTh thr =
					new QtestSelectedMulticlassifierTh(qpd.getProperties(),tab,mcl,"CrossValidationTest");
					thr.start();
				}
				if (typ.equals("MultipleCrossValidationTest")){
					QtestSelectedMulticlassifierTh thr =
					new QtestSelectedMulticlassifierTh(qpd.getProperties(),tab,mcl,"MultipleCrossValidationTest");
					thr.start();
				}
				if (typ.equals("MultipleRandomSplitTest")){
					QtestSelectedMulticlassifierTh thr =
					new QtestSelectedMulticlassifierTh(qpd.getProperties(),tab,mcl,"MultipleRandomSplitTest");
					thr.start();
				}
				} else {
					JOptionPane.showMessageDialog(mainFrame, mainFrame.messages.getString("WarningMulticlassifierIsEmpty"),mainFrame.messages.getString("Warning"), JOptionPane.WARNING_MESSAGE);
				}

			} catch (Exception e) {
				e.printStackTrace();//TODO narazie niech to zostanie do debugowania
//				JOptionPane.showMessageDialog(mainFrame, "Error in test",
//						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(mainFrame,mainFrame.messages.getString("WarningNoTableSelected"),mainFrame.messages.getString("Warning"), JOptionPane.WARNING_MESSAGE);
		}		
	}

	public void testSelectedMulticlassifierEndThred(QMultipleTestResult wyniki) {
		mainFrame.getProject().insertElement(wyniki);
		insertIcon(wyniki, null);
		makeArrowFromTo(SelectedMulticlassifier.getElem(), wyniki);
		makeArrowFromTo(SelectedTable.getElem(), wyniki);
	}

	
	private void trainSelectedMulticlassifier() {
		if (SelectedTable != null) {
			QDataTable tab = (QDataTable) SelectedTable.getElem();
			QMultiClassifier mcl = (QMultiClassifier) SelectedMulticlassifier.getElem();
			
			if (mcl.size()==0){//klasyfikator jest pusty
				JOptionPane.showMessageDialog(mainFrame, mainFrame.messages.getString("WarningMulticlassifierIsEmpty"),mainFrame.messages.getString("Warning"), JOptionPane.WARNING_MESSAGE);
			} else {
				QtrainSelectedMulticlassifierThread thr =
				new QtrainSelectedMulticlassifierThread(tab,mcl,SelectedMulticlassifier,SelectedTable);
				thr.start();
			}
		} else {
			JOptionPane.showMessageDialog(mainFrame,mainFrame.messages.getString("WarningNoTableSelected"),mainFrame.messages.getString("Warning"), JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public void trainSelectedMulticlassifierEndThred(QIcon mult, QIcon tab) {
		deleteArrowsTo(mult);
		makeArrowFromTo(tab.getElem(),mult.getElem());
	}

	private void classifySelectedMulticlassifier() {
		if (SelectedTable != null) {
			QDataTable tab = (QDataTable) SelectedTable.getElem();
			QMultiClassifier mcl = (QMultiClassifier) SelectedMulticlassifier.getElem();
			QClassifySelectedMulticlassifierThread thr = new QClassifySelectedMulticlassifierThread(tab,mcl,SelectedMulticlassifier,SelectedTable);
			thr.start();
		} else {
			JOptionPane.showMessageDialog(mainFrame, mainFrame.messages.getString("WarningNoTableSelected"),mainFrame.messages.getString("Warning"), JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public void classifySelectedMulticlassifierEndThred(Map<String, TestResult> re, QIcon mult, QIcon tab) {
		QTestResult wyniki = new QTestResult(re,mainFrame.getProject().CreateUniqeName("TestResult", false));
		mainFrame.getProject().insertElement(wyniki);
		insertIcon(wyniki, null);
		makeArrowFromTo(mult.getElem(), wyniki);
		makeArrowFromTo(tab.getElem(), wyniki);
	}

	private void showSelectedMulticlassifier() {
		if(SelectedMulticlassifier.okno == null) {
			SelectedMulticlassifier.okno = new QMulticlassifierShowDialog(
				QMainFrame.getMainFrame(),
				(QMultiClassifier) SelectedMulticlassifier.getElem());
			SelectedMulticlassifier.okno.setLocationRelativeTo(QMainFrame.getMainFrame());
		}
		SelectedMulticlassifier.okno.setVisible(true);
	}

public void actionPerformed_QmouseClickedMulticlassifier(java.awt.event.MouseEvent e) {
			QIcon obiekt = (QIcon) e.getSource();
			QKlasyfikator pozycja;
			
			popup_QMulticlassifierIcon.add(MulticlassifierIconAddJMenu);
			MulticlassifierIconAddJMenu.removeAll();
			MulticlassifierIconAddJMenu.setText(mainFrame.messages.getString("jMenuAdd"));
			if (((QMultiClassifier) obiekt.getElem()).areTrained()){
//				MulticlassifierIconTestJMenu.setEnabled(true);
				MulticlassifierClassifyTableJMenuItem.setEnabled(true);
			} else {
//				MulticlassifierIconTestJMenu.setEnabled(false);
				MulticlassifierClassifyTableJMenuItem.setEnabled(false);
			}
			
			QClassifierTypes typy = QMainFrame.getClassifierTypes();
			for (QClassifierType typ : typy.getTypes()) {
				pozycja = new QKlasyfikator(typ);
				pozycja.setText(typ.getClassName());
				pozycja.addActionListener(new ActionListener() {
			        public void actionPerformed(ActionEvent e) {
			        	QMultiClassifier mcl = (QMultiClassifier) SelectedMulticlassifier.getElem();
			        	QClassifierType typ = ((QKlasyfikator) e.getSource()).getClassifierType();
			        	String newName = typ.getClassName()+"_"+(mcl.size()+1);
			        	newName = mainFrame.getProject().CreateUniqeName(newName, false);
			        	QClassifier qCl = new QClassifier(typ,newName);
						QClassifierPropertiesDialog dlg = new QClassifierPropertiesDialog(QMainFrame.getMainFrame(), true);
						dlg.assignData(qCl);
						dlg.setLocationRelativeTo(QMainFrame.getMainFrame());
						dlg.pack();
						dlg.setModal(true);//wazna linijka - bez tego program dzialal dalej i trenowal klasyfikator
						dlg.setVisible(true);
						if (dlg.czyOK()){
							mcl.add(qCl);
				        	mainFrame.getProject().registerName(newName);
				        	if(SelectedMulticlassifier.okno != null)
				        		((QMulticlassifierShowDialog)SelectedMulticlassifier.okno).addItem(newName);
						}
						dlg.dispose();
			        };
			    });
				MulticlassifierIconAddJMenu.add(pozycja);
			}
			popup_QMulticlassifierIcon.show(this,e.getX() + obiekt.getX(),e.getY() + obiekt.getY());		
		}

	private void addFileMultiClassifier() {
		//dodanie do projektu klasyfikatora 
		if (qMultiClassifierOpener == null) {
			qMultiClassifierOpener = new QMultiClassifierOpener(mainFrame);
		}
		iQProjectElement loadedMultiClassifier = qMultiClassifierOpener.load();
		if (loadedMultiClassifier != null) {
			mainFrame.getProject().insertElement(loadedMultiClassifier);
			insertIcon(loadedMultiClassifier, null);
		}
	}
	
	private void renameMultiClassifier() {
		String newName = JOptionPane.showInputDialog(this,
				mainFrame.messages.getString("InfoInsertName") + " " + SelectedMulticlassifier.getElem().getName() + " to",
				"", JOptionPane.PLAIN_MESSAGE);
		if(newName != null) {
			mainFrame.setTitleUnsaved();
			SelectedMulticlassifier.getElem().setName(QmakMain.getMainFrame()
					.getProject().CreateUniqeName(newName, false));
			SelectedMulticlassifier.setText(SelectedMulticlassifier.getElem().getName());
			if(SelectedMulticlassifier.okno != null)
				SelectedMulticlassifier.okno.setTitle(SelectedMulticlassifier.getElem().getName());
			Dimension size = SelectedMulticlassifier.getPreferredSize();
			SelectedMulticlassifier.setBounds(SelectedMulticlassifier.getX(), SelectedMulticlassifier.getY(),
					size.width, size.height);
		}
	}

	private void saveMultiClassifier() {
		iQMultiClassifier activeMultiClassifier = (iQMultiClassifier) SelectedMulticlassifier
				.getElem();

		if (qMultiClassifierSaver == null) {
			qMultiClassifierSaver = new QMultiClassifierSaver(activeMultiClassifier, mainFrame);
		} else {
			qMultiClassifierSaver.setMultiClassifier(activeMultiClassifier);
		}
		qMultiClassifierSaver.store();
		// chce zapisac activeClassifier do pliku, jego properties do pliku oraz
		// chce miec takze system rozszerzen nazw plikow
		// chce moc wczytac klasyfikator
	}

	// CLASSIFIER
	private void showClassifierProperties() {
		iQClassifier activeClassifier = (iQClassifier) SelectedClassifier
				.getElem();
		QClassifierPropertiesDialog dlg = new QClassifierPropertiesDialog(
				QMainFrame.getMainFrame(), true);
		dlg.assignData(activeClassifier);
		dlg.setLocationRelativeTo(QMainFrame.getMainFrame());
		dlg.pack();
		dlg.setModal(true);
		dlg.setVisible(true);
	};

	private void renameClassifier() {
		String newName = JOptionPane.showInputDialog(this,
				mainFrame.messages.getString("InfoInsertName") + " " + SelectedClassifier.getElem().getName() + " to",
				"", JOptionPane.PLAIN_MESSAGE);
		if(newName != null) {
			mainFrame.setTitleUnsaved();
			SelectedClassifier.getElem().setName(QmakMain.getMainFrame()
					.getProject().CreateUniqeName(newName, false));
			SelectedClassifier.setText(SelectedClassifier.getElem().getName());
			if(SelectedClassifier.okno != null)
				SelectedClassifier.okno.setTitle(SelectedClassifier.getElem().getName());
			if(SelectedClassifier.oknoKlas != null)
				SelectedClassifier.oknoKlas.changeName(SelectedClassifier.getElem().getName());
			Dimension size = SelectedClassifier.getPreferredSize();
			SelectedClassifier.setBounds(SelectedClassifier.getX(), SelectedClassifier.getY(),
					size.width, size.height);
		}
	}

	private void saveClassifier() {
		iQClassifier activeClassifier = (iQClassifier) SelectedClassifier
				.getElem();
		
		if(activeClassifier.isFileStoreable()) {
			if (qsaver == null) {
				qsaver = new QClassifierSaver(activeClassifier, mainFrame);
			} else {
				qsaver.setClassifier(activeClassifier);
			}
			qsaver.store();
			// chce zapisac activeClassifier do pliku, jego properties do pliku oraz
			// chce miec takze system rozszerzen nazw plikow
			// chce moc wczytac klasyfikator
		} else {
			JOptionPane.showMessageDialog(QMainFrame.getMainFrame(), activeClassifier.getName() + " does not support saving, can not be saved", "", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void saveTestResult() {
		QTestResult activeTestResult = (QTestResult) SelectedTestResult.getElem();
		
		if (trsaver == null) {
			trsaver = new QTestResultSaver(activeTestResult, mainFrame);
		} else {
			trsaver.setTestResult(activeTestResult);
		}
		trsaver.store();
	}
	
	private void addFileClassifier() {
		//dodanie do projektu klasyfikatora 
		if (qopener == null) {
			qopener = new QClassifierOpener(mainFrame);
		}
		iQProjectElement loadedClassifier = qopener.load();
		if (loadedClassifier != null) {
			mainFrame.getProject().insertElement(loadedClassifier);
			insertIcon(loadedClassifier, null);
		}
	}

	private void addTestResult() {
		//dodanie do projektu test resulta
		if (tropener == null) {
			tropener = new QTestResultOpener(mainFrame);
		}
		iQProjectElement loadedTestResult = tropener.load();
		if (loadedTestResult != null) {
			mainFrame.getProject().insertElement(loadedTestResult);
			insertIcon(loadedTestResult, null);
		}
	}
	
	private void addMultipleTestResult() {
		//dodanie do projektu test resulta
		if (mtropener == null) {
			mtropener = new QMultipleTestResultOpener(mainFrame);
		}
		iQProjectElement loadedMTestResult = mtropener.load();
		if (loadedMTestResult != null) {
			mainFrame.getProject().insertElement(loadedMTestResult);
			insertIcon(loadedMTestResult, null);
		}
	}
		
	public void actionPerformed_QmouseClickedClassifier(
			java.awt.event.MouseEvent e) {
		QIcon obiekt = (QIcon) e.getSource();

		if (SelectedClassifier != null) {
			if (((QClassifier) SelectedClassifier.getElem()).getClassifier() instanceof rseslib.processing.classification.VisualClassifier) {
				popup_QVisClassifierIcon.show(this, e.getX() + obiekt.getX(), e
						.getY()
						+ obiekt.getY());
			} else {
				popup_QClassifierIcon.show(this, e.getX() + obiekt.getX(), e
						.getY()
						+ obiekt.getY());
			}
		}

	}
	
	//kopia actionPerformed_pokazWizualizacje
	private void pokazWizualizacje() {
		iQClassifier activeClassifier = (iQClassifier) SelectedClassifier
				.getElem();
		if (SelectedClassifier.okno == null) {
			SelectedClassifier.okno = new QVisClassifierView(activeClassifier);
			SelectedClassifier.okno.setLocationRelativeTo(QMainFrame.getMainFrame());
			((QVisClassifierView)SelectedClassifier.okno).draw();
			SelectedClassifier.okno.pack();
		}
		SelectedClassifier.okno.setVisible(true);
	}

	private void actionPerformed_pokazWizualizacje(ActionEvent e) {
		iQClassifier activeClassifier = (iQClassifier) SelectedClassifier
				.getElem();
		if (SelectedClassifier.okno == null)
			SelectedClassifier.okno = new QVisClassifierView(activeClassifier);
		SelectedClassifier.okno.setLocationRelativeTo(QMainFrame.getMainFrame());
		((QVisClassifierView)SelectedClassifier.okno).draw();
		SelectedClassifier.okno.pack();
		SelectedClassifier.okno.setVisible(true);
	}

	private void actionPerformed_ClassifyTableWithChoosedClassifier(
			iQProjectElement classifier) {
		QDataTableClassified table = null;
		Iterator i;
		QIcon icon = null;
		boolean stop = false;
		try {
			if (classifier.isMulticlassifier()) {
				i = this.elements.iterator();
				while (i.hasNext() && !stop) {
					icon = (QIcon)i.next(); 
					if (icon.getElem() == classifier) {
						stop = true;
					}
				}
				if (!stop)
					JOptionPane.showMessageDialog(this, mainFrame.messages.getString("QPVclassifierNotFound"));
				else {
					SelectedMulticlassifier = icon;
					classifySelectedMulticlassifier();
				}
			} else {
				table = (QDataTableClassified) ((QDataTable) SelectedTable
						.getElem()).copy().classify((iQClassifier) classifier);
				table.setName(QmakMain.getMainFrame().getProject().CreateUniqeName(
						String.format("%s(%s)", classifier.getName(), table.getName()),
						true));
				table.setFileName("Brak pliku");
				QmakMain.getMainFrame().getProject().GetProjectElements().remove(table);
				QmakMain.getMainFrame().getProject().insertElement(table);
				QPara p = new QPara(SelectedTable.getElem(), table);
				QMainFrame.getMainFrame().getProject().getRelatives().add(p);
				p = new QPara(classifier, table);
				QMainFrame.getMainFrame().getProject().getRelatives().add(p);

				QmakMain.getMainFrame().invalidateProject();
				insertIcon(table, null);
			}
		} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
			JOptionPane.showMessageDialog(this,
					"Tego klasyfikatora nie da sie zastosowac do tej tabeli");
			return;
		}

	}
	
	private void actionPerformed_AddClassifierTrainedOnTable(ActionEvent e) {
		if (SelectedTable == null) {
			JOptionPane.showMessageDialog(this, mainFrame.messages.getString("ErrNoSelectedTable"));
			return;
		}

		iQClassifier Classifier = new QClassifier(
				((QKlasyfikator) e.getSource()).Classifier, 
					QMainFrame.getMainFrame().getProject().CreateUniqeName(
							((QKlasyfikator) e.getSource()).Classifier.getClassName(), false));
		QDataTable tab = (QDataTable) SelectedTable.getElem();
		
		QClassifierPropertiesDialog dlg = new QClassifierPropertiesDialog(
				QMainFrame.getMainFrame(), true);
		dlg.assignData(Classifier);
		dlg.setLocationRelativeTo(QMainFrame.getMainFrame());
		dlg.pack();
		dlg.setModal(true);// wazna linijka - bez tego program dzialal dalej i
							// trenowal klasyfikator
		dlg.setVisible(true);

		if (dlg.czyOK()){
			QClassifyThread CT = new QClassifyThread(Classifier,tab);
			CT.start();
		}
		dlg.dispose();
	}
	
	/**
	 * metoda dedykowana dla QClassifyThread, aby po zakonczeniu dzialania mogla wstawic do projektu swoj klasyfikator
	 */
	public void wstawSklasyfikowanyElementDoProjektu(iQClassifier Classifier) {
		QMainFrame.getMainFrame().getProject().insertElement(Classifier);
		QMainFrame.getMainFrame().invalidateProject();

		if (Classifier != null) {
			insertIcon(Classifier, null);
			QPara p = new QPara(SelectedTable.getElem(), Classifier);
			QMainFrame.getMainFrame().getProject().getRelatives().add(p);
		}
	}

	// TABLE
	public void showSelectedTable(){
		if (SelectedTable.okno == null) {
			SelectedTable.okno = new iQTables(SelectedTable);
			SelectedTable.okno.setLocationRelativeTo(QMainFrame.getMainFrame());
		}
		SelectedTable.okno.setVisible(true);
	}
	
	private void classifySelectedTable() {
		if (SelectedTable == null) {
			JOptionPane.showMessageDialog(
					QMainFrame.getMainFrame().jMainWindow,
					mainFrame.messages.getString("ErrNoSelectedTable"));
			return;
		}
		if (SelectedClassifier == null) {
			JOptionPane.showMessageDialog(
					QMainFrame.getMainFrame().jMainWindow,
					mainFrame.messages.getString("ErrNoSelectedClassifier"));
			return;
		}
		iQClassifier Classifier = (iQClassifier) SelectedClassifier.getElem();
		QDataTableClassified table = null;
		try {
			table = (QDataTableClassified) ((QDataTable) SelectedTable
					.getElem()).copy().classify(Classifier);
		} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
			JOptionPane.showMessageDialog(this,
					mainFrame.messages.getString("ErrClassifierNotCompatibleToTable"));
			return;
		}

		table.setName(QmakMain.getMainFrame().getProject().CreateUniqeName(
				String.format("%s(%s)", Classifier.getName(), table.getName()),
				true));
		table.setFileName(mainFrame.messages.getString("ErrNoFile"));
		QmakMain.getMainFrame().getProject().GetProjectElements().remove(table);
		QmakMain.getMainFrame().getProject().insertElement(table);
		QPara p = new QPara(SelectedTable.getElem(), table);
		QMainFrame.getMainFrame().getProject().getRelatives().add(p);
		p = new QPara(SelectedClassifier.getElem(), table);
		QMainFrame.getMainFrame().getProject().getRelatives().add(p);

		QmakMain.getMainFrame().invalidateProject();
		insertIcon(table, null);
	}
	
	private boolean lounchFileChooser(JFileChooser chos) {
		int rval;
		rval = chos.showSaveDialog(this);
		if (rval == JFileChooser.APPROVE_OPTION) {
			return true;
		}
		return false;
	}

	private String lounchFileChooserForDataTable(String title) {
		JFileChooser chos = new JFileChooser(".");
		chos.setDialogTitle(title);
		chos.setFileFilter(Utils.getFileFilterQDT());
		String name = null;
		if (lounchFileChooser(chos)) {
			File file = chos.getSelectedFile();
			name = chos.getSelectedFile().getAbsolutePath();
			if (Utils.getExtension(file).equals("")) {
				name = name + "." + Utils.qdt;
			}
		}
		return name;
	}

	/**
	 * @autor Krzysztof Mroczek
	 * Zapis tabelki
	 */
	private void SaveTableIcon_actionPerformed() {
		iQDataTable Oryg = (iQDataTable) SelectedTable.getElem();
		if(Oryg.isFileStoreable()) {
			mainFrame.setTitleUnsaved();
			try {

				String HeaderName = lounchFileChooserForDataTable(
						QMainFrame.qmainframe.messages.getString("InfoSelectFileWithHeader"));
				if (HeaderName != null) {
					Oryg.setFileName(HeaderName);
					Oryg.save();
				}
			}
			catch (Exception exc) {
			}
		} else {
			JOptionPane.showMessageDialog(QMainFrame.getMainFrame(), "Tables with classification column do not support saving.\n" + Oryg.getName() + " can not be saved.", "", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @autor Krzysztof Mroczek
	 * Dzieli tabelke na dwie - napisane bardzo brzydko, za to odporne na zmiany w reprezentacji tabelek
	 */
	private void splitTableIcon() {
		mainFrame.setTitleUnsaved();
		Collection<DoubleData>[] tabele;
		QDataTable Oryg = (QDataTable) SelectedTable.getElem();
		try {
			tabele = Oryg.randomPartition(2);
			QDataTable tab;
			Iterator it;
			iQDataTable cp;
			for (int i = 0; i<2; i++) {
				/*
				PASKUDNE!!! Ale to jest jedyna metoda, by uniknac jakies problemy, przy
				ewentualnych dalszych zmianach tabelki - skopiowac cala tabelke (razem 
				z danymi, polami dodatkowymi), usunac wszystkie dane i dodac te dane,
				ktore sa potrzebne
				*/
				
				//Nie zamieniac tego ((QDataTable) SelectedTable.getElem()).clone() na Oryg
				tab = (QDataTable) ((QDataTable) SelectedTable.getElem()).clone();
				it =  ((QDataTable) SelectedTable.getElem()).iterator();
				while (it.hasNext()) tab.remove((DoubleData) it.next());
	
				it = tabele[i].iterator();
				while (it.hasNext()) tab.add((DoubleData) it.next());
					
				cp = tab;
				QmakMain.getMainFrame().getProject().GetProjectElements().add(cp);
				cp.setName(QmakMain.getMainFrame().getProject().CreateUniqeName(
						String.format("%s_part%d", Oryg.getName(), i+1),false));
				insertIcon(cp, null);
				makeArrowFromTo(Oryg, cp);
			}
			mainFrame.invalidateProject();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, mainFrame.messages.getString("ErrInSplit"));
		}
		
	}

	private void renameTableIcon() {
		String newName = JOptionPane.showInputDialog(this,
				mainFrame.messages.getString("InfoInsertName") + " " + SelectedTable.getElem().getName() + " to",
				"", JOptionPane.PLAIN_MESSAGE);
		if(newName != null) {
			mainFrame.setTitleUnsaved();
			SelectedTable.getElem().setName(QmakMain.getMainFrame()
					.getProject().CreateUniqeName(newName, false));
			SelectedTable.setText(((QDataTable) SelectedTable.getElem()).getName());
			if(SelectedTable.okno != null)
				SelectedTable.okno.setTitle(SelectedTable.getElem().getName());
			if(SelectedTable.chart_gen != null)
				SelectedTable.chart_gen.changeTableName(SelectedTable.getElem().getName());
			Dimension size = SelectedTable.getPreferredSize();
			SelectedTable.setBounds(SelectedTable.getX(), SelectedTable.getY(),
					size.width, size.height);
		}
	}

	private void addCopyOfSelectedTable() {
		if (SelectedTable == null)
			return;
		iQDataTable cp = ((QDataTable) SelectedTable.getElem()).copy();
		QmakMain.getMainFrame().getProject().GetProjectElements().add(cp);
		QPara p = new QPara(SelectedTable.getElem(), cp);
		QMainFrame.getMainFrame().getProject().getRelatives().add(p);
		mainFrame.invalidateProject();
		cp.setName(QmakMain.getMainFrame().getProject().CreateUniqeName(
			String.format("%s_copy", ((QDataTable) SelectedTable.getElem()).getName()),false));
		insertIcon(cp, null);
	}

	/*
	 * @autor: Maciej Zuchniak
	 */
	private void generateChart() {
		if (SelectedTable == null)
			return;
		if(SelectedTable.chart_gen == null) {
			iQDataTable tab = (QDataTable) (SelectedTable.getElem());
			SelectedTable.chart_gen = new QChartFrame(tab);
		}
		SelectedTable.chart_gen.setLocationRelativeTo(QMainFrame.getMainFrame());
        //Display the window.
		SelectedTable.chart_gen.setVisible(true);
	}

	private void showSelectedClassifierTestResult() {
		QTestResult qtResult = (QTestResult) SelectedTestResult.getElem(); 
		if (qtResult.isFromMultiClassifier()) {
			qtResult.showResults();
//			QTRFrame qtrframe = new QTRFrame();
		} else {
			if(SelectedTestResult.okno == null) 
				SelectedTestResult.okno = new QTestResultPanel(qtResult).createAndShowGUI(QMainFrame.getMainFrame());
			SelectedTestResult.okno.setVisible(true);
		}
	}

	private void showClassifierTestResult(QTestResult qtResult) {
		if (qtResult.isFromMultiClassifier()) {
			qtResult.showResults();
		} else {
			if(SelectedTestResult.okno == null) 
				SelectedTestResult.okno = new QTestResultPanel(qtResult).createAndShowGUI(QMainFrame.getMainFrame());
			SelectedTestResult.okno.setVisible(true);
		}
	}

	
	public void actionPerformed_QmouseClickedTable(java.awt.event.MouseEvent e) {
		JLabel obiekt = (JLabel) e.getSource();
		JMenuItem ElListy;
		boolean jestKlasyfikator;

		TableIconClassifiersMenu.removeAll();
		/* popup_QTableIconVisClassifiers.removeAll(); */
		QKlasyfikator pozycja; // deklaracja tej klasy na koncu tego pliku
								// (rozszerzenie JMEnuItem)
		/* QKlasyfikator2 vpozycja; */

		QClassifierTypes typy = QMainFrame.getClassifierTypes();
		for (QClassifierType typ : typy.getTypes()) {
			pozycja = new QKlasyfikator(typ);
			pozycja
					.setText(typ.getClassName()/*
												 * + "(" + typ.getPathToClass() +
												 * ")"
												 */);
			pozycja.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					actionPerformed_AddClassifierTrainedOnTable(e);
				};
			});
			TableIconClassifiersMenu.add(pozycja);
		}

		TableIconTrainedClassifiersMenu.removeAll();
		jestKlasyfikator = false;
		for (QIcon qi : elements) {
			if (qi.getElem().isClassifier() || qi.getElem().isMulticlassifier()) {
				ElListy = new JMenuItem(qi.getElem().getName());
				ElListy.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						iQProjectElement el;
						el = QmakMain.getMainFrame().getProject().getElement(
								((JMenuItem) e.getSource()).getText());
						actionPerformed_ClassifyTableWithChoosedClassifier(el);
					};
				});
				TableIconTrainedClassifiersMenu.add(ElListy);
				jestKlasyfikator = true;
			}
		}

		if (jestKlasyfikator) {
			popup_QTableIcon.add(TableIconTrainedClassifiersMenu);
			TableIconTrainedClassifiersMenu.setText(mainFrame.messages.getString("jMenuClassifyWith"));
		} else {
			popup_QTableIcon.remove(TableIconTrainedClassifiersMenu);
		}

		if (((iQDataTable) ((QIcon) e.getSource()).getElem()).isClassified()) {
			if (popup_QTableIcon.isAncestorOf(TableIconCopy))
				popup_QTableIcon.remove(TableIconCopy);
			if (popup_QTableIcon.isAncestorOf(TableIconClassifiersMenu))
				popup_QTableIcon.remove(TableIconClassifiersMenu);
			// if
			// (popup_QTableIcon.isAncestorOf(popup_QTableIconVisClassifiers))
			// popup_QTableIcon.remove(popup_QTableIconVisClassifiers);
		} else {
			if (!popup_QTableIcon.isAncestorOf(TableIconCopy))
				popup_QTableIcon.add(TableIconCopy);
			if (!popup_QTableIcon.isAncestorOf(TableIconClassifiersMenu))
				popup_QTableIcon.add(TableIconClassifiersMenu);
			// if
			// (!popup_QTableIcon.isAncestorOf(popup_QTableIconVisClassifiers))
			// popup_QTableIcon.add(popup_QTableIconVisClassifiers);
		}
		popup_QTableIcon.show(this, e.getX() + obiekt.getX(), e.getY()
				+ obiekt.getY());
	}

	// MULTIPLE TEST RESULT
	public void actionPerformed_QRightClickedMultipleTestResult(
			java.awt.event.MouseEvent e) {
		JLabel obiekt = (JLabel) e.getSource();
		popup_QMultipleTestResultIcon.show(this, e.getX() + obiekt.getX(), e
				.getY()
				+ obiekt.getY());
	}

	public void showSelectedMultipleTestResult() {
		if(SelectedMultipleTestResult.okno == null) {
			SelectedMultipleTestResult.okno = ((QMultipleTestResult) SelectedMultipleTestResult.getElem())
					.createView();
		}
		SelectedMultipleTestResult.okno.setVisible(true);
	}

	private void renameMultipleTestResult() {
		String newName = JOptionPane.showInputDialog(this,
				mainFrame.messages.getString("InfoInsertName") + " " + SelectedMultipleTestResult.getElem().getName() + " to",
				"", JOptionPane.PLAIN_MESSAGE);
		if(newName != null) {
			mainFrame.setTitleUnsaved();
			SelectedMultipleTestResult.getElem().setName(QmakMain.getMainFrame()
					.getProject().CreateUniqeName(newName, false));
			SelectedMultipleTestResult.setText(SelectedMultipleTestResult.getElem().getName());
			if(SelectedMultipleTestResult.okno != null)
				SelectedMultipleTestResult.okno.setTitle(SelectedMultipleTestResult.getElem().getName());
			Dimension size = SelectedMultipleTestResult.getPreferredSize();
			SelectedMultipleTestResult.setBounds(SelectedMultipleTestResult.getX(), SelectedMultipleTestResult.getY(),
					size.width, size.height);
		}
	}
	
	private void saveMultipleTestResult() {
		QMultipleTestResult activeMTestResult = (QMultipleTestResult) SelectedMultipleTestResult.getElem();
		
		if (mtrsaver == null) {
			mtrsaver = new QMultipleTestResultSaver(activeMTestResult, mainFrame);
		} else {
			mtrsaver.setMTestResult(activeMTestResult);
		}
		mtrsaver.store();
	}
	// TEST RESULT
	public void actionPerformed_QRightClickedTestResult(
			java.awt.event.MouseEvent e) {
		JLabel obiekt = (JLabel) e.getSource();
		popup_QTestResultIcon.show(this, e.getX() + obiekt.getX(), e.getY()
				+ obiekt.getY());
	}

//	public void showSelectedTestResult() {
//		((QTestResult) SelectedTestResult.getElem()).showResults();
//	}

	public void showSelectedTestResultStatistics() {
//		((QTestResult) SelectedTestResult.getElem()).showStatistics();
		((QTestResult) SelectedTestResult.getElem()).showResults();
	}

	private void renameTestResult() {
		String newName = JOptionPane.showInputDialog(this,
				mainFrame.messages.getString("InfoInsertName") + " " + SelectedTestResult.getElem().getName() + " to",
				"", JOptionPane.PLAIN_MESSAGE);
		if(newName != null) {
			mainFrame.setTitleUnsaved();
			SelectedTestResult.getElem().setName(QmakMain.getMainFrame()
					.getProject().CreateUniqeName(newName, false));
			SelectedTestResult.setText(SelectedTestResult.getElem().getName());
			if(SelectedTestResult.okno != null)
				SelectedTestResult.okno.setTitle(SelectedTestResult.getElem().getName());
			Dimension size = SelectedTestResult.getPreferredSize();
			SelectedTestResult.setBounds(SelectedTestResult.getX(), SelectedTestResult.getY(),
					size.width, size.height);
		}
	}

	// OTHER
	public void actionPerformed_MousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
		}
		if (e.getButton() == MouseEvent.BUTTON2)
			JOptionPane.showMessageDialog(this,
					mainFrame.messages.getString("InfoPressRightMouseButton"));
		if (e.getButton() == MouseEvent.BUTTON3) {
			int x = e.getX();
			int y = e.getY();
			popup_QMainView.show(this, x, y);
		}
	}

	public void actionPerformed_QmouseDragged(java.awt.event.MouseEvent e) {
		JLabel obiekt = (JLabel) e.getSource();
		if (e.getX() + obiekt.getX() - (obiekt.getWidth()/2) < 0 ||
				e.getX()+obiekt.getX()+(obiekt.getWidth()/2) > this.getWidth() ||
				e.getY() + obiekt.getY() - (obiekt.getHeight()/2) < 0 ||
				e.getY() + obiekt.getY() + (obiekt.getHeight()/2) > this.getHeight())
			return;
		
		obiekt.setBounds(e.getX() + obiekt.getX() - (obiekt.getWidth()/2),
			e.getY() + obiekt.getY() - (obiekt.getHeight()/2),
			obiekt.getWidth(), obiekt.getHeight());
	}

	// METODY DOTYCZACE OPERACJI NA IKONACH W PROJEKCIE
	// ===============================================
	/*
	 * ikona ma zawierac w sobie obiekt klasy iQProjectElement
	 */
	private void zaznaczObiektiIkone(QIcon ikona) {
		iQProjectElement el = ikona.getElem();
		if (el == null)
			return;
		if (el.isClassifier()) {
			if (SelectedClassifier != null)
				SelectedClassifier.setInactive();
			SelectedClassifier = ikona;
			SelectedClassifier.setActive();
		}
		if (el.isMulticlassifier()) {
			if (SelectedMulticlassifier != null)
				SelectedMulticlassifier.setInactive();
			SelectedMulticlassifier = ikona;
			SelectedMulticlassifier.setActive();
		}
		if (el.isTable()) {
			if (SelectedTable != null)
				SelectedTable.setInactive();
			SelectedTable = ikona;
			SelectedTable.setActive();
		}
		if (el.isMultipleTestResult()) {
			if (SelectedMultipleTestResult != null)
				SelectedMultipleTestResult.setInactive();
			SelectedMultipleTestResult = ikona;
			SelectedMultipleTestResult.setActive();
		}
		if (el.isTestResult()) {
			if (SelectedTestResult != null)
				SelectedTestResult.setInactive();
			SelectedTestResult = ikona;
			SelectedTestResult.setActive();
		}
	}

	private void insertIcon(iQProjectElement el, Point p) {
		mainFrame.setTitleUnsaved();
		QIcon nowa = new QIcon(el);
		nowa.setText(el.getName());
		nowa.setToolTipText(el.getFileName());
		elements.add(nowa);
		Dimension size = nowa.getPreferredSize();
		if(p == null)
			nowa.setBounds(rnd(0, getWidth() - size.width), rnd(0, getHeight() - size.height), size.width, size.height);
		else
			nowa.setBounds(p.x, p.y, size.width, size.height);

		nowa.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			public void mouseDragged(java.awt.event.MouseEvent e) {
				zaznaczObiektiIkone((QIcon) e.getSource());
				actionPerformed_QmouseDragged(e);
				QMainFrame.getMainFrame().jMainWindow.repaint();
			}
		});

		// TABLE
		if (el.isTable()) {
			nowa.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					zaznaczObiektiIkone((QIcon) e.getSource());
					if (e.getButton() == MouseEvent.BUTTON3)
						actionPerformed_QmouseClickedTable(e);
					if (e.getButton() == MouseEvent.BUTTON1
							&& e.getClickCount() == 2) {
						showSelectedTable();
					}
					QMainFrame.getMainFrame().jMainWindow.repaint();
				}
			});
		}

		// CLASSIFIER
		if (el.isClassifier()) {
			QClassifier elll = (QClassifier) el;
			if (elll.getClassifier() instanceof rseslib.processing.classification.VisualClassifier){
				nowa.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						zaznaczObiektiIkone((QIcon) e.getSource());
						if (e.getButton() == MouseEvent.BUTTON3)
							actionPerformed_QmouseClickedClassifier(e);
						if (e.getButton() == MouseEvent.BUTTON1
							&& e.getClickCount() == 2) {
							pokazWizualizacje();
						}
						QMainFrame.getMainFrame().jMainWindow.repaint();
					}
				});
			
			}else{//dla nie wizualnego
				nowa.addMouseListener(new java.awt.event.MouseAdapter() {
					public void mouseClicked(java.awt.event.MouseEvent e) {
						zaznaczObiektiIkone((QIcon) e.getSource());
						if (e.getButton() == MouseEvent.BUTTON3)
							actionPerformed_QmouseClickedClassifier(e);
						if (e.getButton() == MouseEvent.BUTTON1
								&& e.getClickCount() == 2) {
								showClassifierProperties();
						}
						QMainFrame.getMainFrame().jMainWindow.repaint();
					}
				});	
			}
		}

		// MULTICLASSIFIER
		if (el.isMulticlassifier()) {
			nowa.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					zaznaczObiektiIkone((QIcon) e.getSource());
					if (e.getButton() == MouseEvent.BUTTON3)
						actionPerformed_QmouseClickedMulticlassifier(e);
					if (e.getButton() == MouseEvent.BUTTON1
							&& e.getClickCount() == 2) {
						showSelectedMulticlassifier();
					}
					QMainFrame.getMainFrame().jMainWindow.repaint();
				}
			});
		}
		// MULTIPLE TEST RESULT
		if (el.isMultipleTestResult()) {
			nowa.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					zaznaczObiektiIkone((QIcon) e.getSource());
					QMultipleTestResult el = (QMultipleTestResult) ((QIcon) e
							.getSource()).getElem();
					if (e.getButton() == MouseEvent.BUTTON3)
						actionPerformed_QRightClickedMultipleTestResult(e);
					if (e.getButton() == MouseEvent.BUTTON1
							&& e.getClickCount() == 2) {
						showSelectedMultipleTestResult();
					}
					QMainFrame.getMainFrame().jMainWindow.repaint();
				}
			});
		}
		// TEST RESULT
		if (el.isTestResult()) {
			nowa.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					zaznaczObiektiIkone((QIcon) e.getSource());
					QTestResult el = (QTestResult) ((QIcon) e.getSource())
							.getElem();
					if (e.getButton() == MouseEvent.BUTTON3)
						actionPerformed_QRightClickedTestResult(e);
					if (e.getButton() == MouseEvent.BUTTON1
							&& e.getClickCount() == 2) {
						showClassifierTestResult(el);
					}
					QMainFrame.getMainFrame().jMainWindow.repaint();
				}
			});
		}

		zaznaczObiektiIkone(nowa);
		this.add(nowa);
		this.validate();
		this.repaint();
	}

	private void removeObjectUnderIcon(QIcon ic) {
		if (SelectedClassifier != null && SelectedClassifier.equals(ic)) SelectedClassifier = null;
		if (SelectedMulticlassifier != null && SelectedMulticlassifier.equals(ic))SelectedMulticlassifier = null; 
		if (SelectedMultipleTestResult != null && SelectedMultipleTestResult.equals(ic))SelectedMultipleTestResult = null; 
		if (SelectedTable != null && SelectedTable.equals(ic))SelectedTable = null; 
		if (SelectedTestResult != null && SelectedTestResult.equals(ic))SelectedTestResult = null; 
		mainFrame.setTitleUnsaved();
		if (ic == null)
			return;
		this.remove(ic);
		QmakMain.getMainFrame().getProject().removeElement(ic.getElem());
		elements.remove(ic);
		ic = null;
		this.repaint();
	}

	/*
	 * Zwraca ikone, ktorej elemnetem jest podany element
	 */
	private QIcon dajIkone(iQProjectElement el) {
		Iterator<QIcon> i = elements.iterator();
		QIcon p;
		while (i.hasNext()) {
			p = i.next();
			if (p.getElem().equals(el))
				return p;
		}
		JOptionPane.showConfirmDialog(this, mainFrame.messages.getString("ErrNoIcon") );
		return null;
	}

	/*
	 * laczy dwie ikony strzalka i dodaje ta strzalke do projektu
	 */
	public void makeArrowFromTo(iQProjectElement element,
			iQProjectElement element2) {
		QPara p = new QPara(element, element2);
		QMainFrame.getMainFrame().getProject().getRelatives().add(p);
		QmakMain.getMainFrame().invalidateProject();
		QMainFrame.getMainFrame().jMainWindow.repaint();
	}

	public void deleteArrowFromTo(QIcon e1, QIcon e2) {
		mainFrame.getProject().RemovePair(e1.getElem(), e2.getElem());
	}
	
	public void deleteArrowsTo(QIcon e){
		mainFrame.getProject().RemoveParentsOf(e.getElem());
	}

	public void deleteArrowsFrom(QIcon e){
		mainFrame.getProject().RemoveChildrenOf(e.getElem());
	}


	// =================================================================================================
	// METODY DOTYCZACE RYSOWANIA ITP
	// ==================================================================
	/**
	 * Wyczyszczenie okna po zamknieciu projektu
	 * 
	 */
	public void clear() {
		Iterator i = elements.iterator();
		while (i.hasNext())
			this.remove((QIcon) i.next());
		elements = new HashSet<QIcon>();
		SelectedTable = null;
		SelectedClassifier = null;
		SelectedMulticlassifier = null;
		repaint();
	}

	/**
	 * losowanie liczby pomiedzy min, a max (min,max>0)
	 */
	private int rnd(int min, int max) {
		// max, min > 0
		double d = Math.random();
		d = Math.abs(d);
		if (d > 1 && d != 0)
			d = 1 / d;
		d = d * (max - min);
		d = d + min;
		return (int) d;
	}

	/*
	 * od tej linijki jest obsluga graficznej strony linii czyli glownie
	 * rysowania strzalek pomiedzy ikonami
	 * 
	 */
	private void drawQLine(Graphics G, int x1, int y1, int x2, int y2) {
		G.drawLine(x1, y1, x2, y2);
	}

	private double kw(double a) {
		return a * a;
	}

	/**
	 * Czyli strzalka z punktu a do punktu b
	 * Koncowka strzalki pokazuje sie na elipsie wokol prostokatow a o wymiarach wymA
	 *    oraz b o wymiarach wymB
	 */
	private void rysujStrzalkeElipsa(Graphics G, Point a, Point b, Point wymA,
			Point wymB) {
		/*
		 * wiem, ze podstawowkowa metoda obliczania pierwiastkow k1 = (b^2 +-
		 * sqrt(delta)) / 2a jest niestabilna numerycznie i w ogole fatalna.
		 * Jednak tutaj akurat jest wystarczajaco skuteczna - przewaznie daje
		 * dobry wynik i jest dosc szybka (w implementacji:P)
		 */
		double dlugStrz = 300; // dlugosc wasow strzalki

		double k1, k2;
		Point w = new Point();
		double a1, c1, delta1;
		double a2, c2, delta2;
		Point strz = new Point();
		w.x = a.x - b.x;
		w.y = a.y - b.y;
		// Elipsa pierwsza;
		a1 = kw(w.x * wymA.y) + kw(w.y * wymA.x);
		c1 = -kw(wymA.x * wymA.y);
		delta1 = -4 * a1 * c1; // b=0
		k1 = (-Math.sqrt(delta1)) / (2 * a1);
		if (k1 > 0)
			k1 = (+Math.sqrt(delta1)) / (2 * a1);

		// Elipsa druga
		a2 = kw(w.x * wymB.y) + kw(w.y * wymB.x);
		c2 = -kw(wymB.x * wymB.y);
		delta2 = -4 * a2 * c2; // b=0
		k2 = (-Math.sqrt(delta2)) / (2 * a2);
		if (k2 < 0)
			k2 = (+Math.sqrt(delta2)) / (2 * a2);

		drawQLine(G, a.x + (int) ((double) w.x * k1), a.y
				+ (int) ((double) w.y * k1), b.x + (int) (w.x * k2), b.y
				+ (int) (w.y * k2));

		// wasy strzalki
		strz.x = (int) (w.x * Math.sqrt(dlugStrz / (kw(w.x) + kw(w.y))));
		strz.y = (int) (w.y * Math.sqrt(dlugStrz / (kw(w.x) + kw(w.y))));
		drawQLine(G, b.x + (int) (w.x * k2), b.y + (int) (w.y * k2), b.x
				+ (int) (w.x * k2) - strz.y + strz.x, b.y + (int) (w.y * k2)
				+ strz.x + strz.y);
		drawQLine(G, b.x + (int) (w.x * k2), b.y + (int) (w.y * k2), b.x
				+ (int) (w.x * k2) + strz.y + strz.x, b.y + (int) (w.y * k2)
				- strz.x + strz.y);
	}

	/**
	 * @author Krzysztof Mroczek
	 * Czyli rysuj wszystkie strzalki na ekanie.
	 */
	private void rysujStrzalki(Graphics G) {
		if (QMainFrame.getMainFrame().getProject() == null)
			return;
		if (QMainFrame.getMainFrame().getProject().getRelatives() == null)
			return;
		if (QMainFrame.getMainFrame().getProject().getRelatives().isEmpty())
			return;
		Iterator<QPara> i = QMainFrame.getMainFrame().getProject()
				.getRelatives().iterator();
		QIcon QIR;
		QIcon QID;
		QPara p;
		while (i.hasNext()) {
			p = i.next();
			QIR = dajIkone(p.Rod);
			QID = dajIkone(p.Dziec);
			rysujStrzalkeElipsa(G, new Point(QIR.getX() + QIR.getWidth() / 2,
					QIR.getY() + QIR.getHeight() / 2), new Point(QID.getX()
					+ QID.getWidth() / 2, QID.getY() + QID.getHeight() / 2),
					new Point(QIR.getWidth(), QIR.getHeight()), new Point(QID
							.getWidth(), QID.getHeight()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics) Ta metoda jest
	 *      wywolywana przy odswiezaniu okna, a takze przy takich operacjach na
	 *      oknie jak repeaint
	 */
	public void paint(Graphics G) {
	        // Retrieve the graphics context; this object is used to paint shapes
	        Graphics2D g2d = (Graphics2D)G;
	    
	        // Determine if antialiasing is enabled
//	        RenderingHints rhints = g2d.getRenderingHints();
//	        boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);

	        // Enable antialiasing for shapes
	        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                             RenderingHints.VALUE_ANTIALIAS_ON);
	    
	        // Disable antialiasing for shapes
//	        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//	                             RenderingHints.VALUE_ANTIALIAS_OFF);
	    
	        // Enable antialiasing for text
	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	      
	        // Disable antialiasing for text
//	        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//	                             RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		super.paint(G);
		rysujStrzalki(G);

	}

	/**
	 * Wstawia wszystkie elementy z projektu do okna 
	 */
	public void drawComponents(Set<iQProjectElement> elements) {
		iQProjectElement next;
		Iterator i;
		i = elements.iterator();
		while (i.hasNext()) {
			next = (iQProjectElement) i.next();
			insertIcon(next, next.getPosition());
		}
	}
}

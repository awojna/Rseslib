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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import rseslib.qmak.UI.QAboutDialog;
import rseslib.qmak.UI.QAskProgramExit;
import rseslib.qmak.UI.QAskProjectSave;
import rseslib.qmak.UI.QHelp;
import rseslib.qmak.UI.QMainFrame_mouseAdapter;
import rseslib.qmak.UI.QNewProjectUI;
import rseslib.qmak.UI.QProjectView;
import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.classifier.QClassifierType;
import rseslib.qmak.dataprocess.classifier.QClassifierTypes;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.project.iQProject;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.QDataTableNewDialog;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.UI.iQProjectProperties;
import rseslib.qmak.dataprocess.classifier.*;
import rseslib.qmak.dataprocess.project.*;
import rseslib.qmak.dataprocess.table.QDataTable;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.util.Utils;
import rseslib.structure.attribute.ArrayHeader;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.formats.HeaderFormatException;
import rseslib.structure.attribute.formats.HeaderReader;
import rseslib.structure.data.formats.DataFormatException;
import rseslib.system.progress.EmptyProgress;
import rseslib.structure.attribute.BadHeaderException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.JMenuItem;

/**
 * Klasa reprezentujaca glowne okno programu.
 * @author Krzysztof Mroczek, Leszek Tur, Damian Wojcik, Maciej Zuchniak
 *
 */
public class QMainFrame extends JFrame implements ActionListener {

	/**
	 * Lokalizacje
	 */
	Locale selectedLanguage = null;
	Locale currentLanguage = null;  //  @jve:decl-index=0:
	public ResourceBundle messages = null;  //  @jve:decl-index=0:
	/**
	 * Typy klasyfikatorow
	 */
	private static QClassifierTypes ClassifierTypes = new QClassifierTypes();  //  @jve:decl-index=0:
	
	/**
	 * Singleton glownego okna programu
	 */
	public static QMainFrame qmainframe;

	/**
	 *  Is there an instance of QProject opened
	 */
	private boolean isProjectOpened;

	/**
	 *  Currently used project
	 */
	private QProject qproject; 

	private QNewProjectUI qpnew_ui;

	private JPanel jMainPanel;

	private QHelp qhelp = null; 

	private JMenuBar jMenuBar = new JMenuBar();
	private JMenu jMenuFile = null;
	private JMenuItem jMenuFileNew = null;
	private JMenuItem jMenuFileOpen = null;
	private JMenuItem jMenuFileSave = null;
	private JMenuItem jMenuFileSaveas = null;
	//private JMenuItem jMenuFileClose = null;
	private JMenuItem jMenuFileQuit = null;
	private JMenu jMenuOptions = null;
	private JMenu jMenuOptionsLang = null;
	private JMenuItem jMenuOptionsLangPolish = null;
	private JMenuItem jMenuOptionsLangEnglish = null;
	private JMenuItem jMenuOptionsProjectProp = null;
	private JMenuItem jMenuOptionsAddClType = null;
	private JMenuItem jMenuOptionsRemoveClType = null;
	private JMenu jMenuHelp = null;

	/**
	 * Okno wizualne projektu
	 */
	public QProjectView jMainWindow = null;

	private JFileChooser open_chooser, save_chooser;

	private QAskProjectSave close_chooser;
	
	private QAskProgramExit exit_chooser;

	private JMenuItem jMenuItemAbout = null;
	private JMenuItem jMenuItemHelp = null;
	
	public static QClassifierTypes getClassifierTypes() {return ClassifierTypes;}

	private WindowEvent zdarzenieWyjscia;  //  @jve:decl-index=0:
	
	/**
	 * Konstruktor
	 */
	public QMainFrame() {
		try {
			ReadLanguage();
			this.setProject(new QProject(null));
			isProjectOpened = true;
			generalInit();
			jbInit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Wczytanie jezyka (katalog: domyslny, plik: 'jezyk')
	 * @author Krzysztof Mroczek
	 */
	public void ReadLanguage() {
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(new File("config"+File.separator+"rsestool"+File.separator+"qmak"+File.separator+"jezyk")));
//			this.currentLanguage = new Locale(br.readLine());
//			br.close();
//		} catch (Exception e) {
//			this.currentLanguage = new Locale("English");
//		}
		this.currentLanguage = new Locale("english");
		messages = ResourceBundle.getBundle("rseslib.qmak.UI.ResourceBoundle.Language",currentLanguage); // @jve:decl-index=0:
	}

	public void repaint() {
		super.repaint();
	}

	/**
	 * Returns project object
	 * 
	 * @return iQProject instance or null(when project == null)
	 */
	public iQProject getProject() {
		if (qproject != null)
			return qproject;
		else
			return null;
	}

	/**
	 * Set project
	 * 
	 * @param newProject
	 */
	public void setProject(QProject newProject) {
		qproject = newProject;
	}

	/**
	 * Initialise data connected with size of window and it's title
	 */
	private void generalInit() throws Exception {
		this.setSize(new Dimension(624, 433));
		if (qproject != null) {
			this.setTitle(qproject.getProperties().getName());
		} else {
		this.setTitle(messages.getString("WindowTitle"));
		}
	}

	public QHelp getHelp() {
		if(qhelp == null)
			qhelp = new QHelp();
		return qhelp;
	}

	public void resetHelp() {
		qhelp = null;
	}

	/**
	 * Adds elements, widgets to jMenuBar
	 */
	private void jbInit() throws Exception {

		jMenuFile = new JMenu(messages.getString("jMenuFile"));
		jMenuFileNew = new JMenuItem(messages
				.getString("jMenuFileNew"));
		jMenuFileOpen = new JMenuItem(messages
				.getString("jMenuFileOpen"));
		jMenuFileSave = new JMenuItem(messages
				.getString("jMenuFileSave"));
		jMenuFileSaveas = new JMenuItem(messages
				.getString("jMenuFileSaveAs"));
		//jMenuFileClose = new JMenuItem(messages.getString("jMenuFileClose"));
		jMenuFileQuit = new JMenuItem(messages
				.getString("jMenuFileQuit"));
		
		
		jMenuOptions = new JMenu(messages.getString("jMenuOptions"));
		jMenuOptionsLang = new JMenu(messages.getString("jMenuOptionsLang"));
//		jMenuOptions.add(jMenuOptionsLang);
//		jMenuOptionsLangEnglish = new JMenuItem(messages.getString("jMenuOptionsLangEnglish"));
//		jMenuOptionsLang.add(jMenuOptionsLangEnglish);
//		jMenuOptionsLangPolish = new JMenuItem(messages.getString("jMenuOptionsLangPl"));
//		jMenuOptionsLang.add(jMenuOptionsLangPolish);

		jMenuOptionsProjectProp = new JMenuItem(messages.getString("jMenuOptionsProjectProp"));
		jMenuOptionsAddClType = new JMenuItem(messages.getString("jMenuOptionsAddClType"));
		jMenuOptionsRemoveClType = new JMenuItem(messages.getString("jMenuOptionsRemoveClType"));
		jMenuHelp = new JMenu(messages.getString("jMenuHelp"));

		jMainWindow = new QProjectView(this);
		
		jMenuHelp.add(getJMenuItemHelp());
		jMenuHelp.add(getJMenuItemAbout());
		jMainPanel = (JPanel) this.getContentPane();
		jMainPanel.setLayout(new BorderLayout());
		jMainPanel.add(jMainWindow, BorderLayout.CENTER);
		jMainWindow.setMinimumSize(new Dimension(23, 23));
		jMainWindow.setPreferredSize(new Dimension(800, 600));
		jMainWindow.setToolTipText("");
		jMainWindow.addMouseListener(new QMainFrame_mouseAdapter(jMainWindow));

		// Menu 'File'
		jMenuBar.add(jMenuFile);
		jMenuFile.add(jMenuFileNew);
		jMenuFile.add(jMenuFileOpen);
		jMenuFile.add(jMenuOptionsProjectProp);
		jMenuFile.add(jMenuFileSave);
		jMenuFile.add(jMenuFileSaveas);
		//jMenuFile.add(jMenuFileClose);
		jMenuFile.add(jMenuFileQuit);

		// ActionListeners for Menu 'File'
		jMenuFileQuit.addActionListener(this);
		jMenuFileNew.addActionListener(this);
		jMenuFileOpen.addActionListener(this);
		jMenuFileSave.addActionListener(this);
		jMenuFileSaveas.addActionListener(this);
		//jMenuFileClose.addActionListener(this);

		// Menu 'Options'
		jMenuBar.add(jMenuOptions);
		jMenuOptions.add(jMenuOptionsAddClType);
		jMenuOptions.add(jMenuOptionsRemoveClType);

		// ActionListeners for Menu 'Options'
		jMenuOptions.addActionListener(this);
		jMenuOptionsProjectProp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuProjectproperties_actionPerformed(e);
			}
		});
		
//		jMenuOptionsLangEnglish.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				jMenuOptionsSetLanguage("English");
//			}
//		});
//
//		jMenuOptionsLangPolish.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				jMenuOptionsSetLanguage("Polski");
//			}
//		});

		jMenuOptionsAddClType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuOptionsAddClType_actionPerformed(e);
			}
		});

		jMenuOptionsRemoveClType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuOptionsRemoveClType_actionPerformed(e);
			}
		});
		
		// Menu 'Help'
		jMenuBar.add(jMenuHelp);

		// ActionListeners for Menu 'Help'

		// Ustawienie Menu dla JFrame
		setJMenuBar(jMenuBar);
		
		//tada!!!!!!!
	    setIconImage(Toolkit.getDefaultToolkit().createImage(QMainFrame.class.getResource("imageQletter.gif")));
	    
	}

	public void invalidateProject() {
		jMainPanel.invalidate();
	}

	private void jMenuOptionsSetLanguage(String jezyk) {
		JOptionPane.showMessageDialog(this, messages.getString("InfoRestartProgram"));
		selectedLanguage = new Locale(jezyk);
	}
	
	/**
	 * exit program
	 * 
	 * @param code
	 */
	public void closeProgram(int code) {
		System.exit(code);
	}

	/**
	 * Sets that there is opened project and variable qproject is valid
	 * param boolean
	 */
	public void set_isProjectOpened(boolean b) {
		isProjectOpened = b;
	}

	/**
	 * Adds * to project name in program title when some changes were performed
	 * in project
	 */
	public void setTitleUnsaved() {
		String presentTitle = this.getTitle();
		if ((! presentTitle.endsWith("*")) && (! presentTitle.equals(messages.getString("WindowTitle"))) && (qproject != null)) {
			this.setTitle(presentTitle + "*");
			}
	}
	
	/**
	 * Removes * from project name in program title when project is 100% saved
	 */
	public void setTitleSaved() {
		String presentTitle = this.getTitle();		
		if ((presentTitle.endsWith("*")) && (! presentTitle.equals(messages.getString("WindowTitle"))) && (qproject != null)) {
			this.setTitle(qproject.getProperties().getName());
			}
	}	
	
	/**
	 * Open 'Save As' window and perform saving
	 */
	public void performSaveAs() {
		int returnVal;

		// wybranie pliku do zapisu
		if (save_chooser == null) {
			save_chooser = new JFileChooser();
			save_chooser.setFileFilter(Utils.getFileFilterQPR());
		}
		returnVal = save_chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File project_file = save_chooser.getSelectedFile();
			if (! Utils.getExtension(project_file.getName()).equals(Utils.qpr)) {
				// wybrano nazwe katalogu, w ktorym ma byc zapisany plik			
				String absolute_path = project_file.getAbsolutePath()
						+ System.getProperty("file.separator")
						+ project_file.getName() + "." + Utils.qpr;
				project_file.mkdir();
				project_file = new File(absolute_path);
			}
			try {
				qproject.saveProjectAs(project_file.getAbsolutePath());
				if (qproject.getProperties().isSaved()) {
					setTitleSaved();
					writeInfo(messages.getString("InfoCorrectlySaved"));
				} else {
					writeInfo(messages.getString("InfoWrongSaved"));
				}
			} catch (Exception ex_2) {
				ex_2.printStackTrace();
				writeInfo(messages.getString("InfoWrongSaved"));
			}

		}
		else {
			zdarzenieWyjscia = null;
		}
	}

	/**
	 * Try to save project, if unsuccesful run method performSaveAs
	 */
	public void performSave() {
		try {
			qproject.saveProject();
			if (qproject.getProperties().isSaved()) {
				setTitleSaved();
				writeInfo(messages.getString("InfoCorrectlySaved"));
			} else {
				writeInfo(messages.getString("InfoWrongSaved"));
			}
		} catch (Exception ex) {
			performSaveAs();
		}
	}

	/**
	 * Basic ActionListener method for JMenuItem`s in QMainFrame class. It
	 * controls Menu: 'File' 'options' ...
	 */
	public void actionPerformed(ActionEvent e) {

		JMenuItem actionSource = (JMenuItem) (e.getSource());
		String optionName = actionSource.getText();
		int returnVal;

		// Option choosen: New Project
		if (optionName == (messages.getString("jMenuFileNew"))) {
			// check whether there is already project opened
			if (isProjectOpened) {
				if (close_chooser == null) {
					close_chooser = new QAskProjectSave(this);
				}
				close_chooser.setLocationRelativeTo(this);
				close_chooser.setModal(true);
				close_chooser.showDialog();
			}
			if (!isProjectOpened) {
				// chcek whether we've already created qpnew_ui JDialog
				if (qpnew_ui == null) {
					qpnew_ui = new QNewProjectUI(this);
					qpnew_ui.setLocationRelativeTo(this);
					qpnew_ui.setModal(true);
				}
				qpnew_ui.showNewDialog(this);				
				// uzytkownik wpisze dane za chwile
				// nowa instancja zaistnieje == (isProjectOpened==true)
			} 
		}

		// Option choosen: Open Project - there is READY, but not in QProject
		if (optionName == (messages.getString("jMenuFileOpen"))) {
			// check (lazy!) whether there is already project opened
			if (isProjectOpened && (! qproject.isSaved())) {
				if (close_chooser == null) {
					close_chooser = new QAskProjectSave(this);
				}
				close_chooser.setLocationRelativeTo(this);
				close_chooser.setModal(true);
				close_chooser.showDialog();
			} else
				closeProject();
			if (!isProjectOpened) {
				if (open_chooser == null) {
					open_chooser = new JFileChooser();
					open_chooser.setFileFilter(Utils.getFileFilterQPR());
				}
				returnVal = open_chooser.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File project_file = open_chooser.getSelectedFile();
					QProject proj = new QProject(project_file.getAbsolutePath());
					try {
						proj.loadProject();
						qproject = proj;
						set_isProjectOpened(true);
						setTitle(qproject.getProperties().getName());	
						jMainWindow.clear();
						jMainWindow.drawComponents(qproject.GetProjectElements());
					} catch (Exception ex) {
						ex.printStackTrace();
						writeInfo(messages.getString("ErrOpenWithError") 
								+ ex.toString());
					}
					repaint();
				}

			}
		}

		if (optionName == (messages.getString("jMenuFileSave"))) {
			if (!isProjectOpened) {
				writeInfo(messages.getString("ErrNoProject"));
			} else {
				if (qproject.getProperties().isSaved()) {
					writeInfo(messages.getString("ErrAlreadySaved"));
				} else {
					performSave();
				}
			}
		}

		if (optionName == (messages.getString("jMenuFileSaveAs"))) {
			if (!isProjectOpened) {
				writeInfo(messages.getString("ErrNoProject"));
			} else {
				performSaveAs();
			}
		}

		/*if (optionName == (messages.getString("jMenuFileClose"))) {
			if (isProjectOpened) {
				if (close_chooser == null) {
					close_chooser = new QAskProjectSave(this);
				}
				close_chooser.setLocationRelativeTo(this);
				close_chooser.setModal(true);
				close_chooser.showDialog();
			}
		}*/

		if (optionName == (messages.getString("jMenuFileQuit"))) {
			if (isProjectOpened) {
				if (!qproject.getProperties().isSaved()) {
					performSave();
				}
			}
			//ObslugaJezyka();
			closeProgram(0);
		}
	}

	/**
	 * Wypisuje na ekranie podana informacje w oknie dialogowym.
	 * 
	 * @param text tekst do wypisania
	 */
	public void writeInfo(String text) {
		JOptionPane.showMessageDialog(this, text, messages.getString("Information"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static QMainFrame getMainFrame() {
		if (qmainframe == null) {
			qmainframe = new QMainFrame();
		}
		return qmainframe;
	}

	public void closeProject() {
		qproject = null;
		set_isProjectOpened(false);
		jMainWindow.clear();
		setProject(new QProject(null));	
		setTitleSaved();
		setTitle(qproject.getProperties().getName());
	}

	/**
	 * Zapisanie i zamkniecie projektu
	 *
	 */
	public void closeAndSaveProject() {
		performSave();
		closeProject();
	}

	void jMenuProjectproperties_actionPerformed(ActionEvent e) {
		if (isProjectOpened) {
			iQProjectProperties dlg = new iQProjectProperties(this, true);
			Dimension dlgSize = dlg.getPreferredSize();
			Dimension frmSize = getSize();
			Point loc = getLocation();
			dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
					(frmSize.height - dlgSize.height) / 2 + loc.y);
			dlg.assignData(qproject.getProperties());
			dlg.setModal(true);
			dlg.show();
			dlg.this_componentResized(null);
			invalidateProject();
		} else {
			writeInfo(messages.getString("ErrNoProject"));
		}
	}

	// na podstawie TOptionsAddClassifierDialog z trikstera
	public void jMenuOptionsAddClType_actionPerformed(ActionEvent e) {
		
		class myFilter extends FileFilter {
		    public boolean accept(File f) {
		    	String extension = Utils.getExtension(f);
		        if (f.isDirectory()) {
		            return true;
		        }
		    	if (extension.equals("java"))
		    		return true;
		    	else 
		    		return false;
		    }
		    public String getDescription() {
		        return "Java files (*.java)";
		    }
		}
		
		
	    JFileChooser dlg = new JFileChooser(".");
	    dlg.setFileFilter(new myFilter());
	    int rval = dlg.showOpenDialog(this);
	    String name = "";
	    if (rval == JFileChooser.APPROVE_OPTION) {
	    	name = dlg.getSelectedFile().getAbsolutePath();
	    }


	 if ((name != null) && (name.length() > 0)) {   
	    try {
	      name = name.substring(name.indexOf("rseslib")); // remove everything before rseslib
	      name = name.substring(0, name.lastIndexOf(".")); // remove ".java"
	      name = name.replace('/', '.'); // change /'s into .'s
	      name = name.replace('\\', '.'); // change /'s into .'s
	    }
	    catch (StringIndexOutOfBoundsException ex) {
	      name = "";
	    }
	    catch (NullPointerException ex) {
	      name = "";
	    }
	    
	    if (name.equals("")){
			JOptionPane.showMessageDialog(qmainframe, "Wrong file","Warning", JOptionPane.WARNING_MESSAGE);
	    } else {
	    	try {
				Class classifierClass = Class.forName(name);
				//sprawdzenie czy wsrod implementowanych interfejsow jest interfejs Classifier
			    Class[] theInterfaces = classifierClass.getInterfaces();
			    Set<String> s = new HashSet<String>();
			    for (int i = 0; i < theInterfaces.length; i++) {
			    	String interfaceName = theInterfaces[i].getName();
			    	s.add(interfaceName);
			    }
			    if (!s.contains("rseslib.processing.classification.Classifier")){
					JOptionPane.showMessageDialog(qmainframe, "Class must implement Classifier interface","Warning", JOptionPane.WARNING_MESSAGE);
					return;
			    }
			} catch (ClassNotFoundException e1) {
				JOptionPane.showMessageDialog(qmainframe, "Class not found","Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}	
	    		QClassifierType nowyTyp = new QClassifierType("new",name);
	    		QMainFrame.getClassifierTypes().add(nowyTyp);
	    		QMainFrame.getClassifierTypes().saveClassifierTypesConfiguration();	
	    	}
	 	}
	}
	
	public void jMenuOptionsRemoveClType_actionPerformed(ActionEvent e) {

		Vector<String> vec = new Vector<String>();
		Object[] possibilities;
		
		for (Iterator it = QMainFrame.getClassifierTypes().getTypes().iterator(); it.hasNext();) {
			QClassifierType el = (QClassifierType) it.next();
			vec.add(el.getPathToClass());
		}
		possibilities = vec.toArray();
		QMainFrame.getClassifierTypes().getTypes();
		
		String s = (String)JOptionPane.showInputDialog(
		                    QMainFrame.getMainFrame(),
		                    messages.getString("jMenuOptionsAddClTypeMessage"),
		                    messages.getString("jMenuOptionsRemoveClType"),
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,//icon
		                    possibilities,
		                    "ham");

		if ((s != null) && (s.length() > 0)) {
		    QMainFrame.getClassifierTypes().remove(s);
		    QMainFrame.getClassifierTypes().saveClassifierTypesConfiguration();
		}
	}

	
	private boolean lounchFileChooser(JFileChooser chos, int dialogType) {
		int rval;
		switch (dialogType) {
		case JFileChooser.OPEN_DIALOG:
			rval = chos.showOpenDialog(this);
			break;
		case JFileChooser.SAVE_DIALOG:
			rval = chos.showSaveDialog(this);
			break;
		default:
			return false;
		}
		if (rval == JFileChooser.APPROVE_OPTION) {
			return true;
		}
		return false;
	}

	private String lounchFileChooserForDataTable(int dialogType, String title) {
		JFileChooser chos = new JFileChooser(".");
		chos.setDialogTitle(title);
		if (dialogType == JFileChooser.SAVE_DIALOG)
			chos.setFileFilter(Utils.getFileFilterQDT());
		String name = null;
		if (lounchFileChooser(chos, dialogType)) {
			File file = chos.getSelectedFile();
			name = chos.getSelectedFile().getAbsolutePath();
			if (Utils.getExtension(file).equals("")
					&& (dialogType == JFileChooser.SAVE_DIALOG)) {
				name = name + "." + Utils.qdt;
			}
		}
		return name;
	}

	public iQProjectElement jMenuTableNew_actionPerformed(ActionEvent e) {
		QDataTableNewDialog dlg = new QDataTableNewDialog(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,
				(frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setVisible(true);
		HeaderReader hr = dlg.getHeaderReader();
		if (hr != null) {
			Header hdr;
			try {
				hdr = new ArrayHeader(hr);
				if (!isCorrectHeader(hdr)) {
					JOptionPane.showMessageDialog(this,
							messages.getString("ErrMustBeDecisionAttribute"),
							messages.getString("ErrMustBeDecisionAttributeTitle"),
							JOptionPane.NO_OPTION); 
					return null;
				}
			} catch (Exception ex) {
				return null;
			}
			iQDataTable tab = new QDataTable(dlg.getDataTableName(), hdr);
			QmakMain.getMainFrame().getProject().GetProjectElements().add(tab);
			invalidateProject();
			return tab;
		}
		return null;
	}

	public boolean isCorrectHeader(Header hdr) {
		int dec = 0;
		for (int i = 0; i < hdr.noOfAttr(); i++) {
			if (hdr.attribute(i).isDecision())
				dec++;
		}
		if (dec != 1)
			return false;
		else {
			if (hdr.attribute(hdr.decision()).isNominal()) {
				return true;
			} else
				return false;
		}
	}
	
	private String getNameFromFileName(String FileName) {
		int i;
		int point;
		i = FileName.length()-1;
		while (i > 1 && FileName.toCharArray()[i] != '.')
			i--;
		point = i;
		i--;
		while (i > 1 && FileName.toCharArray()[i] != '.' && FileName.toCharArray()[i] != '\\')
			i--;
		i++;
		return FileName.substring(i, point);
	}

	public iQProjectElement jMenuFileAddRseslibTable_actionPerformed(
			ActionEvent e) {
		String fileName = lounchFileChooserForDataTable(
				JFileChooser.OPEN_DIALOG, messages.getString("InfoSelectFileWithTable")); 
		if (fileName != null) {
			File file = new File(fileName);

//			QAddTableDialog dial = new QAddTableDialog(this);
//			Dimension dlgSize = dial.getPreferredSize();
//			Dimension frmSize = getSize();
			getLocation();
//			dial.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,(frmSize.height - dlgSize.height) / 2 + loc.y);

//			dial.show();

			/*
			if (!dial.CREATE || (dial.getTName() == null)|| (dial.getTFileName() == null)) {
				return null;
			}
			*/
			try {

				QDataTable newTab = new QDataTable(file, new EmptyProgress(),
						qproject.CreateUniqeName(getNameFromFileName(fileName), true), fileName);

				if (this.getProject() == null)
					QmakMain.Log.error(messages.getString("ErrNoProject2"));
				this.getProject().insertElement(newTab);
				this.invalidateProject();
				return newTab;
			} catch (IOException f) {
				QmakMain.Log.error(f.toString());
			} catch (InterruptedException g) {
				QmakMain.Log.error(g.toString());
			} catch (HeaderFormatException f) {
				QmakMain.Log.error(messages.getString("ErrWrongHeader"));
			} catch (DataFormatException f) {
				QmakMain.Log.error(messages.getString("ErrWrongData"));
			}
		}
		return null;
	}

	public iQProjectElement jMenuFileAddRseslibTableAndHeader_actionPerformed(
			ActionEvent e) {
		String fileName = lounchFileChooserForDataTable(
				JFileChooser.OPEN_DIALOG, messages.getString("InfoSelectFileWithTable")); 
		String HeaderName = lounchFileChooserForDataTable(
				JFileChooser.OPEN_DIALOG, messages.getString("InfoSelectFileWithHeader")); 
		if (fileName != null) {
			File file = new File(fileName);
			File Hfile = new File(HeaderName);

//			QAddTableDialog dial = new QAddTableDialog(this);
//			Dimension dlgSize = dial.getPreferredSize();
//			Dimension frmSize = getSize();
			getLocation();
//			dial.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x,(frmSize.height - dlgSize.height) / 2 + loc.y);

//			dial.show();

			/*
			if (!dial.CREATE || (dial.getTName() == null)|| (dial.getTFileName() == null)) {
				return null;
			}
			*/
			try {

				QDataTable newTab = new QDataTable(file,Hfile,new EmptyProgress(),qproject.CreateUniqeName(getNameFromFileName(fileName), true), fileName);

				if (this.getProject() == null)
					QmakMain.Log.error(messages.getString("ErrNoProject2"));
				this.getProject().insertElement(newTab);
				this.invalidateProject();
				return newTab;
			} catch (IOException f) {
				QmakMain.Log.error(f.toString());
			} catch (InterruptedException g) {
				QmakMain.Log.error(g.toString());
			} catch (BadHeaderException b) {
				QmakMain.Log.error(messages.getString("ErrWrongHeader"));
			} catch (HeaderFormatException f) {
				QmakMain.Log.error(messages.getString("ErrWrongHeader"));
			} catch (DataFormatException f) {
				QmakMain.Log.error(messages.getString("ErrWrongData"));
			}
		}
		return null;
	}

	public iQProjectElement TrainClassifierOnTable_actionPerformed(ActionEvent e) {

		QClassifierType CT = new QClassifierType("nazwa","abc.abc");
		QClassifier Clas = new QClassifier(CT, "f1");

		if (this.getProject() == null) {
			QmakMain.Log.error(messages.getString("ErrNoProject2"));
			return null;
		}
		this.getProject().GetProjectElements().add(Clas);
		this.invalidateProject();
		return Clas;
	}

	/**
	 * This method initializes jMenuItemAbout	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItemAbout() {
		if (jMenuItemAbout == null) {
			jMenuItemAbout = new JMenuItem();
			jMenuItemAbout.setText(messages.getString("About"));
			jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					QAboutDialog dlg = new QAboutDialog(QMainFrame.getMainFrame());
				    Dimension dlgSize = dlg.getPreferredSize();
				    Dimension frmSize = getSize();
				    Point loc = getLocation();
				    dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
				        (frmSize.height - dlgSize.height) / 2 + loc.y);
				    dlg.setModal(true);
				    dlg.pack();
					dlg.setVisible(true);
				}
			});
		}
		return jMenuItemAbout;
	}
	
	/**
	 * This method initializes jMenuItemHelp	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getJMenuItemHelp() {
		if (jMenuItemHelp == null) {
			jMenuItemHelp = new JMenuItem();
			jMenuItemHelp.setText(messages.getString("jMenuHelp"));
			jMenuItemHelp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getHelp().pokaz("QProjectView_popup_menu");
				};
			});
		}
		return jMenuItemHelp;
	}
	
	/**
	 * Obsluga jezykow - zapisanie do pliku aktualnego jezyka
	 * @author Krzysztof Mroczek
	 */
//	private void ObslugaJezyka() {
//		try {
//
//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("config"+File.separator+"rsestool"+File.separator+"qmak"+File.separator+"jezyk")));
//			if (selectedLanguage != null)
//				bw.append(selectedLanguage.getDisplayName());
//			else
//				bw.append(currentLanguage.getDisplayName());
//			bw.newLine();
//			bw.close();
//		} catch (Exception ex) {
//			performSaveAs();
//		}
//	}
	
	/**
	 * Obsluga proby zamkniecia programu rpzez uzytkownika przy pomocy x
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			//ObslugaJezyka();
			if (isProjectOpened) {
				if (!qproject.getProperties().isSaved()) {
					//dodanie tego okna dot zapisania
					zdarzenieWyjscia = e;
					if (exit_chooser == null) {
							exit_chooser = new QAskProgramExit(this);
					}
					exit_chooser.setLocationRelativeTo(this);
					exit_chooser.setModal(true);
					exit_chooser.showDialog();
					
				}
				else super.processWindowEvent(e);
			}
			else super.processWindowEvent(e);
		}
		else super.processWindowEvent(e);
	}

	/**
	 * Wyjscie z programu bez zapisywania projektu
	 *
	 */
	public void exitWithoutSave() {
		if (zdarzenieWyjscia != null) super.processWindowEvent(zdarzenieWyjscia);
	}
	
	/**
	 * Wyjscie z programu z zapisywaniem projektu
	 *
	 */
	public void exitWithSave() {
		performSave();
		if (zdarzenieWyjscia != null) super.processWindowEvent(zdarzenieWyjscia);
	}

} // @jve:decl-index=0:visual-constraint="2,-1"

class QMainFrame_mouseAdapter extends MouseAdapter {
	private QProjectView adaptee;

	QMainFrame_mouseAdapter(QProjectView adaptee) {
		this.adaptee = adaptee;
	}

	public void mouseReleased(MouseEvent e) {
		adaptee.actionPerformed_MousePressed(e);
	}
	/*
	 * public void mousePressed(MouseEvent e) {
	 * adaptee.m_projectTree_mousePressed(e); }
	 */
}

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
import java.awt.Dimension;
import java.awt.List;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;

import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.JTextPane;

import rseslib.qmak.UI.QMainFrame;

/**
 * Okienko w kt�rym pokazywana jest pomoc u�ytkownika
 * @author Krzysztof Mroczek
 */

public class QHelp extends JDialog {
	
	public ResourceBundle help = ResourceBundle.getBundle("rseslib.qmak.UI.ResourceBoundle.help",QMainFrame.getMainFrame().currentLanguage); // @jve:decl-index=0:
	public ResourceBundle helpTemat = ResourceBundle.getBundle("rseslib.qmak.UI.ResourceBoundle.helpHasla",QMainFrame.getMainFrame().currentLanguage); // @jve:decl-index=0:
	
	private JDesktopPane jDesktopPane = null;
	private JButton OKButton = null;
	private List Hasla = null;
	private JTextPane Tresc = null;
	
	public QHelp() {
		this.initialize();
		this.wypelnijHasla();
	}

	/** 
	 * U�ywa� tej metody do otwierania tego okna.
	 * @param haslo Tutaj podajemy nazwe hasla z pliku help.properties lub helpHasla.properties
	 */
	public void pokaz(String haslo) {
		zaznaczHaslo(haslo);
		this.pokazTresc();
		this.setLocationRelativeTo(QMainFrame.getMainFrame());
		this.setVisible(true);
	}
	
	/**
	 * This method initializes 
	 * 
	 */
	private void initialize() {
		this.setSize(new Dimension(524, 356));
		this.setTitle("Help");
		this.setContentPane(getJDesktopPane());
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				QMainFrame.getMainFrame().resetHelp();
				((JDialog) e.getSource()).setVisible(false);
				((JDialog) e.getSource()).dispose();
			}
		});
	}
	
	private void wypelnijHasla() {
		Enumeration<String> klucze = helpTemat.getKeys();
		Hasla.removeAll();
		while (klucze.hasMoreElements()) {
			Hasla.add(helpTemat.getString(klucze.nextElement()));
		}	
	}	

	private void zaznaczHaslo(String nap) {
		Enumeration<String> klucze = helpTemat.getKeys();
		String temat = helpTemat.getString(nap);
		int i = 0;
		while (klucze.hasMoreElements()) {
			nap = klucze.nextElement();
			if (temat == Hasla.getItem(i))
				Hasla.select(i);
			i++;
		}	
	}	

	/**
	 * This method initializes jDesktopPane	
	 * 	
	 * @return javax.swing.JDesktopPane	
	 */
	private JDesktopPane getJDesktopPane() {
		if (jDesktopPane == null) {
			jDesktopPane = new JDesktopPane();
			jDesktopPane.add(getOKButton(), null);
			jDesktopPane.add(getHasla(), null);
			jDesktopPane.add(getTresc(), null);
		}
		return jDesktopPane;
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOKButton() {
		if (OKButton == null) {
			OKButton = new JButton();
			OKButton.setText("OK");
			OKButton.setBounds(new Rectangle(224, 271, 275, 44));
			OKButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					exit();
				}
			});
		}
		return OKButton;
	}
	
	private void exit() {
		QMainFrame.getMainFrame().resetHelp();
		this.setVisible(false);
		this.dispose();
	}

	/**
	 * This method initializes Hasla	
	 * 	
	 * @return java.awt.List	
	 */
	private List getHasla() {
		if (Hasla == null) {
			Hasla = new List();
			Hasla.setBounds(new Rectangle(18, 16, 195, 297));
			Hasla.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					pokazTresc();
				}
			});
		}
		return Hasla;
	}

	private void pokazTresc() {
		Enumeration<String> klucze = helpTemat.getKeys();
		String nap;
		while (klucze.hasMoreElements()) {
			nap = klucze.nextElement();
			if (helpTemat.getString(nap) == Hasla.getSelectedItem())
				Tresc.setText(help.getString(nap));
		}	
	}

	/**
	 * This method initializes Tresc	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getTresc() {
		if (Tresc == null) {
			Tresc = new JTextPane();
			Tresc.setBounds(new Rectangle(225, 16, 273, 243));
		}
		return Tresc;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"

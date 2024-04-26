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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import rseslib.processing.classification.VisualClassifier;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.structure.data.DoubleData;

import javax.swing.JSplitPane;


/**
 * Klasa reprezentujaca panele na ktorych pokazuje sie wizualizacja klasyfikatora
 * @author Damian Wojcik
 *
 */
public class QVisClassifierView extends JDialog {

	private JPanel jPanel = null;
	private VisualClassifier classifier; 	
	
	private String name;
	private String comment;
	
	/**
	 * This method initializes 
	 * 
	 */
	public QVisClassifierView(iQClassifier clas) {
		super();
		classifier = (VisualClassifier) clas.getClassifier();
		name = clas.getName();
		initialize();	
		
		//classifier.draw(jPanel1);	
	}

	
	public void draw() {
		classifier.draw(jPanel);
	}
	/**
	 * Klasyfikuj wizualnie wiersz
	 */
	public void classifyOne(DoubleData obj) {
		classifier.drawClassify(jPanel, obj);	
	}
	
	/**
	 * Wyswietl na drugim panelu komentarz
	 * @param newComment napis
	 */
	public void addComment(String newComment) {
		comment = newComment;
		repaint();
		}
	
	public void changeName(String newName) {
		name = newName;
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		comment = null;
        this.setSize(new Dimension(800, 600));
        this.setContentPane(getJPanel());
        this.repaint();
			
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BorderLayout());
			jPanel.setPreferredSize(new Dimension(800, 600));
			jPanel.validate();
			jPanel.setVisible(true);
		}
		return jPanel;
	}

	public void paint(Graphics g) {
		if (comment == null)
			setTitle(name);
		else
			setTitle(name + " classification result: " + comment);
		super.paint(g);
	}
}  
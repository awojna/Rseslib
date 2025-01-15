/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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

import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import rseslib.qmak.UI.QVisClassifierView;
import rseslib.qmak.UI.chart.QChartFrame;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.dataprocess.table.QDataTable;

/**
 * 
 * @author Krzysztof Mroczek
 * @author Leszek Tur
 *
 */
public class QIcon extends JLabel {
	private iQProjectElement Elem;
	public JDialog okno;
	public QVisClassifierView oknoKlas;
	public QChartFrame chart_gen;
	
	iQProjectElement getElem() {return Elem;}
	
	QIcon(iQProjectElement el){
		okno = null;
		oknoKlas = null;
		chart_gen = null;
		Elem = el;
		setInactive();
	}
	
	public void setBounds(int x,int y, int width, int height) {
		Elem.setPosition(new Point(x,y));
		super.setBounds(x, y, width, height);
	}
	

	public void setInactive(){
		setBorder(BorderFactory.createRaisedBevelBorder());
		if (Elem.isTable()){
			if (((QDataTable) Elem).isClassified())
				setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageTableClassifiedInactive.gif")));
			else
				setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageTableInactive.gif")));
		}
		if (Elem.isClassifier())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageClassifierInactive.gif")));
		if (Elem.isMulticlassifier())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageMulticlassifierInactive.gif")));
		if (Elem.isMultipleTestResult())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageMultipleTestResultInactive.gif")));
		if (Elem.isTestResult())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageTestResultInactive.gif")));

	}
	
	public void setActive(){
		setBorder(BorderFactory.createLoweredBevelBorder());
		if (Elem.isTable()){
			if (((QDataTable) Elem).isClassified())
				setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageTableClassifiedActive.gif")));
			else
				setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageTableActive.gif")));
		}
		if (Elem.isClassifier())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageClassifierActive.gif")));
		if (Elem.isMulticlassifier())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageMulticlassifierActive.gif")));		
		if (Elem.isMultipleTestResult())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageMultipleTestResultActive.gif")));
		if (Elem.isTestResult())
			this.setIcon(new ImageIcon(rseslib.qmak.UI.QMainFrame.class.getResource("imageTestResultActive.gif")));
	}
}










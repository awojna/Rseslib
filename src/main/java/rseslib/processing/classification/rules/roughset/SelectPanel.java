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


package rseslib.processing.classification.rules.roughset;

import java.awt.BorderLayout;

import javax.swing.*;


/**
 * This class is similar to SortPanel and implements selecting part of 
 * all rules to be shown in a table. It has one more combobox with values of given attribute so that user can select value and only those rules which have that value equal to selected will be shown.
 * @author Krzysztof Niemkiewicz
 */
public class SelectPanel extends SortPanel {

	private static final long serialVersionUID = -8112649790421360180L;
	JComboBox select;
	SelectPanel(SelectMainModel smm, int nr) {
		super();		
		setLayout(new BorderLayout());
		choose=new JComboBox();
		this.add(new JLabel("Select: "),BorderLayout.WEST);
		choose.setEditable(false);
		SortModel som=new SortModel(smm,nr);
		choose.setModel(som);
		choose.setRenderer(getAttributeRenderer(smm));
		this.add(choose,BorderLayout.CENTER);
		
		JPanel p1=new JPanel(new BorderLayout());
		p1.add(new JLabel(" = "),BorderLayout.WEST);
		select=new JComboBox();
		SelectModel sm=new SelectModel(smm,nr);
		select.setModel(sm);
		som.addListDataListener(sm);
		p1.add(select,BorderLayout.CENTER);
		this.add(p1,BorderLayout.EAST);
	}


}

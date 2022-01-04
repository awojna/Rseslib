/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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

import javax.swing.*;

/**
 * Main panel which contains all sortPanels. It grows  and adds new 
 * SortPanel if needed.  
 * @author Krzysztof Niemkiewicz
 */

public class MainSortPanel extends ColumnPanel {

	private static final long serialVersionUID = 7824171553520654433L;
	protected int size;
	protected SortMainModel smm;
	
//	private JFrame parent;
	public MainSortPanel(){};	
	public MainSortPanel(RulesTableModel rtm){	
		smm=new SortMainModel(this,rtm);		
		this.add(new SortPanel(smm,0));
		size=0;
		setBorder(BorderFactory.createRaisedBevelBorder());
		
	}
    /**
     * check if we need to grow, in fact only in the case when i==number
     * of sortPanels already existing
     */

	public void valueChanged(){
		if (size+1<smm.getTotalSize()){
			size++;
			this.add(new SortPanel(smm,size));
			
			this.validate();
	//		parent.pack();
		}
	}
}

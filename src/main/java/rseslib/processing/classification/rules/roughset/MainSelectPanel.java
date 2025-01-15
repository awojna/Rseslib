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


package rseslib.processing.classification.rules.roughset;
import javax.swing.*;

/**
 * Main panel which contains all selectPanel. It grows  and adds new 
 * SelectPanel if needed.  
 * @author Krzysztof Niemkiewicz
 */

public class MainSelectPanel extends MainSortPanel {
	
	private static final long serialVersionUID = -4062451835472967398L;
	public MainSelectPanel(RulesTableModel rtm){	
		smm=new SelectMainModel(this,rtm);		
		this.add(new SelectPanel((SelectMainModel)smm,0));
		size=0;
		setBorder(BorderFactory.createRaisedBevelBorder());
		
	}
    /**
     * check if we need to grow, in fact only in the case when i==number
     * of selectPanels already existing
     */
	public void valueChanged(){
		if (size+1<smm.getTotalSize()){
			size++;
			this.add(new SelectPanel((SelectMainModel)smm,size));
			
			this.validate();
	//		parent.pack();
		}
	}
}

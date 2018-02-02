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


package rseslib.processing.classification.rules.roughset;
import java.awt.BorderLayout;
import java.awt.Component;
import rseslib.structure.attribute.Attribute;
import javax.swing.*;


/**
 * Panel which enables control over single sorting option.
 * @author Krzysztof Niemkiewicz
 */
public class SortPanel extends JPanel {
	private static final long serialVersionUID = 1628337065716693137L;
	protected JComboBox choose;
    /**
     *  Default constructor, used only by subclasses
     */
	SortPanel(){};
    /**
     *  Main constructor
     *  @param smm main sort model, used to connect our own sort model to it
     *  @param nr as SortPanel are many in SortMainPanel, we have to know which number this has
     */
	
	SortPanel(SortMainModel smm,int nr){
		super();		
		setLayout(new BorderLayout());
		choose=new JComboBox();
		if (nr==0){this.add(new JLabel("Sort by"),BorderLayout.WEST);}else
			this.add(new JLabel("then sort by:"),BorderLayout.WEST);
		choose.setEditable(false);
		
		choose.setModel(new SortModel(smm,nr));
		choose.setRenderer(getAttributeRenderer(smm));
		this.add(choose,BorderLayout.CENTER);
		this.add(new JPanel(),BorderLayout.EAST);
		
	}
	ListCellRenderer getAttributeRenderer(SortMainModel smm){
		return new AttributeRenderer(smm);
	}
}


/**
 * ListCell Renderer for atrributes, not very elaborated
 */
class AttributeRenderer implements  ListCellRenderer{
	SortMainModel smm;
	AttributeRenderer(SortMainModel smm){
		this.smm=smm;
	}
	public Component getListCellRendererComponent(JList arg0, Object arg1,
			int arg2, boolean arg3, boolean arg4) {
		String s=((Attribute)arg1).name();
		
		return new JLabel(s);


	}
	
}
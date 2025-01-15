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
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

import rseslib.structure.attribute.NominalAttribute;

/**
 * Main panel of Visual Rough Set Classifier
 * @author Krzysztf Niemkiewicz
 */

public class VisualRoughSetPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTable jt; 
    /**
     * Main constructor
     * @param vrs classifier which will be visualized
     * @param defSelector default selector, normally null, used to create visualization of classiffication of single table element
     */
	VisualRoughSetPanel(RoughSetRulesVisual vrs,RulesSelector defSelector){
		JLabel amountLabel=new JLabel();
		RulesTableModel rtm=new RulesTableModel(vrs,defSelector);
		new AmountController(amountLabel,rtm,vrs,(defSelector!=null));				
		jt=new JTable(rtm);
		//jt.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		for (int i=0;i<jt.getColumnModel().getColumnCount()-1;i++){
			jt.getColumnModel().getColumn(i).setMaxWidth(70);
		}
		jt.getTableHeader().setFont(jt.getTableHeader().getFont().deriveFont(Font.BOLD));
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );
		jt.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		jt.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		jt.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		setLayout(new BorderLayout());
		
		JScrollPane tableScroll=new JScrollPane(jt);
		
		JPanel northPane=new JPanel(new BorderLayout());
		northPane.add(tableScroll,BorderLayout.CENTER);
		northPane.add(amountLabel,BorderLayout.SOUTH);
		add(northPane,BorderLayout.CENTER);
		JPanel southPane=new JPanel(new GridLayout(1,2));
		
		add(southPane,BorderLayout.SOUTH);
		southPane.setBorder(BorderFactory.createBevelBorder(3));		
		
		southPane.add(new MainSortPanel(rtm),BorderLayout.WEST);	
		southPane.add(new MainSelectPanel(rtm),BorderLayout.CENTER);
		
		
		
	}
}
/**
 * Listener which changes a small JLabel below table, the label shows how many 
 * rules are selected from the total or from selected by default selector
 * @author Krzysztof Niemkiewicz
 */
class AmountController implements TableModelListener{

	JLabel jl;
	RulesTableModel rtm;
	RoughSetRulesVisual vrs;
	boolean showDefault; 
	private void setLabelText(){
		if (showDefault){
			//jl.setText("All rules: "+vrs.getRules().size()+"          Matching rules: "+rtm.getDefaultRulesCount()+"          Selected rules: "+rtm.getRowCount());
			StringBuffer str = new StringBuffer();
			str.append("<html><table><tr><td colspan=\"3\">All rules: "+vrs.getRules().size()+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			str.append("Matching rules: "+rtm.getDefaultRulesCount()+"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			str.append("Selected rules: "+rtm.getRowCount()+"</td></tr>");
			double sum = rtm.getAllDistrSum();
			if(sum > 0) {
				str.append("<tr><td>VOTING:</td><td></td><td></td></tr>");
				rseslib.structure.vector.Vector distr = rtm.getDistr();
				int d = 0;
				while(d < distr.dimension()) {
					double code = vrs.getHeader().nominalDecisionAttribute().globalValueCode(d);
					str.append("<tr><td>" + NominalAttribute.stringValue(code) + ": " + (int)distr.get(d) + " (" + Math.round(distr.get(d) * 100 / sum) + "%)&nbsp;&nbsp;</td><td>");
					d++;
					if(d < distr.dimension()) {
						code = vrs.getHeader().nominalDecisionAttribute().globalValueCode(d);
						str.append(NominalAttribute.stringValue(code) + ": " + (int)distr.get(d) + " (" + Math.round(distr.get(d) * 100 / sum) + "%)&nbsp;&nbsp;");
						d++;
					}
					str.append("</td><td>");
					if(d < distr.dimension()) {
						code = vrs.getHeader().nominalDecisionAttribute().globalValueCode(d);
						str.append(NominalAttribute.stringValue(code) + ": " + (int)distr.get(d) + " (" + Math.round(distr.get(d) * 100 / sum) + "%)&nbsp;&nbsp;");
						d++;
					}
					str.append("</td></tr>");
				}
			}
			str.append("</table></html>");
			jl.setText(str.toString());			
		}else
		jl.setText("All rules: "+vrs.getRules().size()+"          Selected rules: "+rtm.getRowCount());
	};
/**
 * Main constructor
 * @param jl label to be controlled
 * @param rtm model of table, we register as listener of it
 * @param vrs visual rough set from which we get all data
 * @param showDefault controls if label will be showing also additional number - how many rules are selected by the default selector
 */

	public AmountController(JLabel jl, RulesTableModel rtm,RoughSetRulesVisual vrs,boolean showDefault) {
	
		this.jl = jl;
		this.rtm = rtm;
		this.vrs = vrs;
		this.showDefault=showDefault;
		rtm.addTableModelListener(this);
		setLabelText();
	}


	public void tableChanged(TableModelEvent arg0) {
		setLabelText();
		
	}
	
}

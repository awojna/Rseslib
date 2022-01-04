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


package rseslib.processing.classification.tree.c45;

/**
 * @author Maciej Prochniak
 * @version 1.0
 */

import javax.swing.*;
import java.awt.*;
import rseslib.structure.attribute.*;
import rseslib.structure.vector.*;

import java.awt.event.*;
import javax.swing.border.*;

public class DecisionNodeInfo
    extends JDialog {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
DecisionTreeNode m_node;
  Header m_head;
  JPanel jPanel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JPanel jPanel2 = new JPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JLabel jLabel4 = new JLabel();
  JLabel[] values;
  JLabel[] attributes;
  TitledBorder titledBorder1;

  public DecisionNodeInfo(DecisionTreeNode node, Header hdr) {

    setTitle("Node info");
    m_head = hdr;
    m_node = node;
    try {
      this.jbInit();
      pack();
     }
    catch (Exception e) {
      e.printStackTrace();
    }

    if (node.isLeaf()) {
      jLabel1.setText("Leaf with decision:");
      Vector dec = node.m_DecisionVector;
      int best = 0;
      for (int d = 1; d < dec.dimension(); d++)
          if (dec.get(d) > dec.get(best)) best = d;

      jLabel2.setText(m_node.getDecisionLabel());
      jLabel3.setText("   ");
    }

    else {
      String val0 = node.m_BranchSelector.toString(0);
      int sep;
      if (val0.indexOf('<')>=0) sep = val0.indexOf("<")-1;
      else sep = val0.indexOf("=")-1;
      jLabel1.setText("Branching for "+node.m_BranchSelector.toString(0).substring(0, sep)+':');
      if (val0.indexOf('<')>=0)
      {
          sep++;
          jLabel2.setText(node.m_BranchSelector.toString(0).substring(sep)+"          "+node.m_BranchSelector.toString(1).substring(sep));
      }
      if (val0.indexOf('<')<0)
      {
    	  sep += 3;
    	  StringBuffer buf = new StringBuffer(node.m_BranchSelector.toString(0).substring(sep));
    	  for (int br = 1; br < node.m_BranchSelector.noOfValues(); br++)
    		  buf.append("  "+node.m_BranchSelector.toString(br).substring(sep));
    	  jLabel2.setText(buf.toString());
      }
      jLabel3.setText("   ");
    }
  }

  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(Color.white,2),"");
    this.getContentPane().setLayout(borderLayout1);
    this.setLocale(java.util.Locale.getDefault());
    jPanel1.setLayout(gridBagLayout1);
    jLabel1.setText("jLabel1");
    jLabel2.setText("jLabel2");
    jLabel3.setText("jLabel3");
    jPanel2.setLayout(gridBagLayout2);
    jLabel4.setText("Decision distribution in node:");
    jPanel2.setBorder(BorderFactory.createLineBorder(Color.black));
    jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 2, 2, 2), 0, 0));
    jPanel1.add(jLabel2, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 2, 2, 2), 0, 0));
    jPanel1.add(jLabel3, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 2, 2, 2), 0, 0));
    this.getContentPane().add(jPanel1, BorderLayout.NORTH);
    this.getContentPane().add(jPanel2, BorderLayout.CENTER);

    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        this_componentResized(e);
      }
    });

    GridBagConstraints gb = new GridBagConstraints();
    gb.weightx = 1.0;
    gb.weighty = 1.0;
    gb.gridx = 0;
    gb.gridy = 0;
    jPanel2.add(jLabel4, gb);

    NominalAttribute nom = m_node.m_DecisionAttribute;
    values = new JLabel[nom.noOfValues()];
    attributes = new JLabel[nom.noOfValues()];

    for (int i = 0; i < nom.noOfValues(); i++) {
      gb.insets = new Insets(2, 2, 2, 2);
      gb.ipadx = 2;
      gb.ipady = 2;
      gb.gridx = 0;
      gb.gridy = i + 1;
      int count = (int)m_node.getDecisionVector().get(i);
      if (count > 0) {
        attributes[i] = new JLabel(NominalAttribute.stringValue(nom.globalValueCode(i)));
        jPanel2.add(attributes[i], gb);
        gb.gridx = 1;
        gb.gridy = i + 1;
        values[i] = new JLabel(Integer.toString(count));
        jPanel2.add(values[i], gb);
      }
    }
  }

  public void this_componentResized(ComponentEvent e) {
    int h;
    int w;
    boolean res = false;
    if (getSize().height < getMinimumSize().height) {
      h = getMinimumSize().height;
      res = true;
    }
    else {
      h = getSize().height;
    }
    if (getSize().width < getMinimumSize().width) {
      w = getMinimumSize().width;
      res = true;
    }
    else {
      w = getSize().width;
    }

    if (res) {
      setSize(w, h);
    }
  }

}

/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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

import javax.swing.*;
import java.awt.*;


/**
 * @author Maciej Prochniak
 */

public class GraphNode {

  static Dimension dim = new Dimension(15, 15);
  static ImageIcon leaf =
      new ImageIcon(rseslib.processing.classification.tree.c45.C45Vis.class.getResource("leaf.gif"));
  static ImageIcon branch =
      new ImageIcon(rseslib.processing.classification.tree.c45.C45Vis.class.getResource("branch.gif"));

  /** holds handle to DecisionTreeNode in C45 classifier */
  DecisionTreeNode m_RealNode;
  /** children of this node if it's Leaf, then null */
  GraphNode[] m_Children = null;

  /** level on which the node is in the tree **/
  int m_depth;

  int position_x;

  int position_y;

  GraphNode leftBrother = null;
  GraphNode rightBrother = null;
  GraphNode parent = null;

  //NodeButton button;
  C45Vis m_clasif;

  boolean m_expanded;
  int m_path = 0;

  public GraphNode(DecisionTreeNode orgNode, int level,
                   C45Vis clas) {

    m_clasif = clas;
    m_RealNode = orgNode;
    m_depth = level;
    m_expanded = true;

    if (orgNode.isLeaf()) {
      return;
    }
    m_Children = new GraphNode[orgNode.noOfBranches()];

    for (int i = 0; i < orgNode.noOfBranches(); i++) {
      m_Children[i] = new GraphNode(orgNode.subnode(i), level + 1, clas);
      m_Children[i].parent = this;
    }
  }

  public void cut() {
    m_RealNode.cutTree();
    //m_Children[0].leftBrother.setRBrother(m_Children);
    //for(int i=0;i<m_Children.length;i++) m_Children[i].casate();
    //m_Children = null;
  }

  /**
   * Changes the decision in this node.
   * 
   * @param newDec New decision to be set.
   */
  public void changeDecision(double newDec)
  {
  	m_RealNode.changeDecision(newDec);
  }
  
  public String getDecisionLabel()
  {
	  return m_RealNode.getDecisionLabel();
  }
  
  public int noOfChildren() {
    if (isLeaf()) {
      return 0;
    }
    return m_Children.length;
  }

  public GraphNode[] getChildren() {
    return m_Children;
  }

  public GraphNode getLeftBro() {
    return leftBrother;
  }

  public GraphNode getRightBro() {
    return rightBrother;
  }

  public void setLBrother(GraphNode left) {
    if (left != null) {
      leftBrother = left;
    }
  }

  public void setRBrother(GraphNode right) {
    if (right != null) {
      rightBrother = right;
    }
  }

  public boolean isLeaf() {
    return (m_Children == null);
  }

  public void collapse() {
    m_expanded = false;
    //if (button != null) {
    //  button.getParent().remove(button);
    //}
    //button = null;
  }

  public void collapseAll() {
    collapse();
    for (int i = 0; i < noOfChildren(); i++) {
      m_Children[i].collapseAll();
    }

  }

  public void expand() {
    m_expanded = true;

  }

  public void expandAll() {
    expand();
    for (int i = 0; i < noOfChildren(); i++) {
      m_Children[i].expandAll();
    }
  }

  public int getDepth() {
    return m_depth;
  }

  public void setPath(int as) {
    m_path = as;
  }

  public boolean expanded() {
    return ( (parent == null) || parent.m_expanded);
  }

  public GraphNode getObject(int x, int y) {
    if ( (position_x-dim.width/2 <= x) && (position_y-dim.height/2 <= y) && (position_x + dim.width/2 >= x)
        && (position_y + dim.height/2 >= y)) {
      return this;
    }
    if (isLeaf()) {
      return null;
    }

    for (int i = 0; i < noOfChildren(); i++) {
      GraphNode no = m_Children[i].getObject(x, y);
      if (no != null) {
        return no;
      }
    }
    return null;
  }

  public void expCol() {
    if (m_expanded) collapseAll(); else expand();
    m_clasif.repaint();
  }

  public void expColAll() {
    if (m_expanded) {
      collapseAll();
    }
    else {
      expandAll();
    }
    m_clasif.repaint();
  }
  public void stopClas() {
    m_path = 0;
    for(int i =0;i<noOfChildren();i++) m_Children[i].stopClas();
  }

public void paint(Graphics g, int x, int y, boolean ifClas) {

	g.setColor(Color.BLACK);
	g.drawRect(x, y, dim.width, dim.height);

	position_x = x+(dim.width/2);
	position_y = y+(dim.height/2);
	if (parent != null) {
		if ((m_path == 1) && ifClas) {
			g.setColor(Color.GREEN);
		}
		if ((m_path == 2) && ifClas) {
			g.setColor(Color.red);
		}
		g.drawLine(position_x, position_y, parent.position_x, parent.position_y);
	}
	if (this.isLeaf())
		g.drawImage(leaf.getImage(), x, y, leaf.getImageObserver());
    else g.drawImage(branch.getImage(), x, y, Color.black, branch.getImageObserver());

	g.setColor(Color.black);
  }
}

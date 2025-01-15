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


package rseslib.processing.classification.tree.c45;

import rseslib.processing.classification.*;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.*;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.progress.Progress;
import rseslib.system.PropertyConfigurationException;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/**
 * <p>Description: Base class for visualising C45DecisionTree </p>
 * 
 * @author	Maciej Prochniak
 */

public class C45DecisionTreeVisual
    extends C45DecisionTree
    implements VisualClassifier {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

	// fields describing visualizations of classifier and classifying process
  private TreePainter m_painter = null;
  private TreePainter m_classifpainter;

  // copy of decision tree containing informations about visualization
  private GraphNode m_GraphRoot = null;

  // window containing information about selected node
  private DecisionNodeInfo m_info;

  // canvas, where both visualizations are displayed
  private JPanel canvas;
  private JPanel clas_canvas;

  // constructors coming from C45DecisionTree
  public C45DecisionTreeVisual(Properties prop, DoubleDataTable tab, Progress prog) throws
      InterruptedException, PropertyConfigurationException {
    super(prop, tab, prog);
  }

  /**
   * Writes this object.
   *
   * @param out			Output for writing.
   * @throws IOException	if an I/O error has occured.
   */
  private void writeObject(ObjectOutputStream out) throws IOException
  {
  }

  /**
   * Reads this object.
   *
   * @param out			Output for writing.
   * @throws IOException	if an I/O error has occured.
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
  }

  /**
   * repaint - method repaints all present components
   */

  public void repaint() {
    if (m_painter != null) {
      m_painter.repaint();
    }
    if (m_classifpainter != null) {
      m_classifpainter.repaint();
    }
  }

  /**
   * cut - method which stops visuialization of a branch, and repaints
   * both panels
   */
  public void cut() {
    makeGraphTree(m_GraphRoot.m_RealNode);
    if (m_painter != null) {
      m_painter.m_root = m_GraphRoot;
      m_painter.m_ActNode = m_GraphRoot;
    }
    if (m_classifpainter != null) {
      m_classifpainter.m_root = m_GraphRoot;
      m_classifpainter.m_ActNode = m_GraphRoot;
    }
    repaint();
  }

  /**
   * Constructs tree with visualization information from
   * original decision tree
   * @param orgNode DecisionTreeNode   root of original decision tree
   */
  private void makeGraphTree(DecisionTreeNode orgNode) {
    m_GraphRoot = new GraphNode(orgNode, 0, this);

    LinkedList<GraphNode> list_a = new LinkedList<GraphNode>();
    list_a.add(m_GraphRoot);
    LinkedList<GraphNode> list_b = new LinkedList<GraphNode>();
    GraphNode last = null;

    // iterating over levels of decision tree
    while (true) {
      if ( (list_a == null) || (list_a.isEmpty())) {
        break;
      }

      GraphNode act = (GraphNode) list_a.getFirst();
      if (last != null) {
        last.setRBrother(act);
      }
      act.setLBrother(last);

      list_b = new LinkedList<GraphNode>();
      for (int j = 0; j < act.noOfChildren(); j++) {
        list_b.add(act.getChildren()[j]);

      }
      ListIterator<GraphNode> it = list_a.listIterator();
      it.next();
      while (it.hasNext()) {
        GraphNode now = it.next();
        act.setRBrother(now);
        now.setLBrother(act);
        act = now;
        for (int j = 0; j < act.noOfChildren(); j++) {
          list_b.add(act.getChildren()[j]);
        }
      }
      last = act;
      list_a = list_b;
    }
    for (GraphNode a = m_GraphRoot; a != null; ) {
      a = a.getRightBro();

    }
    repaint();
  }

  /**
   * draws visualization of decision tree on given canvas
   * @param canv JPanel    canvas where tree is visualized
   */
  public void draw(JPanel canv) {
    if (canv.equals(canvas)) {
      return;
    }
    canvas = canv;
    if (m_GraphRoot == null) {
        makeGraphTree(m_Root);
    }
    m_painter = new TreePainter(m_GraphRoot, false, m_Header);
    JScrollPane scroll = new JScrollPane(m_painter);
    scroll.setVisible(true);
    canv.add(scroll);
  }

  /**
   * draws visualization of classification process, returning answer
   * @param canv JPanel         canva where classification is visualized
   * @param obj DoubleData      data being visualized
   */

  public void drawClassify(JPanel canv, DoubleData obj) {
    if (m_GraphRoot == null) {
        makeGraphTree(m_Root);
    }
    if (!canv.equals(clas_canvas)) {
      m_classifpainter = new TreePainter(m_GraphRoot, true, m_Header);
      JScrollPane scroll = new JScrollPane(m_classifpainter);
      scroll.setVisible(true);
      canv.add(scroll);
      clas_canvas = canv;
    }
    // stop classifying
    m_GraphRoot.stopClas();
    ArrayList<GraphNode> list = new ArrayList<GraphNode>();
    list.add(m_GraphRoot);
    //highlight classifying path
    for (; (!list.isEmpty()); ) {
      GraphNode act = (GraphNode) list.remove(0);
      DecisionTreeNode nod = act.m_RealNode;
      int val = 0;
      if (!nod.isLeaf()) val = nod.branch(obj);
      if (val < 0) {
        for (int i = 0; i < act.noOfChildren(); i++) {
          list.add(act.m_Children[i]);
        }
        act.setPath(2);
        continue;
      }
      act.setPath(1);
      if (nod.isLeaf()) {
        continue;
      }
      list.add(act.m_Children[val]);
    }

    m_classifpainter.validate();
  }

  /**
   * stops displaying current inforamtion abouyt selected node, replaces
   * with new window
   * @param nod DecisionNodeInfo
   */
  public void setInfoNode(DecisionNodeInfo nod, JComponent relatedTo) {
	Point p = null;
    if (m_info != null) {
      p = m_info.getLocation();
      m_info.dispose();
    }
    m_info = nod;
    if (m_info != null) {
      Dimension dlgSize = nod.getPreferredSize();
      if (p!=null) nod.setLocation(p);
      else nod.setLocationRelativeTo(relatedTo);
      m_info.this_componentResized(null);
      m_info.setVisible(true);
    }
  }

  /**
   * @author Maciej Prochniak
   */

  class TreePainter
      extends JComponent implements ActionListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	GraphNode m_root;
    GraphNode m_ActNode;
    JPanel m_canvas;
    boolean ifClas;
    Header m_hdr;

    JPopupMenu m_popmenu = new JPopupMenu();
    JMenuItem m_popCutTree = new JMenuItem("Cut...");
    JMenuItem m_expand1 = new JMenuItem("Expand/Collapse");
    JMenuItem m_stopClas = new JMenuItem("Stop classifing");
    JMenuItem m_changeDec = new JMenuItem("Change decision");

    int vertBeg = 10;
    int vertTab = 30;
    int sideTab = 10;
    int maxLev = 10;

    public TreePainter() {

    }

    public TreePainter(GraphNode mrr, boolean ifCl, Header hdr) {

      this.add(m_popmenu);

      ifClas = ifCl;
      m_ActNode = mrr;
      m_hdr = hdr;

      m_popmenu.add(m_popCutTree);
      m_popCutTree.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_ActNode.cut();
          cut();
        }
      });

      m_popmenu.add(m_expand1);
      m_expand1.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_ActNode.expCol();
        };
      });
      /* only when classifiable */
      if (ifClas) {
        m_popmenu.add(m_stopClas);
      }
      m_stopClas.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_root.stopClas();
          m_root.m_clasif.m_classifpainter.repaint();
        }
      });

      m_popmenu.add(m_changeDec);
      m_changeDec.addActionListener(this);

      m_popmenu.setInvoker(this);
      this.addMouseListener(new ClassiVisMouseAdapter(this));

      setDoubleBuffered(true);
      setOpaque(true);
      this.setVisible(true);
      this.setSize(300, 300);
      m_root = mrr;
    }

    public void actionPerformed(ActionEvent e) {
    	NominalAttribute decAttr = m_hdr.nominalDecisionAttribute();
    	Object[] possibilities = new Object[decAttr.noOfValues()];
    	for (int v = 0; v < possibilities.length; v++)
    		possibilities[v] = NominalAttribute.stringValue(decAttr.globalValueCode(v));
        String s = (String)JOptionPane.showInputDialog(m_canvas, "Select the decision:", "",
        		JOptionPane.PLAIN_MESSAGE, null, possibilities, m_ActNode.getDecisionLabel());
        if (s!=null && s.length()>0)
        	m_ActNode.changeDecision(decAttr.globalValueCode(s));
      }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());
      if (m_root != null) {
        drawWhole(g);
      }
    }

    public Dimension getPreferredSize() {
      return getSize();
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    private void drawWhole(Graphics g) {
      int i = 0;
      for (GraphNode nod = m_root; i < maxLev; i++) {
        nod = drawLevel(i, nod, vertBeg + (i * vertTab), g);
        if (nod == null) {
          return;
        }
      }
    }

    /**
     * draws one level of a tree
     *
     * @param level int              depth
     * @param startNode GraphNode    the first node on linked list of nodes
     * @param height int             distance beetween levels
     * @param g Graphics             where to draw
     * @return GraphNode             first node of next level
     */

    private GraphNode drawLevel(int level, GraphNode startNode, int height,
                                Graphics g) {

      int i = 0;
      int midTab;

      for (GraphNode a = startNode; ; ) {
        if (a == null) {
          break;
        }
        if (a.expanded()) {
            i++;
          }
        a = a.getRightBro();
        if ( (a == null) || (a.getDepth() > startNode.getDepth())) {
          break;
        }
      }
      midTab = (int) (this.getSize().getWidth() - (2 * sideTab)) / (i + 1);

      GraphNode a = startNode;
      for (i = 1; ; ) {
        if (a.expanded()) {
          a.paint(g, sideTab + (i * midTab), height, ifClas);
          i++;
        }
        if (a.getRightBro() == null) {
          a = null;
          break;
        }
        a = a.getRightBro();
        if (a.getDepth() > level) {
          break;
        }

      }
      return a;
    }

    public void m_ClassifierMousePressed(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();

      if (m_root == null) {
        return;
      }

      GraphNode nod = m_root.getObject(x, y);
      if (nod == null) {
        return;
      }
      if (e.isPopupTrigger()) {
        m_ActNode = nod;
        m_popmenu.show(this, x, y);
      }
      else if (e.getButton()==MouseEvent.BUTTON1)
      {
    	  if (e.getClickCount() == 1) {
    		  //if (!m_ActNode.equals(nod))  {
    		  m_ActNode = nod;
    		  setInfoNode(new DecisionNodeInfo(nod.m_RealNode, m_Header), this);
    		  //}
    	  }
    	  else {
    		  nod.expColAll();
    	  }
      }
    }

    public void m_ClassifierMouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (m_root == null) {
          return;
        }

        GraphNode nod = m_root.getObject(x, y);
        if (nod == null) {
          return;
        }
        if (e.isPopupTrigger()) {
          m_ActNode = nod;
          if (m_ActNode.isLeaf()) m_changeDec.setEnabled(true);
          else m_changeDec.setEnabled(false);
          m_popmenu.show(this, x, y);
        }
      }
}

  class ClassiVisMouseAdapter
      extends MouseAdapter {
    private TreePainter what;

    public ClassiVisMouseAdapter(TreePainter wh) {
      what = wh;
    }

    public void mousePressed(MouseEvent e) {
      what.m_ClassifierMousePressed(e);
    }

    public void mouseReleased(MouseEvent e) {
        what.m_ClassifierMouseReleased(e);
      }
}

}

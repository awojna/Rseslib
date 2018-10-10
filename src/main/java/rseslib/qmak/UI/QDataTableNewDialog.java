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


package rseslib.qmak.UI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import rseslib.qmak.dataprocess.table.QArrayHeaderTableModel;
import rseslib.qmak.QmakMain;
import rseslib.qmak.dataprocess.table.*;
import rseslib.structure.attribute.Attribute;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import rseslib.structure.attribute.formats.*;

import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JButton;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class QDataTableNewDialog
    extends JDialog
{
  public QDataTableNewDialog()
  {
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public QDataTableNewDialog(Frame frame)
  {
    super(frame);

    try {
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      jbInit();
      pack();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    m_name = null;
    TableColumnModel cm = jTable1.getColumnModel();
    TableColumn col;
    col = cm.getColumn(0);
    col.setResizable(false);
    col.setPreferredWidth(30);
    col.setMaxWidth(30);
    col.setMinWidth(30);
    col = cm.getColumn(1);
    col.setMinWidth(100);
    col.setPreferredWidth(100);
    col = cm.getColumn(2);
    col.setCellEditor(new DefaultCellEditor(m_TypeCombo));
    col.setPreferredWidth(80);
    col.setMaxWidth(80);
    col.setMinWidth(80);
    col = cm.getColumn(3);
    col.setCellEditor(new DefaultCellEditor(m_DomainCombo));
    col.setPreferredWidth(80);
    col.setMaxWidth(80);
    col.setMinWidth(80);
  }

  private void jbInit() throws Exception
  {
    this.getContentPane().setLayout(borderLayout2);
    GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
    gridBagConstraints41.gridx = 0;
    gridBagConstraints41.gridy = 2;
    jLabel32 = new JLabel();
    jLabel32.setText(QmakMain.getMainFrame().messages.getString("QDTNDDecisionColumnName"));
    GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
    gridBagConstraints31.fill = GridBagConstraints.BOTH;
    gridBagConstraints31.gridy = 2;
    gridBagConstraints31.weightx = 1.0;
    gridBagConstraints31.insets = new Insets(5, 5, 5, 5);
    gridBagConstraints31.gridx = 1;
    GridBagConstraints gridBagConstraints5 = new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);
    gridBagConstraints5.gridy = 3;
    GridBagConstraints gridBagConstraints4 = new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);
    gridBagConstraints4.gridx = 1;
    gridBagConstraints4.gridy = 6;
    GridBagConstraints gridBagConstraints3 = new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
    gridBagConstraints3.gridx = 1;
    gridBagConstraints3.gridy = 1;
    GridBagConstraints gridBagConstraints2 = new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
    gridBagConstraints2.gridx = 1;
    gridBagConstraints2.gridy = 0;
    GridBagConstraints gridBagConstraints1 = new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0);
    gridBagConstraints1.gridx = 0;
    gridBagConstraints1.gridwidth = 2;
    gridBagConstraints1.gridy = 4;
    jPanel2.setSize(new Dimension(526, 256));
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridy = 5;
    jLabel31 = new JLabel();
    jLabel31.setText("");
    jPanel2.setLayout(gridBagLayout1);
    this.setModal(true);
    this.setTitle("");
    this.addComponentListener(new ComponentAdapter()
    {
      public void componentResized(ComponentEvent e)
      {
        this_componentResized(e);
      }
    });
    jPanel1.setLayout(flowLayout1);
    buttonCreate.setText(QmakMain.getMainFrame().messages.getString("QDTNDCreate"));
    buttonCreate.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        buttonCreate_actionPerformed(e);
      }
    });
    buttonCancel.setText(QmakMain.getMainFrame().messages.getString("jCancel"));
    buttonCancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        buttonCancel_actionPerformed(e);
      }
    });

    jLabel1.setText(QmakMain.getMainFrame().messages.getString("QDTNDTableName"));
    jTable1.setBorder(BorderFactory.createLineBorder(Color.black));
    jPanel2.setBorder(border);
    jTable1.setModel(tableModel);
    jTable1.setSelectionBackground(Color.lightGray);
    jTable1.setSelectionForeground(Color.black);
    jLabel2.setText(QmakMain.getMainFrame().messages.getString("QDTNDAttributes"));
    jScrollPane1.setBorder(BorderFactory.createLoweredBevelBorder());
    jScrollPane1.setMinimumSize(new Dimension(300, 100));
    jScrollPane1.setPreferredSize(new Dimension(300, 100));
    m_buttonUsunWiersze.setMaximumSize(new Dimension(25, 25));
    m_buttonUsunWiersze.setMinimumSize(new Dimension(25, 25));
    m_buttonUsunWiersze.setPreferredSize(new Dimension(25, 25));
    /*m_buttonUsunWiersze.setIcon(new ImageIcon(QDataTableNewDialog.class.
        getResource("Delete16.gif")));*/
    m_buttonUsunWiersze.setText("");
    m_buttonUsunWiersze.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        m_buttonUsunWiersze_actionPerformed(e);
      }
    });
    TextFieldMissingValue.setText("---");
    jLabel3.setText(QmakMain.getMainFrame().messages.getString("QDTNDMissingValue"));
    jPanel1.add(buttonCreate);
    jPanel1.add(buttonCancel);
    jPanel2.add(TextFieldDataTableName, gridBagConstraints2);
    jPanel2.add(TextFieldMissingValue, gridBagConstraints3);
    jPanel2.add(m_buttonUsunWiersze, gridBagConstraints4);
    jPanel2.add(jScrollPane1, gridBagConstraints1);
    jPanel2.add(jLabel2, gridBagConstraints5);
    this.getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);
    this.getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
    jScrollPane1.getViewport().add(jTable1);
    jPanel2.add(jLabel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5,
        5), 0, 0));
    jPanel2.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5,
        5), 0, 0));
    jPanel2.add(jLabel31, gridBagConstraints);
    jPanel2.add(getTextFieldDecisionColumnName(), gridBagConstraints31);
    jPanel2.add(jLabel32, gridBagConstraints41);
    jTable1.setDefaultRenderer(String.class, new HeaderTableRenderer());
    jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

  }

  private HeaderReader m_headerReader;  //  @jve:decl-index=0:
  private String m_name;

  Border border = BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(5, 5, 5, 5),
      BorderFactory.createEtchedBorder());
  BorderLayout borderLayout2 = new BorderLayout();  //  @jve:decl-index=0:
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();  //  @jve:decl-index=0:visual-constraint="21,144"
  FlowLayout flowLayout1 = new FlowLayout();
  JButton buttonCreate = new JButton();
  JButton buttonCancel = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel jLabel1 = new JLabel();
  JTextField TextFieldDataTableName = new JTextField();
  QArrayHeaderTableModel tableModel = new QArrayHeaderTableModel();
  JScrollPane jScrollPane1 = new JScrollPane();

  JTable jTable1 = new JTable(3, 3);
  String[] m_AttrTypes = {
      Attribute.Type.conditional.name(),
//      Attribute.ATTR_TYPENAME_DECISION,
      Attribute.Type.text.name()};
  String[] m_AttrDomains = {
      Attribute.ValueSet.nominal.name(),
      Attribute.ValueSet.nonapplicable.name(),
      Attribute.ValueSet.numeric.name()};
  JComboBox m_TypeCombo = new JComboBox(m_AttrTypes);
  JComboBox m_DomainCombo = new JComboBox(m_AttrDomains);
  JLabel jLabel2 = new JLabel();
  JButton m_buttonUsunWiersze = new JButton();
  JTextField TextFieldMissingValue = new JTextField();
  JLabel jLabel3 = new JLabel();
private JLabel jLabel31 = null;
private JTextField TextFieldDecisionColumnName = null;
private JLabel jLabel32 = null;
public void m_buttonUsunWiersze_actionPerformed(ActionEvent e)
  {
    ( (QArrayHeaderTableModel) (jTable1.getModel())).deleteSelected();
  }

  public void this_componentResized(ComponentEvent e)
  {
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

  public void buttonCancel_actionPerformed(ActionEvent e)
  {	  
    m_headerReader = null;
    dispose();
  }

  public void buttonCreate_actionPerformed(ActionEvent e)
  {
    QArrayHeaderTableModel htm = (QArrayHeaderTableModel) (jTable1.getModel());
    htm.setValueAt(Attribute.ValueSet.nominal.name(), htm.getRowCount()-1, 3);
    htm.setValueAt(Attribute.Type.decision.name(), htm.getRowCount()-1, 2);
    htm.setValueAt(TextFieldDecisionColumnName.getText(), htm.getRowCount()-1, 1);
       
    if ( (htm.getRowCount() >= 2) &&
      (TextFieldMissingValue.getText().compareTo("") != 0)) {
      htm.initHeaderReader(TextFieldMissingValue.getText());
      m_name = TextFieldDataTableName.getText();
      if (m_name.compareTo("") == 0) {
    	  m_name = QmakMain.getMainFrame().getProject().CreateUniqeName(QmakMain.getMainFrame().messages.getString("Table"), false);
      }
      m_headerReader = htm;

	  /*
      if (null != m_headerReader) {
          Header hdr;
          try {
            hdr = new ArrayHeader(m_headerReader);
            if (!QmakMain.getMainFrame().isCorrectHeader(hdr)) {
                JOptionPane.showMessageDialog(this,
                        "There must be one decision attribute!!!", "Bad header", JOptionPane.NO_OPTION);
                return;
            }
 
          }
          catch (IOException ex) {
            return;
          }
        } else return;
    	   */
      
      dispose();
    }
    else {
       JOptionPane.showMessageDialog(this,
    		   QmakMain.getMainFrame().messages.getString("ErrAttributeMustBeSpecified"),
    		   QmakMain.getMainFrame().messages.getString("ErrIncompleteData"), JOptionPane.OK_OPTION);
    }
  }

  public String getDataTableName()
  {
    return m_name;
  }

  public HeaderReader getHeaderReader()
  {
     return m_headerReader;
  }

  class HeaderTableRenderer
      extends DefaultTableCellRenderer
  {
    private Color unselectedBackground = Color.WHITE;
    private Color selectedBackground = Color.LIGHT_GRAY;
    private Color focusedBackground = new Color(10, 36, 106);
    private Border unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2,
        5, unselectedBackground);
    private Border selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
        selectedBackground);
    private Border focusedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
        focusedBackground);

    public HeaderTableRenderer()
    {
      super();
      setOpaque(true);
    }

    public Component getTableCellRendererComponent(
        JTable table, Object obj,
        boolean isSelected, boolean hasFocus,
        int row, int column)
    {
      super.getTableCellRendererComponent(table, obj, isSelected, hasFocus,
          row, column);

      if (isSelected) {
        if (hasFocus) {
          setBorder(focusedBorder);
        }
        else {
          setBorder(selectedBorder);
        }
      }
      else {
        setBorder(unselectedBorder);
      }
      return this;
    }
  }

/**
 * This method initializes TextFieldDecisionColumnName	
 * 	
 * @return javax.swing.JTextField	
 */
private JTextField getTextFieldDecisionColumnName() {
	if (TextFieldDecisionColumnName == null) {
		TextFieldDecisionColumnName = new JTextField();
		TextFieldDecisionColumnName.setText(QmakMain.getMainFrame().messages.getString("QDTNDDecision"));
	}
	return TextFieldDecisionColumnName;
}
}

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


package rseslib.qmak.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import rseslib.qmak.UI.QTryb;
import rseslib.qmak.QmakMain;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.util.Utils;
import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataObject;
import rseslib.structure.table.DoubleDataTable;

/**
 * Klasa sluzy do wizualizacji tabelki
 * @author Krzysztof Mroczek - Klasa inspirowana analogiczna klasa z programu Trickster
 * @version 1.0
 */

enum QTryb {
	tabelkOnly,
	wizualizacja
}

public class QTableView
    extends JScrollPane
{
  private iQTables owner;
  private JPopupMenu tablePopupMenu = new JPopupMenu();
  private JMenuItem tablePopupMenuClassifyRow = null;
  private JMenuItem tablePopupMenuSortRow = null;
  private JMenuItem tablePopupMenuRemoveRow = null;
  private JMenuItem tablePopupMenuAddRow = null;
   private JMenu tablePopupMenuClassifyWith = null;
  private JMenuItem tablePopupMenuSaveTable = null;
  private JTable jTable1 = new JTable(); 
//  private iQDataTable DataTable;
//  private QTryb tryb;
  
  public QTableView(iQDataTable DT,QTryb t,iQTables wlasciciel) {
	    try {
//	      DataTable = DT;
//	      tryb = t;
	      owner = wlasciciel;
	      jbInit();
	    }
	    catch (Exception exception) {
	      exception.printStackTrace();
	    }
	  }
  
  private ActionListener menuListener = new ActionListener()
  {
    public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand() == "ClasifyRow") {
    	  if (QMainFrame.getMainFrame().jMainWindow.SelectedClassifier != null) {
    	  classifyRow(jTable1.getSelectedRow(),QMainFrame.getMainFrame().jMainWindow.SelectedClassifier.getElem(), true);
    	  }
      }
      if (e.getActionCommand() == "SortByCol") {
    	  if (jTable1.getSelectedColumn() == -1)
    		  JOptionPane.showConfirmDialog(QMainFrame.qmainframe, QMainFrame.qmainframe.messages.getString("QTVChooseColumn"));
    	  owner.sort(jTable1.getSelectedColumn());
      }
      if (e.getActionCommand() == "Remove") {
        int[] selRows = jTable1.getSelectedRows();
        for (int i = selRows.length - 1; i > -1; i--) {
          ( (iQDataTable) jTable1.getModel())
              .remove( ( (iQDataTable) jTable1.getModel())
              .getRow(selRows[i]));
        }
        ( (AbstractTableModel) jTable1.getModel())
            .fireTableDataChanged();

      }
      
      if (e.getActionCommand() == "Add") {
        try { // sprawdzenie czy to poprawny iQDataTable
        	DoubleDataObject dd = new DoubleDataObject( ( (iQDataTable) jTable1
                    .getModel()).attributes());
        	for (int i = 0; i < dd.attributes().noOfAttr(); i++) {
        		dd.set(i, Double.NaN);
        	}
          ( (iQDataTable) jTable1.getModel())
              .add(dd);
        }
        catch (Exception f) {
          return;
        }
        ( (AbstractTableModel) jTable1.getModel())
            .fireTableDataChanged();
        jTable1.scrollRectToVisible(new Rectangle(jTable1.getWidth(),
            jTable1.getHeight(), 100, 100)); // nie jestem pewien
        jTable1.editCellAt(jTable1.getRowCount() - 1, 0,
            new EventObject(jTable1.getValueAt(jTable1
            .getRowCount() - 1, 0)));
      }
      if (e.getActionCommand() == "Save") {
        try {
        	iQDataTable DT = owner.getTable();
        	
    		String HeaderName = lounchFileChooserForDataTable(
    				QMainFrame.qmainframe.messages.getString("InfoSelectFileWithHeader"));
    		if (HeaderName != null) {
    			DT.setFileName(HeaderName);
    			DT.save();
    		}
        }
        catch (Exception exc) {
        }
      }
    }
  };
  
	private boolean lounchFileChooser(JFileChooser chos) {
		int rval;
		rval = chos.showSaveDialog(this);
		if (rval == JFileChooser.APPROVE_OPTION) {
			return true;
		}
		return false;
	}

	private String lounchFileChooserForDataTable(String title) {
		JFileChooser chos = new JFileChooser(".");
		chos.setDialogTitle(title);
		chos.setFileFilter(Utils.getFileFilterQDT());
		String name = null;
		if (lounchFileChooser(chos)) {
			File file = chos.getSelectedFile();
			name = chos.getSelectedFile().getAbsolutePath();
			if (Utils.getExtension(file).equals("")) {
				name = name + "." + Utils.qdt;
			}
		}
		return name;
	}

   
  private void jbInit() throws Exception {
	  tablePopupMenuClassifyRow = new JMenuItem(QMainFrame.qmainframe.messages.getString("QTVClassifyRow"));
	  tablePopupMenuSortRow = new JMenuItem(QMainFrame.qmainframe.messages.getString("QTVSortByThisColumn"));
	  tablePopupMenuRemoveRow = new JMenuItem(QMainFrame.qmainframe.messages.getString("QTVRemoveRows"));
	  tablePopupMenuAddRow = new JMenuItem(QMainFrame.qmainframe.messages.getString("QTVAddRow"));
	  tablePopupMenuClassifyWith = new JMenu(QMainFrame.qmainframe.messages.getString("QTVClassifyRowWith"));
	  tablePopupMenuSaveTable = new JMenuItem(QMainFrame.qmainframe.messages.getString("QTVSaveTable"));

	  jTable1.setBorder(BorderFactory.createLineBorder(Color.black));
    this.getViewport().add(jTable1, null);
    FocusListener f = new FocusListener()
    {
      public void focusGained(FocusEvent e) {
        setCellEditors(jTable1);
      }

      public void focusLost(FocusEvent e) {
      }
    };
    jTable1.addFocusListener(f);
    tablePopupMenuClassifyRow.setActionCommand("ClasifyRow");
    tablePopupMenuSortRow.setActionCommand("SortByCol");
    tablePopupMenuRemoveRow.setActionCommand("Remove");
    tablePopupMenuAddRow.setActionCommand("Add");
    tablePopupMenuSaveTable.setActionCommand("Save");
    
    tablePopupMenuClassifyRow.addActionListener(menuListener);
    tablePopupMenuSortRow.addActionListener(menuListener);
    tablePopupMenuRemoveRow.addActionListener(menuListener);
    tablePopupMenuAddRow.addActionListener(menuListener);
    tablePopupMenuSaveTable.addActionListener(menuListener);
    tablePopupMenu.add(tablePopupMenuClassifyRow);
	tablePopupMenu.add(tablePopupMenuSortRow);
	
	if (owner != null && (!owner.getTable().isClassified())) {
		tablePopupMenu.add(tablePopupMenuRemoveRow);
		tablePopupMenu.add(tablePopupMenuAddRow);
		tablePopupMenu.add(tablePopupMenuSaveTable);
	}
	
	tablePopupMenuClassifyWith.removeAll();
	boolean jestKlasyfikator = false;
	JMenuItem ElListy;
	for (QIcon qi : QmakMain.getMainFrame().jMainWindow.elements) {
		if (qi.getElem().isClassifier()) {
			ElListy = new JMenuItem(qi.getElem().getName());
			ElListy.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	iQProjectElement el;
		        	el = QmakMain.getMainFrame().getProject().getElement(((JMenuItem)e.getSource()).getText());
		        	classifyRow(jTable1.getSelectedRow(),el, true);
		        };
		    });
			tablePopupMenuClassifyWith.add(ElListy);
			jestKlasyfikator = true;
		}
	}

	if (jestKlasyfikator) {
		tablePopupMenu.add(tablePopupMenuClassifyWith);
		tablePopupMenuClassifyWith.setText(QmakMain.getMainFrame().messages.getString("QTVClassifyRowWith"));
	} else {
		tablePopupMenu.remove(tablePopupMenuClassifyWith);
	}
	
	
	
	jTable1.addMouseListener(new PopupListener(tablePopupMenu));
    addMouseListener(new PopupListener(tablePopupMenu));
    jTable1
        .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    jTable1.setCellSelectionEnabled(true);
    jTable1.getTableHeader().setReorderingAllowed(false);
    jTable1.getTableHeader().setDefaultRenderer(
        new TDataTableHeaderRenderer());
    jTable1.setDefaultRenderer(Object.class, new QDataTableCellRenderer());
    setCellEditors(jTable1);
  }
  
  public void classifyRow(int i,iQProjectElement klas, boolean withVisual) {
    if (i < 0 || i >= jTable1.getRowCount()) {
      return;
    }
	if (QMainFrame.getMainFrame().jMainWindow.SelectedClassifier == null) {
		JOptionPane.showMessageDialog(this, QMainFrame.qmainframe.messages.getString("QTVChoseClassifier"));
		return;
	}
    try {
    	if (withVisual && (((iQClassifier)klas).getClassifier() instanceof rseslib.processing.classification.VisualClassifier)) {
    		QIcon klasIcon = null;
			Iterator<QIcon> it = QMainFrame.getMainFrame().jMainWindow.elements.iterator();
			while (it.hasNext() && klasIcon == null) {
				QIcon icon = it.next(); 
				if (icon.getElem() == klas)
					klasIcon = icon;
			}
			if (klasIcon == null)
				JOptionPane.showMessageDialog(this, QMainFrame.getMainFrame().messages.getString("QPVclassifierNotFound"));
			else {
	    		QVisClassifierView visDialog = klasIcon.oknoKlas;
	    		if(visDialog == null) {
	    			visDialog = new QVisClassifierView((iQClassifier)klas);
	    			visDialog.setLocationRelativeTo(QMainFrame.getMainFrame());
	    			klasIcon.oknoKlas = visDialog;
	    		}
	   			visDialog.classifyOne(((iQDataTable) jTable1.getModel()).getRow(i));
	    		visDialog.addComment(((iQDataTable)jTable1.getModel()).NameOfResult( 
	        	    	((iQClassifier) klas).classify(( (iQDataTable) jTable1.getModel()).getRow(i))
	        	          ));
	    		visDialog.pack();
	    		visDialog.setVisible(true);	
			}
    	} else {
        	JOptionPane.showMessageDialog(this, String.format(QMainFrame.getMainFrame().messages.getString("QTVcomment") + ": %s",
        	    	((iQDataTable)jTable1.getModel()).NameOfResult( 
        	    	((iQClassifier) klas).classify(( (iQDataTable) jTable1.getModel()).getRow(i))
        	          )));   		
    	}
    }
    catch (java.lang.ArrayIndexOutOfBoundsException e) {
    	JOptionPane.showMessageDialog(this, QMainFrame.qmainframe.messages.getString("ErrClassifierNotCompatibleToTable"));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  class PopupListener
      extends MouseAdapter
  {
    JPopupMenu popup;

    MouseEvent event;

    PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        event = e;
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  class QDataTableCellRenderer
      extends DefaultTableCellRenderer
  {
    boolean isBordered = true;

    private final Color wellClassifiedColor = new Color(255, 255, 255);

    private final Color badlyClassifiedColor = new Color(250, 50, 50);

    public Component getTableCellRendererComponent(JTable table,
        Object obj, boolean isSelected, boolean hasFocus, int row,
        int column) {
      Component def = super.getTableCellRendererComponent(table, obj,
          isSelected, hasFocus, row, column);
      setHorizontalAlignment(SwingConstants.RIGHT);
      Color color;
      if ( ( (iQDataTable) table.getModel()).isBadlyClassified(row)) {
        color = badlyClassifiedColor;
      }
      else {
        color = wellClassifiedColor;
      }
      super.setForeground(new Color(0, 0, 0));
      super.setBackground(color);
      // super.setForeground(table.getSelectionForeground());
      if (isSelected) {
        super.setForeground(color);
        super.setBackground(table.getSelectionBackground());
      }
      iQDataTable tTable = (iQDataTable) table.getModel();
	  String s = tTable.attributes().missing();
	  if (s == null) s = "?";
      if (Double.isNaN(((Double)obj).doubleValue())) {
        ( (JLabel) def).setText(s);
      }
      else {
        if (table.getModel().getColumnName(column).compareTo(
        		QMainFrame.qmainframe.messages.getString("QTVClassificationResult")) == 0) {
              ((JLabel) def).setText(NominalAttribute.stringValue(
              ( (Double) obj).doubleValue()));
        }
        else {
          if (tTable.attributes().attribute(column).isNominal() || tTable.attributes().attribute(column).isText())
          {
            try {
            	if (obj.toString().compareTo("") == 0) {
            		((JLabel)def).setText(s);
            	} else {
            		((JLabel) def).setText( NominalAttribute
            				.stringValue( ( (Double) obj).doubleValue()));
            	}
            } catch (Exception e) { // na wypadek gdyby takiej wartosci nie bylo w tabeli
            	QmakMain.Log.debug("jakas dziwna wartosc "+obj.getClass().toString()+" "+obj);

              ( (JLabel) def).setText(s);
            }
          }
        }
      }
      improveEditor(column);
      return def;
    }

    private void improveEditor(int column) {

    }
  }

  class TDataTableHeaderRenderer
      extends DefaultTableCellRenderer
  {
    boolean isBordered = true;

    Color decisionAttributeColor = new Color(100, 255, 100);

    public Component getTableCellRendererComponent(JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row,
        int column) {
      if (table != null) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
          setForeground(header.getForeground());
          setBackground(header.getBackground());
          setFont(header.getFont());
          setHorizontalAlignment(getHorizontalAlignment());
        }
      }
      setFont(getFont().deriveFont(Font.BOLD));
      setHorizontalAlignment(SwingConstants.CENTER);
      if (table.getModel().getColumnName(column) == null) {
    	  setText("");
      } else {
    	  setText(table.getModel().getColumnName(column));
      }
      if (table.getModel().getColumnName(column).compareTo(
    		  QMainFrame.qmainframe.messages.getString("QTVClassificationResult")) == 0) {
        this.setBackground(decisionAttributeColor);
        return this;
      }
      
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      if ( ( (DoubleDataTable) table.getModel()).attributes().attribute(
          column).isDecision()) {
        this.setBackground(decisionAttributeColor);
      }
      return this;
    }
  }

  class TDataTableCellEditor extends DefaultCellEditor {

	public TDataTableCellEditor(JTextField arg0) {
		super(arg0);
	}

	public Component getTableCellEditorComponent(JTable table, Object obj, boolean arg2, int row, int col) {
		Component ret = super.getTableCellEditorComponent(table, obj, arg2, row, col);
		String s = ((iQDataTable)table.getModel()).attributes().missing();
		if (s == null) s = "?";
		if (((JTextField)ret).getText().compareTo("NaN") == 0) {
			((JTextField)ret).setText(s);
		}
		return ret;
	}
  }

  public JComboBox getPossibleNominalValues(int attrNo) {
    iQDataTable dataTable = (iQDataTable) jTable1.getModel();
    Attribute a = dataTable.attributes().attribute(attrNo);
    if (!a.isNominal()) {
      return null;
    }
    JComboBox combo = null;
    boolean exc = false;
    try {
      combo = (JComboBox) jTable1.getColumnModel().getColumn(attrNo)
          .getCellEditor();
    }
    catch (Exception e) {
      exc = true;
    }
    if (combo == null || exc) {
        combo = new JComboBox();
        combo.setEditor(new TDataTableComboBoxEditor(jTable1, attrNo));
        combo.setEditable(true);
    }
    // combo.setModel(new DefaultComboBoxModel());
	for (DoubleData data : dataTable.getDataObjects()) {
      int exist = 0;
      for (int i = 0; i < combo.getItemCount(); i++) {
        if (Double.isNaN(data.get(attrNo))
            || NominalAttribute.stringValue(data.get(attrNo))
            .compareTo( (String) combo.getItemAt(i)) == 0) {
          exist = 1;
        }
      }
      if (exist == 0) {
    	  if (!Double.isNaN(data.get(attrNo))) {
    		  ((TDataTableComboBoxEditor) combo.getEditor())
    		  			.setAddingItem(true);
    		  combo.addItem(NominalAttribute.stringValue(data
    				    .get(attrNo)));
    		  ((TDataTableComboBoxEditor) combo.getEditor())
    		  			.setAddingItem(false);
    	  }
      }
    }
    // ((TDataTableComboBoxEditor) combo.getEditor()).setTable(jTable1);
    return combo;
  }

  public void setCellEditors(JTable table) {
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (table.getColumnName(i).compareTo(QMainFrame.qmainframe.messages.getString("QTVClassificationResult")) != 0) {
          if  (((DoubleDataTable)table.getModel()).attributes()
          .attribute(i).isNominal()) {
        	  table.getColumnModel().getColumn(i).setCellEditor(
        			  new DefaultCellEditor(getPossibleNominalValues(i)));
          } else {
        	  table.getColumnModel().getColumn(i).setCellEditor(
        			  new TDataTableCellEditor(new JTextField()));
          }
      }
    }
  }

  class TDataTableComboBoxEditor
      extends BasicComboBoxEditor
  {
    protected int attrNo;

    protected boolean addingItem = false, settingText = false;

    public TDataTableComboBoxEditor(JTable table, int attr) {
      super();
      this.attrNo = attr;
      this.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          setCellEditors(jTable1);
        }
      });
    }

    public Object getItem() {
      iQDataTable data = (iQDataTable) jTable1.getModel();
		String s = data.attributes().missing();
		if (s == null) s = "?";

      if (editor.getText().compareTo(s) == 0) {
    	  return new Double(Double.NaN);
      }
      if (editor.getText().compareTo("") == 0) {
    	  return new Double(Double.NaN);
      }
      return new Double( ( (NominalAttribute) data.attributes().attribute(
          attrNo)).globalValueCode(editor.getText()));
    }

    public void setItem(Object anObject) {
      super.setItem(anObject);
      if (anObject != null) {
        if (!addingItem) {
          settingText = true;
          try {
            iQDataTable data = (iQDataTable) jTable1.getModel();
    		String s = data.attributes().missing();
    		if (s == null) s = "?";

            if (!((Double)anObject).isNaN()) {
            	editor
                	.setText( NominalAttribute
                			.stringValue( ( (Double) anObject)
                					.doubleValue()));
            } else {
            	editor.setText(s);
            }
          }
          catch (Exception e) { /*
             * for catching ClassCastException
             */
           }
          settingText = false;
        }
      }
    }

    public void setAddingItem(boolean bool) {
      addingItem = bool;
    }
  }

  public void invalidateProject() {
    iQDataTable tab = owner.getTable();
    
    if (jTable1.getModel() == tab) {
      return;
    }
    if (tab == null) {
      jTable1.setModel(new DefaultTableModel());
    }
    else {
      jTable1.setModel(tab);
    }
    jTable1.setDefaultRenderer(Object.class, new QDataTableCellRenderer());
    setCellEditors(jTable1);
  }
  
  private void SwitchPopupMenu(){
	  if (owner.getTable().isClassified()) {
			if (tablePopupMenu.isAncestorOf(tablePopupMenuRemoveRow)) tablePopupMenu.remove(tablePopupMenuRemoveRow);
			if (tablePopupMenu.isAncestorOf(tablePopupMenuAddRow)) tablePopupMenu.remove(tablePopupMenuAddRow);
			if (tablePopupMenu.isAncestorOf(tablePopupMenuSaveTable)) tablePopupMenu.remove(tablePopupMenuSaveTable);
	  } else {
			if (!tablePopupMenu.isAncestorOf(tablePopupMenuRemoveRow)) tablePopupMenu.add(tablePopupMenuRemoveRow);
			if (!tablePopupMenu.isAncestorOf(tablePopupMenuAddRow)) tablePopupMenu.add(tablePopupMenuAddRow);
			if (!tablePopupMenu.isAncestorOf(tablePopupMenuSaveTable)) tablePopupMenu.add(tablePopupMenuSaveTable);
	  }
  }

}

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


package rseslib.qmak.dataprocess.table;

import java.util.*;
import javax.swing.table.*;

import rseslib.qmak.dataprocess.table.EditableAttribute;
import rseslib.structure.attribute.*;
import rseslib.structure.attribute.formats.*;

/**
 * <p>Title: TArrayHeaderTableModel</p>
 *
 * <p>Description: Class storing header data for header editing and creating.
 * </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * @version 1.0
 * @author Piotr Skibinski
 */
public class QArrayHeaderTableModel extends AbstractTableModel
    implements HeaderReader
{

  private static final String[] m_columnNames = new String[]
      {"Sel.", "Attribute name", "Type", "Domain"};
  private Vector m_HeaderEntries = new Vector(3);

  private String m_missingValue;
  private boolean[] m_b_table;
  private Attribute[] m_a_table;

  public QArrayHeaderTableModel() {
    m_HeaderEntries.add(new EditableAttribute(EditableAttribute.ATTR_TYPE_CONDITIONAL,
    	EditableAttribute.VALUE_SET_TYPE_NUMERIC, "name"));
    m_HeaderEntries.add(
        new EditableAttribute(EditableAttribute.TYPE_UNSPECIFIED,
        EditableAttribute.TYPE_UNSPECIFIED, ""));
  }

  /**
   * Returns the most specific superclass for all the cell values in the column.
   *
   * @param columnIndex the index of the column
   * @return the common ancestor class of the object values in the model.
   */
  public Class getColumnClass(int columnIndex) {
    if (columnIndex == 0)
      return Boolean.class;
    return String.class;
  }

  /**
   * Returns the number of columns in the model.
   *
   * @return the number of columns in the model
   */
  public int getColumnCount() {
    return 4;
  }

  /**
   * Returns the name of the column at <code>columnIndex</code>.
   *
   * @param columnIndex the index of the column
   * @return the name of the column
   */
  public String getColumnName(int columnIndex) {
    return m_columnNames[columnIndex];
  }

  /**
   * Returns the number of rows in the model.
   *
   * @return the number of rows in the model
   */
  public int getRowCount() {
    return m_HeaderEntries.size();
  }

  /**
   * Returns the value for the cell at <code>columnIndex</code> and
   * <code>rowIndex</code>.
   *
   * @param rowIndex the row whose value is to be queried
   * @param columnIndex the column whose value is to be queried
   * @return the value Object at the specified cell
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    EditableAttribute ahe =
        (EditableAttribute)m_HeaderEntries.elementAt(rowIndex);
    switch (columnIndex)
    {
      case 0:
        return ahe.getSelection();
      case 1:
        return ahe.name();
      case 2:
        return ahe.getAttrTypeName();
      case 3:
        return ahe.getValueSetTypeName();
    }
    return "";
  }

  /**
   * Returns true if the cell at <code>rowIndex</code> and
   * <code>columnIndex</code> is editable.
   *
   * @param rowIndex the row whose value to be queried
   * @param columnIndex the column whose value to be queried
   * @return true if the cell is editable
   */
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  /**
   * Sets the value in the cell at <code>columnIndex</code> and
   * <code>rowIndex</code> to <code>aValue</code>.
   *
   * @param aValue the new value
   * @param rowIndex the row whose value is to be changed
   * @param columnIndex the column whose value is to be changed
   */
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    EditableAttribute ahe =
        (EditableAttribute)m_HeaderEntries.elementAt(rowIndex);
    switch (columnIndex)
    {
      case 0:
        if (rowIndex != m_HeaderEntries.size() - 1)
          ahe.setSelection(((Boolean)aValue).booleanValue());
        break;
      case 1:
        if (((String)aValue).compareTo("") != 0)
          ahe.setName((String)aValue);
        else
          if (rowIndex != m_HeaderEntries.size() -1)
            m_HeaderEntries.remove(rowIndex);
        break;
      case 2:
        ahe.setAttrType((String)aValue);
        this.fireTableCellUpdated(rowIndex, 2);
        break;
      case 3:
        ahe.setValueSetType((String)aValue);
        this.fireTableCellUpdated(rowIndex, 3);
        break;
    }

    // add row if last one is set up correctly
    if (rowIndex == m_HeaderEntries.size() - 1)
      if ((ahe.getAttrType() != EditableAttribute.TYPE_UNSPECIFIED) &&
          (ahe.getValueSetType() != EditableAttribute.TYPE_UNSPECIFIED) &&
          (ahe.name().compareTo("") != 0))
      {
        m_HeaderEntries.add(
            new EditableAttribute(EditableAttribute.TYPE_UNSPECIFIED,
            EditableAttribute.TYPE_UNSPECIFIED, ""));

        this.fireTableRowsInserted(rowIndex+1, rowIndex+1);
      }
  }

  /**
   * Deletes selected rows (attribute descriptions) from table model.
   */
  public void deleteSelected()
  {
    Iterator iter = m_HeaderEntries.iterator();
    EditableAttribute ea;

    while (iter.hasNext())
    {
      ea = (EditableAttribute)iter.next();

      if (ea.isSelected())
      {
        iter.remove();
      }
    }
    this.fireTableRowsDeleted(0, m_HeaderEntries.size());
  }

  public Collection allMissing()
  {
    Collection v = new Vector(1);
    v.add(m_missingValue);
    return v;
  }

  public String singleMissing()
  {
    return m_missingValue;
  }

  public boolean[] bitMaskOfLoaded()
  {
    return m_b_table;
  }

  public Attribute[] attributesForLoading()
  {
    return m_a_table;
  }

  public void initHeaderReader(String missingValue)
  {
    m_missingValue = missingValue;
    m_HeaderEntries.removeElementAt(m_HeaderEntries.size() - 1);

    m_b_table = new boolean[m_HeaderEntries.size()];
    int i;
    for (i = 0; i < m_b_table.length; i++)
      m_b_table[i] = true;

    m_a_table = new Attribute[m_HeaderEntries.size()];
    Iterator iter = m_HeaderEntries.iterator();
    for(i = 0; i < m_a_table.length; i++)
    {
      EditableAttribute atr = (EditableAttribute) iter.next();
      if (atr.isNominal()) {
         m_a_table[i] = new NominalAttribute(atr.type(), atr.name());
      }
      else {
        if (atr.isNumeric()) {
        	m_a_table[i] = new NumericAttribute(atr.type(), atr.name());
        }
        else {
          m_a_table[i] = new Attribute(atr.type(), atr.valueType(), atr.name());
        }
      }
    }
  }
}

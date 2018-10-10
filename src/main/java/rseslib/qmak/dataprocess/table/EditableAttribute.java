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


package rseslib.qmak.dataprocess.table;

import rseslib.structure.attribute.Attribute;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p> Extended Attribute for creating and editing table header
 * purposes.
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Piotr Skibinski
 * @version 1.0
 */
public class EditableAttribute
    extends Attribute
{
	/** Conditional attribute type. */
    public static final byte ATTR_TYPE_CONDITIONAL = 0;
    /** Name of the conditional attribute type. *
    public static final String ATTR_TYPENAME_CONDITIONAL = "conditional";
    /** Decision attribute type. */
    public static final byte ATTR_TYPE_DECISION = 1;
    /** Name of the decision attribute type. *
    public static final String ATTR_TYPENAME_DECISION = "decision";
    /** Text attribute type. */
    public static final byte ATTR_TYPE_TEXT = 2;
    /** Name of the text attribute type. *
    public static final String ATTR_TYPENAME_TEXT = "text";

    /** Non-applicable type of the set of values. */
    public static final byte VALUE_SET_TYPE_NONAPPLICABLE = 10;
    /** Name of the non-applicable type of the set of values. *
    public static final String VALUE_SET_TYPENAME_NONAPPLICABLE = "n/a";
    /** Numeric type of the set of values. */
    public static final byte VALUE_SET_TYPE_NUMERIC = 11;
    /** Name of the numeric type of the set of values. *
    public static final String VALUE_SET_TYPENAME_NUMERIC = "numeric";
    /** Nominal type of the set of values. */
    public static final byte VALUE_SET_TYPE_NOMINAL = 12;
    /** Name of the nominal type of the set of values. *
    public static final String VALUE_SET_TYPENAME_NOMINAL = "nominal";


  /** Unspecified type attribute type. */
  public static final byte TYPE_UNSPECIFIED = 100;

  private Boolean m_selection;
  
  private boolean m_unspecified = true;
  private boolean m_valueSetUnspecified = true;

  public EditableAttribute(byte attrType, byte valueSetType, String name)
  {
    super((attrType==ATTR_TYPE_DECISION)?Type.decision:(attrType==ATTR_TYPE_TEXT)?Type.text:Type.conditional,
    		(valueSetType==VALUE_SET_TYPE_NUMERIC)?ValueSet.numeric:(valueSetType==VALUE_SET_TYPE_NOMINAL)?ValueSet.nominal:ValueSet.nonapplicable,
    				name);
    m_selection = Boolean.FALSE;
    m_unspecified = (attrType==TYPE_UNSPECIFIED); 
    m_valueSetUnspecified = (valueSetType==TYPE_UNSPECIFIED);
  }

  public EditableAttribute(Attribute attr)
  {
    super(attr);
    m_selection = Boolean.FALSE;
  }

  public boolean isTypeUnspecified()
  {
    if (m_unspecified)
      return true;
    return false;
  }

  public boolean isSetTypeUnspecified()
  {
    if (m_valueSetUnspecified)
      return true;
    return false;
  }

  public boolean isNonApplicable()
  {
    if (m_nValueSetType == ValueSet.nonapplicable)
        return true;
    return false;
  }

  public Boolean getSelection()
  {
    return m_selection;
  }

  public void setSelection(boolean new_value)
  {
    if (new_value) {
      m_selection = Boolean.TRUE;
    }
    else {
      m_selection = Boolean.FALSE;
    }
  }

  public boolean isSelected()
  {
    return m_selection.booleanValue();
  }

  public void setName(String new_name)
  {
    m_Name = new_name;
  }

  public ValueSet valueType()
  {
	  return m_nValueSetType;
  }
  
  public byte getValueSetType()
  {
	if (m_valueSetUnspecified)
		return TYPE_UNSPECIFIED;
	if (isNumeric())
		return VALUE_SET_TYPE_NUMERIC;
	if (isNominal())
		return VALUE_SET_TYPE_NOMINAL;
	return VALUE_SET_TYPE_NONAPPLICABLE;
  }

  public String getValueSetTypeName()
  {
    if (isNumeric()) {
      return ValueSet.numeric.name();
    }
    if (isNominal()) {
      return ValueSet.nominal.name();
    }
    if (m_nValueSetType == ValueSet.nonapplicable) {
      return ValueSet.nonapplicable.name();
    }
    return "";
  }

  public void setValueSetType(String newSetType)
  {
    if (this.isInterpretable() || this.isTypeUnspecified()) {
      if (newSetType.compareTo(ValueSet.nominal.name()) == 0) {
        m_nValueSetType = ValueSet.nominal;
        m_valueSetUnspecified = false;
        return;
      }
      if (newSetType.compareTo(ValueSet.numeric.name()) == 0) {
        m_nValueSetType = ValueSet.numeric;
        m_valueSetUnspecified = false;
        return;
      }
    }
    if (newSetType.compareTo(ValueSet.nonapplicable.name()) == 0) {
      m_nValueSetType = ValueSet.nonapplicable;
      m_valueSetUnspecified = false;
    }
  }

  public Type type()
  {
	  return m_nAttrType;
  }
  
  public byte getAttrType()
  {
	if (m_unspecified) return TYPE_UNSPECIFIED;
    switch (m_nAttrType)
    {
    case conditional:
    	return ATTR_TYPE_CONDITIONAL;
    case decision:
    	return ATTR_TYPE_DECISION;
    case text:
    	return ATTR_TYPE_TEXT;
    }
    return TYPE_UNSPECIFIED;
  }

  public String getAttrTypeName()
  {
    if (isConditional()) {
      return Attribute.Type.conditional.name();
    }
    if (isDecision()) {
      return Attribute.Type.decision.name();
    }
    if (isText()) {
      return Attribute.Type.text.name();
    }
    return "";
  }

  public void setAttrType(String newAttrType)
  {
    if (newAttrType.compareTo(Attribute.Type.conditional.name()) == 0) {
      m_nAttrType = Attribute.Type.conditional;
      m_unspecified = false;
      if (this.isNonApplicable() || this.isSetTypeUnspecified())
        m_nValueSetType = ValueSet.numeric;
      m_valueSetUnspecified = false;
    }
    if (newAttrType.compareTo(Attribute.Type.decision.name()) == 0) {
      m_nAttrType = Attribute.Type.decision;
      m_unspecified = false;
      if (this.isNonApplicable() || this.isSetTypeUnspecified())
        m_nValueSetType = ValueSet.numeric;
      m_valueSetUnspecified = false;
    }
    if (newAttrType.compareTo(Attribute.Type.text.name()) == 0) {
      m_nAttrType = Attribute.Type.text;
      m_unspecified = false;
      //m_nValueSetType = VALUE_SET_TYPE_NONAPPLICABLE;
    }
  }
}

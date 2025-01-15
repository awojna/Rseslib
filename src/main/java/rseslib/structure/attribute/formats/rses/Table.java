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


package rseslib.structure.attribute.formats.rses;

import java.util.*;
import java.io.*;


/**
 * Rough Set Library
 *
 * @author Jan Bazan
 *
 */



public class Table extends Element
{

	/**
	 * The variable into which the number of objects is stored.
	 */
	private int noObj;

	/**
	 * The variable into which the number of attributes is stored.
	 */
	private int noAttr;


	/**
     * The array buffer into which the elements of the <tt>Table</tt> are stored.
     */

        private Attr [] attributes;
	private int [] table;

        private double positiveRegion = -1.0;


	/**
     * Constructs an empty table.
     */

	public Table()
	{
		noObj=0;
		noAttr=0;
		table = null;
		attributes = null;

		super.setType(Element.TABLE);
	}

	/*
	public boolean testTableForScalling()
	{
	  if (noAttr==0) return false;

	  for (int i=0; i<noAttr; i++)
	  {
	    if (getAttrType(i)==Attr.SYMBOLIC) return false;
	  }
	  return true;
	}
	*/

	public Attr getAttr(int index)
	{
	  if ((index<0)||(index>=noAttr))
	  {
		throw new IndexOutOfBoundsException(
			"Table.getAttr - Index: "+index+", Size: "+noAttr);
	  }
	  return attributes[index];
	}

	public String getAttrName(int index)
	{
	  return getAttr(index).getName();
	}

	public boolean getAttrType(int index)
	{
	  return getAttr(index).getType();
	}

	public int getAttrNumericType(int index)
	{
	  boolean type = getAttrType(index);
	  int prec = getAttrPrec(index);

	  if (type==Attr.SYMBOLIC) return -1;
	  else return prec;
	}

	public int getAttrPrec(int index)
	{
	  return getAttr(index).getPrec();
	}

	public int getAttrCode(String attrS)
	{
	  for (int i=0; i<noAttr; i++)
	  {
	    if (attrS.compareTo(getAttrName(i))==0) return i;
	  }
	  throw new IndexOutOfBoundsException("Cannot find attribute: "+attrS+" in this table!");
	}


	public void setAttr(int index,Attr attr)
	{
	  if ((index<0)||(index>=noAttr))
	  {
		throw new IndexOutOfBoundsException(
			"Table.setAttrType - Index: "+index+", Size: "+noAttr);
	  }
	  attributes[index] = new Attr(attr);
	}


        static public boolean EXIT_WHEN_DUPLICATED_ATTRIBUTE = false;

	public void setAttrName(int index,String newName)
	{
	  Attr attr = getAttr(index);

	  String locNewName = new String(Element.addQuotationMarks(newName));

	  for (int i=0; i<noAttr; i++)
	  {
	    if ((i!=index)&&(getAttrName(i)!=null))
	    {
	      if (getAttrName(i).compareTo(locNewName)==0)
	      {
		messageL("WARNING: Duplicated attribute name: " + locNewName + " in table!");
		locNewName = locNewName + "_" + i;
		messageL("The attribute name has been changed to: " + locNewName);
                if (EXIT_WHEN_DUPLICATED_ATTRIBUTE) System.exit(0);
	      }
	    }
	  }
	  attr.setName(locNewName);
	}

	public void setAttrSymbolic(int index)
	{
	  Attr attr = getAttr(index);
	  attr.setSymbolic();
	}

	public void setAttrNumeric(int index,int prec)
	{
	  Attr attr = getAttr(index);
	  attr.setNumeric(prec);
	}


	public void setAttrType(int index,boolean type,int precision)
	{
	  Attr attr = getAttr(index);

	  if (type==Attr.SYMBOLIC) attr.setSymbolic();
	  else attr.setNumeric(precision);
	}

	public void setAttrType(int index)
	{
	  Attr attr = getAttr(index);
	  attr.setSymbolic();

	}


	public void copyAttributes(Table tab)
	{
	  attributes = new Attr[tab.getNoAttr()];

	  for (int i=0; i<tab.getNoAttr(); i++)
	  {
	    attributes[i] = new Attr();
	    attributes[i].copy(tab.getAttr(i));
	  }
	}

        public int getNoSymbolicAttr()
        {
          int locNoSymbolic = 0;
          for (int i=0; i<getNoAttr(); i++)
          {
            if (getAttrType(i)==Attr.SYMBOLIC) locNoSymbolic++;
          }
          return locNoSymbolic;
        }

        public int getNoNumericConditionalAttr()
        {
          int locNoNumeric = 0;
          for (int i=0; i<getNoAttr()-1; i++)
          {
            if (getAttrType(i)==Attr.NUMERIC) locNoNumeric++;
          }
          return locNoNumeric;
        }

	//=========================================

        public boolean isEqualObj(int num1,int num2)
        {
          for (int i=0; i<getNoAttr(); i++)
          {
            if (getTable(num1,i)!=getTable(num2,i))
            {
              return false;
            }
          }

          return true;
        }




	/**
     * Creates table with specified number objects and attributes.
     *
     * @param   new_no_obj new number of objects
     * @param   new_no_attr new number of attributes
     */

	public void createTable(int new_no_obj,int new_no_attr)
	{
		int no_elem = new_no_obj * new_no_attr;
		table = new int [no_elem];
		noObj=new_no_obj;
		noAttr=new_no_attr;

		attributes = new Attr[noAttr];
		for (int i=0; i<noAttr; i++) attributes[i] = new Attr();

	}

	/**
	 * Removes all of the elements from table.  The table will
	 * be empty after this call returns.
	 */

	public void clear()
	{
		noObj=0;
		noAttr=0;
		table = null;
		attributes = null;

		super.setType(Element.TABLE);
	}

	public void copy(Table newTable)
	{
	  createTable(newTable.getNoObj(),newTable.getNoAttr());

	  copyAttributes(newTable);

	  for (int i=0; i<getNoObj(); i++)
	  {
	    for (int j=0; j<getNoAttr(); j++)
	    {
	      setTable(i,j,newTable.getTable(i,j));
	    }
	  }

	  super.setType(Element.TABLE);
	}

	/**
     * Returns current number of objects stored in <tt>Table</tt>.
     *
     */

	public int getNoObj(){return noObj;};

	/**
     * Returns current number of attributes stored in <tt>Table</tt>.
     *
     */
	public int getNoAttr() {return noAttr;};

	/**
	 * Returns the value of object and attribute at the specified position in this information system.
     *
     * @param   obj_num number of specified object
     * @param   attr_num number of specified attribute
     * @return the value at the specified position in this information system.
     * @throws    IndexOutOfBoundsException if obj_num is out of range
     * <tt>(obj_num &lt; 0 || obj_num &gt;= getNoObj())</tt> or attr_num is out of range
     * <tt>(attr_num &lt; 0 || attr_num &gt;= getNoAttr())</tt>.
     * *
     */
	public int getTable(int obj_num, int attr_num)
	{
	  if ((obj_num<0)||(obj_num>=noObj))
	  {
		  throw new IndexOutOfBoundsException(
			  "Table - Index: "+obj_num+", Size: "+noObj);
	  }

	  if ((attr_num<0)||(attr_num>=noAttr))
	  {
		  throw new IndexOutOfBoundsException(
			  "Index: "+attr_num+", Size: "+noAttr);
	  }

	  int elem_num = (obj_num * noAttr) + attr_num;

	  return table[elem_num];
	}

	public String getTableString(int obj_num, int attr_num)
	{
  	  if ((obj_num<0)||(obj_num>=noObj))
	  {
		  throw new IndexOutOfBoundsException(
			  "Table - Index: "+obj_num+", Size: "+noObj);
	  }

	  if ((attr_num<0)||(attr_num>=noAttr))
	  {
		  throw new IndexOutOfBoundsException(
			  "Index: "+attr_num+", Size: "+noAttr);
	  }

	  int elem_num = (obj_num * noAttr) + attr_num;

	  return getAttr(attr_num).getStringValue(table[elem_num]);
	}



	/**
	 * Sets the new value of object and attribute at the specified position in this
	 * information system.
     *
     * @param   obj_num number of specified object
     * @param   attr_num number of specified attribute
     * @param   val new value which will be set
     * @throws    IndexOutOfBoundsException if obj_num is out of range
     * <tt>(obj_num &lt; 0 || obj_num &gt;= getNoObj())</tt> or attr_num is out of range
     * <tt>(attr_num &lt; 0 || attr_num &gt;= getNoAttr())</tt>.
     * *
     */

	public void setTable(int obj_num, int attr_num, int val)
	{
          if ((obj_num<0)||(obj_num>=noObj))
	  {
		  throw new IndexOutOfBoundsException(
			  "Table - Index: "+obj_num+", Size: "+noObj);
	  }

	  if ((attr_num<0)||(attr_num>=noAttr))
	  {
		  throw new IndexOutOfBoundsException(
			  "Index: "+attr_num+", Size: "+noAttr);
	  }

	  if ((getAttrType(attr_num)==Attr.SYMBOLIC)&&(val!=Integer.MAX_VALUE))
	  {
	    if ((val<0)||(val>=getNoWordInDictio()))
	    {
	      String name = getAttrName(attr_num);
	      throw new IndexOutOfBoundsException("Cannot find word for code "+val+" in global dictionary for atribute: "+name+"! (from Table.setTable())");
	    }
	  }

	  int elem_num = (obj_num * noAttr) + attr_num;

	  table[elem_num]=val;
	}

	public void setTableString(int obj_num, int attr_num, String val)
	{
          String nval = new String(Element.addQuotationMarks(val));

          if ((obj_num<0)||(obj_num>=noObj))
	  {
		  throw new IndexOutOfBoundsException(
			  "Table - Index: "+obj_num+", Size: "+noObj);
	  }

	  if ((attr_num<0)||(attr_num>=noAttr))
	  {
		  throw new IndexOutOfBoundsException(
			  "Index: "+attr_num+", Size: "+noAttr);
	  }

	  int code = getAttr(attr_num).getIntValue(nval);

	  int elem_num = (obj_num * noAttr) + attr_num;

	  table[elem_num]=code;
	}


	/**
	 * Loads information system from file.
     *
     * @param   fName file name
     */

       public void loadTableOld(String fName)
	throws IOException, InterruptedException, NumberFormatException
	{
	    clear();

	    File inFile = new File(fName);

	    if (!inFile.exists())
		    throw new IOException("Can't open file with table: " + fName);

	    FileReader fr = new FileReader(inFile);
	    BufferedReader br = new BufferedReader(fr);

	    loadFromFileOld(br);

	    br.close();
	}


	public void loadFromFileOld(BufferedReader br)
	throws IOException, InterruptedException, NumberFormatException
	{

	    String line = br.readLine();
	    StringTokenizer st = new StringTokenizer(line);

	    String no_obj_S = st.nextToken();
	    String no_attr_S = st.nextToken();

	    try
	    {
	      noObj = Integer.parseInt(no_obj_S);
	      noAttr = Integer.parseInt(no_attr_S);
	    }
            catch (NumberFormatException e)
	    {
	      throw new IOException("ERROR IN LINE: 1");
	    }

	    progress(0);

	    int no_elem = noObj * noAttr;

	    attributes = new Attr[noAttr];
	    for (int i=0; i<noAttr; i++) attributes[i] = new Attr();

	    line = br.readLine();
	    st = new StringTokenizer(line);

	    for (int j=0; j<noAttr; j++)
	    {
	      String name_S = st.nextToken();

	      int k=name_S.length()-1;
	      int posLeft=-1;
	      int posRight=-1;
	      while (k>=0)
	      {
	       if (name_S.charAt(k)==')') posRight = k;
	       if (name_S.charAt(k)=='(') posLeft = k;
	       if ((posLeft!=-1)&&(posRight!=-1)) break;
	       k--;
	      }
	      if ((posLeft==-1)||(posRight==-1))
	      {
		throw new IOException("Bad format of file with table (row 2) in text: "+name_S );
	      }

	      setAttrName(j,name_S.substring(0,posLeft));

	      for (k=0; k<j; k++)
	      {
		if (getAttrName(k).compareTo(getAttrName(j))==0)
		{
		  throw new IOException("Duplicated attribute name ("+getAttrName(j)+").");
		}
	      }

	      String sType =  name_S.substring(posLeft+1,posRight);

	      try
	      {
	        int precision = Integer.parseInt(sType);
		if (precision>-1) setAttrType(j,Attr.NUMERIC,precision);
		             else setAttrType(j);
      	      }
	      catch (NumberFormatException e)
	      {
	        System.err.println("ERROR IN LINE: "+1);
	      }

	    }


	    table = new int [no_elem];

	    for (int i=0; i<noObj; i++)
	    {
	      progress(i*100/noObj);


	      if ((i>0)&&(i/10000*10000==i))
	           messageL("Current number of loaded object: "+i);

		/*
	      if ((i>0)&&(i/10000*10000==i))
	           System.out.println("Current number of loaded object: "+i);
*/

	      line = br.readLine();
	      st = new StringTokenizer(line);

	      for (int j=0; j<noAttr; j++)
	      {
		Attr currentAttr = getAttr(j);

		String val_S = st.nextToken();

                if (MissingCompleter.isMissing(val_S))
		{
		    table[i*noAttr+j]=Integer.MAX_VALUE;
		}
		else
		{
		  try
		  {
		    if ((currentAttr.getType()==Attr.NUMERIC)&&
			(currentAttr.getPrec()==0)) //atrybut calkowity
		    {
		      Integer intVal = new Integer(val_S);
		      table[i*noAttr+j]=intVal.intValue();
		    }
		    else
		    if ((currentAttr.getType()==Attr.NUMERIC)&&
			(currentAttr.getPrec()!=0)) //atrybut ciagly
		     {
			Double dVal = new Double(val_S);
			table[i*noAttr+j]=(int)(dVal.doubleValue() * Math.pow(10.0,currentAttr.getPrec()));
		     }
		     else
		       if (currentAttr.getType()==Attr.SYMBOLIC)
		       {
			  int code = super.addWordToDictio(val_S);
			  table[i*noAttr+j] = code;
		       }
		       else //attrTypes[j]==-2
		       {
			    table[i*noAttr+j]=0;
		       }
		  }
		  catch (NumberFormatException e)
		  {
		      System.err.println("ERROR FOR ATTR: "+currentAttr.getName()+" Type: "+currentAttr.getType()+"  "+val_S);
		      System.exit(0);
		  }
		  catch (StackOverflowError ee)
		  {
		      System.err.println("StackOverflowError FOR ATTR: "+currentAttr.getName()+" Type: "+currentAttr.getType()+"  "+val_S);
		      System.exit(0);
		  }
		}
	      }
	    }


    	    progress(100);

	}

        	//========================== NOWY FORMAT ==============

        public void loadTable(String fName)
	throws IOException, InterruptedException, NumberFormatException
	{
	    clear();

	    File inFile = new File(fName);

	    if (!inFile.exists())
		    throw new IOException("Can't open file with table: "+fName);

	    FileReader fr = new FileReader(inFile);
	    BufferedReader br = new BufferedReader(fr);

	    loadFromFile(br);

	    br.close();
	}


	public void loadFromFile(BufferedReader br)
	throws IOException, InterruptedException, NumberFormatException
	{
	    IntWrap LINE_NUMBER = new IntWrap(0);
	    super.loadTypeAndNameFromFile(br,LINE_NUMBER,Element.TABLE);

	    String line = readNextLine(br,LINE_NUMBER);

	    StringTokenizer st = new StringTokenizer(line);
	    String token = readNextToken(st,LINE_NUMBER);
	    String TOKEN = token.toUpperCase();
	    if (TOKEN.compareTo("ATTRIBUTES")!=0)
	    {
	      throw new IOException("Expected key word: 'ATTRIBUTES' in line: "+LINE_NUMBER.getValue());
	    }

            token = readNextToken(st,LINE_NUMBER);

	    try
	    {
	      noAttr = Integer.parseInt(token);
	    }
            catch (NumberFormatException e)
	    {
	      throw new IOException("Bad number of attributes in line: "+LINE_NUMBER.getValue());
	    }

            if (noAttr>0)
	    {
	      attributes = new Attr[noAttr];
	      for (int i=0; i<noAttr; i++)
	      {
		attributes[i] = new Attr("Table.loadtable",Attr.SYMBOLIC,0);
	      }
	    } else attributes = null;

	    for (int i=0; i<noAttr; i++)
	    {
	      line = readNextLine(br,LINE_NUMBER);
	      st = new StringTokenizer(line);
	      String attrName = readNextToken(st,LINE_NUMBER);

	      if (attrName.length()<1)
	      {
		throw new IOException("The name of attribute is too short! (see line "+LINE_NUMBER.getValue()+")");
	      }

	      for (int k=0; k<i; k++)
	      {
		if (attributes[k].getName().compareTo(attrName)==0)
		{
		  throw new IOException("Duplicated attribute name ("+attrName+").");
		}
	      }

	      token = readNextToken(st,LINE_NUMBER);
	      TOKEN = token.toUpperCase();
	      if (TOKEN.compareTo("SYMBOLIC")==0)
	      {
		attributes[i].setName(attrName);
		attributes[i].setSymbolic();
	      }
	      else
	      {

		if (TOKEN.compareTo("NUMERIC")!=0)
		{
		  throw new IOException("Expected attribute type as word: 'symbolic' or 'numeric' in line "+LINE_NUMBER.getValue());
		}

		String attrPrecisionS = readNextToken(st,LINE_NUMBER);
		int attrPrecision=Integer.MAX_VALUE;

		try
		{
		  attrPrecision = Integer.parseInt(attrPrecisionS);
		}
		catch (NumberFormatException e)
		{
		  throw new IOException("Bad precision of attribute in line: "+LINE_NUMBER.getValue());
		}

		attributes[i].setName(attrName);
		attributes[i].setNumeric(attrPrecision);
	      }

	    }


	    progress(0);

	    line = readNextLine(br,LINE_NUMBER);

	    st = new StringTokenizer(line);
	    token = readNextToken(st,LINE_NUMBER);
	    TOKEN = token.toUpperCase();
	    if (TOKEN.compareTo("OBJECTS")!=0)
	    {
	      throw new IOException("Expected key word: 'OBJECTS' in line: "+LINE_NUMBER.getValue());
	    }

            token = readNextToken(st,LINE_NUMBER);
	    try
	    {
	      noObj = Integer.parseInt(token);
	    }
            catch (NumberFormatException e)
	    {
	      throw new IOException("Bad number of objects in line: "+LINE_NUMBER.getValue());
	    }

    	    if (noObj==0)
	    {
	      clear(); return;
	    }

//	    System.out.println("noObj="+noObj);

	    int no_elem = noObj * noAttr;

	    table = new int [no_elem];

	    for (int i=0; i<noObj; i++)
	    {
	      progress(i*100/noObj);

	      /*
	      if ((i>0)&&(i/10000*10000==i))
	           messageL("Current number of loaded object: "+i);
		   */

	      line = readNextLine(br,LINE_NUMBER);

	      st = new StringTokenizer(line);

	      for (int j=0; j<noAttr; j++)
	      {

		Attr currentAttr = attributes[j];


		String val_S = readNextToken(st,LINE_NUMBER);

		if (val_S.length()==0)
		{
		  throw new IOException("Expected more words in line: "+LINE_NUMBER.getValue());
		}

	        try
		{
  		  if (currentAttr.getType()==Attr.NUMERIC)
		  {
                    if (MissingCompleter.isMissing(val_S))
		    {
		      table[i*noAttr+j]=Integer.MAX_VALUE;
		    }
		    else
		    {
		      Double dVal = new Double(val_S);
                      table[i*noAttr+j]=(int)(dVal.doubleValue() * Math.pow(10.0,currentAttr.getPrec()));
		    }
		  }
		  else
                   if (currentAttr.getType()==Attr.SYMBOLIC)
                   {
                      if (MissingCompleter.isMissing(val_S))
                      {
                        table[i*noAttr+j]=Integer.MAX_VALUE;
                      }
                      else
                      {
                        int code = super.addWordToDictio(val_S);
                        table[i*noAttr+j] = code;
                      }
                   }
                   else //attrTypes[j]==-2
                   {
                        table[i*noAttr+j]=0;
                   }
		}
		catch (NumberFormatException e)
		{
		    throw new IOException("Bad numerical format of data in line: "+LINE_NUMBER+" (value: "+val_S+") for attribute:"+currentAttr.getName()+" type: "+currentAttr.getType());
		}
                catch (StackOverflowError ee)
                {
		    throw new IOException("StackOverflowError FOR ATTR: "+getAttrName(j));
                }
	      }
	    }


    	    progress(100);
	}

	//--------------------------------------------------

        public void loadTable(String fNameTab,String fNameAttrSack)
	throws IOException, InterruptedException, NumberFormatException
	{
	    clear();

	    File inFile = new File(fNameTab);

	    if (!inFile.exists())
		    throw new IOException("Can't open file with table: " + fNameTab);

	    FileReader fr = new FileReader(inFile);
	    BufferedReader br = new BufferedReader(fr);

	    StringSack attrSack = new StringSack();
	    attrSack.importFromFile(fNameAttrSack);

	    loadFromFile(br,attrSack);

	    br.close();
	}



	public void loadFromFile(BufferedReader br,StringSack attrSack)
	throws IOException, InterruptedException, NumberFormatException
	{

	    IntWrap LINE_NUMBER = new IntWrap(0);
	    super.loadTypeAndNameFromFile(br,LINE_NUMBER,Element.TABLE);

	    String line = readNextLine(br,LINE_NUMBER);

	    StringTokenizer st = new StringTokenizer(line);
	    String token = readNextToken(st,LINE_NUMBER);
	    String TOKEN = token.toUpperCase();
	    if (TOKEN.compareTo("ATTRIBUTES")!=0)
	    {
	      throw new IOException("Expected key word: 'ATTRIBUTES' in line: "+LINE_NUMBER.getValue());
	    }

            token = readNextToken(st,LINE_NUMBER);

	    int noAllAttr;
	    try
	    {
	      noAllAttr = Integer.parseInt(token);
	    }
            catch (NumberFormatException e)
	    {
	      throw new IOException("Bad number of attributes in line: "+LINE_NUMBER.getValue());
	    }

	    boolean [] SELECTED_ATTR = null;
	    Attr [] ATTR = null;

	    if (noAllAttr>0)
	    {
	      ATTR = new Attr[noAllAttr];
	      for (int i=0; i<noAllAttr; i++) ATTR[i] = new Attr("loadTable",Attr.SYMBOLIC,0);

	      SELECTED_ATTR = new boolean[noAllAttr];
	      for (int i=0; i<noAllAttr; i++) SELECTED_ATTR[i] = false;
	    }
	    else ATTR = null;

	    int noNotOmitAttr = 0;

	    for (int i=0; i<noAllAttr; i++)
	    {

	      line = readNextLine(br,LINE_NUMBER);

	      st = new StringTokenizer(line);
	      String attrName = readNextToken(st,LINE_NUMBER);

	      if (token.length()<1)
	      {
		throw new IOException("The name of attribute is too short! (see line "+LINE_NUMBER.getValue()+")");
	      }

	      for (int k=0; k<i; k++)
	      {
		if (ATTR[k].getName().compareTo(attrName)==0)
		{
		  throw new IOException("Duplicated attribute name ("+attrName+").");
		}
	      }

	      token = st.nextToken();
	      TOKEN = token.toUpperCase();
	      if (TOKEN.compareTo("SYMBOLIC")==0)
	      {
		ATTR[i].setName(attrName);
		ATTR[i].setSymbolic();
	      }
	      else
	      {

		if (TOKEN.compareTo("NUMERIC")!=0)
		{
		  throw new IOException("Expected attribute type as word: 'symbolic' or 'numeric' in line "+LINE_NUMBER.getValue());
		}

		String attrPrecisionS = st.nextToken();;
		int attrPrecision=Integer.MAX_VALUE;

		try
		{
		  attrPrecision = Integer.parseInt(attrPrecisionS);
		}
		catch (NumberFormatException e)
		{
		  throw new IOException("Bad precision of attribute in line: "+LINE_NUMBER.getValue());
		}

		ATTR[i].setName(attrName);
		ATTR[i].setNumeric(attrPrecision);
	      }

	      if (attrSack.checkString(attrName))
	      {
		SELECTED_ATTR[i] = true; //NIE ma byc pominiety
		noNotOmitAttr++;
	      }
	    }

	    if (noNotOmitAttr==0)
	    {
	      throw new IOException("No attributes to load in table: "+getName());
	    }

	    noAttr = noNotOmitAttr;

	    if (noAttr>0)
	    {
	      attributes = new Attr[noAttr];
	      for (int i=0; i<noAttr; i++) attributes[i] = new Attr("loadTable",Attr.SYMBOLIC,0);

	      int l=0;
	      for (int i=0; i<noAllAttr; i++)
	      {
		if (SELECTED_ATTR[i])
		{
		  setAttrName(l,ATTR[i].getName());
		  setAttrType(l,ATTR[i].getType(),ATTR[i].getPrec());
		  l++;
		}
	      }
	    }
	    else attributes = null;

	    progress(0);

	    line = readNextLine(br,LINE_NUMBER);


	    st = new StringTokenizer(line);
	    token = readNextToken(st,LINE_NUMBER);
	    TOKEN = token.toUpperCase();
	    if (TOKEN.compareTo("OBJECTS")!=0)
	    {
	      throw new IOException("Expected key word: 'OBJECTS' in line: "+LINE_NUMBER.getValue());
	    }

            token = readNextToken(st,LINE_NUMBER);
	    try
	    {
	      noObj = Integer.parseInt(token);
	    }
            catch (NumberFormatException e)
	    {
	      throw new IOException("Bad number of objects in line: "+LINE_NUMBER.getValue());
	    }

	    if (noObj==0)
	    {
	      clear(); return;
	    }

//	    System.out.println("noObj="+noObj);

	    int no_elem = noObj * noAttr;

	    table = new int [no_elem];

	    for (int i=0; i<noObj; i++)
	    {
	      progress(i*100/noObj);

	      /*
	      if ((i>0)&&(i/10000*10000==i))
	           messageL("Current number of loaded object: "+i);
		   */

	      line = readNextLine(br,LINE_NUMBER);

	      st = new StringTokenizer(line);

              int j=-1;

	      for (int l=0; l<noAllAttr; l++)
	      {

		Attr currentAttr = ATTR[l];

		String val_S = readNextToken(st,LINE_NUMBER);

		if (val_S.length()==0)
		{
		  throw new IOException("Expected more words in line: "+LINE_NUMBER.getValue());
		}

		if (!SELECTED_ATTR[l]) continue;

		j++;


	        try
		{
                  if (currentAttr.getType()==Attr.NUMERIC)
		  {
                    if (MissingCompleter.isMissing(val_S))
		    {
		      table[i*noAttr+j]=Integer.MAX_VALUE;
		    }
		    else
		    {
		      Double dVal = new Double(val_S);
                      table[i*noAttr+j]=(int)(dVal.doubleValue() * Math.pow(10.0,currentAttr.getPrec()));
		    }
		  }
                  else
                     if (currentAttr.getType()==Attr.SYMBOLIC)
                     {
                        if (MissingCompleter.isMissing(val_S))
                        {
                          table[i*noAttr+j]=Integer.MAX_VALUE;
                        }
                        else
                        {
                          int code = super.addWordToDictio(val_S);
                          table[i*noAttr+j] = code;
                        }
                     }
                     else //attrTypes[j]==-2
                     {
                          table[i*noAttr+j]=0;
                     }
		}
		catch (NumberFormatException e)
		{
		    throw new IOException("Bad numerical format of data in line: "+LINE_NUMBER+" (value: "+val_S+")");
		}
                catch (StackOverflowError ee)
                {
		    throw new IOException("StackOverflowError FOR ATTR: "+getAttrName(j));
                }
	      }
	    }


    	    progress(100);
	}


	void saveAttrPrecisions(String fName) throws IOException
	{
	  FileOutputStream fos = new FileOutputStream(fName);

	  OutputStreamWriter osw = new OutputStreamWriter(fos);

	  PrintWriter pw = new PrintWriter(osw);

	  pw.println(noAttr);

	  for (int j=0; j<noAttr; j++)
	  {
	    int type = getAttrPrec(j);
	    if (type<0) pw.print(" 0");
	    pw.print(" 1");
	  }

	  pw.println();

	  pw.close();
	}


        //=============== IMPORT FROM RSES 1.0 ======================

	public void importTableRSES_1_0(String fName)
	throws IOException, InterruptedException, NumberFormatException
	{
       	    clear();

	    File inFile = new File(fName);

	    if (!inFile.exists())
		    throw new IOException("Can't open file with table: " + fName);

            String shortName = inFile.getName();

	    FileReader fr = new FileReader(inFile);
	    BufferedReader br = new BufferedReader(fr);


       	    IntWrap LINE_NUMBER = new IntWrap(1);

	    String line = readNextLine(br,LINE_NUMBER);
	    StringTokenizer st = new StringTokenizer(line);

	    String no_obj_S = st.nextToken();
	    String no_attr_S = st.nextToken();

	    try
	    {
	      noObj = Integer.parseInt(no_obj_S);
	      noAttr = Integer.parseInt(no_attr_S);
	    }
            catch (NumberFormatException e)
	    {
              throw new IOException("Bad numerical format of data in line: "+LINE_NUMBER);
	    }

            LINE_NUMBER.incValue();

	    progress(0);

	    int no_elem = noObj * noAttr;

	    attributes = new Attr[noAttr];
	    for (int i=0; i<noAttr; i++)
            {
              attributes[i] = new Attr();
              attributes[i].setNumeric(0);
              attributes[i].setName("attr"+i);
            }

	    table = new int [no_elem];

	    for (int i=0; i<noObj; i++)
	    {
	      progress(i*100/noObj);

              line = readNextLine(br,LINE_NUMBER);

	      st = new StringTokenizer(line);

	      for (int j=0; j<noAttr; j++)
	      {
	    	  	getAttr(j);

                if (!st.hasMoreElements())
                {
                  throw new IOException("Unexpected end of line: "+LINE_NUMBER.getValue());
                }

		String val_S = st.nextToken();

                try
                {
                  table[i*noAttr+j]=Integer.parseInt(val_S);
                }
                catch (NumberFormatException e)
                {
                  throw new IOException("Bad numerical format of data in line: "+LINE_NUMBER+" (value: "+val_S+")");
                }
	      }
	    }

            this.setName(shortName);

    	    progress(100);

            br.close();

	}

        //=============== IMPORT FROM "RECTANGLE TABLE" ======================

	static public Table importNumericalRectangleTable(String fName)
	throws IOException, InterruptedException, NumberFormatException
	{
	    File inFile = new File(fName);

	    if (!inFile.exists())
		    throw new IOException("Can't open file with table: " + fName);

            String shortName = inFile.getName();

	    FileReader fr = new FileReader(inFile);
	    BufferedReader br = new BufferedReader(fr);

            ArrayList<StringSack> rowList = new ArrayList<StringSack>();

            while(true)
            {
              String line = br.readLine();

              if (line==null) break;

              StringTokenizer st = new StringTokenizer(line," ,;",false);

              if (!st.hasMoreTokens()) break;

              StringSack wordSack = new StringSack();
              while (st.hasMoreTokens())
              {
                String locWord = st.nextToken();
                wordSack.add(locWord);
              }

              rowList.add(wordSack);
	    }

            br.close();

            if (rowList.size()==0) return new Table();

            int locNoObj = rowList.size();
            StringSack firstSack = (StringSack)rowList.get(0);
            int locNoAttr = firstSack.size();

            System.out.println("Liczba obiektow="+locNoObj);
            System.out.println("Liczb attr="+locNoAttr);

            Table newTable = new Table();
            newTable.createTable(locNoObj,locNoAttr);

            for (int i=0; i<newTable.getNoAttr(); i++)
            {
              if (i<newTable.getNoAttr()-1)
              {
                newTable.getAttr(i).setName("Attr_"+i);
                newTable.getAttr(i).setNumeric(3);
              }
              else
              {
                newTable.getAttr(i).setName("Decision");
                newTable.getAttr(i).setSymbolic();
              }
            }

            for (int i=0; i<newTable.getNoObj(); i++)
            {
              StringSack locSack = (StringSack)rowList.get(i);

              if (locNoAttr!=locSack.size())
              {
                System.out.println("locNoAttr!=locSack.size()");
                System.exit(0);
              }

              for (int j=0; j<newTable.getNoAttr(); j++)
              {
                String value = locSack.get(j);

             //   System.out.print(value+" ");

                if (j<newTable.getNoAttr()-1)
                {
                  Attr attr = newTable.getAttr(j);
                  int code = attr.getIntValue(value);
                  newTable.setTable(i,j,code);
                }
                else
                {
                  int code =Element.addWordToDictio(value);
                  newTable.setTable(i,j,code);
                }
              }

            }

            newTable.setName(shortName);


            return newTable;

	}


        //============ IMPORT ROSETTA ================

        /**
         * Loads information system from ROSETTA file.
        *
        * @param   fName file name
        */

        public void importROSETTA(String fName)
        throws IOException, InterruptedException, NumberFormatException
        {
            clear();

            File inFile = new File(fName);

            if (!inFile.exists())
                    throw new IOException("Can't open file with table from ROSETTA: " + fName);

            String shortName = inFile.getName();

            FileReader fr = new FileReader(inFile);
            BufferedReader br = new BufferedReader(fr);

            IntWrap LINE_NUMBER = new IntWrap(0);

            //czytanie nazw atrybutow
            String line = readNextLine(br,LINE_NUMBER);
            StringTokenizer st = new StringTokenizer(line);
            StringSack attrSack = new StringSack();
            while (st.hasMoreTokens())
            {
              String token = readNextToken(st,LINE_NUMBER);
              attrSack.add(token);
            }

            //czytanie typow atrybutow
            line = readNextLine(br,LINE_NUMBER);
            st = new StringTokenizer(line);
            StringSack typesSack = new StringSack();
            while (st.hasMoreTokens())
            {
              String token = readNextToken(st,LINE_NUMBER);
              typesSack.add(token);
            }

            if (attrSack.size()!=typesSack.size())
            {
              throw new IOException("Number of attributes has to be equal to number of attribute types!");
            }

            progress(0);

            noAttr = attrSack.size();

            attributes = new Attr[noAttr];
            for (int i=0; i<noAttr; i++) attributes[i] = new Attr();

            for (int j=0; j<noAttr; j++)
            {
              String nameAttr = attrSack.get(j);
              String nameType = typesSack.get(j);

              if (nameType.equalsIgnoreCase("STRING"))
              {
                attributes[j].setName(nameAttr);
                attributes[j].setSymbolic();
              }
              else
              {
               if (nameType.equalsIgnoreCase("INTEGER"))
               {
                 attributes[j].setName(nameAttr);
                 attributes[j].setNumeric(0);
               }
               else
               {
                 if (nameType.substring(0,5).equalsIgnoreCase("FLOAT"))
                 {
                   int prec;
                   try
                   {

                     int leftPos = 6;
                     int rightPos = nameType.length()-1;
                     if (leftPos>=rightPos) throw new NumberFormatException();
                     String sPrec = nameType.substring(leftPos,rightPos);
                     prec = Integer.parseInt(sPrec);
                   }
                   catch (NumberFormatException e)
                   {
                     throw new IOException("Bad value of attribute precision!");
                   }

                   attributes[j].setName(nameAttr);
                   attributes[j].setNumeric(prec);
                 }
                 else
                 {
                   throw new IOException("Unknown type of attribute! ("+nameType+")");
                 }
               }
              }
            }

            StringSack tabValues = new StringSack();
            noObj = 0;
            while(true)
            {
              line = readLine(br,LINE_NUMBER);
              if (line==null) break;
              st = new StringTokenizer(line);
              for (int i=0; i<noAttr; i++)
              {
                String token = readNextToken(st,LINE_NUMBER);
                tabValues.add(token);
              }
              noObj++;
            }

            if (noObj==0) throw new IOException("Number of objects cannot be equal 0!");

            int no_elem = noObj * noAttr;

            table = new int [no_elem];

            int count = 0;
            for (int i=0; i<noObj; i++)
            {
              progress(i*100/noObj);

              for (int j=0; j<noAttr; j++)
              {
                Attr currentAttr = getAttr(j);
                int val = currentAttr.getIntValue(tabValues.get(count));
                count++;
                table[i*noAttr+j] = val;
              }
            }

            setName(shortName);

            progress(100);

            br.close();

        }

        //============ IMPORT WEKA ================

        /**
         * Loads information system from WEKA file.
        *
        * @param   fName file name
        */

        public void importWEKA(String fName,int precision)
        throws IOException, InterruptedException, NumberFormatException
        {
            clear();

            File inFile = new File(fName);

            if (!inFile.exists())
                    throw new IOException("Can't open file with table from ROSETTA: " + fName);


            FileReader fr = new FileReader(inFile);
            BufferedReader br = new BufferedReader(fr);

            IntWrap LINE_NUMBER = new IntWrap(0);

            //czytanie nazwt tablicy
            String line = readNextLine(br,LINE_NUMBER);
            StringTokenizer st = new StringTokenizer(line);
            String wordRELATION = readNextToken(st,LINE_NUMBER);

            if (!wordRELATION.equalsIgnoreCase("@RELATION"))
            {
              throw new IOException("Expected key word '@RELATION' in line: "+(LINE_NUMBER.getValue()-1));
            }

            String newName = readNextToken(st,LINE_NUMBER);
            this.setName(newName);

            StringSack attrNamesSack = new StringSack();
            StringSack attrTypesSack = new StringSack();

            //czytanie nazw atrybutow
            line = readNextLine(br,LINE_NUMBER);
            st = new StringTokenizer(line);
            String wordATTRIBUTE = readNextToken(st,LINE_NUMBER);
            if (!wordATTRIBUTE.equalsIgnoreCase("@ATTRIBUTE"))
            {
              throw new IOException("Expected key word '@ATTRIBUTE' in line: "+(LINE_NUMBER.getValue()-1));
            }
            String nameAttr = readNextToken(st,LINE_NUMBER);

            attrNamesSack.add(nameAttr);

            String typeAttr = readNextToken(st,LINE_NUMBER);

            if ((!typeAttr.equalsIgnoreCase("REAL"))&&(typeAttr.charAt(0)!='{'))
            {
              throw new IOException("Unknown type of attribute in line: "+(LINE_NUMBER.getValue()-1));
            }

            attrTypesSack.add(typeAttr);

            String word = null;

            while (true)
            {
              line = readNextLine(br,LINE_NUMBER);
              st = new StringTokenizer(line);
              word = readNextToken(st,LINE_NUMBER);
              if (!word.equalsIgnoreCase("@ATTRIBUTE")) break;
              nameAttr = readNextToken(st,LINE_NUMBER);
              attrNamesSack.add(nameAttr);
              typeAttr = readNextToken(st,LINE_NUMBER);
              if ((!typeAttr.equalsIgnoreCase("REAL"))&&(typeAttr.charAt(0)!='{'))
              {
                throw new IOException("Unknown type of attribute in line: "+(LINE_NUMBER.getValue()-1));
              }
              attrTypesSack.add(typeAttr);
            }

            if (!word.equalsIgnoreCase("@DATA"))
            {
              throw new IOException("Expected key word '@DATA' in line: "+(LINE_NUMBER.getValue()-1));
            }

            progress(0);

            noAttr = attrNamesSack.size();

            attributes = new Attr[noAttr];
            for (int i=0; i<noAttr; i++) attributes[i] = new Attr();

            for (int j=0; j<noAttr; j++)
            {
              nameAttr = attrNamesSack.get(j);
              String nameType = attrTypesSack.get(j);

              if (nameType.equalsIgnoreCase("REAL"))
              {
                attributes[j].setName(nameAttr);
                attributes[j].setNumeric(precision);
              }
              else
              {
                 if (nameType.charAt(0)=='{')
                 {
                   attributes[j].setName(nameAttr);
                   attributes[j].setSymbolic();
                 }
                 else throw new IOException("Unknown type of attribute! ("+nameType+")");
              }
            }

            StringSack tabValues = new StringSack();
            noObj = 0;
            while(true)
            {
              line = readLine(br,LINE_NUMBER);
              if (line==null) break;
              st = new StringTokenizer(line);
              for (int i=0; i<noAttr; i++)
              {
                String token = readNextToken(st,LINE_NUMBER);
                tabValues.add(token);
              }
              noObj++;
            }

            if (noObj==0) throw new IOException("Number of objects cannot be equal 0!");

            int no_elem = noObj * noAttr;

            table = new int [no_elem];

            int count = 0;
            for (int i=0; i<noObj; i++)
            {
              progress(i*100/noObj);

              for (int j=0; j<noAttr; j++)
              {
                Attr currentAttr = getAttr(j);
                int val = currentAttr.getIntValue(tabValues.get(count));
                count++;
                table[i*noAttr+j] = val;
              }
            }

            br.close();

            progress(100);

        }


        //============ EXPORT TO WEKA ================

//wersja nieuniwersalna

        public void exportTableToWEKA(String fName) throws IOException
        {
          FileOutputStream fos = new FileOutputStream(fName);
          OutputStreamWriter osw = new OutputStreamWriter(fos);

          PrintWriter pw = new PrintWriter(osw);

          pw.println("@relation '"+this.getName()+"'");

          System.out.println("Atrybuty...");

          for (int i=0; i<noAttr; i++)
          {
            System.out.println(" Atrybut: "+(i+1)+"/"+noAttr);

            Attr attr = this.getAttr(i);
            pw.print("@attribute '"+attr.getName()+"' ");

            if (attr.getType()!=Attr.SYMBOLIC)
            {
              pw.println("real");
            }
            else
            {
              IntSack attrValues = new IntSack();
              for (int j=0; j<noObj; j++)
              {
                attrValues.addNoEqual(getTable(j,i));
              }

              pw.print("{");
              for (int j=0; j<attrValues.size(); j++)
              {
                String value = attr.getStringValue(attrValues.get(j));
                pw.print("'"+value+"'");
                if (j<attrValues.size()-1) pw.print(",");
              }
              pw.println("}");
            }
          }

          System.out.println("Obiekty...");

          pw.println("@data");

          for (int i=0; i<noObj; i++)
          {
            int locDecVal = getTable(i,noAttr-1);

            if (locDecVal==Integer.MAX_VALUE) continue;

            for (int j=0; j<noAttr; j++)
            {
              Attr attr = this.getAttr(j);

              int locVal = getTable(i,j);
              if (locVal!=Integer.MAX_VALUE)
              {
                if (attr.getType()==Attr.SYMBOLIC) pw.print("'");
                pw.print(getTableString(i,j));
                if (attr.getType()==Attr.SYMBOLIC) pw.print("'");
                if (j<noAttr-1) pw.print(",");
              }
              else
              {
                pw.print("?");
                if (j<noAttr-1) pw.print(",");
              }
            }
            pw.println();
          }

          pw.close();
        }



        //======== ZAPISY TABLICY ======================


	public void saveTable(String fName) throws IOException, InterruptedException
	{
		FileOutputStream fos = new FileOutputStream(fName);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		PrintWriter pw = new PrintWriter(osw);

		saveToFile(pw);

		pw.close();
	}


        public void saveToFile(PrintWriter pw) throws IOException, InterruptedException
	{
	        super.saveTypeAndNameToFile(pw);

		pw.println("ATTRIBUTES "+noAttr);

		for (int i=0; i<noAttr; i++)
		{
 		  pw.print(" "+Element.addQuotationMarks(getAttrName(i))+" ");
		  if (getAttrType(i)==Attr.SYMBOLIC) pw.println("symbolic");
		  else
		  {
		    pw.println("numeric "+getAttrPrec(i));
		  }
		}

		pw.println("OBJECTS "+noObj);

		for (int i=0; i<noObj; i++)
		{
		  pw.print(" ");

		  progress(i*100/noObj);

		  for (int j=0; j<noAttr; j++)
		  {
		    pw.print(Element.addQuotationMarks(getTableString(i,j))+" ");
		  }
		  pw.println();
		}
	}

        //============ EXPORT do formatu RSES 1.0 ================

        public void saveTableRSES_1_0(String fName) throws IOException
        {
          FileOutputStream fos = new FileOutputStream(fName);
          OutputStreamWriter osw = new OutputStreamWriter(fos);

          PrintWriter pw = new PrintWriter(osw);

          pw.print(noObj);
          pw.print(" ");
          pw.print(noAttr);
          pw.println();

          for (int i=0; i<noObj; i++)
          {
            for (int j=0; j<noAttr; j++)
            {
                    pw.print(getTable(i,j));
                    pw.print(" ");
            }
            pw.println();
          }

          pw.close();
        }


        //============ EXPORT do formatu SVM ================

        public void saveTableSVM(String fName,int selectedValueAsPositiveExample) throws IOException
        {
          FileOutputStream fos = new FileOutputStream(fName);
          OutputStreamWriter osw = new OutputStreamWriter(fos);

          PrintWriter pw = new PrintWriter(osw);

          pw.println("# Wyeksportowane z RS-liba do SVM");

          for (int i=0; i<noObj; i++)
          {
            int decVal = getTable(i,noAttr-1);

            if (decVal==selectedValueAsPositiveExample) pw.print("+1 ");
            else pw.print("-1 ");

            for (int j=0; j<noAttr-1; j++)
            {
              if (getAttr(j).getType()==Attr.SYMBOLIC) continue;

              if (getTable(i,j)==Integer.MAX_VALUE)
              {
                pw.print((j+1)+":"+Integer.MAX_VALUE+" ");
              }
              else
              {
                pw.print((j+1)+":"+getTableString(i,j)+" ");
              }
            }
            pw.println();
          }

          pw.close();
        }

        //============ EXPORT do formatu CART ================

        public void saveTableCART(String fName) throws IOException
        {
          FileOutputStream fos = new FileOutputStream(fName);
          OutputStreamWriter osw = new OutputStreamWriter(fos);

          PrintWriter pw = new PrintWriter(osw);

          for (int i=0; i<noAttr; i++)
          {
            if (getAttr(i).getType()==Attr.SYMBOLIC) continue;

            String attrName = getAttrName(i);
            pw.print(attrName);
            if (i<noAttr-1) pw.print(",");
          }
          pw.println();

          for (int i=0; i<noObj; i++)
          {
            for (int j=0; j<noAttr; j++)
            {
              if (getAttr(j).getType()==Attr.SYMBOLIC) continue;
              pw.print(getTableString(i,j));
              if (j<noAttr-1) pw.print(",");
            }
            pw.println();
          }

          pw.close();
        }


        //============ EXPORT do XML ================


        public void saveTableToXML(String fName)  throws IOException, InterruptedException
        {

      	  progress(0);

          FileOutputStream fos = new FileOutputStream(fName);

          OutputStreamWriter osw = new OutputStreamWriter(fos);

          PrintWriter pw = new PrintWriter(osw);

          pw.println("<?xml version=\"1.0\"?>");
          pw.println();
          pw.println("<!-- Exported from RSES 2.1 -->");
          pw.println();
          pw.println("<!DOCTYPE decisiontable [");
          pw.println(" <!ELEMENT decisiontable (attributes, objects)>");
          pw.println(" <!ATTLIST decisiontable name CDATA #REQUIRED>");
          pw.println(" <!ELEMENT attributes (attribute+)>");
          pw.println(" <!ELEMENT attribute EMPTY>");
          pw.println(" <!ATTLIST attribute id CDATA #REQUIRED");
          pw.println("                     name CDATA #REQUIRED");
          pw.println("                     type CDATA #REQUIRED");
          pw.println("                     precision CDATA #IMPLIED>");
          pw.println(" <!ELEMENT objects (object+)>");
          pw.println(" <!ELEMENT object (descriptor+)>");
          pw.println(" <!ATTLIST object id CDATA #REQUIRED>");
          pw.println(" <!ELEMENT descriptor EMPTY>");
          pw.println(" <!ATTLIST descriptor attribute CDATA #REQUIRED");
          pw.println("                      value CDATA #REQUIRED>");
          pw.println("]>");
          pw.println();

          pw.println("<decisiontable name=\""+getName()+"\">");

          pw.println(" <attributes>");

          for (int i=0; i<getNoAttr(); i++)
          {
            Attr attr = getAttr(i);
            pw.print("  <attribute id=\""+ i + "\" name=\""+attr.getName()+"\" ");
            if (attr.getType()==Attr.SYMBOLIC)
            {
              pw.println("type=\"symbolic\"/>");
            }
            else
            {
              pw.println("type=\"numeric\" precision=\""+ attr.getPrec() + "\"/>");
            }
          }

          pw.println(" </attributes>");

          pw.println(" <objects>");


          for (int i=0; i<noObj; i++)
          {
            progress(i*100/noObj);

            pw.println("  <object id=\""+i+"\">");

            for (int j=0; j<noAttr; j++)
            {
              pw.println("   <descriptor attribute=\""+getAttr(j).getName()+"\" value=\""+getTableString(i,j)+"\"/>");
            }
            pw.println("  </object>");

          }

          pw.println(" </objects>");

          pw.println("</decisiontable>");

       	  progress(100);

          pw.close();
        }

        //============================================================

	public boolean compareAttrNames(StringSack aNames)
	{
	  if (aNames.size()!=getNoAttr()) return false;

	  for (int i=0; i<getNoAttr(); i++)
	  {
	    if (getAttrName(i).compareTo(aNames.get(i))!=0) return false;
	  }
	  return true;
	}

	public Table extractTable(int noSelectedAttrFromBegin)
	{
	  if (noObj==0) return new Table();

	  if (noSelectedAttrFromBegin>noAttr)
	  {
	    throw new IndexOutOfBoundsException(
			  "!!Table - Index: "+ noSelectedAttrFromBegin+", Size: "+noAttr);
	  }

	  Table newTable = new Table();

	  newTable.createTable(noObj,noSelectedAttrFromBegin);

	  for (int i=0; i<newTable.getNoAttr(); i++)
	  {
	    newTable.getAttr(i).copy(getAttr(i));
	  }

	  for (int i=0; i<noObj; i++)
	  {
	    for (int j=0; j<noSelectedAttrFromBegin; j++)
	    {
	      newTable.setTable(i,j,getTable(i,j));
	    }
	  }

	  return newTable;
	}

	public Table concatTableAndAttr(int attrNum,Table table)
	{
 	  if (noObj==0) return new Table();

	  if ((table.getNoAttr()==0)||
	      (attrNum>=table.getNoAttr())||
	      (attrNum<0)||
	      (noObj!=table.getNoObj()))
  	  {
	    throw new IndexOutOfBoundsException(
			  "Table.concatTableAndAttr - Index");
	  }

	  Table newTable = new Table();
	  newTable.createTable(noObj,noAttr+1);

  	  for (int i=0; i<noAttr; i++)
	  {
	    newTable.getAttr(i).copy(getAttr(i));
	  }

	  newTable.getAttr(noAttr).copy(table.getAttr(attrNum));

	  for (int i=0; i<noObj; i++)
	  {
	    for (int j=0; j<noAttr; j++)
	    {
	      newTable.setTable(i,j,getTable(i,j));
	    }

	    newTable.setTable(i,newTable.getNoAttr()-1,
	                      table.getTable(i,attrNum));
	  }

	  return newTable;

	}

	public Table concatTableAndTable(Table table)
	{
  	  if (noObj==0) return new Table();

	  if (noObj!=table.getNoObj())
  	  {
	    throw new IndexOutOfBoundsException(
			  "Table.concatTableAndTable - Index: ");
	  }

	  Table newTable = new Table();
	  newTable.createTable(noObj,noAttr+table.getNoAttr());

	  int attrCount=0;
    	  for (int i=0; i<noAttr; i++)
	  {
	    newTable.getAttr(attrCount).copy(getAttr(i));
	    attrCount++;
	  }
  	  for (int i=0; i<table.getNoAttr(); i++)
	  {
	    newTable.getAttr(attrCount).copy(getAttr(i));
	    attrCount++;
	  }

	  for (int i=0; i<noObj; i++)
	  {
	    attrCount=0;

	    for (int j=0; j<noAttr; j++)
	    {
	      newTable.setTable(i,attrCount,getTable(i,j));
	      attrCount++;
	    }

	    for (int j=0; j<table.getNoAttr(); j++)
	    {
	      newTable.setTable(i,attrCount,table.getTable(i,j));
	      attrCount++;
	    }
	  }

	  return newTable;
	}

        public Table mergeTableAndTable(Table table)
        {
          if (getNoAttr()!=table.getNoAttr())
          {
            throw new IndexOutOfBoundsException(
                          "Table.mergeTableAndTable - Index");
          }

          Table newTable = new Table();
          newTable.createTable(noObj+table.getNoObj(),noAttr);

          for (int i=0; i<noAttr; i++)
          {
            newTable.getAttr(i).copy(getAttr(i));
          }

          int objCount1 = 0;
          int objCount2 = 0;

          for (int i=0; i<newTable.getNoObj(); i++)
          {
            if (i<getNoObj())
            {
              for (int j=0; j<noAttr; j++) newTable.setTable(i,j,getTable(objCount1, j));
              objCount1++;
            }
            else
            {
              for (int j=0; j<noAttr; j++) newTable.setTable(i,j,table.getTable(objCount2, j));
              objCount2++;
            }
          }

          return newTable;
        }

	public Table extractTable(StringSack attrNameSack)
	{
	  if (noObj==0) return new Table();

	  Table newTable = new Table();
	  newTable.createTable(noObj,attrNameSack.size());

    	  for (int i=0; i<newTable.getNoAttr(); i++)
	  {
	    String name = attrNameSack.get(i);
	    newTable.setAttrName(i,name);

	    int posAttr=-1;
	    for (int j=0; j<noAttr; j++)
	    {
	      if (getAttrName(j).compareTo(name)==0)
	      {
		posAttr=j; break;
	      }
	    }

            if (posAttr==-1)
            {
                System.out.println("Atrybuty do wybrania: ");
                for (int k=0; k<attrNameSack.size(); k++)
                {
                  System.out.println("  "+attrNameSack.get(k));
                }

                System.out.println("Atrybuty z tablicy: ");
                for (int k=0; k<getNoAttr(); k++)
                {
                  System.out.println("  "+this.getAttrName(k));
                }
            }

	    if (posAttr==-1) throw new IndexOutOfBoundsException("Can't find attributes in source table: "+name);

	    newTable.getAttr(i).copy(getAttr(posAttr));

	    for (int j=0; j<noObj; j++)
	    {
	      newTable.setTable(j,i,getTable(j,posAttr));
	    }
	  }

	  return newTable;
	}

	public Table extractTableWithAttrValue(String attrName,int attrValue)
	{
	  if (noObj==0) return new Table();

	  Table newTable = new Table();

	  int selAttrPos = getAttrCode(attrName);
	  int objCount=0;
	  for (int i=0; i<noObj; i++)
	  {
	    if (getTable(i,selAttrPos)==attrValue) objCount++;
	  }

	  if (objCount==0) throw new IndexOutOfBoundsException("Can't find objects to extract table!");

	  newTable.createTable(objCount,noAttr-1);

	  int attrCount=0;
    	  for (int i=0; i<getNoAttr(); i++)
	  {
	    if (getAttrName(i).compareTo(attrName)!=0)
	    {
	      newTable.getAttr(attrCount).copy(getAttr(i));
	      attrCount++;
	    }
	  }

	  objCount=0;
    	  for (int i=0; i<noObj; i++)
	  {
	    if (getTable(i,selAttrPos)==attrValue) continue;

	    attrCount = 0;
	    for (int j=0; j<noAttr; j++)
	    {
	      if (getAttrName(j).compareTo(attrName)!=0)
	      {
	        newTable.setTable(objCount,attrCount,
		                  getTable(i,j));
	        attrCount++;
	      }
	    }

	    objCount++;
	  }

	  return newTable;
	}

       	public Table removeReplicas()
        throws InterruptedException
	{
          progress(0);

          if (getNoObj()==0)
          {
            throw new IndexOutOfBoundsException("Remove replicas cannot be used for empty table!");
          }

          boolean [] REPLICAS = new boolean [getNoObj()];

          for (int i=0; i<getNoObj(); i++) REPLICAS[i] = false;

          for (int i=0; i<getNoObj(); i++)
          {
            progress(i*90/getNoObj());

            if (REPLICAS[i]==false)
            {
              for (int j=i+1; j<getNoObj(); j++)
              {
                if (isEqualObj(i,j)) REPLICAS[j] = true;
              }
            }
          }

          int noNewObj = 0;
          for (int i=0; i<getNoObj(); i++)
          {
            if (REPLICAS[i]==false) noNewObj++;
          }


      	  Table newTable = new Table();
          newTable.createTable(noNewObj,noAttr);

	  for (int i=0; i<newTable.getNoAttr(); i++)
	  {
	    newTable.getAttr(i).copy(getAttr(i));
	  }

          int objCount = 0;
	  for (int i=0; i<noObj; i++)
	  {
            progress(90+i*10/getNoObj());

            if (REPLICAS[i]==false)
            {
              for (int j=0; j<noAttr; j++)
              {
                newTable.setTable(objCount,j,getTable(i,j));
              }
              objCount++;
            }
	  }

          progress(100);

          messageL("Removed "+(noObj-newTable.getNoObj())+" replicas!");

	  return newTable;
	}

       	public Table removeReplicasFast()
	{
          if (getNoObj()==0)
          {
            throw new IndexOutOfBoundsException("Remove replicas (fast method) cannot be used for empty table!");
          }

          IntWrap [] locArray = new IntWrap[noObj];

          for (int i=0; i<noObj; i++) locArray[i] = new IntWrap(i);

          System.out.println("Sorting rows by fast method...");

          Arrays.sort(locArray,
            new Comparator<IntWrap>()
              {
                public int compare(IntWrap a,IntWrap b)
                {
                  IntWrap o1 = (IntWrap)a;
                  IntWrap o2 = (IntWrap)b;

                  for(int i=0; i<noAttr; i++)
                  {
                    int val1 = getTable(o1.getValue(),i);
                    int val2 = getTable(o2.getValue(),i);

                    if (val1>val2) return -1;
                    else
                      if (val1<val2) return 1;
                  }
                  return 0;
                }
              });

            int noNewObj = 1;
            for (int i=1; i<noObj; i++)
            {
              int prevNum = locArray[i-1].getValue();
              int currNum = locArray[i].getValue();

              if (!isEqualObj(prevNum,currNum)) noNewObj++;
            }

            Table newTable = new Table();
            newTable.createTable(noNewObj,noAttr);

            for (int i=0; i<newTable.getNoAttr(); i++)
            {
              newTable.getAttr(i).copy(getAttr(i));
            }

            int objCount = 0;
            for (int i=0; i<noObj; i++)
            {
              if (i==0)
              {
                int currNum = locArray[i].getValue();
                for (int j=0; j<noAttr; j++)
                {
                  newTable.setTable(objCount,j,getTable(currNum,j));
                }
                objCount++;
              }
              else
              {
                  int prevNum = locArray[i-1].getValue();
                  int currNum = locArray[i].getValue();

                  if (!isEqualObj(prevNum,currNum))
                  {
                      for (int j=0; j<noAttr; j++)
                      {
                        newTable.setTable(objCount,j,getTable(currNum,j));
                      }
                      objCount++;
                  }
              }
            }

            messageL("Removed "+(noObj-newTable.getNoObj())+" replicas by fast method!");

            return newTable;
       }

       public Table removeMissingObj()
       {
         if (getNoObj()==0)
         {
           throw new IndexOutOfBoundsException("Remove missing objects!");
         }

         boolean [] MISSING_OBJ = new boolean [getNoObj()];

         for (int i=0; i<getNoObj(); i++) MISSING_OBJ[i] = false;

         for (int i=0; i<getNoObj(); i++)
         {
           boolean isMissing = true;
           for (int j=0; j<noAttr-1; j++)
           {
             if (getTable(i,j)!=Integer.MAX_VALUE) isMissing = false;
           }

           if (isMissing) MISSING_OBJ[i] = true;
         }

         int noNewObj = 0;
         for (int i=0; i<getNoObj(); i++)
         {
           if (MISSING_OBJ[i]==false) noNewObj++;
         }

         Table newTable = new Table();
         newTable.createTable(noNewObj,noAttr);

         for (int i=0; i<newTable.getNoAttr(); i++)
         {
           newTable.getAttr(i).copy(getAttr(i));
         }

         int objCount = 0;
         for (int i=0; i<noObj; i++)
         {
           if (MISSING_OBJ[i]==false)
           {
             for (int j=0; j<noAttr; j++)
             {
               newTable.setTable(objCount,j,getTable(i,j));
             }
             objCount++;
           }
         }

         //messageL("Removed "+(noObj-newTable.getNoObj())+" missing objects!");

         System.out.println("All objects "+noObj);
         System.out.println("Removed "+(noObj-newTable.getNoObj())+" missing objects!");

         return newTable;
       }

       public Table removeObjWithMissingDecision()
       {
         if (getNoObj()==0)
         {
           throw new IndexOutOfBoundsException("Remove objects with missing decision!");
         }

         boolean [] MISSING_OBJ = new boolean [getNoObj()];

         for (int i=0; i<getNoObj(); i++) MISSING_OBJ[i] = false;

         for (int i=0; i<getNoObj(); i++)
         {
           if (getTable(i,noAttr-1)==Integer.MAX_VALUE) MISSING_OBJ[i] = true;
         }

         int noNewObj = 0;
         for (int i=0; i<getNoObj(); i++)
         {
           if (MISSING_OBJ[i]==false) noNewObj++;
         }

         Table newTable = new Table();
         newTable.createTable(noNewObj,noAttr);

         for (int i=0; i<newTable.getNoAttr(); i++)
         {
           newTable.getAttr(i).copy(getAttr(i));
         }

         int objCount = 0;
         for (int i=0; i<noObj; i++)
         {
           if (MISSING_OBJ[i]==false)
           {
             for (int j=0; j<noAttr; j++)
             {
               newTable.setTable(objCount,j,getTable(i,j));
             }
             objCount++;
           }
         }

         //messageL("Removed "+(noObj-newTable.getNoObj())+" missing objects!");

         System.out.println("All objects "+noObj);
         System.out.println("Removed "+(noObj-newTable.getNoObj())+" objects with missing decision!");

         return newTable;
       }


	public void noDescPairBeatwenDecValues()
	{
	  int noDescPair = 0;
	  int noAllNotEqulPair = 0;

	  for (int i=0; i<getNoObj(); i++)
	  {
	    for (int j=0; j<i; j++)
	    {
	      if (getTable(i,getNoAttr()-1)!=getTable(j,getNoAttr()-1))
	      {
		noAllNotEqulPair++;

		boolean rowne=true;
		for (int k=0; k<getNoAttr()-1; k++)
		{
		  if (getTable(i,k)!=getTable(j,k))
		  {
		    rowne=false; break;
		  }
		}

	        if (rowne==false) noDescPair++;
	      }
	    }
	  }

//	  System.out.println("Parametr rozroniania par obiektow="+((float)noDescPair/noAllNotEqulPair));
	}




	public void splitTable(double percent,Table table1,Table table2)
        throws InterruptedException
	{
	    if ((percent<=0.0)||(percent>=1.0))
	    {
	      throw new IndexOutOfBoundsException("Bad split table parameter");
	    }

	    progress(0);

	    Random rand = new Random();

	    int noRandomObj1 = (int)(percent * noObj);
	    int noRandomObj2 = noObj - noRandomObj1;

	    table1.createTable(noRandomObj1,noAttr);

	    for (int i=0; i<table1.getNoAttr(); i++)
	    {
	      table1.getAttr(i).copy(getAttr(i));
	    }

	    table2.createTable(noRandomObj2,noAttr);

    	    for (int i=0; i<table2.getNoAttr(); i++)
	    {
	      table2.getAttr(i).copy(getAttr(i));
	    }


	    BinTreeIntWrap binTree = new BinTreeIntWrap();

	    int i=0;
	    while (i<noRandomObj1)
	    {
	      int r = rand.nextInt(noObj);
	      if (binTree.addElem(r)!=0) i++;
	    }

	    int count1=0;
	    int count2=0;

	    for (i=0; i<noObj; i++)
	    {
	      progress(i*100/noObj);

	      int first_tab = binTree.searchElem(i);

	      if (first_tab==1) //do tablicy pierwszej
	      {
		      for (int j=0; j<noAttr; j++)
		            table1.setTable(count1,j,getTable(i,j));
		      count1++;
	      }
	      else
	      {
		      for (int j=0; j<noAttr; j++)
			    table2.setTable(count2,j,getTable(i,j));
		      count2++;
	      }
	    }

	    progress(100);

	}

        public Table getRandomSubtable(double percent)
        {
            if ((percent<=0.0)||(percent>=1.0))
            {
              throw new IndexOutOfBoundsException("Bad parameter of subtable size: "+percent);
            }

            Random rand = new Random();

            int noRandomObj = (int)(percent * noObj);

            Table locTable = new Table();
            locTable.createTable(noRandomObj,noAttr);

            for (int i=0; i<locTable.getNoAttr(); i++)
            {
              locTable.getAttr(i).copy(getAttr(i));
            }

            BinTreeIntWrap binTree = new BinTreeIntWrap();

            int i=0;
            while (i<noRandomObj)
            {
              int r = rand.nextInt(noObj);
              if (binTree.addElem(r)!=0) i++;
            }

            int count=0;
            for (i=0; i<noObj; i++)
            {
              int first_tab = binTree.searchElem(i);

              if (first_tab==1) //do podtablicy
              {
                  for (int j=0; j<noAttr; j++)
                  {
                    locTable.setTable(count,j,getTable(i,j));
                  }
                  count++;
              }
            }

            return locTable;
        }

        public double getPositiveRegion()
        {
          return positiveRegion;
        }

	public double calculatePosRegion()
	{
          if (getNoObj()==0) return 0.0;

	  int noPosObj = 0;

	  for (int i=0; i<getNoObj(); i++)
	  {

	    boolean IS=true;

	    for (int j=0; j<getNoObj(); j++)
	    {
	      if (i==j) continue;

	      boolean rowne=true;
	      for (int k=0; k<getNoAttr()-1; k++)
	      {
		if (getTable(i,k)!=getTable(j,k))
		{
		  rowne=false; break;
		}
	      }

	      if ((rowne)&&(getTable(i,getNoAttr()-1)!=getTable(j,getNoAttr()-1)))
	      {
		IS=false; break;
	      }
	    }

	    if (IS) noPosObj++;
	  }

	  return (double)noPosObj/getNoObj();

	}

        public double calculatePositiveRegion()
        throws InterruptedException
        {
          progress(0);

          if (getNoObj()==0) return 0.0;

          int noPosObj = 0;

          for (int i=0; i<getNoObj(); i++)
          {

            //if (i/1000*1000==i) System.out.println("i="+i);

            progress(i*100/getNoObj());

            boolean IS=true;

            for (int j=0; j<getNoObj(); j++)
            {
              if (i==j) continue;

              boolean rowne=true;
              for (int k=0; k<getNoAttr()-1; k++)
              {
                if (getTable(i,k)!=getTable(j,k))
                {
                  rowne=false; break;
                }
              }

              if ((rowne)&&(getTable(i,getNoAttr()-1)!=getTable(j,getNoAttr()-1)))
              {
                IS=false; break;
              }
            }

            if (IS) noPosObj++;
          }

          positiveRegion = (double)noPosObj/getNoObj();

          //messageL("Positive region for table "+this.getName()+": "+positiveRegion);

          progress(100);

          return positiveRegion;
        }


        public double calculatePositiveRegion(StringSack negativeRegionDecisions)
        throws InterruptedException
        {
          progress(0);

          if (getNoObj()==0) return 0.0;

          int noPosObj = 0;

          for (int i=0; i<getNoObj(); i++)
          {

            //if (i/1000*1000==i) System.out.println("i="+i);

            progress(i*100/getNoObj());

            StringSack locNegRegion = new StringSack();

            boolean IS=true;

            for (int j=0; j<getNoObj(); j++)
            {
              if (i==j) continue;

              boolean rowne=true;
              for (int k=0; k<getNoAttr()-1; k++)
              {
                if (getTable(i,k)!=getTable(j,k))
                {
                  rowne=false; break;
                }
              }

              if ((rowne)&&(getTable(i,getNoAttr()-1)!=getTable(j,getNoAttr()-1)))
              {
                IS=false;
                locNegRegion.addNoEqual(getTableString(j,getNoAttr()-1));
              }
            }

            if (IS) noPosObj++;
            else
            {
              locNegRegion.addNoEqual(getTableString(i,getNoAttr()-1));

              for (int k=0; k<locNegRegion.size(); k++)
              {
                negativeRegionDecisions.addNoEqual(locNegRegion.get(k));
              }
            }

          }

          positiveRegion = (double)noPosObj/getNoObj();

          //messageL("Positive region for table "+this.getName()+": "+positiveRegion);

          progress(100);

          return positiveRegion;
        }


        public double calculatePositiveRegionAndShowObjects()
        throws InterruptedException
        {
          progress(0);

          if (getNoObj()==0) return 0.0;

          int noPosObj = 0;

          for (int i=0; i<getNoObj(); i++)
          {

            //if (i/1000*1000==i) System.out.println("i="+i);

            progress(i*100/getNoObj());

            boolean IS=true;

            for (int j=0; j<getNoObj(); j++)
            {
              if (i==j) continue;

              boolean rowne=true;
              for (int k=0; k<getNoAttr()-1; k++)
              {
                if (getTable(i,k)!=getTable(j,k))
                {
                  rowne=false; break;
                }
              }

              if ((rowne)&&(getTable(i,getNoAttr()-1)!=getTable(j,getNoAttr()-1)))
              {
                IS=false; break;
              }
            }

            if (IS) noPosObj++;
            else
            {
              System.out.print("ATT: ");
              for (int k=0; k<getNoAttr(); k++)
              {
                System.out.print(this.getAttrName(k)+" ");
              }
              System.out.println();

              System.out.print("OBJ: ");
              for (int k=0; k<getNoAttr(); k++)
              {
                System.out.print(getTableString(i,k)+" ");
              }
              System.out.println();
            }
          }

          positiveRegion = (double)noPosObj/getNoObj();

          //messageL("Positive region for table "+this.getName()+": "+positiveRegion);

          progress(100);

          return positiveRegion;
        }


        public Table extractConsistentTable()
        {
          Table newTable = new Table();

          if (getNoObj()==0) return newTable;

          boolean [] OBJ = new boolean[getNoObj()];
          for (int i=0; i<getNoObj(); i++) OBJ[i] = true;

          int noPosObj = getNoObj();

          for (int i=0; i<getNoObj(); i++)
          {
            if (i/1000*1000==i) System.out.println("i="+i);

            boolean IS=true;

            for (int j=0; j<getNoObj(); j++)
            {
              if (i==j) continue;

              boolean rowne=true;
              for (int k=0; k<getNoAttr()-1; k++)
              {
                if (getTable(i,k)!=getTable(j,k))
                {
                  rowne=false; break;
                }
              }

              if ((rowne)&&(getTable(i,getNoAttr()-1)!=getTable(j,getNoAttr()-1)))
              {
                IS=false; break;
              }
            }

            if (!IS)
            {
              OBJ[i] = false;
              noPosObj--;
            }
          }

          if (noPosObj==0) return newTable;

	  newTable.createTable(noPosObj,noAttr);

	  for (int i=0; i<newTable.getNoAttr(); i++)
	  {
	    newTable.getAttr(i).copy(getAttr(i));
	  }

          int objNum = 0;
	  for (int i=0; i<noObj; i++)
	  {
            if (OBJ[i])
            {
              for (int j=0; j<noAttr; j++)
              {
                newTable.setTable(objNum,j,getTable(i,j));
              }
              objNum++;
            }
	  }

          return newTable;
        }


	public void searchMaxCodes(int [] maxCodes)
	{
	  for (int i=0; i<noAttr-1; i++)
	  {
	    int maxVal = Integer.MIN_VALUE;

	    for (int j=0; j<noObj; j++)
	    {
	      int locCode = getTable(j,i);

	      if (locCode==Integer.MAX_VALUE) continue;

	      if (locCode>maxVal)
	      {
		maxVal = locCode;
	      }
	    }

	    maxCodes[i] = maxVal+1;
	  }
	}

	public void changeNullValuesCodes(int [] maxCodes)
	{
	  for (int i=0; i<noAttr-1; i++)
	  {
	    for (int j=0; j<noObj; j++)
	    {
	      if (getTable(j,i)==Integer.MAX_VALUE)
	      {
		setTable(j,i,maxCodes[i]);
	      }
	    }
	  }
	}

	public void insertDecAttr(String newDecAttrName,Attr newAttr)
	{
	  if (noObj==0)
	  {
	    throw new IndexOutOfBoundsException("Table is empty!");
	  }

	  Table newTable = new Table();
	  newTable.createTable(noObj,noAttr+1);

	  for (int i=0; i<newTable.getNoAttr()-1; i++)
	  {
	    newTable.getAttr(i).copy(getAttr(i));
	  }

          newTable.setAttr(newTable.getNoAttr()-1,newAttr);

          if (newDecAttrName!=null)
          {
            newTable.setAttrName(newTable.getNoAttr()-1,newDecAttrName);
          }

	  for (int i=0; i<noObj; i++)
	  {
	    for (int j=0; j<noAttr; j++)
	    {
	      newTable.setTable(i,j,getTable(i,j));
	    }
	    newTable.setTable(i,newTable.getNoAttr()-1,Integer.MAX_VALUE);
	  }

	  copy(newTable);

	  newTable = null;

	}

        public void setNewDecision(String newDecAttrName)
        {
          int pos = getAttrCode(newDecAttrName);

          for (int i=0; i<this.getNoObj(); i++)
          {
            int oldDecVal = getTable(i,getNoAttr()-1);
            int newDecVal = getTable(i,pos);

            int elem_num = (i * noAttr) + noAttr-1;
	    table[elem_num]=newDecVal;

            elem_num = (i * noAttr) + pos;
	    table[elem_num]=oldDecVal;
          }

          Attr oldDecAttr = attributes[getNoAttr()-1];
          Attr newDecAttr = attributes[pos];

          attributes[getNoAttr()-1] = newDecAttr;
          attributes[pos] = oldDecAttr;

        }

        public int joinValuesOfAttribute(int selAttrNum,
                                         IntSack codeValueSetToJoin,
                                         String nameOfNewValue)
        {
          int codeNewValue = addWordToDictio(nameOfNewValue);

          int noObjWithNewValue = 0;

          if (getAttr(selAttrNum).getType()==Attr.SYMBOLIC)
          {
            for (int i=0; i<getNoObj(); i++)
            {
              int locCode = getTable(i,selAttrNum);
              if (codeValueSetToJoin.check(locCode))
              {
                setTable(i,selAttrNum,codeNewValue);
                noObjWithNewValue++;
              }
            }
          }
          else //NUMERIC
          {
            for (int i=0; i<getNoObj(); i++)
            {
              int locCode = getTable(i,selAttrNum);
              if (codeValueSetToJoin.check(locCode))
              {
                setTable(i,selAttrNum,codeNewValue);
                noObjWithNewValue++;
              }
              else
              {
                int codeLocValue = addWordToDictio(getTableString(i,selAttrNum));
                setTable(i,selAttrNum,codeLocValue);
              }
            }

          }

          this.getAttr(selAttrNum).setSymbolic(); //teraz (po sklejeniu wartosci) bedzie juz to atrybut symboliczny

          return noObjWithNewValue;

        }

        public Table removeObjectsByAttrNumberAndValues(int selAttrNum,
                                                        IntSack codeValueSetToRemove)
        {

          if (noObj==0) return new Table();

          int noNewObj = 0;
          for (int i=0; i<getNoObj(); i++)
          {
            int locCode = getTable(i,selAttrNum);
            if (!codeValueSetToRemove.check(locCode))
            {
              noNewObj++;
            }
          }

          if (noNewObj==0)
          {
            return new Table();
          }

          Table newTable = new Table();
          newTable.createTable(noNewObj,getNoAttr());

          for (int i=0; i<newTable.getNoAttr(); i++)
          {
            newTable.getAttr(i).copy(getAttr(i));
          }

          int objCount = 0;
          for (int i=0; i<getNoObj(); i++)
          {
            int locCode = getTable(i,selAttrNum);
            if (!codeValueSetToRemove.check(locCode))
            {
              for (int j=0; j<getNoAttr(); j++)
              {
                newTable.setTable(objCount,j,getTable(i,j));
              }
              objCount++;
            }
          }

          return newTable;

        }


  public static void main(String[] args)
  {


  }


}

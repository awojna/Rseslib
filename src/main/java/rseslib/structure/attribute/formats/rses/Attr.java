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


package rseslib.structure.attribute.formats.rses;


/**
 * Rough Set Library
 *
 *
 */


import java.util.*;
import java.io.*;

public class Attr
{
  static public boolean SYMBOLIC = true;
  static public boolean NUMERIC = false;

  private String name;
  private boolean type;
  private int precision;

  public Attr()
  {
    name = new String("no_name");
    type = Attr.SYMBOLIC;
    precision = Integer.MAX_VALUE;
  }

  public Attr(String newName,boolean t,int prec)
  {
    name = new String(Element.addQuotationMarks(newName));

    if (t==Attr.SYMBOLIC)
    {
      type = Attr.SYMBOLIC;
      precision = Integer.MAX_VALUE;
    }
    else
    {
      type = Attr.NUMERIC;
      precision = prec;
    }
  }

  public Attr(Attr a)
  {
    name = new String(a.getName());
    type = a.getType();
    precision = a.getPrec();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String newName)
  {
    name = new String(Element.addQuotationMarks(newName));
  }

  public void setSymbolic()
  {
    type = Attr.SYMBOLIC;
    precision = Integer.MAX_VALUE;
  }

  public void setNumeric(int prec)
  {
    type = Attr.NUMERIC;
    precision = prec;
  }

  public boolean getType()
  {
    return type;
  }

  public int getPrec()
  {
    return precision;
  }

  public String getStringValue(int value)
  {
    if (value==Integer.MAX_VALUE) return MissingCompleter.MISSING_TEXT;

    if (type==Attr.NUMERIC)
    {
      if (precision==0)
      {
	return Integer.toString(value);
      }
      else
      {
        if (precision>0)
        {
           double d = (double)value / Math.pow(10,precision);

           String text = Element.formatDouble(d,precision);

           String textResult = new String();
           for (int i=0; i<text.length(); i++)
           {
             char ch = text.charAt(i);
             if ((ch!=',')&&(ch!=' '))
             {
               textResult = textResult + ch;
             }
           }

           return textResult;
        }
        else
        {
          double d = (double)value / Math.pow(10,precision);
          return Double.toString(d);
        }
      }
    }
    else //symboliczny
    {
      return Element.getWordFromDictio(value);
    }
  }

  public double getDoubleValueForNumericAttr(int code)
  {
    if (type==Attr.SYMBOLIC)
    {
      throw new IndexOutOfBoundsException("Double value can't be computed for symbolic attribute: "+name);
    }

    if (code==Integer.MAX_VALUE) return (double)code;

    if (precision==0)
    {
      return (double)code;
    }
    else
    {
      return (double)code / Math.pow(10,precision);
    }
  }

/*
  public int getCodeForDouble(double d)
  {
    if (type==Attr.SYMBOLIC)
    {
      throw new IndexOutOfBoundsException("Code can't be computed for symbolic attribute!");
    }

    return (int)(d * Math.pow(10,precision));
  }
*/

  public int getCodeFromDouble(double value)
  {
    if (type==Attr.SYMBOLIC)
    {
      throw new IndexOutOfBoundsException("Code for double value cannot be computed for symbolic attribute!");
    }

    if (value==Double.MAX_VALUE) return Integer.MAX_VALUE;

    if (precision==0)
    {
      return (int)value;
    }
    else
    {
      return (int) (value * Math.pow(10,precision));
    }
  }


  public int getIntValue(String text)
  {
    int key = Integer.MAX_VALUE;

    if (MissingCompleter.isMissing(text)) return key;

    try
    {
      if (type==Attr.NUMERIC)
      {
	if (precision==0)
	{
          double d = Double.parseDouble(text);
	  key =  (int)d;
	  //key =  Integer.parseInt(text);
	}
	else
	{
	  double d = Double.parseDouble(text);
	  key = (int) (d * Math.pow(10,precision));
	}
      }
      else //symboliczny
      {
	key = Element.addWordToDictio(text);
      }
    }
    catch (NumberFormatException e)
    {
        throw new NumberFormatException("Can't convert text <"+text+"> to number! (Attr.getIntValue()) in attribute: "+name);
    }

    return key;
  }

  public IntSack getIntValues(String text)
  {
    IntSack intSack = new IntSack();

    if (type==Attr.SYMBOLIC)
    {
      intSack.add(getIntValue(text));
      return intSack;
    }

    StringTokenizer st = new StringTokenizer(text,"|\n\r\f",true);

    while (st.hasMoreTokens())
    {
      String token = st.nextToken();
      intSack.add(getIntValue(token));
    }

    return intSack;
  }


  public void copy(Attr attr)
  {
    name = attr.getName();
    type = attr.getType();
    precision = attr.getPrec();
  }

  public boolean equals(Attr a)
  {
    if ((type!=a.getType())||
        (precision!=a.getPrec())||
        (name.compareTo(a.getName())!=0))
    {
      return false;
    }
    else return true;
  }

  public String toString()
  {
    Boolean b = new Boolean(type);
    String text = new String (name);
    text = text + " " + b.toString() + " " + Integer.toString(precision);
    return text;
  }

  public String toStringGUI()
  {
    String text = new String (name);
    return text;
  }

  public void saveToFile(PrintWriter pw)
  {
    pw.print(name+" ");

    if (type==Attr.SYMBOLIC)
    {
      pw.print("symbolic");
    }
    else
    {
      pw.print("numeric "+precision);
    }
    pw.println();
  }

  public void loadFromFile(BufferedReader br) throws IOException
  {
    String line = br.readLine();

    StringTokenizer st = new StringTokenizer(line);

    name = Element.addQuotationMarks(Element.readNextToken(st,null));

    String token = st.nextToken();
    String TOKEN = token.toUpperCase();

    if (TOKEN.compareTo("SYMBOLIC")==0) setSymbolic();
    else
    {
      if (TOKEN.compareTo("NUMERIC")!=0)
      {
	throw new IOException("Expected attribute type as word: 'symbolic' or 'numeric'! - " + TOKEN);
      }

      String textPrecision = st.nextToken();

      int attrPrecision=Integer.MAX_VALUE;

      try
      {
	attrPrecision = Integer.parseInt(textPrecision);
      }
      catch (NumberFormatException e)
      {
	throw new IOException("Bad precision of attribute in line!");
      }

      setNumeric(attrPrecision);
    }
  }


  public void loadFileWithoutPrecisionForNumeric(BufferedReader br,Table trainTab) throws IOException
  {
    String line = br.readLine();

    StringTokenizer st = new StringTokenizer(line);

    name = Element.addQuotationMarks(Element.readNextToken(st,null));

    String token = st.nextToken();
    String TOKEN = token.toUpperCase();

    if (TOKEN.compareTo("SYMBOLIC")==0) setSymbolic();
    else
    {
      if (TOKEN.compareTo("NUMERIC")!=0)
      {
        throw new IOException("Expected attribute type as word: 'symbolic' or 'numeric'! - "+TOKEN);
      }

      int code = trainTab.getAttrCode(name);
      int attrPrecision = trainTab.getAttrPrec(code);
      setNumeric(attrPrecision);
    }
  }

}
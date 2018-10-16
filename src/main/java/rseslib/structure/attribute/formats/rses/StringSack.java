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


package rseslib.structure.attribute.formats.rses;

/**
 * Rough Set Library
 *
 * @author Jan Bazan
 *
 */

import java.util.*;
import java.io.*;

public class StringSack
{

  ArrayList<String> sack;

  public StringSack()
  {
    sack = new ArrayList<String>();
  }

  public StringSack(String s)
  {
    String ns = new String(Element.addQuotationMarks(s));
    sack = new ArrayList<String>();
    add(ns);
  }

  public StringSack(StringSack s)
  {
    sack = new ArrayList<String>();
    for (int i=0; i<s.size(); i++)
    {
      add(s.get(i));
    }
  }

/*  public void addWithoutQuationMarks(String s)
  {
    sack.add(s);
  }
*/
  public void add(String s)
  {
    if (s==null) return;
    sack.add(new String(Element.addQuotationMarks(s)));
  }

  public void addNotMISSING(String s)
  {
    if (!s.equals("MISSING")) add(s);
  }


  public boolean addNoEqual(String s)
  {
    if (s==null) return false;

    Element.addQuotationMarks(s);

    for (int i=0; i<size(); i++)
    {
      String ls = get(i);
      if (ls.compareTo(s)==0) return false;
    }
    add(s);
    return true;
  }


  public String get(int index)
  {
    if ((index<0)||(index>=size()))
    {
      throw new IndexOutOfBoundsException("Index: "+index+", Size: "+ size());
    }
    else
    {
      return (String)sack.get(index);
    }
  }

  public String getWithoutQuationMarks(int index)
  {
    if ((index<0)||(index>=size()))
    {
      throw new IndexOutOfBoundsException("Index: "+index+", Size: "+ size());
    }
    else
    {
      return Element.removeQuotationMarks((String)sack.get(index));
    }
  }


  public void set(int index,String s)
  {
    if ((index<0)||(index>=size()))
    {
      throw new IndexOutOfBoundsException("Index: "+index+", Size: "+ size());
    }
    else
    {
      String ns = new String(Element.addQuotationMarks(s));
      sack.set(index,ns);
    }
  }


  public void remove(int index)
  {
    if ((index<0)||(index>=size()))
    {
      throw new IndexOutOfBoundsException("Index: "+index+", Size: "+ size());
    }
    else
    {
      sack.remove(index);
    }
  }


  public void remove(String s)
  {
    StringSack newSack = new StringSack();

    boolean removed = false;

    for (int i=0; i<size(); i++)
    {
      String elem = get(i);
      if (!elem.equals(s)) newSack.add(elem);
      else
        removed = true;
    }

    if (!removed) throw new IndexOutOfBoundsException("Cannot find element in StringSack: "+s);

    sack.clear();

    for (int i=0; i<newSack.size(); i++)
    {
      sack.add(newSack.get(i));
    }
  }


  public void changePositionOfElem(int n1,int n2)
  {
    if ((n1<0)||(n1>=size())||(n2<0)||(n2>=size()))
    {
      throw new IndexOutOfBoundsException("Bad index in changePosition!");
    }

    String s_n1 = (String)sack.get(n1);
    String s_n2 = (String)sack.get(n2);

    sack.set(n1,s_n2);
    sack.set(n2,s_n1);
  }



  public int size()
  {
    return sack.size();
  }

  public void clear()
  {
    sack.clear();
  }

  public void copy(StringSack s)
  {
    clear();
    for (int i=0; i<s.size(); i++)
    {
      add(s.get(i));
    }
  }


  public boolean checkString(String s)
  {
    String ns = new String(Element.addQuotationMarks(s));

    for (int i=0; i<size(); i++)
    {
      String elem = get(i);
      if (elem.compareTo(ns)==0) return true;
    }

    return false;
  }

  public int getPositionOfString(String s)
  {
    String ns = new String(Element.addQuotationMarks(s));

    for (int i=0; i<size(); i++)
    {
      String elem = get(i);
      if (elem.compareTo(ns)==0) return i;
    }

    throw new IndexOutOfBoundsException("Cannot find string: "+s+" in string sack!");

    //return 0;
  }


  public boolean equals(StringSack s)
  {
    if (size()!=s.size()) return false;

    for (int i=0; i<size(); i++)
    {
      String elem = get(i);
      boolean iS=false;
      for (int j=0; j<s.size(); j++)
      {
        if (elem.compareTo(s.get(j))==0)
	{
	  iS = true; break;
	}
      }
      if (!iS) return false;
    }
    return true;
  }

  public int compareTo(StringSack second)
  {
    //Ma zwracac:
    //	-1 jesli First  <  Second
    //	 0 jesli First ==  Second
    //	 1 jesli First  >  Second

    if ((size()==1)&&(second.size()==1))
    {
      return get(0).compareTo(second.get(0));
    }
    else
    {
      if (size()<second.size()) return -1;
      else
        if (size()>second.size()) return 1;
	else
	return 0;
    }
  }


  public void qSort()
  {
    String [] myArray = sack.toArray(new String[0]);
    Arrays.sort(myArray,new
      Comparator<String>()
      {
        public int compare(String a,String b)
        {
          String e1 = a;
          String e2 = b;
          return e1.compareTo(e2);
        }
      });

    for (int i=0; i<myArray.length; i++)
    {
      sack.set(i,myArray[i]);
    }
  }


  public String toString()
  {
    if (size()==0) return new String();

    if (size()==1) return new String(get(0));

    String text = new String();

    for (int i=0; i<size(); i++)
    {
      text = text + get(i);
      if (i<size()-1) text = text + ",";
    }
    return text;
  }

  public String toStringOR()
  {
    if (size()==0) return new String();

    if (size()==1) return new String(get(0));

    String text = new String();

    for (int i=0; i<size(); i++)
    {
      text = text + get(i);
      if (i<size()-1) text = text + "|";
    }
    return text;
  }

  public void saveToFile(PrintWriter pw)
  {
    saveToFile(pw,null);
  }

  public void saveToFile(PrintWriter pw,String textTitle)
  {
    if (textTitle==null) pw.println(size());
    else pw.println(textTitle+" "+size());

    for (int i=0; i<size(); i++)
    {
      pw.println(get(i));
    }
  }

  public void loadFromFile(BufferedReader br)
  throws IOException
  {
    loadFromFile(br,null);
  }

  public void loadFromFile(BufferedReader br,String textTitle)
  throws IOException
  {
      String line = Element.readNextLine(br,null);
      StringTokenizer st = new StringTokenizer(line);

      if (textTitle!=null)
      {
        String title = Element.readNextToken(st,null);
        String TITLE = title.toUpperCase();
        if (TITLE.compareTo(textTitle)!=0)
        {
          throw new IOException("Expected key word: '"+textTitle+"'!");
        }
      }

      String mySizeString = Element.readNextToken(st,null);
      int mySize = Integer.parseInt(mySizeString);

      for (int i=0; i<mySize; i++)
      {
        line = Element.readNextLine(br,null);
        st = new StringTokenizer(line);
        String token = Element.readNextToken(st,null);
	add(token);
      }
  }

  public void importFromFile(String fName) throws IOException
  {
      clear();

      File inFile = new File(fName);

      if (!inFile.exists())
	      throw new IOException("Can't open file with strings: " + fName);

      FileReader fr = new FileReader(inFile);
      BufferedReader br = new BufferedReader(fr);

      while (true)
      {
	String text = null;

	while(true)
	{
	  String line = br.readLine();

	  if ((line==null)||(line.length()==0))
	  {
	    br.close(); return;
	  }

	  StringTokenizer st = new StringTokenizer(line);

	  text = Element.readNextToken(st,null);

	  if (text.charAt(0)!='#') break;

	}

	add(text);
      }

      //br.close(); nie jest tu potrzebne bo wyskakuje za pomoca return
  }

  //======= CALE PLIKI TEKSTOWE ============

  public void loadTextFile(String fName)
  throws IOException
  {
      File inFile = new File(fName);

      if (!inFile.exists())
              throw new IOException("Can't open text file: " + fName);

      FileReader fr = new FileReader(inFile);
      BufferedReader br = new BufferedReader(fr);

      String line = br.readLine();

      while (line!=null)
      {
        add(line);
        line = br.readLine();
      }

      br.close();
  }

  public void saveTextFile(String fName)
  throws IOException
  {
    FileOutputStream fos = new FileOutputStream(fName);
    OutputStreamWriter osw = new OutputStreamWriter(fos);
    PrintWriter pw = new PrintWriter(osw);

    for (int i=0; i<size(); i++)
    {
      pw.println(getWithoutQuationMarks(i));
    }

    pw.close();
  }



}


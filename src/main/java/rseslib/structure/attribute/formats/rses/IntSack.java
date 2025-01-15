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


public class IntSack  extends Element
{

    /**
     * The array buffer into which the elements of the <tt>IntSack</tt> are stored.
     * The capacity of the <tt>IntSack</tt> is the length of this array buffer.
     */

    ArrayList<IntWrap> sack;


    public ArrayList getArrayList() { return sack; };

    /**
  * Constructs an empty list of integer numbers
  */
    public IntSack()
    {
      sack = new ArrayList<IntWrap>();
    }

    public IntSack(IntSack s)
    {
      sack = new ArrayList<IntWrap>();
      for (int i=0; i<s.size(); i++)
      {
	add(s.get(i));
      }
    }

    /**
     * Removes all of the elements from array.  The array will
     * be empty after this call returns.
     */

    public void clear()
    {
	    sack.clear();
    }

    /**
     * Copies the elements of another array to this array.
     * Call this member function to overwrite the elements of this array with the elements of
     * another array.
     *
     * @param src source of the elements to be copied to an array.
     */

    public void copy(IntSack src)
    {
	    sack.clear();

	    for (int i=0; i<src.size(); i++)
	    {
		    add(src.get(i));
	    }
    }


    /**
     * Appends the elements of another array to this array.
     *
     * @param src source of the elements to be copied to an array.
     */

    public void append(IntSack src)
    {
	    for (int i=0; i<src.size(); i++)
	    {
		    add(src.get(i));
	    }
    }

    public void appendNoEqual(IntSack src)
    {
	    for (int i=0; i<src.size(); i++)
	    {
		    addNoEqual(src.get(i));
	    }
    }


    /**
  * Appends the specified integer number to the end of this array.
  *
  * @param integer number to append
  */


    public void add(int number)
    {
	    IntWrap I = new IntWrap(number);
	    sack.add(I);
    }

    /**
  * Appends the specified integer number to the end of this array, but if and only if
  * the specified integer number cannot be founded in this array.
  *
  * @param number to append
  */


    public boolean addNoEqual(int number)
    {
	    for (int i=0; i<sack.size(); i++)
	    {
		    IntWrap I = (IntWrap)sack.get(i);
		    if (I.getValue()==number) return false;
	    }
	    add(number);

            return true;
    }

    /**
  * Returns size of the <tt>IntSack</tt> (the number of elements it contains).
  *
  */

    public int size()
    {
	    return sack.size();
    }

    /**
  * Returns the element at the specified position in this array.
  *
  * @param  index index of element to return.
  * @return the element at the specified position in this array.
  * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
  * 		  &lt; 0 || index &gt;= size())</tt>.
  */
    public int get(int index)
    {
	    if ((index<0)||(index>=size()))
	    {
		    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size());
	    }
	    else
	    {
		    IntWrap I = (IntWrap)sack.get(index);
		    return I.getValue();
	    }
    }

    public void set(int index,int newVal)
    {
	    if ((index<0)||(index>=size()))
	    {
		    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size());
	    }
	    else
	    {
		    IntWrap I = (IntWrap)sack.get(index);
		    I.setValue(newVal);
	    }
    }

    public void remove(int index)
    {
      if ((index<0)||(index>=size()))
      {
	throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size());
      }
      else
      {
	sack.remove(index);
      }
    }

    public void removeValue(int value)
    {
      IntSack sackLoc = new IntSack();

      for(int i=0; i<size(); i++)
      {
        int curVal = get(i);
	if (curVal!=value)
	{
          sackLoc.add(curVal);
	}
      }

      clear();

      for(int i=0; i<sackLoc.size(); i++)
      {
        add(sackLoc.get(i));
      }

      sackLoc=null;
    }


    public void inc(int index)
    {
      IntWrap I = (IntWrap)sack.get(index);
      I.incValue();
    }

    public boolean check(int value)
    {
      for(int i=0; i<size(); i++)
      {
	if (get(i)==value) return true;
      }
      return false;
    }

  public boolean equals(IntSack s)
  {
    if (size()!=s.size()) return false;

    for (int i=0; i<size(); i++)
    {
      int elem = get(i);
      boolean iS=false;
      for (int j=0; j<s.size(); j++)
      {
        if (elem==s.get(j))
	{
	  iS = true; break;
	}
      }
      if (!iS) return false;
    }
    return true;
  }


    public void write()
    {
      for(int i=0; i<size(); i++)
      {
	write(get(i)+" ");
      }

      writeln("");
    }

    public void saveToFile(PrintWriter pw)
    {
      pw.print(size()+"  ");

      for(int i=0; i<size(); i++)
      {
	pw.print(get(i)+" ");
      }

      pw.println();
    }

    public void loadFromFile(BufferedReader br)
    throws IOException
    {
      clear();

      String line = br.readLine();
      StringTokenizer st = new StringTokenizer(line);

      String sizeS = st.nextToken();

      int sizeI;

      try
      {
	sizeI = Integer.parseInt(sizeS);
      }
      catch (NumberFormatException e)
      {
	throw new IOException("Bad format of file with integer sack!");
      }

      for(int i=0; i<sizeI; i++)
      {
	String valS = st.nextToken();

	int val;

	try
	{
	   val = Integer.parseInt(valS);
	}
	catch (NumberFormatException e)
	{
	  throw new IOException("Bad format of file with integer sack!");
	}

	add(val);
      }
    }

  public void loadTextFile(String fName)
  throws IOException
  {
      File inFile = new File(fName);

      if (!inFile.exists())
              throw new IOException("Can't open text file: " + fName);

      FileReader fr = new FileReader(inFile);
      BufferedReader br = new BufferedReader(fr);

      loadFromFile(br);

      br.close();
  }



    //===== SORTOWANIE =====================

    int compare(int n1,int n2)
    {
      //  0 -> identyczne
      //  1 -> obj1 < obj2
      // -1 -> obj1 > obj2

      int val1 = get(n1);
      int val2 = get(n2);

      if (val1<val2) return 1;
      else
	if (val1>val2) return -1;
	else return 0;
    }

    void swap(int n1,int n2)
    {
      int pom = get(n1);
      set(n1,get(n2));
      set(n2,pom);
    }

    private void qSort(int left,int right)
    {
      if (left<right)
      {
	int m=left;
	for(int i=left+1;i<=right;i++)
	{
	  if (compare(i,left)==1) swap(++m,i);
	}
	swap(left,m);
	qSort(left,m-1);
	qSort(m+1,right);
      }
    }

    public void qSort()
    {
      if (size()>1) qSort(0,size()-1);
    }

    //====== KONIEC SORTOWANIA
}
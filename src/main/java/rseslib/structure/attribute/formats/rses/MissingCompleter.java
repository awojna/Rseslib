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


package rseslib.structure.attribute.formats.rses;

/**
 * Rough Set Library
 *
 * @author Jan Bazan
 *
 */



public class MissingCompleter  extends Element
{
  static public byte REMOVE_NULL_VALUES = 0;
  static public byte MOST_COMMON_OR_MEAN = 1;
  static public byte CONCEPT_MOST_COMMON_OR_MEAN = 2;

  static public String MISSING_TEXT = "MISSING";
  static public int MISSING_VALUE = Integer.MAX_VALUE;

  public MissingCompleter()
  {
     super.setType(Element.UN_KNOWN);
  }

  static public boolean isMissing(String text)
  {
    if (text.toUpperCase().compareTo(MISSING_TEXT)==0) return true;
    else
    {
      if (text.toUpperCase().compareTo("NULL")==0) return true;
      else
      {
        if (text.compareTo("?")==0) return true;
        else
        {
          return false;
        }
      }
    }
  }

  static public int getNoMissingValues(Table table,
                                       DoubleWrap missingPercent)
  {
    int noNullVal = 0;
    int noAllVal = 0;

    for (int i=0; i<table.getNoObj(); i++)
    {
      for (int j=0; j<table.getNoAttr(); j++)
      {
        noAllVal++;
        if (table.getTable(i,j)==Integer.MAX_VALUE)
        {
          noNullVal++;
        }
      }
    }

    double percent = 0.0;
    if (noAllVal>0) percent = (double)(noNullVal*100)/noAllVal;
    missingPercent.setValue(percent);

    return noNullVal;
  }

  static public void getMaxAndMinValueOfAttr(Table table,int attrNum,
                                             DoubleWrap maxValWrap,
                                             DoubleWrap minValWrap)
  {
    double max = (double)Integer.MIN_VALUE;
    double min = (double)Integer.MAX_VALUE;

    for (int i=0; i<table.getNoObj(); i++)
    {
      int val = table.getTable(i,attrNum);
      if (val!=Integer.MAX_VALUE)
      {
        double dval = table.getAttr(attrNum).getDoubleValueForNumericAttr(val);
        if (dval>max) max = dval;
        if (dval<min) min = dval;
      }
    }

    maxValWrap.setValue(max);
    minValWrap.setValue(min);
  }

  static public void getTunesForHistogram(Table table,int attrNum,
                                          double [] nodes,
                                          int [] tunes)
  {
    for (int i=0; i<nodes.length; i++)
    {
      tunes[i]=0;
    }

    for (int i=0; i<table.getNoObj(); i++)
    {
        int val = table.getTable(i,attrNum);
        if (val!=Integer.MAX_VALUE)
        {
          double dval = table.getAttr(attrNum).getDoubleValueForNumericAttr(val);
          for (int j=0; j<nodes.length; j++)
          {
            if ((j==nodes.length-1)||(dval<nodes[j])) { tunes[j]++; break; }
          }
        }
    }
  }



  static public double calculateMeanForAttr(Table table,int attrNum,
                                            DoubleWrap minWrap,
                                            DoubleWrap maxWrap)
  {
    double sum = 0.0;
    int licznik=0;

    double min = (double)Long.MAX_VALUE;
    double max = (double)Long.MIN_VALUE;

    for (int i=0; i<table.getNoObj(); i++)
    {
      int val = table.getTable(i,attrNum);
      if (val!=Integer.MAX_VALUE)
      {
        double dval = table.getAttr(attrNum).getDoubleValueForNumericAttr(val);
        sum = sum + dval;
        if (min>dval) min = dval;
        if (max<dval) max = dval;
        licznik++;
      }
    }

    minWrap.setValue(min);
    maxWrap.setValue(max);

    if (licznik==0) return Integer.MAX_VALUE;
    else return sum/licznik;
  }

  static public double calculateStdDevForAttr(Table table,int attrNum,double mean)
  {
    double sum = 0.0;
    int licznik=0;
    for (int i=0; i<table.getNoObj(); i++)
    {
      int val = table.getTable(i,attrNum);
      if (val!=Integer.MAX_VALUE)
      {
        double dval = table.getAttr(attrNum).getDoubleValueForNumericAttr(val);
        sum = sum + (dval - mean)*(dval - mean);
        licznik++;
      }
    }

    if (licznik<1) return Integer.MAX_VALUE;
    else return Math.sqrt(sum/(licznik-1));
  }



  public Table removeNULL_OBJECT(Table table)
  throws InterruptedException
  {
    progress(0);

    boolean [] info = new boolean [table.getNoObj()];

    int noObjWithoutNull = 0;

    for (int i=0; i<table.getNoObj(); i++)
    {
      progress(i*50/table.getNoObj());

      boolean isNULL = false;
      for (int j=0; j<table.getNoAttr(); j++)
      {
        if (table.getTable(i,j)==Integer.MAX_VALUE)
        {
          isNULL = true; break;
        }
      }

      if (isNULL)
      {
        info[i] = false;
      }
      else
      {
        info[i] = true;
        noObjWithoutNull++;
      }
    }

    if (noObjWithoutNull==0)
    {
      throw new IndexOutOfBoundsException("There are null values in all rows!");
    }

    if (noObjWithoutNull==table.getNoObj())
    {
      throw new IndexOutOfBoundsException("There are't null values in current table!");
    }

    Table newTab = new Table();
    newTab.createTable(noObjWithoutNull,table.getNoAttr());

    for (int i=0; i<table.getNoAttr(); i++)
    {
      newTab.getAttr(i).copy(table.getAttr(i));
    }

    int currNewObj = 0;

    for (int i=0; i<table.getNoObj(); i++)
    {
      progress(50+i*50/table.getNoObj());

      if (info[i])
      {
        for (int j=0; j<table.getNoAttr(); j++)
        {
          newTab.setTable(currNewObj,j,table.getTable(i,j));
        }
        currNewObj++;
      }
    }

    progress(100);

    return newTab;
  }



}
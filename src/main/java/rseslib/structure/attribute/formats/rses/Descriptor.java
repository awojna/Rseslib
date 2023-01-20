/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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


//===================== DESCRIPTOR =======================


public class Descriptor
{
  Attr attr;
  IntSack values;

  public Descriptor(Attr a,int singleValue)
  {
    attr  =  new Attr(a);
    values = new IntSack();
    values.add(singleValue);
  }

  public Descriptor(Attr a,IntSack intSack)
  {
    attr  =  new Attr(a);
    values = new IntSack(intSack);
  }

  public void setAtrr(Attr a)
  {
    attr = new Attr(a);
  }

  public Attr getAttr() { return attr; }
  public IntSack getVal() { return values; }

  public boolean equals(Descriptor d)
  {
    if (!attr.equals(d.getAttr())) return false;
    return values.equals(d.getVal());
  }

  public String toString()
  {
    return new String("("+attr+"="+values.toString()+")");
  }

}

/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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

class BinTreeInt
{
   BinTreeInt left;
   BinTreeInt right;

   int intVal;
   int tune;

  public BinTreeInt(int v)
  {
    left = null;
    right = null;
    intVal = v;
    tune=1;
  }

  public BinTreeInt(int v,int t)
  {
     left = null;
     right = null;
     intVal = v;
     tune=t;
  }

  public int compare(int firstInt,int secondInt)
  {
    //Ma zwracac:
    //	-1 jesli FirstInt  <  SecondInt
    //	 0 jesli FirstInt ==  SecondInt
    //	 1 jesli FirstInt  >  SecondInt

    if (firstInt<secondInt) return -1;
    else if (firstInt>secondInt) return 1;
    else return 0;
  }


  public int searchElem(int elem)
  {
    int result = compare(elem,intVal);

    if (result==0) return 1;
    else
    {
      if (result==-1)
      {
        if (left!=null) return left.searchElem(elem);
        else return 0;
      }
      else
      {
        if (right!=null) return right.searchElem(elem);
        else return 0;
      }
    }
  }

  public int addElem(int elem)
  {
    int result = compare(elem,intVal);

    if (result==0)
    {
      tune++;
      return 0;
    }
    else
    {
      if (result==-1)
      {
	if (left!=null) return left.addElem(elem);
	else
	{
	  left = new BinTreeInt(elem);
	  return 1; //wstawil nowa
	}
      }
      else
      {
        if (right!=null) return right.addElem(elem);
	else
	{
          right = new BinTreeInt(elem);
	  return 1; //wstawil nowa
        }
      }
    }
  }

  public void copyToIntSack(IntSack sack)
  {
    sack.add(intVal);
    if (left!=null) left.copyToIntSack(sack);
    if (right!=null) right.copyToIntSack(sack);
  }

/*
  public void copyToBest(int max_no_elem,IntSack2 sack2)
  {
    sack2.addToBest(intVal,tune,max_no_elem);

    if (left!=null) left.copyToBest(max_no_elem,sack2);

    if (right!=null) right.copyToBest(max_no_elem,sack2);
  }
 */

  public int getNoElem()
  {
    int elemCount=1;

    if (left!=null) elemCount = elemCount + left.getNoElem();

    if (right!=null) elemCount = elemCount + right.getNoElem();

   return elemCount;
  }


  public void getMaxTuneVal(IntWrap maxVal,IntWrap maxTune)
  {
    if (tune>maxTune.getValue())
    {
      maxTune.setValue(tune);
      maxVal.setValue(intVal);
   }

   if (left!=null) left.getMaxTuneVal(maxVal,maxTune);

   if (right!=null) right.getMaxTuneVal(maxVal,maxTune);

  }
}

public class BinTreeIntWrap
{
  BinTreeInt binTree;

  public BinTreeIntWrap()
  {
    binTree = null;
  }

  public int searchElem(int elem)
  {
    if (binTree==null) return 0;
    else
      return binTree.searchElem(elem);
  }

  public int addElem(int elem)
  {
    if (binTree==null)
    {
      binTree = new BinTreeInt(elem);
      return 1;
    }
    else
      return binTree.addElem(elem);
  }

  public void copyToIntSack(IntSack sack)
  {
    if (binTree==null) return;
    else binTree.copyToIntSack(sack);
  }

/*
  public void copyToBest(int max_no_elem,IntSack2 sack2)
  {
    if (binTree==null) return;
    else binTree.copyToBest(max_no_elem,sack2);
  }*/

  public int getNoElem()
  {
    if (binTree==null) return 0;
    else
      return binTree.getNoElem();
  }

  public void getMaxTuneVal(IntWrap maxVal,IntWrap maxTune)
  {
    if (binTree==null) return;
    else binTree.getMaxTuneVal(maxVal,maxTune);
  }

}

// BIN TREE for descriptors

class BinTreeDescrip
{
   BinTreeDescrip left;
   BinTreeDescrip right;

   Descriptor descrip;
   int tune;

  public BinTreeDescrip(Descriptor new_descrip)
  {
    left = null;
    right = null;
    descrip = new_descrip;
    tune=1;
  }

  public int compare(Descriptor first,Descriptor second)
  {
    //Ma zwracac:
    //	-1 jesli FirstInt  <  SecondInt
    //	 0 jesli FirstInt ==  SecondInt
    //	 1 jesli FirstInt  >  SecondInt

    /*
    if (first.getAttr().compareTo(second.getAttr())<0) return -1;
    else
    {
      if (first.getAttr().compareTo(second.getAttr())>0) return 1;
      else //ten sam atrybut
      {
	if (first.getVal().compareTo(second.getVal())<0) return -1;
	else
	{
	  if (first.getVal().compareTo(second.getVal())>0) return 1;
	  else
	    return 0; //ten sam deskryptor
	}
      }
    }
    */
    return 0;
  }


  public int searchDescriptor(Descriptor new_descrip)
  {
    int result = compare(new_descrip,descrip);

    if (result==0) return 1;
    else
    {
      if (result==-1)
      {
        if (left!=null) return left.searchDescriptor(new_descrip);
        else return 0;
      }
      else
      {
        if (right!=null) return right.searchDescriptor(new_descrip);
        else return 0;
      }
    }
  }

  public int addDescriptor(Descriptor new_descrip)
  {
    int result = compare(new_descrip,descrip);

    if (result==0)
    {
      tune++;
      return 0;
    }
    else
    {
      if (result==-1)
      {
	if (left!=null) return left.addDescriptor(new_descrip);
	else
	{
	  left = new BinTreeDescrip(new_descrip);
	  return 1; //wstawil nowy
	}
      }
      else
      {
        if (right!=null) return right.addDescriptor(new_descrip);
	else
	{
          right = new BinTreeDescrip(new_descrip);
	  return 1; //wstawil nowy
        }
      }
    }
  }

/*  public void copyBestTo(int max_no_elem,IntSack3 sack3)
  {
//    sack3.addBestTo(descrip.getAttr(),descrip.getVal(),tune,max_no_elem);

    if (left!=null) left.copyBestTo(max_no_elem,sack3);

    if (right!=null) right.copyBestTo(max_no_elem,sack3);
  }*/

  public int getNoElem()
  {
    int elemCount=1;

    if (left!=null) elemCount = elemCount + left.getNoElem();

    if (right!=null) elemCount = elemCount + right.getNoElem();

   return elemCount;
  }
}

class BinTreeDescriptorWrap
{
  BinTreeDescrip binTreeDescrip;

  public BinTreeDescriptorWrap()
  {
    binTreeDescrip = null;
  }

  public int searchDescriptor(Descriptor d)
  {
    if (binTreeDescrip==null) return 0;
    else
      return binTreeDescrip.searchDescriptor(d);
  }

  public int addDescriptor(Descriptor d)
  {
    if (binTreeDescrip==null)
    {
      binTreeDescrip = new BinTreeDescrip(d);
      return 1;
    }
    else
      return binTreeDescrip.addDescriptor(d);
  }

/*  public void copyBestTo(int max_no_elem,IntSack3 sack3)
  {
    if (binTreeDescrip==null) return;
    else binTreeDescrip.copyBestTo(max_no_elem,sack3);
  }
      */


  public int getNoElem()
  {
    if (binTreeDescrip==null) return 0;
    else
      return binTreeDescrip.getNoElem();
  }


}







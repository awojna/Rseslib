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

package rseslib.util.pair;

/**
 * The class implementing pair of objects of generic type.
 *
 * @author      Grzegorz Gora
 */
public class PairObjObj<OBJ1, OBJ2>
{
  protected OBJ1 m_obj1;
  protected OBJ2 m_obj2;

  public PairObjObj()
  {
  }

  public PairObjObj(OBJ1 _obj1, OBJ2 _obj2)
  {
    m_obj1 = _obj1;
    m_obj2 = _obj2;
  }

  public OBJ1 getFirst()
  {
    return m_obj1;
  }

  public OBJ2 getSecond()
  {
    return m_obj2;
  }

  public String toString()
  {
	  return "(" + m_obj1.toString() + "; " + m_obj2.toString() + ")";
  }
}

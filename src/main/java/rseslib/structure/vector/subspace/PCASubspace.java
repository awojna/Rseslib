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


package rseslib.structure.vector.subspace;

import java.util.ArrayList;

import rseslib.processing.pca.OjaRLS;
import rseslib.structure.data.DoubleData;
import rseslib.structure.vector.Vector;
import rseslib.structure.vector.VectorForDoubleData;

/**
 * Multidimensional linear real value translated subspace
 * spanned on the basis of Oja-RLS rule.
 *
 * @author      Rafal Falkowski
 */

public class PCASubspace extends TranslatedLinearSubspace {

  /**
      * Constructor spans the Subspace on vectors from given table
      * using Oja-RLS rule.
      *
      * @param decClass   Table of vectors.
      * @param dimension  Dimension of spanning vectors.
      * @param PSDim  Dimension of the subspace.
      */

  public PCASubspace(ArrayList<DoubleData> decClass, int dimension, int PSDim)
  {
    super(PSDim, new Vector(dimension));
    ojaRlsK(decClass);
  }

/**
* Calculates the centroid of the given decision class
* and creates new centered array of DoubleVectors
* corresponding to objects of the decision class.
* Calculates also B = max( ||x||^2 ).
*
* @param decClass   Array with data objects from one decision
* 						 class.
* @return				 Array of centered decision class.
*/
private ArrayList<Vector> centerSet(ArrayList<DoubleData> decClass)
{
      setCentroid((Vector)VectorForDoubleData.centroid(decClass));
      ArrayList<Vector> decClass0 = new ArrayList<Vector>();
      // Przesuniecie danych o centroid i znalezienie najwiekszego kwadratu normy elementow (b).
      for (DoubleData obj : decClass)
      {
              Vector dVec = new VectorForDoubleData(obj);
              dVec.subtract(getCentroid());
              decClass0.add(dVec);
      }
      return decClass0;
}

/**
* Calculates principal vectors that span the
* principal subspace.
*
* @param decClass   ArrayB with data objects from one decision
* 						 class.
*/
private void ojaRlsK(ArrayList<DoubleData> decClass)
{
      ArrayList<Vector> decClass0 = centerSet(decClass);
      for (int i = 0; i < getDimension(); i++)
      {
        Vector dVec = OjaRLS.ojaRlsRule(decClass0);
        dVec.normalizeWithEuclideanNorm();
        setSpanningVector(dVec, i);
        if (i < getDimension() - 1)
        {
       	  for (Vector dVec0 : decClass0)
            dVec0.deflate(getSpanningVector(i));
        }
      }
}


}

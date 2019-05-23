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


package rseslib.processing.pca;

import java.util.ArrayList;

import rseslib.structure.vector.Vector;
import rseslib.structure.vector.VectorForDoubleData;
/**
 * Neural network rule that converges to the first
 * principal vector of a given set.
 *
 * @author      Rafal Falkowski
 */

public class OjaRLS {

  /**
   * Calculates first principal vector.
   *
   * @param decClass   ArrayB with data objects from one decision
   *  class and B = max( ||x||^2 ).
   * @return				 Principal vector.
   */
   public static Vector ojaRlsRule(ArrayList<Vector> decClass)
   {
     double bMax = 0;
     for (Vector vec : decClass)
      {
              Vector dVec = new Vector(vec);
              double b = dVec.squareEuclideanNorm();
              if (b > bMax) bMax = b;
      }
        double beta = 2*bMax, y = 0;
        Vector w = new VectorForDoubleData((Vector)decClass.get(0));
        w.normalizeWithEuclideanNorm();
        for (Vector vec : decClass)
        {
          Vector dVec = new VectorForDoubleData(vec);
          y = w.scalarProduct(dVec); // y = xw
          // Next lines mean:
          // w = w + y*(x - y*w)/beta
          Vector pVec =  new VectorForDoubleData((Vector)w); // w
          pVec.multiply(y); // yw
          dVec.subtract(pVec); // x - yw
          dVec.multiply(y/beta); // y(x - yw)/beta
          w.add(dVec);// w + y(x - yw)/beta
          beta = beta + y*y;
        }
        return w;
      }

}

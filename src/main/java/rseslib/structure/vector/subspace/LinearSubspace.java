/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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

import rseslib.structure.vector.Vector;

/**
 * Multidimensional linear real value subspace
 * spanned by DoubleVectors with implemented projections.
 *
 * @author      Rafal Falkowski
 */

public class LinearSubspace {

  /** Vectors that span the subspace. */
  private Vector[] m_nSpanningVectors;

   /**
    * Constructor spans the Subspace on vectors from given table.
    *
    * @param vec   Table of vectors.
    */
  public LinearSubspace(Vector[] vec) {
    m_nSpanningVectors = new Vector[vec.length];
    for (int i = 0; i < vec.length; i++)
      m_nSpanningVectors[i] = new Vector(vec[i]);
  }
  /**
    * Constructor spans the null Subspace on null vectors.
    *
    * @param sDim  Dimension of the subspace.
    * @param vDim  Dimension of spanning vectors.
    */
  public LinearSubspace(int sDim, int vDim) {
    m_nSpanningVectors = new Vector[sDim];
    for (int i = 0; i < sDim; i++)
      m_nSpanningVectors[i] = new Vector(vDim);
  }


    /**
     * Gets the dimension of the subspace.
     *
     * @return Dimension of the subspace.
     */

  public int getDimension() {
    return m_nSpanningVectors.length;
  }

    /**
     * Gets vectors spanning the subspace.
     *
     * @return Spanning vectors.
     */
  public Vector[] getSpanningVectors() {
    return m_nSpanningVectors;
  }

  /**
 * Gets the ith spanning vector.
 *
 * @return Ith spanning vector.
 */
  public Vector getSpanningVector(int i) {
    return m_nSpanningVectors[i];
  }

  /**
   * Sets the ith spanning vector.
   */
  public void setSpanningVector(Vector vec, int i) {
    m_nSpanningVectors[i] = vec;
  }

  /**
   * Calculates the projection of DataVector
   * onto the subspace spanned by first n vectors.
   *
   * @param dVec Vector to be projected.
   * @param n	Dimension of the subspace.
   *		It should be less to the DataVector dimension.
   *
   * @return Projected vector.
   */
  public Vector projection(Vector dVec, int n) {
    double[] y = new double[n];
    for (int i = 0; i < n; i++) // y = W^T x'
            y[i] = dVec.scalarProduct(m_nSpanningVectors[i]);
    Vector vecProjection = new Vector(m_nSpanningVectors[0].dimension()); // z
    for (int att = 0; att < m_nSpanningVectors[0].dimension(); att++) // z = W y
    {
            double sum = 0;
            for (int j = 0; j < n; j++) //to chyba malo efektywne? Ale poprawne :-)
                    sum += y[j]*m_nSpanningVectors[j].get(att);
            vecProjection.set(att, sum);
    }
    return vecProjection;
  }

/**
 * Calculates the projection of DataVector
 * onto the subspace of the Subspace Dimension, i.e. to
 * the number of spanning vectors.
 *
 * @param dVec Vector to be projected.
 *
 * @return Projected vector.
 */
  public Vector projection(Vector dVec) {
    return projection(dVec, m_nSpanningVectors.length);
  }

  /**
   * Calculates the projections of DataVector
   * onto the subspaces for dimensions
   * from 1 to the Subspace Dimension, i.e. to the number
   * of spanning vectors. For calculation of each projection
   * are taken first k vectors from m_nSpanningVectors.
   *
   * @param dVec Vector to be projected.
   *
   * @return Table of projected vectors.
   */
  public Vector[] projections(Vector dVec) { //Liczona jest tu cala tabela projekcji.
    double[] y = new double[m_nSpanningVectors.length];
    Vector vecProjection[] = new Vector[m_nSpanningVectors.length]; // z
    for (int i = 0; i < m_nSpanningVectors.length; i++) // y = W^T x'
    {
      y[i] = dVec.scalarProduct(m_nSpanningVectors[i]);
      vecProjection[i] = new Vector(m_nSpanningVectors[0].dimension());
    }
    for (int att = 0; att < m_nSpanningVectors[0].dimension(); att++) // z = W y
    {
        double sum = 0;
        for (int j = 0; j < m_nSpanningVectors.length; j++) //to chyba malo efektywne? Ale poprawne :-)
        {
            sum += y[j]*m_nSpanningVectors[j].get(att);
            vecProjection[j].set(att, sum);
        }
    }
    return vecProjection;
  }
  /**
   * Calculates distance between given DoubleVector
   * and its projection onto the subspace
   * on the basis of the Euclidean norm.
   *
   * @param dVec Vector to be projected.
   *
   * @return Square of the distance.
   */
  public double euclideanDist(Vector dVec) {
    Vector vecProjection = projection(dVec, m_nSpanningVectors.length);
    vecProjection.subtract(dVec); //  W y - dVec
    return vecProjection.euclideanNorm();
  }
  /**
   * Calculates distance between given DoubleVector
   * and its projection onto the subspace
   * on the basis of the Manhattan city norm.
   *
   * @param dVec Vector to be projected.
   *
   * @return Square of the distance.
   */
  public double cityDist(Vector dVec) {
    Vector vecProjection = projection(dVec, m_nSpanningVectors.length);
    vecProjection.subtract(dVec); //  W y - dVec
    return vecProjection.cityNorm();
  }

}

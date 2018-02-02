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
 * spanned by DoubleVectors and translated to given
 * Centroid with implemented projections.
 *
 * @author      Rafal Falkowski
 */

public class TranslatedLinearSubspace extends LinearSubspace {

  /** Centoid of the the subspace. */
  private Vector m_nCentroid;

  /**
   * Constructor spans the Subspace on vectors from given table
   * and translates it to the centroid.
   *
   * @param vec   Table of vectors.
   * @param centroid Centroid
   */
  public TranslatedLinearSubspace(Vector[] vec, Vector centroid) {
    super(vec);
    m_nCentroid = new Vector(centroid);
  }

  /**
   * Constructor spans the Subspace on vectors from given table
   * and translates it to the centroid.
   *
   * @param vec   Table of vectors.
   * @param centroid Centroid
   */
  public TranslatedLinearSubspace(int sDim, Vector centroid) {
    super(sDim, centroid.dimension());
    m_nCentroid = new Vector(centroid);
  }


  /**
   * Gets the centroid of the principal subspace.
   *
   * @return Centorid.
   */
  public Vector getCentroid()
  {
    return m_nCentroid;
  }

  /**
   * Sets the centroid of the principal subspace.
   */
  public void setCentroid(Vector centroid)
  {
    m_nCentroid.set(centroid);
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
  public Vector projection(Vector dVec, int n)
  {
    dVec.subtract(m_nCentroid);
    Vector vecProjection = super.projection(dVec, n);
    vecProjection.add(m_nCentroid);
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
    dVec.subtract(m_nCentroid);
    Vector vecProjection = super.projection(dVec);
    vecProjection.add(m_nCentroid);
    return vecProjection;
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
   public Vector[] projections(Vector dVec)
        {
           dVec.subtract(m_nCentroid);
           Vector[] vecProjection = super.projections(dVec);
           for (int i = 0; i < vecProjection.length; i++)
             vecProjection[i].add(m_nCentroid);
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
            dVec.subtract(m_nCentroid); // x' = x - c
            return super.euclideanDist(dVec);
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
            dVec.subtract(m_nCentroid); // x' = x - c
            return super.cityDist(dVec);
          }


}

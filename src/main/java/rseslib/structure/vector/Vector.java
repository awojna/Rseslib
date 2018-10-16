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


package rseslib.structure.vector;

import java.io.Serializable;

/**
 * Vector from a multidimensional real value space
 * with variety of vector operations.
 *
 * @author      Rafal Falkowski, Lukasz Ligowski, Arkadiusz Wojna
 */
public class Vector implements Comparable, Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
    /** Vector coordinates. */
    double[] m_arrCoordinates;

    /**
     * Constructor sets initial values of coordinates to  0.
     *
     * @param dim   Vector dimension.
     */
    public Vector(int dim)
    {
        m_arrCoordinates = new double[dim];
    }

    /**
     * Constructs a vector identical with a given template.
     *
     * @param template Original template.
     */
    public Vector(Vector template)
    {
        m_arrCoordinates = new double[template.dimension()];
        for (int coord = 0; coord < m_arrCoordinates.length; coord++)
            m_arrCoordinates[coord] = template.get(coord);
    }

    /**
     * Returns vector dimension.
     *
     * @return Vector dimension.
     */
     public int dimension()
     {
         return m_arrCoordinates.length;
     }

    /**
     * Sets the value of an indicated coordinate.
     *
     * @param coord Coordinate index.
     * @param value New coordinate value.
     */
     public void set(int coord, double value)
     {
         m_arrCoordinates[coord] = value;
     }

    /**
     * Sets the vector values identically as in a given template.
     *
     * @param template Original template.
     */
     public void set(Vector template)
     {
         for (int coord = 0; coord < m_arrCoordinates.length; coord++)
             m_arrCoordinates[coord] = template.get(coord);
     }

    /**
     * Increments an indicated coordinate by 1.
     *
     * @param coord Coordinate index.
     */
     public void increment(int coord)
     {
         m_arrCoordinates[coord] += 1;
     }

    /**
     * Decrements an indicated coordinate by 1.
     *
     * @param coord Coordinate index.
     */
     public void decrement(int coord)
     {
         m_arrCoordinates[coord] -= 1;
     }

    /**
     * Gets the value of an indicated coordinate.
     *
     * @param coord Coordinate index.
     * @return Coordinate value.
     */
     public double get(int coord)
     {
         return m_arrCoordinates[coord];
     }

    /**
     * Resets all coordinates to 0.
     */
     public void reset()
     {
         for (int c = 0; c < m_arrCoordinates.length; c++) m_arrCoordinates[c] = 0;
     }

    /**
     * Adds a given vector.
     *
     * @param vec Vector to be added.
     */
     public void add(Vector vec)
     {
         for (int c = 0; c < m_arrCoordinates.length; c++) m_arrCoordinates[c] += vec.get(c);
     }

    /**
     * Subtracts a given vector.
     *
     * @param vec Subtracted vector.
     */
     public void subtract(Vector vec)
     {
         for (int c = 0; c < m_arrCoordinates.length; c++) m_arrCoordinates[c] -= vec.get(c);
     }

    /**
     * Multiplies this vector.
     *
     * @param coeff Multiplication factor.
     */
     public void multiply(double coeff)
     {
         for (int c = 0; c < m_arrCoordinates.length; c++) m_arrCoordinates[c] *= coeff;
     }

	/**
	 * Multiplies i`th coordinate.
	 *
	 * @param i Coordinate.
	 * @param multiplier Multiplier.
	 */
	 public void multiply(int i,double multiplier)
	 {
	 	m_arrCoordinates[i] *= multiplier;
	 }

    /**
     * Divides this vector.
     *
     * @param div Divisor.
     */
     public void divide(double div)
     {
         for (int c = 0; c < m_arrCoordinates.length; c++) m_arrCoordinates[c] /= div;
     }

	/**
	 * Divides i`th coordinate.
	 *
	 * @param i Coordinate.
	 * @param div Divisor.
	 */
	 public void divide(int i,double div)
	 {
	     m_arrCoordinates[i] /= div;
	 }

     /**
      * Returns the scalar product of this vector and the vector given as the parameter.
      *
      * @param vec	Vector used to compute the product.
      *
      * @return		Scalar product.
      */
     public double scalarProduct(Vector vec)
     {
         double sum = 0;
         for (int att = 0; att < m_arrCoordinates.length; att++)
             sum += m_arrCoordinates[att]*vec.get(att);
         return sum;
     }

     /**
      * Deflates this vector by a given vector.
      * it means that this method performs the projection of this
      * vector onto the space orthogonal to a given vector.
      *
      * @param vec		Vector used to deflate this vector.
      */
     public void deflate(Vector vec)
     {
         // tzn: x = x - (vec^T * x) * w
         double vecx = vec.scalarProduct(this);
         for(int att = 0; att < m_arrCoordinates.length; att++)
             set(att, get(att) - vecx*vec.get(att));
     }

     /**
      * Returns the city-Manhattan norm of this vector.
      *
      * @return		The city-Manhattan norm.
      */
     public double cityNorm()
     {
         double sum = 0;
         for (int c = 0; c < m_arrCoordinates.length; c++)
             if (m_arrCoordinates[c] >= 0) sum += m_arrCoordinates[c];
             else sum -= m_arrCoordinates[c];
         return sum;
      }

      /**
       * Normalises this vector to 1 according to city metric.
       */
       public void normalizeWithCityNorm()
       {
           double norm = cityNorm();
           for (int c = 0; c < m_arrCoordinates.length; c++) m_arrCoordinates[c] /= norm;
       }

     /**
      * Returns the square Euclidean norm of this vector.
      *
      * @return		The square Euclidean norm.
      */
     public double squareEuclideanNorm()
     {
         double sum = 0;
         for (int c = 0; c < m_arrCoordinates.length; c++)
             sum += m_arrCoordinates[c]*m_arrCoordinates[c];
         return sum;
     }

     /**
      * Returns the Euclidean norm of this vector.
      *
      * @return		The Euclidean norm.
      */
     public double euclideanNorm()
     {
         return Math.sqrt(squareEuclideanNorm());
     }

    /**
     * Normalises this vector to 1 according to the Euclidean metric.
     */
     public void normalizeWithEuclideanNorm()
     {
         double norm = euclideanNorm();
         for (int c = 0; c < m_arrCoordinates.length; c++) m_arrCoordinates[c] /= norm;
     }

    /**
     * Compares this vector with the specified vector o according to the lexicographical order.
     * Returns -1, 0, or 1 as this vector is less than, equal to, or greater than the specified object.
     *
     * @param o The vector to be compared.
     * @return The value -1, 0, or 1 as this vector is less than, equal to, or greater than the specified object.
     */
    public int compareTo(Object o)
    {
        for (int coord = 0; coord < m_arrCoordinates.length && coord < ((Vector)o).dimension(); coord++)
            if (m_arrCoordinates[coord] < ((Vector)o).get(coord)) return -1;
            else if (m_arrCoordinates[coord] > ((Vector)o).get(coord)) return 1;
        if (m_arrCoordinates.length < ((Vector)o).dimension()) return -1;
        else if (m_arrCoordinates.length > ((Vector)o).dimension()) return 1;
        return 0;
    }

    /**
     * Return true if this vector and the specified vector o are the same
     * and false otherwise.
     *
     * @param obj The vector to be compared.
     * @return True if this vector and the specified vector o are the same and false otherwise.
     */
    public boolean equals(Object obj)
    {
        if (m_arrCoordinates.length!=((Vector)obj).dimension()) return false;
        for (int coord = 0; coord < m_arrCoordinates.length; coord++)
            if (m_arrCoordinates[coord]!=((Vector)obj).get(coord)) return false;
        return true;
    }

     /**
      * Returns string representing this vector.
      *
      * @return String representing this vector.
      */
     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         sb.append("[");
         for (int d = 0; d < m_arrCoordinates.length; d++)
         {
             sb.append(m_arrCoordinates[d]);
             if (d < m_arrCoordinates.length-1) sb.append(", ");
         }
         sb.append("]");
         return sb.toString();
     }

     /**
      * Returns Manhattan city distance between two vectors.
      *
      * @param vect1 The first vector to be compared.
      * @param vect2 The second vector to be compared.
      * @return Manhattan city distance between two vectors.
      */
     public static double cityDist(Vector vect1, Vector vect2)
     {
         double dist = 0;
         if (vect1.dimension() != vect2.dimension()) throw new RuntimeException("Compared vectors have different dimension "+vect1.dimension()+" and "+vect2.dimension());
         for (int coord = 0; coord < vect1.dimension(); coord++)
             dist += Math.abs(vect1.get(coord)-vect2.get(coord));
         return dist;
     }
     
     /**
      * Returns rank of the vector.
      * Rank is a number of non-zero (and non Not-a-Number) parameters. 
      * @return rank of the vector
      */
     public int rank()
     {
         int rank=0;
         for (double d : m_arrCoordinates)
             if (d!=0&&!Double.isNaN(d)) rank++;
         return rank;
     }
}

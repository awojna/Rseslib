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


package rseslib.structure.vector;

import java.util.Collection;

import rseslib.structure.data.DoubleData;

/**
 * Vector from a multidimensional real value space
 * with direct operations with double data.
 *
 * @author      Rafal Falkowski, Arkadiusz Wojna
 */
public class VectorForDoubleData extends Vector
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/**
     * Constructor sets initial values of coordinates to  0.
     *
     * @param dim   Vector dimension.
     */
    public VectorForDoubleData(int dim)
    {
        super(dim);
    }

    /**
     * Constructs a vector identical with a given template.
     *
     * @param template Original template.
     */
    public VectorForDoubleData(Vector template)
    {
        super(template);
    }

    /**
     * Constructors a vector which attributes
     * correspond to the conditional attributes
     * of a given data object.
     *
     * @param dObj	Data object.
     */
    public VectorForDoubleData(DoubleData dObj)
    {
        super(dObj.attributes().noOfAttr()-1);
        int dec = dObj.attributes().decision();
        int index = 0;
        for (int att = 0; att < dObj.attributes().noOfAttr(); att++)
            if (att!=dec) m_arrCoordinates[index++] = dObj.get(att);
    }

     /**
      * Adds a vector represented in a given data object.
      *
      * @param dObj	Data object to be added.
      */
     public void add(DoubleData dObj)
     {
         int dec = dObj.attributes().decision();
         int index = 0;
         for (int att = 0; att < dObj.attributes().noOfAttr(); att++)
             if (att!=dec) m_arrCoordinates[index++] += dObj.get(att);
     }

     /**
      * Subtracts a vector represented in a given data object.
      *
      * @param dObj	Data object to be subtracted.
      */
     public void subtract(DoubleData dObj)
     {
         int dec = dObj.attributes().decision();
         int index = 0;
         for (int att = 0; att < dObj.attributes().noOfAttr(); att++)
             if (att!=dec) m_arrCoordinates[index++] += dObj.get(att);
     }

     /**
      * Returns the scalar product of this vector and
      * a vector represented in a given data object.
      *
      * @param dObj	Data object used to compute the product.
      *
      * @return		Scalar product.
      */
     public double scalarProduct(DoubleData dObj)
     {
         int dec = dObj.attributes().decision();
         int index = 0;
         double sum = 0;
         for (int att = 0; att < dObj.attributes().noOfAttr(); att++)
             if (att!=dec) sum += m_arrCoordinates[index++]*dObj.get(att);
         return sum;
     }

     /**
      * Returns the centroid of a given collection of data objects.
      *
      * @param dataColl  Set of data objects.
      * @return          Centroid of a given collection of data objects.
      */
     public static Vector centroid(Collection<DoubleData> dataColl)
     {
         // zalozenie, ze na poczatku centroid jest rowny 0.
    	 VectorForDoubleData center = null;
         for (DoubleData obj : dataColl)
         {
        	 if (center == null)
        		 center = new VectorForDoubleData(obj.attributes().noOfAttr()-1);
        	 center.add(obj);
         }
         center.divide(dataColl.size());
         return center;
     }
}

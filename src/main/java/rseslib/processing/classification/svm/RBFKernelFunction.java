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


package rseslib.processing.classification.svm;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;


/**
 * Class realizing Gaussian kernel function
 */
public class RBFKernelFunction  implements KernelFunction {

    /** Information about attributes. */
    private Header m_Attributes;

    /**
     * sigma
     */
    private double sigma;

    /**
     *    RBF function = e^((-||x-y||^2)/(2*sigma^2)) 
     * @param tab
     * @param s  sigma
     */
    public RBFKernelFunction(DoubleDataTable tab, double s) {
        m_Attributes = (Header)tab.attributes();
        sigma = s;
    }

    /**
     * kernel function K
     * @param dObj1 data object 1
     * @param dObj2 data object 2
     * @return kernel function value for given parameters
     */
    public double K(DoubleData dObj1, DoubleData dObj2) {
        double result = 0;
        for (int att = 0; att < dObj1.attributes().noOfAttr(); att++)
            if (m_Attributes.isConditional(att))
                result+=(dObj1.get(att)-dObj2.get(att))*(dObj1.get(att)-dObj2.get(att));
        result = Math.exp(-result/(2*sigma*sigma));
        return result;
    }

}

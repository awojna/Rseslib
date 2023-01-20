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


package rseslib.structure.rule;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.vector.Vector;

/**
 * Abstract class for rule
 * with distributed decision and statistics.
 * 
 * @author Rafal Latkowski
 */
public abstract class AbstractDistrDecRuleWithStatistics extends AbstractDistrDecRule implements RuleWithStatistics
{
	/** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/** Rule accuracy. */
    double m_nAccuracy = Double.NaN;
	/** Rule support. */
    double m_nSupport = Double.NaN;

    /**
     * Sets the decision distribution of this rule.
     *
     * @param decVec	Decision distribution to be set.
     * @param decAttr	Information about the decision attribute.
     */
    public void setDecisionVector(Vector decVec, NominalAttribute decAttr)
    {
    	super.setDecisionVector(decVec, decAttr);
        int bestDec = 0;
        double sum=0.0;
        for (int dec = 0; dec < m_DecisionVector.dimension(); dec++)
        {
            sum+=m_DecisionVector.get(dec);
            if (m_DecisionVector.get(dec) > m_DecisionVector.get(bestDec)) bestDec = dec;
        }
        m_nAccuracy = m_DecisionVector.get(bestDec)/sum;
    }

    /**
     * Sets the accuracy of this rule.
     *
     * @param acc	Accuracy of this rule between 0 and 1.
     */
    public void setAccuracy(double acc)
    {
        m_nAccuracy=acc;
    }
    
    /**
     * Sets the support of this rule.
     *
     * @param supp	Support of this rule between 0 and 1.
     */
    public void setSupport(double supp)
    {
        m_nSupport=supp;
    }

    /**
     * Returns the accuracy of this rule.
     *
     * @return     Accuracy of this rule between 0 and 1.
     * @see rseslib.structure.rule.RuleWithStatistics#getAccuracy()
     */
    public double getAccuracy()
    {
        return m_nAccuracy;
    }

    /**
     * Returns the support of this rule.
     *
     * @return     Support of this rule between 0 and 1.
     * @see rseslib.structure.rule.RuleWithStatistics#getSupport()
     */
    public double getSupport()
    {
        return m_nSupport;
    }
}

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


package rseslib.processing.classification.parameterised.pca;

import java.util.ArrayList;
import java.util.Properties;

import rseslib.processing.classification.parameterised.AbstractParameterisedClassifier;
import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;
import rseslib.structure.vector.VectorForDoubleData;
import rseslib.structure.vector.subspace.PCASubspace;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Classifier that assigns the decision
 * calculated on the basis of principal
 * subspaces of decision classes.
 *
 * @author      Rafal Falkowski
 */
public class PcaClassifier extends AbstractParameterisedClassifier
{
    /** Parameter name. */
    public static final String PRINCIPAL_SUBSPACE_DIM = "principalSubspaceDim";
    /** Parameter name. */
    private static final String OPTIMAL_DIM = "optimalDimension";

    /** The default decision defined by the largest support in a training data set. */
    private int m_nDefaultDec;
    /** Array of principal subspaces for each decision class. */
//    private PrincipalSubspace[] m_nSubspaces;
    PCASubspace[] m_nSubspaces;
    /** Number of decisions. */
    private int m_nNoOfDec;
    /** Decision attribute. */
    NominalAttribute m_DecisionAttribute;
    /** The dimension of the data space, i.e. number of attributes of data without decision attribute. */
    private int m_nDim = 0;
    /** Parameter value, i.e. the dimension of the principal subspace. */
    private int m_nPar;
    /** Optimal dimension of the principal subspace. */
    private int m_nOptimal;

    /**
     * Constructor computes principal subspaces for each decision class.
     *
     * @param prop                Parameters of this clasifier. If null,
     *                            parameters are loaded from the configuration directory.
     * @param trainTable          Table used to train classifier.
     * @param prog                Progress object for training process.
     * @throws InterruptedException when the user interrupts the execution.
     */
	public PcaClassifier(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, BadHeaderException, InterruptedException
    {
        super(prop, OPTIMAL_DIM);
        boolean numericNotFound = true;
        for (int at = 0; numericNotFound && at < trainTable.attributes().noOfAttr(); at++)
            if (trainTable.attributes().isConditional(at) && trainTable.attributes().isNumeric(at))
                numericNotFound = false;
        if (numericNotFound) throw new BadHeaderException("Pca classifier requires numerical attributes");
		prog.set("Learning PCA classifier from training table", 1);
        NominalAttribute decAttr = trainTable.attributes().nominalDecisionAttribute();
		int[] decDistr = trainTable.getDecisionDistribution();
		m_nDefaultDec = 0;
                m_DecisionAttribute = decAttr;
                m_nNoOfDec = m_DecisionAttribute.noOfValues();
		for (int dec = 1; dec < m_nNoOfDec; dec++)
			if (decDistr[dec] > decDistr[m_nDefaultDec]) m_nDefaultDec = dec;

		// Okreslenie ile jest atrybutow warunkowych.
		for (int att = 0; att < trainTable.attributes().noOfAttr(); att++)
			if (trainTable.attributes().isConditional(att))	m_nDim++;

		// Podzial tabeli na klasy decyzyjne.
		ArrayList<DoubleData>[] decisionDistr = new ArrayList[m_nNoOfDec];
		for (int i = 0; i < m_nNoOfDec; i++)
			decisionDistr[i] = new ArrayList<DoubleData>();
        for (DoubleData dObj : trainTable.getDataObjects())
               {
/*                        Integer decNam = new Integer(((DoubleDataWithDecision)dObj).getDecision());
                       if (!decName.containsKey(decNam))
                       {
                               decName.put(decNam, new Integer(dec));
                               dec++;
                       }
                       decisionDistr[((Integer)decName.get(decNam)).intValue()].add(dObj);*/
                       decisionDistr[trainTable.attributes().nominalDecisionAttribute().localValueCode(((DoubleDataWithDecision)dObj).getDecision())].add(dObj);
               }
		m_nPar = getIntProperty(PRINCIPAL_SUBSPACE_DIM);
		m_nSubspaces = new PCASubspace[m_nNoOfDec];
		for (int i = 0; i < m_nNoOfDec; i++)
			m_nSubspaces[i] = new PCASubspace(decisionDistr[i], m_nDim, m_nPar);
        learnOptimalParameterValue(trainTable, new EmptyProgress());
        m_nOptimal = getIntProperty(OPTIMAL_DIM);
		prog.step();
    }

	/**
	 * Classifies a test object on the basis of the dimension of the principal subspace.
	 *
	 * @param dObj         Test object.
	 * @return             Array of assigned decisions, indices correspond to parameter values.
	 */
	public double[] classifyWithParameter(DoubleData dObj)
	{
		double[] decisions = new double[m_nPar];
		int[] bestDec = new int[decisions.length];
		double[] resid = new double[decisions.length];
		double[] minResid = new double[decisions.length];
		for (int n = 1; n < decisions.length; n++)
		{
			resid[n] = 0;
			minResid[n] = -1;
			bestDec[n] = m_nDefaultDec;
			decisions[n] = m_DecisionAttribute.globalValueCode(bestDec[n]);;
		}
		Vector x = new VectorForDoubleData(dObj);
		for (int i = 0; i < m_nNoOfDec; i++)
		{
			Vector[] px = m_nSubspaces[i].projections(new VectorForDoubleData(dObj));
			for (int n = 1; n < decisions.length; n++)
			{
				px[n].subtract(x);
				resid[n] = px[n].squareEuclideanNorm();
				if (minResid[n] < 0 || minResid[n] > resid[n])
				{
					minResid[n] = resid[n];
					bestDec[n] = i;
				}
                decisions[n] = m_DecisionAttribute.globalValueCode(bestDec[n]);
			}
		}
		return decisions;
	}

    /**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
     */
    public double classify(DoubleData dObj)
    {
        return classifyWithParameter(dObj)[m_nOptimal];
    }

    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
    	addToStatistics("Principal subspace dimension", Integer.toString(m_nOptimal));
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

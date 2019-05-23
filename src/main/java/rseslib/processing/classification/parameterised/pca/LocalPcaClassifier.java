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
import rseslib.system.Report;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.Progress;

/**
 * Classifier that assigns the decision
 * calculated on the basis of principal
 * subspaces of clusters in decision classes.
 *
 * @author      Rafal Falkowski
 */
public class LocalPcaClassifier extends AbstractParameterisedClassifier
{
    /** Parameter name. */
    public static final String PRINCIPAL_SUBSPACE_DIM = "principalSubspaceDim";
    /** Parameter name. */
    private static final String OPTIMAL_DIM = "optimalDimension";
    /** Name of the property that indicates number of local linear models for each decision class. */
    public static final String NUMBER_OF_CLUSTERS = "noOfLocalLinearModels";
    /** The thereshold of the relative error of clusterization. */
    public static final double TAU = 0.000001;
    /** Parameter of clusterization distance. */
    public static final double ALFA = 1.5;
    /** Maximal number of iterations of clusterization. */
    public static final int MAX_IT = 100;
    /** The default decision defined by the largest support in a training data set. */
    private int m_nDefaultDec;
    /** Array of principal subspaces for each decision class. */
    private PCASubspace[] m_nSubspaces;
    /** Number of decisions. */
    private int m_nNoOfDec;
    /** Decision attribute. */
    NominalAttribute m_DecisionAttribute;
    /** The dimension of the data space, i.e. number of attributes of data without decision attribute. */
    private int m_nDim = 0;
    /** The number of local linear models for each decision class. */
    private int m_nNoOfClusters = 1;
    /** Parameter value, i.e. the dimension of the principal subspace. */
    private int m_nPar;
    /** Optimal dimension of the principal subspace. */
    private int m_nOptimal;

    /**
     * Constructor computes principal subspaces for each cluster in decision classes.
     *
     * @param prop                Parameters of this clasifier. If null,
     *                            parameters are loaded from the configuration directory.
     * @param trainTable          Table used to train classifier.
     * @param prog                Progress object for training process.
     * @throws InterruptedException when the user interrupts the execution.
     */
        public LocalPcaClassifier(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, BadHeaderException, InterruptedException
        {
                super(prop, OPTIMAL_DIM);
                boolean numericNotFound = true;
                for (int at = 0; numericNotFound && at < trainTable.attributes().noOfAttr(); at++)
                    if (trainTable.attributes().isConditional(at) && trainTable.attributes().isNumeric(at))
                        numericNotFound = false;
                if (numericNotFound) throw new BadHeaderException("Local pca classifier requires numerical attributes");
                prog.set("Learning LPCA classifier from training table", 1);
                NominalAttribute decAttr = trainTable.attributes().nominalDecisionAttribute();
                int[] decDistr = trainTable.getDecisionDistribution();
                m_nDefaultDec = 0;
                m_DecisionAttribute = decAttr;
                m_nNoOfDec = m_DecisionAttribute.noOfValues();
                m_nNoOfClusters = getIntProperty(NUMBER_OF_CLUSTERS);
                m_nPar = getIntProperty(PRINCIPAL_SUBSPACE_DIM);

                for (int dec = 1; dec < m_nNoOfDec; dec++)
                        if (decDistr[dec] > decDistr[m_nDefaultDec]) m_nDefaultDec = dec;

                // Okreslenie ile jest atrybutow warunkowych.
                for (int att = 0; att < trainTable.attributes().noOfAttr(); att++)
                        if (trainTable.attributes().isConditional(att))	m_nDim++;

                // Podzial tabeli na klasy decyzyjne.
//                HashMap decName = new HashMap(); // nazwy decyzji, wg ktorych klasyfikujemy obiekty
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

                // Wygenerowanie podprzestrzeni glownych i ich macierzy projekcji dla poszczegolnych klas decyzyjnych.
                m_nSubspaces = new PCASubspace[m_nNoOfDec*m_nNoOfClusters];
                for (int i = 0; i < m_nNoOfDec; i++) // dla kazdej decyzji
                {
                        // przygotowanie duzej petli w metodzie
                        ArrayList<DoubleData>[] compUnit = new ArrayList[m_nNoOfClusters]; // agenci obliczeniowi
                        Vector[] centroid = new VectorForDoubleData[m_nNoOfClusters]; // centroidy klastrow
                        for (int cl = 0; cl < m_nNoOfClusters; cl++) //	inicjalizacja klastrow
                        {
                          compUnit[cl] = new ArrayList<DoubleData>();
                          // losowo wybrane wektory z i-tej klasy decyzyjnej staja sie centoidami
                          int pom = (int)(Math.random()*decisionDistr[i].size());
                          centroid[cl] = new VectorForDoubleData((DoubleData)decisionDistr[i].get(pom));
                        }
                        int noOfEpoch = 0;
                        double clusterErr = 0;
                        // DUZA PETLA W METODZIE:
                        // odleglosc najblizszego klastra z poprzedniej epoki dla kazdego obiektu inna!!!
                        double[] rhoCentroidPrev = new double[decisionDistr[i].size()];
                        double[] rhoPrev = new double[decisionDistr[i].size()];
                        for (int k = 0; k < decisionDistr[i].size(); k++)
                        {
                          rhoCentroidPrev[k] = -1;
                          rhoPrev[k] = -1;
                        }
                        // numer najblizszego klastra z poprzedniej epoki dla kazdego obiektu inny!!!
                        int[] bestClNo = new int[decisionDistr[i].size()];

                        while (1 > 0)
                        {
                          noOfEpoch++;
                          double prevClusterErr = clusterErr;
                          clusterErr = 0;
                          // licznik obiektow
                          int k = 0;
                          for (DoubleData dObj : decisionDistr[i])
                          {
                            double rhoCurr = -1;
                            int bCN = 0;
                            if (noOfEpoch == 1)
                            {
                              for (int cl = 0; cl < m_nNoOfClusters; cl++)
                              {
                                Vector dVec = new VectorForDoubleData(dObj);
                                dVec.subtract(centroid[cl]);
                                double rho1 = dVec.euclideanNorm();
                                if (rhoCurr < 0 || rhoCurr > rho1)
                                {
                                  rhoCurr = rho1;
                                  rhoCentroidPrev[k] = rho1;
                                  rhoPrev[k] = rho1;
                                  bestClNo[k] = cl;
                                }
                              }
                            } else
                            {
                                double rhoCentr = 0;
                                for (int cl = 0; cl < m_nNoOfClusters; cl++)
                                {
                                  Vector dVec = new VectorForDoubleData(dObj);
                                  double rho1 = m_nSubspaces[i*m_nNoOfClusters+cl].euclideanDist(dVec);
                                  if (rhoCurr < 0 || rhoCurr > rho1)
                                  {
                                    rhoCurr = rho1;
                                    bCN = cl;
                                    dVec.subtract(centroid[cl]);
                                    rhoCentr = dVec.euclideanNorm();
                                  }
                                }
                                if (rhoCentr < ALFA*rhoCentroidPrev[k])
                                {
                                  rhoCentroidPrev[k] = rhoCentr;
                                  bestClNo[k] = bCN;
                                  rhoPrev[k] = rhoCurr;
                                }
                              }
                              clusterErr += rhoPrev[k];
                              compUnit[bestClNo[k]].add(dObj);
                              k++;
                            }
                            for (int cl = 0; cl < m_nNoOfClusters; cl++)
                            {
                              if (compUnit[cl].isEmpty())
                              {
                                int pom = (int)(Math.random()*decisionDistr[i].size());
                                DoubleData dObj = (DoubleData)decisionDistr[i].get(pom);
                                centroid[cl] = new VectorForDoubleData(dObj);
                                compUnit[cl].add(dObj);
                                rhoCentroidPrev[pom] = 0;
                                rhoPrev[pom] = 0;
                                bestClNo[pom] = cl;
                              }
                              m_nSubspaces[i * m_nNoOfClusters + cl] = new PCASubspace(compUnit[cl], m_nDim, m_nPar);
                              centroid[cl] = new VectorForDoubleData((Vector)m_nSubspaces[i * m_nNoOfClusters + cl].getCentroid());
                            }
                            if (Math.abs(prevClusterErr - clusterErr)/clusterErr < TAU || noOfEpoch >= MAX_IT) break;
                            for (int cl = 0; cl < m_nNoOfClusters; cl++)
                              compUnit[cl].clear();
                        }
                }
                learnOptimalParameterValue(trainTable, new EmptyProgress());
                m_nOptimal = getIntProperty(OPTIMAL_DIM);
                prog.step();
    }

        /**
         * Classifies a test object on the basis of the dimension of the principal subspace
         * and number of clusters in each decision class.
         *
         * @param dObj         Test object.
         * @return             Array of assigned decisions, indices correspond to parameters values.
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
                for (int i = 0; i < m_nNoOfDec*m_nNoOfClusters; i++)
                {
                        Vector[] px = m_nSubspaces[i].projections(new VectorForDoubleData(dObj));
                        for (int n = 1; n < decisions.length; n++)
                        {
                                px[n].subtract(x);
                                resid[n] = px[n].squareEuclideanNorm();
                                if (minResid[n] < 0 || minResid[n] > resid[n])
                                {
                                        minResid[n] = resid[n];
                                        bestDec[n] = i / m_nNoOfClusters;
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
            try
            {
                addToStatistics("Number of clusters in each decision class", Integer.toString(getIntProperty(NUMBER_OF_CLUSTERS)));
                addToStatistics("Principal subspace dimension in each cluster", Integer.toString(m_nOptimal));
            }
            catch (PropertyConfigurationException e)
            {
                Report.exception(e);
            }
        }

        /**
         * Resets statistics.
         */
        public void resetStatistics()
        {
        }
}

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


package rseslib.processing.classification.svm;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import rseslib.processing.classification.Classifier;
import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;


/**
 * Main class for support vector machine clasifier
 */
public class SVM extends ConfigurationWithStatistics implements Classifier {

    /**
     * The database of the training examples.
     */
    ArrayList<DoubleData> m_Database;

    /**
     * Training table
     */
    DoubleDataTable tab;

    /**
     * SVM's kernel function.
     */
    KernelFunction kernel;

    /**
     * Global code for first decision class
     */
    double code1=0;

    /**
     * Global code for second decision class
     */
    double code2=0;

    /**
     * C parameter
     */
    public double C=0.05;

    /**
     * Numerical tolerance
     */
    public double tolerance=0.001;

    /**
     * Alphas - Lagrangian multipliers.
     */
    double[][] alphas;

    /**
     * Errors cache.
     */
    double[] errors;

    /**
     * Treshold
     */
    double[] b;

    /**
     * Treshold's delta
     */
    double delta_b;

    /**
     * Epsilon
     */
    public double eps=0.001;

    /**
     * Object for getting random values
     */
    Random r;

    /**
     * Type of kernel
     */
    String kernelType = "linear";

    /**
     * Number of possible decisions
     */
    int noOfDec = 0;
    // Table of class decion codes.
    ArrayList<Double> classes;
    // pointers
    int curr1=0;
    int curr2=0;
    int curr=0;

    // Kernel functions parameters
    int polynomial_degree = 2;
    int polynomial_add = 1;
    double rbf_sigma = 0.5;
    double expotential_sigma = 0.5;
    double sigmoid_kappa = 1;
    double sigmoid_theta = 0;

    /**
     * Class constructor
     * @param prop classifier's properties
     * @param t test table
     * @param prog progress object to report training progress.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public SVM(Properties prop, DoubleDataTable t, Progress prog) throws PropertyConfigurationException, BadHeaderException, InterruptedException {
        super(prop);
        boolean allNumeric = true;
        for (int at = 0; allNumeric && at < t.attributes().noOfAttr(); at++)
            if (t.attributes().isConditional(at) && !t.attributes().isNumeric(at))
                allNumeric = false;
        if (!allNumeric) throw new BadHeaderException("SVM classifier requires all attributes to be numerical");
        prog.set("Learning SVM classifier", 1);
        tab = t;
        m_Database = new ArrayList<DoubleData>(tab.getDataObjects());
        // setting classifier's parameters
        preProcess();
        r=new Random();
        classes = new ArrayList<Double>();
        // running test on test table
        runTest();
        prog.step();
    }

    /**
     * reading and setting parameters
     */
    private void preProcess() throws PropertyConfigurationException {
        kernelType = getProperty("kernel");
        if (kernelType.equals("linear")) {
            kernel = new LinearKernelFunction(tab);
        } else if (kernelType.equals("polynomial")) {
            polynomial_degree = getIntProperty("polynomial_degree");
            polynomial_add = getIntProperty("polynomial_add");
            kernel = new PolynomialKernelFunction(tab,polynomial_degree,polynomial_add);
        } else if (kernelType.equals("rbf")) {
            rbf_sigma = getDoubleProperty("rbf_sigma");
            kernel = new RBFKernelFunction(tab,rbf_sigma);
        } else if (kernelType.equals("sigmoid")) {
            sigmoid_kappa = getDoubleProperty("sigmoid_kappa");
            sigmoid_theta = getDoubleProperty("sigmoid_theta");
            kernel = new SigmoidKernelFunction(tab,sigmoid_kappa,sigmoid_theta);
        } else if (kernelType.equals("expotential")) {
            expotential_sigma = getDoubleProperty("expotential_sigma");
            kernel = new RBFKernelFunction(tab,expotential_sigma);
        }
        C = getDoubleProperty("C");
        tolerance = getDoubleProperty("tolerance");
        eps = getDoubleProperty("epsilon");
    }

    /**
     * returns +1 or -1 due to svm binary classification process
     * @param index - index of object in database table
     * @return +1 or -1 for binary classification
     */
    private double getDec (int index) {
        if ((((DoubleDataWithDecision)m_Database.get(index)).getDecision())==code1)
            return 1;
        else return -1;
    }

    /**
     * returns kernel function value for two data ojects
     * @param i1 index of first object in database
     * @param i2 index of second object in database
     * @return kernel function value for two objects
     */
    private double K(int i1,int i2) {
        return kernel.K((DoubleDataWithDecision)m_Database.get(i1),(DoubleData)m_Database.get(i2));
    }

    /**
     * counts number of possible decions
     */
    void countClasses () {
        Double d;
        for (int i = 0;i < m_Database.size();i++) {
            d = new Double (((DoubleDataWithDecision)m_Database.get(i)).getDecision());
            if(!classes.contains(d)) {
                classes.add(d);
                noOfDec++;
            }
        }
    }

    /**
     * testing test table
     */
    public void runTest() {
        // count number of possible decisions
        countClasses();
        if (noOfDec<2)
            throw new RuntimeException("There must be at least two decisions in SVM's");
        // table for Lagrange multipliers for each object in each of classifications processes
        alphas = new double[tab.noOfObjects()][noOfDec * (noOfDec - 1) / 2];
        // numerical errors for each object
        errors = new double[tab.noOfObjects()];
        // parameter b for each classification process
        b=new double[noOfDec * (noOfDec - 1) / 2];

        // here we construct k(k-1)/2 binary classifiers, where k is number of decisions
        // each binary classifier is based on SMO algorithm
        for(curr1=0;curr1<noOfDec-1;curr1++) {
            for(curr2=curr1+1;curr2<noOfDec;curr2++) {
                int numChanged = 0;
                boolean examineAll = true;
                code1 = ((Double)classes.get(curr1)).doubleValue();
                code2 = ((Double)classes.get(curr2)).doubleValue();
                while (numChanged > 0 || examineAll) {
                    numChanged = 0;
                    if (examineAll) {
                        for (int i = 0; i < m_Database.size(); i++) {
                            if (isConsidered(i))
                                numChanged += examineExample(i);
                        }
                    } else {
                        for (int i = 0; i < m_Database.size(); i++) {
                            if (alphas[i][curr] != 0 && alphas[i][curr] != C && isConsidered(i))
                                numChanged += examineExample(i);
                        }
                    }
                    if (examineAll)
                        examineAll = false;
                    else if (numChanged == 0)
                        examineAll = true;
                }
                curr++;
                for (int l=0;l<tab.noOfObjects();l++)
                    errors[l]=0;
            }
        }
    }

    /**
     * returns true if this object takes part in current binary classification
     * @param i object's index
     * @return true if this object takes part in current binary classification
     */
    private boolean isConsidered(int i) {
        return (((((DoubleDataWithDecision)m_Database.get(i)).getDecision())==code1) || ((((DoubleDataWithDecision)m_Database.get(i)).getDecision())==code2));
    }

    /**
     * returns value of so far learned function for given object
     * @param index object's index
     * @return value of so far learned function for given object
     */
    private double learned_function (int index) {
        double ret = 0;
        for (int i=0; i<m_Database.size(); i++)
            if (alphas[i][curr] > 0 && isConsidered(i)) {
                ret += alphas[i][curr] * getDec(i) * K(i,index);
            }
        ret -= b[curr];
        return ret;
    }

    /**
     *
     * @param i1 index of first Lagrange multiplier
     * @return 1 if two alphas were optimized, 0 instead
     */
    private int examineExample (int i1) {
        // decision value
        double y1=getDec(i1);
        // Lagrange multiplier value
        double alpha1=alphas[i1][curr];
        double E1;
        double r1;

        if (alpha1 > 0 && alpha1 < C)
            E1 = errors[i1];
        else
            E1 = learned_function(i1) - y1;

        r1 = y1 * E1;
        if ((r1 < -tolerance && alpha1 < C) || (r1 > tolerance && alpha1 > 0)) {
            int i2=-1;
            double tmax=0;
            for (int i = 0; i < m_Database.size(); i++)
                if (alphas[i][curr] > 0 && alphas[i][curr] < C && isConsidered(i)) {
                    double E2=errors[i];
                    double temp=Math.abs(E1 - E2);
                    if (temp > tmax) {
                        tmax = temp;
                        i2 = i;
                    }
                }
            if (i2 >= 0 && isConsidered(i2)) {
                if (takeStep (i1, i2)) {
                    return 1;
                }
            }
            int off=r.nextInt(m_Database.size());
            i2=0;
            for (int i = off; i < m_Database.size()+off; i++) {
                i2 = i % m_Database.size();
                if (alphas[i2][curr] > 0 && alphas[i2][curr] < C && isConsidered(i2)) {
                    if (takeStep(i1, i2)) {
                        return 1;
                    }
                }
            }
            i2=0;
            off=r.nextInt(m_Database.size());
            for (int i = off; i < m_Database.size()+off; i++) {
                i2 = i % m_Database.size();
                if(isConsidered(i2))
                    if (takeStep(i1, i2)) {
                        return 1;
                    }
            }
        }
        return 0;
    }

    /**
     * Optimization of two chosen alphas
     * @param i1 index of first alpha
     * @param i2 index of second alpha
     * @return true if optimisation succesful, false otherwise
     */
    private boolean takeStep (int i1, int i2) {
        double y1, y2, s;
        double alpha1=0, alpha2=0; /* old_values of alpha_1, alpha_2 */
        double a1, a2;       /* new values of alpha_1, alpha_2 */
        double E1, E2, L, H, Lobj=0, Hobj=0;

        if (i1 == i2)
            return false;

        alpha1 = alphas[i1][curr];
        y1 = getDec(i1);
        if (alpha1 > 0 && alpha1 < C)
            E1 = errors[i1];
        else
            E1 = learned_function(i1) - y1;

        alpha2 = alphas[i2][curr];
        y2 = getDec(i2);
        if (alpha2 > 0 && alpha2 < C)
            E2 = errors[i2];
        else
            E2 = learned_function(i2) - y2;

        s = y1 * y2;

        if (y1 == y2) {
            double gamma = alpha1 + alpha2;
            if (gamma > C) {
                L = gamma-C;
                H = C;
            } else {
                L = 0;
                H = gamma;
            }
        } else {
            double gamma = alpha1 - alpha2;
            if (gamma > 0) {
                L = 0;
                H = C - gamma;
            } else {
                L = -gamma;
                H = C;
            }
        }
        if (L == H) {
            return false;
        }

        double k11 = K(i1, i1);
        double k12 = K(i1, i2);
        double k22 = K(i2, i2);
        double eta = 2 * k12 - k11 - k22;

        if (eta < 0) {
            a2 = alpha2 + y2 * (E2 - E1) / eta;
            if (a2 < L)
                a2 = L;
            else if (a2 > H)
                a2 = H;
        } else {
            double c1 = eta/2;
            double c2 = y2 * (E1-E2)- eta * alpha2;
            Lobj = c1 * L * L + c2 * L;
            Hobj = c1 * H * H + c2 * H;
            if (Lobj > Hobj+eps)
                a2 = L;
            else if (Lobj < Hobj-eps)
                a2 = H;
            else
                a2 = alpha2;
        }

        if (Math.abs(a2-alpha2) < eps*(a2+alpha2+eps))
            return false;

        a1 = alpha1 - s * (a2 - alpha2);
        if (a1 < 0) {
            a2 += s * a1;
            a1 = 0;
        } else if (a1 > C) {
            double t = a1 - C;
            a2 += s * t;
            a1 = C;
        }
        double b1, b2, bnew;
        if (a1 > 0 && a1 < C)
            bnew = b[curr] + E1 + y1 * (a1 - alpha1) * k11 + y2 * (a2 - alpha2) * k12;
        else {
            if (a2 > 0 && a2 < C)
                bnew = b[curr] + E2 + y1 * (a1 - alpha1) * k12 + y2 * (a2 - alpha2) * k22;
            else {
                b1 = b[curr] + E1 + y1 * (a1 - alpha1) * k11 + y2 * (a2 - alpha2) * k12;
                b2 = b[curr] + E2 + y1 * (a1 - alpha1) * k12 + y2 * (a2 - alpha2) * k22;
                bnew = (b1 + b2) / 2;
            }
        }
        delta_b = bnew - b[curr];
        b[curr] = bnew;
        double t1 = y1 * (a1-alpha1);
        double t2 = y2 * (a2-alpha2);
        for (int i = 0; i < m_Database.size(); i++)
            if (0 < alphas[i][curr] && alphas[i][curr] < C && isConsidered(i)) {
                double temp = errors[i];
                temp +=  t1 * K(i1,i) + t2 * K(i2,i) - delta_b;
                errors[i] = temp;
            }
        errors[i1] = 0;
        errors[i2] = 0;
        alphas[i1][curr] = a1;
        alphas[i2][curr] = a2;
        return true;
    }

    /**
     * classification part
     * @param dObj data object
     * @return decision code
     */
    public double classify(DoubleData dObj) {
        // table for counting votes
        int[] votes = new int[noOfDec];
        int c=0;
        // iteration through all the binary classifiers
        for(int c1=0;c1<noOfDec-1;c1++) {
            for(int c2=c1+1;c2<noOfDec;c2++) {
                code1 = ((Double)classes.get(c1)).doubleValue();
                code2 = ((Double)classes.get(c2)).doubleValue();
                double sum = 0;
                for (int i = 0;i < m_Database.size();i++) {
                    if (alphas[i][c] != 0) {
                        sum+=alphas[i][c]*getDec(i)*(kernel.K(dObj,(DoubleData)m_Database.get(i)));
                    }
                }
                sum-=b[c];
                if (sum >= 0) {
                    votes[c1]++;
                }
                else {
                    votes[c2]++;
                }
                c++;
            }
        }
        int max = -1;
        c=-1;
        // max votes win, first in the table if no max
        for (int j=0; j<noOfDec;j++) {
            if (votes[j]>max) {
                max=votes[j];
                c=j;
            }
        }
        return ((Double)classes.get(c)).doubleValue();
    }

    /**
     * statistics
     */
    public void calculateStatistics() {
        addToStatistics("kernel function",kernel.getClass().getName().substring(kernel.getClass().getName().lastIndexOf('.')+1,kernel.getClass().getName().length()));
        if (kernelType.equals("rbf"))
            addToStatistics("sigma", String.valueOf(rbf_sigma));
        if (kernelType.equals("polynomial")) {
            addToStatistics("degree", String.valueOf(polynomial_degree));
            addToStatistics("a", String.valueOf(polynomial_add));
        }
        if (kernelType.equals("sigmoid")) {
            addToStatistics("kappa", String.valueOf(sigmoid_kappa));
            addToStatistics("theta", String.valueOf(sigmoid_theta));
        }
        if (kernelType.equals("expotential"))
            addToStatistics("sigma",String.valueOf(expotential_sigma));
    }

    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

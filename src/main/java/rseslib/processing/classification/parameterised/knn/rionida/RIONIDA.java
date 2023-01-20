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

package rseslib.processing.classification.parameterised.knn.rionida;

import rseslib.processing.classification.ClassifierWithDistributedDecision;
import rseslib.processing.classification.SingleClassifierTest;
import rseslib.processing.classification.TestResult;
import rseslib.processing.classification.parameterised.SingleParameter;
import rseslib.processing.classification.parameterised.knn.CubeBasedNeighboursFilter;
import rseslib.processing.indexing.metric.TreeIndexer;
import rseslib.processing.metrics.MetricFactory;
import rseslib.processing.searching.metric.ArrayVicinityProvider;
import rseslib.processing.searching.metric.IndexingTreeVicinityProvider;
import rseslib.processing.searching.metric.VicinityProvider;
import rseslib.processing.transformation.AttributeTransformer;
import rseslib.processing.transformation.TableTransformer;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.index.metric.IndexingTreeNode;
import rseslib.structure.metric.AbstractWeightedMetric;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.table.ArrayListDoubleDataTable;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.EmptyProgress;
import rseslib.system.progress.MultiProgress;
import rseslib.system.progress.Progress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

/**
 * Classifier combining k nearest neighbors with rule-based method
 * dedicated to imbalanced data. 
 *
 * @author Grzegorz Gora
 */
public class RIONIDA extends AbstractParameterised3DClassifier implements ClassifierWithDistributedDecision, Serializable
{
	/** Attribute weighting methods. */
	public enum Voting { Equal, InverseDistance, InverseSquareDistance; }

    /** Serialization version. */
	private static final long serialVersionUID = 1L;
    /** Name of property indicating whether the majority decision is used as the minority decision. */
    public static final String MAJORITY_AS_MINORITY_PROPERTY_NAME = "useMajorityDecAsMinorityDec";
    /** Property name for weighting method. */
    public static final String WEIGHTING_METHOD_PROPERTY_NAME = "weightingMethod";
    /** Name of property indicating whether the classifier uses indexing to accelerate search of nearest neighbours. */
    public static final String INDEXING_PROPERTY_NAME = "indexing";
    /** Name of property indicating whether the classifier learns the optimal values of the parameters. */
    public static final String LEARN_OPTIMAL_PARAMETERS_PROPERTY_NAME = "learnOptimalParameters";
    /** Number of nearest neighbours voting for decision of a test object. */
    public static final String K_PROPERTY_NAME = "k";
    /** Name of property defining the maximal number of k while learning the optimal value. */
    public static final String MAXIMAL_K_PROPERTY_NAME = "maxK";
    /** Name of property indicating weight of minority to majority threshold. */
    public static final String P_VALUE_THRESHOLD_PROPERTY_NAME = "pThreshold";
    /** Name of property defining the minimal possible value while learning the optimal pThreshold. */
    public static final String P_VALUE_THRESHOLD_MIN_PROPERTY_NAME = "pThresholdMin";
    /** Name of property defining the maximal possible value while learning the optimal pThreshold. */
    public static final String P_VALUE_THRESHOLD_MAX_PROPERTY_NAME = "pThresholdMax";
    /** Name of property defining the density of values between pThresholdMin and pThresholdMax considered while learning the optimal pThreshold. */
    public static final String P_VALUE_THRESHOLD_STEP_PROPERTY_NAME = "pThresholdStep";
    /** Name of property consistency level for minority class. Values for this property in code is used to be called sMinorityValue. */
    public static final String S_MINORITY_VALUE_PROPERTY_NAME = "sMinority";
    /** Name of property defining the minimal possible value while learning the optimal sMinority. */
    public static final String S_MINORITY_MIN_VALUE_PROPERTY_NAME = "sMinorityMin";
    /** Name of property defining the maximal possible value while learning the optimal sMinority. */
    public static final String S_MINORITY_MAX_VALUE_PROPERTY_NAME = "sMinorityMax";
    /** Name of property defining the density of values between sMinorityMin and sMinorityMax considered while learning the optimal sMinority. */
    public static final String S_MINORITY_STEP_VALUE_PROPERTY_NAME = "sMinorityStep";
    /** Name of property consistency level for minority class. Values for this property in code is used to be called sMajorityValue. */
    public static final String S_MAJORITY_VALUE_PROPERTY_NAME = "sMajority";
    /** Name of property defining the minimal possible value while learning the optimal sMajority. */
    public static final String S_MAJORITY_MIN_VALUE_PROPERTY_NAME = "sMajorityMin";
    /** Name of property defining the maximal possible value while learning the optimal sMajority. */
    public static final String S_MAJORITY_MAX_VALUE_PROPERTY_NAME = "sMajorityMax";
    /** Name of property defining the density of values between sMajorityMin and sMajorityMax considered while learning the optimal sMajority. */
    public static final String S_MAJORITY_STEP_VALUE_PROPERTY_NAME = "sMajorityStep";
    /** Name of property indicating whether consistency checking is considered. */
    public static final String FILTER_NEIGHBOURS_PROPERTY_NAME = "filterNeighboursUsingRules";
    /** Name of property indicating whether neighbour voting is weighted with distance. */
    public static final String VOTING_PROPERTY_NAME = "voting";

	public enum OptimisationMethod { LeaveOneOut, StratifiedCV };
	public static OptimisationMethod s_optimisationMethod = OptimisationMethod.LeaveOneOut; //standardowo
//	public static OptimisationMethod s_optimisationMethod = OptimisationMethod.StratifiedCV;
	
	//jak traktowa� sprzeczno�ci - chodzi o sytuacj� kiedy normalnie otrzymuj� zerowy rozk�ad
	//oddzielnie traktuj� faz� uczenia i oddzielnie faz� klasyfikacji
	public enum HowToTreatInconsistency {	ZeroDistCount,			//je�li si� pojawi� obiekty odleg�e od testowego o 0.0, to zlicz je i zwr�� ich rozk�ad
											ZeroAndEqDistCount};	//jak wy�ej, ale je�li powy�sze daje zerowy rozk�ad, to policz obiekty odleg�e tak samo jak pierwszy obiekt
//	public static HowToTreatInconsistency s_howToTreatInconsistencyForLearning = HowToTreatInconsistency.ZeroAndEqDistCount; //standardowo
	public static HowToTreatInconsistency s_howToTreatInconsistencyForLearning = HowToTreatInconsistency.ZeroDistCount;
//	public static HowToTreatInconsistency s_howToTreatInconsistencyForClassification = HowToTreatInconsistency.ZeroAndEqDistCount; //standardowo
	public static HowToTreatInconsistency s_howToTreatInconsistencyForClassification = HowToTreatInconsistency.ZeroDistCount;
	//czy zastosowa� przyspieszenie w sytuacji gdy wyst�puj� obiekty odleg�e o 0.0 od testowego i maj� r�ne decyzje - mo�na od razu bez dalszych oblicze� na pocz�tku zwr�ci� wynik
	public static boolean s_zeroDistFastLearning = true; //na razie zak�adam, �e powinno by� ustawione na true (przy okazji jest wi�ksza czytelno�� kodu)
	public static boolean s_zeroDistFastClassification = true; //na razie zak�adam, �e powinno by� ustawione na true (przy okazji jest wi�ksza czytelno�� kodu)

    /** Collection of the original training data objects. */  
    private ArrayList<DoubleData> m_OriginalData;
//    protected ArrayList<DoubleData> m_OriginalData;//zmieni�em ggora 1.06.2016
    /** Data transoformer used in the induced metric. */
    AttributeTransformer m_Transformer;
    /** Transformed training data. */
    DoubleDataTable m_TransformedTrainTable;
//    protected DoubleDataTable m_TransformedTrainTable; //zmieni�em ggora 1.06.2016
    /** The induced metric. */
    Metric m_Metric;
    /** Provider of vicinity for test data objects. */
    VicinityProvider m_VicinityProvider;
    /** Filter for neigbours using cubes on objects and consistency. */
    private CubeBasedNeighboursFilter m_NeighboursFilter;
    /** Switch to recognize whether searching for optimal k is going on. */
    private boolean m_bSelfLearning = false;
    /** Maximal value k in parameterised classification. */
    private int m_nMaxK;
    /**
     * if weight of minority class divided by sum of weight of classes (normally 2 classes) is greater than or equal to this threshold
     * then minority class is chosen by classifier, otherwise majority class is chosen
     * (this threshold in code is used to be called pValue; in properties it is called pThreshold)
     * dodane ggora 25.07.2016
     */
//    private double m_pValueThreshold = 0.5;
//wykomentowa�em 6.08.2016 - ju� tego nie u�ywam
//    /**
//     * weights for decision values, usually 2decisions, usually weight for minority class is greater than for majority class
//     * dodane ggora 27.07.2016
//     */
//    double[] m_arrDecisionWeights;
    /**
     * Minority class global code
     * dodane ggora 27.07.2016
     */
    private double m_dMinorityDecGlobalCode = -1;
    
    /** Decision attribute. */
//    private NominalAttribute m_DecisionAttribute;
    protected NominalAttribute m_DecisionAttribute; //zmieni�em ggora 24.05.2016
    /** The default decision defined by the largest support in a training data set. */
    private int m_nDefaultDec;
//    /** The minority decision. */
//    private int m_nMinorityDec = -1;
    
    /**
     * Constructor that induces a metric
     * from a given training set trainTable
     * and constructs an indexing tree.
     * It transforms data objects inside the constructor.
     *
     * @param prop                   Properties of this knn clasifier.
     * @param trainTable             Table used to build vicinity provider and to learn the optimal value of the classifier parameter.
     * @param prog                   Progress object to report training progress.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public RIONIDA(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
//    	super(prop, K_PROPERTY_NAME); //stare
    	super(prop); //zmieni�em ggora 6.08.2016
    	if (trainTable.attributes().nominalDecisionAttribute().noOfValues() != 2)
    		throw new IllegalArgumentException("RIONIDA supports a binary decision attribute only");
    	// prepare progress information
        int[] progressVolumes = null;
//        if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME)) //stare
          if (getBoolProperty(LEARN_OPTIMAL_PARAMETERS_PROPERTY_NAME)) //zmiana ggora 4.08.2016
        {
            progressVolumes = new int[3];
            progressVolumes[0] = 40;
            progressVolumes[1] = 10;
            progressVolumes[2] = 50;
        }
        else
        {
            progressVolumes = new int[2];
            progressVolumes[0] = 80;
            progressVolumes[1] = 20;
        }
        prog = new MultiProgress("Learning the k-nn classifier", prog, progressVolumes);
        // induce a metric and transform training objects for optimization of distance computations 
        m_OriginalData =  trainTable.getDataObjects();
        m_Metric = MetricFactory.getMetric(getProperties(), trainTable);
//wykomentowa� je�li chc� wypisywa� czytelnie kostki; odkomentowa� je�li chc� przyspieszy�
        m_Transformer = m_Metric.transformationOutside(); //wykomentowane chwilowo ggora (mail od Arka) UWaga: mo�e spowolni� uczenie i klasyfikacje
        m_TransformedTrainTable = trainTable;
        if (m_Transformer!=null)
        	m_TransformedTrainTable = TableTransformer.transform(trainTable, m_Transformer);
        if (m_Metric instanceof AbstractWeightedMetric)
        	MetricFactory.adjustWeights(getProperty(WEIGHTING_METHOD_PROPERTY_NAME), (AbstractWeightedMetric)m_Metric, m_TransformedTrainTable, prog);
        // index the training objects
//Wykomentowane 2 poni�sze linijki i dodane 3 kolejne: patrz mail Arka 3.08.2015 (przyspieszenie poprzez wyeliminowanie drzewa indeksuj�cego)
//        IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(m_TransformedTrainTable.getDataObjects(), m_Metric, prog);
//        m_VicinityProvider = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);
//        prog.set("Constructing simple vicinity provider", 1);
//        m_VicinityProvider = new ArrayVicinityProvider(m_Metric, m_TransformedTrainTable.getDataObjects());
//        prog.step();
//zamiast tego co powy�ej Arek doda� co� takiego (indeksowanie ustalone w parametrze):
        if(getBoolProperty(INDEXING_PROPERTY_NAME))
        {
        	// index the training objects
        	IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(m_TransformedTrainTable.getDataObjects(), m_Metric, prog);
        	m_VicinityProvider = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);
        } else {
            prog.set("Constructing simple vicinity provider", 1);
            m_VicinityProvider = new ArrayVicinityProvider(m_Metric, m_TransformedTrainTable.getDataObjects());
            prog.step();
        }
        
        // store information required in classification 
        if (m_Metric instanceof AbstractWeightedMetric)
        	m_NeighboursFilter = new CubeBasedNeighboursFilter((AbstractWeightedMetric)m_Metric, m_Transformer!=null);
//        m_nMaxK = getIntProperty(MAXIMAL_K_PROPERTY_NAME);
//        if (m_nMaxK > trainTable.noOfObjects()-1) {
////        	throw new IllegalArgumentException("na razie zak�adam, �e maxK jest mniejsze r�wne od rozmiaru tabelki-1 - mo�na to ewentualnie zmieni� (m_nMaxK=" + m_nMaxK + ", trainTable.noOfObjects()=" + trainTable.noOfObjects() + ")"); //tak by�o wcze�niej - zmieni�em ggora w XI 2016
//        	m_nMaxK = trainTable.noOfObjects() - 1;
//        	Report.debugnl("Uwaga: Powinno by� spe�nione: maxK <= rozmiarTabelki - 1. Zatem ustawiam maxK=" + m_nMaxK + ", bo trainTable.noOfObjects()=" + trainTable.noOfObjects());
//        }
//ggora 20.05.2017 na razie ustawiam maksymalnie po�owa rozmiaru tabelki
//@todo zmieni� to chyba na sta�e
        m_nMaxK = getIntProperty(MAXIMAL_K_PROPERTY_NAME);
        if (m_nMaxK > (trainTable.noOfObjects())/2) {
        	m_nMaxK = (trainTable.noOfObjects())/2;
        	Report.debugnl("Uwaga: Powinno by� spe�nione: maxK <= rozmiarTabelki - 1. Zatem ustawiam maxK=" + m_nMaxK + ", bo trainTable.noOfObjects()=" + trainTable.noOfObjects());
        }

        setParameterValues(prop);
//    	saveMainRIONIDAparams();
//    	m_pValueThreshold = getDoubleProperty(P_VALUE_THRESHOLD_PROPERTY_NAME);
        m_DecisionAttribute = trainTable.attributes().nominalDecisionAttribute();
        m_nDefaultDec = 0;
        for (int dec = 1; dec < trainTable.getDecisionDistribution().length; dec++)
            if (trainTable.getDecisionDistribution()[dec] > trainTable.getDecisionDistribution()[m_nDefaultDec])
            	m_nDefaultDec = dec;
        m_dMinorityDecGlobalCode = m_DecisionAttribute.getMinorityValueGlobalCode();
        if(getBoolProperty(MAJORITY_AS_MINORITY_PROPERTY_NAME))
        	m_dMinorityDecGlobalCode = m_DecisionAttribute.globalValueCode(1 - m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode));
//        setDecisionWeights(); //doda�em ggora 27.07.2016 ; wykomentowa�em 6.08.2016 - ju� tego nie u�ywam
//    	if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME)) //stare
        if (getBoolProperty(LEARN_OPTIMAL_PARAMETERS_PROPERTY_NAME)) //zmiana ggora 4.08.2016
        {
            m_bSelfLearning = true;
            switch(s_optimisationMethod) {
            case LeaveOneOut:
//            	dla wersji alg.RIONIDA standardowego (z optymalizacj� metod� leave-one-out)
              //learnOptimalMultiDimParameterValue jest wywo�ywana z nadklasy, a ta wywo�uje metod� classifyWithMultiParameter - ta z kolei standardowo wywo�uje classifyWith3DParameter
              learnOptimalMultiDimParameterValue(trainTable, prog); //zmienione ggora 27.07.2016
            	break;
            case StratifiedCV:
//            	dla wersji alg.RIONIDA z optymalizacj� stratyfikowaln� CV
//            	Uwaga: tu ju� nie jest potrzebne m_bSelfLearning, ale zachowuj�, bo w niekt�rych miejscach chyba jest to sprawdzane i by�oby b��dnie
            	if (m_optimisation4D) throw new IllegalArgumentException("na razie tylko zrobione dla optymalizacji 3D, a jest ustawione m_optimisation4D=true");
            	learnOptimalParameterValueCV(m_TransformedTrainTable, prog); //tak powinno by� wg.Arka (patrz mail Arka 22.05.2017)
            	break;
			default:
				throw new IllegalArgumentException("niezdefiniowany case: s_optimisationMethod=" + RIONIDA.s_optimisationMethod);
            }
            m_bSelfLearning = false;
        }
        makePropertyModifiable(K_PROPERTY_NAME);
        makePropertyModifiable(P_VALUE_THRESHOLD_PROPERTY_NAME);
        makePropertyModifiable(S_MINORITY_VALUE_PROPERTY_NAME);
        makePropertyModifiable(S_MAJORITY_VALUE_PROPERTY_NAME);
        makePropertyModifiable(FILTER_NEIGHBOURS_PROPERTY_NAME);
        makePropertyModifiable(VOTING_PROPERTY_NAME);
    }

    /**
     * Dodane 20.05.2017 ggora (od Arka w mailu 27.12.2016)
     * Learn the optimal value of the parameter using cross-validation.
     *
     * @param trainTable Training data table.
     * @param prog       Progress object for optimal parameter value search.
     * @return           Optimal value of the parameter.
     * @throws InterruptedException when the user interrupts the execution.
     */
    protected void learnOptimalParameterValueCV(DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        // podzial danych na n czesci
        Collection<DoubleData>[] parts =  trainTable.randomStratifiedPartition(10);
        int[][][] confusionMatrices = null;
        prog.set("Learning optimal parameter value using cross-validation", trainTable.noOfObjects());
        
        for (int cv = 0; cv < parts.length; cv++)
        {
            // utworzenie tabeli treningowej i testowej
        	ArrayList<DoubleData> trn = new ArrayList<DoubleData>();
        	ArrayList<DoubleData> tst = new ArrayList<DoubleData>();
            for (int part = 0; part < parts.length; part++)
                if (part==cv)
                	tst.addAll(parts[part]);
                else
                	trn.addAll(parts[part]);
            VicinityProvider vicProv = null;
            if (getBoolProperty(INDEXING_PROPERTY_NAME))
            {
//            	IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(trn, m_Metric, prog);
            	IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(trn, m_Metric, new EmptyProgress()); //ggora poprawka 22.05.2017 (po mailu Arka 22.05.2017) 
            	vicProv = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);
            } else
            	vicProv = new ArrayVicinityProvider(m_Metric, trn);
            
            // klasyfikacja jednego foldu
            for (DoubleData dObj : tst)
            {
            	double[] decisions = classifyWith3DParameter(dObj, vicProv.getVicinity(dObj, m_nMaxK));
            	if (confusionMatrices==null)
            	{
            		confusionMatrices = new int[decisions.length][][];
            		for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
            		{
            			confusionMatrices[parVal] = new int[m_DecisionAttribute.noOfValues()][];
            			for (int i = 0; i < confusionMatrices[parVal].length; i++)
            				confusionMatrices[parVal][i] = new int[m_DecisionAttribute.noOfValues()];
            		}
            	}
            	for (int parVal = 1; parVal < confusionMatrices.length; parVal++)
            		confusionMatrices[parVal][m_DecisionAttribute.localValueCode(((DoubleDataWithDecision)dObj).getDecision())][m_DecisionAttribute.localValueCode(decisions[parVal])]++;
                prog.step();
            }
        }

//tak by�o dla algorytmu RIONA
//        // wyb�r najlepszego k
//        ParameterisedTestResult results = new ParameterisedTestResult(getParameterName(), m_DecisionAttribute, trainTable.getDecisionDistribution(), confusionMatrices, new Properties());
//        int bestParamValue = 0;
//        for (int parVal = 1; parVal < results.getParameterRange(); parVal++)
//            if (results.getClassificationResult(parVal).getAccuracy() > results.getClassificationResult(bestParamValue).getAccuracy())
//                bestParamValue = parVal;
//        makePropertyModifiable(K_PROPERTY_NAME);
//        setProperty(K_PROPERTY_NAME, Integer.toString(bestParamValue));
//zmiana ggora 20.05.2017 (dla algorytmu RIONIDA)
        Parameterised3DTestResult results = new Parameterised3DTestResult(getMultiDimParams(), m_DecisionAttribute, trainTable.getDecisionDistribution(), confusionMatrices, new Properties());
        int bestParamValue = 0;
        double bestValue = 0; //tylko dla infromacji i debug
        for (int parVal = 1; parVal < results.getParameterRange(); parVal++)
        {
        	switch (m_optimisationMeasure) {
			case Fmeasure:
	        	if (results.getClassificationResult(parVal).getFmeasure() > results.getClassificationResult(bestParamValue).getFmeasure()) {
	                bestParamValue = parVal;
	        		bestValue = results.getClassificationResult(parVal).getFmeasure();
	        	}
				break;
			case Gmean:
	           	if (results.getClassificationResult(parVal).getGmean() > results.getClassificationResult(bestParamValue).getGmean()) {
	                bestParamValue = parVal;
		           	bestValue = results.getClassificationResult(parVal).getGmean();
	           	}
				break;
			case Accuracy:
	            if (results.getClassificationResult(parVal).getAccuracy() > results.getClassificationResult(bestParamValue).getAccuracy()) {
	                bestParamValue = parVal;
		           	bestValue = results.getClassificationResult(parVal).getAccuracy();
	            }
				break;
			default:
				throw new IllegalArgumentException("niezdefiniowany case: s_optimisationMeasure=" + m_optimisationMeasure);
			}
        } //for parVal
        String paramName;
        ValuesForParameters paramValues = m_multiDimParams.getParamsForIndex(bestParamValue);
        //ustawianie optymalnego k
        paramName = m_multiDimParams.m_kValues.getParameterName();
        makePropertyModifiable(paramName);
        setProperty(paramName, Integer.toString(paramValues.kValue));
        if (paramValues.kValue == 0) {
        	Report.displaynl("Uwaga: optymalne k=0 (prawdopodobnie wszystkie warto�ci w tabelce s� zerowe");
//        	throw new IllegalStateException("co� nie tak: optymalne k=0"); //tak by�o wcze�niej, ale zasadniczo mo�e to wyst�pi�, je�li wszystkie warto�ci s� zerowe, tak wyst�pi�o np. dla zbioru balance-scale dla s=1.0
        }
        //ustawianie optymalnej pValue
        paramName = m_multiDimParams.m_pValues.getParameterName();
        makePropertyModifiable(paramName);
        setProperty(paramName, Double.toString(paramValues.pValue));
        //ustawianie optymalnej sMinValue; dodane 22.08.2016
        paramName = m_multiDimParams.m_sMinValues.getParameterName();
        makePropertyModifiable(paramName);
        setProperty(paramName, Double.toString(paramValues.sMinValue));
        //ustawianie optymalnej sMajValue; dodane 6.05.2017
        paramName = m_multiDimParams.m_sMajValues.getParameterName();
        makePropertyModifiable(paramName);
        if (m_optimisation4D) {
            setProperty(paramName, Double.toString(paramValues.sMajValue));
        } else {
        	setProperty(paramName, Double.toString(paramValues.sMinValue)); //ta sama warto�� sMaj co dla sMin
        }
        
    }
    
    
//wykomentowa�em 6.08.2016 - ju� tego nie u�ywam
//    private void setDecisionWeights() {
//    	m_arrDecisionWeights = new double[m_DecisionAttribute.noOfValues()];
////    	double minorityClassGLobalCode = m_TransformedTrainTable.getMinorityClassGlobalCode(); 
////    	int minorityClassLocalCode = m_DecisionAttribute.localValueCode(minorityClassGLobalCode);
//    	int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
////    	m_arrDecisionWeights[minorityClassLocalCode] = 1 - m_pValueThreshold; //dla klasy mniejszo�ciowej waga 1-p
////    	m_arrDecisionWeights[1-minorityClassLocalCode] = m_pValueThreshold; //dla klasy wi�kszo�ciowej waga p
//    }
    
    /**
     * Tymczasowo - ustawia zakresy warto�ci parametr�w k, p, s
     * dodane ggora 25.07.2016
     * @param _prop 
     * @throws PropertyConfigurationException 
     */
    private void setParameterValues(Properties _prop) throws PropertyConfigurationException {
//        ArrayList<Integer> kValues = SingleParameter.getIntArithmeticSequence(0, 1, 20);
        ArrayList<Integer> kValues = SingleParameter.getIntArithmeticSequenceFromTo(0, m_nMaxK, 1);

        SingleParameter<Integer> kParamValues = new SingleParameter<Integer>(K_PROPERTY_NAME, kValues);

//        ArrayList<Double> pValues = SingleParameter.getDoubleArithmeticSequence(0, 0.05, 10);
//        ArrayList<Double> pValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0, 0.2, 0.05);
//        ArrayList<Double> pValues = SingleParameter.getDoubleSingleValue(getDoubleProperty(P_VALUE_THRESHOLD_PROPERTY_NAME));
//        ArrayList<Double> pValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0.0, 1.0, 0.01);
        //zmiany ggora 2.11.2017
        //przenios�em 7.08.2020 z projektu ...\kod i eksperymenty\142.4.11.2017 (p=percMinority)        
//        double percMinority = m_TransformedTrainTable.getPercentOfMinority();
//        ArrayList<Double> pValues = SingleParameter.getDoubleSingleValue(percMinority); //je�li chc� na sta�e ustawi� parametr p na naturalnego kandydata (bez uczenia si� tego parametru)
        //standard:
        double pThresholdMin = getDoubleProperty(P_VALUE_THRESHOLD_MIN_PROPERTY_NAME);
        double pThresholdMax = getDoubleProperty(P_VALUE_THRESHOLD_MAX_PROPERTY_NAME);
        double pThresholdStep = getDoubleProperty(P_VALUE_THRESHOLD_STEP_PROPERTY_NAME);
        ArrayList<Double> pValues = SingleParameter.getDoubleArithmeticSequenceFromTo(pThresholdMin, pThresholdMax, pThresholdStep); //przyspieszenie - wystarczy ustawia� maxP=0.5 (bo wiemy, �e s� to zbiory niezbalansowane)
//        ArrayList<Double> pValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0.35, 0.41, 0.01);

        SingleParameter<Double> pParamValues = new SingleParameter<Double>(P_VALUE_THRESHOLD_PROPERTY_NAME, pValues);

//      ArrayList<Double> sMinValues = SingleParameter.getDoubleSingleValue(1.0);
//      ArrayList<Double> sMinValues = SingleParameter.getDoubleSingleValue(0.9);
//        ArrayList<Double> sMinValues = SingleParameter.getDoubleSingleValue(0.0);
//      ArrayList<Double> sMinValues = SingleParameter.getDoubleTwoValues(0.0, 1.0);
        double sMinorityMin = getDoubleProperty(S_MINORITY_MIN_VALUE_PROPERTY_NAME);
        double sMinorityMax = getDoubleProperty(S_MINORITY_MAX_VALUE_PROPERTY_NAME);
        double sMinorityStep = getDoubleProperty(S_MINORITY_STEP_VALUE_PROPERTY_NAME);
    	ArrayList<Double> sMinValuesWithMinusOne = SingleParameter.getDoubleSingleValue(-1.0);
        ArrayList<Double> sMinValues = SingleParameter.getDoubleArithmeticSequenceFromTo(sMinorityMin, sMinorityMax, sMinorityStep);
        sMinValuesWithMinusOne.addAll(sMinValues);
//        ArrayList<Double> sMinValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0.9, 1.0, 0.01);
//        ArrayList<Double> sMinValues = SingleParameter.getDoubleArithmeticSequenceFromTo(0.99, 1.0, 0.001);
//        SingleParameter<Double> sMinParamValues = new SingleParameter<Double>(S_MINORITY_VALUE_PROPERTY_NAME, sMinValues);

        SingleParameter<Double> sMinParamValues = new SingleParameter<Double>(S_MINORITY_VALUE_PROPERTY_NAME, sMinValuesWithMinusOne);

        if (!m_optimisation4D) { //dodane ggora 11.05.2017 (przypadek 3D)
        	ArrayList<Double> sMajValues = SingleParameter.getDoubleSingleValue(MultiDimensionalParameters.getArtifical_sMajValue());
            SingleParameter<Double> sMajParamValues = new SingleParameter<Double>(S_MAJORITY_VALUE_PROPERTY_NAME, sMajValues);
            m_multiDimParams = new MultiDimensionalParameters(kParamValues, pParamValues, sMinParamValues, sMajParamValues);
        } else { //przypadek dla 4D
//          ArrayList<Double> sMajValues = SingleParameter.getDoubleSingleValue(1.0);
        	double sMajorityMin = getDoubleProperty(S_MAJORITY_MIN_VALUE_PROPERTY_NAME);
        	double sMajorityMax = getDoubleProperty(S_MAJORITY_MAX_VALUE_PROPERTY_NAME);
        	double sMajorityStep = getDoubleProperty(S_MAJORITY_STEP_VALUE_PROPERTY_NAME);
          	ArrayList<Double> sMajValuesWithMinusOne = SingleParameter.getDoubleSingleValue(-1.0);
            ArrayList<Double> sMajValues = SingleParameter.getDoubleArithmeticSequenceFromTo(sMajorityMin, sMajorityMax, sMajorityStep);
          sMajValuesWithMinusOne.addAll(sMajValues);
//          SingleParameter<Double> sMajParamValues = new SingleParameter<Double>(S_MAJORITY_VALUE_PROPERTY_NAME, sMajValues);
            SingleParameter<Double> sMajParamValues = new SingleParameter<Double>(S_MAJORITY_VALUE_PROPERTY_NAME, sMajValuesWithMinusOne);
            m_multiDimParams = new MultiDimensionalParameters(kParamValues, pParamValues, sMinParamValues, sMajParamValues);
        }
//      m_multiDimParams = new MultiDimensionalParameters(kParamValues, pParamValues, sMinParamValues);
        Report.debugnl("multiDimParams:\n" + m_multiDimParams);
//        Report.displaynl("multiDimParams details:\n" + m_multiDimParams.toStringDetails());
    }
    
//    private void saveMainRIONIDAparams() throws PropertyConfigurationException, FileNotFoundException {
        private void saveMainRIONIDAparams() throws PropertyConfigurationException {
    	String strRIONIDAparams =	"m_optimisation4D=" + m_optimisation4D + System.lineSeparator() + 
    			"s_optimisationMeasure=" + m_optimisationMeasure + System.lineSeparator() +
    			"s_optimisationMethod=" + s_optimisationMethod + System.lineSeparator() +
				"multiDimParams=" + m_multiDimParams + System.lineSeparator() +
				"votingType=" + Voting.valueOf(getProperty(VOTING_PROPERTY_NAME)) + System.lineSeparator() +
				"s_howToTreatInconsistencyForLearning=" + s_howToTreatInconsistencyForLearning +  System.lineSeparator() +
				"s_howToTreatInconsistencyForClassification=" + s_howToTreatInconsistencyForClassification +  System.lineSeparator() +
				"s_zeroDistFastLearning=" + s_zeroDistFastLearning +  System.lineSeparator() +
				"s_zeroDistFastClassification=" + s_zeroDistFastClassification +  System.lineSeparator();				
    	try {
    		Report.printToFile("RIONIDA_log_parameters.txt", strRIONIDAparams);
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		System.exit(1);
    	}
    }

    
    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	writeAbstractParameterisedClassifier(out);
    	out.writeObject(m_OriginalData);
    	out.writeObject(m_Transformer);
    	out.writeObject(m_Metric);
    	out.writeInt(m_nMaxK);
    	out.writeObject(m_DecisionAttribute);
    	out.writeInt(m_nDefaultDec);
    	out.writeInt(m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode));
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readAbstractParameterisedClassifier(in);
    	m_OriginalData = (ArrayList<DoubleData>)in.readObject();
    	ArrayList<DoubleData> transformedObjects = m_OriginalData;
    	m_Transformer = (AttributeTransformer)in.readObject();
        if (m_Transformer!=null)
        {
            transformedObjects = new ArrayList<DoubleData>(m_OriginalData.size());
            for (DoubleData dObj : m_OriginalData)
                transformedObjects.add(m_Transformer.transformToNew(dObj));
        }
        m_TransformedTrainTable = new ArrayListDoubleDataTable(transformedObjects);
    	m_Metric = (Metric)in.readObject();
    	try
    	{
            if(getBoolProperty(INDEXING_PROPERTY_NAME))
            {
            	IndexingTreeNode indexingTree = new TreeIndexer(null).indexing(m_TransformedTrainTable.getDataObjects(), m_Metric, new EmptyProgress());
            	m_VicinityProvider = new IndexingTreeVicinityProvider(null, m_Metric, indexingTree);                
            } else
                m_VicinityProvider = new ArrayVicinityProvider(m_Metric, m_TransformedTrainTable.getDataObjects());
    	}
    	catch (InterruptedException e)
    	{
    		throw new NotSerializableException(e.getMessage());
    	}
    	catch (PropertyConfigurationException e)
    	{
    		throw new NotSerializableException(e.getMessage());
    	}
    	m_bSelfLearning = false;
    	m_nMaxK = in.readInt();
        if (m_Metric instanceof AbstractWeightedMetric)
        	m_NeighboursFilter = new CubeBasedNeighboursFilter((AbstractWeightedMetric)m_Metric, m_Transformer!=null);
    	m_DecisionAttribute = (NominalAttribute)in.readObject();
    	m_nDefaultDec = in.readInt();
    	m_dMinorityDecGlobalCode = m_DecisionAttribute.globalValueCode(in.readInt());
    }

    /**
     * Sets the self-learning switch, required to set,
     * if k optimization is done outside the classifier.
     * 
     * @param selfLearning	The value to be set.
     */
    public void setSelfLearning(boolean selfLearning)
    {
        m_bSelfLearning = selfLearning;
    }
    
    /**
     * Returns a decision distribution vector
     * for a single test object.
     * The weight of each decision value is given
     * at the position of the vector
     * identifed by the local code of this decision value.
     *
     * @param dObj  Test object.
     * @return      Assigned decision distribution.
     */
    public double[] classifyWithDistributedDecision(DoubleData dObj) throws PropertyConfigurationException
    {
        if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = m_VicinityProvider.getVicinity(dObj, getIntProperty(K_PROPERTY_NAME));
    	boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null)
        	m_NeighboursFilter.markConsistency(dObj, neighbours);
        double[] decDistr = new double[m_DecisionAttribute.noOfValues()];
        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
        for (int n = 1; n < neighbours.length; n++)
        {
        	int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
        	if (!checkConsistency || neighbours[n].m_bConsistent)
//        		Report.displaynl("consistent neighbour=" +  neighbours[n].toString());
        		switch (votingType)
        		{
        		case Equal:
        			decDistr[curDec] += 1.0;
        			break;
        		case InverseDistance:
        			decDistr[curDec] += 1.0 / neighbours[n].dist();
        			break;
        		case InverseSquareDistance:
        			decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        			break;
        		}
        }
//z normowaniem: dodane przez ggora 26.04.2016
//        for (int curDec = 0; curDec < decDistr.length; curDec++)
//        	decDistr[curDec] = decDistr[curDec] / m_TransformedTrainTable.getDecisionDistribution()[curDec];
//z normowaniem w kwadracie: dodane przez ggora 17.05.2016
//        for (int curDec = 0; curDec < decDistr.length; curDec++)
//        	decDistr[curDec] = decDistr[curDec] / (m_TransformedTrainTable.getDecisionDistribution()[curDec] * m_TransformedTrainTable.getDecisionDistribution()[curDec]);
//koniec dodane
        //dodane ggora 27.07.2016 (przemno�enie przez wag� dla danej decyzji)
//        multiplyByDecisionWeights(decDistr);
        return decDistr;
    }

    /**
     * Returns a decision distribution vector
     * for a single test object.
     * The weight of each decision value is given
     * at the position of the vector
     * identifed by the local code of this decision value.
     *
     * @param dObj  Test object.
     * @return      Assigned decision distribution.
     */
    public double[] classifyWithDistributedDecisionOnLevels(DoubleData dObj) throws PropertyConfigurationException
    {
        if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = m_VicinityProvider.getVicinity(dObj, getIntProperty(K_PROPERTY_NAME));

    	//pojedyncza s warto�� dla minority - przetworzona na list� jednoelementow�
        double sMinorityValue = getDoubleProperty(S_MINORITY_VALUE_PROPERTY_NAME);
    	ArrayList<Double> sMinorityValueSingleArr = SingleParameter.getDoubleSingleValue(sMinorityValue);
//    	ArrayList<Double> sMinorityValueSingleArr = SingleParameter.getDoubleSingleValue(getDoubleProperty(S_MINORITY_VALUE_PROPERTY_NAME));
        SingleParameter<Double> sMinorityValues = new SingleParameter<Double>(S_MINORITY_VALUE_PROPERTY_NAME, sMinorityValueSingleArr);
    	//pojedyncza s warto�� dla majority - przetworzona na list� jednoelementow� 
        double sMajorityValue = getDoubleProperty(S_MAJORITY_VALUE_PROPERTY_NAME);
        ArrayList<Double> sMajorityValueSingleArr = SingleParameter.getDoubleSingleValue(sMajorityValue);
//        ArrayList<Double> sMajorityValueSingleArr = SingleParameter.getDoubleSingleValue(getDoubleProperty(S_MAJORITY_VALUE_PROPERTY_NAME));
        SingleParameter<Double> sMajorityValues = new SingleParameter<Double>(S_MAJORITY_VALUE_PROPERTY_NAME, sMajorityValueSingleArr);

        //ewentualne przyspieszenie klasyfikacji - nie jest to istotny kod dla dzia�ania (tylko dla przyspieszenia)
        if ((m_bSelfLearning == false) &&  //tylko na etapie klasyfikacji (chyba nie jest potrzebny ten warunek)
        	(s_zeroDistFastClassification == true) && //tylko je�li jawnie ustawi� wywo�ywanie tego przyspieszenia
        	(sMinorityValue >= 0.0) && (sMajorityValue >= 0.0)) //tylko dla warto�ci s nieujemnych (ujemna oznacza metod� kNN)
        {
        	int i = 1;
        	/*
        	 * rozk�ad decyzji dla obiekt�w najbli�szych r�wnoodleg�ych od testowego
        	 * - to jest jakby rozk�ad zast�pczy (na sytuacj� gdy normalny rozk�ad jest zerowy
        	 * , a tak jest mo�liwe w sprzecnej tablicy 
        	 */
            double[] decEqDistDistr = new double[m_DecisionAttribute.noOfValues()];
            Arrays.fill(decEqDistDistr, 0.0);
        	for (i = 1; (i < neighbours.length) && (neighbours[i].dist() == 0.0); i++) {
            	int curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
            	decEqDistDistr[curDec] += 1.0;
        	}
        	if (i > 1) { //wyst�pi�y jakie� obiekty o odleg�o�ci 0.0
//            	int minDec = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
//        		System.out.println("1 poziom decEqDistDistr: na etapie klasyfikacji wyst�pi�y punkty z dist=0 - bior� rozk�ad dla nich: decEqDistDistr[0]=" + decEqDistDistr[0] + ", decEqDistDistr[1]=" + decEqDistDistr[1] + ", minDec=" + minDec);
        		if ((decEqDistDistr[0] > 0) && (decEqDistDistr[1] > 0)) { //czyli badaj�c jakekolwiek kostki, nawet dla s=0.0 wyst�pi� sprzeczno�ci
        			return decEqDistDistr; //Spostrze�enie: dla kodu z s ujemnym nie mo�na tutaj wyj��
        		}
        	}
        } //if m_bSelfLearning
    	boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);

    	if (checkConsistency && m_NeighboursFilter!=null)
//        	m_NeighboursFilter.markConsistency(dObj, neighbours); //stare
        	m_NeighboursFilter.markConsistencyLevels(dObj, neighbours, m_dMinorityDecGlobalCode , sMinorityValues, m_optimisation4D ? sMajorityValues : sMinorityValues); //zmienione ggora 17.08.2016; zmiana 22.08.2016
        double[] decDistr = new double[m_DecisionAttribute.noOfValues()];
//        Arrays.fill(decDistr, 0.0);
        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
        for (int n = 1; n < neighbours.length; n++)
        {
        	int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
//        	if (!checkConsistency || neighbours[n].m_bConsistent)
        	if (!checkConsistency || neighbours[n].m_bConsistentOnLevel[0]) //Uwaga: lista jednoelementowa
        		switch (votingType)
        		{
        		case Equal:
        			decDistr[curDec] += 1.0;
        			break;
        		case InverseDistance:
        			decDistr[curDec] += 1.0 / neighbours[n].dist();
        			break;
        		case InverseSquareDistance:
        			decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        			break;
        		}
        }
        if (m_bSelfLearning == false) { //tylko na etapie klasyfikacji
        	/*
        	 * rozk�ad decyzji dla obiekt�w najbli�szych r�wnoodleg�ych od testowego
        	 * - to jest jakby rozk�ad zast�pczy (na sytuacj� gdy normalny rozk�ad jest zerowy
        	 * , a tak jest mo�liwe w sprzecnej tablicy 
        	 */
            double[] decEqDistDistr = new double[m_DecisionAttribute.noOfValues()];
            if ((decDistr[0] == 0.0) && (decDistr[1] == 0.0)) { //je�li normalny rozk�ad by�by zerowy
//            	System.out.println("rozk�ad zerowy [0.0, 0.0]"); //wypisywa�em tylko na potrzeby debug. Dla wersji RIONIA_newStd wwypisywa� to tylko dla 2 przypadk�w dla dataset postoperative (w 7 powt�wrzeniu, dla 8 foldu)
            	//dla przypadku HowToTreatInconsistency.ZeroDistCount nie powinien tu wej��, bo zerowe odleg�o�ci zosta�y za�atwione na pocz�tku (jednocze�nie przyspiesznie)
            	if (s_howToTreatInconsistencyForClassification == HowToTreatInconsistency.ZeroAndEqDistCount) { //nie tylko dla zerowych odleg�o�ci, ale i dla r�wnych
                	double firstDist = neighbours[1].dist();
                    Arrays.fill(decEqDistDistr, 0.0);
                	for (int i = 1; (i < neighbours.length) && (neighbours[i].dist() == firstDist); i++) { //wszystkie punkty o tej samej odleg�o�ci co pierwszy (zasadniczo zak�adam, �e je�li rozk�ad standardowy by� zerowy, to tu powinny by� punkty o r�nych decyzjach. Ewentualnie udowodni�.
                    	int curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
//                		System.out.println("i=" + i + ", dist=" + firstDist + ", curDec=" + curDec);
//                		System.out.println("na etapie klasyfikacji bior�c standardowo wychodzi rozk�ad zerowy, a pukt�w z dist=0 nie ma - bior� punkty z distEq licz�c od pierwszego najbli�szego" + "; minDec=[uzupe�ni�], curDec=" + curDec);
                    	decEqDistDistr[curDec] += 1.0;
                	}
//                	int minDec = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
//            		System.out.println("2 poziom decEqDistDistr: na etapie klasyfikacji bior�c standardowo wychodzi rozk�ad zerowy, a pukt�w z dist=0 nie ma - bior� punkty z distEq licz�c od pierwszego najbli�szego: decEqDistDistr[0]=" + decEqDistDistr[0] + ", decEqDistDistr[1]=" + decEqDistDistr[1] + ", minDec=" + minDec);
                	return decEqDistDistr;
            	} //if howToTreatInconsitencyForClassification
            }
        } //if not selfLearning
        return decDistr;
    }
    
    /**
     * Assigns a decision to a single test object.
     *
     * @param dObj  Test object.
     * @return      Assigned decision.
     */
    public double classify(DoubleData dObj) throws PropertyConfigurationException
    {
    	if ( getIntProperty(K_PROPERTY_NAME) == 0 ) return m_dMinorityDecGlobalCode; //zasadniczo nie powinien si� wyucza� warto��i k=0, ale mo�e tak by� np. dla balance-scale dla s=1
//    	double[] decDistr = classifyWithDistributedDecision(dObj); //stare
//    	double[] decDistrOld = classifyWithDistributedDecision(dObj); //dla cel�w debug
    	double[] decDistr = classifyWithDistributedDecisionOnLevels(dObj); //zmiana ggora 17.08.2016
//stare:
//        int bestDec = 0;
//        for (int dec = 1; dec < decDistr.length; dec++)
//            if (decDistr[dec] > decDistr[bestDec]) bestDec = dec;
//        return m_DecisionAttribute.globalValueCode(bestDec);
//zmiana ggora 5.08.2016:
    	int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
		int majorityClassLocalCode = 1 - minorityClassLocalCode; //wprowadzam t� zmienn� dla czytelno�ci kodu
    	int retDec;
		if ((decDistr[0]==0.0) && (decDistr[1]==0.0)) {
//			Report.debugnl("Ostrze�enie:oba wsp�czynniki s� r�wne 0");
//			System.out.println("Ostrze�enie:oba wsp�czynniki s� r�wne 0"); //wypisywa�em tylko na potrzeby debug. Dla wersji RIONIA_newStd wwypisywa� to tylko dla 2 przypadk�w dla dataset postoperative (w 7 powt�wrzeniu, dla 8 foldu)
			retDec = minorityClassLocalCode; //w sytuacji kiedy nie wiadomo jak� decyzj� podj�� - decyzja mniejszo�ciowa (Uwaga: nie jest to ju� sytuacja sprzeczno�ci spowodowanej przez obiekty treningowe identyczne jak testowy, bo takie s� ju� rozstrzygane. Ale jest to sytuacja sprzeczno�ci obiekt�w oddalonych troch� od testowego - tak my�l�) 
		} else {
			double pValue = decDistr[minorityClassLocalCode] / (decDistr[0] + decDistr[1]);
			if (decDistr[minorityClassLocalCode] == Double.POSITIVE_INFINITY) pValue=1.0; //mo�e si� zdarzy�, je�li testuj� na pr�bce treningowej i ustawione jest Voting=
			double pValueThreshold = getDoubleProperty(P_VALUE_THRESHOLD_PROPERTY_NAME);
			if (pValue > pValueThreshold) retDec = minorityClassLocalCode;
			else if (pValue < pValueThreshold) retDec = majorityClassLocalCode;
			else if (pValue == pValueThreshold) retDec = minorityClassLocalCode;
			else throw new RuntimeException("nie powinien tu wej��: pValue=" + pValue + ", pValueThreshold=" + pValueThreshold + ", decDistr[0]=" + decDistr[0] + ", decDistr[1]=" + decDistr[1] +  ", decDistr[minorityClassLocalCode]=" + decDistr[minorityClassLocalCode]);
		}
		return m_DecisionAttribute.globalValueCode(retDec);
    }

    /**
     * classifyWithParameter - oryginalna metoda, z ewentualnymi ma�ymi zmianami
     * Classifies a test object on the basis of nearest neighbours.
     * Obecnie nieu�ywana
     *
     * @param dObj         Test object.
     * @return             Array of assigned decisions, indices correspond to parameter values.
     */
    public double[] classifyWithParameter(DoubleData dObj) throws PropertyConfigurationException
    {
    	double pValueThreshold = getDoubleProperty(P_VALUE_THRESHOLD_PROPERTY_NAME);
        if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = null;
        if (m_bSelfLearning)
        {
            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObj, m_nMaxK+1);
            neighbours = new Neighbour[neighboursOneMore.length-1];
            int i = 1;
            for (; i < neighbours.length && !dObj.equals(neighboursOneMore[i].neighbour()); i++)
            	neighbours[i] = neighboursOneMore[i];
           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
        }
        else neighbours = m_VicinityProvider.getVicinity(dObj, m_nMaxK);
        boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null)
        	m_NeighboursFilter.markConsistency(dObj, neighbours);
        double[] decisions = new double[m_nMaxK+1];
        double[] decDistr = new double[m_DecisionAttribute.noOfValues()];
        int bestDec = m_nDefaultDec;
        decisions[0] = m_DecisionAttribute.globalValueCode(bestDec);
        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
        int firstNotSet = 1;
        int[] totalDecDistr = m_TransformedTrainTable.getDecisionDistribution(); //dodane ggora: 26.04.2016
        for (int n = 1; n < neighbours.length; n++)
        {
        	int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
        	if (!checkConsistency || neighbours[n].m_bConsistent)
        		switch (votingType)
        		{
        		case Equal:
        			decDistr[curDec] += 1.0;
        			break;
        		case InverseDistance:
        			decDistr[curDec] += 1.0 / neighbours[n].dist();
        			break;
        		case InverseSquareDistance:
        			decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        			break;
        		}
			int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
			int majorityClassLocalCode = 1 - minorityClassLocalCode; //wprowadzam t� zmienn� dla czytelno�ci kodu
			double pValue = decDistr[minorityClassLocalCode] / (decDistr[0] + decDistr[1]);
			if (n == neighbours.length - 1 || neighbours[n].dist() != neighbours[n+1].dist())
        	{
        			for (int d = 0; d < decDistr.length; d++)
//        	        	if (decDistr[d] > decDistr[bestDec]) bestDec = d; //zamiast (tak by�o oryginalnie przed 26.04.2016)
//        				if (decDistr[d] * m_arrDecisionWeights[d] > decDistr[bestDec] * m_arrDecisionWeights[bestDec]) bestDec = d; //dodane ggora 27.07.2016
        				if (pValue > pValueThreshold) bestDec = minorityClassLocalCode;
        				else if (pValue < pValueThreshold) bestDec = majorityClassLocalCode;
        				else if (pValue == pValueThreshold) bestDec = minorityClassLocalCode;
        			
        			for (int i = firstNotSet; i <= n && i < decisions.length; i++)
        				decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
        		firstNotSet = n + 1;
        	}
        }
//        for (int i = firstNotSet; i < decisions.length; i++)
//			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
        return decisions;
    }
    
    /**
     * classifyWith2DParameter
     * Classifies a test object on the basis of nearest neighbours.
     * Poprzednia wersja - dla 2 wymiar�w, przetestowana i dzia�a�a (dlatego na razie j� zachowuj� dla por�wnania).
     *
     * @param dObj         Test object.
     * @return             Array of assigned decisions, indices correspond to parameter values.
     */
    public double[] classifyWith2DParameter(DoubleData dObj) throws PropertyConfigurationException
    {
        if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = null;
        if (m_bSelfLearning)
        {
            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObj, m_nMaxK+1);
            neighbours = new Neighbour[neighboursOneMore.length-1];
            int i = 1;
            for (; i < neighbours.length && !dObj.equals(neighboursOneMore[i].neighbour()); i++)
            	neighbours[i] = neighboursOneMore[i];
           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
        }
        else neighbours = m_VicinityProvider.getVicinity(dObj, m_nMaxK);
        boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null)
        	m_NeighboursFilter.markConsistency(dObj, neighbours);
//        double[] decisions = new double[m_nMaxK+1]; //stare
        double[] decisions = new double[m_multiDimParams.getVolume()];
        double[] decDistr = new double[m_DecisionAttribute.noOfValues()];
		SingleParameter<Double> possible_pValues = m_multiDimParams.m_pValues;
		int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
		int majorityClassLocalCode = 1 - minorityClassLocalCode; //wprowadzam t� zmienn� dla czytelno�ci kodu
        int bestDec = m_nDefaultDec;
		ValuesForParameters params = new ValuesForParameters(); //jeden raz tworz� klas� do przekazywania parametr�w w metodzie getIndexForParams()
//        decisions[0] = m_DecisionAttribute.globalValueCode(bestDec); //stare
//zmiana ggora 6.08.2016 dodana p�tla for
		for (int j = 0; j < possible_pValues.size(); j++) {
			double current_pValue = possible_pValues.getParamValueByIndex(j);
			params.kValue = 0;
			params.pValue = current_pValue;
			params.sMinValue = 1.0; //potem zmieni�
			int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
//			decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(bestDec);
//			decisions[indexInMultidimensionalTable] = minorityClassLocalCode;
			decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(majorityClassLocalCode);
		}

        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
        int firstNotSet = 1;
        int[] totalDecDistr = m_TransformedTrainTable.getDecisionDistribution(); //dodane ggora: 26.04.2016
        for (int n = 1; n < neighbours.length; n++)
        {
        	int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
        	if (!checkConsistency || neighbours[n].m_bConsistent)
        		switch (votingType)
        		{
        		case Equal:
        			decDistr[curDec] += 1.0;
        			break;
        		case InverseDistance:
        			decDistr[curDec] += 1.0 / neighbours[n].dist();
        			break;
        		case InverseSquareDistance:
        			decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        			break;
        		}
        	if (n == neighbours.length - 1 || neighbours[n].dist() != neighbours[n+1].dist())
        	{
				double pValue = decDistr[minorityClassLocalCode] / (decDistr[0] + decDistr[1]);
				//dla warto�ci mniejszych lub r�wnych pValue b�dzie decyzja MINORITY
				//dla warto�ci wi�kszych ni� pValue b�dzie decyzja MAJORITY
				
//        		for (int i = firstNotSet; i <= n && i < decisions.length; i++) { //stare
				for (int i = firstNotSet; i <= n && i < m_multiDimParams.m_kValues.size(); i++) {
//        			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec); //stare
    				for (int j = 0; j < possible_pValues.size(); j++) {
    					double current_pValue = possible_pValues.getParamValueByIndex(j);
    					int currentDecision;
    					if (current_pValue < pValue) currentDecision = minorityClassLocalCode; //gdyby pr�g p zosta� ustawiony jako current_pValue, to przy takim pValue ten obiekt zosta�by sklasyfikowany jako minorityClass
    					else if (current_pValue > pValue) currentDecision = majorityClassLocalCode;
    					else currentDecision = minorityClassLocalCode; //current_pValue == pValue (warto�� brzegowa rozstrzygana na korzy�� klasy mniejszo�ciowej)
    					params.kValue = i;
    					params.pValue = current_pValue;
    					params.sMinValue = 1.0; //potem zmieni�
    					int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
//    					Report.displaynl(params);
    					decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(currentDecision);
//    					Report.displaynl(indexInMultidimensionalTable);
    				}
        			
        		}
        		firstNotSet = n + 1;
        	}
        }
// Komentarz Arka: To chyba normalnie niepotrzebne, mo�e jakie� szczeg�lne przypadki np. maxK wi�ksze ni� rozmiar tabelki treningowej
// zatem kasuj� ggora 4.08.2016 (obs�uguj� ten przypadek wcze�niej przez wyrzucenie wyj�tku)
//        for (int i = firstNotSet; i < decisions.length; i++)
//			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
        return decisions;
    }

    
    private static double DECISION_VALUE_NOT_SET = -1.0;
    
//wersja alg.RIONIDA standardowa (dla optymalizacji metod� leave-one-out)
//    /**
//     * Uwaga: przywracam t� metod� z backupu nr 80 (5.05.2017)
//     * classifyWith3DParameter - najnowsza wersja (je�li chc� j� por�wna� z 2 wymiarow�, to t� zakomentowa�, a w poprzedniej w nazwie 2D zamieni� na 3D)
//     * Classifies a test object on the basis of nearest neighbours.
//     *
//     * @param dObj         Test object.
//     * @return             Array of assigned decisions, indices correspond to parameter values.
//     */
//    public double[] classifyWith3DParameter(DoubleData dObj) throws PropertyConfigurationException
//    {
////		ggUtils.Timer.startContinue(0);
////      double[] decisions = new double[m_nMaxK+1]; //stare
//    	double[] decisions = new double[m_multiDimParams.getVolume()];
//    	Arrays.fill(decisions, DECISION_VALUE_NOT_SET);
//		ValuesForParameters params = new ValuesForParameters(); //jeden raz tworz� klas� do przekazywania parametr�w w metodzie getIndexForParams()
//		int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
//		int majorityClassLocalCode = 1 - minorityClassLocalCode; //wprowadzam t� zmienn� dla czytelno�ci kodu
//
//		if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
//        Neighbour[] neighbours = null;
////		ggUtils.Timer.startContinue(4);
//        if (m_bSelfLearning)
//        {
//            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObj, m_nMaxK+1);
//            neighbours = new Neighbour[neighboursOneMore.length-1];
//            int i = 1;
//            for (; i < neighbours.length && !dObj.equals(neighboursOneMore[i].neighbour()); i++)
//            	neighbours[i] = neighboursOneMore[i];
//           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
//        }
//        else neighbours = m_VicinityProvider.getVicinity(dObj, m_nMaxK);
////		ggUtils.Timer.stop(4);
//        boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
//        if (checkConsistency && m_NeighboursFilter!=null) {
//        	//pojedyncza s warto�� dla minority - przetworzona na list� jednoelementow� 
//        	
//            SingleParameter<Double> sMinorityValues = m_multiDimParams.m_sMinValues;
//        	//pojedyncza s warto�� dla majority - przetworzona na list� jednoelementow� 
////            ArrayList<Double> sMajorityValue = SingleParameter.getDoubleSingleValue(getDoubleProperty(S_MAJORITY_VALUE_PROPERTY_NAME));
////            SingleParameter<Double> sMajorityValues = new SingleParameter<Double>(S_MAJORITY_VALUE_PROPERTY_NAME, sMajorityValue);
//            //inna mo�liwo��: te same warto�ci co dla sMinorityValues
////            SingleParameter<Double> sMajorityValues = sMinorityValues;
//            SingleParameter<Double> sMajorityValues = null; //na wszelki wypadek jednak tak ustawiam (w metodzie markConsistencyLevels i tak korzystam
//            
//            if (s_zeroDistFastLearning) {
//            	double[] decZeroDistDistr = new double[m_DecisionAttribute.noOfValues()];
//            	Arrays.fill(decZeroDistDistr, 0.0);
//            	for (int i = 1; (i < neighbours.length) && (neighbours[i].dist() == 0.0); i++) { //zbieram punkty odleg�e o 0.0 od punktu dObj (tak mo�e by� w tabelce sprzecznej)
//            		int curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
//            		decZeroDistDistr[curDec] += 1.0; //rozk�ad tylko na podstawie obiekt�w, kt�re s� r�wnoodleg�e od badanego obiektu jako potencjalnego testowego w metodzie leave-one-out
//            	}
//            	if ((decZeroDistDistr[0] > 0.0) && (decZeroDistDistr[1] > 0.0) ) { //je�li prawdziwe, to znaczy �e istniej� obiekty odleg�e o 0.0, kt�re maj� r�ne decyzje (sprzeczne)
//            		//				ggUtils.Timer.startContinue(1);
//            		double pValue = decZeroDistDistr[minorityClassLocalCode] / (decZeroDistDistr[0] + decZeroDistDistr[1]); //tu ju� nie mo�e by� rozk�ad zerowy (wynika z warunku if)
//            		//inicjuj� tablic� wielowymiarow� dla tej sytuacji sprzecznej - walidacyjn� decyzj� podejmiemy na podstawie tych najbli�szych obiekt�w odleg�ych o 0.0
//            		SingleParameter<Double> possible_pValues = m_multiDimParams.m_pValues;
//            		SingleParameter<Double> possible_sMinValues = m_multiDimParams.m_sMinValues;
//            		//				ggUtils.Timer.startContinue(6);
//            		for (int indexForSvalue = 0; indexForSvalue < possible_sMinValues.size(); indexForSvalue++) {
//            			double current_sValue = possible_sMinValues.getParamValueByIndex(indexForSvalue);
//            			for (int k = 0; k < m_multiDimParams.m_kValues.size(); k++) {
//            				for (int indexForPvalue = 0; indexForPvalue < possible_pValues.size(); indexForPvalue++) {
//            					//							ggUtils.Timer.startContinue(8);
//            					double current_pValue = possible_pValues.getParamValueByIndex(indexForPvalue);
//            					//							ggUtils.Timer.stop(8);
//            					params.kValue = k; //dla k>1 i tak normalnie bra�bym wszystkie obiekty r�wnoodleg�e do pierwszego - a to w�a�nie zrobi�em wyliczaj�c na pocz�tku rozk�ad decZeroDistDistr
//            					params.pValue = current_pValue;
//            					params.sMinValue = current_sValue;
//            					params.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue();
//            					//							ggUtils.Timer.startContinue(7);
//            					int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
//            					//							ggUtils.Timer.stop(7); //d�u�ej ni� Timer(8) ok.6x [kilka mno�e�]
//            					//mo�na przyspieszy�: za pierwszym razem zapu�ci� podobn� p�tl�, aby wype�ni� tablic� kolejnych ineks�w; potem u�ywa� tej tablicy indeks�w 
//            					int currentDecision;
//            					//w celu przyspieszenia: mo�e zamie� kolejno�� poni�szych if (chyba cz�ciej jest majority wybierane)
//            					if (current_pValue < pValue) currentDecision = minorityClassLocalCode; //gdyby pr�g p zosta� ustawiony jako current_pValue, to przy takim pValue ten obiekt zosta�by sklasyfikowany jako minorityClass
//            					else if (current_pValue > pValue) currentDecision = majorityClassLocalCode;
//            					else if (current_pValue == pValue) currentDecision = minorityClassLocalCode; //standardowo: current_pValue == pValue, to minDec (warto�� brzegowa rozstrzygana na korzy�� klasy mniejszo�ciowej)
//            					else { //nie mo�e tu wej�� (oznacza�oby to pValue==NaN [not a value] [z zerowego potencjalnie rozk�adu 0.0/0.0=NaN]), bo to wynika z warunku if decZeroDistDistr
//            						throw new IllegalStateException("nie powinien tu wej�� - wynika st�d, �e rozk�ad jest zerowy");
//            					}
//            					if (k==0) decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(majorityClassLocalCode); //dla k=0 inicjacja jak ni�ej
//            					else decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(currentDecision);
//            				}
//            			}
//            		}
//            		//				ggUtils.Timer.stop(6);
//            		//				ggUtils.Timer.startContinue(3);
//            		checkIfAllDecisionsSet(decisions);
//            		//				ggUtils.Timer.stop(3);
//            		//				ggUtils.Timer.stop(0);
//            		//				ggUtils.Timer.stop(1);
//            		return decisions; //przez to przyspieszam dzia�anie uczenia dla przypadku r�wnoodleg�ych punkt�w (dalej i tak by�by rozk�ad zerowy i musia�bym wyliczy� dalej w�a�nie ten)
//            	} //if decZeroDistDistr
//            } //if zeroDistFastLearning
//
////			if (neighbours.length > 150) {
////				ggUtils.Timer.startContinue(2);
////				System.out.println("neighbours.length=" + neighbours.length);
////			}
//			
////        	m_NeighboursFilter.markConsistency(dObj, neighbours);
////			ggUtils.Timer.startContinue(5);
//        	m_NeighboursFilter.markConsistencyLevels(dObj, neighbours, m_dMinorityDecGlobalCode ,sMinorityValues, s_sMajEqual_sMin ? sMinorityValues : sMajorityValues); //zmienione ggora 22.08.2016
////			ggUtils.Timer.stop(5);
//        }
////        double[] decDistr = new double[m_DecisionAttribute.noOfValues()]; //ggora 2.09.2016: przeniesione ni�ej, aby zawsze inicjowa� zerami
//		SingleParameter<Double> possible_pValues = m_multiDimParams.m_pValues;
//		SingleParameter<Double> possible_sMinValues = m_multiDimParams.m_sMinValues;
////		int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
////		int majorityClassLocalCode = 1 - minorityClassLocalCode; //wprowadzam t� zmienn� dla czytelno�ci kodu
//        int bestDec = m_nDefaultDec;
////		ValuesForParameters params = new ValuesForParameters(); //jeden raz tworz� klas� do przekazywania parametr�w w metodzie getIndexForParams()
////        decisions[0] = m_DecisionAttribute.globalValueCode(bestDec); //stare
////zmiana ggora 6.08.2016 dodana p�tla for; zmiana 22.08.2016
//		//inicjuj� tablic� wielowymiarow� dla sytuacji brzegowej k=0
//		for (int indexForSvalue = 0; indexForSvalue < possible_sMinValues.size(); indexForSvalue++) {
//			double current_sValue = possible_sMinValues.getParamValueByIndex(indexForSvalue);
//			for (int indexForPvalue = 0; indexForPvalue < possible_pValues.size(); indexForPvalue++) {
//				double current_pValue = possible_pValues.getParamValueByIndex(indexForPvalue);
//				params.kValue = 0;
//				params.pValue = current_pValue;
//				params.sMinValue = current_sValue;
//				params.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue();
//				int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
//				decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(majorityClassLocalCode);
//			}
//		}
//
//        Voting votingType;
//        try
//        {
//        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
//        }
//        catch (IllegalArgumentException e)
//        {
//        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
//        }
//		for (int indexForSvalue = 0; indexForSvalue < possible_sMinValues.size(); indexForSvalue++) {
//	        double[] decDistr = new double[m_DecisionAttribute.noOfValues()]; //ggora 2.09.2016: przeniesione tutaj , aby zawsze inicjowa� zerami
//	        int firstNotSet = 1;
//			double current_sValue = possible_sMinValues.getParamValueByIndex(indexForSvalue);
//
//			for (int n = 1; n < neighbours.length; n++) //kolejne warto�ci k (ale ju� r�ne od zera)
//			{
//				int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
////				if (!checkConsistency || neighbours[n].m_bConsistent)
//				if (!checkConsistency || neighbours[n].m_bConsistentOnLevel[indexForSvalue]) //zmiana ggora 22.08.2016
//					switch (votingType)
//					{
//					case Equal:
//						decDistr[curDec] += 1.0;
//						break;
//					case InverseDistance:
//						decDistr[curDec] += 1.0 / neighbours[n].dist();
//						break;
//					case InverseSquareDistance:
//						decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
//						break;
//					}
//				if (n == neighbours.length - 1 || neighbours[n].dist() != neighbours[n+1].dist())
//				{
//					//Uwaga: do 18.05.2017 tu by� b��d, bo pValue by�a wyliczana przed poni�szym if
//					if ((decDistr[0] == 0.0) && (decDistr[1] == 0.0)) { //na etapie uczenia jest rozk�ad zerowy
//						//przypadek HowToTreatInconsistency.ZeroDistCount ju� w�a�ciwie zosta� za�atwiony na pocz�tku (jednocze�nie przyspieszenie)
//						double firstDist = neighbours[1].dist();
//						switch(s_howToTreatInconsistencyForLearning) {
//						case ZeroDistCount:
//							if (firstDist > 0.0) break; //dla tego przypadku firstDist musi by� r�wny 0.0 (bierzemy tylko odleg�e o 0.0)
//						case ZeroAndEqDistCount:
//							for (int i = 1; (i < neighbours.length) && (neighbours[i].dist() == firstDist); i++) {
//								curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
//								decDistr[curDec] += 1.0; //rozk�ad b�dzie tylko na podstawie obiekt�w, kt�re s� r�wnoodleg�e od badanego obiektu jako testowego w metodzie leave-one-out
//							}
//							break;
//						default:
//							throw new IllegalArgumentException("niezdefiniowany case: s_howToTreatInconsistencyForLearning=" + s_howToTreatInconsistencyForLearning);
//						}
//					} //if decDistr zerowe
//					//powy�ej wstawili�my ewentualnie zast�pczy rozk�ad je�li standardowy by� zerowy
//					double pValue = decDistr[minorityClassLocalCode] / (decDistr[0] + decDistr[1]); //gdyby by� rozk�ad zerowy to wtedy: pValue=0.0/0.0=NaN [not a number] (wtedy ka�de por�wanie z NaN daje false)
//					//dla warto�ci mniejszych lub r�wnych pValue b�dzie decyzja MINORITY
//					//dla warto�ci wi�kszych ni� pValue b�dzie decyzja MAJORITY
//
//					for (int i = firstNotSet; i <= n && i < m_multiDimParams.m_kValues.size(); i++) {
//						for (int j = 0; j < possible_pValues.size(); j++) {
//							double current_pValue = possible_pValues.getParamValueByIndex(j);
//							int currentDecision;
//							if (current_pValue < pValue) currentDecision = minorityClassLocalCode; //gdyby pr�g p zosta� ustawiony jako current_pValue, to przy takim pValue ten obiekt zosta�by sklasyfikowany jako minorityClass
//							else if (current_pValue > pValue) currentDecision = majorityClassLocalCode;
//							else if (current_pValue == pValue) currentDecision = minorityClassLocalCode; //standardowo: current_pValue == pValue (warto�� brzegowa rozstrzygana na korzy�� klasy mniejszo�ciowej)
//							else { //mo�e tu wej�� je�li pValue==NaN [not a value] [z zerowego rozk�adu 0.0/0.0=NaN]
////								System.out.println("Uwaga: pValue=" + pValue);
//								currentDecision = minorityClassLocalCode; //ale i tak dla wszystkich current_pValue powinien tu wej�� czyli b�d� jednakowe warto�ci (zawsze b��dne lub zawsze prawdziwe dla wszystkich parametr�w p) (czyli przy wyborze max nie b�dzie to gra�o roli)
//							}
////chwilowo zmieniam na potrzeby eksperymentu 28.04.2017
////							else currentDecision = majorityClassLocalCode; //current_pValue == pValue (warto�� brzegowa rozstrzygana na korzy�� klasy wi�kszo�ciowej)
//							params.kValue = i;
//							params.pValue = current_pValue;
//							params.sMinValue = current_sValue;
//							params.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue() ; //sztuczna warto��, ale i tak b�d� bra� tak jak sMin (potrzebna aby dobrze wyliczy� indeks w tablicy)
//							int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
////							System.out.println("currentDecision=" + currentDecision);
//							decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(currentDecision);
//						}
//
//					}
//					firstNotSet = n + 1;
////					if (tmpPrint) System.exit(1);
//				}
//			} //for n
//		} // for indexForSvalue
//// Komentarz Arka: To chyba normalnie niepotrzebne, mo�e jakie� szczeg�lne przypadki np. maxK wi�ksze ni� rozmiar tabelki treningowej
//// zatem kasuj� ggora 4.08.2016 (obs�uguj� ten przypadek wcze�niej przez wyrzucenie wyj�tku)
////        for (int i = firstNotSet; i < decisions.length; i++)
////			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);
//
////sprawdzam czy na pewno wszystkie elementy tablicy decisions zosta�y uzupe�nione (na potrzeby debug)
//		checkIfAllDecisionsSet(decisions);
////		String strParams = "";
////		for (int i = 0; i < decisions.length; i++) {
////			if (decisions[i] == DECISION_VALUE_NOT_SET) {
////				params = m_multiDimParams.getParamsForIndex(i);
////				strParams += "i=" + i + "; params: k=" + params.kValue + ", p=" + params.pValue + ", s=" + params.sMinValue + Report.lineSeparator;
////			}
////		}
////		if (strParams.length()>0) throw new RuntimeException("decisions[i] nie zosta�o ustawione dla:" + strParams);
//
////		ggUtils.Timer.stop(0);
////		if (neighbours.length > 150) ggUtils.Timer.stop(2);
//		return decisions;
//    }

//wesja dla alg.RIONIDA optymalizowanej metod� stratified cross-validation
    /**
     * classifyWith3DParameter - najnowsza wersja (je�li chc� j� por�wna� z 2 wymiarow�, to t� zakomentowa�, a w poprzedniej w nazwie 2D zamieni� na 3D)
     * Classifies a test object on the basis of nearest neighbours.
     *
     * @param dObj         Test object.
     * @return             Array of assigned decisions, indices correspond to parameter values.
     */
    public double[] classifyWith3DParameter(DoubleData dObj) throws PropertyConfigurationException
    {
		if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = null;
        if (m_bSelfLearning)
        {
            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObj, m_nMaxK+1);
            neighbours = new Neighbour[neighboursOneMore.length-1];
            int i = 1;
            for (; i < neighbours.length && !dObj.equals(neighboursOneMore[i].neighbour()); i++)
            	neighbours[i] = neighboursOneMore[i];
           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
        }
        else neighbours = m_VicinityProvider.getVicinity(dObj, m_nMaxK);
        return classifyWith3DParameter(dObj, neighbours);
    }
    /**
     * classifyWith3DParameter - najnowsza wersja (je�li chc� j� por�wna� z 2 wymiarow�, to t� zakomentowa�, a w poprzedniej w nazwie 2D zamieni� na 3D)
     * Classifies a test object on the basis of nearest neighbours.
     *
     * @param dObj         Test object.
     * @return             Array of assigned decisions, indices correspond to parameter values.
     */
    public double[] classifyWith3DParameter(DoubleData dObj, Neighbour[] neighbours) throws PropertyConfigurationException
    {
//		if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
//        Neighbour[] neighbours = null;
//        if (m_bSelfLearning)
//        {
//            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObj, m_nMaxK+1);
//            neighbours = new Neighbour[neighboursOneMore.length-1];
//            int i = 1;
//            for (; i < neighbours.length && !dObj.equals(neighboursOneMore[i].neighbour()); i++)
//            	neighbours[i] = neighboursOneMore[i];
//           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
//        }
//        else neighbours = m_VicinityProvider.getVicinity(dObj, m_nMaxK);

    	double[] decisions = new double[m_multiDimParams.getVolume()];
    	Arrays.fill(decisions, DECISION_VALUE_NOT_SET);
		ValuesForParameters params = new ValuesForParameters(); //jeden raz tworz� klas� do przekazywania parametr�w w metodzie getIndexForParams()
		int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
		int majorityClassLocalCode = 1 - minorityClassLocalCode; //wprowadzam t� zmienn� dla czytelno�ci kodu
    	
        boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null) {
        	//pojedyncza s warto�� dla minority - przetworzona na list� jednoelementow� 
        	
            SingleParameter<Double> sMinorityValues = m_multiDimParams.m_sMinValues;
            SingleParameter<Double> sMajorityValues = null; //na wszelki wypadek jednak tak ustawiam (w metodzie markConsistencyLevels i tak korzystam
            
            if (s_zeroDistFastLearning) {
            	double[] decZeroDistDistr = new double[m_DecisionAttribute.noOfValues()];
            	Arrays.fill(decZeroDistDistr, 0.0);
            	for (int i = 1; (i < neighbours.length) && (neighbours[i].dist() == 0.0); i++) { //zbieram punkty odleg�e o 0.0 od punktu dObj (tak mo�e by� w tabelce sprzecznej)
            		int curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
            		decZeroDistDistr[curDec] += 1.0; //rozk�ad tylko na podstawie obiekt�w, kt�re s� r�wnoodleg�e od badanego obiektu jako potencjalnego testowego w metodzie leave-one-out
            	}
            	if ((decZeroDistDistr[0] > 0.0) && (decZeroDistDistr[1] > 0.0) ) { //je�li prawdziwe, to znaczy �e istniej� obiekty odleg�e o 0.0, kt�re maj� r�ne decyzje (sprzeczne)
            		double pValue = decZeroDistDistr[minorityClassLocalCode] / (decZeroDistDistr[0] + decZeroDistDistr[1]); //tu ju� nie mo�e by� rozk�ad zerowy (wynika z warunku if)
            		//inicjuj� tablic� wielowymiarow� dla tej sytuacji sprzecznej - walidacyjn� decyzj� podejmiemy na podstawie tych najbli�szych obiekt�w odleg�ych o 0.0
            		SingleParameter<Double> possible_pValues = m_multiDimParams.m_pValues;
            		SingleParameter<Double> possible_sMinValues = m_multiDimParams.m_sMinValues;
            		for (int indexForSvalue = 0; indexForSvalue < possible_sMinValues.size(); indexForSvalue++) {
            			double current_sValue = possible_sMinValues.getParamValueByIndex(indexForSvalue);
            			for (int k = 0; k < m_multiDimParams.m_kValues.size(); k++) {
            				for (int indexForPvalue = 0; indexForPvalue < possible_pValues.size(); indexForPvalue++) {
            					double current_pValue = possible_pValues.getParamValueByIndex(indexForPvalue);
            					params.kValue = k; //dla k>1 i tak normalnie bra�bym wszystkie obiekty r�wnoodleg�e do pierwszego - a to w�a�nie zrobi�em wyliczaj�c na pocz�tku rozk�ad decZeroDistDistr
            					params.pValue = current_pValue;
            					params.sMinValue = current_sValue;
            					params.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue();
            					int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
            					//mo�na przyspieszy�: za pierwszym razem zapu�ci� podobn� p�tl�, aby wype�ni� tablic� kolejnych ineks�w; potem u�ywa� tej tablicy indeks�w 
            					int currentDecision;
            					//w celu przyspieszenia: mo�e zamie� kolejno�� poni�szych if (chyba cz�ciej jest majority wybierane)
            					if (current_pValue < pValue) currentDecision = minorityClassLocalCode; //gdyby pr�g p zosta� ustawiony jako current_pValue, to przy takim pValue ten obiekt zosta�by sklasyfikowany jako minorityClass
            					else if (current_pValue > pValue) currentDecision = majorityClassLocalCode;
            					else if (current_pValue == pValue) currentDecision = minorityClassLocalCode; //standardowo: current_pValue == pValue, to minDec (warto�� brzegowa rozstrzygana na korzy�� klasy mniejszo�ciowej)
            					else { //nie mo�e tu wej�� (oznacza�oby to pValue==NaN [not a value] [z zerowego potencjalnie rozk�adu 0.0/0.0=NaN]), bo to wynika z warunku if decZeroDistDistr
            						throw new IllegalStateException("nie powinien tu wej�� - wynika st�d, �e rozk�ad jest zerowy");
            					}
            					if (k==0) decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(majorityClassLocalCode); //dla k=0 inicjacja jak ni�ej
            					else decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(currentDecision);
            				}
            			}
            		}
            		checkIfAllDecisionsSet(decisions);
            		return decisions; //przez to przyspieszam dzia�anie uczenia dla przypadku r�wnoodleg�ych punkt�w (dalej i tak by�by rozk�ad zerowy i musia�bym wyliczy� dalej w�a�nie ten)
            	} //if decZeroDistDistr
            } //if zeroDistFastLearning

			
        	m_NeighboursFilter.markConsistencyLevels(dObj, neighbours, m_dMinorityDecGlobalCode ,sMinorityValues, m_optimisation4D ? sMajorityValues : sMinorityValues); //zmienione ggora 22.08.2016
        }
		SingleParameter<Double> possible_pValues = m_multiDimParams.m_pValues;
		SingleParameter<Double> possible_sMinValues = m_multiDimParams.m_sMinValues;
        int bestDec = m_nDefaultDec;
		//inicjuj� tablic� wielowymiarow� dla sytuacji brzegowej k=0
		for (int indexForSvalue = 0; indexForSvalue < possible_sMinValues.size(); indexForSvalue++) {
			double current_sValue = possible_sMinValues.getParamValueByIndex(indexForSvalue);
			for (int indexForPvalue = 0; indexForPvalue < possible_pValues.size(); indexForPvalue++) {
				double current_pValue = possible_pValues.getParamValueByIndex(indexForPvalue);
				params.kValue = 0;
				params.pValue = current_pValue;
				params.sMinValue = current_sValue;
				params.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue();
				int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
				decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(majorityClassLocalCode);
			}
		}

        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
		for (int indexForSvalue = 0; indexForSvalue < possible_sMinValues.size(); indexForSvalue++) {
	        double[] decDistr = new double[m_DecisionAttribute.noOfValues()]; //ggora 2.09.2016: przeniesione tutaj , aby zawsze inicjowa� zerami
	        int firstNotSet = 1;
			double current_sValue = possible_sMinValues.getParamValueByIndex(indexForSvalue);

			for (int n = 1; n < neighbours.length; n++) //kolejne warto�ci k (ale ju� r�ne od zera)
			{
				int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
				if (!checkConsistency || neighbours[n].m_bConsistentOnLevel[indexForSvalue]) //zmiana ggora 22.08.2016
					switch (votingType)
					{
					case Equal:
						decDistr[curDec] += 1.0;
						break;
					case InverseDistance:
						decDistr[curDec] += 1.0 / neighbours[n].dist();
						break;
					case InverseSquareDistance:
						decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
						break;
					}
				if (n == neighbours.length - 1 || neighbours[n].dist() != neighbours[n+1].dist())
				{
					if ((decDistr[0] == 0.0) && (decDistr[1] == 0.0)) { //na etapie uczenia jest rozk�ad zerowy
						//przypadek HowToTreatInconsistency.ZeroDistCount ju� w�a�ciwie zosta� za�atwiony na pocz�tku (jednocze�nie przyspieszenie)
						double firstDist = neighbours[1].dist();
						switch(s_howToTreatInconsistencyForLearning) {
						case ZeroDistCount:
							if (firstDist > 0.0) break; //dla tego przypadku firstDist musi by� r�wny 0.0 (bierzemy tylko odleg�e o 0.0)
						case ZeroAndEqDistCount:
							for (int i = 1; (i < neighbours.length) && (neighbours[i].dist() == firstDist); i++) {
								curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
								decDistr[curDec] += 1.0; //rozk�ad b�dzie tylko na podstawie obiekt�w, kt�re s� r�wnoodleg�e od badanego obiektu jako testowego w metodzie leave-one-out
							}
							break;
						default:
							throw new IllegalArgumentException("niezdefiniowany case: s_howToTreatInconsistencyForLearning=" + s_howToTreatInconsistencyForLearning);
						}
					} //if decDistr zerowe
					//powy�ej wstawili�my ewentualnie zast�pczy rozk�ad je�li standardowy by� zerowy
					double pValue = decDistr[minorityClassLocalCode] / (decDistr[0] + decDistr[1]); //gdyby by� rozk�ad zerowy to wtedy: pValue=0.0/0.0=NaN [not a number] (wtedy ka�de por�wanie z NaN daje false)
					//dla warto�ci mniejszych lub r�wnych pValue b�dzie decyzja MINORITY
					//dla warto�ci wi�kszych ni� pValue b�dzie decyzja MAJORITY

					for (int i = firstNotSet; i <= n && i < m_multiDimParams.m_kValues.size(); i++) {
						for (int j = 0; j < possible_pValues.size(); j++) {
							double current_pValue = possible_pValues.getParamValueByIndex(j);
							int currentDecision;
							if (current_pValue < pValue) currentDecision = minorityClassLocalCode; //gdyby pr�g p zosta� ustawiony jako current_pValue, to przy takim pValue ten obiekt zosta�by sklasyfikowany jako minorityClass
							else if (current_pValue > pValue) currentDecision = majorityClassLocalCode;
							else if (current_pValue == pValue) currentDecision = minorityClassLocalCode; //standardowo: current_pValue == pValue (warto�� brzegowa rozstrzygana na korzy�� klasy mniejszo�ciowej)
							else { //mo�e tu wej�� je�li pValue==NaN [not a value] [z zerowego rozk�adu 0.0/0.0=NaN]
//								System.out.println("Uwaga: pValue=" + pValue);
								currentDecision = minorityClassLocalCode; //ale i tak dla wszystkich current_pValue powinien tu wej�� czyli b�d� jednakowe warto�ci (zawsze b��dne lub zawsze prawdziwe dla wszystkich parametr�w p) (czyli przy wyborze max nie b�dzie to gra�o roli)
							}
							params.kValue = i;
							params.pValue = current_pValue;
							params.sMinValue = current_sValue;
							params.sMajValue = MultiDimensionalParameters.getArtifical_sMajValue() ; //sztuczna warto��, ale i tak b�d� bra� tak jak sMin (potrzebna aby dobrze wyliczy� indeks w tablicy)
							int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
							decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(currentDecision);
						}

					}
					firstNotSet = n + 1;
				}
			} //for n
		} // for indexForSvalue

//sprawdzam czy na pewno wszystkie elementy tablicy decisions zosta�y uzupe�nione (na potrzeby debug)
		checkIfAllDecisionsSet(decisions);

		return decisions;
    }
    
    //@todo poprawi� dla 4D!!!
    private void checkIfAllDecisionsSet(double[] _decisions) {
    	int noErrors = 0;
    	String strParams = "";
		ValuesForParameters params = new ValuesForParameters(); //klasa do przekazywania parametr�w w metodzie getIndexForParams()
		for (int i = 0; i < _decisions.length; i++) {
			if (_decisions[i] == DECISION_VALUE_NOT_SET) {
				noErrors++;
				if (noErrors <= 10) { //wypisz tylko pierwsze 10 komunikat�w (w przypadku b��du tablica mo�e by� bardzo du�a)
					params = m_multiDimParams.getParamsForIndex(i);
					//teoretycznie mog� by� te� zdefiniowane case'y dla 1D i 2D dla trzeba poprawi� kod (patrz uwaga do zmiennej m_optimisation4D
					if(m_optimisation4D)
						strParams += "i=" + i + "; params: k=" + params.kValue + ", p=" + params.pValue + ", sMin=" + params.sMinValue + ", sMaj=" + params.sMajValue + Report.lineSeparator;
					else
						strParams += "i=" + i + "; params: k=" + params.kValue + ", p=" + params.pValue + ", s=" + params.sMinValue + Report.lineSeparator;
				} //if noErrors
			}
		}
		if (noErrors > 10) strParams+= "..." + Report.lineSeparator + "[razem " + noErrors + " takich sytuacji" + Report.lineSeparator;
		if (strParams.length()>0) throw new RuntimeException("decisions[i] nie zosta�o ustawione dla:" + strParams);
    }
    
    
    /**
     * classifyWith3DParameter - najnowsza wersja (je�li chc� j� por�wna� z 2 wymiarow�, to t� zakomentowa�, a w poprzedniej w nazwie 2D zamieni� na 3D)
     * Classifies a test object on the basis of nearest neighbours.
     *
     * @param dObj         Test object.
     * @return             Array of assigned decisions, indices correspond to parameter values.
     */
    public double[] classifyWith4DParameter(DoubleData dObj) throws PropertyConfigurationException
    {
        if (m_Transformer!=null) dObj = m_Transformer.transformToNew(dObj);
        Neighbour[] neighbours = null;
        if (m_bSelfLearning)
        {
            Neighbour[] neighboursOneMore = m_VicinityProvider.getVicinity(dObj, m_nMaxK+1);
            neighbours = new Neighbour[neighboursOneMore.length-1];
            int i = 1;
            for (; i < neighbours.length && !dObj.equals(neighboursOneMore[i].neighbour()); i++)
            	neighbours[i] = neighboursOneMore[i];
           	for (; i < neighbours.length; i++) neighbours[i] = neighboursOneMore[i+1];
        }
        else neighbours = m_VicinityProvider.getVicinity(dObj, m_nMaxK);

        double[] decisions = new double[m_multiDimParams.getVolume()];
        Arrays.fill(decisions, DECISION_VALUE_NOT_SET);
		ValuesForParameters params = new ValuesForParameters(); //jeden raz tworz� klas� do przekazywania parametr�w w metodzie getIndexForParams()
		int minorityClassLocalCode = m_DecisionAttribute.localValueCode(m_dMinorityDecGlobalCode);
		int majorityClassLocalCode = 1 - minorityClassLocalCode; //wprowadzam t� zmienn� dla czytelno�ci kodu
        
        boolean checkConsistency = getBoolProperty(FILTER_NEIGHBOURS_PROPERTY_NAME);
        if (checkConsistency && m_NeighboursFilter!=null) {
        	//pojedyncza s warto�� dla minority - przetworzona na list� jednoelementow� 
        	
            SingleParameter<Double> sMinorityValues = m_multiDimParams.m_sMinValues; 
            //Uwag: tu by� b��d; zmiana 26.05.2017 ggora - wykomentowuj� i wstawiam poprawn� lini�
            //inna mo�liwo��: te same warto�ci co dla sMinorityValues
//            SingleParameter<Double> sMajorityValues = sMinorityValues;
            SingleParameter<Double> sMajorityValues = m_multiDimParams.m_sMajValues;

            if (s_zeroDistFastLearning) {
            	double[] decZeroDistDistr = new double[m_DecisionAttribute.noOfValues()];
            	Arrays.fill(decZeroDistDistr, 0.0);
            	for (int i = 1; (i < neighbours.length) && (neighbours[i].dist() == 0.0); i++) { //zbieram punkty odleg�e o 0.0 od punktu dObj (tak mo�e by� w tabelce sprzecznej)
            		int curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
            		decZeroDistDistr[curDec] += 1.0; //rozk�ad tylko na podstawie obiekt�w, kt�re s� r�wnoodleg�e od badanego obiektu jako potencjalnego testowego w metodzie leave-one-out
            	}
            	if ((decZeroDistDistr[0] > 0.0) && (decZeroDistDistr[1] > 0.0) ) { //je�li prawdziwe, to znaczy �e istniej� obiekty odleg�e o 0.0, kt�re maj� r�ne decyzje (sprzeczne)
            		double pValue = decZeroDistDistr[minorityClassLocalCode] / (decZeroDistDistr[0] + decZeroDistDistr[1]); //tu ju� nie mo�e by� rozk�ad zerowy (wynika z warunku if)
            		//inicjuj� tablic� wielowymiarow� dla tej sytuacji sprzecznej - walidacyjn� decyzj� podejmiemy na podstawie tych najbli�szych obiekt�w odleg�ych o 0.0
            		SingleParameter<Double> possible_pValues = m_multiDimParams.m_pValues;
            		SingleParameter<Double> possible_sMinValues = m_multiDimParams.m_sMinValues;
            		SingleParameter<Double> possible_sMajValues = m_multiDimParams.m_sMajValues;
            		for (int indexForSmajValue = 0; indexForSmajValue < possible_sMajValues.size(); indexForSmajValue++) {
            			double current_sMajValue = possible_sMajValues.getParamValueByIndex(indexForSmajValue);
            			for (int indexForSminValue = 0; indexForSminValue < possible_sMinValues.size(); indexForSminValue++) {
            				double current_sMinValue = possible_sMinValues.getParamValueByIndex(indexForSminValue);
            				for (int k = 0; k < m_multiDimParams.m_kValues.size(); k++) {
            					for (int indexForPvalue = 0; indexForPvalue < possible_pValues.size(); indexForPvalue++) {
            						double current_pValue = possible_pValues.getParamValueByIndex(indexForPvalue);
            						params.kValue = k; //dla k>1 i tak normalnie bra�bym wszystkie obiekty r�wnoodleg�e do pierwszego - a to w�a�nie zrobi�em wyliczaj�c na pocz�tku rozk�ad decZeroDistDistr
            						params.pValue = current_pValue;
            						params.sMinValue = current_sMinValue;
//!!!          						params.sMinValue = current_sMajValue; //Uwaga: tu by� taki b��d; poprawi�em 1.06.2017 jak ni�ej; Uwaga: do 29.05.2017 by�o dobrze, w szczeg�lno�ci w kodzie D:\wszystko z 3penD\Gorz�dziej2009-\studDok\programistyczne\kod i eksperymenty\130.29.05.2017 10x10 RIONIDA_4DnewStd
            						params.sMajValue = current_sMajValue;
            						int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params); //r�nica w stosunku do 3D
            						//mo�na przyspieszy�: za pierwszym razem zapu�ci� podobn� p�tl�, aby wype�ni� tablic� kolejnych ineks�w; potem u�ywa� tej tablicy indeks�w 
            						int currentDecision;
            						//w celu przyspieszenia: mo�e zamie� kolejno�� poni�szych if (chyba cz�ciej jest majority wybierane)
            						if (current_pValue < pValue) currentDecision = minorityClassLocalCode; //gdyby pr�g p zosta� ustawiony jako current_pValue, to przy takim pValue ten obiekt zosta�by sklasyfikowany jako minorityClass
            						else if (current_pValue > pValue) currentDecision = majorityClassLocalCode;
            						else if (current_pValue == pValue) currentDecision = minorityClassLocalCode; //standardowo: current_pValue == pValue, to minDec (warto�� brzegowa rozstrzygana na korzy�� klasy mniejszo�ciowej)
            						else { //nie mo�e tu wej�� (oznacza�oby to pValue==NaN [not a value] [z zerowego potencjalnie rozk�adu 0.0/0.0=NaN]), bo to wynika z warunku if decZeroDistDistr
            							throw new IllegalStateException("nie powinien tu wej�� - wynika st�d, �e rozk�ad jest zerowy");
            						}
            						if (k==0) decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(majorityClassLocalCode); //dla k=0 inicjacja jak ni�ej
            						else decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(currentDecision);
            					}
            				}
            			} //for indexForSminValue
            		} //for indexForSmajValue
            		checkIfAllDecisionsSet(decisions);
            		return decisions; //przez to przyspieszam dzia�anie uczenia dla przypadku r�wnoodleg�ych punkt�w (dalej i tak by�by rozk�ad zerowy i musia�bym wyliczy� dalej w�a�nie ten)
            	} //if decZeroDistDistr
            } //if zeroDistFastLearning
            
            
        	m_NeighboursFilter.markConsistencyLevels(dObj, neighbours, m_dMinorityDecGlobalCode ,sMinorityValues, m_optimisation4D ? sMajorityValues : sMinorityValues); //zmienione ggora 22.08.2016
        }
//        double[] decDistr = new double[m_DecisionAttribute.noOfValues()]; //ggora 2.09.2016: przeniesione ni�ej, aby zawsze inicjowa� zerami
		SingleParameter<Double> possible_pValues = m_multiDimParams.m_pValues;
		SingleParameter<Double> possible_sMinValues = m_multiDimParams.m_sMinValues;
		//zmiana ggora 6.05.2017
		SingleParameter<Double> possible_sMajValues = m_multiDimParams.m_sMajValues;

        int bestDec = m_nDefaultDec;
		//inicjuj� tablic� wielowymiarow� dla sytuacji brzegowej k=0
		for (int indexForSmajValue = 0; indexForSmajValue < possible_sMajValues.size(); indexForSmajValue++) {
			double current_sMajValue = possible_sMajValues.getParamValueByIndex(indexForSmajValue);
			for (int indexForSminValue = 0; indexForSminValue < possible_sMinValues.size(); indexForSminValue++) {
				double current_sMinValue = possible_sMinValues.getParamValueByIndex(indexForSminValue);
				for (int indexForPvalue = 0; indexForPvalue < possible_pValues.size(); indexForPvalue++) {
					double current_pValue = possible_pValues.getParamValueByIndex(indexForPvalue);
					params.kValue = 0;
					params.pValue = current_pValue;
					params.sMinValue = current_sMinValue;
					//zmiana ggora 6.05.2017
					params.sMajValue = current_sMajValue;
					int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
					decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(majorityClassLocalCode);
				}
			} //for indexForSminValue
		} //for indexForSmajValue

        Voting votingType;
        try
        {
        	votingType = Voting.valueOf(getProperty(VOTING_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown voting method: "+getProperty(VOTING_PROPERTY_NAME));
        }
//ggora 6.05.2017 dodaj�
        for (int indexForSmajValue = 0; indexForSmajValue < possible_sMajValues.size(); indexForSmajValue++) {
        	double current_sMajValue = possible_sMajValues.getParamValueByIndex(indexForSmajValue);
        	for (int indexForSminValue = 0; indexForSminValue < possible_sMinValues.size(); indexForSminValue++) {
        		double[] decDistr = new double[m_DecisionAttribute.noOfValues()]; //ggora 2.09.2016: przeniesione tutaj , aby zawsze inicjowa� zerami
        		int firstNotSet = 1;
        		double current_sMinValue = possible_sMinValues.getParamValueByIndex(indexForSminValue);

        		for (int n = 1; n < neighbours.length; n++) //kolejne warto�ci k (ale ju� r�ne od zera)
        		{
        			int curDec = m_DecisionAttribute.localValueCode(neighbours[n].neighbour().getDecision());
//!!!      			if (!checkConsistency || neighbours[n].m_bConsistentOnLevel[indexForSminValue]) //zmiana ggora 22.08.2016 (tak by�o do 6.05.2017); Uwaga: 26.05.2017 zauwa�y�em b��d - nie by�a wykomentowana ta linia
        			//isConsistentOnLevel(): w zale�no�ci od tego czy obiekt neighbours[n] jest z klasy mniejszo�ciowej czy wi�kszo�ciowej to u�yj odpowiedniej tablicy sValue i odpowiadaj�cego im indeksu (indexForSminValue lub indexForSmajValue)
        			//isConsistentOnLevel(): Uwaga: tylko jedna z tych warto�ci indexForSminValue, indexForSmajValue jest wykorzystywana (jest tak zrobione dla czytelno�ci i jednolito�ci kodu
        			if (!checkConsistency || neighbours[n].isConsistentOnLevel(m_dMinorityDecGlobalCode, indexForSminValue, indexForSmajValue)) //zmiana ggora 6.05.2017
        				switch (votingType)
        				{
        				case Equal:
        					decDistr[curDec] += 1.0;
        					break;
        				case InverseDistance:
        					decDistr[curDec] += 1.0 / neighbours[n].dist();
        					break;
        				case InverseSquareDistance:
        					decDistr[curDec] += 1.0 / (neighbours[n].dist()*neighbours[n].dist());
        					break;
        				}
        			if (n == neighbours.length - 1 || neighbours[n].dist() != neighbours[n+1].dist())
        			{
    					if ((decDistr[0] == 0.0) && (decDistr[1] == 0.0)) { //na etapie uczenia jest rozk�ad zerowy
    						//przypadek HowToTreatInconsistency.ZeroDistCount ju� w�a�ciwie zosta� za�atwiony na pocz�tku (jednocze�nie przyspieszenie)
    						double firstDist = neighbours[1].dist();
    						switch(s_howToTreatInconsistencyForLearning) {
    						case ZeroDistCount:
    							if (firstDist > 0.0) break; //dla tego przypadku firstDist musi by� r�wny 0.0 (bierzemy tylko odleg�e o 0.0)
    							//a je�li firstDist == 0.0, to wykona poni�sze instrukcje
    						case ZeroAndEqDistCount:
    							for (int i = 1; (i < neighbours.length) && (neighbours[i].dist() == firstDist); i++) {
    								curDec = m_DecisionAttribute.localValueCode(neighbours[i].neighbour().getDecision());
    								decDistr[curDec] += 1.0; //rozk�ad b�dzie tylko na podstawie obiekt�w, kt�re s� r�wnoodleg�e od badanego obiektu jako testowego w metodzie leave-one-out
    							}
    							break;
    						default:
    							throw new IllegalArgumentException("niezdefiniowany case: s_howToTreatInconsistencyForLearning=" + s_howToTreatInconsistencyForLearning);
    						}
    					} //if decDistr zerowe
    					//powy�ej wstawili�my ewentualnie zast�pczy rozk�ad je�li standardowy by� zerowy
        				
        				double pValue = decDistr[minorityClassLocalCode] / (decDistr[0] + decDistr[1]);
        				//dla warto�ci mniejszych lub r�wnych pValue b�dzie decyzja MINORITY
        				//dla warto�ci wi�kszych ni� pValue b�dzie decyzja MAJORITY

        				for (int i = firstNotSet; i <= n && i < m_multiDimParams.m_kValues.size(); i++) {
        					for (int j = 0; j < possible_pValues.size(); j++) {
        						double current_pValue = possible_pValues.getParamValueByIndex(j);
        						int currentDecision;
        						if (current_pValue < pValue) currentDecision = minorityClassLocalCode; //gdyby pr�g p zosta� ustawiony jako current_pValue, to przy takim pValue ten obiekt zosta�by sklasyfikowany jako minorityClass
        						else if (current_pValue > pValue) currentDecision = majorityClassLocalCode;
        						else if (current_pValue == pValue) currentDecision = minorityClassLocalCode; //standardowo: current_pValue == pValue (warto�� brzegowa rozstrzygana na korzy�� klasy mniejszo�ciowej)
        						else { //mo�e tu wej�� je�li pValue==NaN [not a value] [z zerowego rozk�adu 0.0/0.0=NaN]
//    								System.out.println("Uwaga: pValue=" + pValue);
    								currentDecision = minorityClassLocalCode; //ale i tak dla wszystkich current_pValue powinien tu wej�� czyli b�d� jednakowe warto�ci (zawsze b��dne lub zawsze prawdziwe dla wszystkich parametr�w p) (czyli przy wyborze max nie b�dzie to gra�o roli)
        						}
        						//chwilowo zmieniam na potrzeby eksperymentu 28.04.2017
        						//							else currentDecision = majorityClassLocalCode; //current_pValue == pValue (warto�� brzegowa rozstrzygana na korzy�� klasy wi�kszo�ciowej)
        						params.kValue = i;
        						params.pValue = current_pValue;
        						params.sMinValue = current_sMinValue;
        						params.sMajValue = current_sMajValue;
        						int indexInMultidimensionalTable = m_multiDimParams.getIndexForParams(params);
        						decisions[indexInMultidimensionalTable] = m_DecisionAttribute.globalValueCode(currentDecision);
        					}

        				}
        				firstNotSet = n + 1;
        			}
        		} //for n
        	} // for indexForSMinvalue
        } //for indexForSMajvalue

// Komentarz Arka: To chyba normalnie niepotrzebne, mo�e jakie� szczeg�lne przypadki np. maxK wi�ksze ni� rozmiar tabelki treningowej
// zatem kasuj� ggora 4.08.2016 (obs�uguj� ten przypadek wcze�niej przez wyrzucenie wyj�tku)
//        for (int i = firstNotSet; i < decisions.length; i++)
//			decisions[i] = m_DecisionAttribute.globalValueCode(bestDec);

//sprawdzam czy na pewno wszystkie elementy tablicy decisions zosta�y uzupe�nione (na potrzeby debug)
		checkIfAllDecisionsSet(decisions);
//        String strParams = "";
//		for (int i = 0; i < decisions.length; i++) {
//			if (decisions[i] == DECISION_VALUE_NOT_SET) {
//				params = m_multiDimParams.getParamsForIndex(i);
//				strParams += "i=" + i + "; params: k=" + params.kValue + ", p=" + params.pValue + ", sMin=" + params.sMinValue + ", sMaj=" + params.sMajValue + Report.lineSeparator;
//			}
//		}
//		if (strParams.length()>0) throw new RuntimeException("decisions[i] nie zosta�o ustawione dla:" + strParams);
		return decisions;
    }
    
    /**
     * classifyWithMultiParameter - wprowadzam t� metod�, �eby w klasie wywo�uj�cej by�a sta�a nazwa metody (tej). Tutaj natomiast mo�na ustawi� jak� metod�
     * optymalizacji chc� zastosowa�: 1-wymiarow�, 2-wymiarow�, 3-wymiarow�, czy 4-wymiarow� (zaczynam j� pisa� 5.05.2017
     * Classifies a test object on the basis of nearest neighbours.
     *
     * @param dObj         Test object.
     * @return             Array of assigned decisions, indices correspond to parameter values.
     */
    public double[] classifyWithMultiParameter(DoubleData dObj) throws PropertyConfigurationException
    {
//    	return classifyWithParameter(dObj);
//    	return classifyWith2DParameter(dObj);
    	//teoretycznie mog� by� te� zdefiniowane case'y dla 1D i 2D dla trzeba poprawi� kod (patrz uwaga do zmiennej s_optimisationDimension
    	if(m_optimisation4D)
        	return classifyWith4DParameter(dObj); //standarodowo tej u�ywam
    	else
        	return classifyWith3DParameter(dObj);
//    	return classifyWith3DParameter(dObj);
//    	return classifyWith4DParameter(dObj); //standarodowo tej u�ywam
    }    
    
    /**
     * Generuje wyniki klasyfikacji dla wszystkich mo�liwych parametr�w.
     * Metoda na potrzeby wypisywania macierzy dla r�nych parametr�w na rzeczywistej tabelce testowej
     * @param testTable
     * @param prog
     * @return
     * @throws InterruptedException
     * @throws PropertyConfigurationException
     */
    public Parameterised3DTestResult classifyWith3DParameter(DoubleDataTable testTable, Progress prog) throws InterruptedException, PropertyConfigurationException
    {
		ValuesForParameters paramValues = null;
    	TestResult[] results = new TestResult[m_multiDimParams.getVolume()];
    	prog.set("classifyWith3DParameter", m_multiDimParams.getVolume());
    	for (int index = 0; index < m_multiDimParams.getVolume(); index++) {
    		paramValues = m_multiDimParams.getParamsForIndex(index);
//ustawianie parametr�w klasyfikatora
    		String paramName;
            //ustawianie k
            paramName = m_multiDimParams.m_kValues.getParameterName();
            makePropertyModifiable(paramName);
            setProperty(paramName, Integer.toString(paramValues.kValue));
            //ustawianie pValue
            paramName = m_multiDimParams.m_pValues.getParameterName();
            makePropertyModifiable(paramName);
            setProperty(paramName, Double.toString(paramValues.pValue));
            //ustawianie sValue
            paramName = m_multiDimParams.m_sMinValues.getParameterName();
            makePropertyModifiable(paramName);
            setProperty(paramName, Double.toString(paramValues.sMinValue));

            //sztucznie ustawiam te� na razie dla klasy wi�kszo�ciowej
            makePropertyModifiable(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_VALUE_PROPERTY_NAME);
            setProperty(rseslib.processing.classification.parameterised.knn.rionida.RIONIDA.S_MAJORITY_VALUE_PROPERTY_NAME, Double.toString(paramValues.sMinValue));
            
//klasyfikacja przy tych parametrach
        	TestResult result = new SingleClassifierTest().classify(this, testTable, new EmptyProgress());
        	results[index] = result;
        	prog.step();
        }
    	return new Parameterised3DTestResult(m_multiDimParams, m_DecisionAttribute, testTable.getDecisionDistribution(), results, null);
    }
    
    /**
     * Calculates statistics.
     */
    public void calculateStatistics()
    {
        try
        {
//            if (getBoolProperty(LEARN_OPTIMAL_K_PROPERTY_NAME)) //stare
        	if (getBoolProperty(LEARN_OPTIMAL_PARAMETERS_PROPERTY_NAME)) //zmienione ggora 4.08.2016
        	{
                addToStatistics("Optimal "+K_PROPERTY_NAME, getProperty(K_PROPERTY_NAME));
                addToStatistics("Optimal "+P_VALUE_THRESHOLD_PROPERTY_NAME, getProperty(P_VALUE_THRESHOLD_PROPERTY_NAME));
                addToStatistics("Optimal "+S_MINORITY_VALUE_PROPERTY_NAME, getProperty(S_MINORITY_VALUE_PROPERTY_NAME));
                addToStatistics("Optimal "+S_MAJORITY_VALUE_PROPERTY_NAME, getProperty(S_MAJORITY_VALUE_PROPERTY_NAME));
        	}
        }
        catch (PropertyConfigurationException e)
        {
        }
        //addToStatistics("Average number of distance calculations", Double.toString(m_VicinityProvider.getAverageNoOfDistCalculations()));
        //addToStatistics("Std. dev. of the number of distance calculations", Double.toString(m_VicinityProvider.getStdDevNoOfDistCalculations()));
    }

    /*
     * ggora 2.05.2017: doda�em
     */
    public String getKoptimal() throws PropertyConfigurationException {
    	return getProperty(K_PROPERTY_NAME);
    }
    /*
     * ggora 2.05.2017: doda�em
     */
    public String getPoptimal() throws PropertyConfigurationException {
    	return getProperty(P_VALUE_THRESHOLD_PROPERTY_NAME);
    }
    /*
     * ggora 2.05.2017: doda�em
     */
    public String getSminOptimal() throws PropertyConfigurationException {
    	return getProperty(S_MINORITY_VALUE_PROPERTY_NAME);
    }
    /*
     * ggora 2.05.2017: doda�em
     */
    public String getSmajOptimal() throws PropertyConfigurationException {
    	return getProperty(S_MAJORITY_VALUE_PROPERTY_NAME);
    }

    /*
     * ggora 31.05.2017: doda�em
     */
    //1.06.2017 komentuj� - raczej niepotrzebne
//    public void setLearnOptimalMultiDimensionalParams(String _propertyValue) throws PropertyConfigurationException {
//        makePropertyModifiable(LEARN_OPTIMAL_MULTIDIMENSIONAL_PARAMETERS_PROPERTY_NAME);
//    	setProperty(LEARN_OPTIMAL_MULTIDIMENSIONAL_PARAMETERS_PROPERTY_NAME, _propertyValue);
//    }
    
    //dodane ggora 13.04.2015
    public NominalAttribute getDecisionAttribute() {
    	return m_DecisionAttribute;
    }
    
    //dodane ggora 1.06.2016
    public DoubleDataTable getTransformedTrainTable() {
    	return m_TransformedTrainTable;
    }
    
    //dodane ggora 31.05.2017
    //pobiera Properties z nadklasy Configuration. Je�li wywo�uj� to po uczeniu, to tutaj s� wyuczone parametry. Tu jest te� parametr czy si� wyucza� itd. 
    public Properties getCurrentProperties() {
    	return getProperties();
    }
    
    /**
     * Resets statistics.
     */
    public void resetStatistics()
    {
    }
}

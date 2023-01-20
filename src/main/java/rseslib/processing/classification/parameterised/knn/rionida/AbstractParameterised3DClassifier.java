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

import rseslib.processing.classification.parameterised.knn.rionida.Parameterised3DTestResult.PrintMeasureInMatrix;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.Progress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

/**
 * Abstract class for parameterised classification methods
 * only with the method classifing a single test data object
 * left to be implemented by subclasses. Non-parameterised
 * method returns decision for a fixed parameter value.
 *
 * @author Grzegorz Gora
 */
public abstract class AbstractParameterised3DClassifier extends ConfigurationWithStatistics implements Parameterised3DClassifier
{
    /** Measures for optimisation. */
	public enum OptimisationMeasure { Fmeasure, Gmean, Accuracy };

    public static final String OPTIMISATION_4D_PROPERTY_NAME = "optimisation4D";
    public static final String OPTIMISATION_MEASURE_PROPERTY_NAME = "optimisationMeasure";
    
//	/** Parameter name. */
//    private String m_ParamName; //zakomentowa�em ggora 5.08.2016
    /** 
     * Number of dimension for optimisation.
     *  1D: not available but possible (k) [p=0.5, s=1.0] (ale trzeba by poprawi� kod, �eby przystosowa� do 4 wymiarowej tablicy, podobnie jak zosta�o zrobione z 3D)
     *  2D: not available but possible (k, p) [s=1.0] (ale trzeba by poprawi� kod, �eby przystosowa� do 4 wymiarowej tablicy, podobnie jak zosta�o zrobione z 3D)
     *  3D: (k, p, s) [s=sMaj=sMin]
     *  4D: (k, p, sMin, sMaj) 
     * Zmienna okre�la czy parametry sMaj i sMin s� zwi�zane (tak jest dla optymalizacji 3D) czy te� niezale�ne (dla optymalizacji 4D)
     * Na razie chc�c wywo�a� optymalizacj� 4D ten parametr trzeba ustawi� na true, a dla 3D na false (w optymalizacji 3D jest jeden parametr s wsp�lny dla klas maj i min)
     */
    boolean m_optimisation4D = false; //standard
//    boolean m_optimisation4D = true;
    
	//poni�sz� lini� doda�em 8.08 na podstawie sugestii Arka 
	OptimisationMeasure m_optimisationMeasure = OptimisationMeasure.Gmean; //10.04.2017: standardowo ustawiam na Gmean (kilka dni wcze�niej by�o: standardowo ustawiam na Fmeasure) 
//	OptimisationMeasure m_optimisationMeasure = OptimisationMeasure.Fmeasure; //chwilowo tak ustawiam na potrzeby eksperymentu 

    /**
     * Values od parameters
     * dodane ggora 25.07.2016
     */
    protected MultiDimensionalParameters m_multiDimParams; 

//	private ParameterisedTestResult m_results; //zmiana ggora: zmieni�em zmienn� lokaln� na element klasy (aby potem wypisa� wyniki) //stare
	private Parameterised3DTestResult m_results; //zmiana ggora: zmieni�em zmienn� lokaln� na element klasy (aby potem wypisa� wyniki)  //zmiana ggora 5.08.2016

	/*
	 * Czy drukowa� macierze estymacji. Na poziomie klasyfikacji mo�na wy��czy� (ustawi� false). U�ywam tego tylko do analizy dzia�ania algorytmu.
	 */
//	public static boolean s_printMatrixes = true;
	public static boolean s_printMatrixes = false; //standardowo; zmieni�em 7.08.2020
	
	/** Path to file with matrix with estimated values for different parameters. */
//	private static final String PATH_TO_MATRIX_ESTIMATED = "src/data/DEBUG/matrix_estimated.txt";
//ko�c�wka pliku i rozszerzenie dopisywane w programie
//	private static final String PATH_TO_MATRIX_ESTIMATED = "src/data/DEBUG/matrix_estimated";
	private static final String PATH_TO_MATRIX_ESTIMATED = "C:/DR_GG/GG_PLAN/ggPlan/src/data/DEBUG/matrix_estimated"; //na potrzeby eksperymentu wywo�ywania na zewn�trz w p�tli wypisywanie macierzy 
	
	
    /**
     * Constructor assuming that the default value of the parameter
     * is given in the properties.
     *
     * @param prop                   Map between property names and property values.
     */
//    public AbstractParameterised3DClassifier(Properties prop, String paramName) throws PropertyConfigurationException //stare
//    * @param paramName              Parameter name.
    public AbstractParameterised3DClassifier(Properties prop) throws PropertyConfigurationException //zmieni�em ggora 6.08.2016
    {
        super(prop);
//        m_ParamName = paramName;
    	//teoretycznie mog� by� te� zdefiniowane case'y dla 1D i 2D dla trzeba poprawi� kod (patrz uwaga do zmiennej m_optimisation4D
        m_optimisation4D = getBoolProperty(OPTIMISATION_4D_PROPERTY_NAME);
        //poni�sze 8 linijek (try-catch) doda�em 8.08 na podstawie sugestii Arka 
        try
        {
        	m_optimisationMeasure = OptimisationMeasure.valueOf(getProperty(OPTIMISATION_MEASURE_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
        	throw new PropertyConfigurationException("Unknown optimisation measure: "+getProperty(OPTIMISATION_MEASURE_PROPERTY_NAME));
        }
    }

    /**
     * Constructor used when loadind the object from a file.
     */
    public AbstractParameterised3DClassifier()
    {
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void writeAbstractParameterisedClassifier(ObjectOutputStream out) throws IOException
    {
    	writeConfigurationAndStatistics(out);
//    	out.writeObject(m_ParamName); //stare
    	out.writeObject(m_multiDimParams); //zmieni�em ggora 5.08.2016
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    protected void readAbstractParameterisedClassifier(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	readConfigurationAndStatistics(in);
        try
        {
            m_optimisation4D = getBoolProperty(OPTIMISATION_4D_PROPERTY_NAME);
        	m_optimisationMeasure = OptimisationMeasure.valueOf(getProperty(OPTIMISATION_MEASURE_PROPERTY_NAME));
        }
        catch (IllegalArgumentException e)
        {
    		throw new NotSerializableException(e.getMessage());
        }
        catch (PropertyConfigurationException e)
        {
    		throw new NotSerializableException(e.getMessage());
        }
//    	m_ParamName = (String)in.readObject(); //stare
    	m_multiDimParams = (MultiDimensionalParameters)in.readObject(); //zmieni�em ggora 5.08.2016
    }

//stare
//    /**
//     * Return name of the classifier parameter.
//     *
//     * @return Name of the classifier parameter.
//     */
//    public String getParameterName()
//    {
//        return m_ParamName;
//    }
//zmiana ggora 5.08.2016
    /**
     * Returns multi dimensional parameters
	 * @return the m_multiDimParams
	 */
	public MultiDimensionalParameters getMultiDimParams() {
		return m_multiDimParams;
	}
    
    
	/**
     * Learns the optimal value of the parameter.
     *
     * @param trainTable Training data table.
     * @param prog       Progress object for optimal parameter value search.
     * @return           Optimal value of the parameter.
     * @throws InterruptedException when the user interrupts the execution.
     */
    protected void learnOptimalMultiDimParameterValue(DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
        int[][][] confusionMatrices = null;
        NominalAttribute decAttr = trainTable.attributes().nominalDecisionAttribute();
        prog.set("Learning optimal parameter value", trainTable.noOfObjects());
//na potrzeby debug (analizowania czasu dzia�ania poszczeg�lnych cz�ci kodu
//        ggUtils.Timer.setName(0, "ca�o�� classifyWith3DParameter()");
//        ggUtils.Timer.setName(1, "ca�y wewn�trzny if dla distZero");
//        ggUtils.Timer.setName(2, "normalna p�tla dla przypadku gdy neighbours.size > 150");
//        ggUtils.Timer.setName(3, "funkcja checkIfDecSet");
//        ggUtils.Timer.setName(4, "znajdowanie najbli�szych s�siad�w");
//        ggUtils.Timer.setName(5, "markConsistencyLevels");
//        ggUtils.Timer.setName(6, "p�tla for dla przypadku distZero");
//        ggUtils.Timer.setName(7, "getIndexOfValue w p�tli for dla distZero");
//        ggUtils.Timer.setName(8, "getParamValueByIndex(indexForPvalue)");
        for (DoubleData dObj : trainTable.getDataObjects())
        {
//zakomentowa�em ggora 30.11.2016 try i catch (po mailu Arka 28.11.2016_g15:58)
//odkomentowa� je�li interesuje mnie sytuacja, �e pojedyncza klasyfikacja si� nie powiedzie i chcia�bym, �eby kod dzia�a� dalej
//        	try
//            {
//                double[] decisions = classifyWith3DParameter(dObj);
        	double[] decisions = classifyWithMultiParameter(dObj);
                if (confusionMatrices==null)
                {
                    confusionMatrices = new int[decisions.length][][];
                    for (int parVal = 0; parVal < confusionMatrices.length; parVal++)
                    {
                        confusionMatrices[parVal] = new int[decAttr.noOfValues()][];
                        for (int i = 0; i < confusionMatrices[parVal].length; i++)
                            confusionMatrices[parVal][i] = new int[decAttr.noOfValues()];
                    }
                }
                for (int parVal = 1; parVal < confusionMatrices.length; parVal++) {
//                	Report.debugnl("parVal=" + parVal);
//                	Report.debugnl("decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())=" + decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision()));
//
//                	Report.debugnl("decisions[parVal]=" + decisions[parVal]);
//                	Report.debugnl("decAttr.localValueCode(decisions[parVal])=" + decAttr.localValueCode(decisions[parVal]));
                	confusionMatrices[parVal][decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())][decAttr.localValueCode(decisions[parVal])]++;
                }
//            }
//            catch (RuntimeException e)
//            {
//            	Report.exception(e);
//            }
            prog.step();
        }

//        System.out.println(ggUtils.Timer.getAllTimersInfo());
//zmiana 7.08.2020: wynios�em t� linijk� z wn�trza p�tli if
        m_results = new Parameterised3DTestResult(getMultiDimParams(), decAttr, trainTable.getDecisionDistribution(), confusionMatrices, new Properties()); // zmiana ggora 5.08.2016; 11.08.2016
        if (s_printMatrixes) {
//          Report.debugnl("results=" + m_results.toString()); //dodane ggora
//          Report.debugnl("matrix=\n" + m_results.getMatrix());
            try {
            	for (PrintMeasureInMatrix printMeasure : PrintMeasureInMatrix.values()) { //Fmeasure, Gmean etc.
            		String strMatrix = m_results.getMatrix(printMeasure); //ggora 6.05.2017: na razie komentuj�; trzeba to poprawi� dla tablicy 4D
            		String newStrMatrix = strMatrix.replace('.', ',');//ggora 6.05.2017: na razie komentuj�; trzeba to poprawi� dla tablicy 4D
            		//          	Report.printToFile("src/data/DEBUG/matrix.txt", "matrix=\n" + m_results.getMatrix());
            		//          	Report.printToFile("src/data/DEBUG/matrix.txt", "matrix=\n" + newStrMatrix);

            		String strMatrixDelta = m_results.getMatrixDeltas(printMeasure); //ggora 6.05.2017: na razie komentuj�; trzeba to poprawi� dla tablicy 4D
            		String newStrMatrixDeltas = strMatrixDelta.replace('.', ','); //ggora 6.05.2017: na razie komentuj�; trzeba to poprawi� dla tablicy 4D
            		//          		Report.debugnl(newStrMatrixDeltas);
            		String fileName = PATH_TO_MATRIX_ESTIMATED + printMeasure + ".txt";
            		Report.printToFile(fileName, "matrix_estimated" + printMeasure  + "_deltas=\n" + newStrMatrixDeltas + "matrix_estimated=\n" + newStrMatrix);
            		//ggora 1.05.2017: chwilowo komentuj� 2 poni�sze linijki i powy�szy try, aby sprawdzi�, czy to co� przyspieszy
            		Report.printToFile(fileName, "matrix_estimated" + printMeasure  + "_deltas=" + Report.lineSeparator + newStrMatrixDeltas + "matrix_estimated=" + Report.lineSeparator + newStrMatrix);
            	}
            } catch (FileNotFoundException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            	System.exit(1);
            } //[ggora 1.05.2017 dot�d komentuj�]
//          Report.debugnl("matrix=\n" + m_results.getMatrix());
        	
        }
        int bestParamValue = 0;
        double bestValue = 0; //tylko dla infromacji i debug
        for (int parVal = 1; parVal < m_results.getParameterRange(); parVal++)
        {
//zmienione ggora: 5.04.2017
        	switch (m_optimisationMeasure) {
			case Fmeasure:
	        	if (m_results.getClassificationResult(parVal).getFmeasure() > m_results.getClassificationResult(bestParamValue).getFmeasure()) {
	                bestParamValue = parVal;
	        		bestValue = m_results.getClassificationResult(parVal).getFmeasure();
	        	}
				break;
			case Gmean:
	           	if (m_results.getClassificationResult(parVal).getGmean() > m_results.getClassificationResult(bestParamValue).getGmean()) {
	                bestParamValue = parVal;
		           	bestValue = m_results.getClassificationResult(parVal).getGmean();
	           	}
				break;
			case Accuracy:
	            if (m_results.getClassificationResult(parVal).getAccuracy() > m_results.getClassificationResult(bestParamValue).getAccuracy()) {
	                bestParamValue = parVal;
		           	bestValue = m_results.getClassificationResult(parVal).getAccuracy();
	            }
				break;
			default:
				throw new IllegalArgumentException("niezdefiniowany case: s_optimisationMeasure=" + m_optimisationMeasure);
			}
//stare:
////zmienione: ggora 24.06.2016; 5.08.2016
//            if (m_results.getClassificationResult(parVal).getAccuracy() > m_results.getClassificationResult(bestParamValue).getAccuracy()) //zamiast tego
//        	if (m_results.getClassificationResult(parVal).getFmeasure() > m_results.getClassificationResult(bestParamValue).getFmeasure())
//           	if (m_results.getClassificationResult(parVal).getFmeasure() > m_results.getClassificationResult(bestParamValue).getGmean()) //4.04.2017 ggora: tymczasowo dla sprawdzenia
//                bestParamValue = parVal;
        } //for parVal
//        System.out.println("bestParamValue" + bestParamValue + ", bestValue=" + bestValue); //dla cel�w debug
//stare:
//        makePropertyModifiable(m_ParamName);
//        setProperty(m_ParamName, Integer.toString(bestParamValue));
//zmieni�em ggora 5.08.2016:
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
        if (m_optimisation4D) { //dodane: ggora 11.05.2017
            setProperty(paramName, Double.toString(paramValues.sMajValue));
        } else {
        	setProperty(paramName, Double.toString(paramValues.sMinValue)); //ta sama warto�� sMaj co dla sMin
        }
        //sztucznie ustawiam te� na razie dla klasy wi�kszo�ciowej (przed 6.05.2017)
//      makePropertyModifiable(rseslib_Arek.processing.classification.parameterised.knn.KnnIDClassifier.S_MAJORITY_VALUE_PROPERTY_NAME);
//      setProperty(rseslib_Arek.processing.classification.parameterised.knn.KnnIDClassifier.S_MAJORITY_VALUE_PROPERTY_NAME, Double.toString(paramValues.sMinValue));
    }

    /**
     * Learns the optimal value of the parameter.
     * Obecnie nie u�ywane. To by�o u�ywane na samym pocz�tku tworzenia pomys�u optymalizacji 3D.
     *
     * @param testTable Training data table.
     * @param prog       Progress object for optimal parameter value search.
     * @return           Optimal value of the parameter.
     * @throws InterruptedException when the user interrupts the execution.
     */
    protected double[] classifyForAll3DParameterValues(DoubleData dObj, DoubleDataTable testTable, Progress prog) throws PropertyConfigurationException, InterruptedException
    {
    	double decisions[] = new double[m_multiDimParams.getVolume()]; //klasyfikacje obiektu dObj dla wszystkich mo�liwych parametr�w
    	ValuesForParameters paramValues;
    	for (int index = 0; index < decisions.length; index++) {
//ustawienie parametr�w klasyfikatora
            String paramName;
            paramValues = m_multiDimParams.getParamsForIndex(index);
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
			decisions[index] = classify(dObj);
		}
    	return decisions;
    }
    
    
//zakomentowa�em ggora 6.08.2016 (bo nie u�ywane)    
//    /**
//     * Zwraca opis dla jakich parametr�w k, jaki by� wynik klasyfikacji dla zbioru treningowego
//     * Dodane: ggora (IX 2015)
//     * @return String opisuj�cy wyniki
//     */
//    public String getPartialResults() {
//    	return m_results.toString();
//    }

}

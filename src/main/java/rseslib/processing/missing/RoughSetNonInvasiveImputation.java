/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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


package rseslib.processing.missing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import rseslib.structure.data.DoubleData;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.indiscernibility.SymmetricSimilarityIndiscernibility;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.ConfigurationWithStatistics;
import rseslib.system.PropertyConfigurationException;

/**
 * Non-invasive data imputation based on rough sets. See:
 * G. Gediga, I. Duentsch,
 * "Rough Set Data Analysis (RDA): Non invasive data imputation".
 * 
 * @author Rafal Latkowski
 */
public class RoughSetNonInvasiveImputation  extends ConfigurationWithStatistics implements Imputation
{
    /**
     * Indiscernibility relation used
     */
    private Indiscernibility m_oIndRelation = new SymmetricSimilarityIndiscernibility();

    /**
     * Limit maximal iterations
     */
    private static final String s_strMaxIterationLimit = "MAX_ITERATION_LIMIT";
    private int m_nMaxIterationLimit;

	/* Statistics */
    private int m_nIterations=0;
    private ArrayList<Integer> m_alIterationStatistics = new ArrayList<Integer>();

    /**
     * Constructor.
     * 
     * @param prop		Properties.
     * @throws PropertyConfigurationException
     */
    public RoughSetNonInvasiveImputation(Properties prop) throws PropertyConfigurationException
    {
        super(prop);
        m_nMaxIterationLimit=getIntProperty(s_strMaxIterationLimit);
    }

	/**
	 * Returns a table with imputed missing values.
	 * 
	 * @param aTable	Original data table with missing values.
	 * @return			Table with imputed missing values.
	 */
    public DoubleDataTable imputation(DoubleDataTable table)
    {
        boolean flag = true;
        while (flag && m_nIterations<m_nMaxIterationLimit)
        {
            m_nIterations++;
            DoubleDataTable imput = (DoubleDataTable)table.clone();
            flag = imputationIteration(imput,table);
            table = imput;
        }
        return table;
    }

    private boolean imputationIteration(DoubleDataTable imputationTable,DoubleDataTable referenceTable)
    {
        boolean imputed_flag = false;
        int cnt = 0;
        int decision = imputationTable.attributes().decision();
        for (DoubleData object : imputationTable.getDataObjects())
        {
            boolean has_missing = false;
            for (int i=0;i<object.attributes().noOfAttr();i++)
                if (i!=decision)
                if (Double.isNaN(object.get(i))) has_missing=true;
            // if an object contain a missing value
            if (has_missing)
            {
                // create temporary dictionaries for values 
                HashSet<Double> values[] = new HashSet[object.attributes().noOfAttr()];
                for (int i=0;i<object.attributes().noOfAttr();i++)
                    values[i] = new HashSet<Double>();
                // for each similar object fill its values into dictionaries
                for (DoubleData object2 : referenceTable.getDataObjects())
                    if (m_oIndRelation.similar(object,object2))
                    {
                        for (int i=0;i<object.attributes().noOfAttr();i++)
                            if (!Double.isNaN(object2.get(i)))
                            values[i].add(object2.get(i));                        
                    }
                // if value is missing and dictionary is consistent (size=1) then impute
                for (int i=0;i<object.attributes().noOfAttr();i++)
                    if (i!=decision)
                    if (Double.isNaN(object.get(i)))
                    if (values[i].size()==1)
                    {
                        object.set(i,values[i].iterator().next());
                        cnt++;
                        imputed_flag=true;
                    }
            }
        }
        m_alIterationStatistics.add(cnt);
        return imputed_flag;
    }
    
    public int getNumberOfIterations()
    {
        return m_nIterations;
    }
    
    public ArrayList<Integer> getImputedValueCounts()
    {
        return m_alIterationStatistics;
    }

    public void calculateStatistics()
    {
        addToStatistics("Number of iterations", Integer.toString(getNumberOfIterations()));
    }

    public void resetStatistics()
    {
        m_nIterations=0;
        m_alIterationStatistics=new ArrayList<Integer>();
    }
}

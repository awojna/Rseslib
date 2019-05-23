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


package rseslib.processing.classification;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.Report;
import rseslib.system.progress.Progress;

/**
 * Abstract class for classification methods
 * only with the method classifing a single test data object
 * left to be implemented by subclasses.
 *
 * @author      Arkadiusz Wojna
 */
public class SingleClassifierTest
{
    /**
     * Classifies a test data collection.
     *
     * @param cl        Classifier to be tested.
     * @param testTable Test data table.
     * @param prog      Progress object for classification process.
     * @return          Result of classification for the whole test table.
     * @throws InterruptedException when the user interrupts the execution.
     */
    public TestResult classify(Classifier cl, DoubleDataTable testTable, Progress prog) throws InterruptedException
    {
        prog.set("Classifing test table", testTable.noOfObjects());
        NominalAttribute decAttr = testTable.attributes().nominalDecisionAttribute();
        int[][] confusionMatrix = new int[decAttr.noOfValues()][];
        for (int i = 0; i < confusionMatrix.length; i++)
            confusionMatrix[i] = new int[decAttr.noOfValues()];
        for (DoubleData dObj : testTable.getDataObjects())
        {
            try
            {
            	double dec = cl.classify(dObj);
            	if (!Double.isNaN(dec))
            		confusionMatrix[decAttr.localValueCode(((DoubleDataWithDecision)dObj).getDecision())][decAttr.localValueCode(dec)]++;
            }
            catch (RuntimeException e)
            {
                Report.exception(e);
            }
            catch (PropertyConfigurationException e)
            {
                Report.exception(e);
            }
            prog.step();
        }
        cl.calculateStatistics();
        return new TestResult(decAttr, testTable.getDecisionDistribution(), confusionMatrix, cl.getStatistics());
    }
}

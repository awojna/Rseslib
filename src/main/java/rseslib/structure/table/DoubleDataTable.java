/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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


package rseslib.structure.table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import rseslib.structure.Headerable;
import rseslib.structure.data.DoubleData;
import rseslib.system.progress.Progress;

/**
 * Table of data objects with double values.
 *
 * @author      Arkadiusz Wojna, Grzegorz Gora
 */
public interface DoubleDataTable extends Headerable, Cloneable
{
    /**
     * Returns the number of objects.
     *
     * @return Number of objects.
     */
    public int noOfObjects();

    /**
     * Adds a data object to this table.
     *
     * @param obj The object to be added.
     */
    public void add(DoubleData obj);

    /**
     * Removes a data object from this table.
     *
     * @param obj  The object to be removed.
     * @return     True, if the object was found and removed from this table,
     *             false otherwise.
     */
    public boolean remove(DoubleData obj);

    /**
     * Identifies and sets the minority decision.
     * The minority decision is set only in case
     * when this table has two decision classes.
     */
    public void setMinorityDecision();

    /**
     * Assigns the minority decision in this table the same as in a given table.
     * 
     * @param table  Table providing the minority decision to be set.
     */
    public void takeMinorityDecisionFrom(DoubleDataTable table);
    
    /**
     * Returns collection of all objects from this table.
     *
     * @return Collection of all objects from this table.
     */
    public ArrayList<DoubleData> getDataObjects();

    /**
     * Returns the basic statistics of a given numerical attribute.
     *
     * @return Statistics of a given numerical attribute.
     */
    public NumericalStatistics getNumericalStatistics(int attr);

    /**
     * Returns the distribution of decision values in this table if the decision is nominal.
     * Array indices correspond to local decision codes from this data header.
     *
     * @return Distribution of decisions in this table.
     */
    public int[] getDecisionDistribution();

    /**
     * Returns the fraction of the minority decision in this table.
     *
     * @return Fraction of the minority decision in this table.
     */
    public double getPercentOfMinorityDecision();
    
    /**
     * Returns the distribution of values in this table for a nominal attribute.
     * Array indices correspond to local value codes for a given attribute.
     *
     * @param attrInd	Index of the attribute.
     * @return			Distribution of values in this table.
     */
    public int[] getValueDistribution(int attrInd);

    /**
     * Random split of this table into 2 data collections
     * with the splitting ratio noOfPartsForLeft to noOfPartsForRight.
     *
     * @param noOfPartsForLeft  Number of parts for the table returned at the position 0.
     * @param noOfPartsForRight Number of parts for the table returned at the position 1.
     * @return                  Table split into 2 data collections.
     */
    public ArrayList<DoubleData>[] randomSplit(int noOfPartsForLeft, int noOfPartsForRight);

    /**
     * Random partition of this table into a given number of parts of equal sizes.
     *
     * @param noOfParts Number of parts to be generated.
     * @return          Table divided into noOfParts collections.
     */
    public ArrayList<DoubleData>[] randomPartition(int noOfParts);

    /**
     * Random partition of this table into a given number of parts of equal sizes preserving class distribution.
     *
     * @param noOfParts Number of parts to be generated.
     * @return          Table divided into noOfParts collections.
     */
    public ArrayList<DoubleData>[] randomStratifiedPartition(int noOfParts);

    /**
     * Create and return a copy of this object.
     * 
     * @return Copy of this object.
     */
    public Object clone();

    /**
     * Saves this object to a file.
     *
     * @param outputFile File to be used for storing this object.
     * @param prog       Progress object for progress reporting.
     * @throws IOException If an I/O error has occured.
     * @throws InterruptedException If user has interrupted saving object.
     */
    public void store(File outputFile, Progress prog) throws IOException, InterruptedException;

    /**
     * Saves this object to a file in arff format.
     *
     * @param outputFile File to be used for storing this object.
     * @param prog       Progress object for progress reporting.
     * @throws IOException If an I/O error has occured.
     * @throws InterruptedException If user has interrupted saving object.
     */
    public void storeArff(String name, File outputFile, Progress prog) throws IOException, InterruptedException;
}

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


package rseslib.processing.reducts;

import java.util.BitSet;
import java.util.Collection;
import java.util.Properties;

import rseslib.processing.discernibility.DiscernibilityMatrixProvider;
import rseslib.processing.logic.KurzydlowskiPrimeImplicantsProvider;
import rseslib.processing.logic.PrimeImplicantsProvider;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Exhaustive algorithm finding all global reducts in a data table
 * by reduction to prime implicants.
 * 
 * @author Rafal Latkowski
 */
public class AllGlobalReductsProvider extends Configuration implements GlobalReductsProvider
{
	/** Number of attributes. */
	private int m_nNumberOfAttributes;
	/** Discernibility matrix computed for a given table. */
	private DiscernibilityMatrixProvider m_Discernibility;
	/** Algorithm finding all prime implicants given a CNF boolean formula. */
    private PrimeImplicantsProvider m_oPrimeImplicantsProvider = new KurzydlowskiPrimeImplicantsProvider();

    /**
     * Constructor taking a data table used to compute global reducts.
     *   
     * @param prop 		Parameters of the algorithm.
     * @param table		Data table used to compute global reducts.
     * @throws PropertyConfigurationException 
     */
    public AllGlobalReductsProvider(Properties prop, DoubleDataTable table) throws PropertyConfigurationException
    {
        super(prop);
        m_nNumberOfAttributes = table.attributes().noOfAttr();
        m_Discernibility = new DiscernibilityMatrixProvider(getProperties(), table);
    }

    /**
     * Returns a set of global reducts.
     * Each reduct is represented by a BitSet object,
     * get(i) returns true if and only if the i-th attribute belongs to the reduct.
     * The attribute indices are defined by the header of the data table.
     *
     * @return	Set of global reducts.
     */
    public Collection<BitSet> getReducts()
    {
        /* generate CNF */
        Collection<BitSet> cnf = m_Discernibility.getDiscernibilityMatrix();
        /* compute all prime implicants */
        return m_oPrimeImplicantsProvider.generatePrimeImplicants(cnf, m_nNumberOfAttributes);
    }

    /**
     * Returns the indiscernibility relation used to compute the discernibility matrix.
     *
     * @return	Indiscernibility relation.
     */
    public Indiscernibility getIndiscernibilityForMissing()
    {
    	return m_Discernibility.getIndiscernibilityForMissing();
    }
}

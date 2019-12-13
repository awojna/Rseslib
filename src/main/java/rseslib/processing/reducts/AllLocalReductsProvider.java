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


package rseslib.processing.reducts;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Properties;

import rseslib.processing.discernibility.DiscernibilityMatrixProvider;
import rseslib.processing.logic.KurzydlowskiPrimeImplicantsProvider;
import rseslib.processing.logic.PrimeImplicantsProvider;
import rseslib.structure.data.DoubleData;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * Exhaustive algorithm finding all local reducts
 * in a data table for a given data object
 * by reduction to prime implicants.
 * 
 * @author Rafal Latkowski
 */
public class AllLocalReductsProvider extends Configuration implements LocalReductsProvider
{
	/** Number of attributes. */
	private int m_nNumberOfAttributes;
	/** Discernibility matrix computed for a given table. */
	private DiscernibilityMatrixProvider m_Discernibility;
	/** Algorithm finding all prime implicants given a CNF boolean formula. */
    private PrimeImplicantsProvider m_oPrimeImplicantsProvider = new KurzydlowskiPrimeImplicantsProvider(null);

    /**
     * Constructor taking a data table used to compute local reducts.
     *   
     * @param prop 		Parameters of the algorithm.
     * @param table		Data table used to compute local reducts.
     * @throws PropertyConfigurationException 
     */
    public AllLocalReductsProvider(Properties prop, DoubleDataTable table) throws PropertyConfigurationException
    {
        super(prop);
        m_nNumberOfAttributes = table.attributes().noOfAttr();
        m_Discernibility = new DiscernibilityMatrixProvider(getProperties(), table);
    }

    /**
     * Returns a set of local reducts of a given data object.
     * Each reduct is represented by a BitSet object,
     * get(i) returns true if and only if the i-th attribute belongs to the reduct.
     * The attribute indices are defined by the header of the data table.
     *
     * @param object	Data object used to compute local reducts.
     * @return			Set of local reducts.
     */
    public Collection<BitSet> getSingleObjectReducts(DoubleData object)
    {
        /* generate CNF */
        Collection<BitSet> cnf = m_Discernibility.getLocalDiscernibility(object);
        if (cnf.isEmpty()) return new ArrayList<BitSet>();
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

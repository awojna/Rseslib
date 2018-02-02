/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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
 * @author Rafal Latkowski
 *
 */
public class AllGlobalReductsProvider extends Configuration implements GlobalReductsProvider
{
	private int m_nNumberOfAttributes;
	private DiscernibilityMatrixProvider m_Discernibility;
    private PrimeImplicantsProvider m_oPrimeImplicantsProvider = new KurzydlowskiPrimeImplicantsProvider(null);

    /**
     * @throws PropertyConfigurationException 
     * 
     */
    public AllGlobalReductsProvider(Properties prop, DoubleDataTable table) throws PropertyConfigurationException
    {
        super(prop);
        m_nNumberOfAttributes = table.attributes().noOfAttr();
        m_Discernibility = new DiscernibilityMatrixProvider(getProperties(), table);
    }

    public Collection<BitSet> getReducts()
    {
        /* generowanie CNF */
        Collection<BitSet> cnf = m_Discernibility.getDiscernibilityMatrix();
        /* wyliczanie implikantow pierwszych */
        return m_oPrimeImplicantsProvider.generatePrimeImplicants(cnf, m_nNumberOfAttributes);
    }

    public Indiscernibility getIndiscernibilityForMissing()
    {
    	return m_Discernibility.getIndiscernibilityForMissing();
    }
}

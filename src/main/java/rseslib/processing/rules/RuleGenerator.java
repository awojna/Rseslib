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


package rseslib.processing.rules;

import java.util.Collection;

import rseslib.structure.rule.Rule;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/**
 * RuleGenerator generates a collection of rules.
 *
 * @author      Arkadiusz Wojna
 */
public interface RuleGenerator
{
    /**
     * Returns a collection of rules generated from data table.
     *
     * @param tab  Data objects to be used for rule generation.
     * @param prog Progress object.
     * @return     Collection of generated rules.
     * @throws PropertyConfigurationException
     * @throws InterruptedException
     */
    public Collection<Rule> generate(DoubleDataTable tab, Progress prog) throws PropertyConfigurationException, InterruptedException;
}

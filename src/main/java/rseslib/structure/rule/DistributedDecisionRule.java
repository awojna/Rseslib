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


package rseslib.structure.rule;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.vector.Vector;

/**
 * Inteface for a rule with distributed decision.
 * 
 * @author Rafal Latkowski
 */
public interface DistributedDecisionRule extends Rule
{
    /**
     * Sets the decision distribution of this rule.
     *
     * @param decVec	Decision distribution to be set.
     * @param decAttr	Information about the decision attribute.
     */
    public void setDecisionVector(Vector decVec, NominalAttribute decAttr);

    /**
     * Returns the decision distribution of this rule.
     *
     * @return Decision distribution of this rule.
     */
    public Vector getDecisionVector();
}

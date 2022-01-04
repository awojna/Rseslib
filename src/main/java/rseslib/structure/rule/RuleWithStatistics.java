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


package rseslib.structure.rule;

/**
 * Interface for a rule with statistics.
 *
 * @author      Arkadiusz Wojna
 */
public interface RuleWithStatistics extends Rule
{
    /**
     * Sets the accuracy of this rule.
     *
     * @param acc	Accuracy of this rule between 0 and 1.
     */
    public void setAccuracy(double acc);

    /**
     * Sets the support of this rule.
     *
     * @param supp	Support of this rule between 0 and 1.
     */
    public void setSupport(double supp);

    /**
     * Returns the accuracy of this rule.
     *
     * @return     Accuracy of this rule between 0 and 1.
     */
    public abstract double getAccuracy();

    /**
     * Returns the support of this rule.
     *
     * @return     Support of this rule between 0 and 1.
     */
    public abstract double getSupport();
}

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


package rseslib.structure.attribute;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * Header with information about attributes.
 *
 * @author      Arkadiusz Wojna
 */
public interface Header extends Serializable
{
    /**
     * Returns the number of attributes.
     *
     * @return Number of attributes.
     */
    public abstract int noOfAttr();

    /**
     * Returns an attribute.
     *
     * @param attrInd Index of the attribute.
     * @return        Attribute.
     */
    public abstract Attribute attribute(int attrInd);

    /**
     * Returns the name of an attribute.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        Name of an attribute.
     */
    public abstract String name(int attrInd);

    /**
     * Checks whether this attribute is conditional or decision.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if this attribute is conditional or decision false otherwise.
     */
    public abstract boolean isInterpretable(int attrInd);

    /**
     * Checks whether an attribute is a text.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if an attribute is a text false otherwise.
     */
    public abstract boolean isText(int attrInd);

    /**
     * Checks whether an attribute is numeric.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if an attribute is numeric false otherwise.
     */
    public abstract boolean isNumeric(int attrInd);

    /**
     * Checks whether an attribute is nominal.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if an attribute is nominal false otherwise.
     */
    public abstract boolean isNominal(int attrInd);

    /**
     * Checks whether a given string denotes missing value.
     *
     * @param value String to be checked.
     * @return      True if value denotes missing value false otherwise.
     */
    public abstract boolean isMissing(String value);

    /**
     * Returns missing value.
     *
     * @return String that dentoes the missing value.
     */
    public abstract String missing();

    /**
     * Checks whether this attribute is conditional.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if this attribute is conditional false otherwise.
     */
    public abstract boolean isConditional(int attrInd);

    /**
     * Checks whether this attribute is decision.
     *
     * @param attrInd Index of the attribute to be checked.
     * @return        True if this attribute is decision false otherwise.
     */
    public abstract boolean isDecision(int attrInd);

    /**
     * Returns the index of the decision attribute.
     *
     * @return Index of the decision attribute.
     */
    public abstract int decision();

    /**
     * Returns the information about the decision attribute
     * if the decision is nominal.
     *
     * @return Decision attribute as a nominal attribute.
     */
    public abstract NominalAttribute nominalDecisionAttribute();

    /**
     * Returns true for equivallent header object.
     * @param obj - object for comparison 
     * @return true if header object is equivallent.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj);

    /**
     * Writes this object.
     *
     * @param output                          Output for writing.
     * @throws IOException                    if an I/O error has occured.
     */
    public abstract void store(BufferedWriter output) throws IOException;

    /**
     * Writes this header in arff format.
     *
     * @param name		Name of the relation.
     * @param output	Output for writing.
     * @throws IOException if an I/O error has occured.
     */
    public abstract void storeArff(String name, BufferedWriter output) throws IOException;
}

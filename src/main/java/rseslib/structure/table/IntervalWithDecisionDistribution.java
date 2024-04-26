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


package rseslib.structure.table;

import java.io.Serializable;

import rseslib.structure.vector.Vector;

/**
 * Value interval of a numeric attribute
 * having the same decision distribution
 * in vicinities of all values
 * contained in this interval.
 *
 * @author      Arkadiusz Wojna
 */
public class IntervalWithDecisionDistribution implements Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	/** Left end of the interval. */
    private double m_nLeft;
    /** Right end of the interval. */
    private double m_nRight;
    /** Flag indicating whether the interval is left-hand closed. */
    private boolean m_bLeftClosed;
    /** Flag indicating whether the interval is right-hand closed. */
    private boolean m_bRightClosed;
    /** Decison distribution for values contained in this interval. */
    private Vector m_DecDistribution;

    /**
     * Constructor.
     *
     * @param left        Left end of the interval.
     * @param right       Right end of the interval.
     * @param leftClosed  Flag indicating whether the interval is left-hand closed.
     * @param rightClosed Flag indicating whether the interval is right-hand closed.
     * @param decDistr    Decison distribution for values contained in this interval.
     */
    public IntervalWithDecisionDistribution(double left, double right, boolean leftClosed, boolean rightClosed, Vector decDistr)
    {
        m_nLeft = left;
        m_nRight = right;
        m_bLeftClosed = leftClosed;
        m_bRightClosed = rightClosed;
        m_DecDistribution = decDistr;
    }

    /**
     * Sets the left end of the interval.
     *
     * @param left Left end of the interval.
     */
    public void setLeft(double left)
    {
        m_nLeft = left;
    }

    /**
     * Returns the left end of the interval.
     *
     * @return Left end of the interval.
     */
    public double getLeft()
    {
        return m_nLeft;
    }

    /**
     * Sets the right end of the interval.
     *
     * @param right Right end of the interval.
     */
    public void setRight(double right)
    {
        m_nRight = right;
    }


    /**
     * Returns the right end of the interval.
     *
     * @return Right end of the interval.
     */
    public double getRight()
    {
        return m_nRight;
    }

    /**
     * Sets this interval left-hand open.
     */
    public void setLeftOpen()
    {
        m_bLeftClosed = false;
    }

    /**
     * Sets this interval left-hand closed.
     */
    public void setLeftClosed()
    {
        m_bLeftClosed = true;
    }

    /**
     * Returns the flag indicating whether the interval is left-hand closed.
     *
     * @return True, if this interval is left-hand closed, false otherwise.
     */
    public boolean leftClosed()
    {
        return m_bLeftClosed;
    }

    /**
     * Sets this interval right-hand open.
     */
    public void setRightOpen()
    {
        m_bRightClosed = false;
    }

    /**
     * Sets this interval right-hand closed.
     */
    public void setRightClosed()
    {
        m_bRightClosed = true;
    }

    /**
     * Returns the flag indicating whether the interval is right-hand closed.
     *
     * @return True, if this interval is right-hand closed, false otherwise.
     */
    public boolean rightClosed()
    {
        return m_bRightClosed;
    }

    /**
     * Returns decison distribution for values contained in this interval.
     *
     * @return Decison distribution for values contained in this interval.
     */
    public Vector getDecDistribution()
    {
        return m_DecDistribution;
    }

    /**
     * Compares a value with the range of this interval.
     *
     * @param val  Numeric value to be compared.
     * @return     Value -1, 0 or 1, as this interval is below,
     *             contains or is over the value val.
     */
    public int compareTo(double val)
    {
        if (m_nLeft > val || m_nLeft==val && !m_bLeftClosed) return 1;
        else if (m_nRight < val || m_nRight==val && !m_bRightClosed) return -1;
        return 0;
    }

    /**
     * Constructs string representation of this interval.
     *
     * @return String representation of this interval.
     */
    public String toString()
    {
        StringBuffer sbuf = new StringBuffer();
        if (m_nLeft < m_nRight)
        {
            sbuf.append("Interval ");
            if (m_bLeftClosed) sbuf.append("<");
            else sbuf.append("(");
            sbuf.append(m_nLeft+"; "+m_nRight);
            if (m_bRightClosed) sbuf.append(">");
            else sbuf.append(")");
        }
        else sbuf.append("Value "+m_nLeft);
        sbuf.append(": "+m_DecDistribution);
        return sbuf.toString();
    }
}

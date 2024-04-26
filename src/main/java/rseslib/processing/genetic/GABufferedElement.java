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


package rseslib.processing.genetic;

import java.util.HashMap;

/**
 * @author Rafal Latkowski
 *
 */
public abstract class GABufferedElement implements GAElement
{
    class BestElement
    {
        double max_fitness = Double.NaN;
        GABufferedElement max_element = null;
    }
    
    static HashMap<Integer,BestElement> s_mapBestElements 
        = new HashMap<Integer,BestElement>();

    static HashMap<Integer,HashMap<GABufferedElement,Double>> s_mapBuffers 
        = new HashMap<Integer,HashMap<GABufferedElement,Double>>();
    
    HashMap<GABufferedElement,Double> m_mapFittnesBuffer;

    BestElement m_oGlobalBestElement;
    
    /**
     * 
     */
    public GABufferedElement(int token)
    {
        m_oGlobalBestElement = s_mapBestElements.get(token);
        if (m_oGlobalBestElement==null)
        {
            m_oGlobalBestElement = new BestElement();
            m_mapFittnesBuffer = new HashMap<GABufferedElement,Double>();
            s_mapBestElements.put(token,m_oGlobalBestElement);
            s_mapBuffers.put(token,m_mapFittnesBuffer);
        }
        else
        {
            m_mapFittnesBuffer = s_mapBuffers.get(token);
        }
    }

    public GABufferedElement(GABufferedElement template)
    {
        m_mapFittnesBuffer=template.m_mapFittnesBuffer;
        m_oGlobalBestElement=template.m_oGlobalBestElement;
    }

    abstract public double getNonBufferedFitness();
    
    /**
     * @see rseslib.processing.genetic.GAElement#fitness()
     */
    public double fitness()
    {
        Double d = m_mapFittnesBuffer.get(this);
        if (d==null)
        {
            double f = getNonBufferedFitness();
            if (Double.isNaN(m_oGlobalBestElement.max_fitness)||
               (f>m_oGlobalBestElement.max_fitness))
            {
                m_oGlobalBestElement.max_fitness=f;
                m_oGlobalBestElement.max_element=this;
                System.out.println("Best fitness = "+f);
            }
            m_mapFittnesBuffer.put(this,f);
            return f;
        }
        else return d;
    }

    public static int createNewToken()
    {
        int token=0;
        for (Integer i : s_mapBestElements.keySet())
        {
            if (i>token) token=i;
        }
        return token+1;
    }
    
    public int getBufferSize()
    {
        return m_mapFittnesBuffer.size();
    }
    
    abstract public boolean equals(Object o);

    abstract public int hashCode();
    
    public double getBestElementFitness()
    {
        return m_oGlobalBestElement.max_fitness;
    }

    public GABufferedElement getBestElement()
    {
        return m_oGlobalBestElement.max_element;
    }
}

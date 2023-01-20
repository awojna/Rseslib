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


package rseslib.qmak.UI.chart;

import rseslib.qmak.UI.chart.AttrInfo;

/**
 * Informacja o atrybucie, wykorzystywana przy listach wyboru atrybutow
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class AttrInfo {
    
    /** indeks atrybutu w naglowku */
    int id;
    /** nazwa atrybutu */
    String name;
    
    /**
     * Konstruktor tworzacy informacje o atrybucie na podstawie nazwy i 
     * indeksu atrybutu w naglowku
     * 
     * @param no indeks atrybutu w naglowku
     * @param label nazwa atrybutu
     */
    public AttrInfo(int no, String label)
    {
        id = no;
        name = label;
    }
    
    /**
     * Zwraca indeks atrybutu w naglowku
     * 
     * @return indeks atrybutu w naglowku
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Zwraca nazw� atrybutu
     * 
     * @return nazwa atrybutu
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Konstruuje tekstowa reprezentacje informacji o atrybucie
     * 
     * @return tekstowa reprezentacja naglowka
     */
    public String toString()
    {
        return name;
    }
    
    /**
     * Sprawdza czy podany obiekt reprezentuje ten sam naglowek
     * 
     * @return wynik porownania
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof AttrInfo) {
            AttrInfo attr = (AttrInfo) obj;
            if ((id == attr.id) && (name == attr.name))
                return true;
            else 
                return false;
        }
        else return false;
    }
}

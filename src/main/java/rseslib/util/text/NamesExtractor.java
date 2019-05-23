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


package rseslib.util.text;

import java.util.ArrayList;

/**
 * String analyser transforming a single string
 * with comma separated names into an array of strings.
 *
 * @author      Arkadiusz Wojna
 */
public class NamesExtractor
{
    /**
     * Transforms a single string
     * with comma separated names into an array of strings.
     *
     * @param names Comma separated names.
     * @return      Array of names.
     */
    public static String[] getNames(String names)
    {
	ArrayList<String> nameList = new ArrayList<String>();
	int nextIndex;
        for (int searchFrom = 0; searchFrom < names.length(); searchFrom = nextIndex + 1)
        {
            nextIndex = names.indexOf(',', searchFrom);
            if (nextIndex==-1) nextIndex = names.length();
            nameList.add(names.substring(searchFrom, nextIndex));
            searchFrom = nextIndex + 1;
        }
	String[] nameArray = new String[nameList.size()];
	for (int name = 0; name < nameArray.length; name++)
	    nameArray[name] = (String)nameList.get(name);
	return nameArray;
    }
}
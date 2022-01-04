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


package rseslib.structure.attribute.formats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * The objects recognize the data format in a file.
 *
 * @author      Arkadiusz Wojna
 */
public class DataFormatRecognizer
{
	public enum Format { ARFF, RSES, CSV }; 
  
	/**
     * Checks whether the format of data in a given file is from RSES2.x.
     *
     * @param dataFile Data to be checked.
     * @return         True if the format of data is from RSES2.x, false otherwise.
     * @throws IOException if an I/O error has occured.
     */
    public Format recognizeFormat(File dataFile) throws IOException
    {
    	Format frm = null;
    	boolean arff_possible = true;    	
        BufferedReader bw = new BufferedReader(new FileReader(dataFile));
        while (bw.ready() && frm==null)
        {
        	String first_line = bw.readLine().trim();
        	if (first_line.length() > 0)
        		switch (first_line.charAt(0))
        		{
        		case '#':
        			frm = Format.CSV;
        			break;
        		case '$':
        			arff_possible = false;
        			break;
        		case '%':
        			break;
        		default:
        			if (first_line.startsWith("TABLE"))
        				frm = Format.RSES;
        			else if (arff_possible && first_line.toLowerCase().startsWith("@relation"))
        				frm = Format.ARFF;
        			else
        				frm = Format.CSV;
        		}
        }
        bw.close();
        return frm;
    }
}

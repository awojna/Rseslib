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


package rseslib.qmak.dataprocess.project;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author damian
 *
 */
public interface iQXMLstoreable {
	
	/**
	 * Method that stores iQProjectElement at the end of BufferedWriter. Data should be stored using
	 * with bw.append(String) method in XML-similar format
	 * 
	 * @param bw BufferedWriter 
	 */
	public void XMLstore(BufferedWriter bw) throws IOException ;
		
	
}

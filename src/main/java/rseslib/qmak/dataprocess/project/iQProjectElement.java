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


package rseslib.qmak.dataprocess.project;

import java.awt.Point;

/**
 * Interfejs elementu projektu iQProjectElement
 * @author damian
 *
 */
public interface iQProjectElement {
	
	/**
	 * Method that answers whether object implementing this 
	 * interface should be considered as a test result 
	 * 
	 * @return answer 
	 */
	public boolean isTestResult();
	
	/**
	 * Method that answers whether object implementing this 
	 * interface should be considered as a multiple test result 
	 * 
	 * @return answer 
	 */	
	public boolean isMultipleTestResult();

	/**
	 * Method that answers whether object implementing this 
	 * interface should be considered as a table 
	 * 
	 * @return answer 
	 */
	public boolean isTable();


	/**
	 * Method that answers whether object implementing this 
	 * interface should be considered as a classifier 
	 * 
	 * @return answer 
	 */
	public boolean isClassifier();
	
	/**
	 * Method that answers whether object implementing this 
	 * interface should be considered as a multiclassifier 
	 * 
	 * @return answer 
	 */
	public boolean isMulticlassifier();
	
	/**
	 * Method that answer whether object implements XMLstoreable 
	 * interface.
	 */
	public boolean isXMLstoreable();
	
	/**
	 * Method that answer whether object implements FileStoreable interface
	 * 
	 */
	public boolean isFileStoreable();
	
	/**
	 * Introduce yourself with unique name. Useful when saving project elements to files
	 */
	public String getName();
	
	/**
	 * Change element name
	 */
	public void setName(String name);
	
	/**
	 * Get element file name that is used to store file
	 */
	public String getFileName();
	
	/**
	 * Change element file name
	 */
	public void setFileName(String fileName);
	
	/**
	 * Remember element's position on pulpit
	 * 
	 * @param p
	 */
	public void setPosition(Point p);
	
	/**
	 * Get element's position on pulpit 
	 * 
	 */
	public Point getPosition();

	
	/**
	 * Ask for String representing skladowy-file extension
	 * @return
	 */
	public	String getBaseFileExtension();
	
	/**
	 * Ask for String representing XML-file extension name
	 * 
	 * @return String extension(with dot)
	 */
	public String getDescriptionFileExtension();
	
}

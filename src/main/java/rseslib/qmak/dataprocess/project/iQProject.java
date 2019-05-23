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


package rseslib.qmak.dataprocess.project;

import java.io.*;
import java.util.Set;

import rseslib.qmak.dataprocess.project.QPara;
import rseslib.qmak.dataprocess.project.QProjectProperties;
import rseslib.qmak.dataprocess.project.iQProjectElement;

/**
 * Interfejs projektu dla relacji z elementammi projektu i czescia graficzna
 * programu
 * 
 * @author damian
 * @author krzys
 * @version 1.00
 */
public interface iQProject {

	/**
	 * Zwraca liste par(rodzic, dziecko) zwiazanych ze soba elementow projektu
	 */
	public Set<QPara> getRelatives();

	/**
	 * Usuwa zwiazek dwoch elementow
	 * @param e1
	 * @param e2
	 */
	public void RemovePair(iQProjectElement e1, iQProjectElement e2);

	/**
	 * Usuwa elementy nadrzedne wzgledem danego(rodzicow)
	 * @param e
	 */
	public void RemoveParentsOf(iQProjectElement e);

	/**
	 * Usuwa elementy podrzedne wzgledem danego(dzieci)
	 * @param e
	 */
	public void RemoveChildrenOf(iQProjectElement e);

	/**
	 * Dodaje do parametru name liczbe tak, by zwracany napis byl unikalny w skali aktualnego projektu
	 * @param name 
	 * @param isPath informuje czy orginalna nazwa jest sciezka dostepu
	 * @return  unikalna nazwa w skali projektu
	 */
	public String CreateUniqeName(String name, boolean isPath);

	/**
	 * Zwraca liste wszystkich elementow projektu
	 * 
	 */
	public Set<iQProjectElement> GetProjectElements();

	/** 
	 * Default method for inserting to project such elements like: tables,
	 * classifiers, etc. Element must have unique name.
	 * @param obj object implementing iQProjectElement interface
	 * @return success
	 */
	public boolean insertElement(iQProjectElement obj);

	/**
	 * Default method for removing from project such elements like: tables,
	 * classifiers, etc.
	 * @param obj object to remove
	 * @return success
	 */
	public boolean removeElement(iQProjectElement obj);

	/**
	 * Access method to project's properties
	 * 
	 * @return requested properties object
	 */
	public QProjectProperties getProperties();

	/**
	 * Access method to project's properties
	 * 
	 * @param prop ProjectProperties
	 */
	public void setProperties(QProjectProperties prop);

	/**
	 * Saves project and its elements into default project's file
	 */
	public void saveProject() throws IOException;

	/**
	 * Saves project and its elements in specified file. File may not contain
	 * suffix .qpr
	 * @param fileName - absolute path to file
	 */
	public void saveProjectAs(String fileName) throws IOException;

	/**
	 * Loads project form file specified in projectProperties
	 */
	public void loadProject() throws IOException;

	/**
	 * Says whether project is saved (takes care only about project elements)
	 * 
	 * @return true = saved; false = unsaved
	 */
	public boolean isSaved();

	/**
	 * Gets project element with given name
	 * 
	 * @param elementName
	 *            nazwa elementu do pobrania
	 * @return element or null
	 */
	public iQProjectElement getElement(String elementName);

	/**
	 * Zastrzezenie nazwy elementu projektu
	 */
	public void registerName(String newName);
	
	/**
	 * Zwraca aktualny katalog do zapisywania elementow projektu
	 */
	public String getCurrentPath();
}

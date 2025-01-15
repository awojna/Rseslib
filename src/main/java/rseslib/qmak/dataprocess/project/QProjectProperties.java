/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import rseslib.qmak.dataprocess.project.QProjectProperties;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;
import rseslib.qmak.util.Utils;

import java.io.BufferedWriter;

/**
 * @author damian
 * 
 */
public class QProjectProperties implements iQXMLstoreable{

	/**
	 * Project descriptive name.
	 */
	private String projectName;

	/**
	 * Name of the project file (with .qpr suffix).
	 */
	private String fileName;

	/**
	 * Project access date and time.
	 */
	private Date accessDate;

	/**
	 * Project creation date and time.
	 */
	private Date creationDate;

	/**
	 * Project modification date and time.
	 */
	private Date modificationDate;

	/**
	 * Name of the project author.
	 */
	private String author;

	/**
	 * Project description
	 */
	private String description;

	/**
	 * Is project saved
	 */
	private boolean saved;

	/**
	 * Default constructor.
	 */
	public QProjectProperties() {
		String absolute_user_path = System.getProperty("user.dir") + System.getProperty("file.separator") + "default" + System.getProperty("file.separator") + "default.qpr";
		
		initialize("default", absolute_user_path , System.getProperty("user.name"));
	}

	/**
	 * Second constructor
	 * @param name String
	 * @param filename String
	 */
	public QProjectProperties(String newName, String newFilename,
			String newAuthor) {
		initialize(newName, newFilename, newAuthor);
	}

	/**
	 * Initialization method.
	 * @param newName String
	 * @param newFilename String
	 */
	private void initialize(String newName, String newFilename, String newAuthor) {
		setName(newName);
		setFileName(newFilename);
		accessDate = new Date(System.currentTimeMillis());
		creationDate = new Date(System.currentTimeMillis());
		modificationDate = new Date(System.currentTimeMillis());
		setAuthor(newAuthor);
		setDescription("Default description");
		setSaved();
	}

	/**
	 * Zwraca nazwe projektu
	 * @return String
	 */
	public String getName() {
		return projectName;
	}

	/**
	 * Nadaje nazwe projektowi
	 * @param newname String
	 */
	public void setName(String newname) {
		projectName = newname;
	}

	/**
	 * Zwraca plik glowny projektu
	 * @return String
	 */
	public String getFileName() {
		return fileName;
	}

	/** 
	 * Metoda zwraca nazwe katalogu z plikiem fileName. 
	 * Napis powinien sie konczyc separatorem pliku.
	 */
	public String getCurrentPath() {
		return fileName.substring(0, fileName.lastIndexOf(System.getProperty("file.separator")) + 1);
	}
	
	/**
	 * Zmienia nazwe pliku projektu wraz ze sciezka dostepu do niego
	 * @param newName String
	 */
	public void setFileName(String newName) {
		if (Utils.getExtension(newName).compareToIgnoreCase(Utils.qpr) == 0) {
			fileName = newName;
		} else {
			fileName = (newName + ".qpr");
		}
	}

	/**
	 * Zwraca date utworzenia projektu
	 * @return String
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Nadaje date utworzenia projektu
	 * @param date Date
	 */
	public void setCreationDate(Date date) {
		creationDate = date;
	}

	/**
	 * Zwraca date ostatniego dostepu do projektu
	 * @return String
	 */
	public Date getAccessDate() {
		return accessDate;
	}

	/**
	 * Ustawia date ostatiego dostepu
	 * @param date Date
	 */
	public void setAccessDate(Date date) {
		accessDate = date;
	}

	/**
	 * Zwraca date ostatniej modyfikacji projektu
	 * @return String
	 */
	public Date getModificationDate() {
		return modificationDate;
	}

	/**
	 * Ustawia date modyfikacji projektu
	 * @param date Date
	 */
	public void setModificationDate(Date date) {
		modificationDate = date;
	}

	/**
	 * Zwraca autora projektu
	 * @return String
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Ustawia autora projektu
	 * @param newname String
	 */

	public void setAuthor(String newname) {
		author = newname;
	}

	/**
	 * Zwraca opis projektu
	 * @param des String
	 */
	public void setDescription(String des) {
		description = des;
	}

	/**
	 * Ustawia opis projektu
	 * @return String - description of the project
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set project as saved
	 * 
	 */
	public void setSaved() {
		saved = true;
	}

	/**
	 * Set project as unsaved
	 * 
	 */
	public void setUnSaved() {
		saved = false;
	}

	/**
	 * 
	 * @return boolean - is project saved
	 */
	public boolean isSaved() {
		return saved;
	}

	/**
	 * Copies argument object on self. For dialog boxes purposes.
	 * 
	 * @param obj
	 *            object to assign from
	 */
	public void assign(QProjectProperties obj) {
		projectName = obj.projectName;
		fileName = obj.fileName;
		accessDate = obj.accessDate;
		creationDate = obj.creationDate;
		modificationDate = obj.modificationDate;
		author = obj.author;
		description = obj.description;
		saved = obj.isSaved();
	}

	public String toString() {
		StringBuffer rep = new StringBuffer();
		rep.append(" Project: " + projectName);
		rep.append(" File: " + fileName);
		rep.append(" Access date: " + accessDate.toString());
		rep.append(" Creation date: " + creationDate.toString());
		rep.append(" Modification date: " + modificationDate.toString());
		rep.append(" Author: " + author);
		rep.append(" Description: " + description + "\n");
		return rep.toString();
	}

	/**
	 * Zapisuje wlasciwosci w formacie xml
	 */
	public void XMLstore(BufferedWriter bw) throws IOException {
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG,
				Locale.ENGLISH);

		bw.append(" name=\"" + projectName + "\"");
		bw.append(" creationdate=\"" + df.format(creationDate) + "\"");
		bw.append(" moddate=\"" + df.format(modificationDate) + "\"");
		bw.append(" author=\"" + author + "\"");
		bw.append(" description=\"" + description + "\"");
	}
}

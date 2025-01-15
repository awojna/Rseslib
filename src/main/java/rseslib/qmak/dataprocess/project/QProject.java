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

import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import rseslib.qmak.dataprocess.project.QPara;
import rseslib.qmak.dataprocess.project.QProjectProperties;
import rseslib.qmak.dataprocess.project.iQProject;
import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;
import rseslib.qmak.XmlOpener;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.FileStoreable;
import rseslib.system.progress.EmptyProgress;

/**
 * Klasa reprezentujaca projekt uzytkownia w programie
 * 
 * @author damian
 * @author Leszek Tur - kilka funkcji
 * 
 */
public class QProject implements iQProject {
	
	/**
	 * Zaleznosci pomiedzy elementami projektu
	 */
	private Set<QPara> pokrewienstwo;

	/**
	 * Wlasciwosci projektu. Niestandardowa postac.
	 */
	private QProjectProperties projectProperties;

	/**
	 * Elementy projektu
	 */
	private Set<iQProjectElement> projectElements;
	
	/**
	 * Zbior nazw elementow w projekcie
	 */
	private Set<String> reservedNames;

	
	public static String extension = "qpr";
	
	/**
	 * Default constructor. 
	 */
	public QProject(String fileName) {
		projectProperties = new QProjectProperties();
		if (fileName != null) projectProperties.setFileName(fileName);
		projectElements = new HashSet<iQProjectElement>();
		pokrewienstwo = new HashSet<QPara>();
		reservedNames = new HashSet<String>();
		projectProperties.setSaved();
	}

	/**
	 * Parametrised constructor.
	 * 
	 * @param name String
	 * @param fileName String
	 */
	public QProject(String name, String fileName, String author) {
		projectProperties = new QProjectProperties(name, fileName, author);
		projectElements = new HashSet<iQProjectElement>();
		pokrewienstwo = new HashSet<QPara>();
		reservedNames = new HashSet<String>();
		projectProperties.setSaved();
	}
	
	public void registerName(String newName) {
		reservedNames.add(newName);
	}
	
	public Set<QPara> getRelatives() {
		return pokrewienstwo;
	}

	public Set<iQProjectElement> GetProjectElements() {
		return projectElements;
	}

	public void RemovePair(iQProjectElement e1,iQProjectElement e2) {
		for (Iterator it = pokrewienstwo.iterator(); it.hasNext();) {
			QPara pa = (QPara) it.next();
			if (pa.Dziec.equals(e1) && pa.Rod.equals(e2))
				it.remove();
		}	
	}
	
	public void RemoveParentsOf(iQProjectElement e) {
		for (Iterator it = pokrewienstwo.iterator(); it.hasNext();) {
			QPara pa = (QPara) it.next();
			if (pa.Dziec.equals(e))
				it.remove();
		}
	}
	
	public void RemoveChildrenOf(iQProjectElement e) {
		for (Iterator it = pokrewienstwo.iterator(); it.hasNext();) {
			QPara pa = (QPara) it.next();
			if (pa.Rod.equals(e))
				it.remove();
		}
	}
	
	/**
	 * Sprawdzenie czy w projekcie jest element o nazwie name
	 * @param elementName nazwa
	 * @return true - jest, false - brak
	 */
	private boolean includesElement(String elementName) {
		Iterator i, j;
		iQProjectElement el;
		String reserved;
		i = projectElements.iterator();
		while (i.hasNext()) {
			el = (iQProjectElement) i.next();
			if (el.getName().compareTo(elementName) == 0) return true;
		}
		j = reservedNames.iterator();
		while (j.hasNext()) {
			reserved = (String) j.next();
			if (reserved.compareTo(elementName) == 0) return true;
		}
		return false;
	}
	
	public iQProjectElement getElement(String elementName) {
		iQProjectElement el;
		Iterator i = projectElements.iterator();
		while (i.hasNext()) {		
			el = (iQProjectElement) i.next();
			if (el.getName().compareTo(elementName) == 0) return el;
		}
		return null;	
	}

	public String CreateUniqeName(String name, boolean isPath) {
		if (isPath) {
			/*
			 * tutaj jako podstawe dla wyniku dam znaki w name po ostanim
			 * rozdzielaczu
			 */
			int index = name.lastIndexOf(System.getProperty("file.separator"));
			if (index >= (name.length() - 1))
				name = "default";
			else
				name = name.substring(index + 1);
		}
		if (!includesElement(name))
			return name;
		else {
			int i = 2;
			while (includesElement(String.format("%s%d", name, i)))
				i++;
			return String.format("%s%d", name, i);
			}
	}

	public boolean insertElement(iQProjectElement obj) {
		projectProperties.setUnSaved();
		return projectElements.add(obj);
	}

	public boolean removeElement(iQProjectElement obj) {
		projectProperties.setUnSaved();
		RemoveChildrenOf(obj);
		RemoveParentsOf(obj);
		return projectElements.remove(obj);
	}

	public QProjectProperties getProperties() {
		return projectProperties;
	}

	public void setProperties(QProjectProperties prop) {
		projectProperties = prop;
		prop.setUnSaved();
	}

	public void saveProject() throws IOException {
		iQProjectElement next;
		EmptyProgress prog = new EmptyProgress();		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				projectProperties.getFileName())));
		bw.append("<?xml version='1.0' encoding='ISO-8859-2'?>");
		bw.newLine();
		bw.append("<Project ");
		projectProperties.XMLstore(bw);
		bw.append(" >");
		bw.newLine();

		Iterator iter = projectElements.iterator();
		while (iter.hasNext()) {
			next = (iQProjectElement) iter.next();
			String next_name = next.getName();
			String f_name = projectProperties.getCurrentPath() + next_name + "." +  next.getDescriptionFileExtension();
			next.setFileName(f_name);
			if(next.isFileStoreable()) {
				if (next.isXMLstoreable()) {
					//zapisanie danych xml-owych o elementach projektu
					((iQXMLstoreable) next).XMLstore(bw);
					bw.newLine();
				}
				try {
					//zapisanie elementu projektu - jezeli wymaga - do osobnego pliku
					((FileStoreable) next).store(new File(f_name), prog);
				} catch (InterruptedException ex) {
					throw new IOException();
				}
			} else {
				JOptionPane.showMessageDialog(QMainFrame.getMainFrame(), next_name + " does not support saving, will not be saved");
			}
		}
		
		bw.append("</Project>");
		bw.newLine();
		bw.close();
		
		/*zapisanie relacji pomiedzy elementami w projekcie*/
		//TODO pozbyc sie problemu spacji
		String name_relative = projectProperties.getCurrentPath() + "relatives";
		File file_relative = new File(name_relative);
		BufferedWriter writer_relative = new BufferedWriter(new FileWriter(file_relative));
		Iterator it = getRelatives().iterator();
		QPara nexxt;
		while (it.hasNext()) {
			nexxt = (QPara) it.next();	
			writer_relative.append(nexxt.Rod.getName() + " " + nexxt.Dziec.getName());
			writer_relative.newLine();
		}
		writer_relative.close();
		projectProperties.setSaved();
	}

	public void saveProjectAs(String fileName) throws IOException {
		projectProperties.setFileName(fileName);
		saveProject();
	}

	public void loadProject() throws IOException {
		XmlOpener handler = new XmlOpener(this);
		SAXParserFactory fact = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = fact.newSAXParser();
			saxParser.parse(projectProperties.getFileName(), handler);
		} catch (ParserConfigurationException e1) {
			throw new IOException(e1);
		} catch (SAXException e2) {
			throw new IOException(e2);
		}
		loadRelatives();	
	}

	/**
	 * Wczytanie zaleznisci miedzy elementami projektu
	 * @throws IOException
	 */
	private void loadRelatives() throws IOException {
		BufferedReader rs = new BufferedReader(new FileReader(projectProperties.getCurrentPath() + "relatives"));
		String s, Parent_name, Children_name;
		iQProjectElement e1, e2;
		int podzial;
		boolean missing_objs = false;
		while ((s = rs.readLine()) != null) {
			podzial = s.lastIndexOf(" ");
			Parent_name = s.substring(0, podzial);
			Children_name = s.substring(podzial + 1);
			e1 = getElement(Parent_name);
			e2 = getElement(Children_name);
			if ((e1 == null)||(e2 == null)) {
				missing_objs = true;
			}
			else {
				pokrewienstwo.add(new QPara(e1, e2));
			}
		}
		if(missing_objs)
			JOptionPane.showMessageDialog(QMainFrame.getMainFrame(), "Some objects in the project did not support saving and were not saved");
	}

	public boolean isSaved() {
		return projectProperties.isSaved();
	}
	
	public String getCurrentPath() {
		String pom = projectProperties.getFileName();
		return pom.substring(0, pom.lastIndexOf(System.getProperty("file.separator")));
	}

}

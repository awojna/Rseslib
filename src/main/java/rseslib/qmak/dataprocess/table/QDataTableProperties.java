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


package rseslib.qmak.dataprocess.table;

import rseslib.qmak.dataprocess.table.QDataTableProperties;

/**
 * QDataTableProperties class represents properties of a table.
 * 
 * @author Damian Wojcik
 */
public class QDataTableProperties {
	//przy dodawaniu nowych pol prosze pamietac o metodzie clone - Krzysiek

	  /*
	   * File to save table name
	   */
	  private String fileName = null;
	  
	  /*
	   * Table name
	   */
	  private String name = null;

	  /*
	   * State of beeing saved
	   */
	  private boolean saved;

	  public QDataTableProperties(String newName, String newFileName) {
	    fileName = newFileName;
	    name = newName;
	  }

	  public QDataTableProperties(QDataTableProperties j) {
		    fileName = new String(j.getFileName());
		    name = new String(j.getName());
		  }
	  
	  public QDataTableProperties clone() {
		  QDataTableProperties cp = new QDataTableProperties(this);
		  if (this.saved()) cp.set_saved(); else cp.set_unsaved();
		  return cp;
	  }

	  public String getFileName() {
	    return fileName;
	  }

	  public void setFileName(String newName) {
	    fileName = newName;
	  }

	  public String getName() {
	    return name;
	  }

	  public void setName(String newName) {
	    name = newName;
	  }

	  public void assign(QDataTableProperties obj) {
	    fileName = obj.getFileName();
	    name = obj.getName();
	  }

	  public void set_saved() {
	    saved = true;
	  }

	  public void set_unsaved() {
	    saved = false;
	  }

	  public boolean saved() {
	    return saved;
	  }
	  
	  public String toString() {
		  StringBuffer rep = new StringBuffer();
		  rep.append("QDataTableProperties name: " + name + " fileName: " + fileName + " \n");
		  rep.append("Is saved: " + saved + "\n");
		  
		  return rep.toString();
	  }
}

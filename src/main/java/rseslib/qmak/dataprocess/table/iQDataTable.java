/*
 * Copyright (C) 2002 - 2017 Logic Group, Institute of Mathematics, Warsaw University
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

import java.io.IOException;


import javax.swing.table.TableModel;

import rseslib.qmak.dataprocess.project.iQProjectElement;
import rseslib.qmak.dataprocess.project.iQXMLstoreable;
import rseslib.qmak.dataprocess.table.QDataTable;
import rseslib.qmak.dataprocess.table.QDataTableProperties;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.dataprocess.classifier.iQClassifier;
import rseslib.qmak.dataprocess.project.*;
import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.data.formats.DataFormatException;
import rseslib.structure.table.DoubleDataTable;
/**
 * Interface for DataTable.
 * 
 * @author damian and Trickster's authors
 */
public interface iQDataTable extends iQProjectElement, DoubleDataTable, TableModel, iQXMLstoreable {
	  /**
	   * Pobranie obiektu DoubleDataTable zwiazanego z tabela
	   * @return tabela Rseslib
	   */
	  public DoubleDataTable getDataTable();

	  /**
	   * Pobranie wiersza tabeli
	   * @param number numer wiersza
	   * @return wiersz tabeli
	   */
	  public DoubleDataWithDecision getRow(int number);

	  /**
	   * Pobranie wlasciwosci tabeli
	   */
	  public QDataTableProperties getProperties();

	  /**
	   * Czy tabela jest zapisana
	   */
	  public boolean isSaved();
	  
	  /**
	   * Ustawienie tabeli jako zapisanej
	   * @param b
	   */
	  public void setSaved(boolean b);

	  public iQDataTable classify(iQClassifier myclassifier);

	  public iQDataTable modifyAttributes(Header hdr);

	  public QDataTable copy();
	  
	  public void restoreOldTable(iQDataTable old);
	
	  public String NameOfResult(double i);

	  public boolean isBadlyClassified(int row);

	  /**
	   * Czy tabela jest sklasyfikowana
	   */
	  public boolean isClassified();

	  /**
	   * Ustaw tabele jako sklasyfikowana
	   * @param bool
	   */
	  public void setClassified(boolean bool);
	  
	  /**
	   * Zapisz tabele do pliku zwiazanego z tabela
	   * @throws IOException
	   * @throws StoringNotImplementedException
	   */
	  public void save() throws IOException;
	  
	  /**
	   * Wczytaj dane i stan tabeli z pliku
	   * @throws IOException
	   * @throws InterruptedException
	   * @throws DataFormatException
	   */
	  public void load() throws IOException, InterruptedException, DataFormatException;
	  
      public void sort(int col);
}

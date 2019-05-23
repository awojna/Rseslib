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


package rseslib.qmak.util;

import javax.swing.filechooser.*;

import rseslib.qmak.dataprocess.classifier.QClassifier;
import rseslib.qmak.dataprocess.multiclassifier.QMultiClassifier;
import rseslib.qmak.dataprocess.project.QProject;
import rseslib.qmak.dataprocess.results.QMultipleTestResult;
import rseslib.qmak.dataprocess.results.QTestResult;
import rseslib.qmak.dataprocess.table.QDataTable;
import rseslib.qmak.dataprocess.classifier.*;
import rseslib.qmak.dataprocess.multiclassifier.*;
import rseslib.qmak.dataprocess.project.*;
import rseslib.qmak.dataprocess.results.*;
import rseslib.qmak.dataprocess.table.*;

import java.io.File;

/**
 * Funkcje dla programu Qmak
 * 
 * @author damian, Krzysztof Mroczek, Trickster's authors
 * @version 2.0
 */
public class Utils {
  public Utils() {
  }

  /**
   * Rozszerzenie dla pliku xml opisu projektu
   */
  public final static String qpr = "qpr";
  
  /**
   * Rozszerzenie dla pliku stanu klasyfikatora
   */
  //public final static String qcl = "qcl";
  
  /**
   * Rozszerzenie dla pliku xml opisu klasyfikatora
   */
  //public final static String qcld = "qcld";
  
  /**
   * Rozszerzenie dla pliku xml opisu multiklasyfikatora
   */
  public final static String qmcd = "qmcd"; 
  
  /**
   * Rozszerzenie dla pliku stanu i danych tabeli
   */
  public final static String qdt = "qdt";

  /**
   * Pobierz rozszerzenie pliku
   * @param f plik
   * @return rozszerzenie pliku(po kropce)
   */
  public static String getExtension(File f) {
    String ext = "";
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }
    return ext;
  }

  /**
   * Pobierz rozszerzenie z nazwy pliku
   * @param s nazwa pliku(np. sciezka absolutna)
   * @return rozszerzenie pliku(po kropce)
   */
  public static String getExtension(String s) {
	String ext = "";
	int i = s.lastIndexOf('.');
	if (i > 0 && i < s.length() - 1) {
		ext = s.substring(i + 1).toLowerCase();
		}
	return ext;
   }

  /**
   * Filtr plikow projektu ".qpr"
   */
  public static FileFilter getFileFilterQPR() {
    return new FileFilter() {
      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
          if (extension.equals(QProject.extension) || (extension.equals(""))) {
            return true;
          }
        }
        return false;
      }
      //The description of this filter
      public String getDescription() {
        return "Directory name or Qmak project (*." + QProject.extension + ")";
      }
    };
  }
  
  /**
   * Filtr plikow opisu klasyfikatora ".qcld"
   */
  public static FileFilter getFileFilterQCLD() {
	  return new FileFilter() {
	      public boolean accept(File f) {
	        if (f.isDirectory()) {
	          return true;
	        }

	        String extension = Utils.getExtension(f);
	        if (extension != null) {
	          if (extension.equals(QClassifier.description_extension)) {
	            return true;
	          }
	        }
	        return false;
	      }
	      //The description of this filter
	      public String getDescription() {
	        return "Qmak classifier description (*." + QClassifier.description_extension + ")";
	      }
	    };
  }
  
  /**
   * Filtr plikow opisu test resultu
   *
   */
  public static FileFilter getFileFilterQTRD() {
      return new FileFilter() {
	      public boolean accept(File f) {
	          if (f.isDirectory()) {
	              return true;
	              }
		      String extension = Utils.getExtension(f);
		      if (extension != null) {
		          if (extension.equals(QTestResult.description_extension)) {
		              return true;
		              }
		          }
		      return false;
		      }
		  //The description of this filter
		  public String getDescription() {
		      return "Qmak test result description (*." + QTestResult.description_extension + ")";
		      }
		  };	  
  }
  
  /**
   * Filtr plikow opisu multiple test resultu
   *
   */
  public static FileFilter getFileFilterQMTRD() {
      return new FileFilter() {
	      public boolean accept(File f) {
	          if (f.isDirectory()) {
	              return true;
	              }
		      String extension = Utils.getExtension(f);
		      if (extension != null) {
		          if (extension.equals(QMultipleTestResult.description_extension)) {
		              return true;
		              }
		          }
		      return false;
		      }
		  //The description of this filter
		  public String getDescription() {
		      return "Qmak multiple test result description (*." + QMultipleTestResult.description_extension + ")";
		      }
		  };	  
  }
  
  /**
   * Filtr plikow opisu multiklasyfikatora ".qmcd"
   */
  public static FileFilter getFileFilterQMCD() {
      return new FileFilter() {
	      public boolean accept(File f) {
	          if (f.isDirectory()) {
	              return true;
	              }
		      String extension = Utils.getExtension(f);
		      if (extension != null) {
		          if (extension.equals(QMultiClassifier.description_extension)) {
		              return true;
		              }
		          }
		      return false;
		      }
		  //The description of this filter
		  public String getDescription() {
		      return "Qmak multiclassifierr description (*." + QMultiClassifier.description_extension + ")";
		      }
		  };
    }
    
  /**
   * Filtr plikow tabeli ".qdt"
   */
  public static FileFilter getFileFilterQDT() {
      return new FileFilter() {
          public boolean accept(File f) {
              if (f.isDirectory()) {
                  return true;
                  }
              String extension = Utils.getExtension(f);
              if (extension != null) {
                  if (extension.equals(QDataTable.extension)) {
                      return true;
                      }
                  }
              return false;
          }
          //The description of this filter
          public String getDescription() {
              return "Qmak/Trickster data table (*." + QDataTable.extension + ")";
          }
      };
  }
}

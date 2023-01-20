/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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


package rseslib.qmak.dataprocess.classifier;

import java.io.*;
import java.util.*;

import rseslib.qmak.dataprocess.classifier.QClassifierType;

/**
 * Klasa reprezentuje dostepne w programie typy klasyfikatorow.
 * 
 * @author Leszek Tur
 *
 */
public class QClassifierTypes {
	
    public static String pathToConfig = "qmak.config";

    private Set<QClassifierType> classifierTypes = new HashSet<QClassifierType>();

    /**
     * wczytuje z pliku konfiguracyjnego dost�pne klasyfikatory
     *
     */
    
    
    // TODO obs�uga b��d�w
    // TODO nie dodawa� b��dnych typ�w - usuwa� je w bloku catch
    public QClassifierTypes() {
		try {
        	Properties defaultProps = new Properties();
        	FileInputStream in = new FileInputStream(pathToConfig);
        	defaultProps.load(in);
        	in.close();
        	
        	//dodawanie do zbioru nowych typ�w klasyfikator�w wczytanych z pliku
        	int i = 0;
        	for (Object klucz : defaultProps.keySet()) {
        		i++;
        		classifierTypes.add(new QClassifierType("klasyfi"+i,defaultProps.getProperty((String) klucz).replace(File.separatorChar,'.')));	
        		//usun�� koncowe spacje - je�li w pliku s� dodatkowe spacje na ko�cu za nazw� klasyfikatora to przy Class.forName wyskajuje b��d �e nie da si� znale�� klasy
        	}
        	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    }

   
    //czy zawiera juz podany typ klasyfikatorow - porownanie po sciezce do klasyfikatora
    public boolean contains(QClassifierType type){
    	for (QClassifierType typ : classifierTypes) {
    		if (typ.getPathToClass().equals(type.getPathToClass()))
    			return true;
    	}
    	return false;
    }

    //dodawanie tylko nowego typu
    public void add(QClassifierType type) {
    	if (!contains(type))
    		classifierTypes.add(type);
    }
    
    public void remove(QClassifierType type) {    	
    	for (Iterator<QClassifierType> it = classifierTypes.iterator(); it.hasNext(); )
    	        if (   (it.next()).getPathToClass().equals(type.getPathToClass()))
    	            it.remove();
    }
    
    //path - wzgledna sciezka z kropkami
    public void remove(String path) {    	
    	for (Iterator<QClassifierType> it = classifierTypes.iterator(); it.hasNext(); )
    	        if (   (it.next()).getPathToClass().equals(path))
    	            it.remove();
    }
    
    public  Set<QClassifierType> getTypes() {
        return classifierTypes;
    }
    
    //zapisuje typy klasyfikator�w do pliku 
    public void saveClassifierTypesConfiguration(){
    	
  		FileOutputStream out;
  		Properties outProps = new Properties();
  		
		try {
			out = new FileOutputStream(pathToConfig);
			int i = 0;
			for (QClassifierType typ : classifierTypes) {
  	  			i++;
  	  			outProps.setProperty("classifier"+i,typ.getPathToClass());
			}		
			outProps.store(out,null);
			out.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}




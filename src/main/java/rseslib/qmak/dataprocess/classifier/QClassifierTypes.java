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


package rseslib.qmak.dataprocess.classifier;

import java.io.*;
import java.util.*;

/**
 * Klasa reprezentuje dostepne w programie typy klasyfikatorow.
 * 
 * @author Leszek Tur
 *
 */
public class QClassifierTypes {
	
    public static String pathToConfig = "qmak.config";

    private ArrayList<QClassifierType> classifierTypes = new ArrayList<QClassifierType>();

    /**
     * wczytuje z pliku konfiguracyjnego dost�pne klasyfikatory
     *
     */
    
    
    // TODO obs�uga b��d�w
    // TODO nie dodawa� b��dnych typ�w - usuwa� je w bloku catch
    public QClassifierTypes() {
		try {
        	BufferedReader in = new BufferedReader(new FileReader(pathToConfig));
        	
        	//dodawanie do zbioru nowych typ�w klasyfikator�w wczytanych z pliku
        	String line = null;
        	if (in.ready())
        		line = in.readLine();
        	int i = 0;
        	while (line != null) {
        		i++;
        		line = line.trim();
        		classifierTypes.add(new QClassifierType("klasyfi"+i, line));	
        		line = (in.ready() ? in.readLine() : null); 
        	}
        	in.close();
        	
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
    
    public  ArrayList<QClassifierType> getTypes() {
        return classifierTypes;
    }
    
    //zapisuje typy klasyfikator�w do pliku 
    public void saveClassifierTypesConfiguration(){
    	
		try {
	    	BufferedWriter output = new BufferedWriter(new FileWriter(pathToConfig));
			for (QClassifierType typ : classifierTypes) {	
	    		output.write(typ.getPathToClass());
	    		output.newLine();
			}
	    	output.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}




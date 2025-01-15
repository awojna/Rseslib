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


package rseslib.qmak.dataprocess.classifier;

/**
 * Reprezentuje typ klasyfikatora.
 * 
 * @author Leszek Tur
 *
 */
public class QClassifierType{
    public String name;
    public String className;
    public String pathToClass;//juz w formacie z kropkami jako separatorami
    public Class classifierClass;

    //sciezka w formacie z kropkami
    public QClassifierType(String nazwa, String sciezka) {
    	name=nazwa; 
    	className=sciezka.substring(sciezka.lastIndexOf('.')+1);
    	pathToClass=sciezka;
    	try {
			classifierClass = Class.forName(pathToClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }


    public String getPathToClass() {
		return pathToClass;
	}
    
	public String getClassName() {
		return className;
	}

	public Class getClassifierClass() {
		return classifierClass;
	}
	
//do testow
//    public static void main(String[] args) {
//    	@SuppressWarnings("unused")
//		QClassifierTypes typyKlasyfikatorow = new QClassifierTypes();
//    }
    
}

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

package rseslib.processing.classification;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import rseslib.structure.table.DoubleDataTable;
import rseslib.system.progress.Progress;


/**
 * This class represents a factory that creates classifiers.
 * 
 * @author Sebastian Stawicki
 *
 */
public class ClassifierFactory {

	/**
	 * Constructs a classifier of given class using standard constructor form - (Properties, DoubbleDataTable, Progress). 
	 * 
	 * @param classifierClass	Class of a classifier to be created
	 * @param prop				Properties of a classifier to be created. If null, default properties are loaded.
	 * @param trainTable		Training data set.
	 * @param prog				Progress object to report training progress.
	 * @return Trained classifier of class declared in parameter classifierClass.  
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	public static Classifier createClassifier(Class classifierClass, Properties prop, DoubleDataTable trainTable, Progress prog) 
		throws IllegalArgumentException, SecurityException, InstantiationException, 
			IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{		
		Class[] args_t = { Properties.class, DoubleDataTable.class, Progress.class };
		Object[] args = { prop, trainTable, prog };
		return (Classifier) classifierClass.getConstructor(args_t).newInstance(args);
	}
	
}

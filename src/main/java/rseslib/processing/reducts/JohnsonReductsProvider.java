/*
 * Copyright (C) 2010 Dariusz Og�rek
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


package rseslib.processing.reducts;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Vector;

import rseslib.processing.discernibility.DiscernibilityMatrixProvider; 
import rseslib.processing.reducts.GlobalReductsProvider;
import rseslib.structure.attribute.Header;
import rseslib.structure.indiscernibility.Indiscernibility;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/**
 * @author Dariusz Og�rek
 *
 */
public class JohnsonReductsProvider extends Configuration implements GlobalReductsProvider{

	private enum GenerateMethod { AllJohnson, OneJohnson; };

	private static final String s_sGenerate = "Reducts";

	private GenerateMethod m_Generate;
	private DiscernibilityMatrixProvider m_Discernibility;
    private Header m_Header;
    
    public JohnsonReductsProvider(Properties prop, DoubleDataTable table) throws PropertyConfigurationException
    {
        super(prop);
        m_Generate = GenerateMethod.valueOf(getProperty(s_sGenerate));
        m_Discernibility = new DiscernibilityMatrixProvider(getProperties(), table);
        m_Header = table.attributes();
    }
	
	public Collection<BitSet> getReducts() {
		Collection<BitSet> cnf = m_Discernibility.getDiscernibilityMatrix();
		Vector<BitSet> bs = new Vector<BitSet>();
		bs.addAll(cnf);
		Collection<BitSet> collection =null;
		switch (m_Generate) {
		case AllJohnson:
			collection = getAllCountedReducts(bs);
			break;
		case OneJohnson:
			collection = getOneCountedReducts(bs);
			break;
		}
		return collection;
	} 

	/**
	* Count occurrence of attribute in indescernibility matrix.
	*
	* @param discernMatrix 	Indescernibility matrix.
	* @return               One dimension array with counted occurence all attributes from indescirnibility matrix.
	*/
	private int[] getCountingAttributes(Vector<BitSet> discernMatrix){
	  
		int[] i_bsCount= new  int[m_Header.noOfAttr()];
		for (BitSet da : discernMatrix)
	    {
			for (int a=0;a< i_bsCount.length;a++){
				if (da.get(a)) i_bsCount[a]++;
			}	    
	    }		
		return  i_bsCount;
	}
 	/**
	* Generates all possible reducts from indescrnibility matrix. First, the function counts all occurrences of attributes placed in indescernibility matrix, and later, based on gathered data, it counts all possible reducts.
	* All reducts can be generated because in this heuristic, there are sometimes few reducts with maximum occurrence. 
	* @param discern_attrs 		Indiscirnibility matrix.
	* @return                  	All reducts set.
	*/
	private Collection<BitSet> getAllCountedReducts(Vector<BitSet> discern_attrs){
		HashSet<BitSet> reducts = new HashSet<BitSet>(); 

		int[] startCount=getCountingAttributes(discern_attrs);	 
		LinkedList<Integer[]> attributesList = FindFirstAllMaximums(startCount);  

		while (!attributesList.isEmpty()){
			LinkedList<Integer[]> newAttributesList = new LinkedList<Integer[]>();

			Vector<BitSet> copydiscern_attrs = RemoveCheckedObjectsByAttr((Vector<BitSet>) discern_attrs.clone(),attributesList.get(0));
		
			startCount=getCountingAttributes(copydiscern_attrs);

			boolean isNullable=IsNullable(startCount);
			
			if (isNullable){		
				AddingToReducts(attributesList.get(0), reducts);
				attributesList.remove(0);	
				} else{	
				newAttributesList = FindAllMaximums(startCount, attributesList.get(0));	
				attributesList.addAll(newAttributesList); 	
				newAttributesList.clear();				  	
				attributesList.remove(0); 					
			}
		}
		return reducts;
	}
	
 	/**
	* Generates one possible reduct from indescrrnibility matrix. First count of one occurrence of attribute existed in indescernibility matrix and later based on this value it will counting reduct.
	* @param discern_attrs 		Indiscirnibility matrix.
	* @return                  	All reducts set.
	*/
	private Collection<BitSet> getOneCountedReducts(Vector<BitSet> discern_attrs){
		HashSet<BitSet> reducts = new HashSet<BitSet>(); 

		int[] startCount=getCountingAttributes(discern_attrs);	 
		LinkedList<Integer[]> attributesList = FindFirstOneMaximum(startCount); 
		
		while (!attributesList.isEmpty()){
			LinkedList<Integer[]> newAttributesList = new LinkedList<Integer[]>(); 

			Vector<BitSet> copydiscern_attrs = RemoveCheckedObjectsByAttr((Vector<BitSet>) discern_attrs.clone(),attributesList.get(0));
		
			startCount=getCountingAttributes(copydiscern_attrs);

			boolean isNullable=IsNullable(startCount);
			
			if (isNullable){		
				AddingToReducts(attributesList.get(0), reducts);
				attributesList.remove(0);	
				} else{	
				newAttributesList = FindOneMaximum(startCount, attributesList.get(0));				
				attributesList.addAll(newAttributesList); 	
				newAttributesList.clear();				  	
				attributesList.remove(0); 					
			}
		}
		return reducts;
	}
	
	 /**
	* Remove from copy of discernibility matrix all set of atributes which has one of attributes existed in toRemove array.
	*
	* @param copyDiscernMatrix 	Indescirnibility matrix.
	* @param toRemove 			Array with possition attributes selected in previous steps. Tablica atrybut�w o najwiekszej liczbie wyst�pie�.
	* @return                  	A cut collection of indescirnibility matrix which will be returned, in order to count new attribute/s for generated reduct. 
	*/
	private Vector<BitSet> RemoveCheckedObjectsByAttr(Vector<BitSet> copyDiscernMatrix, Integer[] toRemove){
		Vector<BitSet> truncatedDiscernMatrix = (Vector<BitSet>) copyDiscernMatrix.clone(); 
		BitSet attrsToRemove = new BitSet(m_Header.noOfAttr());
		for (BitSet bs : copyDiscernMatrix){
			for (Integer iSet : toRemove) {
				attrsToRemove.set(iSet);
				if (bs.intersects(attrsToRemove)) {
					truncatedDiscernMatrix.remove(bs);	
				break;
				}	
				attrsToRemove.clear();
			}	
		}
		return truncatedDiscernMatrix;		
	}
	
	 
	 /**
* Return first occurrence of attribute/s that has/have maximum counter. 
* This function search maximum of attributes counter and add to return list all atribute possition from header file that have this maximum value.
*
* @param startCount 	Array with count occurence of attributes. 
* @return              First initialization of a list with positioned attributes that have maximum occurence.
*/
	private  LinkedList<Integer[]> FindFirstAllMaximums(int[] startCount)
	{		
		LinkedList<Integer[]> attributesList = new LinkedList<Integer[]>(); 
		int posMaxValue= 0;
		for (int i=0;i<startCount.length;i++){
			if (startCount[posMaxValue]<startCount[i]){
				posMaxValue=i;
				attributesList.clear();				
			}
			if (startCount[posMaxValue]==startCount[i]){
				attributesList.add(new Integer[]{i});
			}
		}	
		return attributesList;		
	}	
	 /**
	* Function search new maximum of counted attribute/s in next reducing steps.
	* @param startCount 		Array with count occurences of each attributes after deleting a set of attributes (which are included in the next parameter), from it.
	* @param firstAttributesList 	Collection of attributes count in previous steps of process.
	* @return              		Set of attributes that will be reduct = Previous set of counted attributes + one/few new counted in this step.
	*/
	private  LinkedList<Integer[]> FindAllMaximums(int[] startCount, Integer[] firstAttributesList)
	{		
		LinkedList<Integer[]> attributesList = new LinkedList<Integer[]>(); 
		int posMaxValue= 0;
		for (int i=0;i<startCount.length;i++){
			if (startCount[posMaxValue]<startCount[i]){
				posMaxValue=i;
				attributesList.clear();	
			}
			if (startCount[posMaxValue]==startCount[i]){
				Integer[] array= new Integer[firstAttributesList.length+1];
				for (int j=0;j<firstAttributesList.length;j++) 
					array[j]=firstAttributesList[j];
				array[firstAttributesList.length]=i;
				attributesList.add(array);				
			}
		}	
		return attributesList;		
	}
	
	/**
	* Return first occurrence of attribute that have maximum counter. 
	* This function search maximum of attributes counter and add to return list all atribute possition from header file that have this maximum value.
	*
	* @param startCount 	Array with counted occurence of atributes. 
	* @return              First initialization of a list with positioned attributes that have maximum occurence.
	*/
	private  LinkedList<Integer[]> FindFirstOneMaximum(int[] startCount)
	{		
		LinkedList<Integer[]> attributesList = new LinkedList<Integer[]>(); 
		int posMaxValue= 0;
		for (int i=0;i<startCount.length;i++){
			if (startCount[posMaxValue]<startCount[i]){
				posMaxValue=i;
				attributesList.clear();				
				attributesList.add(new Integer[]{i});
			}
		}	
		return attributesList;		
	}
	
	private  LinkedList<Integer[]> FindOneMaximum(int[] startCount, Integer[] firstAttributesList)
	{		
		LinkedList<Integer[]> attributesList = new LinkedList<Integer[]>(); 
		int posMaxValue= 0;
		for (int i=0;i<startCount.length;i++){
			if (startCount[posMaxValue]<startCount[i]){
				posMaxValue=i;
				attributesList.clear();				
				Integer[] array= new Integer[firstAttributesList.length+1]; //zwiekszenie aktualnego wektora o 1 !!!
				for (int j=0;j<firstAttributesList.length;j++) 
					array[j]=firstAttributesList[j];
				array[firstAttributesList.length]=i;
				attributesList.add(array);
			}
		}	
		return attributesList;		
	}
	
    /**
	* This function add array of reduced attributes to reducts set.
	*
	* @param integers 	Tablica numer�w atrybut�w w pliku nag�owkowym.
	* @param reducts 	Reducts set that will be return (glabal variable).
	* @return          void.
	*/
	private void AddingToReducts(Integer[] integers, HashSet<BitSet> reducts) {
		BitSet bs = new BitSet();
		for (int i=0;i<integers.length;i++) bs.set(integers[i]);
		reducts.add(bs);
	}

	/**
	* Check if array has one row with non-zero value.
	*
	* @param array 	Array with counted atributes.
	* @return          True if is nullable otherwise false.
	*/
	private boolean IsNullable(int[] array){
		for (int i=0;i<array.length;i++){
			if(array[i]!=0)   return false; 
		}		
		return true;
	}

    public Indiscernibility getIndiscernibilityForMissing()
    {
    	return m_Discernibility.getIndiscernibilityForMissing();
    }
}


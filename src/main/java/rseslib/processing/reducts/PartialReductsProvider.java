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


package rseslib.processing.reducts;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Properties;

import rseslib.structure.attribute.Header;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.Configuration;
import rseslib.system.PropertyConfigurationException;

/** 
 * Constructs local and global partial decision reducts by a greedy algorithm.
 * 
 * @author Marcin Piliszczuk & Beata Zielosko
 */
public class PartialReductsProvider extends Configuration implements GlobalReductsProvider, LocalReductsProvider {
	
	public static final String s_sAlpha = "AlphaForPartialReducts";

	DoubleDataTable TrainTable;
	Header DataHeader;
	double alpha;
    protected int NumberOfSubsets = 0;  //num_vars
    protected int NumberOfRows = 0;
    protected int NumberOfElements = 0; //t.total_diff_rows
    ArrayList<DoubleData> T;
    protected boolean[] PartialCover;
    long ElementsCovered = 0;//card_pcover

    /**
     *  Constructor
     * 
     * @param DoubleDataTable 
     * @param alpha     0 <= alpha < 1      covering precision
     */
    public PartialReductsProvider(Properties prop, DoubleDataTable table) throws PropertyConfigurationException {
    	super(prop);
    	alpha = getDoubleProperty(s_sAlpha);
    	TrainTable = table;
    	DataHeader = table.attributes();
    }
    
    /**
     * Returns global reduct for data table
     */
    public Collection<BitSet> getReducts() {
        NumberOfSubsets = DataHeader.noOfAttr();
        PartialCover = new boolean[NumberOfSubsets];   
        NumberOfRows = TrainTable.noOfObjects();
        T = TrainTable.getDataObjects();
        countPTCardinality();

        int[] card = new int[NumberOfSubsets];
        ElementsCovered = 0;
        long M = (long) Math.ceil((double) NumberOfElements * (1 - alpha));
        while (M > ElementsCovered) {

            card = count_reduct_card();
            short set = findBestSet(card);
            if (card[set] == 0) {
                throw new RuntimeException("greedy algorithm: best set is empty");
            }
            insertAttribute(set, card);
            card = new int[NumberOfSubsets];
        } // while

        minimizeCover(alpha);

        return getBitSetPartialCover();
    };

    /**
     * Return local reduct for data object
     */
    public Collection<BitSet> getSingleObjectReducts(DoubleData object)
    {
        NumberOfSubsets = DataHeader.noOfAttr();
        PartialCover = new boolean[NumberOfSubsets];   
        NumberOfRows = TrainTable.noOfObjects();
        T = TrainTable.getDataObjects();
        int row = 0;
        while(row < T.size() && object != T.get(row))
        	++row;
        if(row == T.size())
            throw new RuntimeException("Object not found in train table while generating local partial reduct");
        T = createU(TrainTable.getDataObjects(), row);
        NumberOfElements = T.size() -1;
        NumberOfRows = T.size();

        int[] card = new int[NumberOfSubsets];
        ElementsCovered = 0;
        long M = (long) Math.ceil((double) NumberOfElements * (1 - alpha));
        while (M > ElementsCovered) {

            card = count_rule_card();
            short set = findBestSet(card);
            if (card[set] == 0) {
                throw new RuntimeException("greedy algorithm: best set is empty");
            }
            insertAttribute(set, card);
            card = new int[NumberOfSubsets];
        } // while

        minimizeCover(alpha);

        return getBitSetPartialCover();
    }

    /**
     * Returns  partial cover in form of boolean array 
     * @return
     */
    public boolean[] getPartialCover(){
        return PartialCover;
    }
    
/**
 * Creates set U containing rows from table t which have different decisions and different description from row r 
 * @param t     decision table
 * @param r     row for which we want to make a decision rule
 * @return      Set U containing rows different from r
 */
    protected ArrayList<DoubleData> createU(ArrayList<DoubleData> t, int r) {
        ArrayList<DoubleData> tmp = new ArrayList<DoubleData>();
        tmp.add(t.get(r));

        for (int i = 0; i < NumberOfRows; i++) {
            if (differentDecisions(r, i)) {
                if (differentRows(r, i)) {
                    tmp.add(t.get(i));
                /*
                for(int j=0; j<num_vars; j++)
                tmp.w[j]=w[j];
                 */
                }
            }
        }
        return tmp;
    }
    
    /**  Computes number of pairs of different rows with different decisions from decision table
     *    |P(T)|   
     * @return
     */
    protected void countPTCardinality() { //count_diff_rows()
        NumberOfElements = 0;
        for (int i = 0; i < NumberOfRows - 1; i++) {
            for (int j = i + 1; j < NumberOfRows; j++) {
                if (differentDecisions(i, j) && differentRows(i, j)) {
                    NumberOfElements++;
                //if((tab[num_vars][i]!=tab[num_vars][j]) && (differentRows(i,j)==1)) NumberOfElements++;         
                }
            }
        }
    }
  
    protected boolean differentDecisions(int i, int j) {
        if (((DoubleDataWithDecision) T.get(i)).getDecision() != ((DoubleDataWithDecision) T.get(j)).getDecision()) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean pairCovered(int i, int j, int atr) {  //pairSeparatedbyAttr
        if (T.get(i).get(atr) != T.get(j).get(atr) /*exclude missings&& t.tab[c][i] >= 0 && t.tab[c][j] >= 0*/) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean pairCovered(int i, int j) {
        boolean result = false;
        for (int c = 0; c < NumberOfSubsets; c++) {
            if (PartialCover[c] == true && pairCovered(i, j, c)) {
                result = true;
                break;
            }
        }
        return result;
    }

    protected boolean differentRows(int i, int j) {
        boolean result = false;
        for (int c = 0; c < NumberOfSubsets; c++) {
            if (DataHeader.isConditional(c) && pairCovered(i, j, c)) {
                result = true;
                break;
            }
        }
        return result;
    }

    protected void add_card(int[] c1, int[] c2) {
        for (int i = 0; i < c1.length; i++) {
            c1[i] += c2[i];
        }
    }

    protected synchronized int[] count_local_card(int i) {
        int[] card = new int[NumberOfSubsets];
        for (int j = i + 1; j < T.size(); j++) {
            if (differentDecisions(i, j) && !pairCovered(i, j) && differentRows(i, j)) {
                for (int c = 0; c < NumberOfSubsets; c++) {
                    if (PartialCover[c] == false && DataHeader.isConditional(c)) {
                        if (pairCovered(i, j, c)) {
                            card[c]++;
                        }
                    }
                }
            }
        }
        return card;
    }

    protected int[] count_rule_card() {
        return count_local_card(0);
    }

    /**
     * 
     * @return
     */
    protected int[] count_reduct_card() {
        int[] card = new int[NumberOfSubsets];
        for (int i = 0; i < NumberOfRows - 1; i++) {
            int[] c = count_local_card(i);
            add_card(card, c);
        }
        return card;
    }

    /**
     * Finds set which covers maximal number of elements
     * @param card      array of numbers of elements covered by each set
     * @return
     */
    protected short findBestSet(int[] card) {
        short set = 0;
        for (short c = 1; c < NumberOfSubsets; c++) {
            if (card[set] < card[c]) {
                set = c;
            }
        }
        return (set);
    }
    
    /**
     * Adds set to partial cover
     * @param set       choosen set 
     * @param card      array of numbers of elements covered by each set
     */
    protected void insertAttribute(short set, int[] card) {
        PartialCover[set] = true;
        ElementsCovered += card[set];
//    gamma = t.total_diff_rows - card_pcover;
//    gamma /=(double) t.total_diff_rows;
    }

    /**
     * Transforms alpha-cover to irreducible alpha-cover
     * @param alpha     covering precision
     */
    protected void minimizeCover(double alpha) {
        for (int i = 0; i < NumberOfSubsets; i++) {
            if (PartialCover[i] == true) {
                PartialCover[i] = false;
                if (!alphaCover(alpha)) {
                    PartialCover[i] = true;
                }
            }
        }
    }

    /**
     * Method checks if we have an alpha-cover
     * @param alpha     covering precision  
     * @return          true when we cover no less than (1-alpha)|A| elements (pairs of rows)
     */
    protected boolean alphaCover(double alpha) {
        boolean result = true;
        //    System.out.println("alpha = " + alpha);
        //    System.out.println("tdr = " + t.total_diff_rows);
        //    System.out.println("num unsep ok = " + Math.floor(alpha*t.total_diff_rows));
        long nuo = (long) Math.floor(alpha * NumberOfElements);
        long curr_unsep = 0;

        for (int i = 0; i < NumberOfRows - 1; i++) {
            //System.out.println("partialSetCoverProvider::alphaCover i = " + i);
            for (int j = i + 1; j < NumberOfRows; j++) {
                //System.out.println("partialSetCoverProvider::alphaCover j = " + j);
                if (differentDecisions(j, i)) 
                    if (differentRows(j, i)) {
                        //check if the reduct separates a pair
                        //int atr = 0;
                        int separated = 0;
                        for (int k = 0; k < NumberOfSubsets; k++) 
                            if (PartialCover[k] == true) 
                                if (pairCovered(i,j,k))/*(t.tab[atr][i] != t.tab[atr][j])*/ {
                                    separated = 1;
                                    break;
                                }                        
                            //atr++;                    
                        if (separated == 0) curr_unsep++;                    
                        if (curr_unsep > nuo) return false;                    
                    }            
            } 
        }   
        return result;
    }

    /**
     * Returns reduct as Collection<BitSet>
     */
    protected Collection<BitSet> getBitSetPartialCover()
    {
        Collection<BitSet> reducts = new ArrayList<BitSet>();
        
        BitSet b = new BitSet(PartialCover.length);
        for(int i = 0; i < PartialCover.length; i++)
            if (PartialCover[i] == true) b.set(i);
        
        reducts.add(b);
        return reducts;    	
    }    
}

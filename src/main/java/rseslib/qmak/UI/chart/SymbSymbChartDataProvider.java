/*
 * Copyright (C) 2002 - 2022 The Rseslib Contributors
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


package rseslib.qmak.UI.chart;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;

/**
 * SymbSymbChartDataProvider zajmuje sie przetwarzaniem danych
 * na potrzeby wykresu typu symboliczne*symboliczne
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class SymbSymbChartDataProvider {

    /** etykieta wartosci brakujacych na wykresach */
    static private final String MISSING_TEXT = "missing value";
    
    /**
     * Przetwarza dane do formatu z ktorego mozna stworzyc wykres
     * symb*symb
     * 
     * @param data zbior obiektow danych do przetworzenia na potrzeby wykresu
     * @param paramX numer atrybutu ze zbioru data, ktory bedzie na osi X
     * @param paramY numer atrybutu ze zbioru data, ktory bedzie na osi Y
     * @return zbior danych typu CategoryDataset potrzebnych do 
     * wygenerowania wykresu
     */
    public static CategoryDataset generateData(DoubleDataTable data, 
            int paramX, int paramY)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Header header = data.attributes();
        NominalAttribute atrX = (NominalAttribute) header.attribute(paramX);
        NominalAttribute atrY = (NominalAttribute) header.attribute(paramY);
        
        /* zsumowanie wystapien wszystkich wartosci */
        int sum[][]= new int[atrX.noOfValues() + 1][atrY.noOfValues() + 1];
      
        boolean missingOnX = false;
        boolean missingOnY = false;
		for (DoubleData row : data.getDataObjects())
        {
            double gX = row.get(paramX);
            double gY = row.get(paramY);
            if (Double.isNaN(gX)) {
                missingOnX = true;
                if (Double.isNaN(gY)) {
                    missingOnY = true;
                    sum[atrX.noOfValues()][atrY.noOfValues()]++;
                }
                else
                    sum[atrX.noOfValues()][atrY.localValueCode(gY)]++;
            }
            else {
                int valX = atrX.localValueCode(gX);
                int valY = atrY.localValueCode(gY);
                if (Double.isNaN(gY)) {
                    missingOnY = true;
                    sum[valX][atrY.noOfValues()]++;
                }
                else
                    sum[valX][valY]++;
            }
           
           
      
        }
        
        /* dodanie par do dataset */
        for (int i = 0; i < atrX.noOfValues(); i++)
            for (int j = 0; j < atrY.noOfValues(); j++)
            {
                dataset.addValue(sum[i][j],NominalAttribute.stringValue(atrY.globalValueCode(j)),
                        NominalAttribute.stringValue(atrX.globalValueCode(i)));
            }
        /* dodanie par z wartosciami missing_value, (gdy takie wystapily) */
        if (missingOnY) {
            int j = atrY.noOfValues();
            for (int i = 0; i < atrX.noOfValues(); i++)
                dataset.addValue(sum[i][j],MISSING_TEXT,
                        NominalAttribute.stringValue(atrX.globalValueCode(i)));
        }
        if (missingOnX) {
            for (int j = 0; j < atrY.noOfValues(); j++)
                dataset.addValue(sum[atrX.noOfValues()][j],NominalAttribute.stringValue(atrY.globalValueCode(j)),
                        MISSING_TEXT);
            if (missingOnY)
                dataset.addValue(sum[atrX.noOfValues()][atrY.noOfValues()],MISSING_TEXT,
                        MISSING_TEXT);
                
        }
        return dataset;
    }
    
    /**
     * Na podstawie zbioru danych z udzialem ilosciowym poszczegolnych 
     * wartosci, tworzy nowy zbior danych, pokazujacy procentowy 
     * udzial poszczegolnych wartosci
     * 
     * @param dataset zbior danych do przetworzenia
     * @return zbior danych do wykresu z rozkladem procentowym
     */
    public static CategoryDataset convertData(CategoryDataset dataset)
    {
        DefaultCategoryDataset newDataset = new DefaultCategoryDataset();
        int n_cols = dataset.getColumnCount();
        int n_rows = dataset.getRowCount();
        double all[] = new double[n_cols];
        for (int i = 0; i < n_cols; i++)
            all[i] = 0;
        for (int i = 0; i < n_rows; i++)
            for (int j = 0; j < n_cols; j++)
                all[j] += dataset.getValue(i,j).doubleValue();
        
     
        for (int i = 0; i < n_rows; i++)
            for (int j = 0; j < n_cols; j++)
            {
                newDataset.addValue(((dataset.getValue(i,j).doubleValue())/all[j])*100,
                        dataset.getRowKey(i), dataset.getColumnKey(j));
            }
        return newDataset;
    }
}

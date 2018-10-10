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


package rseslib.qmak.UI.chart;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYSeries;

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;


/**
 * NumSymbChartDataProvider zajmuje sie przetwarzaniem danych
 * na potrzeby wykresu typu numeryczne*symboliczne
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class SymbNumChartDataProvider {
    
    /** etykieta wartosci brakujacych na wykresach */
    static private final String MISSING_TEXT = "missing value";
    
    /** ilosc punktow w ktorych jest obliczana wartosc funkcji */
    private static final int MAX_POINTS = 100; 
    
    /**
     * Przetwarza dane do formatu, z ktorego mozna po 
     * otrzymaniu dokladnosci z jaka ma byc tworzony wykres
     * w prosty sposob uzyskac dane potrzebne do wykresu num*symb 
     * 
     * @param data zbior obiektow danych do przetworzenia na potrzeby wykresu
     * @param paramX numer atrybutu numerycznego ze zbioru data, ktory bedzie na osi X
     * @param paramY numer atrybutu symbolicznego ze zbioru data, ktory bedzie na osi Y
     * @return przetworzony zbior danych typu TreeMap
     */
    
    public static TreeMap prepareData(DoubleDataTable data, 
            int paramX, int paramY)
    {
        
        Header header = data.attributes();
        NominalAttribute atrY = (NominalAttribute) header.attribute(paramY);
        
        int noOfVal = atrY.noOfValues() + 1; /*ilosc wartosci atrybutow + 1 na missing_value */
        TreeMap values = new TreeMap();
        for (DoubleData row : data.getDataObjects())
        {
            double gX = row.get(paramX);
            double gY = row.get(paramY);
            
            if (!Double.isNaN(gX)) {
                // zliczam numeryczne tylko gdy gX != missing_value
                Double valX = new Double(gX);
                int valY;
                if (Double.isNaN(gY))
                    valY = atrY.noOfValues();
                else
                    valY = atrY.localValueCode(gY);
                double curVal[] = (double[]) values.get(valX);
               
                if (curVal == null) {
                    curVal = new double[noOfVal];
                    for (int i = 0; i < noOfVal; i++)
                        curVal[i] = 0;
                }
                curVal[valY]++;
                values.put(valX, curVal);
            }
        }
        
        return values;
    }
    
    /**
     * Przetwarza dane do formatu, z ktorego mozna stworzyc wykres
     * num*symb. Bierze pod uwage przekazana dokladnosc 
     * 
     * @param values uporzadkowana kolekcja wektorow wartosci
     * @param atrX atrybut numeryczny, ktory bedzie 
     * reprezentowany na osi X
     * @param atrY atrybut symboliczny, ktory bedzie reprezentowany
     * na osi Y
     * @param accuracy dokladnosc wykresu
     * @return zbior danych typu TableXYDataset, potrzebny 
     * do wygenerowania wykresu
     */
    public static TableXYDataset generateData(
            TreeMap values, NumericAttribute atrX, 
            NominalAttribute atrY, double accuracy)
    {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        int noOfVal = atrY.noOfValues() + 1; /*ilosc wartosci atrybutow + 1 na missing_value */
        int realNoOfVal; /* rzeczywista ilosc wartosci atrybutu na wykresie
        					uzalezniona od tego czy wystapily missing_value */
        
        /* obliczenie skrajnych punktow */
        double minX = ((Double) values.firstKey()).doubleValue();
        double maxX = ((Double) values.lastKey()).doubleValue();
        double d = (maxX - minX)/MAX_POINTS;
       
        
        /* sprawdzenie czy w zbiorze wystepuja missing_value */
        boolean missingOnY = false;
        Iterator iter = values.keySet().iterator();
        while (iter.hasNext())
        {
            Double no = (Double) iter.next();
            double curVal[] = (double[]) values.get(no);
            if (curVal[noOfVal - 1] > 0) {
                missingOnY = true;
                break;
            }
        }
        if (!missingOnY)
            realNoOfVal = noOfVal - 1;
        else
            realNoOfVal = noOfVal;
        
        /* inicjacja lokalnych zmiennych */
        double sum[] = new double[realNoOfVal];
        int count = 0;
        for (int i = 0; i < realNoOfVal; i++) {
            sum[i] = 0;
        }
        
        /* stworzenie wszystkich serii */
        XYSeries series[] = new XYSeries[realNoOfVal];
        for (int i = 0; i < realNoOfVal; i++)
            if (i < noOfVal - 1)
                series[i] = new XYSeries(NominalAttribute.stringValue(atrY.globalValueCode(i)),
                    	false, false);
            else
                series[i] = new XYSeries(MISSING_TEXT, false, false);
        
        Set keys = values.keySet();
        Iterator iter1 = keys.iterator(), iter2 = keys.iterator();
        double i = minX; /* aktualnie przetwarzany klucz */
        Double p = (Double) iter1.next();
        Double k = (Double) iter2.next();
        
        /* przejscie po wszystkich wystepujacych elementach w 
         * zbiorze keys i 
         * na ich podstawie ustalenie wartosci dla MAX_POINTS
         * argumentow (biorac pod uwage dokladnosc wykresu 
         */
        Double prev = p;
        boolean endList = false;
        while (i < maxX) 
        {
          
            /* odjecie poprzednich elementow (nie mieszczacych sie juz
             * w przedziale) */
            while (p.doubleValue() < (i - accuracy)) {
                double curVal[] = (double[]) values.get(p);
                for (int j = 0; j < realNoOfVal; j++)
                    sum[j] -= curVal[j];
                count--;
                prev = p;
                p = (Double) iter1.next();
               
            }
            
            /* odjecie kolejnych elementow (mieszczacych sie juz
             * w przedziale) */
            while ((!endList) && (k.doubleValue() <= (i + accuracy))) 
            {
                double curVal[] = (double[]) values.get(k);
                for (int j = 0; j < realNoOfVal; j++) {
                    sum[j] += curVal[j];
                   
                }
                count++;
                if (iter2.hasNext())
                    k = (Double) iter2.next();
                else
                    endList = true;
            
           	}
            /* dodanie wartosci do poszczegolnych serii,
             * reprezentujacych okreslone wartosci atrybutu */
            if (count == 0) {
                /* zaden rzeczywisty element nie zalapal sie do tego
                 * przedzialu, wartosci sa liczone na podstawie
                 * funkcji liniowej (stworzonej na podstawie 
                 * najblizszego punktu na lewo i na prawo)
                 * 
                 */
                
                double prevVal[] = (double[]) values.get(prev);
                double nextVal[] = (double[]) values.get(k);
                for (int j = 0; j < realNoOfVal; j++) {
                    double a = (nextVal[j] - prevVal[j])/
                    	(k.doubleValue() - prev.doubleValue());
                    double b = prevVal[j] - (a * prev.doubleValue());
                
                    series[j].add(i, a * i + b);
                }
                
            }
            else {
                /* przynajmniej jeden punkt zmiescil sie w przedziale
                 * wartosciami w tym punkcie bedzie wyliczony wektor sum
                 */
                for (int j = 0; j < realNoOfVal; j++) {
                    series[j].add(i, sum[j]/count);
                }
            }
       
            i += d;
        }
        
        for (int j = 0; j < realNoOfVal; j++)
            dataset.addSeries(series[j]);
    
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
   public static TableXYDataset convertData(TableXYDataset dataset)
    {
    
        DefaultTableXYDataset newDataset = new DefaultTableXYDataset();
        int n_cols = dataset.getItemCount();
        int n_rows = dataset.getSeriesCount();       
        double all[] = new double[n_cols];
 
        for (int i = 0; i < n_cols; i++)
            all[i] = 0;
        
        for (int i = 0; i < n_rows; i++)
            for (int j = 0; j < n_cols; j++)
                all[j] += dataset.getYValue(i,j);
        
        
        XYSeries newSeries[] = new XYSeries[n_rows];
        for (int i = 0; i < n_rows; i++)
            newSeries[i] = new XYSeries(dataset.getSeriesName(i),
                    	false, false);
     
        for (int i = 0; i < n_rows; i++)
            for (int j = 0; j < n_cols; j++) 
            {
                newSeries[i].add(dataset.getXValue(i,j), 
                        (dataset.getYValue(i,j)/all[j])*100);
            }
        
        for (int i = 0; i < n_rows; i++)
            newDataset.addSeries(newSeries[i]);
            
        return newDataset;
    }
}

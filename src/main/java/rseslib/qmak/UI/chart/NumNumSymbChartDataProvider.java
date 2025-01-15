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


package rseslib.qmak.UI.chart;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;

/**
 * NumNumSymbChartDataProvider zajmuje sie przetwarzaniem danych
 * na potrzeby wykresu typu numeryczne*numeryczne*symboliczne
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class NumNumSymbChartDataProvider {

    /** etykieta wartosci brakujacych na wykresach */
    static private final String MISSING_TEXT = "missing value";
    
    /**
     * Przetwarza dane do formatu z ktorego mozna stworzyc wykres 
     * num*num*symb
     * 
     * @param data zbior obiektow danych do przetworzenia na potrzeby wykresu
     * @param paramX numer atrybutu ze zbioru data, ktory bedzie na osi X
     * @param paramY numer atrybutu ze zbioru data, ktory bedzie na osi Y
     * @param paramV numer atrybutu ze zbioru data, ktorego rozklad badamy
     * @return zbior danych typu XYDataset, potrzebny 
     * do wygenerowania wykresu
     */
    public static XYDataset generateData(DoubleDataTable data, 
            int paramX, int paramY, int paramV)
    {
        XYSeriesCollection dataset = new XYSeriesCollection ();
        Header header = data.attributes();
        NominalAttribute atrV = (NominalAttribute) header.attribute(paramV);
        
        XYSeries series[] = new XYSeries[atrV.noOfValues() + 1];
        for (int i = 0; i < atrV.noOfValues(); i++)
            series[i] = new XYSeries(NominalAttribute.stringValue(atrV.globalValueCode(i)),
                    	false, true);
        series[atrV.noOfValues()] = new XYSeries(MISSING_TEXT, false, true);
        
        boolean missingOnV = false;
		for (DoubleData row : data.getDataObjects())
        {
            double gV = row.get(paramV);
            double gX = row.get(paramX);
            double gY = row.get(paramY);
            if (!Double.isNaN(gX) && !Double.isNaN(gY)) 
            {
                if (Double.isNaN(gV)) {
                    missingOnV = true;
                    series[atrV.noOfValues()].
                		add(gX, gY);
                }
                else
                    series[atrV.localValueCode(gV)].
            			add(gX, gY);
            }
        }
        
        for (int i = 0; i < atrV.noOfValues(); i++)
            dataset.addSeries(series[i]);
        if (missingOnV)
            dataset.addSeries(series[atrV.noOfValues()]);
        
        return dataset;
        
    }
}

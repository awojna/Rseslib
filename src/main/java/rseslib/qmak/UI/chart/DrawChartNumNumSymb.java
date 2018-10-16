/*
 * Copyright (C) 2002 - 2018 The Rseslib Contributors
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

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;

/**
 * DrawChartNumNumSymb tworzy i rysuje diagram typu
 * numeryczne * numeryczne * symboliczne
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class DrawChartNumNumSymb extends JPanel {
    
    /** wyswietlany wykres */
    private JFreeChart chart;
    
    /** panel zawierajacy wykres */
    private ChartPanel panel;
    
    /**
     * Konstruktor tworzy diagram num*num*symb, na podstawie
     * zbioru danych XYDataset o zadanym tytule i nazwach osi
     * 
     * @param dataset dane na podstawie ktorych tworzony jest wykres
     * @param title tytul wykresu
     * @param xAxisLabel nazwa osi X
     * @param yAxisLabel nazwa osi Y
     */
    public DrawChartNumNumSymb(XYDataset dataset, String title, 
            String xAxisLabel, String yAxisLabel)
    {
        super(new BorderLayout());
        chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, 
            	dataset, PlotOrientation.VERTICAL, true, true, false);
        panel = new ChartPanel(chart);
        this.add(panel, BorderLayout.CENTER);
    }
    
    /**
     * Wyswietla diagram trzymany pod zmienna chart
     */
    public void draw()
    {
        this.updateUI();
        
    }
    
    /**
     * Zwraca panel zawierajacy aktualnie wyswietlany wykres
     * 
     * @return aktualnie widoczny panel z wykresem
     */
    public ChartPanel getCurrentChartPanel()
    {
        return panel;
    }
    
}

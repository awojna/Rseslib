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


import java.awt.CardLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import rseslib.qmak.UI.chart.SymbSymbChartDataProvider;

/**
 * DrawChartSymbSymb tworzy i rysuje diagram typu
 * symboliczne * symboliczne
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class DrawChartSymbSymb extends JPanel 
{
    /** nazwa wykresu procentowego*/
    private static final String NORMAL_GRAPH_TEXT = "Percentage chart";
    /** nazwa wykresu ilosciowego */
    private static final String PERCENT_GRAPH_TEXT = "Quantity chart";

    
    /** wykres ilosciowy */
    private JFreeChart chart;
    /** Wykres procentowy */
    private JFreeChart chartPercent;
    
    /** panel opakowujacy wykres ilosciowy */
    private ChartPanel panel;
    /** panel opakowujacy wykres procentowy */
    private ChartPanel panelPercent;
    /** panel aktualnie wyswietlanego wykresu */ 
    private ChartPanel currentChart;
   
    
    
    /**
     * Konstruktor tworzy diagram symb*symb, na podstawie
     * zbioru danych CategoryDataset o zadanym tytule i nazwach osi
     * 
     * @param dataset dane na podstawie ktorych tworzony jest wykres
     * @param title tytul wykresu
     * @param xAxisLabel nazwa osi X
     * @param yAxisLabel nazwa osi Y
     */
    public DrawChartSymbSymb(CategoryDataset dataset, String title, 
            String xAxisLabel, String yAxisLabel)
    {
        /* narysowanie wykresow */
        chart = ChartFactory.createStackedBarChart(title,
                xAxisLabel, yAxisLabel, dataset, 
    	        PlotOrientation.VERTICAL, true, true, false);
        CategoryDataset datasetPercent = 
            SymbSymbChartDataProvider.convertData(dataset);
        chartPercent = ChartFactory.createStackedBarChart(title,
                xAxisLabel, yAxisLabel, datasetPercent, 
    	        PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = (CategoryPlot) chartPercent.getPlot();
        plot.getRangeAxis().setUpperBound(101);
        
        /* stworzenie paneli i wyswietlenie wykresu ilosciowego */
        panel = new ChartPanel(chart);
        panelPercent = new ChartPanel(chartPercent);
        CardLayout crd = new CardLayout();
        this.add(panel);
        this.add(panelPercent);
        this.setLayout(crd);
        crd.addLayoutComponent(panel, NORMAL_GRAPH_TEXT);
        crd.addLayoutComponent(panelPercent, PERCENT_GRAPH_TEXT);
        crd.show(this, NORMAL_GRAPH_TEXT);
        currentChart = panel;
    }
   
    /**
     * Wyswietla diagram procentowy
     */
    public void drawPercent()
    {
        ((CardLayout) this.getLayout()).show(this, PERCENT_GRAPH_TEXT);
        currentChart = panelPercent;
        
        this.updateUI();
    }
    
    /**
     * Wyswietla diagram ilosciowy
     */
    public void draw()
    {
        ((CardLayout) this.getLayout()).show(this, NORMAL_GRAPH_TEXT);
        currentChart = panel;        
        
        this.updateUI();
    }
   
    
    /**
     * Zwraca panel zawierajacy aktualnie wyswietlany wykres
     * 
     * @return aktualnie widoczny panel z wykresem
     */
    public ChartPanel getCurrentChartPanel()
    {
        return currentChart;
    }
    
}

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


import java.awt.CardLayout;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.TableXYDataset;

import rseslib.qmak.UI.chart.SymbNumChartDataProvider;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;

/**
 * DrawChartSymbNum tworzy i rysuje diagram typu
 * symboliczne * numeryczne
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class DrawChartSymbNum extends JPanel {

    /** nazwa wykresu procentowego*/
    private static final String NORMAL_GRAPH_TEXT = "Percentage chart";
    /** nazwa wykresu ilosciowego */
    private static final String PERCENT_GRAPH_TEXT = "Quantity chart";
    
    /** ogolne dane niezale�ne od accuracy */ 
    TreeMap chartData;
    /** atrybut na osi X */
    NumericAttribute atrX;
    /** atrybut na osi Y */
    NominalAttribute atrY;
    /** tytul wykresu */
    String chartTitle;
    /** accuracy, dla ktorego s� narysowane aktualne wykresy */
    private double acc;
    
    /** wykres ilo�ciowy */
    private JFreeChart chart;
    /** Wykres procentowy */
    private JFreeChart chartPercent;
    
    /** panel opakowuj�cy wykres ilo�ciowy */
    private ChartPanel panel;
    /** panel opakowuj�cu wykres procentowy */
    private ChartPanel panelPercent;
    /** panel aktualnie wy�wietlanego wykresu */ 
    private ChartPanel currentChart;
    
    
    /**
     * Konstruktor tworzy diagram num*symb, na podstawie
     * podanych danych w postaci TreeMap. Diagram b�dzie tworzony
     * wedlug podanej dokladnosci accuracy i dla zadanych atrybutow 
     * na poszczegolnych osiach. Domy�lnie zostanie wy�wietlony wykres 
     * ilo�ciowy 
     * 
     * @param data dane na podstawie ktorych tworzony jest wykres
     * @param aX atrybut numeryczny, ktorego warto�ci b�d� reprezentowane 
     * na osi X
     * @param aY atrybut symboliczny, ktorego warto�ci b�d� reprezentowane 
     * na osi Y
     * @param title tytul wykresu
     * @param accuracy poczatkowa dokladno�c wykresow
     */
    public DrawChartSymbNum(TreeMap data, NumericAttribute aX,
            NominalAttribute aY, String title, double accuracy)
    {
        chartData = data;
        chartTitle = title;
        atrX = aX;
        atrY = aY;
        
        createCharts(accuracy);
        
        /* stworzenie paneli i wy�wietlenie wykresu ilo�ciowego */
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
     * Najpierw konwertuje wedlug podanej dokladno�ci dane do 
     * postaci, ktora jest potrzebna do stworzenia wykresu 
     * typu num*symb czyli TableXYDataset, a nast�pnie na ich podstawie
     * tworzy wykresy (ilo�ciowy i procentowy)
     * 
     * @param accuracy
     */
    private void createCharts(double accuracy)
    {
        acc = accuracy;
        
        /* przetworzenie danych */
        TableXYDataset dataset = 
            SymbNumChartDataProvider.generateData(chartData, 
                    atrX, atrY, acc);
        TableXYDataset percentDataset =
            SymbNumChartDataProvider.convertData(dataset);
        
        /* narysowanie wykresow */
        chart = ChartFactory.createStackedXYAreaChart(chartTitle, 
                atrX.name(), atrY.name(), 
            	dataset, PlotOrientation.VERTICAL, true, true, false);
        chartPercent = ChartFactory.createStackedXYAreaChart(chartTitle, 
                atrX.name(), atrY.name(), 
            	percentDataset, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot) chartPercent.getPlot();
        plot.getRangeAxis().setUpperBound(101);
        
    }
    
    /**
     * Zmiana dokladno�ci wykresow, co skutkuje ponownym 
     * przetworzeniem danych i przerysowaniem obu wykresow
     * 
     * @param accuracy
     */
    public void changeAccuracy(double accuracy)
    {
        createCharts(accuracy);
       
        panel.setChart(chart);
        panelPercent.setChart(chartPercent);
        
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
     * Wyswietla diagram ilo�ciowy
     */
    public void draw()
    {
        ((CardLayout) this.getLayout()).show(this, NORMAL_GRAPH_TEXT);
        currentChart = panel;
        
        this.updateUI();
        
    }
    
    /**
     * Zwraca panel zawieraj�cy aktualnie wy�wietlany wykres
     * 
     * @return aktualnie widoczny panel z wykresem
     */
    public ChartPanel getCurrentChartPanel()
    {
        return currentChart;
    }
}

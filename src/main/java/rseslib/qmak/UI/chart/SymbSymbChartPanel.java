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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.category.CategoryDataset;

import rseslib.qmak.UI.chart.DrawChartSymbSymb;
import rseslib.qmak.UI.chart.SymbSymbChartDataProvider;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.table.DoubleDataTable;


/**
 * Panel wyswietlajacy wykres typu symboliczne*symboliczne.
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class SymbSymbChartPanel extends JPanel 
	implements ActionListener{
    
    /** wewnetrzny panel z wyswietlanym wykresem */
    DrawChartSymbSymb chartPanel;
    /** przycisk do wyboru rodzaju wykresu (procentowy czy ilosciowy)*/
    JButton typeButton;
    /** przycisk do drukowania wykresu */
    JButton printButton;
    /** nazwa przycisku do zmiany wykresu na procentowy */
    static private final String CHANGE_TO_PERCENT_TEXT = "Change to percentage";
    /** nazwa przycisku do zmiany wykresu na ilosciowy */
    static private final String CHANGE_TO_NORMAL_TEXT = "Change to quantity";
    /** nazwa przycisku do drukowania wykresu */
    static private final String PRINT_TEXT = "Print";
    
    
    /** Wydrukowanie wykresu */
    private void printChart()
    {
        ChartPanel chartP = chartPanel.getCurrentChartPanel();
        chartP.createChartPrintJob();
       
    }
    
    /**
     * Konstruktor tworzy panel zawierajacy wykres symb*symb 
     * 
     * @param dataTable surowe dane, na podstawie ktorych bedzie stworzony wykres
     * @param iatr1 numer atrybutu w naglowku dataTable, ktory bedzie na osi X
     * @param iatr2 numer atrybutu w naglowku dataTable, ktory bedzie na osi Y
     */
    public SymbSymbChartPanel(DoubleDataTable dataTable, int iatr1, 
            int iatr2) 
    {
        super(new BorderLayout());
        
        Header header = dataTable.attributes();
        NominalAttribute natr1 = (NominalAttribute) header.attribute(iatr1);
        NominalAttribute natr2 = (NominalAttribute) header.attribute(iatr2);
       
        
        CategoryDataset dataset = SymbSymbChartDataProvider.generateData(dataTable,iatr2,iatr1);
        chartPanel = new DrawChartSymbSymb(dataset,"Chart symb*symb",
                natr2.name(),natr1.name());
        
        /* komponenty do zmiany rodzaju wykresu */
        typeButton = new JButton(CHANGE_TO_PERCENT_TEXT);
        typeButton.setActionCommand("percentage");
        typeButton.addActionListener(this);
        typeButton.setMaximumSize(new Dimension(250,20));
        JPanel typePanel = new JPanel();
        typePanel.add(typeButton);
        
        /* komponenty do drukowania */
        printButton = new JButton(PRINT_TEXT);
        printButton.setActionCommand("print");
        printButton.addActionListener(this);
        printButton.setMaximumSize(new Dimension(250,20));
        JPanel printPanel = new JPanel();
        printPanel.add(printButton);
        
        
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        buttonPanel.add(typePanel, c);
        buttonPanel.add(printPanel, c);
        
        this.add(chartPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
     
        
    }
    
    /**
     * Obsluguje akcje wygenerowane wewnatrz panelu SymbSymbChartPanel.
     * 
     * @param evt zdarzenie
     */
    public void actionPerformed(ActionEvent evt) {
        
        JButton source = (JButton)(evt.getSource());
		String s = source.getText();
        if (source == typeButton) {
            if (s == CHANGE_TO_PERCENT_TEXT) {
                chartPanel.drawPercent();
                typeButton.setText(CHANGE_TO_NORMAL_TEXT);
            }
           
            else {
                chartPanel.draw();
                typeButton.setText(CHANGE_TO_PERCENT_TEXT);
            }
        }
        else if (s == PRINT_TEXT) {
            printChart();
        }

         
     }

}

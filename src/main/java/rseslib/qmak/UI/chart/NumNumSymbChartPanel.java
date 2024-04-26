/*
 * Copyright (C) 2002 - 2024 The Rseslib Contributors
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYDataset;

import rseslib.qmak.UI.chart.DrawChartNumNumSymb;
import rseslib.qmak.UI.chart.NumNumSymbChartDataProvider;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.table.DoubleDataTable;


/**
 * Panel wyswietlajacy wykres typu numeryczne*numeryczne*symboliczne.
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class NumNumSymbChartPanel extends JPanel 
	implements ActionListener {
    
    /** wewnetrzny panel z wyswietlanym wykresem */
    private DrawChartNumNumSymb chartPanel;
    /** przycisk drukowania wykresu */
    private JButton printButton;
    /** nazwa przycisku drukowania */
    static private final String PRINT_TEXT = "Print";
    
    /**
     * Konstruktor tworzy panel zawierajacy wykres num*num*symb 
     * 
     * @param dataTable surowe dane, na podstawie ktorych bedzie stworzony wykres
     * @param iatr1 numer atrybutu w naglowku dataTable, ktory bedzie na osi X
     * @param iatr2 numer atrybutu w naglowku dataTable, ktory bedzie na osi Y
     * @param iatrV numer atrybutu w naglowku dataTable, ktorego rozklad 
     * bedzie badany
     */
    public NumNumSymbChartPanel(DoubleDataTable dataTable, int iatr1, 
            int iatr2, int iatrV) 
    {
        super(new BorderLayout());
        

        Header header = dataTable.attributes();   
        NumericAttribute natr1 = (NumericAttribute) header.attribute(iatr1);
        NumericAttribute natr2 = (NumericAttribute) header.attribute(iatr2);
        
        XYDataset dataset = NumNumSymbChartDataProvider.generateData(dataTable,iatr2,iatr1,iatrV);
        
        chartPanel = new DrawChartNumNumSymb(dataset,"Chart num*num*symb",
                natr2.name(),natr1.name());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
       
        printButton = new JButton(PRINT_TEXT);
        printButton.setActionCommand("print");
        printButton.addActionListener(this);
        printButton.setMaximumSize(new Dimension(250,20));
        JPanel printPanel = new JPanel();
        printPanel.add(printButton);
        
        this.add(chartPanel, BorderLayout.CENTER);
        this.add(printPanel, BorderLayout.SOUTH);
        
    }
    
    /** Wydrukowanie wykresu */
    private void printChart()
    {
        ChartPanel chartP = chartPanel.getCurrentChartPanel();
        chartP.createChartPrintJob();
        
    }
    
    /**
     * Obsluguje akcje wygenerowane wewnatrz panelu NumNumSymbChartPanel.
     * 
     * @param evt zdarzenie
     */
    public void actionPerformed(ActionEvent evt) {
        
        JButton source = (JButton)(evt.getSource());
		String s = source.getText();
        if (s == PRINT_TEXT) {
            printChart();
        }
         
     }

}

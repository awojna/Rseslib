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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;

import rseslib.qmak.UI.chart.DrawChartSymbNum;
import rseslib.qmak.UI.chart.SymbNumChartDataProvider;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.attribute.NumericAttribute;
import rseslib.structure.table.DoubleDataTable;

/**
 * Panel wyswietlajacy wykres typu numeryczne*symboliczne.
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class SymbNumChartPanel extends JPanel 
	implements ActionListener, ChangeListener {

    /** ilosc wypisanych wartosci na suwaku zmieniajacym dokladnosc */
    static private final int MAX_LABELS = 5;
    
    /** ilosc mozliwych roznych wartosci dokladnosci */
    static private final int DEF_ACC = 100;
    
    /** domyslna poczatkowa wartosc dokladnosci, przedstawiona
     * w postaci procentu maksymalnej wartosci dokladnosci
     */
    static private final int BEGIN_ACC = DEF_ACC/4;
    /** nazwa przycisku do zmiany wykresu na procentowy */
    static private final String CHANGE_TO_PERCENT_TEXT = "Change to percentage";
    /** nazwa przycisku do zmiany wykresu na ilosciowy */
    static private final String CHANGE_TO_NORMAL_TEXT = "Change to quantity";
    /** nazwa przycisku do drukowania wykresu */
    static private final String PRINT_TEXT = "Print";
    /** wewnetrzny panel z wyswietlanym wykresem */
    private DrawChartSymbNum chartPanel;
    /** przycisk do wyboru rodzaju wykresu (procentowy czy ilosciowy)*/
    private JButton typeButton;
    /** przycisk do drukowania wykresu */
    private JButton printButton;
    /** suwak do ustawiania dokladnosci */
    private JSlider accSlider;
    
    /** minimalna mozliwa wartosc dokladnosci */
    private double minAcc;
    /** maksymalna mozliwa wartosc dokladosci */
    private double maxAcc;
    
    /**
     * Konstruktor tworzy panel zawierajacy wykres num*symb 
     * 
     * @param dataTable surowe dane, na podstawie ktorych bedzie stworzony wykres
     * @param iatr1 numer atrybutu w naglowku dataTable, ktory bedzie na osi X
     * @param iatr2 numer atrybutu w naglowku dataTable, ktory bedzie na osi Y
     */
    public SymbNumChartPanel(DoubleDataTable dataTable, int iatr1, int iatr2) 
    {
        super(new BorderLayout());
        
        Header header = dataTable.attributes();
        NumericAttribute natr1 = (NumericAttribute) header.attribute(iatr1);
        NominalAttribute natr2 = (NominalAttribute) header.attribute(iatr2);
        
        // wstepne przygotowanie danych do wykresu
        TreeMap datas = SymbNumChartDataProvider.prepareData(dataTable,iatr1,iatr2);
    
        double minVal = ((Double) datas.firstKey()).doubleValue();
        double maxVal = ((Double) datas.lastKey()).doubleValue();
        minAcc = 0;
        maxAcc = (maxVal - minVal)/2;
        double succ = ((maxAcc - minAcc)/MAX_LABELS);
  
        
       /* komponenty do zmiany rodzaju wykresu */
        typeButton = new JButton(CHANGE_TO_PERCENT_TEXT);
        typeButton.setActionCommand("percentage");
        typeButton.addActionListener(this);
        JPanel typePanel = new JPanel();
        typePanel.add(typeButton);
        
        /* komponenty do drukowania */
        printButton = new JButton(PRINT_TEXT);
        printButton.setActionCommand("print");
        printButton.addActionListener(this);
        printButton.setMaximumSize(new Dimension(250,20));
        JPanel printPanel = new JPanel();
        printPanel.add(printButton);
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.NONE;
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        c.insets = new Insets(0,5,5,5);
        buttonPanel.add(typeButton, c);
        c.insets = new Insets(5,5,0,5);
        buttonPanel.add(printButton, c);
        double beginVal = ((maxAcc - minAcc)/DEF_ACC)*BEGIN_ACC;
        
        /* stworzenie wykresu z domyslna dokladnoscia (szerokoscia
         * przedzialu
         */
        chartPanel = new DrawChartSymbNum(datas,
                natr1,natr2,"Chart symb*num", beginVal);
        
        /* komponenty do ustawiania dokladnosci */
        JLabel sliderLabel = new JLabel("Interval width", SwingConstants.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        accSlider = new JSlider(SwingConstants.HORIZONTAL,
                0, DEF_ACC, BEGIN_ACC);
        accSlider.setName("Setting accuracy");
        accSlider.setMajorTickSpacing(10);
        accSlider.setMinorTickSpacing(1);
        accSlider.setPaintTicks(true);
        
        Hashtable labelTable = new Hashtable();
        for (int i = 0; i <= MAX_LABELS; i++)
        {
            Double tmp = new Double((minAcc + succ*i)*100);
            int tmp2 = tmp.intValue();
            labelTable.put( new Integer(i*(DEF_ACC/MAX_LABELS)), 
                    new JLabel((tmp2/100)+"."+(tmp2%100)));
        }
        
        accSlider.setLabelTable( labelTable );
        accSlider.setPaintLabels(true); 
        accSlider.setPaintTrack(true); 
        accSlider.setPreferredSize(new Dimension(400,100));
        accSlider.addChangeListener(this);
        
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
        sliderPanel.add(sliderLabel);
        sliderPanel.add(accSlider);
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        
        JPanel managerPanel = new JPanel(new GridLayout(1,2));
        managerPanel.add(buttonPanel);
        managerPanel.add(sliderPanel);
        managerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        this.add(chartPanel, BorderLayout.CENTER);
        this.add(managerPanel, BorderLayout.SOUTH);
        
    }
    
    
    /** Wydrukowanie wykresu */
    private void printChart()
    {
        ChartPanel chartP = chartPanel.getCurrentChartPanel();
        chartP.createChartPrintJob();
        
    }
    
    /**
     * Obsluguje akcje wygenerowane wewnatrz panelu SymbNumChartPanel
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
    
    /**
     * Obsluguje akcje wygenerowane poprzez zmiane dokladnosci
     * 
     * @param e zdarzenie
     */
    public void stateChanged(ChangeEvent e) 
    {
        JSlider source = (JSlider) e.getSource();
        
        if (!source.getValueIsAdjusting()) {
            double acc = ((maxAcc - minAcc)/DEF_ACC)*(source.getValue());
            chartPanel.changeAccuracy(acc);
           
        }
    }
}

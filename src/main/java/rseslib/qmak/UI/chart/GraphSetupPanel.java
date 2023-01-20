/*
 * Copyright (C) 2002 - 2023 The Rseslib Contributors
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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import rseslib.qmak.UI.chart.AttrInfo;
import rseslib.qmak.UI.chart.NumNumSymbChartPanel;
import rseslib.qmak.UI.chart.SymbNumChartPanel;
import rseslib.qmak.UI.chart.SymbSymbChartPanel;
import rseslib.qmak.dataprocess.table.iQDataTable;
import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.table.*;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.formats.HeaderFormatException;
import rseslib.structure.table.DoubleDataTable;


/**
 * Glowny panel zarzadzania wykresami. Umozliwia wybor typu wykresu, 
 * typu danych jakiego beda na nim wyswietlane, a takze atrybutow jakie 
 * beda reprezentowane przez poszczegolne osie wykresu
 *  
 * @author Damian Manski & Maciej Zuchniak
 */
public class GraphSetupPanel extends JPanel 
	implements ActionListener {
    
    /** wypisywana nazwa wykresu symb*symb */
    private final static String SYMB_SYMB_ID = "symb*symb";
    /** wypisywana nazwa wykresu num*symb */
    private final static String NUM_SYMB_ID = "num*symb";
    /** wypisywana nazwa wykresu num*num*symb */
    private final static String NUM_NUM_SYMB_ID = "num*num*symb";
    
    /** comboBox do wybierania typu wykresu */
    private JComboBox graphCombo;
    
/*    *//** comboBox do wybierania typu danych do wykresu *//* 
    private JComboBox dataCombo;
*/    
    /** comboBox do ustalenia atrybutu X wykresu num*symb */
    private JComboBox numSymbXCombo;
    /** comboBox do ustalenia atrybutu Y wykresu num*symb */
    private JComboBox numSymbYCombo;
    
    /** comboBox do ustalenia atrybutu X wykresu symb*symb */
    private JComboBox symbSymbXCombo;
    /** comboBox do ustalenia atrybutu Y wykresu symb*symb */
    private JComboBox symbSymbYCombo;
    
    /** comboBox do ustalenia atrybutu X wykresu num*num*symb */
    private JComboBox numNumSymbXCombo;
    /** comboBox do ustalenia atrybutu Y wykresu num*num*symb */
    private JComboBox numNumSymbYCombo;
    /** comboBox do ustalenia atrybutu symbolicznego wykresu num*num*symb */
    private JComboBox numNumSymbVCombo;
    /** przycisk do zatwierdzania tworzenia wykresu */
    private JButton createButton;
    
    /** czy okreslone dane pozwalaja na stworzenie wykresu num*symb */
    boolean numSymbEnable = true;
    /** czy okreslone dane pozwalaja na stworzenie wykresu symb*symb */
    boolean symbSymbEnable = true;
    /** czy okreslone dane pozwalaja na stworzenie wykresu num*num*symb */
    boolean numNumSymbEnable = true;
    
    /** panel przechowujacy comboBox'y do ustalania atrybutow na osiach */
    JPanel attrChangePanel;
    
    //TODO: zrobic tak aby ta klasa miala tylko atrybuty zwiazane z GUI a nie 
    // jakies tabele jak ponizej:
    iQDataTable qtable = null;
    
    /**
     * Konstruktor tworzy panel zarzadzajacy ustawianiem opcji, 
     * na podstawie ktorych generowane sa wykresy
     * @throws IOException 
     * @throws HeaderFormatException 
     *
     */
    public GraphSetupPanel(iQDataTable qtab)  
    {//TODO: zrobic inny sposob dostawania sie do tabeli - nie przez parametr (jesli sie da)
        this.setLayout(new GridLayout(2,2));
        
        String[] graphTypes = {
                SYMB_SYMB_ID,
                NUM_SYMB_ID,
                NUM_NUM_SYMB_ID,
                };
       
        /* komponenty do wyboru typu wykresu */
        JLabel graphTypeLabel = new JLabel("Chart type:");
        graphTypeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        graphTypeLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        graphCombo = new JComboBox(graphTypes);
        graphCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        graphCombo.setAlignmentY(Component.CENTER_ALIGNMENT);
        graphCombo.setMaximumSize(new Dimension(200,20));
        graphCombo.addActionListener(this);

        JPanel graphTypePanel = new JPanel();
        graphTypePanel.setLayout(new BoxLayout(graphTypePanel, BoxLayout.PAGE_AXIS));
        graphTypePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        graphTypePanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        graphTypePanel.add(graphTypeLabel);
        graphTypePanel.add(graphCombo);
        
        JPanel dataTypePanel = new JPanel();
        dataTypePanel.setLayout(new BoxLayout(dataTypePanel, BoxLayout.PAGE_AXIS));
        dataTypePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dataTypePanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        /* komponenty do wyboru atrybutow dla wykresu num*symb */
        JPanel symbNumAttrPanel = new JPanel();
        symbNumAttrPanel.setLayout(new BoxLayout(symbNumAttrPanel, BoxLayout.PAGE_AXIS));
        numSymbXCombo = new JComboBox();
        numSymbXCombo.setMaximumSize(new Dimension(250,20));
        numSymbYCombo = new JComboBox();
        numSymbYCombo.setMaximumSize(new Dimension(250,20));
        
        JLabel nsAxisXLabel = new JLabel("Numeric attribute on X axis:");
        nsAxisXLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel nsAxisYLabel = new JLabel("Symbolic attribute on Y axis:");
        nsAxisYLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        /* komponenty do wyboru atrybutow dla wykresu symb*symb */
        JPanel symbSymbAttrPanel = new JPanel();
        symbSymbAttrPanel.setLayout(new BoxLayout(symbSymbAttrPanel, BoxLayout.PAGE_AXIS));
        symbSymbXCombo = new JComboBox();
        symbSymbXCombo.setMaximumSize(new Dimension(250,20));
        symbSymbYCombo = new JComboBox();
        symbSymbYCombo.setMaximumSize(new Dimension(250,20));
        JPanel numNumSymbAttrPanel = new JPanel();
        
        JLabel ssAxisXLabel = new JLabel("Symbolic attribute on X axis:");
        ssAxisXLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel ssAxisYLabel = new JLabel("Symbolic attribute on Y axis:");
        ssAxisYLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        /* komponenty do wyboru atrybutow dla wykresu num*num*symb */
        numNumSymbAttrPanel.setLayout(new BoxLayout(numNumSymbAttrPanel, BoxLayout.PAGE_AXIS));
        numNumSymbXCombo = new JComboBox();
        numNumSymbXCombo.setMaximumSize(new Dimension(250,20));
        numNumSymbYCombo = new JComboBox();
        numNumSymbYCombo.setMaximumSize(new Dimension(250,20));
        numNumSymbVCombo = new JComboBox();
        numNumSymbVCombo.setMaximumSize(new Dimension(250,20));
        
        JLabel nnsAxisXLabel = new JLabel("Numeric attribute on X axis:");
        nnsAxisXLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel nnsAxisYLabel = new JLabel("Numeric attribute on Y axis:");
        nnsAxisYLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel nnsAxisVLabel = new JLabel("Coloured symbolic attribute:");
        nnsAxisVLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        /* ustalenie elementow wszystkich list wyboru dla wszystkich
         * typow wykresow
         */

        qtable = qtab;
        Header header = qtable.attributes();
        
        AttrInfo dec = null;  /* informacja o atrybucie decyzyjnym */
        int count = header.noOfAttr();
        for (int i = 0;i < count; i++)
        {
            if (header.isNominal(i)) {
                numSymbYCombo.addItem(new AttrInfo(i, 
                        header.name(i)));
                symbSymbXCombo.addItem(new AttrInfo(i, 
                        header.name(i)));
                symbSymbYCombo.addItem(new AttrInfo(i, 
                        header.name(i)));
                numNumSymbVCombo.addItem(new AttrInfo(i, 
                        header.name(i)));
            }
            if (header.isNumeric(i)) {
                numSymbXCombo.addItem(new AttrInfo(i, 
                        header.name(i)));
                numNumSymbXCombo.addItem(new AttrInfo(i, 
                        header.name(i)));
                numNumSymbYCombo.addItem(new AttrInfo(i, 
                        header.name(i)));
            }
            if (header.isDecision(i))
                dec = new AttrInfo(i, header.name(i));
        }
        
        /* ustawienie poczatkowe list wyboru dla wykresu num*symb */
        if (dec != null)
            numSymbYCombo.setSelectedItem(dec);
        symbNumAttrPanel.add(nsAxisXLabel);
        symbNumAttrPanel.add(numSymbXCombo);
        symbNumAttrPanel.add(nsAxisYLabel);
        symbNumAttrPanel.add(numSymbYCombo);
        if ((numSymbYCombo.getItemCount() == 0) 
        	|| (numSymbXCombo.getItemCount() == 0) 
//        	|| (dataCombo.getItemCount() == 0)
            )
            numSymbEnable = false;
        
        /* ustawienie poczatkowe list wyboru dla wykresu symb*symb */
        if (dec != null)
            symbSymbYCombo.setSelectedItem(dec);
        symbSymbAttrPanel.add(ssAxisXLabel);
        symbSymbAttrPanel.add(symbSymbXCombo);
        symbSymbAttrPanel.add(ssAxisYLabel);
        symbSymbAttrPanel.add(symbSymbYCombo);
        if ((symbSymbYCombo.getItemCount() == 0) 
        	|| (symbSymbXCombo.getItemCount() == 0) 
//        	|| (dataCombo.getItemCount() == 0)
            )
            symbSymbEnable = false;
        if ((symbSymbXCombo.getItemCount() > 1) &&
            (dec.equals(symbSymbXCombo.getItemAt(0))))
            symbSymbXCombo.setSelectedIndex(1);
        
        /* ustawienie poczatkowe list wyboru dla wykresu num*num*symb */
        if (dec != null)
            numNumSymbVCombo.setSelectedItem(dec);
        if (numNumSymbYCombo.getItemCount() > 1)
            numNumSymbYCombo.setSelectedIndex(1);
        numNumSymbAttrPanel.add(nnsAxisXLabel);
        numNumSymbAttrPanel.add(numNumSymbXCombo);
        numNumSymbAttrPanel.add(nnsAxisYLabel);
        numNumSymbAttrPanel.add(numNumSymbYCombo);
        numNumSymbAttrPanel.add(nnsAxisVLabel);
        numNumSymbAttrPanel.add(numNumSymbVCombo);
        if ((numNumSymbYCombo.getItemCount() == 0) 
        	|| (numNumSymbXCombo.getItemCount() == 0) 
        	|| (numNumSymbVCombo.getItemCount() == 0) 
//            || (dataCombo.getItemCount() == 0)
            )
            numNumSymbEnable = false;
        
        /* komponenty do zatwierdzania tworzenia wykresu */
        createButton = new JButton("Generate chart");
        createButton.setActionCommand("generate");
        createButton.setMaximumSize(new Dimension(180,30));
        JPanel applyPanel = new JPanel();
        applyPanel.add(createButton);
        createButton.addActionListener(this);
        
        /* ogolny panel do wybierania atrybutow dla wykresow, 
         * zawierajacy panele dla kazdego typu wykresu i uaktualniajacy
         * ten ktory akurat jest potrzebny
         */
        attrChangePanel = new JPanel();
        attrChangePanel.add(symbSymbAttrPanel);
        attrChangePanel.add(symbNumAttrPanel);
        attrChangePanel.add(numNumSymbAttrPanel);
        attrChangePanel.setLayout(new CardLayout());
        ((CardLayout) attrChangePanel.getLayout()).addLayoutComponent(symbSymbAttrPanel, SYMB_SYMB_ID);
        ((CardLayout) attrChangePanel.getLayout()).addLayoutComponent(symbNumAttrPanel, NUM_SYMB_ID);
        ((CardLayout) attrChangePanel.getLayout()).addLayoutComponent(numNumSymbAttrPanel,NUM_NUM_SYMB_ID);
     
        createButton.setEnabled(symbSymbEnable);
        ((CardLayout) attrChangePanel.getLayout()).show(attrChangePanel, SYMB_SYMB_ID);
        
        this.add(graphTypePanel);
        this.add(dataTypePanel);
        this.add(attrChangePanel);
        this.add(applyPanel);
        
    }
    
    /**
     * Obsluguje akcje wygenerowane przez liste wyboru typu wykresu 
     * lub przycisk zatwierdzania tworzenia wykresu
     * 
     * @param e zdarzenie
     */
    public void actionPerformed(ActionEvent e) {
        JPanel nextPanel;
        DoubleDataTable data;
        data = qtable.getDataTable();
        
        if (e.getSource() instanceof JButton) 
        {
            /* zostal wcisniety przycisk tworzenia wykresu */
            e.getSource();
            String newGr = (String)graphCombo.getSelectedItem();
            if (newGr == SYMB_SYMB_ID) 
            {
                int iatr1 = ((AttrInfo) symbSymbXCombo.getSelectedItem()).getId();
                int iatr2 = ((AttrInfo) symbSymbYCombo.getSelectedItem()).getId();
                nextPanel = new SymbSymbChartPanel(data, iatr1, iatr2);
            }
            else
            if (newGr == NUM_NUM_SYMB_ID)
            {

                int iatr1 = ((AttrInfo) numNumSymbXCombo.getSelectedItem()).getId();
                int iatr2 = ((AttrInfo) numNumSymbYCombo.getSelectedItem()).getId();
                int iatrV = ((AttrInfo) numNumSymbVCombo.getSelectedItem()).getId();
                nextPanel = new NumNumSymbChartPanel(data, iatr1, iatr2, iatrV);
            }
            else  
            {
                int iatr1 = ((AttrInfo) numSymbXCombo.getSelectedItem()).getId();
                int iatr2 = ((AttrInfo) numSymbYCombo.getSelectedItem()).getId();
                nextPanel = new SymbNumChartPanel(data, iatr1, iatr2);
            }
            
            JDialog frame = new JDialog(QMainFrame.getMainFrame(), qtable.getName());
            frame.getContentPane().add( nextPanel, BorderLayout.CENTER );
            frame.setSize( 640, 480 );
            frame.setLocationRelativeTo(QMainFrame.getMainFrame());
            frame.setVisible( true );
            frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
            
        }
        else {
            /* zostal zmieniony typ wykresu */
            JComboBox cb = (JComboBox)e.getSource();
            String newGr = (String)cb.getSelectedItem();
            if (newGr == SYMB_SYMB_ID) {
                 createButton.setEnabled(symbSymbEnable);
                ((CardLayout) attrChangePanel.getLayout()).show(attrChangePanel, SYMB_SYMB_ID);
            }
            else
            if (newGr == NUM_NUM_SYMB_ID) {
                createButton.setEnabled(numNumSymbEnable);
                ((CardLayout) attrChangePanel.getLayout()).show(attrChangePanel, NUM_NUM_SYMB_ID);
            }
            else
            if (newGr == NUM_SYMB_ID) {
                createButton.setEnabled(numSymbEnable);
                ((CardLayout) attrChangePanel.getLayout()).show(attrChangePanel, NUM_SYMB_ID);
            }
            
            attrChangePanel.updateUI();
       
        }
    }
}

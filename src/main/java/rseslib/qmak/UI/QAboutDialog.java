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


package rseslib.qmak.UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import rseslib.qmak.UI.QAboutDialog;

public class QAboutDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  private JPanel panel1 = new JPanel();
  private JPanel panel2 = new JPanel();
  private JPanel insetsPanel1 = new JPanel();
  private JPanel insetsPanel2 = new JPanel();
  private JPanel insetsPanel3 = new JPanel();
  private JButton buttonOk = new JButton();
  private JLabel imageLabel = new JLabel();
  private JLabel label1 = new JLabel();
  private JLabel label3 = new JLabel();
  private JLabel label4 = new JLabel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private FlowLayout flowLayout1 = new FlowLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private String product = "QMAK 3.6.0-SNAPSHOT";
  private String copyright = "Copyright (C) 2005 - 2025";
  private TitledBorder titledBorder2;
  JPanel panelAuthors = new JPanel();
  JPanel authorNames = new JPanel();
  JLabel jLabel1 = new JLabel();
  GridLayout gridLayout2 = new GridLayout();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel4 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JLabel jLabel6 = new JLabel();
  JLabel jLabel7 = new JLabel();
  JLabel jLabel8 = new JLabel();
  JLabel jLabel9 = new JLabel();
  JLabel jLabel10 = new JLabel();
  JLabel jLabel11 = new JLabel();
  JLabel jLabel12 = new JLabel();
  JLabel jLabel13 = new JLabel();
  JLabel jLabel14 = new JLabel();
  JLabel jLabel15 = new JLabel();
  JLabel jLabel16 = new JLabel();
  JLabel jLabel17 = new JLabel();
  JLabel jLabel18 = new JLabel();

  public QAboutDialog(Frame parent) {
    super(parent);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  //Component initialization
  private void jbInit() throws Exception  {
    titledBorder2 = new TitledBorder("");
    imageLabel.setBorder(titledBorder2);
    imageLabel.setIcon(new ImageIcon(QAboutDialog.class.getResource("imageQletter.gif")));
    this.setTitle("About");
    panel1.setLayout(borderLayout1);
    panel2.setLayout(borderLayout2);
    insetsPanel1.setLayout(flowLayout1);
    insetsPanel2.setLayout(flowLayout1);
    insetsPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    gridLayout1.setRows(4);
    gridLayout1.setColumns(1);
    label1.setText(product);
    label3.setText(copyright);
    label4.setText("The Rseslib Contributors");
    insetsPanel3.setLayout(gridLayout1);
    insetsPanel3.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
    buttonOk.setText("Ok");
    buttonOk.addActionListener(this);
    authorNames.setLayout(gridLayout2);
    panelAuthors.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
    panelAuthors.setLayout(new BorderLayout());
    jLabel1.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
    jLabel1.setText("Authors:");
    jLabel1.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    gridLayout2.setColumns(1);
    gridLayout2.setHgap(20);
    gridLayout2.setRows(6);
    jLabel2.setText("Arkadiusz Wojna");
    jLabel3.setText("Katarzyna Jachim");
    jLabel4.setText("Lukasz Kosson");
    jLabel5.setText("Lukasz Kowalski");
    jLabel6.setText("Damian Manski");
    jLabel7.setText("Michal Manski");
    jLabel8.setText("Krzysztof Mroczek");
    jLabel9.setText("Krzysztof Niemkiewicz");
    jLabel10.setText("Robert Piszczatowski");
    jLabel11.setText("Maciej Prochniak");
    jLabel12.setText("Tomasz Romanczuk");
    jLabel13.setText("Piotr Skibinski");
    jLabel14.setText("Marcin Staszczyk");
    jLabel15.setText("Michal Szostakiewicz");
    jLabel16.setText("Leszek Tur");
    jLabel17.setText("Damian Wojcik");
    jLabel18.setText("Maciej Zuchniak");
    insetsPanel2.add(imageLabel, null);
    panel2.add(insetsPanel2, BorderLayout.WEST);
    this.getContentPane().add(panel1, null);
    insetsPanel3.add(label1, null);
    insetsPanel3.add(label3, null);
    insetsPanel3.add(label4, null);
    panel1.add(panelAuthors, java.awt.BorderLayout.CENTER);
    panelAuthors.add(jLabel1, BorderLayout.NORTH);
    panelAuthors.add(authorNames, BorderLayout.CENTER);
    authorNames.add(jLabel2);
    authorNames.add(jLabel3);
    authorNames.add(jLabel4);
    authorNames.add(jLabel5);
    authorNames.add(jLabel6);
    authorNames.add(jLabel7);
    authorNames.add(jLabel8);
    authorNames.add(jLabel9);
    authorNames.add(jLabel10);
    authorNames.add(jLabel11);
    authorNames.add(jLabel12);
    authorNames.add(jLabel13);
    authorNames.add(jLabel14);
    authorNames.add(jLabel15);
    authorNames.add(jLabel16);
    authorNames.add(jLabel17);
    authorNames.add(jLabel18);
    panel2.add(insetsPanel3, BorderLayout.CENTER);
    insetsPanel1.add(buttonOk, null);
    panel1.add(insetsPanel1, BorderLayout.SOUTH);
    panel1.add(panel2, BorderLayout.NORTH);
    setResizable(true);
  }
  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      cancel();
    }
    super.processWindowEvent(e);
  }
  //Close the dialog
  void cancel() {
    dispose();
  }
  //Close the dialog on a button event
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == buttonOk) {
      cancel();
    }
  }
}

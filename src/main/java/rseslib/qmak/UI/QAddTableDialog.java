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


package rseslib.qmak.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class QAddTableDialog extends JDialog{
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  FlowLayout flowLayout1 = new FlowLayout();
  JButton OKButton = new JButton();
  JButton CancelButton = new JButton();
  JTextField TextFileName = new JTextField();
  JTextField TextName = new JTextField();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  public boolean CREATE = false;
  private String name= null;
  private String filename = null;

  public QAddTableDialog(Frame frame) {
    super(frame);
    try {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      jbInit();
      pack();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.setModal(true);
    this.getContentPane().setLayout(borderLayout1);
    jPanel2.setLayout(gridBagLayout1);
    jPanel1.setLayout(flowLayout1);
    OKButton.setText("Create");
    OKButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        OKButton_actionPerformed(e);
      }
    }
    );
    CancelButton.setText("Cancel");
    CancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        CancelButton_actionPerformed(e);
      }
    }
    );
    TextFileName.setMinimumSize(new Dimension(69, 19));
    TextFileName.setPreferredSize(new Dimension(69, 19));
    TextFileName.setRequestFocusEnabled(true);
    TextFileName.setSelectionStart(11);
    TextFileName.setText("");
    jLabel1.setText("Table name");
    jLabel2.setText("Table filename");
    TextName.setMinimumSize(new Dimension(69, 19));
    TextName.setPreferredSize(new Dimension(69, 19));
    this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(OKButton, null);
    jPanel1.add(CancelButton, null);
    this.getContentPane().add(jPanel2, BorderLayout.CENTER);
    jPanel2.add(TextFileName,     new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    jPanel2.add(TextName,    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    jPanel2.add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel2.add(jLabel2,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  }

  void CancelButton_actionPerformed(ActionEvent e) {
    dispose();
  }

  void OKButton_actionPerformed(ActionEvent e) {
    name = new String(TextName.getText());
    filename = new String(TextFileName.getText());
    CREATE = true;
    dispose();
  }

  public String getTName() {
    return name;
  }
  public String getTFileName() {
    return filename;
  }
}

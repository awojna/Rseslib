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


package rseslib.qmak.UI;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import javax.swing.border.Border;

import rseslib.qmak.UI.QMainFrame;
import rseslib.qmak.dataprocess.project.QProjectProperties;
import rseslib.qmak.QmakMain;
import rseslib.qmak.dataprocess.project.*;

/**
 * <p>Title: Dialog opcji projektu</p>
 * <p>Description: pokazanie opcji projektu do wy�wietlenia</p>
 * 
 * @author Krzysiek && Trickster's autors
 * @version 1.0
 */
public class iQProjectProperties extends JDialog {

  private QProjectProperties prop_tmp = new QProjectProperties();  //  @jve:decl-index=0:
  private QProjectProperties prop_orig;
  // obiekty interfejsu
  private JPanel panel1 = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel buttonsPanel = new JPanel();
  private JButton buttonOk = new JButton();
  private FlowLayout flowLayout1 = new FlowLayout();
  private JButton buttonCancel = new JButton();
  JPanel jPanel1 = new JPanel();
  Border border = BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(5, 5, 5, 5),
      BorderFactory.createEtchedBorder());
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JEditorPane EditorPaneDescription = new JEditorPane();
  JLabel jLabel3 = new JLabel();
  JLabel LabelDate = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel4 = new JLabel();
  JTextField TextFieldAuthor = new JTextField();
  JLabel jLabel5 = new JLabel();
  JTextField TextFieldProjectName = new JTextField();
  JLabel jLabel1 = new JLabel();
  JTextField TextFieldFileName = new JTextField();

  private QMainFrame owner;
  
  public iQProjectProperties(QMainFrame frame, boolean modal) {
    super(frame, modal);
    owner = frame;
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public iQProjectProperties() {
    this(null, false);
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    panel1.setLayout(borderLayout1);
    buttonOk.setText(QmakMain.getMainFrame().messages.getString("jOK"));
    buttonOk.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonOk_actionPerformed(e);
      }
    });
    buttonsPanel.setLayout(flowLayout1);
    buttonCancel.setText(QmakMain.getMainFrame().messages.getString("jCancel"));
    buttonCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonCancel_actionPerformed(e);
      }
    });
    this.setModal(false);
    this.setTitle(owner.messages.getString("iQPPproperties"));
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        this_componentResized(e);
      }
    });
    jPanel1.setAlignmentX( (float) 0.2);
    jPanel1.setBorder(border);
    jPanel1.setLayout(gridBagLayout1);
    borderLayout1.setHgap(0);
    borderLayout1.setVgap(0);
    EditorPaneDescription.setBorder(BorderFactory.createLoweredBevelBorder());
    EditorPaneDescription.setMinimumSize(new Dimension(300, 64));
    EditorPaneDescription.setPreferredSize(new Dimension(300, 40));
    EditorPaneDescription.setToolTipText("");
    jLabel3.setText(owner.messages.getString("iQPPcreation"));
    LabelDate.setText("D");
    jLabel2.setText(owner.messages.getString("iQPPfile"));
    jLabel4.setText(owner.messages.getString("iQPPauthor"));
    TextFieldAuthor.setText("");
    TextFieldAuthor.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        TextFieldAuthor_keyPressed(e);
      }
    });
    TextFieldAuthor.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        TextFieldAuthor_focusLost(e);
      }
    });
    jLabel5.setText(QmakMain.getMainFrame().messages.getString("iQPPProjectName"));
    TextFieldProjectName.setText("");
    TextFieldProjectName.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        TextFieldProjectName_keyPressed(e);
      }
    });
    TextFieldProjectName.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        TextFieldProjectName_focusLost(e);
      }
    });
    jLabel1.setText(owner.messages.getString("iQPPdescription"));
    EditorPaneDescription.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        EditorPaneDescription_focusLost(e);
      }
    });
    EditorPaneDescription.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        EditorPaneDescription_keyPressed(e);
      }
    });
    TextFieldFileName.setCaretPosition(0);
    TextFieldFileName.setText("");
    TextFieldFileName.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        TextFieldFileName_keyPressed(e);
      }
    });
    TextFieldFileName.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(FocusEvent e) {
        TextFieldFileName_focusLost(e);
      }
    });
    getContentPane().add(panel1);
        buttonsPanel.add(buttonOk, null);
    buttonsPanel.add(buttonCancel, null);
    panel1.add(buttonsPanel, java.awt.BorderLayout.SOUTH);

    panel1.add(jPanel1, java.awt.BorderLayout.CENTER);
    jPanel1.add(jLabel1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 0, 5), 0, 0));
    jPanel1.add(jLabel5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.EAST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
    jPanel1.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.EAST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
    jPanel1.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.EAST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
    jPanel1.add(LabelDate, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(5, 5, 5, 5), 0, 0));
    jPanel1.add(TextFieldProjectName,
                new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
                                       , GridBagConstraints.WEST,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(5, 5, 5, 5), 0, 0));
    jPanel1.add(TextFieldFileName,
               new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                      , GridBagConstraints.WEST,
                                      GridBagConstraints.HORIZONTAL,
                                      new Insets(5, 5, 5, 5), 0, 0));

    jPanel1.add(EditorPaneDescription,
                new GridBagConstraints(0, 4, 3, 1, 0.1, 0.1
                                       , GridBagConstraints.CENTER,
                                       GridBagConstraints.BOTH,
                                       new Insets(0, 5, 5, 5), 0, 0));
    jPanel1.add(TextFieldAuthor, new GridBagConstraints(1, 5, 1, 1, 1.4, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0));
    jPanel1.add(jLabel4, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.EAST,
                                                GridBagConstraints.NONE,
                                                new Insets(5, 5, 5, 5), 0, 0));
  }

  void assignData(QProjectProperties prop) {
    prop_orig = prop;
    prop_tmp.assign(prop_orig);
    // przypisanie wartosci wszystkim potrzebnym kontrolkom
    LabelDate.setText(prop_tmp.getCreationDate().toString());
    //LabelProjectFile.setText(prop_tmp.getFileName());
    TextFieldFileName.setText(prop_tmp.getFileName());
    TextFieldAuthor.setText(prop_tmp.getAuthor());
    TextFieldProjectName.setText(prop_tmp.getName());
    EditorPaneDescription.setText(prop_tmp.getDescription());
  }

  void buttonOk_actionPerformed(ActionEvent e) {
    //TODO: jakies rzeczy zwiazane z nacisnieciem Ok (sprawdzewnie poprawnosci)

    prop_orig.assign(prop_tmp);    
    prop_tmp = null;
    QMainFrame.getMainFrame().jMainWindow.repaint();
    dispose();
  }

  void buttonCancel_actionPerformed(ActionEvent e) {
    prop_tmp = null;
    dispose();
  }

  public void TextFieldProjectName_focusLost(FocusEvent e) {
    prop_tmp.setName(TextFieldProjectName.getText());
    prop_tmp.setUnSaved();
  }

  public void TextFieldProjectName_keyPressed(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ENTER) {
      prop_tmp.setName(TextFieldProjectName.getText());
      prop_tmp.setUnSaved();
    }
  }

  void TextFieldFileName_focusLost(FocusEvent e) {
    prop_tmp.setFileName(TextFieldFileName.getText());
    prop_tmp.setUnSaved();
  }

  void TextFieldFileName_keyPressed(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ENTER) {
      prop_tmp.setFileName(TextFieldFileName.getText());
      prop_tmp.setUnSaved();
    }

  }



  public void TextFieldAuthor_focusLost(FocusEvent e) {
    prop_tmp.setAuthor(TextFieldAuthor.getText());
    prop_tmp.setUnSaved();
  }

  public void TextFieldAuthor_keyPressed(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ENTER) {
      prop_tmp.setAuthor(TextFieldAuthor.getText());
      prop_tmp.setUnSaved();
    }
  }

  public void EditorPaneDescription_focusLost(FocusEvent e) {
    prop_tmp.setDescription(EditorPaneDescription.getText());
    prop_tmp.setUnSaved();
  }

  public void EditorPaneDescription_keyPressed(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ENTER) {
      prop_tmp.setDescription(EditorPaneDescription.getText());
      prop_tmp.setUnSaved();
    }
  }

  public void this_componentResized(ComponentEvent e) {
    int h;
    int w;
    boolean res = false;
    if (getSize().height < getMinimumSize().height) {
      h = getMinimumSize().height;
      res = true;
    }
    else
      h = getSize().height;
    if (getSize().width < getMinimumSize().width) {
      w = getMinimumSize().width;
      res = true;
    }
    else
      w =  getSize().width;

    if (res)
      setSize(w, h);
  }

}

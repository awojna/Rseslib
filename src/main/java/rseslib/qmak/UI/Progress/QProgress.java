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


package rseslib.qmak.UI.Progress;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import rseslib.qmak.UI.Progress.QVisualProgress;
import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.QMainFrame;

/**
 * pokazanie opcji projektu do wyswietlenia
 * @author Krzysiek Mroczek
 */
public class QProgress extends JFrame
{
	private JLabel jLabel = null;  //  @jve:decl-index=0:visual-constraint="36,264"
	private JProgressBar progres = null;  //  @jve:decl-index=0:
	private QVisualProgress owner;

	
	public QProgress(QVisualProgress own) {
		this.setSize(new Dimension(378, 167));
		this.initialize();
		owner = own;
	}
	
	private void initialize() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        jLabel = new JLabel();
        jLabel.setSize(new Dimension(112, 43));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridy = 1;
        this.getContentPane().setLayout(new GridBagLayout());
        progres = new JProgressBar();
        progres.setMinimum(0);

        this.getContentPane().add(progres, gridBagConstraints);
        this.getContentPane().add(jLabel, gridBagConstraints1);
        
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 2;
        JButton buttonCancel = new JButton();
        this.getContentPane().add(buttonCancel, gridBagConstraints2);
        buttonCancel.setText(QmakMain.getMainFrame().messages.getString("jCancel"));
        buttonCancel.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
        	  Cancel_actionPerformed();
          }
        });
        	
        
        this.setSize(new Dimension(300, 222));
	}
	
	private void Cancel_actionPerformed() {
		owner.setCancel();
	}
	
	public void pokaz() {
		this.setLocationRelativeTo(QMainFrame.getMainFrame());
		this.setVisible(true);
	}

	public void setText(String text){
		jLabel.setText(String.format("%s",text));
	}
	public void setMaximum(int i){
		progres.setMaximum(i);
	}
	public void setProgres(int i){
		progres.setValue(i);
	}

} 

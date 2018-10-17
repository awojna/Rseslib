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


package rseslib.qmak;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import rseslib.qmak.QmakMain;
import rseslib.qmak.UI.QMainFrame;
import rseslib.system.Report;
import rseslib.system.output.StandardErrorOutput;

public class QmakMain {

	  public static class Log {
	      public static void error(String s) {
	        JOptionPane.showMessageDialog(QMainFrame.getMainFrame(), s, "Error",
	                JOptionPane.ERROR_MESSAGE);
	      }
	      public static void debug(String s) {/*System.out.println(s);*/}
	      public static void fatal(String s) {
	          JOptionPane.showMessageDialog(QMainFrame.getMainFrame(), s + "\n " +
	                  "Program will be terminated", "Fatal Error",
	                JOptionPane.ERROR_MESSAGE);
	          QMainFrame.getMainFrame().dispose();
	      }
	  }
	private static QMainFrame mainFrame;
	
	public static QMainFrame getMainFrame() { return mainFrame; }
	  
	public QmakMain() {

		// Creating QmainFrame with decorated LookAndFeel
		JFrame.setDefaultLookAndFeelDecorated(true);
		mainFrame = new QMainFrame();
		QMainFrame.qmainframe = mainFrame;

		// Setting behaviour when exit pressed (later we'll move to ActionListener)
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );

		// Pack frames that have useful preferred size info, e.g. from their
		// layout
		mainFrame.pack();

		// Center the window and set sizes
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = mainFrame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		mainFrame.setLocation((screenSize.width - frameSize.width) / 2,
				(screenSize.height - frameSize.height) / 2);

		try {
			mainFrame.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Report.addErrorOutput(new StandardErrorOutput());
		new QmakMain();
	}

}

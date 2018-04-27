/*
 * $Name:  $
 * $RCSfile: NodeInfoFrame.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/07/07 10:20:11 $
 * Created on 2006-11-06
 *
 * Copyright (c) 2006 Uniwersytet Warszawski
 */

package rseslib.simplegrid.node;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Toolkit;

/**
 * @author Rafal Latkowski
 *
 */
public class NodeInfoFrame extends JFrame
{

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JPanel jPanelSoutj = null;

    private JPanel jPanelCenter = null;

    private JButton jButton = null;

    private JLabel jLabel1 = null;

    private JLabel jLabel2 = null;

    private JLabel jLabel3 = null;

    private JLabel jLabel4 = null;

    private JLabel jLabel5 = null;

    private JLabel jLabel6 = null;

    private JLabel jLabel7 = null;

    long start_time;
    /**
     * This is the default constructor
     */
    public NodeInfoFrame()
    {
        super();
        start_time = System.currentTimeMillis();
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setSize(640, 160);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/rseslib/simplegrid/sg_node16.gif")));
        this.setContentPane(getJContentPane());
        this.setTitle("SG-Node "+rseslib.simplegrid.common.Communication.s_strSGMVersion);
        this.addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent e)
            {
                showExitDialog();
            }
        });
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane()
    {
        if (jContentPane == null)
        {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
             jContentPane.add(getJPanelSoutj(), BorderLayout.SOUTH);
            jContentPane.add(getJPanelCenter(), BorderLayout.CENTER);
       }
        return jContentPane;
    }

    void showExitDialog()
    {
        JOptionPane pane = new JOptionPane("Are you sure you want to exit SGM-Node?",JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION);                
        JDialog dialog;
        dialog = pane.createDialog(this,"Confirmation");
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setVisible(true);
        Object selectedValue = pane.getValue();
        if ((selectedValue != null) && (selectedValue instanceof Integer))
        {
          int value=((Integer)selectedValue).intValue();
          if (value==JOptionPane.YES_OPTION)
            System.exit(0);
        }
        dialog.setVisible(false);
        dialog.dispose();

    }

    /**
     * This method initializes jPanelSoutj	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanelSoutj()
    {
        if (jPanelSoutj == null)
        {
            jPanelSoutj = new JPanel();
            jPanelSoutj.setLayout(new GridBagLayout());
            jPanelSoutj.add(getJButton(), null);
        }
        return jPanelSoutj;
    }

    /**
     * This method initializes jPanelCenter	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanelCenter()
    {
        if (jPanelCenter == null)
        {
            jLabel1 = new JLabel();
            jLabel1.setText("Manager:");
            jLabel2 = new JLabel();
            jLabel2.setText("Channel:");
            jLabel3 = new JLabel();
            jLabel3.setText("Computed tasks:");
            jLabel4 = new JLabel();
            jLabel4.setText("Task:");
            jLabel5 = new JLabel();
            jLabel5.setText("Up-time:");
            jLabel6 = new JLabel();
            jLabel6.setText("Automatic shutdown is not scheduled");
            jLabel7 = new JLabel();
            jLabel7.setText("Relay server is disabled");
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(7);
            gridLayout.setColumns(1);
            jPanelCenter = new JPanel();
            jPanelCenter.setLayout(gridLayout);
            jPanelCenter.add(jLabel1, null);
            jPanelCenter.add(jLabel2, null);
            jPanelCenter.add(jLabel3, null);
            jPanelCenter.add(jLabel4, null);
            jPanelCenter.add(jLabel5, null);
            jPanelCenter.add(jLabel6, null);
            jPanelCenter.add(jLabel7, null);
        }
        return jPanelCenter;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getJButton()
    {
        if (jButton == null)
        {
            jButton = new JButton();
            jButton.setText("Exit");
            jButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    showExitDialog();
                }
            });
        }
        return jButton;
    }
    
    void updateInfo(String manager,String channel,int computed_tasks,String task)
    {
        jLabel1.setText("Manager: "+manager);
        jLabel2.setText("Channel: "+channel);
        jLabel3.setText("Computed tasks: "+computed_tasks);
        jLabel4.setText("Task : "+task);
        jLabel5.setText("Up-time: "+getTime(System.currentTimeMillis()-start_time));
        if (WatchDog.m_lDeathTime>0)
        {
            jLabel6.setText("I will die in: "+getTime(WatchDog.m_lDeathTime-System.currentTimeMillis()));
        }
        if (RelayServer.s_nRelayServerIsWorking>0)
        {
            jLabel7.setText("Relaying servers: "+RelayServer.s_nRelayServerIsWorking+"  Relayed messages: "+RelayServer.s_nRelayedDatagramsCounter );
        }
    }
    public String getTime(long dtime)
    {
      int secs=(int)dtime/1000;
      int mins=secs/60;
      secs=secs%60;
      int hours=mins/60;
      mins=mins%60;

      if (hours>0)
      return hours+"h "+mins+"m "+secs+"s";
      if (mins>0)
      return mins+"m "+secs+"s";
      if (secs>0)
      return secs+"s "+(dtime%1000)+"ms";
      else
      return dtime%1000+"ms";
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"

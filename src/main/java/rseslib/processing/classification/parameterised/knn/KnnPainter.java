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


package rseslib.processing.classification.parameterised.knn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import rseslib.structure.attribute.Attribute;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Metric;
import rseslib.structure.metric.Neighbour;

/*
 * Main class responsible for visualization of k-nearest neighbor classifier.
 * 
 * @author		Lukasz Kosson
 */
public class KnnPainter extends JPanel implements MouseMotionListener, MouseListener
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	private static final int FIND_THRES = 10;
	private static final int POINT_SIZE = 5;
	private static final double START_MULT = 0.02;
	private static final double MAX_JUMP = 0.1;
	// private static final double DECAY_MULT = 0.99; // dla kwadratow
	private static final double DECAY_MULT = 0.995; // dla liniowego
	private static final double DECAY_MIN = 0.01;
	private static final double EPSILON = 0.00000000000001;
	private static final int START_MAX_ROWS = 150;

    private ArrayList<DoubleData> origData;
    private ArrayList<DoubleData> transformedData;
    private Metric metric;
    
	private Hashtable<DoubleData, DPoint> placement = new Hashtable<DoubleData, DPoint>();
    private Random rnd;
    private double avg;
	private double xmin = Double.NEGATIVE_INFINITY;
	private double ymin = Double.NEGATIVE_INFINITY;
	private double xmax = Double.POSITIVE_INFINITY;
	private double ymax = Double.POSITIVE_INFINITY;
    double mult = START_MULT;
    private String strLegend;
    private String dataPlaceholder;
    
    double currDev = 0.00;
    double iter = 0;
    double fProg = 0;  
	private Hashtable<Double, Integer> htCols;
	private Thread calcThread;
	private JLabel lblInfo;
	private boolean showDetails = true;
	private JButton btnRun;
	volatile private boolean runThread;
	
	private DoubleData hovered;
	private DoubleData selected;
	private DoubleData classified;
    private DoubleData origClassified;
	
	public KnnPainter(ArrayList<DoubleData> orig, ArrayList<DoubleData> transformed, Metric m, Random r, Hashtable<Double, Integer> colors, double avg_dist, String legend)
	{
		origData = orig;
		transformedData = transformed;
		metric = m;
		rnd = r;
		htCols = colors;
		avg = avg_dist;
		strLegend = legend;
		dataPlaceholder = "";
		for (int i = 0; i < transformed.get(0).attributes().noOfAttr(); ++i)
			dataPlaceholder += "<br>";
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	private void findRandomPlacement(int maxcnt)
	{
		mult = START_MULT;
		iter = 0;
		placement.clear();
        int cnt = transformedData.size();
        for (DoubleData next : transformedData)
        {
           if (cnt < maxcnt || rnd.nextInt(cnt) < maxcnt)
           {
        	   DPoint guess = new DPoint(avg);
        	   //obj.add(next);
        	   placement.put(next, guess);
           	}
        }		
	}
	
	private DoubleData findOriginal(DoubleData dat)
	{
		if(classified != null && classified == dat)
			return origClassified;
		int nr = 0;
        for (DoubleData next : transformedData)
        {
        	if (next == dat)
        		break;
        	nr++;
        }
        
        if (nr < origData.size())
        	return origData.get(nr);
        return dat;
	}
	
	private String formatData(DoubleData dat)
	{
		dat = findOriginal(dat);

		String out = "";
		int cnt = dat.attributes().noOfAttr();
		for (int i=0;i<cnt;i++)
		{
			Attribute attr = dat.attributes().attribute(i);
			String val;
			if (attr.isNominal())
			{
				val = NominalAttribute.stringValue(dat.get(i));
			}
			else
			{
				val = "" + dat.get(i);
				if (val.length() > 5) val = val.substring(0, 5);
			}
			out += attr.name() + ": <i>" + val + "</i><br>";
		}
		return out;
	}

	public void draw(JPanel canvas)
	{
		if(calcThread != null)
			stopThread();
        findRandomPlacement(Integer.MAX_VALUE);
        drawInternal(canvas, true);
		startThread();
	}

	private void drawInternal(JPanel canvas, boolean all_obj)
	{
	    JScrollPane scroll = new JScrollPane(this);
	    scroll.setVisible(true);
	    //canvas.add(scroll);
	    
	    JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	    canvas.add(jsp);
	    jsp.setRightComponent(scroll);
	    jsp.setDividerLocation(-1);
	    
	    int max = transformedData.size();
	    if (max > START_MAX_ROWS) max = START_MAX_ROWS;
	    final JTextField jtMax = new JTextField("" + transformedData.size());
	    jtMax.setPreferredSize(new Dimension(100, 24));
	    JButton btnRestart = new JButton("Restart");
	    btnRestart.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e) 
			{
				String txt = jtMax.getText();
				int maxcnt = Integer.MAX_VALUE;
				try
				{
					maxcnt = Integer.parseInt(txt);
				}
				catch (NumberFormatException ignored)
				{
				}
				jtMax.setText("" + maxcnt);
				findRandomPlacement(maxcnt);
				classified = null;
				origClassified = null;
	        	repaint();
	        	startThread();
			}
	    });
	    /*
	    JButton btnStep = new JButton("Step");
	    btnStep.addActionListener(new ActionListener()
	    {

			public void actionPerformed(ActionEvent e) 
			{
				new Thread(new Runnable()
				{
					public void run()
					{
			        	findPlacement();
			        	painter.repaint();						
					}
				}).start();
			}	    	
	    });
	    */
	    btnRun = new JButton("Start");
	    btnRun.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e) 
			{
				if (calcThread == null)
				{
					startThread();
				}
				else
				{
					stopThread();
				}
			}	    		    	
	    });
	    
	    final JCheckBox jcbDetails = new JCheckBox("Show object details");
	    jcbDetails.setSelected(showDetails);
	    jcbDetails.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent arg0)
			{
				showDetails = jcbDetails.isSelected();
				invalidate();
				repaint();
			}
	    });
	    
	    JPanel pnlBtns = new JPanel();
	    pnlBtns.setLayout(new FlowLayout());
	    if(all_obj)
	    {
	    	pnlBtns.add(new JLabel("Objects displayed: "));
	    	pnlBtns.add(jtMax);
	    	pnlBtns.add(btnRestart);
	    }
	    // pnlBtns.add(btnStep);
	    pnlBtns.add(btnRun);
	    pnlBtns.add(jcbDetails);
	    canvas.add(pnlBtns, BorderLayout.SOUTH);
	    
	    lblInfo = new JLabel("");
	    JPanel pnlInfo = new JPanel();
	    pnlInfo.setLayout(new BorderLayout());
	    pnlInfo.add(lblInfo, BorderLayout.CENTER);
	    pnlInfo.setPreferredSize(new Dimension(200, 100));
	    pnlInfo.setAlignmentX(0);
	    lblInfo.setAlignmentX(0);
	    //canvas.add(pnlInfo, BorderLayout.EAST);
	    jsp.setLeftComponent(pnlInfo);
	}
	
	public void stopThread()
	{
		fProg = 0;
		btnRun.setText("Start");
		runThread = false;
		try {
			if (calcThread != null)
				calcThread.join();
		} catch (InterruptedException e) {
		}
		calcThread = null;
	}
	
	private void startThread()
	{
		btnRun.setText("Stop");
		runThread = true;
		calcThread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					while (runThread)
					{
						findPlacement();
						invalidate();
						revalidate();
						repaint();
						try
						{
							Thread.sleep(50);
						}
						catch (InterruptedException ie) {}
					}
				}
				catch (ThreadDeath td)
				{
				}
				catch (Throwable thr)
				{
					thr.printStackTrace();
				}
			}
		});
		calcThread.start();
	}

	public void drawClassify(JPanel canvas, DoubleData obj, DoubleData origObj, Neighbour[] n)
	{
		try
		{
			if (classified == null)
				drawInternal(canvas, false);			
			if(calcThread != null)
				stopThread();
			mult = START_MULT;
			iter = 0;
			placement.clear();
			for (int i=0;i<n.length;i++)
			{
        	   DPoint guess = new DPoint(avg);
        	   //obj.add(next);
        	   if (n[i] == null) continue;
        	   placement.put(n[i].neighbour(), guess);				
			}
	    	placement.put(obj, new DPoint(avg));				
			classified = obj;
			origClassified = origObj;
			selected = obj;
			repaint();

			startThread();
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
	}

    private void findPlacement()
    {
        mult *= DECAY_MULT;
        if (mult < DECAY_MIN) mult = DECAY_MIN;
        fProg = 0;
        int i = 0;
        int s = placement.size();
        Hashtable<DoubleData, DPoint> htDelta = new Hashtable<DoubleData, DPoint>();
        long reftime = System.currentTimeMillis();
        Set<DoubleData> set = placement.keySet();        
        DoubleData[] arr = set.toArray(new DoubleData[0]);
        int cnt = arr.length;
        for (int e1 = 0;e1<cnt;e1++)
        {
        	DoubleData elem1 = arr[e1];
        	i++;
        	long currtime = i%10==0 ? System.currentTimeMillis() : reftime;
        	if (currtime - reftime > 200)
        	{
        		reftime = currtime;
        		fProg = (double)i / s;
        		invalidate();
        		repaint();
        	}
        	DPoint guess = placement.get(elem1);
        	for (int e2 = 0;e2<cnt;e2++)
        	{
        		DoubleData elem2 = arr[e2];
        		double dist;
    			dist = metric.dist(elem1, elem2);
                if (dist == 0) continue;

                DPoint delta;            	
            	if (htDelta.containsKey(elem2)) 
            		delta = htDelta.get(elem2);
            	else
            		delta = new DPoint(0, 0);
            	
        		DPoint pp = placement.get(elem2);

        		DPoint vec = guess.vect(pp);
        		double veclen = vec.len();

        		if (veclen < EPSILON)
        		{	
                    htDelta.put(elem2, delta);
        			continue;
        		}
        		else
        		{
        			vec.x /= veclen;
        			vec.y /= veclen;
        			//vec.z /= veclen;
        		}
                double lenerr = veclen - dist;
        		double weight = mult * lenerr;
        		//double weight = mult * Math.signum(lenerr) * (lenerr*lenerr);
        		
        		if (Math.abs(weight) < EPSILON)
        		{
                    htDelta.put(elem2, delta);
        			continue;
        		}
        		
                delta.x -= vec.x * weight;
        		delta.y -= vec.y * weight;
                //delta.z -= vec.z * weight;
                
                htDelta.put(elem2, delta);
        	}
        }
        
    	xmax = Double.NEGATIVE_INFINITY;
    	ymax = Double.NEGATIVE_INFINITY;
    	xmin = Double.POSITIVE_INFINITY;
    	ymin = Double.POSITIVE_INFINITY;

    	for (DoubleData elem : placement.keySet())
        {
        	DPoint opos = placement.get(elem);
        	DPoint delta = htDelta.get(elem);
        	if (delta.x > MAX_JUMP * avg) delta.x = MAX_JUMP*avg;
        	if (delta.x < -MAX_JUMP * avg) delta.x = -MAX_JUMP*avg;
        	if (delta.y > MAX_JUMP * avg) delta.y = MAX_JUMP*avg;
        	if (delta.y < -MAX_JUMP * avg) delta.y = -MAX_JUMP*avg;
        	opos.x += delta.x;
        	opos.y += delta.y;
        	placement.put(elem, opos);
        	
        	if (opos.x < xmin) xmin = opos.x;
        	if (opos.x > xmax) xmax = opos.x;
        	if (opos.y < ymin) ymin = opos.y;
        	if (opos.y > ymax) ymax = opos.y;
        }
        
    	iter ++;
        fProg = 0;
		invalidate();
		repaint();
		// System.out.println(System.currentTimeMillis() - starttime);
    }
	
    public void paint(Graphics g)
    {
    	String info = "<html>";
    	//g.setFont(Font.getFont("Monospaced"));
    	int psize = (classified == null) ? POINT_SIZE : POINT_SIZE * 2;
    	int w = getWidth()-psize*2;
    	int h = getHeight()-psize*2;
    	g.clearRect(0, 0, getWidth(), getHeight());
    	g.translate(psize, psize);
    	{
    		int dec = -1;
    		for (DoubleData elem1 : placement.keySet())
    		{
    			if (dec == -1) dec = elem1.attributes().decision();
    			double col = elem1.get(dec);
    			int val = htCols.get(col);
    			DPoint guess = placement.get(elem1);
    			int x = (int)((guess.x - xmin) / (xmax - xmin) * w); 
    			int y = (int)((guess.y - ymin) / (ymax - ymin) * h);
    			if (val < 0) val = -val;

    			if (elem1 == selected)
    			{
    				g.setColor(new Color(0, 0, 0));
    				g.fillOval(x-psize/2, y-psize/2, psize*2, psize*2);		        		
    			}

    			g.setColor(new Color(val%256, (val/256)%256, 0));
    			g.fillOval(x, y, psize, psize);

    			if (elem1 == classified)
    			{
    				g.drawLine(x-psize*2, y+psize/2, x+psize*3, y+psize/2);
    				g.drawLine(x+psize/2, y-psize*2, x+psize/2, y+psize*3);
    			}

    			if (elem1 == hovered)
    			{
    				g.setColor(new Color(0, 0, 0));
    				g.fillOval(x+1, y+1, psize/2, psize/2);		        		
    			}
    		}
    		g.setColor(Color.BLACK);
        	
    		info += "<b>Iteration:</b> " + (int)iter + "<br>";
    		info += "<b>Scaling:</b> " + Math.round(mult * 10000) / 10000.0 + "<br><br>";

        	info += strLegend + "<br";

    		if (showDetails)
    		{
    			if (selected != null)
    				info += "<b>Selected:</b><br>" + formatData(selected) + "<br>";
    			if (hovered != null)
    				info += "<b>Hovered:</b><br>" + formatData(hovered) + "<br>"; 
    			else
    				info += "<br><br>" + dataPlaceholder;
    			if (selected == null)
    				info += "<br><br>" + dataPlaceholder;
    		}

    		if (selected != null && hovered != null)
    		{
    			DPoint p_sel = placement.get(selected);
    			DPoint p_hov = placement.get(hovered);
    			double len_met = metric.dist(selected, hovered);
    			double len_vis = p_sel.dist(p_hov);

    			int x1 = (int)((p_sel.x - xmin) / (xmax - xmin) * w)+psize/2; 
    			int y1 = (int)((p_sel.y - ymin) / (ymax - ymin) * h)+psize/2;
    			int x2 = (int)((p_hov.x - xmin) / (xmax - xmin) * w)+psize/2; 
    			int y2 = (int)((p_hov.y - ymin) / (ymax - ymin) * h)+psize/2;

    			g.drawLine(x1, y1, x2, y2);

    			info += "<b>Distance:</b><br>";
    			info += "Metric: <i>" + Math.round(len_met * 100) / 100.0 + "</i><br>";
    			info += "Visible: <i>" + Math.round(len_vis * 100) / 100.0 + "</i><br>";
    		}
    		else
    			info += "<br><br><br>";
    	}
    	lblInfo.setText(info);
    	if (fProg > 0)
    	{
    		g.setColor(Color.BLACK);
    		g.fillRect(0, 0, (int)(w*fProg), 2);
    	}
    }

    public void mouseDragged(MouseEvent e) 
    {
    }

    public void mouseMoved(MouseEvent e)
    {
    	hovered = findObject(e.getX(), e.getY());
    	repaint();			
    }

    private DoubleData findObject(int x, int y)
    {
    	int w = getWidth();
    	int h = getHeight();
    	int min = FIND_THRES * FIND_THRES;
    	DoubleData ret = null;
    	for (DoubleData elem1 : placement.keySet())
    	{
    		DPoint guess = placement.get(elem1);
    		int dx = (int)((guess.x - xmin) / (xmax - xmin) * w); 
    		int dy = (int)((guess.y - ymin) / (ymax - ymin) * h);
    		dx -= x-2;
    		dy -= y-2;
    		if (dx*dx+dy*dy < min)
    		{
    			min = dx*dx+dy*dy;
    			ret = elem1;
    		}
    	}
    	return ret;
    }

    public void mouseClicked(MouseEvent e)
    {
    	selected = findObject(e.getX(), e.getY());
    	repaint();			
    }

    public void mouseEntered(MouseEvent e)
    {
    }

    public void mouseExited(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    class DPoint
    {
    	public double x;
    	public double y;
    	
    	public DPoint(double x, double y)
    	{
    		this.x = x;
    		this.y = y;
    	}
        
        public DPoint(double range)
        {
            x = rnd.nextDouble()*range*2 - range;
            y = rnd.nextDouble()*range*2 - range;
        }
        
        public DPoint vect(DPoint p)
        {
            return new DPoint(p.x - x, p.y - y);        
        }

        public double len()
        {
            return Math.sqrt(x*x + y*y);
        }
        
        public double dist(DPoint p)
        {
        	double dx = p.x - x;
        	double dy = p.y - y;
        	return Math.sqrt(dx*dx + dy*dy);
        }
        
        public int hashCode()
        {
        	return (int)(x*1000000+y*1000);
        }
    }
    
    class HashEntry
    {
    	public DoubleData p1;
    	public DoubleData p2;
    	
    	public HashEntry(DoubleData p1, DoubleData p2)
    	{
    		this.p1 = p1;
    		this.p2 = p2;
    	}
    	
    	public int hashCode()
    	{
    		return p1.hashCode() * 33221 + p2.hashCode()*71;
    	}
    }
}

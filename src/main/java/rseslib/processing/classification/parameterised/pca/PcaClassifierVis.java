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


package rseslib.processing.classification.parameterised.pca;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import rseslib.processing.classification.VisualClassifier;
import rseslib.structure.attribute.BadHeaderException;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;
import rseslib.structure.vector.Vector;
import rseslib.structure.vector.VectorForDoubleData;
import rseslib.structure.vector.subspace.PCASubspace;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;


public class PcaClassifierVis 
	extends PcaClassifier
	implements VisualClassifier
{
	
	// fields describing visualizations of classifier and classifying process
	private PCAPainter m_painter;
	private PCAPainter m_classifpainter;
	
	// canvas, where both visualizations are displayed
	private JPanel canvas;
	private JPanel clas_canvas;
	
	DoubleDataTable train_table;

	// constructors coming from PcaClassifier

	public PcaClassifierVis(Properties prop, DoubleDataTable tab, Progress prog) throws
		InterruptedException, BadHeaderException, PropertyConfigurationException {
		super(prop, tab, prog);
		train_table = tab;
	}

	public void draw(JPanel canv) {
		//System.out.println("draw");
		
	    if (canv.equals(canvas)) {
	        return;
	    }
	    canvas = canv;
		m_painter = new PCAPainter(train_table, canv);
	}

	public void drawClassify(JPanel canv, DoubleData obj) {
		//System.out.println("drawClassify");
		
		if (!canv.equals(clas_canvas)) {
			m_classifpainter = new PCAPainter(train_table, canv);
			clas_canvas = canv;
		}		
		m_classifpainter.set_selected_obs(obj);
	}

	public Header attributes() {
		return train_table.attributes();
	}
	
	class PCAPainter extends JComponent implements ActionListener {
		
	    /** Serialization version. */
		private static final long serialVersionUID = 1L;

		DoubleDataTable tr_table;
		DoubleData selected_obs = null;
		
		private JPanel canvas;

		TextField x_tf;
		TextField y_tf;
		Button refresh_button;
		Choice choice_box;
		
		PCASubspace subsp; // aktualna podprzestrzen decyzyjna
		int s1; // wymiar dla osi X
		int s2; // wymiar dla osi Y
		
		double par_ax; //do rysowania
		double par_bx; //do rysowania
		double par_ay; //do rysowania
		double par_by; //do rysowania
		int x0;
		int y0;
		
		void set_selected_obs (DoubleData sel_obs) {
			selected_obs = sel_obs;
		}
		
		// ustawienie parametrow do skalowania dla osi X
		void setParamX(double a_min, double a_max, double r_min, double r_max) {
			double margin = 0.1;
			par_ax = (r_max - r_min)/((1+margin)*a_max - (1-margin)*a_min);
			par_bx = r_min - (1 - margin) * a_min * par_ax;
		}
		
//		 ustawienie parametrow do skalowania dla osi Y
		void setParamY(double a_min, double a_max, double r_min, double r_max) {
			double margin = 0.1;
			par_ay = (r_max - r_min)/((1+margin)*a_max - (1-margin)*a_min);
			par_by = r_min - (1 - margin) * a_min * par_ay;
		}
		
		// zaokragla do podanej liczby cyfr po przecinku
		private double round(double value, int decimalPlace) {
			double power_of_ten = 1;
			while (decimalPlace-- > 0)
				power_of_ten *= 10.0;
			return Math.round(value * power_of_ten) / power_of_ten;
		}
		
		/* zaokragla x do pelnej jendostki unit */
		double round_to(double x, double unit) {
			double ceil = unit * Math.ceil(x / unit);
			double floor = unit * Math.floor(x / unit);
			if (Math.abs(x - floor) < Math.abs(ceil - x)) return floor;
			else return ceil;
		}
		
		/* zaokragla x do pelnej jendostki unit (sufit) */
		double round_to_ceil(double x, double unit) {
			return unit * Math.ceil(x / unit);
		}
		
		/* zaokragla x do pelnej jendostki unit (podloga) */
		double round_to_floor(double x, double unit) {
			return unit * Math.floor(x / unit);
		}
		
		/* liczba cyfr liczby x */
		int count_cyf(double x) {
			int i = 1;
			while (true) {
				if (x < 10) return i;
				x /= 10;
				i++;
			}
		}
		
		/* zaokragla liczbe do liczby pelnych dziesiatek, setek, tysiecy, w zaleznosci od jej dlugosci */
		double get_rounded(double x) {
			return round_to(x, Math.pow(10, count_cyf(x)-1));
		}
		
		/* zaokragla liczbe do liczby pelnych dziesiatek, setek, tysiecy, w zaleznosci od jej dlugosci (sufit) */
		double get_rounded_ceil(double x) {
			return round_to_ceil(x, Math.pow(10, count_cyf(x)-1));
		}
		
		/* rysuje os pozioma 
		 * min_v, max_v - wartosc najmniejsza i najwieksza
		 * count - liczba pionowych kresek na osi
		 * level - wpsolrzedna y, na jakiej os ma lezec */
		private void drawHorScale(Graphics g, double min_v, double max_v, int count, int level) {
			int i, x_tmp;
			double interval = getWidth()/count;
			double interv = (max_v - min_v)/count;
			double int_tmp;
			for (i=0; i<=count-1; i++) {
				x_tmp = (int)(i*interval);
				g.drawLine(x_tmp, level-5, x_tmp, level+5);
				int_tmp = min_v + i*interv;
				if (int_tmp % 1 == 0)
					g.drawString(""+(Math.round(min_v + i*interv)), x_tmp+2, level+10);
				else
					g.drawString(""+(round(min_v + i*interv, 3)), x_tmp+2, level+10);
			}
		}
		
		/* j.w. - rysuje os pionowa */
		private void drawVerScale(Graphics g, double min_v, double max_v, int count, int level) {
			int i, y_tmp;
			double interval = getHeight()/count;
			double interv = (max_v - min_v)/count;
			double int_tmp;
			for (i=0; i<=count-1; i++) {
				y_tmp = (int)(i*interval);
				g.drawLine(level-5, y_tmp, level+5, y_tmp);
				int_tmp = min_v + i*interv;
				if (int_tmp % 1 == 0)
					g.drawString(""+(Math.round(min_v + i*interv)), level+10, y_tmp+10);
				else
					g.drawString(""+(round(min_v + i*interv, 3)), level+10, y_tmp+10);
			}
		}
		
		/**
	     * Wielkosc piksela, gdy podany rozmiar najmniejszej, najwiekszej i danej obserwacji.
	     *
	     * @param min_res	Najmniejsza odleglosc (najwiekszy rozmiar piksela)
	     * @param max_res	Najwieksza odleglosc (najmniejszy rozmiar piksela)
	     * @param act_res	Aktualna odleglosc
	     */
		private int getObsSize(double min_res, double max_res, double act_res) {
			int count = 7; // liczba klas grubosci obszaru reprezentujacego obserwacje
			double interval = (max_res - min_res) / count;
			int i = 1;
			double act = min_res + interval;
			while (act <= act_res) {
				i++;
				act += interval;
			}
			return (count-i+1); // bo im wiekszy resid, tym wieksza odleglosc i tym mniejszy pixel
		}
		
		public void addControlArea(JPanel canvas, JPanel controlArea) {
			controlArea.setPreferredSize(new Dimension(180, 0));
			controlArea.setBackground(Color.lightGray);
			// podprzestrzen
			controlArea.add(new Label("Model for decision:"), BorderLayout.WEST);
		    // choice box
		    choice_box = new Choice();
		    for (int i=0; i<m_DecisionAttribute.noOfValues(); i++)
		    	choice_box.add(NominalAttribute.stringValue(m_DecisionAttribute.globalValueCode(i)));
		    choice_box.setPreferredSize(new Dimension(140, 20));
		    controlArea.add(choice_box, BorderLayout.WEST);
			controlArea.add(new Label("Principal components:"), BorderLayout.WEST);
		    // os X
		    controlArea.add(new Label("Axis X"), BorderLayout.WEST);
		    x_tf = new TextField("0");
		    controlArea.add(x_tf, BorderLayout.EAST);
		    // os Y
		    controlArea.add(new Label("Axis Y"), BorderLayout.WEST);
		    y_tf = new TextField("1");
		    controlArea.add(y_tf, BorderLayout.EAST);
		    // refresh button
		    refresh_button = new Button("Update chart");
		    refresh_button.addActionListener(this);
		    refresh_button.setPreferredSize(new Dimension(140, 30));
		    controlArea.add(refresh_button, BorderLayout.SOUTH);
		    // legenda
			controlArea.add(new Label("Decision classes:"));
		    for (int i=0; i<m_DecisionAttribute.noOfValues(); i++) {
		    	Label l = new Label(NominalAttribute.stringValue(m_DecisionAttribute.globalValueCode(i)));
		    	l.setPreferredSize(new Dimension(90, 15));
		    	l.setBackground(this.getColorForDecision(i, 0));
		    	controlArea.add(l);
		    }
		    // koniec
		    canvas.add(controlArea, BorderLayout.EAST);
		}
		
		public void setVisualiztionParams(int subsp_nr, int s_1, int s_2) {
			subsp = m_nSubspaces[subsp_nr];
			s1 = s_1;
			s2 = s_2;
		}
		
		public void actionPerformed(ActionEvent e) {
			int subsp = new Integer(choice_box.getSelectedIndex());
			int x = new Integer(x_tf.getText());
			int y = new Integer(y_tf.getText());
			this.setVisualiztionParams(subsp, x, y);
			canvas.validate();
			canvas.repaint();
		}
		
		public Color getColorForDecision(double org_dec, double min_dec) {
			//double dec = org_dec - min_dec + 1;
			double dec = org_dec; 
			if (dec == 0) return Color.green;
			if (dec == 1) return Color.red;
			if (dec == 2) return Color.blue;
			if (dec == 3) return Color.orange;
			if (dec == 4) return Color.magenta;
			if (dec == 5) return Color.cyan;
			if (dec == 6) return Color.pink;
			if (dec == 7) return Color.darkGray;
			if (dec == 8) return Color.gray;
			if (dec == 9) return Color.lightGray;
			return Color.yellow;
		}
			
		public PCAPainter(DoubleDataTable tr_tab, JPanel canv) {
			tr_table = tr_tab;
			canvas = canv;
			setVisualiztionParams(0, 0, 1);
			this.setVisible(true);
			this.setSize(300, 300);
			JScrollPane scroll = new JScrollPane(this);
			scroll.setVisible(true);
			canv.add(scroll);
			//JPanel controlArea = new JPanel(new GridLayout(4,2));
			JPanel controlArea = new JPanel(new FlowLayout());
			addControlArea(canv, controlArea);
		}
		
		public PCAPainter(DoubleDataTable tr_tab, DoubleData obj, JPanel canv) {
			tr_table = tr_tab;
			selected_obs = obj;
			canvas = canv;
			setVisualiztionParams(0, 0, 1);
			this.setVisible(true);
			this.setSize(300, 300);
			JScrollPane scroll = new JScrollPane(this);
			scroll.setVisible(true);
			canv.add(scroll);
			//JPanel controlArea = new JPanel(new GridLayout(4,2));
			JPanel controlArea = new JPanel(new FlowLayout());
			addControlArea(canv, controlArea);
		}
		
		public void paint_selected_obs(Graphics g, int s1, int s2) {
			//System.out.println(selected_obs.get(s1)+", "+selected_obs.get(s2));
			try {
				int psd = getIntProperty(PRINCIPAL_SUBSPACE_DIM);
				int ss = 8; //selection size
				double[] o = new double[psd];
				Vector x = new VectorForDoubleData(selected_obs);
				Vector px = subsp.projections(x)[psd-1];
			    for (int i = 0; i < psd; i++)
			    	o[i] = px.scalarProduct(subsp.getSpanningVector(i));
			    g.setColor(Color.black);
			    drawCircle(g, (int)(x0+par_ax*o[s1]+par_bx), (int)(y0+par_ay*o[s2]+par_by), ss);
			} catch (PropertyConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void drawCircle(Graphics g, int x, int y, int diameter) {
			g.drawOval(x-diameter/2, y-diameter/2, diameter, diameter);
		}
		
		public void fillCircle(Graphics g, int x, int y, int diameter) {
			g.fillOval(x-diameter/2, y-diameter/2, diameter, diameter);
		}
		
		public void paintComponent(Graphics g) {
			//System.out.println("paintComponent");
			super.paintComponent(g);
			//int s1 = 0;
			//int s2 = 1;
			x0 = getWidth()/8;
			y0 = getHeight()/4;
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.black);
			g.drawLine(0, y0, getWidth(), y0);
			g.drawLine(x0, 0, x0, getHeight());
			Iterator<DoubleData> it = tr_table.getDataObjects().iterator();
			DoubleData obs;
			double minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY, maxx = Double.NEGATIVE_INFINITY, maxy = Double.NEGATIVE_INFINITY, min_dec = Double.POSITIVE_INFINITY, max_dec = Double.NEGATIVE_INFINITY;
			double minres = Double.POSITIVE_INFINITY, maxres = Double.NEGATIVE_INFINITY;
			int dec_atr, psd = 0;
			try {
				psd = getIntProperty(PRINCIPAL_SUBSPACE_DIM);
			} catch (PropertyConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (it.hasNext()) {
				double[] y = new double[psd];
				obs = it.next();
				Vector x = new VectorForDoubleData(obs);	
				//DoubleVector px = subsp.projections(x)[12];
				Vector px = subsp.projections(x)[psd-1];
				px.subtract(x);
				double resid = px.squareEuclideanNorm();
				if (resid > maxres) maxres = resid;
				if (resid < minres) minres = resid;
				px.add(x);
			    for (int i = 0; i < psd; i++)
			    	y[i] = px.scalarProduct(subsp.getSpanningVector(i));
			    dec_atr = obs.attributes().decision();
				if (y[s1] < minx ) minx = y[s1];
				if (y[s1] > maxx ) maxx = y[s1];
				if (y[s2] < miny ) miny = y[s2];
				if (y[s2] > maxy ) maxy = y[s2];
				if (obs.get(dec_atr) < min_dec ) min_dec = obs.get(dec_atr);
				if (obs.get(dec_atr) > max_dec ) max_dec = obs.get(dec_atr);
			}
			setParamX(minx, maxx, 0, getWidth()-x0);
			setParamY(miny, maxy, 0, getHeight()-y0);
			
			double roz = get_rounded_ceil((maxx - minx)/10);
			minx = round_to_floor(minx, roz);
			maxx = round_to_ceil(maxx, roz);
			roz = get_rounded_ceil((maxy - miny)/10);
			miny = round_to_floor(miny, roz);
			maxy = round_to_ceil(maxy, roz);
			drawHorScale(g, minx, maxx, 10, y0);
			drawVerScale(g, miny, maxy, 10, x0);
			it = tr_table.getDataObjects().iterator();
			while (it.hasNext()) {
				double[] y = new double[psd];
				obs = it.next();
				Vector x = new VectorForDoubleData(obs);
				Vector px = subsp.projections(x)[psd-1];
				px.subtract(x);
				double resid = px.squareEuclideanNorm();
				px.add(x);
			    for (int i = 0; i < psd; i++)
			    	y[i] = px.scalarProduct(subsp.getSpanningVector(i));
				dec_atr = obs.attributes().decision();				
/*				if (obs.get(dec_atr) == min_dec) g.setColor(Color.red);
				else if (obs.get(dec_atr) == max_dec) g.setColor(Color.green);
				else g.setColor(Color.blue);
*/				//m_DecisionAttribute.globalValueCode(i);
				//NominalAttribute.stringValue(globalValueCode)
				g.setColor(getColorForDecision(m_DecisionAttribute.localValueCode(obs.get(dec_atr)), min_dec));
				int obs_size = getObsSize(minres, maxres, resid);
				fillCircle(g, (int)(x0+par_ax*y[s1]+par_bx), (int)(y0+par_ay*y[s2]+par_by), obs_size);
				
			}
			
			if (selected_obs != null) paint_selected_obs(g, s1, s2);
			this.setVisible(true);
		}
	}

}

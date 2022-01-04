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


package rseslib.processing.classification.neural;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.Image;
import java.util.*;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.geom.*;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import java.awt.Graphics;
import java.awt.geom.Line2D;
import java.awt.Rectangle;

import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.table.DoubleDataTable;

/**
 * Klasa reprezentujaca panel z siecia neuronowa
 * 
 * @author Damian Wojcik
 * 
 */
public class VNNPanel extends JComponent implements
		MouseListener {

    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	static int IMAGEX = 20;

	static int IMAGEY = 20;

	static int FIRSTX = IMAGEX;

	static int FIRSTY = IMAGEY / 2;

	static int SECONDX = 0;

	static int SECONDY = IMAGEY / 2;

	static int ROW_DISTANCE = 20;
	
	static int LEFT_MARGIN = 120;
	
	static int RIGHT_MARGIN = 240;

	private int DimensionX = 800;

	private int DimensionY = 600;

	private Image blueImg[] = null;

	private double blueImgBrackets[] = null;

	private Image orangeImg = null;
	
	private Image inputImage = null;

	private Perceptron zoomedPerceptron = null;

	private int[] networkStructure;
	
	private List<Layer> layers;

	private Map<Integer, List<PPair>> paths = new HashMap<Integer, List<PPair>>();

	private Map<Perceptron, Rectangle> points = new HashMap<Perceptron, Rectangle>();

	private Map<Perceptron, Image> pictures = new HashMap<Perceptron, Image>();

	private double minWeight, maxWeight;

	private DoubleDataTable table;

	private Layer inputFoo;
	
	public JButton TrainButton;

	public JButton AddNodes;
	
	public boolean validShapes = false;

	public boolean showClassification;

	/**
	 * Konstruktor VNNPanel
	 * 
	 * @param newSizes
	 *            lista rozmiarow kolejnych warstw sieci - tez z wejsciem i
	 *            zakonczeniem
	 * @param newLayers
	 *            lista perceptronow w kolejnych warstwach - bez wejscia
	 */
	public VNNPanel(int[] newSizes, List<Layer> newLayers, DoubleDataTable tab, boolean addButtons) {
		/* inicjalizacja jako podklasy JPanel */
		super();
		
		DimensionX = getSize().width;
		DimensionY = getSize().height;
		
		int i;
		/* pobranie argumentow */
		if ((newSizes == null) || (newLayers == null)) {
			System.out.print("Jeden z parametrow konstruktora klasy NUpperPanel jest bledny");
			System.exit(1);
		} else {
			networkStructure = newSizes;
			layers = newLayers;
		}

		/* wczytanie obrazkow dla perceptronow */

			blueImg = new Image[6];
			blueImg[0] = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("perceptron1.png")).getImage();
			blueImg[1] = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("perceptron2.png")).getImage();
			blueImg[2] = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("perceptron3.png")).getImage();
			blueImg[3] = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("perceptron4.png")).getImage();
			blueImg[4] = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("perceptron5.png")).getImage();
			blueImg[5] = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("perceptron6.png")).getImage();
			orangeImg = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("perceptronZaznaczony.png")).getImage();
			inputImage = new ImageIcon(rseslib.processing.classification.neural.VNNPanel.class.getResource("input.png")).getImage();
			blueImgBrackets = new double[6];
			for (i = 0; i < 6; i++)
				blueImgBrackets[i] = 0;


		inputFoo = new Layer(networkStructure[0]);

		/* inicjacja skojarzen pomiecdzy perceptronami a obrazkami */
		pictures.clear();

		// dodanie opisu okna
		setToolTipText("Visualisation of classification with neuron network classifier");

		if (addButtons)
		{
			AddNodes = new JButton("Add Neurons");
			this.add(AddNodes);
			AddNodes.setLocation(20, getHeight() - 30);
			AddNodes.setSize(new Dimension(160, 30));
			AddNodes.setVisible(true);
			
			TrainButton = new JButton("Retrain");
			this.add(TrainButton);
			TrainButton.setLocation(180, getHeight() - 30);
			TrainButton.setSize(new Dimension(100, 30));
			TrainButton.setVisible(true);
		}
		
		setLayout(null);
		//setPreferredSize(new Dimension(DimensionX, DimensionY));
		addMouseListener(this);
		table = tab;
		showClassification = false;

		repaint();
	}

	public void setNotValidShapes() {
		validShapes = false;
		repaint();
	}
	
	/**
	 *  Zmiana rozmiaru okna - dezaktualizacja ulozenia elementow 
	 */
	public void setBounds(int x, int y, int width, int height) {
		validShapes = false;
		super.setBounds(x, y, width, height);
	}

	/**
	 *  Zmiana rozmiaru okna - dezaktualizacja ulozenia elementow 
	 */
	public void setBounds(Rectangle r) {
		validShapes = false;
		super.setBounds(r);
	}

	/**
	 * Uaktualnienie informacji o strukturze sieci neuronowej
	 * 
	 * @param newSizes
	 * @param newLayers
	 */
	public void updateModel(int[] newSizes, List<Layer> newLayers) {
		networkStructure = newSizes;
		layers = newLayers;
		pictures.clear();
		setNotValidShapes();
	}

	/**
	 * Rysowanie sieci neuronowej
	 */
	public void paint(Graphics g) {
		Graphics2D gg = (Graphics2D) g;
		Color baseColor = gg.getColor();

		super.paint(g);

		/* wyrysowanie perceptronow */
		Perceptron p1;		
		if (!validShapes) {
			points.clear();
			calculate();
		}	
		Iterator<Perceptron> it = points.keySet().iterator();
		while (it.hasNext()) {
			p1 = it.next();
			if (inputFoo.includesPercetron(p1)) {
				drawInput(g, points.get(p1));
			} else
				drawPerceptron(g, points.get(p1), p1);
		}

		/* wyskalowanie kolorow linii */
		if (!validShapes) {
			double pom1, pom2;
			minWeight = layers.get(0).getMinWeight();
			maxWeight = layers.get(0).getMaxWeight();
			for (Layer present : layers) {
				if (layers.get(0) != present) {
					pom1 = present.getMinWeight();
					pom2 = present.getMaxWeight();
					if (pom1 < minWeight)
						minWeight = pom1;
					if (pom2 > maxWeight)
						maxWeight = pom2;
				}
			}
		}

		/* wyliczenie prostych reprezentujacych linie pomiedzy perceptronami */
		if (!validShapes) {
			Perceptron p2;
			ListIterator<Perceptron> it1, it2;
			Layer previousl = null;
			Line2D.Float newLine;

			paths.clear();
			for (Layer present : layers) {
				if (previousl == null) {
					previousl = inputFoo;
				}
				it1 = previousl.perceptrons.listIterator();
				int i1 = -1;
				it2 = present.perceptrons.listIterator();
				while (it1.hasNext()) {
					p1 = it1.next();
					i1++;
					while (it2.hasNext()) {
						p2 = it2.next();
						newLine = new Line2D.Float(points.get(p1).x + FIRSTX,
								points.get(p1).y + FIRSTY, points.get(p2).x
										+ SECONDX, points.get(p2).y + SECONDY);
						if (paths.containsKey(i1)) {
							paths.get(i1).add(new PPair(p1, p2, newLine));
						} else {
							List<PPair> l = new ArrayList<PPair>();
							l.add(new PPair(p1, p2, newLine));
							paths.put(i1, l);
						}
					}
					it2 = present.perceptrons();
				}
				previousl = present;
			}
			validShapes = true;
		}
		List<PPair> lista;

		/* wyrysowanie linii pomiedzy perceptronami */
		for (Integer klucz : paths.keySet()) {
			lista = paths.get(klucz);
			for (PPair element : lista) {
				gg.setColor(ColorChooser.getColor(element.getSPerceptron()
						.getWeight(klucz), minWeight, maxWeight));
				gg.draw(element.getLine());
			}
		}

		/* dodanie legendy kolorow */
		int i;
		Point2D.Float corner = new Point2D.Float(getWidth() - RIGHT_MARGIN, 20);
		gg.setColor(Color.black);
		gg.drawString("Legend", (int) corner.getX(), (int) corner.getY());
		for (i = 0; i < ColorChooser.COLOR_NO; i++) {
			corner.y = corner.y + ROW_DISTANCE;
			gg.setColor(ColorChooser.COLORS[i]);
			Rectangle rec = new Rectangle((int) (corner.x), (int) (corner.y),
					40, 20);
			gg.draw(rec);
			gg.fill(rec);
			gg.setColor(Color.black);
			gg.drawString(ColorChooser.getBounds(i, minWeight, maxWeight),
					corner.x + 60, corner.y + 15);
		}

		/* dodanie opisu do zaznaczonego neuronu */
		if (zoomedPerceptron != null) {
			gg.setColor(Color.black);
			corner.y = corner.y + 45;
			if (showClassification) {
				gg.drawString("Output: " + zoomedPerceptron.getOutput(), corner.x, corner.y);
				corner.y = corner.y + 2*ROW_DISTANCE;
			}
			gg.drawString("Bias: " + zoomedPerceptron.getWeight(zoomedPerceptron.getWeightLength() - 1), corner.x, corner.y);
			corner.y = corner.y + ROW_DISTANCE;
			gg.drawString("Weights: ", corner.x, corner.y);
			corner.y = corner.y + ROW_DISTANCE;
			for (i = 0; i < (zoomedPerceptron.getWeightLength() - 1); i++) {
				gg.drawString(i + " : " + zoomedPerceptron.getWeight(i), corner.x, corner.y);
				corner.y = corner.y + ROW_DISTANCE;
			}
		}
		
		/* dodanie opisu do neuronow wejsciowych - z lewej strony obrazka neuronu */
		StringBuffer text = new StringBuffer();
		Set<Double> valueSet = new HashSet<Double>();
		Iterator vSIterator = valueSet.iterator();
		Rectangle ElementPosition, stringPosition;
		int attr_no = 0;		
		for (Perceptron p : inputFoo.perceptrons) {
			ElementPosition = points.get(p);
			stringPosition = (Rectangle) ElementPosition.clone();
			if (stringPosition.x - LEFT_MARGIN > 0)
				stringPosition.setLocation(stringPosition.x - LEFT_MARGIN,
						stringPosition.y + 12);
			else
				stringPosition.setLocation(0, stringPosition.y + 12);
			text.setLength(0);
			text.append(table.attributes().name(attr_no));
			if (text.length() > 15)
				text.setLength(15);
			if (table.attributes().isNumeric(attr_no)) {
				attr_no++;
			} else {
				if (table.attributes().attribute(attr_no).isNominal()) {
					if (table.attributes().attribute(attr_no).isDecision()) {
						attr_no++;
						text.setLength(0);
						text.append(table.attributes().name(attr_no));
					}
					if (!vSIterator.hasNext()) {
						valueSet = new HashSet<Double>();
						for (DoubleData dd : table.getDataObjects()) {
							valueSet.add(new Double(dd.get(attr_no)));
						}
						vSIterator = valueSet.iterator();
					}
					// dopisanie kolejnej wartosci
					text.append(" "+ (NominalAttribute.stringValue((Double) vSIterator.next())));
					// sprawdzenie czy moge sknczyc iterowanie
					if (!vSIterator.hasNext())
						attr_no++;
				}
			}
			gg.drawString(text.toString(), stringPosition.x, stringPosition.y);
		}

		/* dodanie opisu do neuronow wyjsciowych - z prawej strony obrazka neuronu*/
		attr_no = 0;
		while (! table.attributes().attribute(attr_no).isDecision())
			attr_no++;
		valueSet = new HashSet<Double>();
		for (DoubleData dd : table.getDataObjects()) {
			valueSet.add(new Double(dd.get(attr_no)));
		}
		vSIterator = valueSet.iterator();
		
		for (Perceptron p : layers.get(layers.size() - 1).perceptrons) {
			ElementPosition = points.get(p);
			stringPosition = (Rectangle) ElementPosition.clone();
			stringPosition.setLocation(stringPosition.x + 40,
					stringPosition.y + 12);
			gg.drawString(NominalAttribute.stringValue((Double) vSIterator.next()), stringPosition.x, stringPosition.y);
		}

		/* dodanie przyciskow */
		if (TrainButton!=null)
		{
			AddNodes.setLocation(20, getHeight() - 30);
			AddNodes.setVisible(true);
		}
		
		if (TrainButton!=null)
		{
			TrainButton.setLocation(180, getHeight() - 30);
			TrainButton.setVisible(true);
		}
		
		/* inne */
		gg.setColor(baseColor);
	}

	/**
	 * Rysowanie perceptronu w okreslonej lokalizacji
	 * @param g Graphics
	 * @param rec Rectangle
	 * @param p Perceptron
	 */
	private void drawPerceptron(Graphics g, Rectangle rec, Perceptron p) {
		if (pictures.containsKey(p)) {
			g.drawImage(pictures.get(p), rec.x, rec.y, null);
		} else {
			if (!showClassification) {
				g.drawImage(blueImg[3], rec.x, rec.y, null);
			} else {
				double pom = p.getOutput();
				int i = 0;
				while ((i < blueImgBrackets.length)
						&& (blueImgBrackets[i] < pom))
					i++;
				if (i >= blueImgBrackets.length)
					i = blueImgBrackets.length - 1;
				g.drawImage(blueImg[i], rec.x, rec.y, null);
			}
		}
	}

	/**
	 * Rysowanie obrazka wejscia z okreslonej lokalizacji
	 * @param g Graphics
	 * @param rec Rectangle
	 */
	private void drawInput(Graphics g, Rectangle rec) {
		g.drawImage(inputImage, rec.x, rec.y, null);
	}

	/**
	 * Wyjcie z trybu klasyfikacji wiersza
	 *
	 */
	public void setNotShowClassification() {

		showClassification = false;
		repaint();
	}

	/** wejscie do trybu klasyfikacji wiersza*/
	public void setShowClassification() {
		double min, max, pom;
		double distance;

		showClassification = true;
		min = layers.get(0).getMinOutput();
		max = layers.get(0).getMaxOutput();
		for (Layer o : layers) {
			pom = o.getMinOutput();
			if (pom < min)
				min = pom;
			pom = o.getMaxOutput();
			if (pom > max)
				max = pom;

		}
		distance = (max - min) / (6);
		if (distance < 0)
			distance = -distance;
		int i;
		for (i = 0; i < 6; i++) {
			if (i == 0)
				blueImgBrackets[i] = min + distance;
			else
				blueImgBrackets[i] = blueImgBrackets[i - 1] + distance;
		}
	}

	/**
	 *  Policzenie rozmieszczenia perceptronow na panelu
	 */
	private void calculate() {
		Perceptron p;
		ListIterator<Perceptron> it;
		int i, j;
		/* przesuniecia(w pikslach) przy przechodzeniu miedzy wierszami i kolumnami*/
		int stepRow, stepColumn; 
		/* wspolrzedne(pikslowe) reozpoczecia przechodzenia miedzy wierszami lub kolumnami*/
		int zeroRow, zeroColumn; 
		/*biezace wspolrzedne*/
		int currentX, currentY;

		/* w wypadku gdy perceptrono w pierwszej warstwie ejst za duzo - powiekszenie panelu*/
		if ((inputFoo.perceptrons.size() * IMAGEY + 100) > getHeight()) {
			setPreferredSize(new Dimension(DimensionX, inputFoo.perceptrons.size() * IMAGEY + 100));
		}
		/*rozmiary panelu*/
		int sizeX = getWidth();
		int sizeY = getHeight();
		
		/*incjalizacja zmiennych*/
		i = 0; 
		j = 0;
		zeroRow = LEFT_MARGIN;
		stepRow = (sizeX - zeroRow) / (networkStructure.length + 1);
		stepColumn = (sizeY - 50) / (networkStructure[i] + 2);
		zeroColumn = (sizeY - 50) / 2 - stepColumn * (networkStructure[i] / 2);


		
		/* obliczenia dla warstwy wejsciowej*/
		it = inputFoo.perceptrons();
		currentX = zeroRow;
		currentY = zeroColumn;	
		while (it.hasNext()) {
			p = it.next();
			points.put(p, new Rectangle(currentX, currentY, IMAGEX, IMAGEY));
			j = j + 1;
			currentY += stepColumn;
		}
		currentX += stepRow;
		i++;
		
		/*obliczenia dla neuronow wlasciwych*/
		for (Layer o : layers) {
			it = o.perceptrons();
			j = 0;
			stepColumn = (sizeY - 50) / (networkStructure[i] + 2);
			zeroColumn = (sizeY - 50) / 2 - stepColumn * (networkStructure[i] / 2);
			currentY = zeroColumn;
			while (it.hasNext()) {
				p = it.next();
				points.put(p, new Rectangle(currentX, currentY, IMAGEX, IMAGEY));
				j = j + 1;
				currentY += stepColumn;
			}
			currentX += stepRow;
			i = i + 1;
		}
	}

	public void mouseClicked(MouseEvent e) {
		;
	}

	public void mouseEntered(MouseEvent e) {
		;
	}

	public void mouseExited(MouseEvent e) {
		/* opuszczenie obszaru panelu */
		zoomedPerceptron = null;
		pictures.clear();
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		/* sprawdzene czy wybrano neuron */
		pictures.clear();
		for (Perceptron p : points.keySet()) {
			if (points.get(p).contains(e.getPoint())) {
				if (inputFoo.perceptrons.contains(p))
					zoomedPerceptron = null;
				else {
					pictures.put(p, orangeImg);
					/* dac info o obrazku */
					zoomedPerceptron = p;
					repaint();
				}
				return;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		;
	}

}

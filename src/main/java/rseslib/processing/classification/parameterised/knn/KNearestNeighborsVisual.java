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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

import javax.swing.JPanel;

import rseslib.processing.classification.VisualClassifier;
import rseslib.structure.attribute.Header;
import rseslib.structure.attribute.NominalAttribute;
import rseslib.structure.data.DoubleData;
import rseslib.structure.metric.Neighbour;
import rseslib.structure.table.DoubleDataTable;
import rseslib.system.PropertyConfigurationException;
import rseslib.system.progress.Progress;

/*
 * Extension of k-nearest-neighbors classifier adding visualization.
 * 
 * @author		Lukasz Kosson
 */
public class KNearestNeighborsVisual extends KNearestNeighbors implements VisualClassifier
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;

	private static int[] startcolors = new int[] { 0, 255, 255*256, 128, 128*256, 128*256+128, 128*256+255, 255*256+128 };

    private Random rnd = new Random(System.currentTimeMillis());
    private Random rnd_clas = new Random(System.currentTimeMillis());
    private JPanel pnl;
    private JPanel pnl_clas;
    private KNNPainter painter;
    private KNNPainter painter_clas;
    private double avg;
    private String strLegend = "";
	private Hashtable<Double, Integer> htCols = new Hashtable<Double, Integer>();
	
	public KNearestNeighborsVisual(Properties prop, DoubleDataTable trainTable, Progress prog) throws PropertyConfigurationException, InterruptedException
	{
		super(prop, trainTable, prog);
		initVisualization();
	}
	
	private void initVisualization()
	{
        int cnt = m_TransformedTrainTable.noOfObjects();
        avg = 0.00;
		int dec = -1;
		

    	for (DoubleData v1 : m_TransformedTrainTable.getDataObjects())
        {
        	if (dec == -1) dec = v1.attributes().decision();
        	if (!htCols.containsKey(v1.get(dec)))
        	{
        		int p = htCols.size();
        		if (p<startcolors.length)
        			htCols.put(v1.get(dec), startcolors[p]);
        		else
        			htCols.put(v1.get(dec), rnd.nextInt());
        	}
        	
        	double partsum = 0.00;
            for (DoubleData v2 : m_TransformedTrainTable.getDataObjects())
            {
            	partsum += m_Metric.dist(v1, v2);
            }
            avg += partsum / cnt;
        }
        avg /= cnt;

        strLegend = "<br><b>DECISION CLASSES:</b><br>";
        for (Double key : htCols.keySet())
        {
			String name = NominalAttribute.stringValue(key);
			int color = htCols.get(key);
			int cr = color % 256;
			int cg = (color >> 8) % 256;
			int cb = (color >> 16) % 256; 
			String hexColor = toHex(cr) + toHex(cg) + toHex(cb);
			strLegend += "<font color=#" + hexColor + ">" + name + "</font><br>";
        }
	}

	private String toHex(int val)
	{
		return ("" + "0123456789ABCDEF".charAt(val>>4)) + ("" + "0123456789ABCDEF".charAt(val%16));
	}
	
	/**
	 * Writes this object.
	 *
	 * @param out			Output for writing.
	 * @throws IOException	if an I/O error has occured.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException
	{
	}

	/**
	 * Reads this object.
	 *
	 * @param out			Output for writing.
	 * @throws IOException	if an I/O error has occured.
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
	    rnd = new Random(System.currentTimeMillis());
	    rnd_clas = new Random(System.currentTimeMillis());
	    strLegend = "";	    
		htCols = new Hashtable<Double, Integer>();
		initVisualization();
	}

	public void draw(JPanel canvas)
	{
		if (canvas.equals(pnl))
			return;
		pnl = canvas;
		if(painter != null)
			painter.stopThread();
		painter = new KNNPainter(m_OriginalData, m_TransformedTrainTable.getDataObjects(), m_Metric, rnd, htCols, avg, strLegend);
		painter.draw(canvas);
	}

	public void drawClassify(JPanel canvas, DoubleData obj)
	{
		try
		{
			DoubleData orig = obj;
	        if (m_Transformer!=null)
	        	obj = m_Transformer.transformToNew(obj);
			Neighbour[] n = m_VicinityProvider.getVicinity(obj, getIntProperty(K_PROPERTY_NAME));
			int noOfZeroDist = 0;
			if (n.length > 1 && n[1].dist() == 0.0) {
				noOfZeroDist = 2;
				for(; noOfZeroDist < n.length && n[noOfZeroDist].dist() == 0.0; ++noOfZeroDist);
				if (noOfZeroDist < n.length)
					--noOfZeroDist;
				Neighbour[] withoutZero = new Neighbour[n.length-noOfZeroDist];
				for (int i = 1; i < withoutZero.length; ++i)
					withoutZero[i] = n[i + noOfZeroDist];
				n = withoutZero;
			}
		    if (!canvas.equals(pnl_clas)) {
		    	if(painter_clas != null)
		    		painter_clas.stopThread();
		    	painter_clas = null;
		    	pnl_clas = canvas;
		    }
			if (painter_clas == null)
				painter_clas = new KNNPainter(m_OriginalData, m_TransformedTrainTable.getDataObjects(), m_Metric, rnd_clas, htCols, avg, strLegend);
			double[] nWeights = null;
			double[] decDistr;
			if	(noOfZeroDist > 0)
				decDistr = classifyWithDistributedDecision(orig);
			else
			{
				nWeights = new double[n.length];
				decDistr = getDistributedDecisionAndVotingWeights(obj, n, nWeights);
				double sum = 0;
				for (double v : nWeights)
					sum += v;
				if (sum > 0.0)
					for (int i = 0; i < nWeights.length; ++i)
						nWeights[i] /= sum;
			}
			double sum = 0;
			for (double v : decDistr)
				sum += v;
			if (sum > 0)
				for (int d = 0; d < decDistr.length; ++d)
					decDistr[d] /= sum;
			painter_clas.drawClassify(canvas, obj, orig, n, decDistr, nWeights, getProperty(VOTING_PROPERTY_NAME));
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
	}

	public Header attributes() 
	{
		return m_OriginalData.get(0).attributes();
	}
}

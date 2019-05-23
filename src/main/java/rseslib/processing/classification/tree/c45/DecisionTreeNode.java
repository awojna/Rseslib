/*
 * Copyright (C) 2002 - 2019 The Rseslib Contributors
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


package rseslib.processing.classification.tree.c45;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import rseslib.structure.attribute.*;
import rseslib.structure.data.DoubleData;
import rseslib.structure.data.DoubleDataWithDecision;
import rseslib.structure.function.intval.Discrimination;
import rseslib.structure.vector.Vector;
import rseslib.system.Report;
import rseslib.processing.classification.tree.c45.DiscriminationProvider;

/**
 * The node in the decision tree.
 *
 * @author      Arkadiusz Wojna
 */
public class DecisionTreeNode implements Serializable
{
    /** Serialization version. */
	private static final long serialVersionUID = 1L;
	
	/** Information about the decision. */
    NominalAttribute m_DecisionAttribute = null;
    /** Statistics of the decision distribution for this node. */
    Vector m_DecisionVector = null;
    /** Label of the decision for this node. */
    double m_Decision;
	/** Childer (sub-nodes) of this node. If node is a leaf than null. */
	DecisionTreeNode m_Children[] = null;
    /** Determines to which sub-node the object should be sent. */
    Discrimination m_BranchSelector = null;

    public DecisionTreeNode(Collection<DoubleData> dataObjects, Header hdr, DiscriminationProvider discrProv, double defaultDec)
    {
        m_DecisionAttribute = hdr.nominalDecisionAttribute();
        m_DecisionVector = new Vector(m_DecisionAttribute.noOfValues());
        for (DoubleData dObj : dataObjects)
            m_DecisionVector.increment(m_DecisionAttribute.localValueCode(((DoubleDataWithDecision)dObj).getDecision()));
        int best = 0;
        int different = 0;
        for (int dec = 0; dec < m_DecisionVector.dimension(); dec++)
        {
            if (m_DecisionVector.get(dec)>m_DecisionVector.get(best)) best = dec;
            if (m_DecisionVector.get(dec)>0) different++;
        }
        if (different==0) m_Decision = defaultDec;
        else m_Decision = m_DecisionAttribute.globalValueCode(best);
        if (different > 1) m_BranchSelector = discrProv.getDiscrimination(dataObjects, hdr);
        if (m_BranchSelector!=null)
        {
            ArrayList<DoubleData>[] childrenSets = new ArrayList[m_BranchSelector.noOfValues()];
            for (int child = 0; child < childrenSets.length; child++)
                childrenSets[child] = new ArrayList<DoubleData>();
            for (DoubleData dObj : dataObjects)
            {
                int branch = m_BranchSelector.intValue(dObj);
                if (branch>=0) childrenSets[branch].add(dObj);
            }
            m_Children = new DecisionTreeNode[childrenSets.length];
            for (int child = 0; child < m_Children.length; child++)
                m_Children[child] = new DecisionTreeNode(childrenSets[child], hdr, discrProv, m_Decision);
        }
    }

    /**
     * Writes this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	out.writeObject(m_BranchSelector);
    	out.writeObject(m_DecisionAttribute);
    	out.writeObject(m_DecisionVector);
    	out.writeObject(NominalAttribute.stringValue(m_Decision));
    	out.writeObject(m_Children);
    }

    /**
     * Reads this object.
     *
     * @param out			Output for writing.
     * @throws IOException	if an I/O error has occured.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	m_BranchSelector = (Discrimination)in.readObject();
    	m_DecisionAttribute = (NominalAttribute)in.readObject();
    	m_DecisionVector = (Vector)in.readObject();
    	m_Decision = m_DecisionAttribute.globalValueCode((String)in.readObject());
    	m_Children = (DecisionTreeNode[])in.readObject();
    }

    /**
     * Prunes this subtree and returns the number
     * of correctly classified objects from a given set.
     *
     * @param dataObject	Objects used to validate and prune this subtree.
     * @return    			Number of correctly classified objects from dataObjects. 
     */
    public int prune(Collection<DoubleData> dataObjects)
    {
    	int correct = 0;
    	if (isLeaf())
    	{
    		for (DoubleData dObj : dataObjects)
    			if (((DoubleDataWithDecision)dObj).getDecision()==m_Decision)
    				correct++;
    	}
    	else
    	{
	        ArrayList<DoubleData> childrenSets[] = new ArrayList[m_Children.length];
	        for (int child = 0; child < childrenSets.length; child++)
	        	childrenSets[child] = new ArrayList<DoubleData>();
	        for (DoubleData dObj : dataObjects)
	        {
	            int branch = m_BranchSelector.intValue(dObj);
	            if (branch>=0) childrenSets[branch].add(dObj);
	        }
	    	int leafsCorrect = 0;	
	        for (int child = 0; child < m_Children.length; child++)
	        	leafsCorrect += m_Children[child].prune(childrenSets[child]);
	        for (DecisionTreeNode child : m_Children)
	            if (!child.isLeaf()) return leafsCorrect;
    		for (DoubleData dObj : dataObjects)
    			if (((DoubleDataWithDecision)dObj).getDecision()==m_Decision)
    				correct++;
    		if (correct >= leafsCorrect) cutTree();
    		else correct = leafsCorrect;
    	}
    	return correct;
    }

    /**
     * Changes the decision in this node.
     * 
     * @param newDec New decision to be set.
     */
    public void changeDecision(double newDec)
    {
    	m_Decision = newDec;
    }
    
    /**
     * Returns classification for object <code>obj</code>.
     * Classification is represented by decision distribution.
     * @param obj object for which classification is done.
     * @return classification for object <code>obj</code>.
     */
    public double classify(DoubleData obj)
    {
        // If the node is a leaf than its decision distribution is returned
        if (isLeaf()) return m_Decision;
        else
        {
            int val = m_BranchSelector.intValue(obj);
            /* If the test determines one node
             * then its decision distribution is returned */
            if (val>=0) return m_Children[val].classify(obj);
            /* If the test can not determine one node
             * than sum of all decision distributions is returned */
            else
            {
                Vector decVec = new Vector(m_Children[0].classifyWithDecDistribution(obj));
                for (int i=1; i<m_Children.length; i++)
                decVec.add(m_Children[i].classifyWithDecDistribution(obj));
                int bestDec = 0;
                for (int dec = 1; dec < decVec.dimension(); dec++)
                    if (decVec.get(dec) > decVec.get(bestDec)) bestDec = dec;
                return m_DecisionAttribute.globalValueCode(bestDec);
            }
        }
    }

    /**
     * Returns classification for object <code>obj</code>.
     * Classification is represented by decision distribution.
     * @param obj object for which classification is done.
     * @return classification for object <code>obj</code>.
     */
    public Vector classifyWithDecDistribution(DoubleData obj)
    {
        // If the node is a leaf than its decision distribution is returned
        if (isLeaf()) return m_DecisionVector;
        else
        {
            int val = m_BranchSelector.intValue(obj);
            /* If the test determines one node
             * then its decision distribution is returned */
            if (val>=0)
            {
            	Vector res = m_Children[val].classifyWithDecDistribution(obj);
            	for (int i=0; i<res.dimension(); i++)
            		if (res.get(i)!=0) return res;
            	return m_DecisionVector;
            }
            /* If the test can not determine one node
             * than sum of all decision distributions is returned */
            else
            {
                Vector decVec = new Vector(m_Children[0].classifyWithDecDistribution(obj));
                for (int i=1; i<m_Children.length; i++)
                decVec.add(m_Children[i].classifyWithDecDistribution(obj));
                return decVec;
            }
        }
    }

    /**
     * Returns true if this node has no childern.
     * @return true if this node has no childern.
     */
    public boolean isLeaf()
    {
        return (m_Children==null) || (m_Children.length==0);
    }

    /**
     * Returns depth of the tree beginning from this node.
     * @return depth of the tree beginning from this node.
     */
    public int getDepth()
    {
        if (isLeaf()) return 0;
        else
        {
            int max=0;
            for (int i=0;i<m_Children.length;i++)
            {
                int depth=m_Children[i].getDepth();
                if (depth>max) max=depth;
            }
            return 1+max;
        }
    }

    /**
     * Returns number of leaves in subtree starting from this node.
     * @return number of leaves in subtree starting from this node.
     */
    public int getLeaves()
    {
        if (isLeaf()) return 1;
        else
        {
            int sum=0;
            for (int i=0;i<m_Children.length;i++)
            sum+=m_Children[i].getLeaves();
            return sum;
        }
    }

    /**
     * Returns the index of the branch matching the object.
     *
     * @param obj Object to be tested.
     * @return    The index of the branch matching the object.
     */
    public int branch(DoubleData obj)
    {
    	return m_BranchSelector.intValue(obj);
    }

    /**
     * Returns the number of branches.
     *
     * @return    The number of branches.
     */
    public int noOfBranches()
    {
    	return m_Children.length;
    }

    /**
     * Returns the subnode from a given branch.
     *
     * @param branchIndex The index of a branch.
     * @return    		  The subnode from the branch.
     */
    public DecisionTreeNode subnode(int branchIndex)
    {
      return m_Children[branchIndex];
    }

    /**
     * Cuts the subtree at this node and converts it to a leaf node.
     */
    public void cutTree()
    {
    	m_Children = null;
    	m_BranchSelector = null;
    }

    /**
     * Returns the decision distribution in this node.
     *
     * @return The decision distribution in this node.
     */
    public Vector getDecisionVector()
    {
        return m_DecisionVector;
    }

    /**
     * Returns the decision for this node.
     *
     * @return The decision for this node.
     */
    public String getDecisionLabel()
    {
        return NominalAttribute.stringValue(m_Decision);
    }

    /**
     * Outputs a tree at a given level.
     *
     * @param level The level at which the tree is to be printed.
     * @return      The description of a tree at a given level.
     */
    public String toString(int level)
    {
        StringBuffer text = new StringBuffer();
        if (isLeaf())
        {
            int bestDec = 0;
            for (int dec = 1; dec < m_DecisionVector.dimension(); dec++)
                if (m_DecisionVector.get(dec) > m_DecisionVector.get(bestDec))
                    bestDec = dec;
            text.append(": "+NominalAttribute.stringValue(m_DecisionAttribute.globalValueCode(bestDec)));
        }
        else
        {
            for (int child = 0; child < m_Children.length; child++)
            {
                text.append(Report.lineSeparator);
                for (int i = 0; i < level; i++) text.append("|  ");
                text.append(m_BranchSelector.toString(child));
                text.append(m_Children[child].toString(level + 1));
            }
      }
      return text.toString();
    }
}

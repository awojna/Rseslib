/*
 * Copyright (C) 2002 - 2025 The Rseslib Contributors
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


package rseslib.processing.classification.rules.roughset;
import javax.swing.*;
import java.awt.*;


   /**
    *  Extension of JPanel with vertical ordering of elements but different
    *  form GridLayout ordering 
    *  @author Krzysztof Niemkiewicz
    */

public class ColumnPanel extends JPanel{
    
	private static final long serialVersionUID = -9136989269080945298L;
JPanel last;

    /**
     * Main constructor
     */
    public ColumnPanel() {
        super(new BorderLayout());
        last=this;
    }

    /**
     * Only method, used to add new component below all other components added before
     * @param comp component to be added
     * @return the same component
     */
    
    public Component add(Component comp){
        if (comp==null){return null;};
        try{
        last.add(comp,BorderLayout.NORTH);
        }catch(UnsupportedOperationException e){};
        JPanel n=new JPanel(new BorderLayout());
        try {
            last.add(n,BorderLayout.CENTER);
        } catch(UnsupportedOperationException e) {}
        last=n;
        return comp;
    }
 
   
    /**
     * Unsupported operation
     * @throws UnsupportedOperationException
     */
    public void add(Component comp,Object constraints){
        super.add(comp,constraints);
        throw new UnsupportedOperationException("This operation isn't supported");
    }
}
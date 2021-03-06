/*
 * Represents a family of graphs given by a union of SmallGraphs.
 *
 * $Id$
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.smallgraph;

import java.util.Vector;

public class UnionFamily extends Family{

    Vector<SmallGraph> subfamilies;

    /**
     * Creates a new UnionFamily without Graphs
     */
    public UnionFamily(){
        super();
        subfamilies = null;
    }

    /**
     * Builds a new UnionFamily containing the complement
     * @return a new instance of UnionFamily containing the complement of this.
     */
    public UnionFamily buildComplement() {
        UnionFamily c = (UnionFamily) super.buildComplement();

        if (getSubfamilies() != null) {
            c.subfamilies = (Vector<SmallGraph>) getSubfamilies().clone();
            for (int i = 0; i < c.subfamilies.size(); i++) {
                c.subfamilies.setElementAt(c.subfamilies.elementAt(i).getComplement(), i);
            }
        }

        return c;
    }


    /**
     * Adds subfamily <tt>subf</tt> to UnionFamily
     *
     * @param subf subfamily which should be added to UnionFamily
     */
    public void addSubfamily(SmallGraph subf){
        if (subfamilies == null)
            subfamilies = new Vector<>(2,2);
        subfamilies.addElement(subf);
    }

    /**
     *
     * @return Vector subfamilies
     */
    public Vector<SmallGraph> getSubfamilies(){
        return subfamilies;
    }

    /**
     *
     * @return string representation of this UnionFamily
     */
    public String toString(){
        int i;
        String s = "Name: "+getName()+"\nSubfamilies: ";
        for (i=0; i<subfamilies.size(); i++)
            s+=(subfamilies.elementAt(i)).getName()+"; ";
        s+="\nLink: "+link+"\nComplement: "+complement.getName();
        return s;
    }
}
    
/* EOF */

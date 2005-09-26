/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2005
 * Copyright (C) CNR Pisa, 2005
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl;

import java.lang.ClassNotFoundException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information about those
 * objects.
 *
 * NOTE(JE): hashtabale are indexed by URI.
 * This is strange, but there might be a reason
 * 
 * @author J�r�me Euzenat, David Loup, Rapha�l Troncy
 * @version $Id$
 */

public class BasicAlignment implements Alignment {
    public void accept(AlignmentVisitor visitor) throws AlignmentException {
	visitor.visit(this);
    }

    /* Set to true for rejecting the use of deprecated (non deterministic) primitives */
    private boolean STRICT_IMPLEMENTATION = false;

    protected int debug = 0;

    protected String level = "0";

    protected String type = "**";

    protected OWLOntology onto1 = null;

    protected OWLOntology onto2 = null;

    protected Hashtable hash1 = null;

    protected Hashtable hash2 = null;

    /**
     * This is the URI of the place from which the ontology has been loaded!
     * This is NOT the Ontology URI which can be obtained by
     * onto1.getLogicalURI();
     */
    protected URI uri1 = null;

    protected URI uri2 = null;

    public BasicAlignment() {
	hash1 = new Hashtable();
	hash2 = new Hashtable();
    }

    // Note: protected is a problem outside of package
    //  but everything else is public
    // JE[15/5/2005]: This does not seem to be a problem indeed
    protected void init(OWLOntology onto1, OWLOntology onto2) {
	this.onto1 = onto1;
	this.onto2 = onto2;
    }

    public int nbCells() {
	//-return hash1.size();
	int sum = 0;
	for ( Enumeration e = hash1.elements(); e.hasMoreElements(); ) {
	    sum = sum + ((HashSet)e.nextElement()).size();
	}
	return sum;
    }

    /** Alignment methods * */
    public Object getOntology1() {
	return onto1;
    };

    public Object getOntology2() {
	return onto2;
    };

    public void setOntology1(Object ontology) throws AlignmentException {
	if ( ontology instanceof OWLOntology ){
	    onto1 = (OWLOntology) ontology;
	} else {
	    throw new AlignmentException("setOntology1: arguments must be OWLOntology");
	};
    };

    public void setOntology2(Object ontology) throws AlignmentException {
	if ( ontology instanceof OWLOntology ){
	    onto2 = (OWLOntology) ontology;
	} else {
	    throw new AlignmentException("setOntology2: arguments must be OWLOntology");
	};
    };

    public void setType(String type) { this.type = type; };

    public String getType() { return type; };

    public void setLevel(String level) { this.level = level; };

    public String getLevel() { return level; };

    public URI getFile1() { return uri1; };

    public void setFile1(URI u) { uri1 = u; };

    public URI getFile2() { return uri2; };

    public void setFile2(URI u) { uri2 = u; };

    public Enumeration getElements() { 
	//-return hash1.elements(); 
	return new MEnumeration( hash1 );
    }

    public ArrayList getArrayElements() {
	ArrayList array = new ArrayList();
	for (Enumeration e = getElements(); e.hasMoreElements(); ) {
	    array.add( (BasicCell)e.nextElement() );
	}
	return array;
    }

    /** Cell methods **/
    //-public Cell addAlignCell(Object ob1, Object ob2, String relation,
    //			     double measure) throws AlignmentException {
    //	if ( !( ob1 instanceof OWLEntity && ob2 instanceof OWLEntity ) )
    //	    throw new AlignmentException("addAlignCell: arguments must be OWLEntities");
    //	    Cell cell = (Cell) new BasicCell((OWLEntity)ob1, (OWLEntity)ob2,
    //					     relation, measure);
    //	    addCell( (OWLEntity)ob1, (OWLEntity)ob2, cell );
    //	    return cell;
    //};
    public Cell addAlignCell(Object ob1, Object ob2, String relation, double measure) throws AlignmentException {
 
        if ( !( ob1 instanceof OWLEntity && ob2 instanceof OWLEntity ) )
            throw new AlignmentException("addAlignCell: arguments must be OWLEntities");
        try {
            Cell cell = (Cell) new BasicCell((OWLEntity) ob1, (OWLEntity) ob2, relation, measure);
            HashSet s1 = (HashSet)hash1.get((Object)(((OWLEntity)ob1).getURI()));
            if (s1 == null) {
                s1 = new HashSet();
                hash1.put((Object)(((OWLEntity)ob1).getURI()),s1);
            }
            s1.add(cell);
            HashSet s2 = (HashSet)hash2.get((Object)(((OWLEntity)ob2).getURI()));
            if (s2 == null) {
                s2 = new HashSet(); 
                hash2.put((Object)(((OWLEntity)ob2).getURI()),s2);
            } 
            s2.add(cell);
            return cell;
        } catch (OWLException e) {
            throw new AlignmentException("getURI problem", e);
        }
    };

    public Cell addAlignCell(Object ob1, Object ob2) throws AlignmentException {
	return addAlignCell( ob1, ob2, "=", 1. );
    }

    //-
    //public void addCell( OWLEntity ob1, OWLEntity ob2, Cell cell ) throws AlignmentException {
    //	try {
    //	    hash1.put((Object)(((OWLEntity)ob1).getURI()), cell);
    //	    hash2.put((Object)(((OWLEntity)ob2).getURI()), cell);
    //	} catch (OWLException e) {
    //	    throw new AlignmentException("getURI problem", e);
    //	}
    //}
    protected void addCell( Cell c ) throws AlignmentException {
        try {
            HashSet s1 = (HashSet)hash1.get(((OWLEntity)c.getObject1()).getURI());
	    if( s1 != null ){
		s1.add( c ); // I must check that there is no one here
	    } else {
		s1 = new HashSet();
		hash1.put(((OWLEntity)c.getObject1()).getURI(),s1);
		s1.add( c );
	    }

            HashSet s2 = (HashSet)hash2.get(((OWLEntity)c.getObject2()).getURI());
	    if( s2 != null ){
		s2.add( c ); // I must check that there is no one here
	    } else {
		s2 = new HashSet();
		hash2.put(((OWLEntity)c.getObject2()).getURI(),s2);
		s2.add( c );
	    }

        } catch (OWLException ex) {
            throw new AlignmentException("getURI problem", ex);
        }
    }

    // Beware, the catch applies both for getURI and OWLException
    public Set getAlignCells1(Object ob) throws AlignmentException {
	if ( ob instanceof OWLEntity ){
	    try{ return (HashSet)hash1.get(((OWLEntity)ob).getURI());
	    } catch (Exception e) { return null;}
	} else {
	    throw new AlignmentException("getAlignCells1: argument must be OWLEntity");
	}
    }
    public Set getAlignCells2(Object ob) throws AlignmentException {
	if ( ob instanceof OWLEntity ){
	    try{ return (HashSet)hash2.get(((OWLEntity)ob).getURI());
	    } catch (Exception e) { return null;}
	} else {
	    throw new AlignmentException("getAlignCells1: argument must be OWLEntity");
	}
    }

    // Deprecated: implement as the one retrieving the highest strength correspondence (
    public Cell getAlignCell1(Object ob) throws AlignmentException {
	if ( STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("getAlignCell1: deprecated (use getAlignCells1 instead)");
	} else {
	    if ( ob instanceof OWLEntity ){
		try { 
		    Set s2 = (Set)hash1.get(((OWLEntity) ob).getURI());
		    Cell bestCell = null;
		    double bestStrength = 0.;
		    if ( s2 != null ) {
			for( Iterator it2 = s2.iterator(); it2.hasNext(); ){
			    Cell c = (Cell)it2.next();
			    double val = c.getStrength();
			    if ( val > bestStrength ) {
				bestStrength = val;
				bestCell = c;
			    }
			}
		    }
		    return bestCell;
		} catch (OWLException e) { throw new AlignmentException("getURI problem", e); }
	    } else {
		throw new AlignmentException("getAlignCell1: argument must be OWLEntity");
	    }
	}
    }

    public Cell getAlignCell2(Object ob) throws AlignmentException {
	if ( STRICT_IMPLEMENTATION == true ){
	    throw new AlignmentException("getAlignCell2: deprecated (use getAlignCells2 instead)");
	} else {
	    if ( ob instanceof OWLEntity ){
		try { 
		    Set s1 = (Set)hash2.get(((OWLEntity) ob).getURI());
		    Cell bestCell = null;
		    double bestStrength = 0.;
		    if ( s1 != null ){
			for( Iterator it1 = s1.iterator(); it1.hasNext(); ){
			    Cell c = (Cell)it1.next();
			    double val = c.getStrength();
			    if ( val > bestStrength ) {
				bestStrength = val;
				bestCell = c;
			    }
			}
		    }
		    return bestCell;
		} catch (OWLException e) { throw new AlignmentException("getURI problem", e); }
	    } else {
		throw new AlignmentException("getAlignCell2: argument must be OWLEntity");
	    }
	}
    }

    //- Deprecated
    public Object getAlignedObject1(Object ob) throws AlignmentException {
	Cell c = getAlignCell1(ob);
	if (c != null) return c.getObject2();
	else return null;
    };

    //- Deprecated
    public Object getAlignedObject2(Object ob) throws AlignmentException {
	Cell c = getAlignCell2(ob);
	if (c != null) return c.getObject1();
	else return null;
    };

    //- Deprecated
    public Relation getAlignedRelation1(Object ob) throws AlignmentException {
	Cell c = getAlignCell1(ob);
	if (c != null) return c.getRelation();
	else return (Relation) null;
    };

    //- Deprecated
    public Relation getAlignedRelation2(Object ob) throws AlignmentException {
	Cell c = getAlignCell2(ob);
	if (c != null) return c.getRelation();
	else return (Relation) null;
    };

    //- Deprecated
    public double getAlignedStrength1(Object ob) throws AlignmentException {
	Cell c = getAlignCell1(ob);
	if (c != null) return c.getStrength();
	else return 0;
    };

    //- Deprecated
    public double getAlignedStrength2(Object ob) throws AlignmentException {
	Cell c = getAlignCell2(ob);
	if (c != null) return c.getStrength();
	else return 0;
    };

    //-
    //public void removeAlignCell(Cell c) throws AlignmentException {
    //try {
    //    HashSet s1 = (HashSet)hash1.get(((OWLEntity)c.getObject1()).getURI());
    //    HashSet s2 = (HashSet)hash2.get(((OWLEntity)c.getObject2()).getURI());
    //    s1.remove(c);
    //    s2.remove(c);
    //} catch (OWLException ex) {
    //    throw new AlignmentException("getURI problem", ex);
    //}
    //}

    public void removeAlignCell(Cell c) throws AlignmentException {
        try {
            HashSet s1 = (HashSet)hash1.get(((OWLEntity)c.getObject1()).getURI());
            HashSet s2 = (HashSet)hash2.get(((OWLEntity)c.getObject2()).getURI());
            s1.remove(c);
            s2.remove(c);
            if (s1.isEmpty())
                hash1.remove(((OWLEntity) c.getObject1()).getURI());
            if (s2.isEmpty())
                hash2.remove(((OWLEntity) c.getObject2()).getURI());
        } catch (OWLException ex) {
            throw new AlignmentException("getURI problem", ex);
        }
    }

    /***************************************************************************
     * The cut function suppresses from an alignment all the cell over a
     * particulat threshold
     **************************************************************************/
    //-public void cut2(double threshold) throws AlignmentException {
    //	for (Enumeration e = hash1.keys(); e.hasMoreElements();) {
    //	    Cell c = (Cell) hash1.get(e.nextElement());
    //	    if (c.getStrength() < threshold) {
    //		// Beware, this suppresses all cells with these keys
    //		// There is only one of them
    //		try {
    //		    //Raph:
    //		    //removeAlignCell( c );
    //		    hash1.remove(((OWLEntity) c.getObject1()).getURI());
    //		    hash2.remove(((OWLEntity) c.getObject2()).getURI());
    //		} catch (OWLException ex) {
    //		    throw new AlignmentException("getURI problem", ex);
    //		}
    //	    }
    //	} //end for
    //   };
    public void cut2(double threshold) throws AlignmentException {
	for (Enumeration e = getElements(); e.hasMoreElements(); ) {
	    Cell c = (Cell)e.nextElement();
	    if (c.getStrength() < threshold)
		removeAlignCell( c );
	}
    }


    /***************************************************************************
     * Default cut implementation
     * For compatibility with API until version 1.1
     **************************************************************************/
    public void cut( double threshold ) throws AlignmentException {
	cut( "hard", threshold );
    }

    /***************************************************************************
     * Cut refinement :
     * - above n (hard)
     * - above n under the best ()
     * - getting the n% better (perc)
     * - getting the under n% of the best (prop)
     * - getting the n best values
     **************************************************************************/
    //-public void cut( String method, double threshold ) throws AlignmentException {
    //	// Check that threshold is a percent
    //	if ( threshold > 1. || threshold < 0. )
    //	    throw new AlignmentException( "Not a percentage or threshold : "+threshold );
    //	// Create a sorted list of cells
    //	// Raph: this will not work anymore
    //	List buffer = new ArrayList( hash1.values() );
    //	Collections.sort( buffer );
    //	int size = buffer.size();
    //	boolean found = false;
    //	int i = 0;
    //	// Depending on the method, find the limit
    //	if ( method.equals("hard") ){
    //	    for( i=0; i < size && !found ; i++ ) {
    //		if ( ((Cell)buffer.get(i)).getStrength() <= threshold ) found = true;
    //	    }
    //	} else if ( method.equals("span") ){
    //	    double max = ((Cell)buffer.get(0)).getStrength() -threshold;
    //	    for( i=0; i < size && !found ; i++ ) {
    //		if ( ((Cell)buffer.get(i)).getStrength() <= max ) found = true;
    //	    }
    //	} else if ( method.equals("prop") ){
    //	    double max = ((Cell)buffer.get(0)).getStrength() * (1-threshold);
    //	    for( i=0; i < size && !found ; i++ ) {
    //		if ( ((Cell)buffer.get(i)).getStrength() <= max ) found = true;
    //	    }
    //	} else if ( method.equals("perc") ){
    //	    i = (new Double(size*threshold)).intValue();
    //	} else if ( method.equals("best") ){
    //	    i=(new Double(threshold)).intValue();
    //	} else throw new AlignmentException( "Not a cut specification : "+method );
    //	// Flush the structure
    //	for( size-- ; size > i ; size-- ) buffer.remove(size);
    //	// Introduce the result back in the structure
    //	size = i;
    //	hash1.clear();
    //	hash2.clear();
    //	try {
    //	    for( i = 0; i < size; i++ ) {
    //		Cell c = (Cell)buffer.get(i);
    //		hash1.put((Object) (((OWLEntity)c.getObject1()).getURI()), c);
    //		hash2.put((Object) (((OWLEntity)c.getObject2()).getURI()), c);
    //	    }
    //	} catch (OWLException e) {
    //	    throw new AlignmentException("getURI problem", e);
    //	}
    //    };
    public void cut( String method, double threshold ) throws AlignmentException
    {
	// Check that threshold is a percent
	if ( threshold > 1. || threshold < 0. )
	    throw new AlignmentException( "Not a percentage or threshold : "+threshold );
	// Create a sorted list of cells
	// For sure with sorted lists, we could certainly do far better
	// Raph: this will not work anymore
	//List buffer = new ArrayList( hash1.values() );
	List buffer = getArrayElements();
	Collections.sort( buffer );
	int size = buffer.size();
	boolean found = false;
	int i = 0;
	// Depending on the method, find the limit
	if ( method.equals("hard") ){
	    for( i=0; i < size && !found ; i++ ) {
		if ( ((Cell)buffer.get(i)).getStrength() <= threshold ) found = true;
	    }
	} else if ( method.equals("span") ){
	    double max = ((Cell)buffer.get(0)).getStrength() -threshold;
	    for( i=0; i < size && !found ; i++ ) {
		if ( ((Cell)buffer.get(i)).getStrength() <= max ) found = true;
	    }
	} else if ( method.equals("prop") ){
	    double max = ((Cell)buffer.get(0)).getStrength() * (1-threshold);
	    for( i=0; i < size && !found ; i++ ) {
		if ( ((Cell)buffer.get(i)).getStrength() <= max ) found = true;
	    }
	} else if ( method.equals("perc") ){
	    i = (new Double(size*threshold)).intValue();
	} else if ( method.equals("best") ){
	    i=(new Double(threshold)).intValue();
	} else throw new AlignmentException( "Not a cut specification : "+method );
	// Flush the structure
	for( size-- ; size > i ; size-- ) buffer.remove(size);
	// Introduce the result back in the structure
	size = i;
	hash1.clear();
	hash2.clear();
	for( i = 0; i < size; i++ ) {
	    addCell( (Cell)buffer.get(i) );
	    // OLD Stuff pre-v2
	    //hash1.put((Object) (((OWLEntity)c.getObject1()).getURI()), c);
	    //hash2.put((Object) (((OWLEntity)c.getObject2()).getURI()), c);
	}
    };

    /***************************************************************************
     * The harden function acts like threshold but put all weights to 1.
     **************************************************************************/
    //-public void harden(double threshold) throws AlignmentException {
    //	for (Enumeration e = hash1.keys(); e.hasMoreElements();) {
    //	    Cell c = (Cell) hash1.get(e.nextElement());
    //	    if (c.getStrength() < threshold) {
    //		// Beware, this suppresses all cells with these keys
    //		// There is only one of them
    //		try {
    //		    hash1.remove(((OWLEntity) c.getObject1()).getURI());
    //		    hash2.remove(((OWLEntity) c.getObject2()).getURI());
    //		} catch (OWLException ex) {
    //		    throw new AlignmentException("getURI problem", ex);
    //		}
    //	    } else {
    //		c.setStrength(1.);
    //	    }
    //	} //end for
    //    };
    public void harden(double threshold) throws AlignmentException {
	for (Enumeration e = getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    if (c.getStrength() < threshold)
		removeAlignCell( c );
	    else
		c.setStrength(1.);
	}
    }

   /**
     * The second alignment is meet with the first one meaning that for
     * any pair (o, o', n, r) in O and (o, o', n', r) in O' the resulting
     * alignment will contain:
     * ( o, o', meet(n,n'), r)
     * any pair which is in only one alignment is preserved.
     */
    public Alignment meet(Alignment align) throws AlignmentException {
	BasicAlignment result = new BasicAlignment();
	result.init(onto1,onto2);
	return result;
    }

   /**
     * The second alignment is join with the first one meaning that for
     * any pair (o, o', n, r) in O and (o, o', n', r) in O' the resulting
     * alignment will contain:
     * ( o, o", join(n,n'), r)
     * any pair which is in only one alignment is discarded.
     */
    public Alignment join(Alignment align) throws AlignmentException {
	BasicAlignment result = new BasicAlignment();
	result.init(onto1,onto2);
	return result;
    }

    /**
     * The second alignment is composed with the first one meaning that for
     * any pair (o, o', n, r) in O and (o',o", n', r') in O' the resulting
     * alignment will contain:
     * ( o, o", join(n,n'), compose(r, r')) iff compose(r,r') exists.
     */
    public Alignment compose(Alignment align) throws AlignmentException {
	BasicAlignment result = new BasicAlignment();
	result.init(onto1,onto2);
	return result;
    }

    /**
     * A new alignment is created such that for
     * any pair (o, o', n, r) in O the resulting alignment will contain:
     * ( o', o, n, inverse(r)) iff compose(r) exists.
     */
    public void inverse () {
	OWLOntology o = onto1;
	onto1 = onto2;
	onto2 = o;
	// We must inverse getType
	URI u = uri1;
	uri1 = uri2;
	uri2 = u;
	Hashtable h = hash1;
	hash1 = hash2;
	hash2 = h;
	for ( Enumeration e = getElements() ; e.hasMoreElements(); ){
	    ((Cell)e.nextElement()).inverse();
	}
    };

    /** Housekeeping **/
    public void dump(ContentHandler h) {
    };

    /**
     * Incorporate the cells of the alignment into its own alignment. Note: for
     * the moment, this does not copy but really incorporates. So, if hardening
     * or cutting, are applied, then the ingested alignmment will be modified as
     * well.
     */
    //-
    //protected void ingest(Alignment alignment) throws AlignmentException {
    //	for (Enumeration e = alignment.getElements(); e.hasMoreElements();) {
    //	    Cell c = (Cell) e.nextElement();
    //	    try {
    //		hash1.put((Object) ((OWLEntity) c.getObject1()).getURI(), c);
    //		hash2.put((Object) ((OWLEntity) c.getObject2()).getURI(), c);
    //	    } catch (OWLException ex) {
    //		throw new AlignmentException("getURI problem", ex);
    //	    }
    //	}
    //    };
    protected void ingest(Alignment alignment) throws AlignmentException {
	for (Enumeration e = alignment.getElements(); e.hasMoreElements();) {
	    addCell((Cell)e.nextElement());
	};
    }

    /**
     * This should be rewritten in order to generate the axiom ontology instead
     * of printing it! And then use ontology serialization for getting it
     * printed.
     */
    public void render( AlignmentVisitor renderer ) throws AlignmentException {
	accept(renderer);
    }
}

class MEnumeration implements Enumeration {
    private Enumeration set = null; // The enumeration of sets
    private Iterator current = null; // The current set's enumeration

    MEnumeration( Hashtable s ){
	set = s.elements();
	while( set.hasMoreElements() && current == null ){
	    current = ((Set)set.nextElement()).iterator();
	    if( !current.hasNext() ) current = null;
	}
    }
    public boolean hasMoreElements(){
	return ( current != null);
    }
    public Object nextElement(){
	Object val = current.next();
	if( !current.hasNext() ){
	    current = null;
	    while( set.hasMoreElements() && current == null ){
		current = ((Set)set.nextElement()).iterator();
		if( !current.hasNext() ) current = null;
	    }
	}
	return val;
    }
}

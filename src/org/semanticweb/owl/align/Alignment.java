/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2005, 2007-2008
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

package org.semanticweb.owl.align; 

import java.lang.Cloneable;
import java.util.Enumeration;
import java.util.Set;
import java.net.URI;

import org.xml.sax.ContentHandler;

/**
 * Represents an Ontology alignment.
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */


public interface Alignment extends Cloneable
{

    /** Initialize the alignement before using it **/

    public void init( Object onto1, Object onto2 ) throws AlignmentException;

    /** Initialize the alignement before using it, with some ontology caching trick **/

    public void init( Object onto1, Object onto2, Object cache ) throws AlignmentException;

    /** Alignment methods **/

    public void accept( AlignmentVisitor visitor ) throws AlignmentException;
    /**
     * The alignment has reference to the two aligned ontology.
     * All Alignment cells contain firts the entity from the first ontology
     * The alignment is from the first ontology to the second.
     */
    public Object getOntology1();
    public Object getOntology2();
    public URI getOntology1URI() throws AlignmentException;
    public URI getOntology2URI() throws AlignmentException;
    public void setOntology1(Object ontology) throws AlignmentException;
    public void setOntology2(Object ontology) throws AlignmentException;
    /**
     * Alignment type:
     * Currently defined a sa String.
     * This string is supposed to contain two characters: among ?, 1, *, +
     * Can be implemented otherwise
     */
    public void setLevel( String level );
    public String getLevel();
    /**
     * Alignment type:
     * Currently defined a sa String.
     * This string is supposed to contain two characters: among ?, 1, *, +
     * Can be implemented otherwise
     */
    public void setType( String type );
    public String getType();

    /**
     * Alignment type:
     * Currently defined a sa String.
     * This string is supposed to contain two characters: among ?, 1, *, +
     * Can be implemented otherwise
     */
    public void setFile1( URI type );
    public void setFile2( URI type );
    public URI getFile1();
    public URI getFile2();

    /** Cell methods **/
    /**
     * The alignment itself is a set of Alignment cells relating one
     * entity of the firt ontology to an entity of the second one.
     * These cells are indexed within the Alingnment by the URI of the
     * entities in each ontology (with one hashtable for each).
     * In addition to the coupe of entities, the cells contains a
     * qualification of the relation between them (a Relation object)
     * and a quantification of the confidence in the relation (an int).
     */

    /**
     * Cells are created and indexed at once
     */
    public Cell addAlignCell( Object ob1, Object ob, String relation, double measure) throws AlignmentException;
    public Cell addAlignCell( Object ob1, Object ob2) throws AlignmentException;

    /**
     * Cells are retrieved
     * These primitives are deprecated. Use getAlignCells1 and getAlignCells2
     * instead.
     * Reason: this applies only for 1:1 alignments
     * Some implementations might act cleverly (retrieving the best value).
     * Basic implementations may raise the exception
     */
    public Cell getAlignCell1( Object ob ) throws AlignmentException;
    public Cell getAlignCell2( Object ob ) throws AlignmentException;
    /**
     * Each part of the cell can be queried independently.
     * These primitives are deprecated. Use getAlignCells1 and getAlignCells2
     * instead.
     * Reason: this applies only for 1:1 alignments
     * Some implementations might act cleverly (retrieving the best value).
     * Basic implementations may raise the exception
     */
    public Object getAlignedObject1( Object ob ) throws AlignmentException;
    public Object getAlignedObject2( Object ob ) throws AlignmentException;
    public Relation getAlignedRelation1( Object ob ) throws AlignmentException;
    public Relation getAlignedRelation2( Object ob ) throws AlignmentException;
    public double getAlignedStrength1( Object ob ) throws AlignmentException;
    public double getAlignedStrength2( Object ob ) throws AlignmentException;

    /**
     * Cells are retrieved
     * These primitives are deprecated. Use getAlignCells1 and getAlignCells2
     * instead.
     * Reason: this applies only for 1:1 alignments
     * Some implementations might act cleverly (retrieving the best value).
     * Basic implementations may raise the exception
     */
    public Set getAlignCells1( Object ob ) throws AlignmentException;
    public Set getAlignCells2( Object ob ) throws AlignmentException;

    /**
     * Extensions are a way to read and add other information (metadata)
     * to the alignment structure itself.
     */
    public Parameters getExtensions();
    public String getExtension( String uri, String label );
    public void setExtension( String uri, String label, String value );

    public Enumeration getElements();
    public int nbCells();

    public void cut( String method, double threshold ) throws AlignmentException;
    public void cut( double threshold ) throws AlignmentException;
    public void harden( double threshold ) throws AlignmentException;

    /**
     * Algebra of alignment manipulation operations: compose, join, meet.
     */
    public Alignment inverse() throws AlignmentException;
    public Alignment meet(Alignment align) throws AlignmentException;
    public Alignment join(Alignment align) throws AlignmentException;
    public Alignment compose(Alignment align) throws AlignmentException;

    /** Housekeeping **/
    /**
     * The methods for outputing and dispalying alignments are common to
     * all alignment. They depend on the implementation of the similar
     * methods in Cell and Relation.
     */
    /**
     * Dump should be implemented as a method generating SAX events
     * for a SAXHandler provided as input 
     */
    public void dump(ContentHandler h);
    //    public void write( PrintStream writer ) throws IOException, AlignmentException;
    //public void write( PrintWriter writer ) throws IOException, AlignmentException;

    /** Exporting
	The alignments are exported for other purposes.
    */
    public void render( AlignmentVisitor renderer ) throws AlignmentException;

}


/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2004
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

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.PrintStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */


public class RDFRendererVisitor implements AlignmentVisitor
{
    PrintStream writer = null;
    Alignment alignment = null;
    Cell cell = null;

    public RDFRendererVisitor( PrintStream writer ){
	this.writer = writer;
    }

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	writer.print("<?xml version='1.0' encoding='utf-8");
	//	writer.print(writer.getEncoding().toString());
	writer.print("' standalone='no'?>\n");
	writer.print("<!DOCTYPE rdf:RDF SYSTEM \"align.dtd\">\n\n");
	// add date, etc.
	writer.print("<rdf:RDF xmlns='http://exmo.inrialpes.fr/align/1.0'\n         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'>\n");
	writer.print("<Alignment>\n  <xml>yes</xml>\n");
	writer.print("  <level>");
	writer.print( align.getLevel() );
	writer.print("</level>\n  <type>");
	writer.print( align.getType() );
	writer.print("</type>\n  <onto1>");
	try {
	    writer.print( ((OWLOntology)align.getOntology1()).getLogicalURI().toString());
	    writer.print("</onto1>\n  <onto2>");
	    writer.print( ((OWLOntology)align.getOntology2()).getLogicalURI().toString());
	    writer.print("</onto2>\n");
	    if ( align.getFile1() != null )
		writer.print("  <uri1>"+align.getFile1().toString()+"</uri1>\n");
	    if ( align.getFile2() != null )
		writer.print("  <uri2>"+align.getFile2().toString()+"</uri2>\n");
	    writer.print("  <map>\n");
	    
	    for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
		Cell c = (Cell)e.nextElement();
		c.accept( this );
	    } //end for
	} catch (OWLException ex) { throw new AlignmentException( "getURI problem", ex); }
	writer.print("  </map>\n</Alignment>\n");
	writer.print("</rdf:RDF>\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	//OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	try {
	    writer.print("    <Cell");
	    if ( cell.getId() != null ){
		writer.print(" rdf:resource=\"#"+cell.getId()+"\"");
	    }
	    writer.print(">\n      <entity1 rdf:resource='");
	    writer.print( ((OWLEntity)cell.getObject1()).getURI().toString() );
	    writer.print("'/>\n      <entity2 rdf:resource='");
	    writer.print( ((OWLEntity)cell.getObject2()).getURI().toString() );
	    writer.print("'/>\n      <measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>");
	    writer.print( cell.getStrength() );
	    writer.print("</measure>\n      <relation>");
	    cell.getRelation().accept( this );
	    writer.print("</relation>\n    </Cell>\n");
	} catch ( OWLException e) { throw new AlignmentException( "getURI problem", e ); }
    }
    public void visit( Relation rel ) {
	rel.write( writer );
    };
}

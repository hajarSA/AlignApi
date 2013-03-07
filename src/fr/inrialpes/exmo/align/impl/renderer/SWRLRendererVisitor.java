/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2004, 2007-2010, 2012-2013
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

package fr.inrialpes.exmo.align.impl.renderer; 

import java.util.Enumeration;
import java.util.Properties;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.*;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

/**
 * Renders an alignment as a SWRL rule set interpreting
 *.data of the first ontology into the second one.
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */

public class SWRLRendererVisitor extends GenericReflectiveVisitor implements AlignmentVisitor {
    PrintWriter writer = null;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    public SWRLRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
   };

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	if ( align instanceof ObjectAlignment ) {
	    alignment = align;
	} else {
	    try {
		alignment = ObjectAlignment.toObjectAlignment( (URIAlignment)align );
	    } catch ( AlignmentException alex ) {
		throw new AlignmentException("SWRLRenderer: cannot render simple alignment. Need an ObjectAlignment", alex );
	    }
	}
	onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	if ( embedded == false )
	    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	writer.println("<swrlx:Ontology swrlx:name=\"generatedAl\"");
	writer.println("                xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
	writer.println("                xmlns:swrlx=\"http://www.w3.org/2003/11/swrlx#\"");
	writer.println("                xmlns:owlx=\"http://www.w3.org/2003/05/owl-xml\"");
	writer.println("                xmlns:ruleml=\"http://www.w3.org/2003/11/ruleml#\">");
	writer.print("\n  <!-- Generated by fr.inrialpes.exmo.impl.renderer.SWRLRendererVisitor -->\n");
	for ( String[] ext : align.getExtensions() ){
	    writer.print("  <owlx:Annotation><owlx:Documentation>"+ext[1]+": "+ext[2]+"</owlx:Documentation></owlx:Annotation>\n");
	}
	writer.print("\n");
	writer.println("  <owlx:Imports rdf:resource=\""+onto1.getURI()+"\"/>\n");
	for( Cell c : align ){
	    c.accept( this );
	}
	writer.println("</swrlx:Ontology>");
    }

    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
	// default behaviour
	this.cell = cell;
	cell.getRelation().accept( this );
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	// JE: We should send warnings when dataproperties are mapped to individual properties and vice versa...
	Object ob1 = cell.getObject1();
	Object ob2 = cell.getObject2();
	URI uri1;
	URI uri2;
	try {
	    uri1 = onto1.getEntityURI( ob1 );
	    writer.println("  <ruleml:imp>");
	    writer.println("    <ruleml:_body>");
	    if ( onto1.isClass( ob1 ) ){
		writer.println("      <swrl:classAtom>");
		writer.println("        <owllx:Class owllx:name=\""+uri1+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("      </swrl:classAtom>");
	    } else if ( onto1.isDataProperty( ob1 ) ){
		writer.println("      <swrl:datavaluedPropertyAtom swrlx:property=\""+uri1+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      <swrl:datavaluedPropertyAtom>");
	    } else {
		writer.println("      <swrl:individualPropertyAtom swrlx:property=\""+uri1+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      </swrl:individualPropertyAtom>");
	    }
	    writer.println("    </ruleml:_body>");
	    writer.println("    <ruleml:_head>");
	    uri2 = onto2.getEntityURI( ob2 );
	    if ( onto2.isClass( ob2 ) ){
		writer.println("      <swrlx:classAtom>");
		writer.println("        <owllx:Class owllx:name=\""+uri2+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("      </swrl:classAtom>");
	    } else if ( onto2.isDataProperty( ob2 )  ){
		writer.println("      <swrl:datavaluedPropertyAtom swrlx:property=\""+uri2+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      </swrl:datavaluedPropertyAtom>");
	    } else {
		writer.println("      <swrl:individualPropertyAtom swrlx:property=\""+uri2+"\"/>");
		writer.println("        <ruleml:var>x</ruleml:var>");
		writer.println("        <ruleml:var>y</ruleml:var>");
		writer.println("      </swrl:individualPropertyAtom>");
	    }
	    writer.println("    </ruleml:_head>");
	    writer.println("  </ruleml:imp>\n");
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error accessing ontology", owex );
	}
    }

    public void visit( SubsumeRelation rel ){};
    public void visit( SubsumedRelation rel ){};
    public void visit( IncompatRelation rel ){};
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	throw new AlignmentException( "Cannot render generic Relation" );
    }

}

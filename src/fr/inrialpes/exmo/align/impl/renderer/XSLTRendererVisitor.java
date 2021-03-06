/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2004, 2006-2010, 2012-2014
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

import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.rel.*;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

/**
 * Renders an alignment as a XSLT stylesheet transforming 
 *.data of the first ontology into the second one.
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */

public class XSLTRendererVisitor extends GenericReflectiveVisitor implements AlignmentVisitor {
    PrintWriter writer = null;
    Alignment alignment = null;
    Cell cell = null;
    LoadedOntology<Object> onto1 = null;
    LoadedOntology<Object> onto2 = null;
    Hashtable<String,String> namespaces = null;
    int nsrank = 0;
    boolean embedded = false; // if the output is XML embeded in a structure

    public XSLTRendererVisitor( PrintWriter writer ){
	this.writer = writer;
	namespaces = new Hashtable<String,String>();
	namespaces.put( "http://www.w3.org/1999/XSL/Transform", "xsl" );
	namespaces.put( "http://www.w3.org/2002/07/owl#", "owl" );
	namespaces.put( "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf" );
	namespaces.put( "http://www.w3.org/2000/01/rdf-schema#", "rdfs" );
    }

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
    };

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	alignment = align;
	if ( align instanceof ObjectAlignment ) {
	    onto1 = ((ObjectAlignment)align).getOntologyObject1();
	    onto2 = ((ObjectAlignment)align).getOntologyObject2();
	}
	for( Cell c : alignment ){
	    collectURIs( c );
	}
	alignment = align;
	if ( embedded == false )
	    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	writer.println("<xsl:stylesheet version=\"1.0\"");
	for ( Entry<String,String> e : namespaces.entrySet() ) {
	    writer.println("    xmlns:"+e.getValue()+"=\""+e.getKey()+"\"");
	}
	writer.println("  >\n");

	writer.print("  <!-- Generated by fr.inrialpes.exmo.impl.renderer.XSLTRendererVisitor -->\n");
	for ( String[] ext : align.getExtensions() ){
	    String name = ext[1];
	    writer.print("  <!-- "+name+": "+ext[2]+" -->\n");
	}
	writer.print("\n");

	for ( Cell c : alignment ) { c.accept( this ); }

	writer.println("  <!-- Copying the root -->");
	writer.println("  <xsl:template match=\"/\">");
	writer.println("    <xsl:apply-templates/>");
	writer.println("  </xsl:template>");
	writer.println("");
	writer.println("  <!-- Copying all elements and attributes -->");
	writer.println("  <xsl:template match=\"*|@*|text()\">");
	writer.println("    <xsl:copy>");
	writer.println("      <xsl:apply-templates select=\"*|@*|text()\"/>");
	writer.println("    </xsl:copy>");
	writer.println("  </xsl:template>");
	writer.println("");
	writer.print("</xsl:stylesheet>\n");
    }

    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
	// default behaviour
	this.cell = cell;
	cell.getRelation().accept( this );
    }

    private void collectURIs ( Cell cell ) throws AlignmentException {
	URI entity1URI, entity2URI;
	// JE: I think that now these two clauses should be unified (3.4)
	if ( onto1 != null ){
	    try {
		entity1URI = onto1.getEntityURI( cell.getObject1() );
		entity2URI = onto2.getEntityURI( cell.getObject2() );
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Cannot find entity URI", owex );
	    }
	} else {
	    entity1URI = cell.getObject1AsURI( alignment );
	    entity2URI = cell.getObject2AsURI( alignment );
	}
	if ( entity1URI != null ) {
	    String ns1 = entity1URI.getScheme()+":"+entity1URI.getSchemeSpecificPart()+"#";
	    if ( namespaces.get( ns1 ) == null ){
		namespaces.put( ns1, "ns"+nsrank++ );
	    }
	}
	if ( entity2URI != null ) {
	    String ns2 = entity2URI.getScheme()+":"+entity2URI.getSchemeSpecificPart()+"#";
	    if ( namespaces.get( ns2 ) == null ){
		namespaces.put( ns2, "ns"+nsrank++ );
	    }
	}
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	// The code is exactly the same for properties and classes
	if ( onto1 != null ){
	    try {
		writer.println("  <xsl:template match=\""+namespacify(onto1.getEntityURI( cell.getObject1() ))+"\">");
		writer.println("    <xsl:element name=\""+namespacify(onto2.getEntityURI( cell.getObject2() ))+"\">");
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Cannot find entity URI", owex );
	    }
	} else {
	    writer.println("  <xsl:template match=\""+namespacify(cell.getObject1AsURI( alignment ))+"\">");
	    writer.println("    <xsl:element name=\""+namespacify(cell.getObject2AsURI( alignment ))+"\">");
	}
	writer.println("      <xsl:apply-templates select=\"*|@*|text()\"/>");
	writer.println("    </xsl:element>");
	writer.println("  </xsl:template>\n");
    }

    private String namespacify( URI u ) {
	String ns = u.getScheme()+":"+u.getSchemeSpecificPart()+"#";
	return namespaces.get(ns)+":"+u.getFragment();
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

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

package fr.inrialpes.exmo.align.impl.renderer; 

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

import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * Renders an alignment as a XSLT stylesheet transforming 
 *.data of the first ontology into the second one.
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */

public class XSLTRendererVisitor implements AlignmentVisitor
{
    PrintStream writer = null;
    Alignment alignment = null;
    Cell cell = null;

    public XSLTRendererVisitor( PrintStream writer ){
	this.writer = writer;
    }

    public void visit( Alignment align ) throws AlignmentException {
	alignment = align;
	    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	    writer.println("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\"");
	writer.println("    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"");
	writer.println("    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
	writer.println("    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" ");
	writer.println("    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n");	
	for( Enumeration e = align.getElements() ; e.hasMoreElements(); ){
	    Cell c = (Cell)e.nextElement();
	    c.accept( this );
	}

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
	this.cell = cell;
	OWLOntology onto1 = (OWLOntology)alignment.getOntology1();
	try {
	    URI entity1URI = ((OWLEntity)cell.getObject1()).getURI();
	    if ( ((OWLEntity)onto1.getClass( entity1URI ) != null )
		 || ((OWLEntity)onto1.getDataProperty( entity1URI ) != null)
		 || ((OWLEntity)onto1.getObjectProperty( entity1URI ) != null )) { 
		cell.getRelation().accept( this );
	    }
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	// The code is exactly the same for properties and classes
	try {
	    writer.println("  <xsl:template match=\""+((OWLEntity)cell.getObject1()).getURI().toString()+"\">");
	    writer.println("    <xsl:element name=\""+((OWLEntity)cell.getObject2()).getURI().toString()+"\">");
	    writer.println("      <xsl:apply-templates select=\"*|@*|text()\"/>");
	    writer.println("    </xsl:element>");
	    writer.println("  </xsl:template>\n");
	} catch (OWLException e) { throw new AlignmentException("getURI problem", e); };
    }

    public void visit( SubsumeRelation rel ){};
    public void visit( IncompatRelation rel ){};

    public void visit( Relation rel ) throws AlignmentException {
	// JE: I do not understand why I need this,
	// but this seems to be the case...
	try {
	    Method mm = null;
	    if ( Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation").isInstance(rel) ){
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation")});
	    }
	    if ( mm != null ) mm.invoke(this,new Object[] {rel});
	} catch (Exception e) { throw new AlignmentException("Dispatching problem ", e); };
    };
}

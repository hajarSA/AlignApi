/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2004, 2007-2010
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
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.Visitable;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */

public class OWLAxiomsRendererVisitor implements AlignmentVisitor {
    boolean heterogeneous = false;
    PrintWriter writer = null;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;

    public OWLAxiomsRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Properties p ) {
	if ( p.getProperty("heterogeneous") != null ) heterogeneous = true;
    };

    public void visit( Visitable o ) throws AlignmentException {
	if ( o instanceof Alignment ) visit( (Alignment)o );
	else if ( o instanceof Cell ) visit( (Cell)o );
	else if ( o instanceof Relation ) visit( (Relation)o );
    }

    public void visit( Alignment align ) throws AlignmentException {
	if ( !( align instanceof ObjectAlignment ))  {
	    throw new AlignmentException("OWLAxiomsRenderer: cannot render simple alignment. Turn them into ObjectAlignment, by toObjectAlignement()");
	}
	alignment = align;
	if ( align instanceof ObjectAlignment ){
	    onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	    onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	}
	writer.print("<rdf:RDF\n");
	writer.print("    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n");
	writer.print("    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
	writer.print("    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" \n");
	writer.print("    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">\n\n");	
	writer.print("  <owl:Ontology rdf:about=\"\">\n");
	writer.print("    <rdfs:comment>Matched ontologies</rdfs:comment>\n");
	writer.print("    <rdfs:comment>Generated by fr.inrialpes.exmo.align.renderer.OWLAxiomsRendererVisitor</rdfs:comment>\n");
	for ( String[] ext : align.getExtensions() ){
	    writer.print("    <rdfs:comment>"+ext[1]+": "+ext[2]+"</rdfs:comment>\n");
	}
	writer.print("    <owl:imports rdf:resource=\""+align.getOntology1URI().toString()+"\"/>\n");
	writer.print("    <owl:imports rdf:resource=\""+align.getOntology2URI().toString()+"\"/>\n");
	writer.print("  </owl:Ontology>\n\n");
	
	for( Cell c : align ){
	    Object ob1 = c.getObject1();
	    Object ob2 = c.getObject2();

	    if ( heterogeneous ||
		 ( onto1.isClass( ob1 ) && onto2.isClass( ob2 ) ) ||
		 ( onto1.isDataProperty( ob1 ) && onto2.isDataProperty( ob2 ) ) ||
	  	 ( onto1.isObjectProperty( ob1 ) && onto2.isObjectProperty( ob2 ) ) ||
                 ( onto1.isIndividual( ob1 ) && onto2.isIndividual( ob2 ) ) )
		c.accept( this );
                      			 
	} //end for
	writer.print("</rdf:RDF>\n");
    }

    public void visit( Cell cell ) throws AlignmentException {
	this.cell = cell;
	Object ob1 = cell.getObject1();
	Object ob2 = cell.getObject2();
	URI u1;
	if ( cell.getRelation() instanceof SubsumedRelation ){
	    u1 = onto2.getEntityURI( cell.getObject2() );
	} else {
	    u1 = onto1.getEntityURI( ob1 );
	}
	if ( onto1.isClass( ob1 ) ) {
	    writer.print("  <owl:Class rdf:about=\""+u1+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("  </owl:Class>\n");
	} else if ( onto1.isDataProperty( ob1 ) ) {
	    writer.print("  <owl:DatatypeProperty rdf:about=\""+u1+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("  </owl:DatatypeProperty>\n");
	} else if ( onto1.isObjectProperty( ob1 ) ) {
	    writer.print("  <owl:ObjectProperty rdf:about=\""+u1+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("  </owl:ObjectProperty>\n");
	} else if ( onto1.isIndividual( ob1 ) ) {
	    writer.print("  <owl:Thing rdf:about=\""+u1+"\">\n");
	    cell.getRelation().accept( this );
	    writer.print("  </owl:Thing>\n");
	}
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	Object ob2 = cell.getObject2();
	URI u2 = onto2.getEntityURI( ob2 );
	if ( onto2.isClass( ob2 ) ) {
	    writer.print("    <owl:equivalentClass rdf:resource=\""+u2+"\"/>\n");
	} else if ( onto2.isDataProperty( ob2 ) ) {
	    writer.print("    <owl:equivalentProperty rdf:resource=\""+u2+"\"/>\n");
	} else if ( onto2.isObjectProperty( ob2 ) ) {
	    writer.print("    <owl:equivalentProperty rdf:resource=\""+u2+"\"/>\n");
	} else if ( onto2.isIndividual( ob2 ) ) {
	    writer.print("    <owl:sameAs rdf:resource=\""+u2+"\"/>\n");
	}
    }

    public void visit( SubsumeRelation rel ) throws AlignmentException {
	Object ob2 = cell.getObject2();
	URI u2 = onto2.getEntityURI( ob2 );
	if ( onto1.isClass( ob2 ) ) {
	    writer.print("    <rdfs:subClassOf rdf:resource=\""+u2+"\"/>\n");
	} else if ( onto1.isDataProperty( ob2 ) ) {
	    writer.print("    <rdfs:subPropertyOf rdf:resource=\""+u2+"\"/>\n");
	} else if ( onto1.isObjectProperty( ob2 ) ) {
	    writer.print("    <rdfs:subPropertyOf rdf:resource=\""+u2+"\"/>\n");
	}
    }

    public void visit( SubsumedRelation rel ) throws AlignmentException {
	Object ob1 = cell.getObject1();
	URI u1 = onto1.getEntityURI( ob1 );
	if ( onto1.isClass( ob1 ) ) {
	    writer.print("    <rdfs:subClassOf rdf:resource=\""+u1+"\"/>\n");
	} else if ( onto1.isDataProperty( ob1 ) ) {
	    writer.print("    <rdfs:subPropertyOf rdf:resource=\""+u1+"\"/>\n");
	} else if ( onto1.isObjectProperty( ob1 ) ) {
	    writer.print("    <rdfs:subPropertyOf rdf:resource=\""+u1+"\"/>\n");
	}
    }

    public void visit( IncompatRelation rel ) throws AlignmentException {
	Object ob2 = cell.getObject2();
	URI u2 = onto2.getEntityURI( ob2 );
	writer.print("    <owl:disjointWith rdf:resource=\""+u2+"\"/>\n");
    }

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
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation")});
	    } else if (Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation").isInstance(rel) ) {
		mm = this.getClass().getMethod("visit",
					       new Class [] {Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation")});
	    }
	    if ( mm != null ) mm.invoke(this,new Object[] {rel});
	    else {
		if ( Class.forName("fr.inrialpes.exmo.align.impl.rel.BasicRelation").isInstance(rel) ){
		    Object ob2 = cell.getObject2();
		    URI u2 = onto2.getEntityURI( ob2 );
		    if ( onto2.isIndividual( ob2 ) ) { // ob1 has been checked before
			// It would be better to check that r is a relation of one of the ontologies by
			// onto1.isObjectProperty( onto1.getEntity( new URI ( r ) ) )
			String r = ((BasicRelation)rel).getRelation();
			if ( r!=null && !r.equals("") )
			    writer.print("    <"+r+" rdf:resource=\""+u2+"\"/>\n");
		    }
		}
	    }
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	} catch (InvocationTargetException e) { 
	    e.printStackTrace();
	}
    };
}

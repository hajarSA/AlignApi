/*
 * $Id$
 *
 * Copyright (C) INRIA, 2003-2008, 2010
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

package fr.inrialpes.exmo.ontowrap.jena25;

import java.net.URI;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;

import fr.inrialpes.exmo.ontowrap.BasicOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;

public class JENAOntology extends BasicOntology<OntModel> implements LoadedOntology<OntModel>{

    // JE: this is not very Java 1.5...
    // This is because of the version of Jena we use apparently

    public Object getEntity(URI u) throws AlignmentException {
	return onto.getOntResource(u.toString());
    }
    
    public void getEntityAnnotations(Object o, Set<String> annots) throws AlignmentException {
	OntResource or = (OntResource) o;
	Iterator<?> z = onto.listAnnotationProperties();
	while (z.hasNext()) {
	    NodeIterator j = or.listPropertyValues((Property) z.next());
		while (j.hasNext()) {
		    RDFNode n = j.nextNode();
		    if (n.isResource())
			getEntityAnnotations(n, annots);
		    else if (n.isLiteral()) {
			annots.add(((Literal) n.as(Literal.class)).getLexicalForm());
		    }
		}
	}
    }

    public Set<String> getEntityAnnotations(Object o) throws AlignmentException {
	Set<String> annots = new HashSet<String>();
	getEntityAnnotations(o,annots);
	return annots;
    }

    public Set<String> getEntityComments(Object o, String lang) throws AlignmentException {
	Set<String> comments = new HashSet<String>();
	OntResource or = (OntResource) o;
	Iterator<?> i = or.listComments(lang);
	while (i.hasNext()) {
	    String comment = ((LiteralImpl) i.next()).getLexicalForm();
	    comments.add(comment);
	}
	return comments;
    }

    public Set<String> getEntityComments(Object o) throws AlignmentException {
	return getEntityComments(o,null);
    }


    public String getEntityName( Object o ) throws AlignmentException {
	try {
	    // Should try to get labels first... (done in the OWLAPI way)
	    return getFragmentAsLabel( new URI( ((OntResource) o).getURI() ) );
	} catch ( Exception oex ) {
	    return null;
	}
    }

    public String getEntityName( Object o, String lang ) throws AlignmentException {
	// Should first get the label in the language
	return getEntityName( o );
    }

    public Set<String> getEntityNames(Object o, String lang) throws AlignmentException {
	Set<String> labels = new HashSet<String>();
	OntResource or = (OntResource) o;
	Iterator<?> i = or.listLabels(lang);
	while (i.hasNext()) {
	    String label = ((LiteralImpl) i.next()).getLexicalForm();
	    labels.add(label);
	}
	return labels;
    }

    public Set<String> getEntityNames(Object o) throws AlignmentException {
	return getEntityNames(o,null);
    }

    public URI getEntityURI(Object o) throws AlignmentException {
	try {
	    OntResource or = (OntResource) o;
	    return new URI(or.getURI());
	} catch (Exception e) {
	    throw new AlignmentException(o.toString()+" do not have uri", e );
	}
    }

    protected Set<OntResource> getEntitySet(final Iterator<OntResource> i) {
	return  new AbstractSet<OntResource>() {
	    private int size=-1;
	    public Iterator<OntResource> iterator() {
		return new JENAEntityIt(getURI(),i);
	    }
	    public int size() {
		if (size==-1) {
		    for (OntResource r : this)
			size++;
		    size++;
		}
		return size;
	    }
	};
    }

    @SuppressWarnings("unchecked")
    public Set<?> getClasses() {
	return getEntitySet(onto.listNamedClasses());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getDataProperties() {
	return getEntitySet(onto.listDatatypeProperties());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getEntities() {
	return getEntitySet(onto.listObjectProperties().andThen(onto.listDatatypeProperties()).
		andThen(onto.listIndividuals()).andThen(onto.listClasses()));
    }

    @SuppressWarnings("unchecked")
    public Set<?> getIndividuals() {
	return getEntitySet(onto.listIndividuals());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getObjectProperties() {
	return getEntitySet(onto.listObjectProperties());
    }

    @SuppressWarnings("unchecked")
    public Set<?> getProperties() {
	return getEntitySet(onto.listObjectProperties().andThen(onto.listDatatypeProperties()));
    }

    public boolean isClass(Object o) {
	return o instanceof OntClass;
    }

    public boolean isDataProperty(Object o) {
	return o instanceof DatatypeProperty;
    }

    public boolean isEntity(Object o) {
	return isClass(o)||isProperty(o)||isIndividual(o);
    }

    public boolean isIndividual(Object o) {
	return o instanceof Individual;
    }

    public boolean isObjectProperty(Object o) {
	return o instanceof ObjectProperty;
    }

    public boolean isProperty(Object o) {
	return o instanceof OntProperty;
    }

    public int nbEntities() {
	return this.getEntities().size();
    }

    public int nbClasses() {
	return this.getClasses().size();
    }

    public int nbDataProperties() {
	return this.getDataProperties().size();
    }

    public int nbIndividuals() {
	return this.getIndividuals().size();
    }

    public int nbObjectProperties() {
	return this.getObjectProperties().size();
    }

    public int nbProperties() {
	return this.getProperties().size();
    }

    public void unload() {

    }


}
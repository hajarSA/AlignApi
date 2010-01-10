/*
 * $Id$
 *
 * Copyright (C) INRIA, 2007-2008, 2010
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

package fr.inrialpes.exmo.ontowrap;

import java.net.URI;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;

/**
 * Store the information regarding ontologies in a specific structure
 */

public class BasicOntology<O> implements Ontology<O> {

    protected URI uri = null;
    protected URI file = null;
    protected URI formalismURI = null;
    protected String formalism = null;
    protected O onto = null;

    public BasicOntology() {};

    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    public URI getURI() { return uri; }
    public URI getFile() { return file; }
    public URI getFormURI() { return formalismURI; }
    public String getFormalism() { return formalism; }
    public O getOntology() { return onto; }

    public void setURI( URI uri ) { this.uri = uri; }
    public void setFile( URI file ) { this.file = file; }
    public void setFormURI( URI u ) { formalismURI = u; }
    public void setFormalism( String name ) { formalism = name; }
    public void setOntology( O o ) { this.onto = o; }

    public String getFragmentAsLabel( URI u ){
	String result = u.getFragment();
	if ( result == null ) {
	    String suri = u.toString();
	    int end = suri.length()-1;
	    if ( suri.charAt(end) == '/' ) end--;
	    int start = end-1;
	    for ( ; suri.charAt( start ) != '/' && start != 0 ; start-- );
	    result = suri.substring( start+1, end+1 );
	}
	return result;
    }
}

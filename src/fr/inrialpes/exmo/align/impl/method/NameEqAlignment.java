/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2003-2004
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.method; 

import java.util.Iterator;
import java.util.Hashtable;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.BasicAlignment;


/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * An improvement of that class is that, since it is based on names only,
 * it can match freely property names with class names...
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */


public class NameEqAlignment extends BasicAlignment implements AlignmentProcess
{
	
    /** Creation **/
    public NameEqAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	init( onto1, onto2 );
	setType("11");
    };

    /** Processing **/
    /** This is not exactly equal, this uses toLowerCase() */
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	Hashtable table = new Hashtable();
	OWLEntity ob1 = null;
	OWLEntity ob2 = null;
	//ignore alignment;
	// This is a stupid O(2n) algorithm:
	// Put each class of onto1 in a hashtable indexed by its name (not qualified)
	// For each class of onto2 whose name is found in the hash table
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); ){
	    ob1 = (OWLEntity)it.next();
	    if ( ob1.getURI().getFragment() != null )
		table.put((Object)ob1.getURI().getFragment().toLowerCase(), ob1);
	}
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); ){
	    ob2 = (OWLEntity)it.next();
	    if ( ob2.getURI().getFragment() != null ) {
		ob1 = (OWLEntity)table.get((Object)ob2.getURI().getFragment().toLowerCase());
		if( ob1 != null ){ addAlignCell( ob1, ob2 ); }
	    }
	}
	for ( Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); ){
	    ob1 = (OWLEntity)it.next();
	    if ( ob1.getURI().getFragment() != null )
		table.put((Object)ob1.getURI().getFragment().toLowerCase(), ob1);
	}
	for ( Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); ){
	    ob2 = (OWLEntity)it.next();
	    if ( ob2.getURI().getFragment() != null ){
		ob1 = (OWLEntity)table.get((Object)ob2.getURI().getFragment().toLowerCase());
		if( ob1 != null ){ addAlignCell( ob1, ob2 ); }
	    }
	}
	for ( Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); ){
	    ob1 = (OWLEntity)it.next();
	    if ( ob1.getURI().getFragment() != null )
		table.put((Object)ob1.getURI().getFragment().toLowerCase(), ob1);
	}
	for ( Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); ){
	    ob2 = (OWLEntity)it.next();
	    if ( ob2.getURI().getFragment() != null ){
		ob1 = (OWLEntity)table.get((Object)ob2.getURI().getFragment().toLowerCase());
		if( ob1 != null ){ addAlignCell( ob1, ob2 ); }
	    }
	}
	//for ( Iterator it = onto1.getIndividuals().iterator(); it.hasNext(); ){
	//	id = (OWLIndividual)it.next();
	//	table.put((Object)pr.getURI().getFragment().toLowerCase(), id);
	//}
	//for ( Iterator it = onto2.getIndividuals().iterator(); it.hasNext(); ){
	//	OWLIndividual id2 = (OWLIndividual)it.next();
	//	id = (OWLIndividual)table.get((Object)id2.getURI().getFragment().toLowerCase());
	//	if( id != null ){ addAlignCell( id, id2 ); }
	//  }
    }

}

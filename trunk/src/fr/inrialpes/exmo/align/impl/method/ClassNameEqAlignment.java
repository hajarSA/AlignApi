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

package fr.inrialpes.exmo.align.impl.method; 

import java.util.Iterator;
import java.util.Hashtable;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
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
 * @author J�r�me Euzenat
 * @version $Id$ 
 */


public class ClassNameEqAlignment extends BasicAlignment implements AlignmentProcess
{
	
    /** Creation **/
    public ClassNameEqAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	init( onto1, onto2 );
	setType("11");
    };

    /** Processing **/
    /** This is not exactly equal, this uses toLowerCase() */
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	Hashtable table = new Hashtable();
	OWLClass cl = null;

	//ignore alignment;
	// This is a stupid O(2n) algorithm:
	// Put each class of onto1 in a hashtable indexed by its name (not qualified)
	// For each class of onto2 whose name is found in the hash table
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); ){
	    cl = (OWLClass)it.next();
	    if ( cl.getURI().getFragment() != null )
		table.put((Object)cl.getURI().getFragment().toLowerCase(), cl);
	}
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); ){
	    OWLClass cl2 = (OWLClass)it.next();
	    if ( cl2.getURI().getFragment() != null ) {
		cl = (OWLClass)table.get((Object)cl2.getURI().getFragment().toLowerCase());
		if( cl != null ){ addAlignCell( cl, cl2 ); }
	    }
	}
    }

}

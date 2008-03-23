/*
 * $Id: OntologyCache.java 670 2008-03-02 00:06:16Z euzenat $
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2007-2008
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.onto; 

// import java classes
import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URI;

/**
 * This caches the loaded ontologies so that it is possible to share them between alignments
 * as well as to unload them if necessary.
 * 
 * NOTE[3.2]: This class may be obsoleted and its code go to OntolofyFactory
 *
 * @author J�r�me Euzenat
 * @version $Id: OntologyCache.java 670 2008-03-02 00:06:16Z euzenat $ 
 */

public class OntologyCache {
 
  /** The list of currently loaded ontologies as a function:
   * URI --> Ontology
   * This is the ontology URI, NOT its filename
   */
    Hashtable<URI,LoadedOntology> ontologies = null;
    
    public OntologyCache() {
	ontologies = new Hashtable<URI,LoadedOntology>();
    }
  
    public void recordOntology( URI uri, LoadedOntology ontology ){
	ontologies.put(uri,ontology);
    }

    public Ontology getOntology( URI uri ){
	return ontologies.get( uri );
    }

    public void unloadOntology( URI uri, LoadedOntology ontology ){
	LoadedOntology o = ontologies.get(uri);
	o.unload();
	ontologies.remove( uri );
    }

    public void clear(){
	// JE: Onto [Is this correct ??]
	//for ( Enumeration e = ontologies.elements() ; e.hasMoreElements();  ){
	//    Ontology o = (Ontology)e.nextElement();
	for ( LoadedOntology o : ontologies.values() ){
	    o.unload();
	}
	ontologies.clear();
    }

}

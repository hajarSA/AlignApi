/*
 * $Id$
 *
 * Copyright (C) INRIA, 2006-2014
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Random;
import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;

import org.semanticweb.owl.align.OntologyNetwork;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

/**
 * This class implements the Alignment store of the server in
 * a non persistent way.
 * It can be subclassed by for implementing persistent storage.
 *
 * It indexes all entities in specific tables
 * Allocate them URIs
 */

public class VolatilCache implements Cache {
    final static Logger logger = LoggerFactory.getLogger( VolatilCache.class );

    Hashtable<String,Alignment> alignmentTable = null;
    Hashtable<URI,Set<Alignment>> ontologyTable = null;
    Hashtable<URI,OntologyNetwork> onetworkTable = null;

    String idprefix = null;

    static protected final String SVCNS = Namespace.ALIGNSVC.getUriPrefix();
    static protected final String CACHED = "cached";
    static protected final String STORED = "stored";
    static protected final String ALID = "alid/";
    static protected final String OURI1 = "ouri1";
    static protected final String OURI2 = "ouri2";
    static protected final String ONID = "onid/";
	
    //**********************************************************************

    public VolatilCache() {
	resetTables();
    }

    public void resetTables() {
	alignmentTable = new Hashtable<String,Alignment>();
	ontologyTable = new Hashtable<URI,Set<Alignment>>();
	onetworkTable = new Hashtable<URI,OntologyNetwork>();
    }

    public void reset() throws AlignmentException {
	resetTables();
    }

    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     */
    public void init( Properties p, String prefix ) throws AlignmentException {
	logger.debug( "Initializing Database cache" );
	idprefix = prefix;
    }

    public void close() throws AlignmentException {
    }

    // **********************************************************************
    // INDEXES

    protected Enumeration<Alignment> listAlignments() {
	return alignmentTable.elements();
    }

    public Collection<Alignment> alignments() {
	return alignmentTable.values();
    }

    public Collection<URI> ontologies() {
	return ontologyTable.keySet();
    }
    
    public Collection<URI> ontologyNetworkUris() {
    	return onetworkTable.keySet();
    }
    
    public Collection<OntologyNetwork> ontologyNetworks() {
    	return onetworkTable.values();
    }
        
    public Set<Alignment> getAlignments( URI uri ) {
	return ontologyTable.get( uri );
    }

    /**
     * returns the alignments between two ontologies
     * if one of the ontologies is null, then return them all
     */
    public Set<Alignment> getAlignments( URI uri1, URI uri2 ) {
	Set<Alignment> result;
	Set<Alignment> potential = new HashSet<Alignment>();
	if ( uri2 != null ){
	    String uri2String = uri2.toString();
	    Set<Alignment> found = ontologyTable.get( uri2 );
	    if ( found != null ) {
		for( Alignment al : found ) {
		    if ( al.getExtension(SVCNS, OURI2).equals( uri2String ) ) {
			potential.add( al );
		    }
		}
	    }
	} 
	if ( uri1 != null ) {
	    if ( potential.isEmpty() ) {
		Set<Alignment> found = ontologyTable.get( uri1 );
		if ( found != null ) {
		    potential = found;
		} else return potential;
	    }
	    result = new HashSet<Alignment>();
	    String uri1String = uri1.toString();
	    for(  Alignment al : potential ) {
		// This is not the best because URI are not resolved here...
		if ( al.getExtension(SVCNS, OURI1).equals( uri1String ) ) {
		    result.add( al );
		}
	    }
	} else { result = potential; }
	return result;
    }

    public Collection<Alignment> alignments( URI u1, URI u2 ) {
	Collection<Alignment> results = new HashSet<Alignment>();
	if ( u1 != null ) {
	    for ( Alignment al : ontologyTable.get( u1 ) ) {
		try {
		    //    if ( al.getOntology1URI().equals( u1 ) ) {
		    if ( u2 == null ) results.add( al );
		    else if ( al.getOntology2URI().equals( u2 ) 
			      || al.getOntology1URI().equals( u2 )) results.add( al );
		    //    }
		} catch (AlignmentException alex) {
		    logger.debug( "IGNORED Exception", alex );
		}
	    }
	} else if ( u2 != null ) {
	    for ( Alignment al : ontologyTable.get( u2 ) ) {
		results.add( al );
	    }
	} else { results = alignmentTable.values(); }
	return results;
    }

    //**********************************************************************
    // DEALING WITH URIs

    // Public because this is now used by AServProtocolManager
    public String generateAlignmentUri() {
	// Generate an id based on a URI prefix + Date + random number
	return recoverAlignmentUri( generateId() );
    }
    
    protected String recoverAlignmentUri( String id ) {
	// Recreate Alignment URI from its id
	return idprefix + "/" + ALID + id;
    }
    
    protected String stripAlignmentUri( String alid ) {
	return alid.substring( alid.indexOf( ALID )+5 );
    }

    public String generateOntologyNetworkUri() { //For Ontology and Ontology Networks
	// Generate an id based on a URI prefix + Date + random number
	return recoverNetworkUri( generateId() );
    }
    
    protected String recoverNetworkUri( String id ) {
	// Recreate Ontology Network URI from its id
	return idprefix + "/" + ONID + id;
    }
    
    protected String stripNetworkUri( String onetid ) {
	return onetid.substring( onetid.indexOf( ONID )+5 ); 
    }
    
    private String generateId() {
	// Generate an id based on Date + random number
	return new Date().getTime() + "/" + randomNum();
    }
    
    private int randomNum() {
	Random rand = new Random(System.currentTimeMillis());
	return Math.abs(rand.nextInt(1000)); 
    }

    /*
     * Rules for cell ids:
     * (1) if users set cell_id uses them (check them for URI)
     * (2) if not, generate a *local* cell id if necessary and add ##
     * (3) use these cell-id in the extension part...
     * STORE:
     * if cell has extension && no id, create cell id, store it in db, not in setId
     * if cell has extension && id, us it with getId/setId
     * UNSTORE:
     * suppress those extensions with the cell_id if exists
     * LOAD-FROM-DB: 
     * if there is a cell id, use it for loading extensions
     * At alignment store time, use getCellId -> store it
     * At alignment load-from-db time, get the id and all the 
     */

    protected String generateCellId() {
	return "##"+generateId();
    }

    //**********************************************************************
    // FETCHING FROM CACHE

    /**
     * retrieve alignment metadata from id
     * This is more difficult because we return the alignment we have 
     * disreagarding if it is complete o only metadata
     */
    public Alignment getMetadata( String uri ) throws AlignmentException {
	Alignment result = alignmentTable.get( uri );
	if ( result == null )
	    throw new AlignmentException("getMetadata: Cannot find alignment");
	return result;
    }
	
    /**
     * retrieve full alignment from id (and cache it)
     */
    public Alignment getAlignment( String uri ) throws AlignmentException {
	Alignment result = null;
	try {
	    result = alignmentTable.get( uri );
	} catch( Exception ex ) {
	    //logger.trace( "Unknown exception with Id = {}", uri );
	    logger.debug( "IGNORED: Unknown exception", ex );
	}
	
	if ( result == null ) {
	    //logger.trace( "Cache: Id ={} is not found.", uri );
	    throw new AlignmentException( "getAlignment: Cannot find alignment "+uri );
	}

	// If not cached, persistent implementation has to get it
	if ( ( result.getExtension( SVCNS, CACHED ) == null || result.getExtension( SVCNS, CACHED ).equals("") )
	     && result.getExtension(SVCNS, STORED ) != null 
	     && !result.getExtension(SVCNS, STORED ).equals("") ) {
	    return fetchAlignment( uri, result );
	}
	return result;
    }

    protected Alignment fetchAlignment( String uri, Alignment result ) throws AlignmentException {
	return result;
    }
	
    /**
     * retrieve network of ontologies from id
     */
    public OntologyNetwork getOntologyNetwork( String uri ) throws AlignmentException {
	OntologyNetwork result = onetworkTable.get( uri );
	if ( result == null )
	    throw new AlignmentException("Cannot find ontology network "+uri);
	return result;
    }
	
    public void flushCache() { // throws AlignmentException
	for ( Alignment al : alignmentTable.values() ){
	    if ( al.getExtension(SVCNS, CACHED ) != null && 
		 !al.getExtension( SVCNS, CACHED ).equals("") &&
		 al.getExtension(SVCNS, STORED ) != null && 
		 !al.getExtension( SVCNS, STORED ).equals("") ) flushAlignment( al );
	};
    }

    /**
     * unload the cells of an alignment...
     * This should help releasing some space
     * 
     * should be invoked when:
     * 	( result.getExtension(CACHED) != ""
     *  && obviously result.getExtension(STORED) != ""
     */
    protected void flushAlignment( Alignment alignment ) {// throws AlignmentException
	//alignment.removeAllCells();
	// reset
    	//alignment.setExtension( SVCNS, CACHED, "" );
    }
    
    //**********************************************************************
    // RECORDING ALIGNMENTS

    /**
     * records newly created alignment
     */
    public String recordNewAlignment( Alignment alignment, boolean force ) {
	try { return recordNewAlignment( generateAlignmentUri(), alignment, force );
	} catch (AlignmentException ae) { return (String)null; }
    }

    /**
     * records alignment identified by id
     */
    public String recordNewAlignment( String uri, Alignment al, boolean force ) throws AlignmentException {
	al.setExtension(SVCNS, OURI1, al.getOntology1URI().toString());
	al.setExtension(SVCNS, OURI2, al.getOntology2URI().toString());
	// Index
	recordAlignment( uri, al, force );
	// Not yet stored
	al.setExtension(SVCNS, STORED, (String)null);
	// Cached now
	resetCacheStamp( al );
	return uri;
    }

    /**
     * records alignment identified by id
     */
    public String recordAlignment( String uri, Alignment alignment, boolean force ) {
	// record the Alignment at the corresponding Uri in tables!
	alignment.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, uri );

	// Store it
	try {
	    URI ouri1 = new URI( alignment.getExtension( SVCNS, OURI1) );
	    URI ouri2 = new URI( alignment.getExtension( SVCNS, OURI2) );
	    if ( force || alignmentTable.get( uri ) == null ) {
		Set<Alignment> s1 = ontologyTable.get( ouri1 );
		if ( s1 == null ) {
		    s1 = new HashSet<Alignment>();
		    ontologyTable.put( ouri1, s1 );
		}
		s1.add( alignment );
		Set<Alignment> s2 = ontologyTable.get( ouri2 );
		if ( s2 == null ) {
		    s2 = new HashSet<Alignment>();
		    ontologyTable.put( ouri2, s2 );
		}
		s2.add( alignment );
		alignmentTable.put( uri, alignment );
	    }
	    return uri;
	} catch ( URISyntaxException e ) {
	    logger.debug( "IGNORED: Unlikely URI exception", e );
	    return null;
	}
    }

    /**
     * suppresses the record for an alignment
     */
    public void unRecordAlignment( Alignment alignment ) {
	String id = alignment.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	try {
	    Set<Alignment> s1 = ontologyTable.get( new URI( alignment.getExtension( SVCNS, OURI1) ) );
	    if ( s1 != null ) s1.remove( alignment );
	    Set<Alignment> s2 = ontologyTable.get( new URI( alignment.getExtension( SVCNS, OURI2) ) );
	    if ( s2 != null ) s2.remove( alignment );
	} catch ( URISyntaxException uriex ) {
	    logger.debug( "IGNORED: Unlikely URI exception", uriex );
	}
	alignmentTable.remove( id );
    }

    //**********************************************************************
    // RECORDING NETWORKS OF ONTOLOGIES

    /**
     * records a newly created network
     */
    public String recordNewNetwork( OntologyNetwork network, boolean force ) {
	try { return recordNewNetwork( generateOntologyNetworkUri(), network, force );
	} catch ( AlignmentException ae ) { return (String)null; }
    }

    /**
     * records alignment identified by id
     */
    public String recordNewNetwork( String uri, OntologyNetwork network, boolean force ) throws AlignmentException {
	// Index
	recordNetwork( uri, network, force );
	// Not yet stored
	((BasicOntologyNetwork)network).setExtension( SVCNS, STORED, (String)null );
	// Cached now
	resetCacheStamp( network );
	return uri;
    }

    /**
     * records a network identified by id
     */
    public String recordNetwork( String uri, OntologyNetwork network, boolean force ) {
	// record the network at the corresponding Uri in tables!
	((BasicOntologyNetwork)network).setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, uri );

	// Store it
	if ( force || onetworkTable.get( uri ) == null ) {
	    try {
		onetworkTable.put( new URI( uri ), network );
	    } catch ( URISyntaxException uriex ) {
		logger.debug( "IGNORED: Unlikely URI exception", uriex );
		return null;
	    }
	}
	return uri;
    }

    /**
     * suppresses the record for a network of ontologies
     */
    public void unRecordNetwork( OntologyNetwork network ) {
	String id = ((BasicOntologyNetwork)network).getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	onetworkTable.remove( id );
    }

    //**********************************************************************
    // STORING IN DATABASE

    public boolean isAlignmentStored( Alignment alignment ) {
	return ( alignment.getExtension( SVCNS, STORED ) != null &&
		 !alignment.getExtension( SVCNS, STORED ).equals("") );
    }

    public boolean isNetworkStored( OntologyNetwork network ) {
	return ( ((BasicOntologyNetwork)network).getExtension( SVCNS, STORED ) != null &&
		 !((BasicOntologyNetwork)network).getExtension( SVCNS, STORED ).equals("") );
    }

    /**
     * Non publicised class
     */
    public void eraseAlignment( String uri, boolean eraseFromDB ) throws AlignmentException {
        Alignment alignment = getAlignment( uri );
        if ( alignment != null ) {
	    if ( eraseFromDB ) unstoreAlignment( uri, alignment );
	    unRecordAlignment( alignment );
        }
    }

    public void unstoreAlignment( String uri, Alignment alignment ) throws AlignmentException {
    }

    public void storeAlignment( String uri ) throws AlignmentException {
    }

    /**
     * Non publicised class
     */
    public void eraseOntologyNetwork( String uri, boolean eraseFromDB ) throws AlignmentException {
        OntologyNetwork network = getOntologyNetwork( uri );
        if ( network != null ) {
	    if ( eraseFromDB ) unstoreOntologyNetwork( uri, network );
	    unRecordNetwork( network );
        }
    }

    public void unstoreOntologyNetwork( String uri, OntologyNetwork network ) throws AlignmentException {
    }

    public void storeOntologyNetwork( String uri ) throws AlignmentException {
    }

    //**********************************************************************
    // CACHE MANAGEMENT (Not implemented yet)
    public void resetCacheStamp( Alignment al ){
	al.setExtension(SVCNS, CACHED, new Date().toString() );
    }

    public void resetCacheStamp( OntologyNetwork network ){
	((BasicOntologyNetwork)network).setExtension(SVCNS, CACHED, new Date().toString() );
    }

    public void cleanUpCache() {
	// for each alignment in the table
	// set currentDate = Date();
	// if ( DateFormat.parse( result.getExtension(SVCNS, CACHED) ).before( ) ) {
	// - for each ontology if no other alignment => unload
	// - clean up cells
	// }
    }

}

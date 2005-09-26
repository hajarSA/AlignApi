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
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.lang.Integer;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLFrame;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLEntity;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Parameters;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;

/** This class has been built for ISWC experiments with bibliography.
 * It implements a non iterative (one step) OLA algorithms based on
 * the name of classes and properties. It could be made iterative by
 *  just adding range/domain on properties...
 *  The parameters are:
 *  - threshold: above what do we select for the alignment;
 *  - epsillon [ignored]: for convergence
 *  - pic1: weigth for class name
 *  - pic2: weight for class attributes
 *  - pia1 [ignored=1]: weigth for property name
 *  - pia3 [ignored=0]: weigth for property domain
 *  - pia4 [ignored=0]: weigth for property range
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */


public class StrucSubsDistAlignment extends DistanceAlignment implements AlignmentProcess
{
    /** Creation **/
    public StrucSubsDistAlignment( OWLOntology onto1, OWLOntology onto2 ){
    	super( onto1, onto2 );
	setType("**");
    };

    private double max( double i, double j) { if ( i>j ) return i; else return j; }

    /** Processing **/
    public void align( Alignment alignment, Parameters params ) throws AlignmentException, OWLException {
	//ignore alignment;
	double threshold = 1.; // threshold above which distances are too high
	int i, j = 0;     // index for onto1 and onto2 classes
	int l1, l2 = 0;   // length of strings (for normalizing)
	int nbclass1 = 0; // number of classes in onto1
	int nbclass2 = 0; // number of classes in onto2
	Vector classlist2 = new Vector(10); // onto2 classes
	Vector classlist1 = new Vector(10); // onto1 classes
	double classmatrix[][];   // class distance matrix
	int nbprop1 = 0; // number of properties in onto1
	int nbprop2 = 0; // number of properties in onto2
	Vector proplist2 = new Vector(10); // onto2 properties
	Vector proplist1 = new Vector(10); // onto1 properties
	double propmatrix[][];   // properties distance matrix
	double pic1 = 0.5; // class weigth for name
	double pic2 = 0.5; // class weight for properties
	double pia1 = 1.; // relation weight for name
	double pia2 = 0.; // relation weight for domain
	double pia3 = 0.; // relation weight for range
	double epsillon = 0.05; // stoping condition

	if ( params.getParameter("debug") != null )
	    debug = ((Integer)params.getParameter("debug")).intValue();

	// Create property lists and matrix
	for ( Iterator it = onto1.getObjectProperties().iterator(); it.hasNext(); nbprop1++ ){
	    proplist1.add( it.next() );
	}
	for ( Iterator it = onto1.getDataProperties().iterator(); it.hasNext(); nbprop1++ ){
	    proplist1.add( it.next() );
	}
	for ( Iterator it = onto2.getObjectProperties().iterator(); it.hasNext(); nbprop2++ ){
	    proplist2.add( it.next() );
	}
	for ( Iterator it = onto2.getDataProperties().iterator(); it.hasNext(); nbprop2++ ){
	    proplist2.add( it.next() );
	}
	propmatrix = new double[nbprop1+1][nbprop2+1];
	
	// Create class lists
	for ( Iterator it = onto1.getClasses().iterator(); it.hasNext(); nbclass1++ ){
	    classlist1.add( it.next() );
	}
	for ( Iterator it = onto2.getClasses().iterator(); it.hasNext(); nbclass2++ ){
	    classlist2.add( it.next() );
	}
	classmatrix = new double[nbclass1+1][nbclass2+1];

	if (debug > 0) System.err.println("Initializing property distances");
	for ( i=0; i<nbprop1; i++ ){
	    OWLProperty cl = (OWLProperty)proplist1.get(i);
	    String s1 = cl.getURI().getFragment();
	    if ( s1 != null ) s1 = s1.toLowerCase();
	    for ( j=0; j<nbprop2; j++ ){
		cl = (OWLProperty)proplist2.get(j);
		String s2 = cl.getURI().getFragment();
		if ( s2 != null ) s2 = s2.toLowerCase();
		if ( s1 == null || s2 == null ) { propmatrix[i][j] = pia1; }
		else {
		    propmatrix[i][j] = pia1 * StringDistances.subStringDistance( s1, s2 );
		}
	    }
	}

	if (debug > 0) System.err.println("Initializing class distances");
	// Initialize class distances
	for ( i=0; i<nbclass1; i++ ){
	    OWLClass cl = (OWLClass)classlist1.get(i);
	    for ( j=0; j<nbclass2; j++ ){
		classmatrix[i][j] = pic1 * StringDistances.subStringDistance(
						    cl.getURI().getFragment().toLowerCase(),
						    ((OWLClass)classlist2.get(j)).getURI().getFragment().toLowerCase());
	    }
	}

	// Iterate until completion
	double factor = 1.0;
	while ( factor > epsillon ){
	    // Compute property distances
	    // -- FirstExp: nothing to be done: one pass
	    // Here create the best matches for property distance already
	    // -- FirstExp: goes directly in the alignment structure
	    //    since it will never be refined anymore...
	    if (debug > 0) System.err.print("Storing property alignment\n");
	    for ( i=0; i<nbprop1; i++ ){
		boolean found = false;
		int best = 0;
		double max = threshold;
		for ( j=0; j<nbprop2; j++ ){
		    if ( propmatrix[i][j] < max) {
			found = true;
			best = j;
			max = propmatrix[i][j];
		    }
		}
		if ( found ) { addAlignDistanceCell( (OWLProperty)proplist1.get(i), (OWLProperty)proplist2.get(best), "=", max ); }
	    }
	    
	    if (debug > 0) System.err.print("Computing class distances\n");
	    // Compute classes distances
	    // -- for all of its attribute, find the best match if possible... easy
	    // -- simply replace in the matrix the value by the value plus the 
	    // classmatrix[i][j] =
	    // pic1 * classmatrix[i][j]
	    // + pic2 * 2 *
	    //  (sigma (att in c[i]) getAllignCell... )
	    //  / nbatts of c[i] + nbatts of c[j]
	    for ( i=0; i<nbclass1; i++ ){
		Set properties1 = getProperties( (OWLClass)classlist1.get(i), onto1 );
		int nba1 = properties1.size();
		if ( nba1 > 0 ) { // if not, keep old values...
		    Set correspondences = new HashSet();
		    for ( j=0; j<nbclass2; j++ ){
			Set properties2 = getProperties( (OWLClass)classlist2.get(j), onto2 );
			int nba2 = properties2.size();
			double attsum = 0.;
			// check that there is a correspondance
			// in list of class2 atts and add their weights
			for ( Iterator prp = properties1.iterator(); prp.hasNext(); ){
			    Set s2 = getAlignCells1( (OWLEntity)prp.next() );
			    // Find the property with the higest similarity
			    // that is matched here
			    double currentValue = 0.;
			    for( Iterator it2 = s2.iterator(); it2.hasNext(); ){
				Cell c2 = (Cell)it2.next();
				if ( properties2.contains((Object)c2.getObject2() ) ) {
				    double val = c2.getStrength();
				    if ( val > currentValue )
					currentValue = val;
				}
			    }
			    attsum = attsum + 1 - currentValue;
			}
			classmatrix[i][j] = classmatrix[i][j]
			    + pic2 * (2 * attsum / (nba1 + nba2));
		    }
		}
	    }
	    // Assess factor
	    // -- FirstExp: nothing to be done: one pass
	    factor = 0.;
	}

	// This mechanism should be parametric!
	// Select the best match
	// There can be many algorithm for these:
	// n:m: get all of those above a threshold
	// 1:1: get the best discard lines and columns and iterate
	// Here we basically implement ?:* because the algorithm
	// picks up the best matching object above threshold for i.
	if (debug > 0) System.err.print("Storing class alignment\n");
	
	for ( i=0; i<nbclass1; i++ ){
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for ( j=0; j<nbclass2; j++ ){
		if ( classmatrix[i][j] < max) {
		    found = true;
		    best = j;
		    max = classmatrix[i][j];
		}
	    }
	    if ( found ) { addAlignDistanceCell( (OWLClass)classlist1.get(i), (OWLClass)classlist2.get(best), "=", max ); }
	}
    }
    
    public void getProperties( OWLDescription desc, OWLOntology o, Set list, Set accu){
	// I am Jerome Euzenat and I am sure that there is some problem here...
	// DISPATCHING MANUALLY !
	try {
	    Method mm = null;
	    if ( Class.forName("org.semanticweb.owl.model.OWLRestriction").isInstance(desc) ){
		mm = this.getClass().getMethod("getProperties",
					       new Class [] {Class.forName("org.semanticweb.owl.model.OWLRestriction"),Class.forName("org.semanticweb.owl.model.OWLOntology"),Class.forName("java.util.Set"),Class.forName("java.util.Set")});
	    } else if (Class.forName("org.semanticweb.owl.model.OWLClass").isInstance(desc) ) {
		mm = this.getClass().getMethod("getProperties",
					       new Class [] {Class.forName("org.semanticweb.owl.model.OWLClass"),Class.forName("org.semanticweb.owl.model.OWLOntology"),Class.forName("java.util.Set"),Class.forName("java.util.Set")});
	    } else if (Class.forName("org.semanticweb.owl.model.OWLNaryBooleanDescription").isInstance(desc) ) {
		mm = this.getClass().getMethod("getProperties",
					       new Class [] {Class.forName("org.semanticweb.owl.model.OWLNaryBooleanDescription"),Class.forName("org.semanticweb.owl.model.OWLOntology"),Class.forName("java.util.Set"),Class.forName("java.util.Set")});
	    }
	    if ( mm != null ) mm.invoke(this,new Object[] {desc,o,list,accu});
	    //Method mmm[] = this.getClass().getMethods();
	    //for ( int i = 0; i < mmm.length ; i++ ){
	    //	if ( mmm[i].getName().equals("getProperties") ){
	    //	    mmm[i].invoke(this,new Object[] {desc,o,list,accu});
	    //	    i = mmm.length;
	    //	}
	    // }
	} catch (Exception e) { e.printStackTrace(); };
    }
    public void getProperties( OWLRestriction rest, OWLOntology o, Set list, Set accu) throws OWLException {
	if ( !accu.contains( (Object)rest ) ) {
	    accu.add((Object)rest);
	    list.add( (Object)rest.getProperty() );
	}
    }
    public void getProperties( OWLNaryBooleanDescription d, OWLOntology o, Set list, Set accu) throws OWLException {
	if ( !accu.contains( (Object)d ) ) {
	    accu.add((Object)d);
	    for ( Iterator it = d.getOperands().iterator(); it.hasNext() ;){
		getProperties( (OWLDescription)it.next(), o, list, accu );
	    }
	}
    }
    public void getProperties( OWLClass cl, OWLOntology o, Set list, Set accu) throws OWLException {
	if ( !accu.contains( (Object)cl ) ) {
	    accu.add((Object)cl);
	    for ( Iterator it = cl.getSuperClasses(o).iterator(); it.hasNext(); ){
		OWLDescription dsc = (OWLDescription)it.next();
		getProperties( dsc, o, list, accu );
	    }
	    // avoid loops
	    //for ( Iterator it = cl.getEquivalentClasses(o).iterator(); it.hasNext(); ){
	    //    getProperties( (OWLDescription)it.next(), o, list, accu );
	    //}
	}
    }

    private Set getProperties( OWLClass cl, OWLOntology o ) throws OWLException {
	Set resultSet = new HashSet();
	Set accu = new HashSet();
	getProperties( cl, o, resultSet, accu );
	return resultSet;
    }
}

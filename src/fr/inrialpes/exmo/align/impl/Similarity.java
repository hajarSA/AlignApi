/*
 * $Id$
 *
 * Copyright (C) INRIA Rh�ne-Alpes, 2004, 2006-2007
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

package fr.inrialpes.exmo.align.impl;

import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLIndividual;

import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Alignment;

/**
 * Represents the implementation of a similarity measure
 *
 * @author J�r�me Euzenat
 * @version $Id$ 
 */

public interface Similarity
{
    // These parameters contains usually:
    // ontology1 and ontology2
    // It would be better if they where explicit...
    // Apparently the initialize also compute the similarity

    public void initialize( OWLOntology onto1, OWLOntology onto2 );
    public void initialize( OWLOntology onto1, OWLOntology onto2, Alignment align );
    public void compute( Parameters p );
    public double getClassSimilarity( OWLClass c1, OWLClass c2 );
    public double getPropertySimilarity( OWLProperty p1, OWLProperty p2);
    public double getIndividualSimilarity( OWLIndividual i1, OWLIndividual i2 );

    public void printClassSimilarityMatrix( String type );
    public void printPropertySimilarityMatrix( String type );
    public void printIndividualSimilarityMatrix( String type );

    // New implementation
    // JE: this is better as a new implementation.
    // however, currently the implementation does not follow it:
    // the abstract matrix class provides the get- accessors and the 
    // concrete classes implement measure as their computation function.
    // This is not clean. What should be done is:
    public double measure( OWLClass c1, OWLClass c2 ) throws Exception;
    public double measure( OWLProperty p1, OWLProperty p2) throws Exception;
    public double measure( OWLIndividual i1, OWLIndividual i2 ) throws Exception;
}

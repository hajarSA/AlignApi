/*
 * $Id$
 *
 * Copyright (C) INRIA, 2008
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

//package test.com.acme.dona.dep;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
//import org.testng.annotations.*;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment;

import fr.inrialpes.exmo.align.onto.Ontology;
import fr.inrialpes.exmo.align.onto.BasicOntology;
import fr.inrialpes.exmo.align.onto.LoadedOntology;
import fr.inrialpes.exmo.align.onto.HeavyLoadedOntology;
import fr.inrialpes.exmo.align.onto.OntologyFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * These tests corresponds to the README file in the main directory
 */

public class AlgTest {

    Alignment align1 = null;
    Alignment align2 = null;

    @Test(groups = { "full", "impl", "noling" })
    public void initTest() throws Exception {
	Parameters params = new BasicParameters();
	AlignmentProcess alignment1 = new StringDistAlignment();
	alignment1.init( new URI("file:examples/rdf/onto1.owl"), new URI("file:examples/rdf/onto2.owl"));
	alignment1.align( (Alignment)null, params );
	AlignmentProcess alignment2 = new StringDistAlignment();
	alignment2.init( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
	alignment2.align( (Alignment)null, params );
	align1 = alignment1;
	align2 = alignment2;
    }

    @Test(expectedExceptions = AlignmentException.class, dependsOnMethods = {"initTest"}, groups = { "full", "impl", "noling" })
    public void errorTest1() throws Exception {
	// should throw an exception: 
	align1.join( align2 );
    }

    @Test(groups = { "full", "impl", "noling" }, dependsOnMethods = {"initTest"})
    public void genericityTest() throws Exception {
	// does createNewAlignment is able to return the correct method
	Alignment al = (Alignment)((BasicAlignment)align1).clone();
	assertTrue( al instanceof ObjectAlignment );
	assertTrue( al.getExtension( Annotations.ALIGNNS, "method" ).equals("fr.inrialpes.exmo.align.impl.method.StringDistAlignment#clone") );
    }

    @Test(groups = { "full", "impl", "noling" }, dependsOnMethods = {"genericityTest"})
    public void fullTest() throws Exception {
	AlignmentProcess alignment1 = new NameAndPropertyAlignment();
	alignment1.init( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
	alignment1.align( (Alignment)null, new BasicParameters() );
	align1 = alignment1;
	assertEquals( align1.nbCells(), 36 );
	assertEquals( align2.nbCells(), 10 );
	Alignment al = align1.inverse();
	assertEquals( al.getOntology1(), align1.getOntology2() );
	assertEquals( al.nbCells(), 36 );
	al = (Alignment)((BasicAlignment)align1).clone();
	assertEquals( al.getOntology1(), align1.getOntology1() );
	assertEquals( al.nbCells(), 36 );
	al = align1.diff( align2 );
	assertEquals( al.getOntology1(), align1.getOntology1() );
	assertEquals( al.nbCells(), 35 ); // short diff
	al = align2.diff( align1 );
	assertEquals( al.getOntology1(), align1.getOntology1() );
	assertEquals( al.nbCells(), 9 );
	al = align1.meet( align2 );
	assertEquals( al.getOntology1(), align1.getOntology1() );
	assertEquals( al.nbCells(), 1 );
	al = align1.join( align2 );
	assertEquals( al.getOntology1(), align1.getOntology1() );
	assertEquals( al.nbCells(), 45 );
    }

    @Test(expectedExceptions = AlignmentException.class, groups = { "full", "impl", "noling" }, dependsOnMethods = {"fullTest"})
    public void composeErrorTest() throws Exception {
	Alignment al = align1.compose( align2 );
    }

    @Test(groups = { "full", "impl", "noling" }, dependsOnMethods = {"initTest"})
    public void composeTest() throws Exception {
	AlignmentProcess alignment1 = new NameAndPropertyAlignment();
	alignment1.init( new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"), new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"));
	alignment1.align( (Alignment)null, new BasicParameters() );
	assertEquals( alignment1.nbCells(), 38 );
	assertEquals( align2.nbCells(), 10 );
	Alignment al = alignment1.compose( align2 );
	assertEquals( al.getOntology1(), alignment1.getOntology1() );
	assertEquals( al.getOntology2(), align2.getOntology2() );
	assertEquals( al.nbCells(), 4 );
    }
}
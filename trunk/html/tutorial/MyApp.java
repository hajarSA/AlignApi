/*
 * $Id$
 *
 * Copyright (C) 2006-2007, INRIA Rh�ne-Alpes
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
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

// Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Parameters;
import org.semanticweb.owl.align.Evaluator;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.OntologyCache;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.SWRLRendererVisitor;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

// SAX standard classes
import org.xml.sax.SAXException;

// Java standard classes
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.net.URI;

/**
 * MyApp
 *
 * Takes two files as arguments and align them.
 */

public class MyApp {

    static OntologyCache loaded = null;

    public static void main( String[] args ) {
	URI onto1 = null;
	URI onto2 = null;
	Parameters params = new BasicParameters();

	try {
	    // Initializing ontology parsers
	    loaded = new OntologyCache();
	
	    // Loading ontologies
	    if (args.length >= 2) {
		onto1 = new URI(args[0]);
		onto2 = new URI(args[1]);
	    } else {
		System.err.println("Need two arguments to proceed");
		return ;
	    }

	    // Run two different alignment methods (e.g., ngram distance and smoa)
	    AlignmentProcess a1 = new StringDistAlignment();
	    params.setParameter("stringFunction","smoaDistance");
	    a1.init ( onto1, onto2, loaded );
	    a1.align( (Alignment)null, params );
	    AlignmentProcess a2 = new StringDistAlignment();
	    a2.init ( onto1, onto2, loaded );
	    params = new BasicParameters();
	    params.setParameter("stringFunction","ngramDistance");
	    a2.align( (Alignment)null, params );

	    // Merge the two results.
	    ((BasicAlignment)a1).ingest(a2);

	    // Threshold at various thresholds
	    // Evaluate them against the references
	    // and choose the one with the best F-Measure
	    AlignmentParser aparser = new AlignmentParser(0);
	    Alignment reference = aparser.parse( "file://"+(new File ( "refalign.rdf" ) . getAbsolutePath()) );
	    // JE: This does not work because BasicAlignment vs. OWLAPIAlignment
	    Evaluator evaluator = new PRecEvaluator( reference, a1 );

	    double best = 0.;
	    Alignment result = null;
	    for ( int i = 0; i <= 10 ; i = i+2 ){
		a1.cut( ((double)i)/10 );
		evaluator.eval( new BasicParameters() );
		System.err.println("Threshold "+(((double)i)/10)+" : "+((PRecEvaluator)evaluator).getFmeasure());
		if ( ((PRecEvaluator)evaluator).getFmeasure() > best ) {
		    result = (BasicAlignment)((BasicAlignment)a1).clone();
		    best = ((PRecEvaluator)evaluator).getFmeasure();
		}
	    }

	    // Displays it as SWRL Rules
	    PrintWriter writer = new PrintWriter (
				  new BufferedWriter(
		                   new OutputStreamWriter( System.out, "UTF-8" )), true);
	    AlignmentVisitor renderer = new SWRLRendererVisitor(writer);
	    // JE: Here this will not break because result is already a OWLAPIAlignment
	    result.render(renderer);
	    writer.flush();
	    writer.close();

	} catch (Exception e) { e.printStackTrace(); };
    }
}

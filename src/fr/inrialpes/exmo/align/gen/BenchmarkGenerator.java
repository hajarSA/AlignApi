/*
 * $Id$
 *
 * Copyright (C) 2011, INRIA
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

/*
 * Generates the OAEI Benchmark dataset from an ontology
 * It can generate it in a continuous way (each test build on top of a previous one)
 * or generate tests independently.
 */

package fr.inrialpes.exmo.align.gen;

import java.util.Properties;

public class BenchmarkGenerator extends TestSet {

    public void initTestCases( Properties params ) {
	// Process params
	debug = ( params.getProperty( "debug" ) != null );

	// Test configuration parameters
	String mod = params.getProperty( "modality" ); // "mult"
	String hard = params.getProperty( "increment" );
	String max = params.getProperty( "maximum" );
	if ( debug ) System.err.println( " Mod: "+mod+" / Incr: "+hard );
	String SUFFIX = null;

        /* Test 101 Generate the initial situation */
	root = initTests( "101" );

        /*
         * Tests 201, 202, 248, 249, 253, 258, 259, 250, 254,
         *       262, 257, 260, 265, 261, 266, 251, 252
         */
	boolean multModality = (mod != null && mod.startsWith( "mult" ));
	float i1 = 0.0f;
	int maximum = Integer.parseInt( "5" );
	float incr = Float.parseFloat( "0.2" );
	try {
	    if ( hard != null && !hard.equals("") ) incr = Float.parseFloat( hard );
	} catch ( Exception ex ) {
	    ex.printStackTrace(); // continue with the default
	}
	String PREVTEST = "101";
	for ( int i = 0; i1 < 1.00f ; i++ ) { // && i < maximum
	    if ( i > 0 ) PREVTEST = "201"+SUFFIX; // The previous suffix
	    if ( !multModality ) i1 += incr; // traditional
	    else i1 += (1. - i1) * incr; // hardened
	    //if ( debug ) System.err.println( " ******************************************************** "+i+": i1 = "+i1 );

	    if ( i1 < 1.0f ) {
		SUFFIX = "-"+((Float)i1).toString().substring(2, 3); // 2 4 6 8
	    } else {
		SUFFIX = "";
	    }

            /* 201-x *** no names */
	    addTestChild( PREVTEST, "201"+SUFFIX,
			  newProperties( ParametersIds.RENAME_CLASSES, ((Float)i1).toString(),
					 ParametersIds.RENAME_PROPERTIES, ((Float)i1).toString() ) );
	    /* 202-x *** no names + no comments */
	    addTestChild( "201"+SUFFIX, "202"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_COMMENTS, FULL ) );
	    /* 248-x *** no names + no comments +  no hierarchy */	
	    addTestChild( "202"+SUFFIX, "248"+SUFFIX,
			  newProperties( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY ) );
	    /* 253-x *** no names + no comments + no hierarchy + no instance */
	    addTestChild( "248"+SUFFIX, "253"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	    /* 249-x *** no names + no comments + no instance */
	    addTestChild( "202"+SUFFIX, "249"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	    /* 250-x *** no names + no comments + no property */
	    addTestChild( "202"+SUFFIX, "250"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_PROPERTIES, FULL ) );
	    /* 254-x *** no names + no comments + no property + no hierarchy */
	    addTestChild( "250"+SUFFIX, "254"+SUFFIX,
			  newProperties( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY ) );
	    /* 262-x *** no names + no comments + no property + no hierarchy + no instance */
	    addTestChild( "254"+SUFFIX, "262"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	    /* 257-x *** no names + no comments + no property + no instance */
	    addTestChild( "250"+SUFFIX, "257"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	    /* 261-x *** no names + no comments + no property + expand */
	    addTestChild( "250"+SUFFIX, "261"+SUFFIX,
			  newProperties( ParametersIds.ADD_CLASSES, FULL ) );
	    /* 266-x *** no names + no comments + no property + expand + no instance */
	    addTestChild( "261"+SUFFIX, "266"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	    /* 260-x *** no names + no comments + no property + flatten */
	    addTestChild( "250"+SUFFIX, "260"+SUFFIX,
			  newProperties( ParametersIds.LEVEL_FLATTENED, "2" ) );
	    /* 265-x *** no names + no comments + no property + flatten + no instance */
	    addTestChild( "260"+SUFFIX, "265"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	    /* 251-x *** no names + no comments + flatten */
	    addTestChild( "202"+SUFFIX, "251"+SUFFIX,
			  newProperties( ParametersIds.LEVEL_FLATTENED, "2" ) );
	    /* 258-x *** no names + no comments + flatten + no instance */
	    addTestChild( "251"+SUFFIX, "258"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	    /* 252-x *** no names + no comments + expand */
	    addTestChild( "202"+SUFFIX, "252"+SUFFIX,
			  newProperties( ParametersIds.ADD_CLASSES, FULL ) );
	    /* 259-x *** no names + no comments + expand + no instance */
	    addTestChild( "252"+SUFFIX, "259"+SUFFIX,
			  newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
        }
        /* 221 *** no hierarchy */
	addTestChild( "101", "221",
		      newProperties( ParametersIds.NO_HIERARCHY, ParametersIds.NO_HIERARCHY ) );
	/* 232 *** no hierarchy + no instance */
	addTestChild( "221", "232",
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	/* 233 *** no hierarchy + no property */
	addTestChild( "221", "233",
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FULL ) );
	/* 241 *** no hierarchy + no property + no instance */
	addTestChild( "233", "241",
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
        /* 222 *** flatten */
	addTestChild( "101", "222",
		      newProperties( ParametersIds.LEVEL_FLATTENED, "2" ) );
	/* 237 *** flatten + no instance */
	addTestChild( "222", "237",
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
        /* 223 *** expand */
	addTestChild( "101", "223",
		      newProperties( ParametersIds.ADD_CLASSES, FULL ) );
	/* 238 *** expand + no instance */
	addTestChild( "223", "238", 
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL) );
        /* 224 *** no instance */
	addTestChild( "101", "224", 
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
        /* 225 *** no restrictions */
	addTestChild( "101", "225", 
		      newProperties( ParametersIds.REMOVE_RESTRICTIONS, FULL ) );
        /* 228 *** no property */
	addTestChild( "101", "228", 
		      newProperties( ParametersIds.REMOVE_PROPERTIES, FULL ) );
	/* 236 *** no property + no instance */
	addTestChild( "228", "236", 
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	/* 240 *** no property + expand */
	addTestChild( "228", "240", 
		      newProperties( ParametersIds.ADD_CLASSES, FULL ) );
	/* 247 *** no property + expand + no instance */
	addTestChild( "240", "247", 
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
	/* 239 *** no property + flatten */
	addTestChild( "228", "239", 
		      newProperties( ParametersIds.LEVEL_FLATTENED, FULL ) );
	/* 246 *** no property + flatten + no instance */
	addTestChild( "239", "246", 
		      newProperties( ParametersIds.REMOVE_INDIVIDUALS, FULL ) );
    }

    /*
*101 --> 201
*        201 --> 202
*                202 --> 248
*                        248 --> 253
*                202 --> 249
*                202 --> 250
*                        250 --> 254
*                                254 --> 262
*                        250 --> 257
*                        250 --> 261
*                                261 --> 266
*                        250 --> 260
*                                260 --> 265
*                202 --> 251
*                        251 --> 258
*                202 --> 252
*                        252 --> 259
//-------
*101 --> 221
*        221 --> 232
*        221 --> 233
*                233 --> 241
*101 --> 222
*        222 --> 237
*101 --> 223
*        223 --> 238
*101 --> 224
*101 --> 225
*101 --> 228
*        228 --> 236
*        228 --> 240
*                240 --> 247
*        228 --> 239
*                239 --> 246
    */

}

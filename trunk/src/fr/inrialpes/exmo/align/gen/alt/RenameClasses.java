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

package fr.inrialpes.exmo.align.gen.alt;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.vocabulary.OWL;

import java.util.Properties;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

public class RenameClasses extends RenameThings {

    public RenameClasses( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.RENAME_CLASSES );
	if ( p == null ) return null;
	float percentage = Float.parseFloat( p );
	modifiedModel = renameResource( false, true, percentage, true, false, false, 0 );
	return this; // useless
    };

}
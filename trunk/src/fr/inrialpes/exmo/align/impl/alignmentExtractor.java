/*
 * Copyright (C) INRIA Rh�ne-Alpes, 2004-2005
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

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Parameters;

/**
 * @author jpierson
 * JE: THIS CLASS SHOULD BE OBSOLETED!
 */

public class alignmentExtractor implements Extractor {

    public Alignment extract(String type, Parameters param) {
	BasicAlignment Al = new BasicAlignment();
	return ((Alignment)Al);}
    public void threshold (String type, Parameters param){}
}

/*
 * $Id: ParametersIds.java
 *
 * Copyright (C) 2003-2010, INRIA
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

/* This program represents the list of all the modifications
   that can be applied to the test generator
 */

package fr.inrialpes.exmo.align.gen;

public class ParametersIds {
    public static String ADD_SUBCLASS    = "addSubClass";               //adds random classes
    public static String REMOVE_SUBCLASS = "removeSubClass";            //removes random classes
    public static String REMOVE_PROPERTY = "removeProperty";            //removes random properties
    public static String REMOVE_COMMENT  = "removeComment";             //removes random comments
    public static String LEVEL_FLATTENED = "levelFlattened";            //flattens a level
    public static String ADD_PROPERTY    = "addProperty";               //adds random propeties
    public static String REMOVE_CLASSES  = "removeClasses";             //remove classes from level
    public static String ADD_CLASSES      = "addClasses";               //add c classes beginning from level l -> the value of this parameters should be:
                                                                        //beginning_level.number_of_classes_to_add
    public static String RENAME_PROPERTIES="renameProperties";          //renames properties
    public static String RENAME_CLASSES   ="renameClasses";		//renames classes
    public static String RENAME_RESOURCES = "renameResources";          //renames properties + classes
    public static String REMOVE_RESTRICTION= "removeRestriction";	//removes restrictions
    public static String REMOVE_INDIVIDUALS= "removeIndividuals";	//removes individuals
    public static String NO_HIERARCHY      = "noHierarchy";             //no hierarchy
}

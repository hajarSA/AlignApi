/*
 * $Id$
 *
 * Copyright (C) INRIA, 2012-2014
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
package fr.inrialpes.exmo.align.impl.renderer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.EDOALVisitor;
import fr.inrialpes.exmo.align.impl.edoal.InstanceExpression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;
import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Translate correspondences into Graph Patterns
 *
 */
// JE: create a string... problem with increment.
public abstract class GraphPatternRendererVisitor extends IndentedRendererVisitor implements EDOALVisitor {

    final static Logger logger = LoggerFactory.getLogger(GraphPatternRendererVisitor.class);
    protected static String FEDERATED_SERVICE_INTRODUCTION = "FEDERATED_SERVICE_BEGIN";
    protected static String FEDERATED_SERVICE_FINALIZATION = "FEDERATED_SERVICE_END";

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String, String> nslist = null;

    protected boolean ignoreerrors = false;
    protected static boolean blanks = false;
    protected boolean weakens = false;
    protected boolean corese = false;

    private boolean inClassRestriction = false;
    private Object valueRestriction = null;
    private boolean flagRestriction;

    private String instance = null;
    private String value = "";
    private String uriType = null;
    private String datatype = "";
    private Constructor op = null;
    private Integer nbCardinality = null;
    private String opOccurence = "";
    private int numberNs;
    private int fileIndex = 1;
    private String sub = "";
    protected String obj = "";
    private String strBGP = "";
    protected List<String> listBGP = null;
    private Set<String> subjectsRestriction = null;
    
    private Set<String> objectsRestriction = null;
    protected Hashtable<String, String> prefixList = null;

    private int varsIndexcount = 1;

    private boolean split = false;                                  // split each query in a file, not on the writer
    private String splitdir = "";                                   // directory where to put query files
    protected HashMap<Object, List<String>> queries;

    public GraphPatternRendererVisitor(PrintWriter writer) {
        super(writer);
        listBGP = new ArrayList<String>();
        subjectsRestriction = new HashSet<String>();
        objectsRestriction = new HashSet<String>();
        prefixList = new Hashtable<String, String>();
        prefixList.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        queries = new LinkedHashMap<Object, List<String>>();
    }

    
    protected void initStructure() {
        varsIndexcount = 1;
        strBGP = "";
        listBGP.clear();
        objectsRestriction.clear();
        flagRestriction = false;
    }

    protected void resetVariables(Expression expr, String s, String o) throws AlignmentException {
        if (expr instanceof ClassExpression) {
            resetVariables((ClassExpression) expr, s, o);
        } else if (expr instanceof PathExpression) {
            resetVariables((PathExpression) expr, s, o);
        } else if (expr instanceof InstanceExpression) {
            resetVariables((InstanceExpression) expr, s, o);
        } else {
            throw new AlignmentException("Cannot render as query : " + expr);
        }
    }

    protected void resetVariables(ClassExpression expr, String s, String o) {
        initStructure();
        resetVariables("?" + s, createVarName());
    }

    protected void resetVariables(PathExpression expr, String s, String o) {
        initStructure();
        resetVariables("?" + s, "?" + o);
    }

    protected void resetVariables(InstanceExpression expr, String s, String o) {
        initStructure();
        resetVariables("?" + s, createVarName());
    }

    protected void resetVariables(String s, String o) {
        sub = s;
        obj = o;
    }

    // JE2014: Why do we only create variables for obj?
    private String createVarName() {
        if (blanks) {
            obj = "_:o" + ++varsIndexcount;
        } else {
            obj = "?o" + ++varsIndexcount;
        }
        return obj;
    }

    protected String getGP() {
        return strBGP;
    }

    protected List<String> getBGP() {
        return listBGP;
    }

    public String registerPrefix(URI u) {
        String prefix = getPrefixDomain(u);
        String tag = getPrefixName(u);
        String shortCut = getOrGenerateNSPrefix(prefix);
        return shortCut + ":" + tag;
    }

    public String getPrefixDomain(URI u) {
        String str = u.toString();
        int index;
        if (str.contains("#")) {
            index = str.lastIndexOf("#");
        } else {
            index = str.lastIndexOf("/");
        }
        return str.substring(0, index + 1);
    }

    public String getPrefixName(URI u) {
        String str = u.toString();
        int index;
        if (str.contains("#")) {
            index = str.lastIndexOf("#");
        } else {
            index = str.lastIndexOf("/");
        }
        return str.substring(index + 1);
    }

    public String getOrGenerateNSPrefix(String namespace) {
        if (namespace.length() == 0) {
            return "";
        }
        String ns = prefixList.get(namespace);
        if (ns == null) {
            prefixList.put(namespace, ns = "ns" + numberNs++);
        }
        return ns;
    }
    
    
    protected final void split(boolean split, String splitdir){
        if(split){
            this.split = split;
            this.splitdir = splitdir;
        }
        else{
            this.split = false;
            this.splitdir = null;
        }
    }
    
    
    private static String cleanQuery(String query, String serviceName){
        if(serviceName == null || serviceName == "" ){
            return query.replaceAll(FEDERATED_SERVICE_INTRODUCTION, "").replaceAll(FEDERATED_SERVICE_FINALIZATION, "");
        }else{
            return query.replaceAll(FEDERATED_SERVICE_INTRODUCTION, String.format("\nSERVICE <%s> {", serviceName)).replaceAll(FEDERATED_SERVICE_FINALIZATION, "}\n");
        }
    }
    
    public void saveQuery(Object referer, String query) {
        //Query is stored in memory
        if(!queries.containsKey(referer)){
            queries.put(referer, new LinkedList<String>());
        }
        queries.get(referer).add(query);
        query = cleanQuery(query, null);
        if (split) {
            BufferedWriter out = null;
            try {
                FileWriter writer = new FileWriter(splitdir + "query" + fileIndex + ".rq");
                out = new BufferedWriter(writer);
                fileIndex++;
                out.write(query);
                if (out != null) // there was at least one file
                {
                    out.close();
                }
            } catch (IOException ioe) {
                logger.debug("IGNORED Exception", ioe);
            }
        }else{
            writer.println(query);
        }
    }
    
    /**
     * Produce Query only for local call.
     * @param referer
     * @param index
     * @return 
     */
    public String getQuery(Object referer, int index){
        return getQuery(referer, index, null);
    }
    
    public String getQuery(Object referer, int index, URI remoteURI){
        return cleanQuery(queries.get(referer).get(index), (remoteURI == null ? null : remoteURI.toString()));
    }

    protected String createPrefixList() {
        String result = "";
        for (String k : prefixList.keySet()) {
            result += "PREFIX " + prefixList.get(k) + ":<" + k + ">" + NL;
        }
        return result;
    }

    public void visit(final ClassId e) throws AlignmentException {
        if (e.getURI() != null) {
            String id = registerPrefix(e.getURI());
            if (!subjectsRestriction.isEmpty()) {
                Iterator<String> listSub = subjectsRestriction.iterator();
                while (listSub.hasNext()) {
                    String str = listSub.next();
                    strBGP += str + " rdf:type " + id + " ." + NL;
                }
                subjectsRestriction.clear();
            } else {
                strBGP += sub + " rdf:type " + id + " ." + NL;
            }
        }
    }

    public void visit(final ClassConstruction e) throws AlignmentException {
        op = e.getOperator();
        if (op == Constructor.OR) {
            int size = e.getComponents().size();
            for (final ClassExpression ce : e.getComponents()) {
                strBGP += "{" + NL;
                ce.accept(this);
                size--;
                if (size != 0) {
                    strBGP += "}" + " UNION " + NL;
                } else {
                    strBGP += "}" + NL;
                }
            }
        } else if (op == Constructor.NOT) {
            strBGP += "FILTER (NOT EXISTS {" + NL;
            for (final ClassExpression ce : e.getComponents()) {
                ce.accept(this);
            }
            strBGP += "})" + NL;
        } else {
            for (final ClassExpression ce : e.getComponents()) {
                ce.accept(this);
                if (weakens && !strBGP.equals("")) {
                    listBGP.add(strBGP);
                    strBGP = "";
                }
            }
        }
    }

    public void visit(final ClassValueRestriction c) throws AlignmentException {
        String str = "";
        instance = "";
        value = "";
        flagRestriction = true;
        c.getValue().accept(this);
        flagRestriction = false;

        if (!instance.equals("")) {
            valueRestriction = instance;
        } else if (!value.equals("")) {
            valueRestriction = value;
        }

        if (c.getComparator().getURI().equals(Comparator.GREATER.getURI())) {
            opOccurence = ">";
            inClassRestriction = true;
        }
        if (c.getComparator().getURI().equals(Comparator.LOWER.getURI())) {
            opOccurence = "<";
            inClassRestriction = true;
        }
        flagRestriction = true;
        c.getRestrictionPath().accept(this);
        flagRestriction = false;
        String temp = obj;
        if (inClassRestriction && !objectsRestriction.isEmpty()) {
            Iterator<String> listObj = objectsRestriction.iterator();
            if (op == Constructor.COMP) {
                String tmp = "";
                while (listObj.hasNext()) {
                    tmp = listObj.next();
                }
                str = "FILTER (" + tmp + opOccurence + valueRestriction + ")" + NL;
            } else {
                while (listObj.hasNext()) {
                    str += "FILTER (" + listObj.next() + opOccurence + valueRestriction + ")" + NL;
                }
            }
            strBGP += str;
        }
        valueRestriction = null;
        inClassRestriction = false;
        obj = temp;
        if (op == Constructor.AND) {
            createVarName();
        }
    }

    public void visit(final ClassTypeRestriction c) throws AlignmentException {
        String str = "";
        datatype = "";
        inClassRestriction = true;
        flagRestriction = true;
        c.getRestrictionPath().accept(this);
        flagRestriction = false;
        if (!objectsRestriction.isEmpty()) {
            Iterator<String> listObj = objectsRestriction.iterator();
            int size = objectsRestriction.size();
            if (size > 0) {
                str = "FILTER (datatype(" + listObj.next() + ") = ";
                visit(c.getType());
                str += "xsd:" + datatype;
            }
            while (listObj.hasNext()) {
                str += " && datatype(" + listObj.next() + ") = ";
                visit(c.getType());
                str += "xsd:" + datatype;
            }
            str += ")" + NL;

            strBGP += str;
        }
        objectsRestriction.clear();
        inClassRestriction = false;
    }

    public void visit(final ClassDomainRestriction c) throws AlignmentException {
        inClassRestriction = true;
        flagRestriction = true;
        c.getRestrictionPath().accept(this);
        flagRestriction = false;
        Iterator<String> listObj = objectsRestriction.iterator();
        while (listObj.hasNext()) {
            subjectsRestriction.add(listObj.next());
        }
        c.getDomain().accept(this);
        objectsRestriction.clear();
        inClassRestriction = false;
    }

    public void visit(final ClassOccurenceRestriction c) throws AlignmentException {
        String str = "";
        inClassRestriction = true;
        if (c.getComparator().getURI().equals(Comparator.EQUAL.getURI())) {
            nbCardinality = c.getOccurence();
            opOccurence = "=";
        }
        if (c.getComparator().getURI().equals(Comparator.GREATER.getURI())) {
            nbCardinality = c.getOccurence();
            opOccurence = ">";
        }
        if (c.getComparator().getURI().equals(Comparator.LOWER.getURI())) {
            nbCardinality = c.getOccurence();
            opOccurence = "<";
        }
        flagRestriction = true;
        c.getRestrictionPath().accept(this);
        flagRestriction = false;
        if (!objectsRestriction.isEmpty()) {
            Iterator<String> listObj = objectsRestriction.iterator();
            if (op == Constructor.COMP) {
                String tmp = "";
                while (listObj.hasNext()) {
                    tmp = listObj.next();
                }
                str += "FILTER(COUNT(" + tmp + ")" + opOccurence + nbCardinality + ")" + NL;
            } else {
                while (listObj.hasNext()) {
                    str += "FILTER(COUNT(" + listObj.next() + ")" + opOccurence + nbCardinality + ")" + NL;
                }
            }

            strBGP += str;
        }
        nbCardinality = null;
        opOccurence = "";
        inClassRestriction = false;
    }

    public void visit(final PropertyId e) throws AlignmentException {
        if (e.getURI() != null) {
            String id = registerPrefix(e.getURI());
            String temp = obj;
            if (valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction) {
                obj = "\"" + valueRestriction.toString() + "\"";
            }
            if (flagRestriction && inClassRestriction) {
                objectsRestriction.add(obj);
            }
            // createVarName(); //JE2014!
            strBGP += sub + " " + id + " " + obj + " ." + NL;
            obj = temp;
        }
    }

    public void visit(final PropertyConstruction e) throws AlignmentException {
        op = e.getOperator();
        if (op == Constructor.OR) {
            if (valueRestriction != null && !inClassRestriction) {
                obj = "\"" + valueRestriction.toString() + "\"";
            }
            int size = e.getComponents().size();
            for (final PathExpression re : e.getComponents()) {
                strBGP += "{" + NL;
                re.accept(this);
                size--;
                if (size != 0) {
                    strBGP += "}" + " UNION " + NL;
                } else {
                    strBGP += "}" + NL;
                }
            }
            objectsRestriction.add(obj);
        } else if (op == Constructor.NOT) {
            strBGP += "FILTER (NOT EXISTS {" + NL;
            for (final PathExpression re : e.getComponents()) {
                re.accept(this);
            }
            strBGP += "})" + NL;
        } else if (op == Constructor.COMP) {
            String tempSub = sub;
	    //if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
            //	obj = "_:o" + ++count;
            //}
            String tempObj = obj;
            int size = e.getComponents().size();
            for (final PathExpression re : e.getComponents()) {
                size--;
                // next object 
                if (size == 0) { // last step
                    if (valueRestriction != null && !inClassRestriction) {
                        obj = "\"" + valueRestriction.toString() + "\"";
                    } else {
                        obj = tempObj;
                    } // otherwise, generating intermediate variables...
                } else if (blanks && this.getClass() == SPARQLConstructRendererVisitor.class) {
                    obj = "_:o" + ++varsIndexcount;
                } else {
                    obj = "?o" + ++varsIndexcount;
                }
                // sub = last obj; obj = obj if last, var or blank otherwise
                re.accept(this); // p
                sub = obj; // sub <= last object
            }
            objectsRestriction.add(obj);
            obj = tempObj;
            sub = tempSub;
        } else {
            if (valueRestriction != null && !inClassRestriction) {
                obj = "\"" + valueRestriction.toString() + "\"";
            }
            int size = e.getComponents().size();
            for (final PathExpression re : e.getComponents()) {
                re.accept(this);
                size--;
                objectsRestriction.add(obj);
                if (size != 0 && valueRestriction == null) {
                    //createVarName();
                    obj = "?o" + ++varsIndexcount;
                }
                if (weakens && !strBGP.equals("") && !inClassRestriction) {
                    listBGP.add(strBGP);
                    strBGP = "";
                }
            }
        }
        obj = "?o" + ++varsIndexcount;
    }

    public void visit(final PropertyValueRestriction c) throws AlignmentException {
        String str = "";
        value = "";
        uriType = "";
        flagRestriction = true;
        c.getValue().accept(this);
        flagRestriction = false;
        if (c.getComparator().getURI().equals(Comparator.EQUAL.getURI())) {
            str = "FILTER (xsd:" + uriType + "(" + obj + ") = ";
        } else if (c.getComparator().getURI().equals(Comparator.GREATER.getURI())) {
            str = "FILTER (xsd:" + uriType + "(" + obj + ") > ";
        } else {
            str = "FILTER (xsd:" + uriType + "(" + obj + ") < ";
        }
        str += "\"" + value + "\")" + NL;

        strBGP += str;
        value = "";
        uriType = "";
    }

    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
        flagRestriction = true;
        c.getDomain().accept(this);
        flagRestriction = false;
    }

    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
        String str = "";
        if (!objectsRestriction.isEmpty()) {
            Iterator<String> listObj = objectsRestriction.iterator();
            int size = objectsRestriction.size();
            if (size > 0) {
                str = "FILTER (datatype(" + listObj.next() + ") = ";
                visit(c.getType());
                str += "xsd:" + datatype;
            }
            while (listObj.hasNext()) {
                str += " && datatype(" + listObj.next() + ") = ";
                visit(c.getType());
                str += "xsd:" + datatype;
            }
            str += ")" + NL;
            strBGP += str;
        }
        objectsRestriction.clear();
    }

    public void visit(final RelationId e) throws AlignmentException {
        if (e.getURI() != null) {
            String id = registerPrefix(e.getURI());
            strBGP += sub + " " + id + "";
            if (op == Constructor.TRANSITIVE && flagRestriction) {
                strBGP += "*";
            }
            if (valueRestriction != null && !inClassRestriction && op != Constructor.COMP && flagRestriction) {
                obj = valueRestriction.toString();
            }
            if (flagRestriction && inClassRestriction && op != Constructor.COMP) {
                objectsRestriction.add(obj);
            }
            strBGP += " " + obj + " ." + NL;
        }
    }

    public void visit(final RelationConstruction e) throws AlignmentException {
        op = e.getOperator();
        if (op == Constructor.OR) {
            int size = e.getComponents().size();
            if (valueRestriction != null && !inClassRestriction) {
                obj = valueRestriction.toString();
            }
            String temp = obj;
            for (final PathExpression re : e.getComponents()) {
                strBGP += "{" + NL;
                re.accept(this);
                obj = temp;
                size--;
                if (size != 0) {
                    strBGP += "}" + "UNION " + NL;
                } else {
                    strBGP += "}" + NL;
                }
            }
            objectsRestriction.add(obj);
        } else if (op == Constructor.AND) {
            if (valueRestriction != null && !inClassRestriction) {
                obj = valueRestriction.toString();
            }
            String temp = obj;
            for (final PathExpression re : e.getComponents()) {
                re.accept(this);
                obj = temp;
            }
            objectsRestriction.add(obj);
        } else if (op == Constructor.NOT) {
            strBGP += "FILTER (NOT EXISTS {" + NL;
            for (final PathExpression re : e.getComponents()) {
                re.accept(this);
            }
            strBGP += "})" + NL;
        } else if (op == Constructor.COMP) {
            int size = e.getComponents().size();
            String temp = sub;
	    //if ( blanks && this.getClass() == SPARQLConstructRendererVisitor.class ) {
            //obj = "_:o" + ++count;
            createVarName();
            //}
            for (final PathExpression re : e.getComponents()) {
                re.accept(this);
                size--;
                if (size != 0) {
                    sub = obj;
                    if (size == 1 && valueRestriction != null && !inClassRestriction) {
                        obj = valueRestriction.toString();
                    } else {
                        if (this.getClass() == SPARQLConstructRendererVisitor.class) {
                            createVarName();
                        }
                        objectsRestriction.add(obj);
                    }
                }
            }
            sub = temp;
        } else if (op == Constructor.INVERSE) {
            String tempSub = sub;
            for (final PathExpression re : e.getComponents()) {
                String temp = sub;
                sub = obj;
                obj = temp;
                re.accept(this);
                sub = tempSub;
            }
        } else if (op == Constructor.SYMMETRIC) {
            String tempSub = sub;
            for (final PathExpression re : e.getComponents()) {
                strBGP += "{" + NL;
                re.accept(this);
                objectsRestriction.add(obj);
                String temp = sub;
                sub = obj;
                obj = temp;
                strBGP += "} UNION {" + NL;
                re.accept(this);
                objectsRestriction.add(obj);
                strBGP += "}" + NL;
            }
            sub = tempSub;
        } else if (op == Constructor.TRANSITIVE) {
            for (final PathExpression re : e.getComponents()) {
                flagRestriction = true;
                re.accept(this);
                flagRestriction = false;
            }
        } else if (op == Constructor.REFLEXIVE) {
            for (final PathExpression re : e.getComponents()) {
                strBGP += "{" + NL;
                re.accept(this);
                strBGP += "} UNION {" + NL + "FILTER(" + sub + "=" + obj + ")" + NL + "}";
            }
        } else { // JE2014: THIS IS SUPPOSED TO BE A AND!
            if (valueRestriction != null && !inClassRestriction) {
                obj = valueRestriction.toString();
            }
            for (final PathExpression re : e.getComponents()) {
                re.accept(this);
                objectsRestriction.add(obj);
                if (weakens && !strBGP.equals("") && !inClassRestriction) {
                    listBGP.add(strBGP);
                    strBGP = "";
                }
            }
        }
        //obj = "?o" + ++count;    	
    }

    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
        String stemp = sub;
        sub = obj;
        flagRestriction = true;
        c.getCoDomain().accept(this);
        flagRestriction = false;
        sub = stemp;
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
        flagRestriction = true;
        c.getDomain().accept(this);
        flagRestriction = false;
    }

    public void visit(final InstanceId e) throws AlignmentException {
        if (e.getURI() != null) {
            String id = registerPrefix(e.getURI());
            if (flagRestriction) {
                instance = id;
            } else {
                strBGP += id + " ?p ?o1 ." + NL;
            }
        }
    }

    public void visit(final Value e) throws AlignmentException {
        if (e.getType() != null) {
            String str = e.getType().toString();
            int index;
            if (str.contains("#")) {
                index = str.lastIndexOf("#");
            } else {
                index = str.lastIndexOf("/");
            }
            uriType = str.substring(index + 1);
        }
        value = e.getValue();
        if (uriType != null && uriType.equals("")) {
            uriType = "string";
        }

    }

    public void visit(final Apply e) throws AlignmentException {
    }

    public void visit(final Transformation transf) throws AlignmentException {
    }

    public void visit(final Datatype e) throws AlignmentException {
        int index;
        if (e.getType().contains("#")) {
            index = e.getType().lastIndexOf("#");
        } else {
            index = e.getType().lastIndexOf("/");
        }
        datatype = e.getType().substring(index + 1);
    }

}

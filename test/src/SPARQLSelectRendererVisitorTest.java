import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.renderer.SPARQLSelectRendererVisitor;
import fr.inrialpes.exmo.align.test.Utils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import org.semanticweb.owl.align.Cell;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Nicolas Guillouet <nicolas.guillouet@inria.fr>
 */
public class SPARQLSelectRendererVisitorTest {

    @Test(groups = {"full", "impl", "raw"})
    public void QueryFromWithoutLinkkey() throws Exception {
        String alignmentFileName = "alignment3.rdf";
        EDOALAlignment alignment = Utils.loadAlignement(alignmentFileName);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        SPARQLSelectRendererVisitor renderer = new SPARQLSelectRendererVisitor(writer);
        Properties properties = new Properties();
        renderer.init(properties);
        alignment.render(renderer);
        assertEquals(alignment.nbCells(), 1);
        Enumeration<Cell> cells = alignment.getElements();
        Cell cell = cells.nextElement();

        String expectedQuery = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "SELECT DISTINCT ?s1 ?s2 \n"
                + "WHERE {\n"
                + "?s1 rdf:type ns0:RootElement .\n"
                + "?s2 rdf:type ns1:MusicalWork .\n"
                + "FILTER(?s1 != ?s2)\n"
                + "}\n";
        assertEquals(renderer.getQueryFromOnto1ToOnto2(cell), expectedQuery);

        expectedQuery = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors#>\n"
                + "PREFIX ns1:<http://purl.org/ontology/mo/>\n"
                + "SELECT DISTINCT ?s1 ?s2 \n"
                + "WHERE {\n"
                + "?s2 rdf:type ns0:RootElement .\n"
                + "?s1 rdf:type ns1:MusicalWork .\n"
                + "FILTER(?s1 != ?s2)\n"
                + "}\n";
        assertEquals(renderer.getQueryFromOnto2ToOnto1(cell), expectedQuery);
    }

    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"QueryFromWithoutLinkkey"})
    public void QueryFromSimpleLinkkeyFromIntersects() throws Exception {
        String alignmentFileName = "people_intersects_alignment.rdf";
        EDOALAlignment alignment = Utils.loadAlignement(alignmentFileName);
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        SPARQLSelectRendererVisitor renderer = new SPARQLSelectRendererVisitor(writer);
        Properties properties = new Properties();
        renderer.init(properties);
        alignment.render(renderer);
        assertEquals(alignment.nbCells(), 1);
        Enumeration<Cell> cells = alignment.getElements();
        Cell cell = cells.nextElement();
        //Without any service
//        PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
//        PREFIX ns0:<http://exmo.inrialpes.fr/connectors-core/>
//        PREFIX ns1:<http://xmlns.com/foaf/0.1/>
//        SELECT DISTINCT ?s1 ?s2 
//        WHERE {
//        ?s1 rdf:type ns0:Personne .
//        ?s2 rdf:type ns1:Person .
//        ?s1 ns0:nom ?o1 .
//        ?s2 ns1:givenName ?o2 .
//        FILTER(?s1 != ?s2 && ?o2 = ?o1)
//        }
        String query = renderer.getQueryFromOnto1ToOnto2(cell);
//        System.out.println("QUERY = " + query);
        Model values = Utils.loadValues(new String[]{"intersects_people_1.rdf", "intersects_people_2.rdf"});

        Query selectQuery = QueryFactory.create(query);
        QueryExecution selectQueryExec = QueryExecutionFactory.create(selectQuery, values);
        ResultSet results = selectQueryExec.execSelect();
        String[] expectedS1 = {
            "http://exmo.inrialpes.fr/connectors-data/people#alice_c1_1",
            "http://exmo.inrialpes.fr/connectors-data/people#alice_c2_1"};
        String[] expectedS2 = {
            "http://exmo.inrialpes.fr/connectors-data/people#alice_c1_2",
            "http://exmo.inrialpes.fr/connectors-data/people#alice_c2_2"};
        HashMap<String, Collection<String>> allResultValues = Utils.getResultValues(results);
        Collection<String> resultValues = allResultValues.get("s1");
        assertEquals(resultValues.size(), expectedS1.length);
        for (String expected : expectedS1) {
            assertTrue(resultValues.contains(expected));
        }

        resultValues = allResultValues.get("s2");
        assertEquals(resultValues.size(), expectedS2.length);
        for (String expected : expectedS2) {
            assertTrue(resultValues.contains(expected));
        }
        //Ok for other sens : 
        query = renderer.getQueryFromOnto2ToOnto1(cell);
        values = Utils.loadValues(new String[]{"intersects_people_1.rdf", "intersects_people_2.rdf"});

        selectQuery = QueryFactory.create(query);
        selectQueryExec = QueryExecutionFactory.create(selectQuery, values);
        results = selectQueryExec.execSelect();
        allResultValues = Utils.getResultValues(results);
        resultValues = allResultValues.get("s1");
        assertEquals(resultValues.size(), expectedS2.length);
        for (String expected : expectedS2) {
            assertTrue(resultValues.contains(expected), "For expected : " + expected);
        } 

        resultValues = allResultValues.get("s2");
        assertEquals(resultValues.size(), expectedS1.length);
        for (String expected : expectedS1) {
            assertTrue(resultValues.contains(expected));
        }
    }
//
//    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"QueryFromWithoutLinkkey", "QueryFromSimpleLinkkeyFromIntersects"})
//    public void QueryFromSimpleLinkkeyFromEquals() throws Exception {
//        String alignmentFileName = "people_equals_alignment.rdf";
//        EDOALAlignment alignment = Utils.loadAlignement(alignmentFileName);
//        StringWriter stringWriter = new StringWriter();
//        PrintWriter writer = new PrintWriter(stringWriter);
//        SPARQLSelectRendererVisitor renderer = new SPARQLSelectRendererVisitor(writer);
//        Properties properties = new Properties();
//        renderer.init(properties);
//        alignment.render(renderer);
//
//        assertEquals(alignment.nbCells(), 1);
//        Enumeration<Cell> cells = alignment.getElements();
//        Cell cell = cells.nextElement();
//        //Without any service
//        String expectedQuery = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
//                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors-core/>\n"
//                + "PREFIX ns1:<http://xmlns.com/foaf/0.1/>\n"
//                + "SELECT DISTINCT ?s1 ?s2 \n"
//                + "WHERE {\n"
//                + "?s1 rdf:type ns0:Personne .\n"
//                + "?s2 rdf:type ns1:Person .\n"
//                + "?s1 ns0:nom ?o1 .\n"
//                + "?s2 ns1:givenName ?o2 .\n"
//                + "MINUS { \n"
//                + "SELECT DISTINCT ?s1 ?s2 \n"
//                + "WHERE \n"
//                + "{ \n"
//                + "?s1 ns0:nom ?o1 .\n"
//                + "?s1 ns0:nom ?o2 .\n"
//                + "?s2 ns1:givenName ?o3 .\n"
//                + "FILTER(?s1 != ?s2 && ?o2 != ?o1 && ?o3 = ?o1 && NOT EXISTS {\n"
//                + "?s2 ns1:givenName ?o2 .\n"
//                + "}) \n"
//                + "} \n"
//                + "} \n"
//                + "MINUS {\n"
//                + "SELECT DISTINCT ?s1 ?s2 \n"
//                + "WHERE \n"
//                + "{ \n"
//                + "?s1 ns0:nom ?o1 .\n"
//                + "?s2 ns1:givenName ?o2 .\n"
//                + "?s2 ns1:givenName ?o3 .\n"
//                + "FILTER(?s1 != ?s2 && ?o2 != ?o3 && ?o2 = ?o1 && NOT EXISTS {\n"
//                + "?s1 ns0:nom ?o3 .\n"
//                + "})\n"
//                + "}\n"
//                + "}\n"
//                + "FILTER(?s1 != ?s2 && ?o2 = ?o1)\n"
//                + "}\n";
//        String query = renderer.getQueryFromOnto1ToOnto2(cell);//Where ?p1 is in onto1
//        System.out.println("QueryFromSimpleLinkkeyFromEquals expected Query : " + expectedQuery);
////        assertEquals(query, expectedQuery);
//
//        Model values = Utils.loadValues(new String[]{"equals_people_1.rdf", "equals_people_2.rdf"});
//        Query selectQuery = QueryFactory.create(query);
//        String[] expectedS1 = {
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c1_1"};
//        String[] expectedS2 = {
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c1_2",};
//        HashMap<String, Collection<String>> allResultValues = Utils.getResultValues(QueryExecutionFactory.create(selectQuery, values).execSelect());
//        Collection<String> resultValues = allResultValues.get("s1");
//        assertEquals(resultValues.size(), expectedS1.length);
//        for (String expected : expectedS1) {
//            assertTrue(resultValues.contains(expected), "For expected : " + expected);
//        }
//
//        resultValues = allResultValues.get("s2");
//        assertEquals(resultValues.size(), expectedS2.length);
//        for (String expected : expectedS2) {
//            assertTrue(resultValues.contains(expected), "For expected : " + expected);
//        }
//
//        //With from onto2ToOnto1
//        query = renderer.getQueryFromOnto2ToOnto1(cell);//Where ?p1 is in onto2
//        values = Utils.loadValues(new String[]{"equals_people_1.rdf", "equals_people_2.rdf"});
//        selectQuery = QueryFactory.create(query);
//        allResultValues = Utils.getResultValues(QueryExecutionFactory.create(selectQuery, values).execSelect());
//
//        resultValues = allResultValues.get("s1");
//        assertEquals(resultValues.size(), expectedS1.length);
//        for (String expected : expectedS2) {//Change here
//            assertTrue(resultValues.contains(expected), "For expected : " + expected);
//        }
//
//        resultValues = allResultValues.get("s2");
//        assertEquals(resultValues.size(), expectedS2.length);
//        for (String expected : expectedS1) {//Change here
//            assertTrue(resultValues.contains(expected), "For expected : " + expected);
//        }
//    }
//
//    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {})//"QueryFromSimpleLinkkeyFromIntersects"
//    public void QueryFromRelationLinkkeyAndIntersects() throws Exception {
//        String alignmentFileName = "people_relation_intersects_alignment.rdf";
//        EDOALAlignment alignment = Utils.loadAlignement(alignmentFileName);
//        StringWriter stringWriter = new StringWriter();
//        PrintWriter writer = new PrintWriter(stringWriter);
//        SPARQLSelectRendererVisitor renderer = new SPARQLSelectRendererVisitor(writer);
//        Properties properties = new Properties();
//        renderer.init(properties);
//        alignment.render(renderer);
//
//        assertEquals(alignment.nbCells(), 1);
//        Enumeration<Cell> cells = alignment.getElements();
//        Cell cell = cells.nextElement();
//
//        Model values = Utils.loadValues(new String[]{"intersects_people_1.rdf", "intersects_people_2.rdf"});
//
//        String query = renderer.getQueryFromOnto1ToOnto2(cell);
//        String expectedQuery = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
//                + "PREFIX ns0:<http://exmo.inrialpes.fr/connectors-core/>\n"
//                + "PREFIX ns1:<http://xmlns.com/foaf/0.1/>\n"
//                + "SELECT DISTINCT ?s1 ?s2 \n"
//                + "WHERE {\n"
//                + "?s1 rdf:type ns0:Personne .\n"
//                + "?s2 rdf:type ns1:Person .\n"
//                + "?s1 ns0:connait ?o1 .\n"
//                + "?s2 ns1:knows ?o2 .\n"
//                + "?o2 rdf:type ns1:Person .\n"
//                + "?o2 ns1:givenName ?o3 .\n"
//                + "FILTER(?s1 != ?s2 && ?o3 = ?o1)\n"
//                + "}";
//        System.out.println("QueryFromRelationLinkkeyAndIntersects expectedQuery : " + expectedQuery);
//        System.out.println("query : " + query);
////        assertEquals(expectedQuery, query);
//        Query selectQuery = QueryFactory.create(query);
//        QueryExecution selectQueryExec = QueryExecutionFactory.create(selectQuery, values);
//        ResultSet results = selectQueryExec.execSelect();
//        String[] expectedS1 = {
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c1_1",
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c2_1"};
//        String[] expectedS2 = {
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c1_2",
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c2_2"};
//
//        HashMap<String, Collection<String>> allResultValues = Utils.getResultValues(results);
//        Collection<String> resultValues = allResultValues.get("s1");
//        assertEquals(resultValues.size(), expectedS1.length);
//        for (String expected : expectedS1) {
//            assertTrue(resultValues.contains(expected));
//        }
//
//        resultValues = allResultValues.get("s2");
//        assertEquals(resultValues.size(), expectedS2.length);
//        for (String expected : expectedS2) {
//            assertTrue(resultValues.contains(expected));
//        }
//        //On other sens : 
//        query = renderer.getQueryFromOnto2ToOnto1(cell);
//        selectQuery = QueryFactory.create(query);
//        selectQueryExec = QueryExecutionFactory.create(selectQuery, values);
//        results = selectQueryExec.execSelect();
//
//        allResultValues = Utils.getResultValues(results);
//        resultValues = allResultValues.get("s1");
//        assertEquals(resultValues.size(), expectedS2.length);
//        for (String expected : expectedS2) {
//            assertTrue(resultValues.contains(expected));
//        }
//
//        resultValues = allResultValues.get("s2");
//        assertEquals(resultValues.size(), expectedS1.length);
//        for (String expected : expectedS1) {
//            assertTrue(resultValues.contains(expected));
//        }
//    }
//
//    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"QueryFromSimpleLinkkeyFromEquals"})
//    public void QueryFromRelationLinkkeyAndEquals() throws Exception {
//        //With Equals Linkkey => throw Exception, I already don't know to do it, could be performances innefficiente ...
//        String alignmentFileName = "people_relation_equals_alignment.rdf";
//        EDOALAlignment alignment = Utils.loadAlignement(alignmentFileName);
//        StringWriter stringWriter = new StringWriter();
//        PrintWriter writer = new PrintWriter(stringWriter);
//        SPARQLSelectRendererVisitor renderer = new SPARQLSelectRendererVisitor(writer);
//        Properties properties = new Properties();
//        renderer.init(properties);
//        alignment.render(renderer);
//
//        assertEquals(alignment.nbCells(), 1);
//        Enumeration<Cell> cells = alignment.getElements();
//        Cell cell = cells.nextElement();
//        String query = renderer.getQueryFromOnto1ToOnto2(cell);
////        System.out.println("QueryFromSimpleLinkkeyFromIntersects : " + query);
//        Model values = Utils.loadValues(new String[]{"equals_people_1.rdf", "equals_people_2.rdf"});
//
//        Query selectQuery = QueryFactory.create(query);
//        QueryExecution selectQueryExec = QueryExecutionFactory.create(selectQuery, values);
//        ResultSet results = selectQueryExec.execSelect();
//        String[] expectedS1 = {
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c2_1"};
//        String[] expectedS2 = {
//            "http://exmo.inrialpes.fr/connectors-data/people#alice_c2_2"};
//        HashMap<String, Collection<String>> allResultValues = Utils.getResultValues(results);
//        Collection<String> resultValues = allResultValues.get("s1");
//        assertEquals(resultValues.size(), expectedS1.length);
//        for (String expected : expectedS1) {
//            assertTrue(resultValues.contains(expected));
//        }
//
//        resultValues = allResultValues.get("s2");
//        assertEquals(resultValues.size(), expectedS2.length);
//        for (String expected : expectedS2) {
//            assertTrue(resultValues.contains(expected));
//        }
//
//        //Other sens
//        query = renderer.getQueryFromOnto2ToOnto1(cell);
////        System.out.println("QueryFromSimpleLinkkeyFromIntersects : " + query);
//        values = Utils.loadValues(new String[]{"equals_people_1.rdf", "equals_people_2.rdf"});
//
//        selectQuery = QueryFactory.create(query);
//        selectQueryExec = QueryExecutionFactory.create(selectQuery, values);
//        results = selectQueryExec.execSelect();
//        allResultValues = Utils.getResultValues(results);
//        resultValues = allResultValues.get("s1");
//        assertEquals(resultValues.size(), expectedS2.length);
//        for (String expected : expectedS2) {
//            assertTrue(resultValues.contains(expected));
//        }
//
//        resultValues = allResultValues.get("s2");
//        assertEquals(resultValues.size(), expectedS1.length);
//        for (String expected : expectedS1) {
//            assertTrue(resultValues.contains(expected));
//        }
//
//    }

//    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"QueryFromWithoutLinkkey", "QueryFromSimpleLinkkeyFromEquals"})
//    public void QueryFromSimpleLinkkeyFromIntersectsAndRelation() throws Exception {
//        fail("HAVE TODO : with (in + eq) => OPTIONAL (with SERVICE call on remote) / many correspondances / With transformations / On local (FROM ...) or remote sparql endpoint (SERVICE) ");
//    }
}

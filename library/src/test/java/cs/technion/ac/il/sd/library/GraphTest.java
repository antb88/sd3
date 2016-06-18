package cs.technion.ac.il.sd.library;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * Created by michal on 01/06/2016.
 */
public class GraphTest {

    Graph<Integer, String> graph;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    @Before
    public void before(){
        graph = new Graph<>();
    }

    @Test
    public void addVertex() throws Exception {
        assertEquals(0, graph.size());
        graph.addVertex(1, "one");
        assertEquals(1, graph.size());
        graph.addVertex(1, "one");
        assertEquals(1, graph.size());
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        assertEquals(3, graph.size());
    }

    /**
     * 1->2->3  4
     * @throws Exception
     */
    @Test
    public void addEdge() throws Exception {
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);

        assertTrue(graph.doesEdgeExists(1,2));
        assertTrue(graph.doesEdgeExists(2,3));
        assertFalse(graph.doesEdgeExists(1,3));
        assertFalse(graph.doesEdgeExists(2,1));
        assertFalse(graph.doesEdgeExists(4,2));
        assertFalse(graph.doesEdgeExists(3,4));
        assertFalse(graph.doesEdgeExists(3,2));
    }

    @Test
    public void removeVertex() throws Exception {
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);

        graph.removeVertex(4);
        assertTrue(graph.doesEdgeExists(1,2));
        assertTrue(graph.doesEdgeExists(2,3));
        assertFalse(graph.doesEdgeExists(1,3));
        assertFalse(graph.doesEdgeExists(2,1));
        assertFalse(graph.doesEdgeExists(4,2));
        assertFalse(graph.doesEdgeExists(3,4));
        assertFalse(graph.doesEdgeExists(3,2));
        assertEquals(3, graph.size());

        graph.removeVertex(2);
        assertFalse(graph.doesEdgeExists(1,2));
        assertFalse(graph.doesEdgeExists(2,3));
        assertFalse(graph.doesEdgeExists(2,1));
        assertFalse(graph.doesEdgeExists(4,2));
        assertFalse(graph.doesEdgeExists(3,2));
        assertEquals(2, graph.size());

    }

    @Test
    public void getSources() throws Exception {
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);

        LinkedList<String> sources = graph.getSources();
        assertEquals(2, sources.size());
        assertTrue(sources.contains("one"));
        assertTrue(sources.contains("four"));
    }

    @Test
    public void getTarget() throws Exception {
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);

        LinkedList<String> targets = graph.getTargets();
        assertEquals(2, targets.size());
        assertTrue(targets.contains("three"));
        assertTrue(targets.contains("four"));
    }

    @Test
    public void removeEdge() throws Exception {
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);

        assertTrue(graph.doesEdgeExists(1,2));
        assertTrue(graph.doesEdgeExists(2,3));

        graph.removeEdge(1,2);
        assertFalse(graph.doesEdgeExists(1,2));
        assertTrue(graph.doesEdgeExists(2,3));

        graph.removeEdge(2,3);
        assertFalse(graph.doesEdgeExists(2,3));
    }

    /**
     *      1-> 3
     *     / \
     *    v   v
     *   4 -> 2
     */
    @Test
    public void toposort() throws Exception {
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);
        graph.addEdge(1,4);
        graph.addEdge(4,2);

        LinkedList<String> topo = graph.toposort();
        assertEquals(4, topo.size());
        assertEquals("one", topo.get(0));
        assertEquals("four", topo.get(1));
        assertEquals("two", topo.get(2));
        assertEquals("three", topo.get(3));
    }

    /**
     *      1
     *     / \
     *    v   v
     *   4 -> 2 -> 3
     */
    @Test
    public void isReachable() throws Exception {
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);
        graph.addEdge(1,4);
        graph.addEdge(4,2);

        assertTrue(graph.isReachable(1,2));
        assertFalse(graph.isReachable(3,4));
        assertTrue(graph.isReachable(4,2));
        assertFalse(graph.isReachable(3,1));

    }

    @Test
    public void getVertices() throws Exception{
        graph.addVertex(1, "one");
        graph.addVertex(2, "two");
        graph.addVertex(3, "three");
        graph.addVertex(4, "four");

        graph.addEdge(1,2);
        graph.addEdge(2,3);
        graph.addEdge(1,4);
        graph.addEdge(4,2);

        LinkedList<String> vertices = graph.getVertices();
        assertTrue(vertices.contains("one"));
        assertTrue(vertices.contains("two"));
        assertTrue(vertices.contains("three"));
        assertTrue(vertices.contains("four"));

        graph.removeVertex(3);
        vertices = graph.getVertices();
        assertTrue(vertices.contains("one"));
        assertTrue(vertices.contains("two"));
        assertFalse(vertices.contains("three"));
        assertTrue(vertices.contains("four"));
    }

    @Test
    public void reachableFromVertex() {
        Graph<Integer, Integer> $ = createComplex();
        assertEquals($.getAllReachableFrom(5), Sets.newHashSet(5,2,9,10,11));
        assertEquals($.getAllReachableFrom(3), Sets.newHashSet(3,8,9,10));
        assertEquals($.getAllReachableFrom(7), Sets.newHashSet(7,8,2,9,10,11));
        assertEquals($.getAllReachableFrom(8), Sets.newHashSet(8,9));
        assertEquals($.getAllReachableFrom(10), Sets.newHashSet(10));
        assertEquals($.getAllReachableFrom(11), Sets.newHashSet(2,9,10,11));
        assertEquals($.getAllReachableFrom(2), Sets.newHashSet(2));
        assertEquals($.getAllReachableFrom(9), Sets.newHashSet(9));
    }

    @Test
    public void removeIncomingEdges() {
        Graph<Integer, Integer> $ = createComplex();
        $.removeIncomingEdgesOf(11);
        assertFalse("should not contain edge 5->11", $.doesEdgeExists(5,11));
        assertFalse("should not contain edge 7->11", $.doesEdgeExists(7,11));
    }
    @Test
    public void addEdgesToVertex() {
        Graph<Integer,Integer> $ = createComplex();
        assertFalse("should not contain edge 3->11", $.doesEdgeExists(3,11));
        assertFalse("should not contain edge 8->11", $.doesEdgeExists(8,11));
        assertFalse("should not contain edge 3->11", $.doesEdgeExists(3,11));
        $.addEdgesTo(11, Sets.newHashSet(3,8,10));
        assertTrue("should  contain edge 3->11", $.doesEdgeExists(3,11));
        assertTrue("should contain edge 8->11", $.doesEdgeExists(8,11));
        assertTrue("should contain edge 3->11", $.doesEdgeExists(3,11));
    }

    private Graph<Integer, Integer> createComplex() {
        Graph<Integer,Integer> g = new Graph<>();
        g.addVertex(5,5);
        g.addVertex(7,7);
        g.addVertex(3,3);
        g.addVertex(11,11);
        g.addVertex(8,8);
        g.addVertex(2,2);
        g.addVertex(9,9);
        g.addVertex(10,10);
        g.addEdge(5, 11);
        g.addEdge(11, 2);
        g.addEdge(11, 9);
        g.addEdge(11, 10);
        g.addEdge(7, 11);
        g.addEdge(7, 8);
        g.addEdge(8, 9);
        g.addEdge(3, 8);
        g.addEdge(3, 10);
        return g;
    }


}
package library.graph;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import library.graph.exception.ChildAlreadyExistsException;
import library.graph.exception.CycleException;
import library.graph.exception.NodeAlreadyExistsException;
import library.graph.exception.NodeDoesntExistsException;

public class GraphTest {
	private final String unexpeceted = "Unexpeceted exception";

	@Test
	public void canAddNodeOnce(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(5);
		} catch (Exception e) {
		}
		Assert.assertTrue(graph.doesNodeExist(5));
		
	}

	@Test
	public void cantAddSameNodeTwice() throws NodeAlreadyExistsException {
		Graph<Integer> graph = new Graph<Integer>();
		graph.addNode(5);
		graph.addNode(5);
	}
	
	@Test
	public void canAddChildeToNodeOnce(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(5);
			graph.addNode(6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			graph.addEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		for(Node<Integer> n : graph.getNode(5).getChildren()){
			Assert.assertTrue(n.getData() == 6);
		}
		
	}
	
	@Test(expected = AssertionError.class)
	public void cantAddNotExistingChild() throws NodeDoesntExistsException{
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(5);
		}catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			graph.addEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
	}
	
	@Test(expected = AssertionError.class)
	public void cantAddNotExistingFather() throws NodeDoesntExistsException{
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(5);
		}catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			graph.addEdge(6,5);
		} catch (Exception e) {
			fail(unexpeceted);
		}
	}
	
	
	@Test(expected = AssertionError.class)
	public void cantAddChildToNodeTwice() throws ChildAlreadyExistsException {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(5);
			graph.addNode(6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		
		try {
			graph.addEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			graph.addEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
	}
	
	

	@Test
	public void canDeleteChildFromNode() {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(5);
			graph.addNode(6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		
		try {
			graph.addEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			graph.deleteEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		Assert.assertTrue(graph.doesNodeExist(6));
		Assert.assertTrue(!graph.getNode(5).getChildren().contains(6));

	}
	
	@Test
	public void cantDeleteSameChildTwice() {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(5);
			graph.addNode(6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		
		try {
			graph.addEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			graph.deleteEdge(5, 6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
	}

	@Test(expected = CycleException.class)
	public void checkTopologicSortCycle2Nodes() throws CycleException {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(1);
			graph.addNode(2);
			graph.addEdge(1, 2);
			graph.addEdge(2, 1);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		graph.topologicSort();
	}
	
	@Test(expected = CycleException.class)
	public void checkTopologicSortCycleMoreThen2Nodes() throws CycleException {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(1);
			graph.addNode(2);
			graph.addNode(3);
			graph.addNode(4);
			graph.addNode(5);
			graph.addNode(6);
			graph.addNode(7);
			graph.addEdge(1, 2);
			graph.addEdge(2, 3);
			graph.addEdge(3, 1);
			graph.addEdge(4,1);
			graph.addEdge(5,6);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		graph.topologicSort();
	}
	
	@Test
	public void checkTopologicSortOneNode(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(6);
			graph.addNode(2);
			graph.addNode(1);
			graph.addNode(4);

		} catch (Exception  e) {
			fail(unexpeceted);
		}
		try {
			List<Integer> sorted = graph.topologicSort();
			Integer[] verify = {1,2,4,6};
			Integer node;
			for(int i=0;i<4;i++) {
				node = sorted.get(i);
				Assert.assertEquals(node,verify[i]);
			}
		} catch (CycleException e) {
			fail(unexpeceted);
		}
	}
	@Test
	public void checkTopologicSortNoCycle(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(6);
			graph.addNode(2);
			graph.addNode(1);
			graph.addNode(4);
			graph.addEdge(6, 2);
			graph.addEdge(6, 1);
			graph.addEdge(2, 1);

		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			List<Integer> sorted = graph.topologicSort();
			Integer[] verify = {4,6,2,1};
			Integer node;
			for(int i=0;i<4;i++) {
				node = sorted.get(i);
				Assert.assertEquals(node,verify[i]);
			}
		} catch (CycleException e) {
			fail(unexpeceted);
		}
	}
	
	@Test
	public void checkTopologicSort2Compoment(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(3);
			graph.addNode(2);
			graph.addNode(1);
			graph.addNode(4);
			graph.addEdge(1, 2);
			graph.addEdge(3, 4);

		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			List<Integer> sorted = graph.topologicSort();
			Integer[] verify = {1,3,2,4};
			Integer node;
			for(int i=0;i<4;i++) {
				node = sorted.get(i);
				Assert.assertEquals(node,verify[i]);
			}
		} catch (CycleException e) {
			fail(unexpeceted);
		}
	}
	
	

	@Test
	public void checkTopologicSortManyNodes(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			for(int i=1;i<=10000;i++){
				graph.addNode(i);
			}
			for(int i=1;i<10000;i++){
				graph.addEdge(i, i+1);
			}
			

		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			List<Integer> sorted = graph.topologicSort();
			Integer node;
			for(int i=1;i<=10000;i++) {
				node = sorted.get(i-1);
				Assert.assertEquals(node.intValue(),i);
			}
		} catch (CycleException e) {
			fail(unexpeceted);
		}
	}
	
	@Test
	public void checkTopologicSortStar(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			for(int i=1;i<=10000;i++){
				graph.addNode(i);
			}
			for(int i=2;i<=10000;i++){
				graph.addEdge(1, i);
			}
		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			List<Integer> sorted = graph.topologicSort();
			Integer node;
			for(int i=1;i<=10000;i++) {
				node = sorted.get(i-1);
				Assert.assertEquals(node.intValue(),i);
			}
		} catch (CycleException e) {
			fail(unexpeceted);
		}
	}
	@Test
	public void twoTopologicSortToCheckIfCopyGraphWorkFine(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(3);
			graph.addNode(2);
			graph.addNode(1);
			graph.addNode(4);
			graph.addEdge(1, 2);
			graph.addEdge(3, 4);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			List<Integer> sorted1 = graph.topologicSort();
			Integer[] verify = {1,3,2,4};
			Integer node;
			for(int i=0;i<4;i++) {
				node = sorted1.get(i);
				Assert.assertEquals(node,verify[i]);
			}
			List<Integer> sorted2 = graph.topologicSort();
			for(int i=0;i<4;i++) {
				node = sorted2.get(i);
				Assert.assertEquals(node,verify[i]);
			}
		} catch (CycleException e) {
			fail(unexpeceted);
		}
	}
	
	@Test
	public void twoTopologicSortToCheckIfChangingGraphIsOK(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(3);
			graph.addNode(2);
			graph.addNode(1);
			graph.addNode(4);
			graph.addEdge(1, 2);
			graph.addEdge(3, 4);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		try {
			List<Integer> sorted1 = graph.topologicSort();
			Integer[] verify = {1,3,2,4};
			Integer node;
			for(int i=0;i<4;i++) {
				node = sorted1.get(i);
				Assert.assertEquals(node,verify[i]);
			}
			graph.addEdge(2, 3);
			List<Integer> sorted2 = graph.topologicSort();
			Integer[] verify2 = {1,2,3,4};
			for(int i=0;i<4;i++) {
				node = sorted2.get(i);
				Assert.assertEquals(node,verify2[i]);
			}
		} catch (Exception e) {
			fail(unexpeceted);
		}
	}
	
	@Test
	public void checkAnccetorsOfChain(){
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(1);
			graph.addNode(2);
			graph.addNode(3);
			graph.addEdge(1, 2);
			graph.addEdge(2, 3);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		Map<Integer, Set<Integer>> map=null;
		try {
			map = graph.getAncestorsByTopologicalOrder();
		} catch (CycleException e) {
			fail(unexpeceted);
		}
		Set<Integer> set = new  HashSet<Integer>();
		Assert.assertEquals(map.get(1),set);
		set.add(1);
		Assert.assertEquals(map.get(2),set);
		set.add(2);
		Assert.assertEquals(map.get(3), set);
	}
	@Test
	public void checkAncestorsOfSimpleTree() {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(1);
			graph.addNode(2);
			graph.addNode(3);
			graph.addEdge(1, 2);
			graph.addEdge(1, 3);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		Map<Integer, Set<Integer>> map=null;
		try {
			map = graph.getAncestorsByTopologicalOrder();
		} catch (CycleException e) {
			fail(unexpeceted);
		}
		Assert.assertEquals(map.get(1), new HashSet<Integer>());
		Set<Integer> set = new  HashSet<Integer>();
		set.add(1);
		Assert.assertEquals(map.get(2), set);
		Assert.assertEquals(map.get(3),set);
		
	}
	@Test(expected = CycleException.class )
	public void checkCycleGraphGetAncestorsFails() throws CycleException {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(1);
			graph.addNode(2);
			graph.addEdge(1, 2);
			graph.addEdge(2, 1);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		Map<Integer, Set<Integer>> map = graph.getAncestorsByTopologicalOrder();		
	}
	
	@Test
	public void checkGetAncestorsReturnsAllFathers() {
		Graph<Integer> graph = new Graph<Integer>();
		try {
			graph.addNode(1);
			graph.addNode(2);
			graph.addNode(3);
			graph.addEdge(1, 3);
			graph.addEdge(2, 3);
		} catch (Exception e) {
			fail(unexpeceted);
		}
		Map<Integer, Set<Integer>> map=null;
		try {
			map = graph.getAncestorsByTopologicalOrder();
		} catch (CycleException e) {
			fail(unexpeceted);
		}
		Set<Integer> set = new  HashSet<Integer>();
		set.add(1);
		set.add(2);
		Assert.assertEquals(set, map.get(3));
	}
	
}

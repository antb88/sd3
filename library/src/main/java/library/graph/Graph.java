package library.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import library.graph.exception.ChildAlreadyExistsException;
import library.graph.exception.CycleException;
import library.graph.exception.NodeAlreadyExistsException;
import library.graph.exception.NodeDoesntExistsException;
public class Graph<T>{
	
	
	private Map<T,Node<T>> nodes;
	private List<Pair<Node<T>,Node<T>> > edges;
	
	private final String fatherErrorMsg = "Father doesn't exist";
	private final String childErrorMsg = "Child doesn't exist";
	
	public Graph() {
		nodes = new HashMap<T,Node<T>>();
		edges = new ArrayList<Pair<Node<T>,Node<T>> >();
	}
	
	public Set<T> getNodesData() {
		return nodes.keySet();
	}
	
	public Integer getNumOfNodes() {
		return nodes.size();
	}
	
	public boolean doesNodeExist(T data){
		return nodes.containsKey(data);
	}
	
	public void addNode(T data) throws NodeAlreadyExistsException {
		Node<T> newNode = new Node<T>(data);
		if(nodes.containsKey(data)) {
			throw new NodeAlreadyExistsException("Error");
		}
		nodes.put(data,newNode);
	}
	
	private Node<T> getNodeFromT(T data,String msg) throws NodeDoesntExistsException {
		Node<T> node = nodes.get(data);
		if(node==null) {
			throw new NodeDoesntExistsException(msg);
		}
		return node;
	}
	
	public void addEdge(T father,T child) throws NodeDoesntExistsException, ChildAlreadyExistsException {	
		Node<T> fatherNode = getNodeFromT(father,fatherErrorMsg);
		Node<T> childNode = getNodeFromT(child,childErrorMsg);
		fatherNode.addChild(childNode);
		edges.add(new Pair<Node<T>,Node<T>>(fatherNode, childNode));
	}
	
	public Node<T> getNode(T data){
		return nodes.get(data);
	}
	
	public void deleteEdge(T father,T child) throws NodeDoesntExistsException {
		Node<T> fatherNode = getNodeFromT(father,fatherErrorMsg);
		Node<T> childNode = getNodeFromT(child,childErrorMsg);
		fatherNode.deleteChild(child);
		childNode.deleteFather(father);
		edges.remove(new Pair<Node<T>,Node<T>>(fatherNode,childNode));
	}
	
	public List<T> getChildren(T element) throws NodeDoesntExistsException {
		List<T> list = new LinkedList<T>();
		for(Node<T> node: getNodeFromT(element,fatherErrorMsg).getChildren()) {
			list.add(node.getData());
		}
		return list;
	}
	
	public List<T> getFathers(T element) throws NodeDoesntExistsException {
		List<T> list = new LinkedList<T>();
		for(Node<T> node: getNodeFromT(element,fatherErrorMsg).getFathers()) {
			list.add(node.getData());
		}
		return list;
	}
	
	
	
	private Graph<T> copyGraph(){
		Graph<T> newGraph =  new Graph<T>();
		for(Node<T> n : nodes.values()){
			try {
				newGraph.addNode(n.getData());
			}
			catch (NodeAlreadyExistsException e) {
				
			}
		}
		for (Pair<Node<T>, Node<T>> entry : edges) {
		    Node<T> father = entry.getLeft();
		    Node<T> son = entry.getRight();
		    try {
				newGraph.addEdge(father.getData(), son.getData());
			} catch (NodeDoesntExistsException | ChildAlreadyExistsException e) {
			}
		}
		return newGraph;
	}
	
	public List<T> getSources() {
		List<T> sources = new ArrayList<T>();
		for(Node<T> node : nodes.values()){
			if(node.getFathers().size() == 0){
				sources.add(node.getData());
			}
		}
		return sources;
	}
	
	public List<T> topologicSort() throws CycleException {
		Graph<T> newGraph = this.copyGraph();
		List<T> res = new ArrayList<T>();
		List<Node<T>> sources = new ArrayList<Node<T>>();
		for(Node<T> node : newGraph.nodes.values()){
			if(node.getFathers().size() == 0){
				sources.add(node);
			}
		}
		while(!sources.isEmpty()){
			Node<T> n = sources.get(0);
			sources.remove(0);
			res.add(res.size(), n.getData());
			for(Iterator<Node<T>> it = n.getChildren().iterator(); it.hasNext();){
				Node<T> son = it.next();
				newGraph.edges.remove(new Pair<Node<T>,Node<T>>(n, son));
				it.remove();
				son.deleteFather(n.getData());
				if(son.getFathers().size() == 0){
					sources.add(son);
				}	
			}
		}
		if(newGraph.edges.size() != 0){
			throw new CycleException("Error");
		}
		return res;
	} 
	/*
	 * return LinkedHashMap
	 */
	public Map<T,Set<T>> getAncestorsByTopologicalOrder() throws CycleException{
		Map<T,Set<T>> map = new LinkedHashMap<T,Set<T>>();
		/*for(T n : nodes.keySet()){
			map.put(n, new HashSet<T>());
		}*/
		List<T> topologicSort = topologicSort();
		for(T t : topologicSort){
			map.put(t, new HashSet<T>());
			Node<T> n = nodes.get(t);
			for(Node<T> father : n.getFathers()){
				map.get(t).add(father.getData());
				map.get(t).addAll(map.get(father.getData()));
			}
		}
		return map;
	}
	public void printGraph() {
		for(Node<T> node: nodes.values()) {
			System.out.println("Node: " + node.getData().toString());
			for(Node<T> c: node.getChildren()) {
				System.out.println("----> " + c.getData().toString());
			}
		}
	}
}
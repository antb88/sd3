package library.graph;

import java.util.ArrayList;
import java.util.List;

import library.graph.exception.ChildAlreadyExistsException;

class Node<T> {
	private T data;
	private List<Node<T> > children;
	private List<Node<T>> fathers;
	Node(T data) {
		this.data=data;
		this.children = new ArrayList<Node<T>>();
		this.fathers = new ArrayList<Node<T>>();
	}
	Node(T data,List<Node<T>> children) {
		this.data=data;
		this.children=children;
	}
	T getData() {
		return data;
	}
	void setData(T data) {
		this.data=data;
	}
	
	List<Node<T>> getChildren() {
		return children;
	}
	List<Node<T>> getFathers() {
		return fathers;
	}
	void addChild(Node<T> child) throws ChildAlreadyExistsException {
		if(children.contains(child)) {
			throw new ChildAlreadyExistsException("Error");
		}
		children.add(child);
		child.fathers.add(this);
	}
	
	void deleteChild(T data) {
		Node<T> nodeToDelete = new Node<T>(data);
		children.remove(nodeToDelete);
	}
	
	void deleteFather(T data){
		Node<T> nodeToDelete = new Node<T>(data);
		fathers.remove(nodeToDelete);
	}
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null)
	        return false;
	    if (!(obj instanceof Node<?>))
	        return false;
	    Node<T> other = (Node<T>) obj;
	    return this.data.equals(other.data);
	}
	
	
}

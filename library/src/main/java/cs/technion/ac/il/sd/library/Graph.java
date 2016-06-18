package cs.technion.ac.il.sd.library;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by michal on 31/05/2016.
 */


public class Graph<K,T> {


    public void addEdgesTo(K toKey, Set<K> fromKeys) {
        fromKeys.forEach(k -> addEdge(k,toKey));
    }

    public void removeIncomingEdgesOf(K key) {
       Set<K> preds = adjacency.get(key)
               .getPredecessors()
               .stream()
               .collect(Collectors.toSet());
        preds.forEach(k -> removeEdge(k,key));
    }

    public Set<T> getAllReachableFrom(K key) {

        Set<T> reachable = new HashSet<>();
        Set<K> traversed = new HashSet<>();
        List<K> queue = new LinkedList<>();

        if (adjacency.containsKey(key)) {
            queue.add(key);
        }
        while (!queue.isEmpty()) {
            K k = queue.remove(0);
            reachable.add(adjacency.get(k).getData());
            if (!traversed.contains(k)) {
                queue.addAll(adjacency.get(k).getSuccessors());
                traversed.add(k);
            }
        }
        return reachable;
    }


    /**
     * A class representing a vertex and his neighbours.
     * @param <K> - Key
     * @param <T> - Data
     */
    private class Vertex<K,T> implements Cloneable{
        LinkedList<K> successors;
        LinkedList<K> predecessors;
        T data;

        Vertex(T data) {
            this.data = data;
            successors = new LinkedList<>();
            predecessors = new LinkedList<>();
        }

        /**
         * adds an edge from this vertex to key
         * @param key - key of vertex
         */
        void addSuccessor(K key) {
            successors.add(key);
            //v.predecessors.add(this); //TODO
        }

        /**
         * adds an edge from vertex represented by key to this vertex.
         * @param key - key of vertex
         */
        void addPredecessor(K key) {
            predecessors.add(key);
        }

        T getData() {
            return data;
        }

        /**
         * @return - the out degree of the vertex.
         */
        public int dout() {
            return successors.size();
        }

        /**
         * @return - the in degree of the vertex.
         */
        public int din() {
            return predecessors.size();
        }

        /**
         * removes the edge from this vertex to key.
         * @param key - key of vertex.
         */
        public void removeSuccessor(K key) {
            successors.remove(key);
        }

        /**
         * removes the edge from key to this vertex.
         * @param key = key of vertex.
         */
        public void removePredecessor(K key){
            predecessors.remove(key);
        }

        /**
         * @param key - key of vertex.
         * @return - true iff there is an edge from this vertex to key.
         */
        public boolean connectedTo(K key){
            return successors.contains(key);
        }

        /**
         * @return - list of all the successors of this vertex in the graph.
         */
        public LinkedList<K> getSuccessors(){
            return successors;
        }

        /**
         * @return - list of all the predecessors of this vertex in the graph.
         */
        public LinkedList<K> getPredecessors(){
            return predecessors;
        }
    }

    HashMap<K, Vertex<K,T>> adjacency;

    public Graph() {
        adjacency = new HashMap<K, Vertex<K,T>>();
    }

    /**
     *  Copy constructor.
     * @param graph - the graph to be copied.
     */
    public Graph(Graph<K,T> graph){
        adjacency = new HashMap<K, Vertex<K,T>>();
        for (Map.Entry<K, Vertex<K,T>> v : graph.adjacency.entrySet()){
            addVertex(v.getKey(), v.getValue().getData());
        }
        for (Map.Entry<K, Vertex<K,T>> v : graph.adjacency.entrySet()){
            for(K u : v.getValue().getSuccessors()){
                addEdge(v.getKey(),u);
            }
        }

    }

    /**
     * Adds new vertex to the graph.
     * @param key - the key of the new vertex.
     * @param data - the data of the new vertex.
     */
    public void addVertex(K key, T data) {
        Vertex v = new Vertex(data);
        if (adjacency.containsKey(key)) {
            return;
        }
        adjacency.put(key, v);
    }




    /**
     * Adds new edge to the graph.
     * @param key1 - the key of the source vertex.
     * @param key2 - the key of the target vertex.
     */
    public void addEdge(K key1, K key2) {
        Vertex<K,T> v1 = adjacency.get(key1);
        Vertex<K,T> v2 = adjacency.get(key2);
        v1.addSuccessor(key2);
        v2.addPredecessor(key1);
    }

    /**
     * removes a vertex from the graph.
     * @param key - the key of the vertex to be removed.
     */
    public void removeVertex(K key) {
        Vertex<K,T> v = adjacency.get(key);
        for(K k : v.getSuccessors()){
            adjacency.get(k).removePredecessor(key);
        }
        for(K k : v.getPredecessors()){
            adjacency.get(k).removeSuccessor(key);
        }
        adjacency.remove(key);
    }

    /**
     * @return - all the data of vertices in the graph that have in degree of 0.
     */
    public LinkedList<T> getSources() {
        LinkedList<T> sources = new LinkedList<>();
        for (K key : getSourcesKeys()){
            sources.add(adjacency.get(key).getData());
        }
        return sources;
    }

    /**
     * @return - all the keys of vertices in the graph that have in degree of 0.
     */
    private LinkedList<K> getSourcesKeys(){
        LinkedList<K> sources = new LinkedList<>();
        for (Map.Entry<K, Vertex<K,T>> v : adjacency.entrySet()) {
            if(v.getValue().din() <= 0){
                sources.add(v.getKey());
            }
        }
        return sources;
    }

    /**
     * @return - all the data of vertices in the graph that have out degree of 0.
     */
    public LinkedList<T> getTargets() {
        LinkedList<T> targets = new LinkedList<>();
        for (K key : getTargetsKeys()){
            targets.add(adjacency.get(key).getData());
        }
        return targets;
    }

    /**
     * @return - all the keys of vertices in the graph that have out degree of 0.
     */
    private LinkedList<K> getTargetsKeys(){
        LinkedList<K> targets = new LinkedList<>();
        for (Map.Entry<K, Vertex<K,T>> v : adjacency.entrySet()) {
            if(v.getValue().dout() <= 0){
                targets.add(v.getKey());
            }
        }
        return targets;
    }


    /**
     * removes edge from the graph.
     * @param key1 - the source of the edge.
     * @param key2 - the target of the edge.
     */
    public void removeEdge(K key1, K key2) {
        Vertex v1 = adjacency.get(key1);
        Vertex v2 = adjacency.get(key2);
        v1.removeSuccessor(key2);
        v2.removePredecessor(key1);
    }

    /**
     * @return - topological sort apon the graph.
     */
    public LinkedList<T> toposort(){
        Graph temp = new Graph(this);
        LinkedList<T> sort = new LinkedList<>();
        LinkedList<K> sources;
        do{
            sources = temp.getSourcesKeys();
            for(K source : sources){
                sort.addLast(adjacency.get(source).getData());
                temp.removeVertex(source);
            }
        }while (!sources.isEmpty());
        if(sources.isEmpty() && temp.size() > 0){
            return new LinkedList<T>();
        }
        return sort;
    }

    public LinkedList<T> toposort(Consumer<T> visitor){
        Graph temp = new Graph(this);
        LinkedList<T> sort = new LinkedList<>();
        LinkedList<K> sources;
        do{
            sources = temp.getSourcesKeys();
            for(K source : sources){
                sort.addLast(adjacency.get(source).getData());
                temp.removeVertex(source);
            }
        }while (!sources.isEmpty());
        List<T> unsorted = temp.getVertices();
        sort.forEach(visitor);
        unsorted.forEach(visitor);
        return sort;
    }

    /**
     * @return - the amount of vertices in the graph.
     */
    public int size(){
        return adjacency.size();
    }

    /**
     * @param key1 - key of source vertex.
     * @param key2 - key of target vertex.
     * @return - true iff there is an edge from key1 to key2.
     */
    public boolean doesEdgeExists(K key1, K key2){
        Vertex<K,T> v = adjacency.get(key1);
        if (v == null){
            return false;
        }
        return v.connectedTo(key2);
    }

    /**
     * @param key1 - key of source vertex.
     * @param key2 - key of target vertex.
     * @return - true iff there is a path from key1 to key2.
     */
    public boolean isReachableAux(K key1, K key2, LinkedList<K> visited){
        if(key1.equals(key2)){
            return true;
        }
        visited.add(key1);
        for(K key : adjacency.get(key1).getSuccessors()){
            if(!visited.contains(key) && isReachable(key, key2)){
                return true;
            }
        }
        return false;
    }
    public boolean isReachable(K key1, K key2){
        return isReachableAux(key1, key2, new LinkedList<>());
    }

    public LinkedList<T> getVertices(){
        LinkedList<T> vertices = new LinkedList<>();
        for(Map.Entry<K, Vertex<K,T>> entry : adjacency.entrySet()){
            vertices.add(entry.getValue().getData());
        }
        return vertices;
    }
}

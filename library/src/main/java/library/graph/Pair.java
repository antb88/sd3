package library.graph;

public class Pair<K,V>{
		K left;
		V right;
		public Pair(K left,V right){
			this.left = left;
			this.right = right;
		}
		
		public V getRight(){
			return right;
		}
		
		public K getLeft(){
			return left;
		}
		
		@Override
		public int hashCode(){
			return 1;
		}
		@Override
		public boolean equals(Object obj) {
		    if (this == obj)
		        return true;
		    if (obj == null)
		        return false;
		    if (!(obj instanceof Pair))
		        return false;
		    Pair<?, ?> other = (Pair) obj;
		    return this.right.equals(other.right) && this.left.equals(other.left) ;
		} 
	}
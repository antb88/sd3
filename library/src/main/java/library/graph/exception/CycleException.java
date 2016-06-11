package library.graph.exception;

public class CycleException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CycleException(String message) {
		super(message);
	}
}

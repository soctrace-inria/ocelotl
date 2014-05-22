package fr.inria.soctrace.tools.ocelotl.core.exceptions;

public class OcelotlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String INVALIDQUERY = "Invalid query : modify your settings";
	public static final String NOEVENTS="No events found";
	
	public OcelotlException() {
		super();
	}

	public OcelotlException(String message, Throwable cause) {
		super(message, cause);
	}

	public OcelotlException(String message) {
		super(message);
	}

	public OcelotlException(Throwable cause) {
		super(cause);
	}

}

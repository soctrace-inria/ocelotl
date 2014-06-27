package fr.inria.soctrace.tools.ocelotl.core.exceptions;

public class OcelotlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String INVALIDQUERY = "Error: Invalid query";
	public static final String NOEPS = "Error: No event producers selected";
	public static final String NOETS = "Error: No event types selected";
	public static final String NOEVENTS = "Error: No events found";
	public static final String JNI =  "Error: problem with native library";
	public static final String NOTRACE = "Error: No trace selected";
	public static final String NOMICROSCOPICDESCRIPTION = "Error: No microscopic description selected";
	public static final String NOVISUALIZATION = "Error: No visualitazion selected";
	public static final String INVALIDTIMERANGE = "Error: The final timestamp is smaller than the starting timestamp";

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

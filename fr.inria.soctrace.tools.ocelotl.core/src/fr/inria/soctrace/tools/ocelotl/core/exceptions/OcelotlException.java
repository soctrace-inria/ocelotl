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
	public static final String INVALIDTIMERANGE = "Error: The starting timestamp is greater than the ending timestamp";
	public static final String INVALID_START_TIMESTAMP = "Error: The provided starting timestamp is smaller than the trace starting timestamp";
	public static final String INVALID_END_TIMESTAMP = "Error: The provided ending timestamp is greater than the trace ending timestamp";
	public static final String NOTIMESTAMP = "Error: At least one of the starting or ending timestamps is missing.";

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

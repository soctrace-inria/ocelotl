package fr.inria.soctrace.tools.ocelotl.core.exceptions;

public class OcelotlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String INVALID_QUERY = "Error: Invalid query";
	public static final String NO_EVENT_PRODUCER = "Error: No event producers selected";
	public static final String NO_EVENT_TYPE = "Error: No event types selected";
	public static final String NO_EVENTS = "Error: No events found";
	public static final String JNI =  "Error: problem with native library";
	public static final String NO_TRACE = "Error: No trace selected";
	public static final String NO_TYPE = "Error: No type selected";
	public static final String NO_MICROSCOPIC_DESCRIPTION = "Error: No microscopic description selected";
	public static final String NO_VISUALIZATION = "Error: No visualitazion selected";
	public static final String INVALID_TIMERANGE = "Error: The starting timestamp is greater than the ending timestamp";
	public static final String INVALID_START_TIMESTAMP = "Error: The provided starting timestamp is smaller than the trace starting timestamp";
	public static final String INVALID_END_TIMESTAMP = "Error: The provided ending timestamp is greater than the trace ending timestamp";
	public static final String NO_TIMESTAMP = "Error: At least one of the starting or ending timestamps is missing.";
	public static final String INCOMPLETE_HIERARCHY = "Error: The event producer hierarchy is incomplete.";
	public static final String INVALID_CACHEFILE = "Error: The selected file is not a valid cache file.";
	public static final String INVALID_CACHED_TRACE = "Error: The corresponding trace was not found in the database.";
	public static final String INVALID_MICRO_DESCRIPTION = "Error: The corresponding microscopic description was not found.";
	public static final String INVALID_CACHED_OPERATOR = "Error: The corresponding aggregation operator was not found.";
	public static final String INVALID_MAX_CACHE_SIZE = "Error: The given cache size value is not correct.";
	public static final String USER_CANCEL_INTERRUPTION = "User interrupted the current operation.";
	
	
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

package fr.inria.soctrace.tools.ocelotl.microdesc.config;

public class DefaultSettingsConstant {

	/**
	 * Number of threads for computation
	 */
	public static final int threadNumber = 8;
	
	/**
	 * Number of events loaded by thread
	 */
	public static final int EVENTS_PER_THREAD = 10000;
	public static final int MIN_EVENTS_PER_THREAD = 100;
	public static final int MAX_EVENTS_PER_THREAD = 10000000;
	
	public static final int MIN_NUMBER_OF_THREAD = 1;
	public static final int MAX_NUMBER_OF_THREAD = 1000000;
	
	public static final int MIN_EVENT_PRODUCERS_PER_QUERY = 0;
	public static final int MAX_EVENT_PRODUCERS_PER_QUERY = 1000000;
}

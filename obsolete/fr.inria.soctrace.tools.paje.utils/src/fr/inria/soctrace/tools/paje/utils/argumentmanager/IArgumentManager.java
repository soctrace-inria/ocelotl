package fr.inria.soctrace.tools.paje.utils.argumentmanager;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

public interface IArgumentManager {
	public void debugArgs();

	public void parseArgs() throws SoCTraceException;

	public void printArgs();

	public void processArgs() throws SoCTraceException;
}

package fr.inria.soctrace.tools.ocelotl.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.utils.DeltaManager;

public class DeltaManagerOcelotl extends DeltaManager {

	private static final Logger logger = LoggerFactory.getLogger(DeltaManagerOcelotl.class);
	
	public void end(String s) {
		end();
		logger.info(getMessage(s));
	}

	private String getMessage(String s) {
		return "[" + s + "] Delta: " + ( getDelta() ) + " ms";
	}
	
}

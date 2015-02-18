/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 ******************************************************************************/
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

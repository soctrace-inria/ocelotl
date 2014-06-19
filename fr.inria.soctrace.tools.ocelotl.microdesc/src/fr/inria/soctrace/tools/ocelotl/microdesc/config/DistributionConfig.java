/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in that enables to visualize a trace 
 * overview by using aggregation techniques
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.microdesc.config;

import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.ui.loaders.ConfDataLoader;

public class DistributionConfig implements ITraceTypeConfig {

	private List<EventType> types = new LinkedList<EventType>();
	private int eventsPerThread = 10000;
	private int threadNumber = 8;

	public DistributionConfig() {
		super();
	}

	@Override
	public List<EventType> getTypes() {
		return types;
	}

	public void setTypes(final List<EventType> types) {
		this.types = types;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public int getEventsPerThread() {
		return eventsPerThread;
	}

	public void setEventsPerThread(int eventsPerThread) {
		this.eventsPerThread = eventsPerThread;
	}

	@Override
	/**
	 * Initialize the configuration with default settings
	 * 
	 * @param anObject
	 * 		Must be of type ConfDataLoader. Typed as Object to avoid circular dependency
	 */
	public void init(Object anObject) {
		if (!anObject.getClass().getSimpleName().equals("ConfDataLoader")) {
			System.err
					.println("Wrong class given in argument: expected an object of type ConfDataLoader");
			return;
		}

		ConfDataLoader aConfDataLoader = (ConfDataLoader) anObject;

		if (getTypes().isEmpty())
			setTypes(aConfDataLoader.getTypes());
		
		setThreadNumber(DefaultSettingsConstant.threadNumber);
		setEventsPerThread(DefaultSettingsConstant.eventsPerThread);
	}

}

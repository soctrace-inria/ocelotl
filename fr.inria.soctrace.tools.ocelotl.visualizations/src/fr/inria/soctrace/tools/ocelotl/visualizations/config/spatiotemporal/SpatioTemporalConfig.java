/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.visualizations.config.spatiotemporal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.config.IVisuConfig;
import fr.inria.soctrace.tools.ocelotl.visualizations.temporal.proportion.views.EventColorManager;

public class SpatioTemporalConfig implements IVisuConfig {

	protected List<EventType> types = new LinkedList<EventType>();
	protected List<EventType> undisplayedTypes = new LinkedList<EventType>();
	protected List<EventProducer> producers = new LinkedList<EventProducer>();
	protected EventColorManager colors;

	public SpatioTemporalConfig() {
		super();
	}
	
	public List<EventType> getTypes() {
		return types;
	}
	
	public List<String> getDisplayedTypeNames() {
		List<String> l = new ArrayList<String>();
		for (EventType et: types){
			if(!undisplayedTypes.contains(et))
				l.add(et.getName());
		}
		return l;
	}

	public void setTypes(final List<EventType> types) {
		this.types = types;
	}
	
	public List<EventType> getUndisplayedTypes() {
		return undisplayedTypes;
	}

	public void setUndisplayedTypes(List<EventType> undisplayedTypes) {
		this.undisplayedTypes = undisplayedTypes;
	}

	public EventColorManager getColors() {
		return colors;
	}
	
	public List<EventProducer> getProducers() {
		return producers;
	}

	public void setProducers(List<EventProducer> producers) {
		this.producers = producers;
	}
	
	public List<String> getProducerNames() {
		List<String> l = new ArrayList<String>();
		for (EventProducer ep: producers){
			l.add(ep.getName());
		}
		return l;
	}

	public void initColors() {
		this.colors = new EventColorManager();
	}
	
	/**
	 * Check that the types contain in the config are not filtered out and if so
	 * remove them
	 * 
	 * @param notFilteredTypes
	 *            the event types that are not filtered
	 */
	public void checkForFilteredType(List<EventType> notFilteredTypes) {
		ArrayList<EventType> typesToRemove = new ArrayList<EventType>();

		for (EventType anET : types) {
			if (!notFilteredTypes.contains(anET))
				typesToRemove.add(anET);
		}
		types.removeAll(typesToRemove);
	}
}

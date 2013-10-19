/* ===========================================================
 * Filter Tool module
 * ===========================================================
 *
 * (C) Copyright 2013 Damien Dosimont. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 */

package fr.inria.soctrace.tools.filters.timefilter;

import fr.inria.soctrace.lib.model.Trace;
import java.util.List;
import fr.inria.soctrace.lib.model.EventType;

public class TimeFilterParameters {

	private String			label;
	private boolean			include;
	private boolean			event;
	private TimeRegion		timeRegion;
	private Trace			trace		= null;
	private List<EventType>	eventTypes	= null;
	private List<String>	values		= null;

	public TimeFilterParameters() {
		super();
	}

	public List<EventType> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<EventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isInclude() {
		return include;
	}

	public void setInclude(boolean include) {
		this.include = include;
	}

	public TimeRegion getTimeRegion() {
		return timeRegion;
	}

	public void setTimeRegion(TimeRegion timeRegion) {
		this.timeRegion = timeRegion;
	}

	public boolean isEvent() {
		return event;
	}

	public void setEvent(boolean event) {
		this.event = event;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

}
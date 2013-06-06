/* ===========================================================
 * Paje Trace Manager module
 * =====================================================================
 * 
 * This module is a FrameSoC plug in which enables to import, export and
 * process Pajé trace files
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

package fr.inria.soctrace.tools.paje.tracemanager.common.model;

import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.paje.tracemanager.common.constants.PajeConstants;

/**
 *         This class inherit from EventType. Add Paje Id management feature
 *         (since two Paje Event Types could have the same name although they
 *         are not different) This feature provides methods to create Event
 *         Param Types in passing Paje Id to make parsing easier
 */
public class PajeEventType extends EventType {

	private String	pajeName;
	private String	pajeId;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            : database id field
	 */
	public PajeEventType(int id) {
		super(id);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            : database id field
	 * @param name
	 *            : name of EventType in Paje trace
	 * @param pajeId
	 *            : number associate to EventType in Paje trace
	 */
	public PajeEventType(int id, String name, String pajeId) {
		super(id);
		pajeName = name;
		this.pajeId = pajeId;
		this.setName();
	}

	/**
	* Create a EventParamType
	* 
	* @param id
	* @param name
	* @param type
	*/
	public void addEventParamType(int id, String name, String type) {
		EventParamType currentEventParamType = new EventParamType(id);
		currentEventParamType.setName(name);
		currentEventParamType.setType(type);
		currentEventParamType.setEventType(this);
	}

	/**
	 * @return the pajeId
	 */
	public String getPajeId() {
		return pajeId;
	}

	/**
	 * @return the pajeName
	 */
	public String getPajeName() {
		return pajeName;
	}

	/**
	 * Merge pajeId and pajeName into EventType name field (without argument)
	 */
	public void setName() {
		setName(pajeId + PajeConstants.PajeIdNameSeparator + pajeName);
	}

	/**
	 * Merge pajeId and pajeName into EventType name field
	 * 
	 * @param pajeId
	 * @param pajeName
	 */
	public void setName(String pajeId, String pajeName) {
		setName(pajeId + PajeConstants.PajeIdNameSeparator + pajeName);
	}

	/**
	 * @param pajeId
	 *            the pajeId to set
	 */
	public void setPajeId(String pajeId) {
		this.pajeId = pajeId;
	}

	/**
	 * @param pajeName
	 *            the pajeName to set
	 */
	public void setPajeName(String pajeName) {
		this.pajeName = pajeName;
	}

}

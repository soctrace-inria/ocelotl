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

package fr.inria.soctrace.tools.paje.tracemanager.common.constants;

public abstract class PajeConstants {

	public final static String	ImportToolName								= "PajeTraceImporter";

	public final static String	ExportToolName								= "PajeTraceCleaner";

	public final static String	PajeFormatName								= "Paje 1.2.5";

	public final static String	PajeExportTraceFile							= "trace.paje";

	public final static String	PajeIdNameSeparator							= "_";

	public final static String	PajeFormatComment							= "#";

	public final static String	PajeFormatStartEventDef						= "%EventDef";

	public final static String	PajeFormatEndEventDef						= "%EndEventDef";

	public final static String	PajeFormatParamEventDef						= "%";

	public final static String	PajeTimestampType							= "date";

	public final static String	PajeEventDefParamContainerName				= "Container";

	public final static String	PajeCreateContainerEventDefName				= "PajeCreateContainer";

	public final static String	PajeCreateContainerEventDefParamTypeName	= "Type";

	public final static String	PajeCreateContainerEventDefParamNameName	= "Name";

	public final static String	PajeCreateContainerEventDefParamAliasName	= "Alias";

	public final static long	NoTimestamp									= -1L;

	public final static int		TimestampShifter							= 9;

}

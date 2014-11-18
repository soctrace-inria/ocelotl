package fr.inria.soctrace.tools.ocelotl.core.statistics;

import java.util.List;

import fr.inria.soctrace.framesoc.ui.model.ITableRow;

public interface IStatisticsProvider {
	
	/**
	 * Compute the statistics data
	 */
	public void computeData();

	/*
	 * Provide a list of data to put in the table
	 */
	public List<ITableRow> getTableData();
	
	/**
	 * Update the color of the event types
	 */
	public void updateColor();
}

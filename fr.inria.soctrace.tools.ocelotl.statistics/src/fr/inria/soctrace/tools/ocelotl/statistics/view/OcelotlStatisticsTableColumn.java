package fr.inria.soctrace.tools.ocelotl.statistics.view;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;

public enum OcelotlStatisticsTableColumn implements ITableColumn {

	NAME("Name", 250), PERCENTAGE("Percentage", 120), OCCURRENCES("Value", 120);

	private String name;
	private int width;

	private OcelotlStatisticsTableColumn(String name, int width) {
		this.name = name;
		this.width = width;
	}

	@Override
	public String getHeader() {
		return name;
	}

	@Override
	public int getWidth() {
		return width;
	}
}

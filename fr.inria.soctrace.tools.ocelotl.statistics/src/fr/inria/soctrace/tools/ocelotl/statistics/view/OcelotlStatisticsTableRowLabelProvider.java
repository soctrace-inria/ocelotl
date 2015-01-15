package fr.inria.soctrace.tools.ocelotl.statistics.view;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;

public class OcelotlStatisticsTableRowLabelProvider extends
		TableRowLabelProvider {

	protected String toolTip;

	public OcelotlStatisticsTableRowLabelProvider(ITableColumn col) {
		super(col);
		toolTip = "";
	}

	/**
	 * Provide the displayed tooltip
	 */
	@Override
	public String getToolTipText(Object element) {
		return toolTip;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}
}

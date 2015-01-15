package fr.inria.soctrace.tools.ocelotl.statistics.view;

import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.providers.SquareIconLabelProvider;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.SummaryStat.SummaryStatModel;

public class OcelotlStatisticsTableRowLabelImageProvider extends SquareIconLabelProvider  {

	/**
	 * Managed column
	 */
	protected ITableColumn col;
	
	/**
	 * Constructor
	 * @param col ITableColumn the provider is related to.
	 */
	public OcelotlStatisticsTableRowLabelImageProvider(ITableColumn col) {
		super();
		this.col = col;
	}
	
	@Override
	public String getText(Object element) {
		String text = "";
		try {
			text = ((ITableRow) element).get(col);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return text;
	}

	@Override
	public Color getColor(Object element) {
		SummaryStatModel row = (SummaryStatModel)element;
		return row.getColor();
	}
}

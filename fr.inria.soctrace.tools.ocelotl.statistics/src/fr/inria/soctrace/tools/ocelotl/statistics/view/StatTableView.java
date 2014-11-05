package fr.inria.soctrace.tools.ocelotl.statistics.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.StatisticsProvider;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.TemporalSummaryStat;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.statview.StatView;

public class StatTableView extends StatView {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory
			.getLogger(StatTableView.class);

	/**
	 * The table viewer
	 */
	private TableViewer tableViewer;

	/**
	 * Provide the stat data
	 */
	private StatisticsProvider statProvider;

	/**
	 * Constructor
	 */
	public StatTableView(OcelotlView theView) {
		super(theView);
	}

	@Override
	public void createDiagram() {
		statProvider = new TemporalSummaryStat(ocelotlView);
		createPartControl(ocelotlView.getStatComposite());
		statProvider.computeData();
		updateTableData();
	}

	@Override
	public void deleteDiagram() {
		if(tableViewer != null)
			tableViewer.setInput(null);
	}

	
	public void updateTableData() {
		tableViewer.setInput(statProvider);
	}

	/**
	 * Images
	 */
	private Map<String, Image> images = new HashMap<String, Image>();

	public void createPartControl(Composite parent) {
		Composite compositeTable = parent;

		// Table
		tableViewer = new TableViewer(compositeTable, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		tableViewer.setContentProvider(new StatContentProvider());
		createColumns();
		Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setBounds(0, 0, 1000, 550);

		// Create a new loader map
		/*
		 * currentDescriptor.map = new PieChartLoaderMap();
		 * 
		 * TimeInterval loadInterval = new TimeInterval(
		 * currentDescriptor.interval.startTimestamp = ocelotlView
		 * .getParams().getTimeRegion().getTimeStampStart(),
		 * currentDescriptor.interval.endTimestamp = ocelotlView
		 * .getParams().getTimeRegion().getTimeStampEnd());
		 */

		// Create loader and drawer threads
		/*
		 * LoaderThread loaderThread = new LoaderThread(loadInterval); DrawerJob
		 * drawerJob = new DrawerJob("Pie Chart Drawer Job", loaderThread);
		 * loaderThread.start(); drawerJob.schedule();
		 */
	}

	private void createColumns() {
		for (final OcelotlStatisticsTableColumn col : OcelotlStatisticsTableColumn.values()) {
			TableViewerColumn elemsViewerCol = new TableViewerColumn(
					tableViewer, SWT.NONE);

			if (col.equals(OcelotlStatisticsTableColumn.NAME)) {
				// add a filter for this column
				//nameFilter = new StatisticsTableRowFilter(col);
				//tableViewer.addFilter(nameFilter);

				// the label provider puts also the image
				elemsViewerCol
						.setLabelProvider(new OcelotlStatisticsTableRowLabelProvider(
								col, images));
			} else
				elemsViewerCol.setLabelProvider(new TableRowLabelProvider(col));

			final TableColumn elemsTableCol = elemsViewerCol.getColumn();
			elemsTableCol.setWidth(col.getWidth());
			elemsTableCol.setText(col.getHeader());
			elemsTableCol.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableViewer.getTable().setSortColumn(elemsTableCol);
					tableViewer.refresh();
				}
			});
		}
	}

	public StatisticsProvider getStatProvider() {
		return statProvider;
	}

	public void setStatProvider(StatisticsProvider statProvider) {
		this.statProvider = statProvider;
	}

}

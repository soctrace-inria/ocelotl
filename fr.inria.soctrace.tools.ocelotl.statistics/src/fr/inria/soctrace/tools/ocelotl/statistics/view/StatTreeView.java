package fr.inria.soctrace.tools.ocelotl.statistics.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.piechart.loaders.EventProducerPieChartLoader;
import fr.inria.soctrace.framesoc.ui.piechart.model.IPieChartLoader;
import fr.inria.soctrace.framesoc.ui.piechart.model.PieChartLoaderMap;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableColumn;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableFolderRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRowFilter;
import fr.inria.soctrace.framesoc.ui.piechart.providers.StatisticsTableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.statview.StatView;

public class StatTreeView extends StatView {

	/**
	 * Build update timeout
	 */
	private static final long BUILD_UPDATE_TIMEOUT = 300;

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory
			.getLogger(StatTreeView.class);

	/**
	 * Filter for table
	 */
	private StatisticsTableRowFilter nameFilter;

	/**
	 * The table viewer
	 */
	private TreeViewer tableTreeViewer;

	/**
	 * Descriptor related to the current active loader.
	 */
	private LoaderDescriptor currentDescriptor;

	/**
	 * Current shown trace
	 */
	protected Trace currentShownTrace = null;

	/**
	 * Constructor
	 */
	public StatTreeView(OcelotlView theView) {
		super(theView);
		showTrace();
	}

	@Override
	public void createDiagram() {
		createPartControl(ocelotlView.getStatComposite());
	}

	/**
	 * Images
	 */
	private Map<String, Image> images = new HashMap<String, Image>();

	public void createPartControl(Composite parent) {
		Composite compositeTable = parent;

		// Table
		tableTreeViewer = new TreeViewer(compositeTable, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER
				| SWT.VIRTUAL);
		tableTreeViewer.setContentProvider(new TreeContentProvider());
		Tree table = tableTreeViewer.getTree();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setBounds(0, 0, 1000, 550);
		createColumns();

		// Create a new loader map
		currentDescriptor.map = new PieChartLoaderMap();

		TimeInterval loadInterval = new TimeInterval(
				currentDescriptor.interval.startTimestamp = ocelotlView
						.getParams().getTimeRegion().getTimeStampStart(),
				currentDescriptor.interval.endTimestamp = ocelotlView
						.getParams().getTimeRegion().getTimeStampEnd());

		// Create loader and drawer threads
		LoaderThread loaderThread = new LoaderThread(loadInterval);
		DrawerJob drawerJob = new DrawerJob("Pie Chart Drawer Job",
				loaderThread);
		loaderThread.start();
		drawerJob.schedule();
	}

	private void createColumns() {
		for (final StatisticsTableColumn col : StatisticsTableColumn.values()) {
			TreeViewerColumn elemsViewerCol = new TreeViewerColumn(
					tableTreeViewer, SWT.NONE);

			if (col.equals(StatisticsTableColumn.NAME)) {
				// add a filter for this column
				nameFilter = new StatisticsTableRowFilter(col);
				tableTreeViewer.addFilter(nameFilter);

				// the label provider puts also the image
				elemsViewerCol
						.setLabelProvider(new StatisticsTableRowLabelProvider(
								col, images));
			} else
				elemsViewerCol.setLabelProvider(new TableRowLabelProvider(col));

			final TreeColumn elemsTableCol = elemsViewerCol.getColumn();
			elemsTableCol.setWidth(col.getWidth());
			elemsTableCol.setText(col.getHeader());
			elemsTableCol.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					tableTreeViewer.getTree().setSortColumn(elemsTableCol);
					tableTreeViewer.refresh();
				}
			});
		}
	}

	/**
	 * Loader thread.
	 */
	private class LoaderThread extends Thread {

		private final TimeInterval loadInterval;
		private final IProgressMonitor monitor;

		public LoaderThread(TimeInterval loadInterval) {
			this.loadInterval = loadInterval;
			this.monitor = new NullProgressMonitor();
		}

		@Override
		public void run() {
			currentDescriptor.loader.load(currentShownTrace, loadInterval,
					currentDescriptor.map, monitor);
		}

		public void cancel() {
			monitor.setCanceled(true);
		}
	}

	/**
	 * Drawer job.
	 */
	private class DrawerJob extends Job {

		private final LoaderThread loaderThread;

		public DrawerJob(String name, LoaderThread loaderThread) {
			super(name);
			this.loaderThread = loaderThread;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			DeltaManager dm = new DeltaManager();
			dm.start();
			try {
				PieChartLoaderMap map = currentDescriptor.map;
				boolean done = false;
				boolean refreshed = false;
				while (!done) {
					done = map.waitUntilDone(BUILD_UPDATE_TIMEOUT);
					if (!map.isDirty()) {
						continue;
					}
					if (monitor.isCanceled()) {
						loaderThread.cancel();
						logger.debug("Drawer thread cancelled");
						return Status.CANCEL_STATUS;
					}
					refresh();
					refreshed = true;
				}
				if (!refreshed) {
					// refresh at least once when there is no data.
					refresh();
				}
				return Status.OK_STATUS;
			} finally {
				logger.debug(dm.endMessage("finished drawing"));
			}
		}

	}

	/**
	 * Refresh the UI using the current trace and the current descriptor.
	 * 
	 * @param dataReady
	 */
	private void refresh() {

		// compute graphical elements
		PieChartLoaderMap map = currentDescriptor.map;
		final Map<String, Double> values = map
				.getSnapshot(currentDescriptor.interval);
		final IPieChartLoader loader = currentDescriptor.loader;
		final StatisticsTableFolderRow root = loader.getTableDataset(values);

		// update the new UI
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				if (currentDescriptor.dataReady() && values.isEmpty()) {
					// store the loaded interval in case of no data
					currentDescriptor.interval.startTimestamp = ocelotlView
							.getParams().getTimeRegion().getTimeStampStart();
					currentDescriptor.interval.endTimestamp = ocelotlView
							.getParams().getTimeRegion().getTimeStampEnd();
				}

				// update other elements
				if (!root.hasChildren()) {
					tableTreeViewer.setInput(null);
				} else {
					tableTreeViewer.setInput(root);
				}
				tableTreeViewer.expandAll();
			}
		});
	}

	public void showTrace() {
		currentShownTrace = ocelotlView.getConfDataLoader().getCurrentTrace();
		currentDescriptor = new LoaderDescriptor();
	}

	/**
	 * Data Loader
	 */
	private class LoaderDescriptor {
		public IPieChartLoader loader;
		public PieChartLoaderMap map;
		public TimeInterval interval;

		public LoaderDescriptor() {
			this.loader = new EventProducerPieChartLoader();
			this.interval = new TimeInterval(0, 0);
			this.map = new PieChartLoaderMap();
		}

		public boolean dataReady() {
			return (map != null && map.isComplete());
		}

		@Override
		public String toString() {
			return "LoaderDescriptor [loader=" + loader + ", dataset=" + map
					+ ", interval=" + interval + "]";
		}
	}

}

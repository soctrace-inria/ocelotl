package fr.inria.soctrace.tools.ocelotl.statistics.view;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.model.TableRow;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.StatisticsProvider;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.TemporalSummaryStat;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.statview.StatView;

public class StatTableView extends StatView implements IFramesocBusListener {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory
			.getLogger(StatTableView.class);

	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList topics = null;

	/**
	 * The table viewer
	 */
	private TableViewer tableViewer;

	/**
	 * Provide the stat data
	 */
	private StatisticsProvider statProvider;

	/**
	 * Filter text for table
	 */
	private Text textFilter;

	/**
	 * Filter for table
	 */
	private OcelotlStatisticsTableRowFilter nameFilter;

	/**
	 * Column comparator
	 */
	private OcelotlStatisticsColumnComparator comparator;

	private Composite compositeTable;

	/**
	 * Images
	 */
	private Map<String, Image> images = new HashMap<String, Image>();
	
	/**
	 * Constructor
	 */
	public StatTableView(OcelotlView theView) {
		super(theView);

		// Register update to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
	}
	
	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		if (statProvider != null && tableViewer != null) {
			disposeImages();
			statProvider.updateColor();
			updateTableData();
			tableViewer.refresh();
		}
	}

	@Override
	public void createDiagram() {
		dispose();
		statProvider = new TemporalSummaryStat(ocelotlView);
		createPartControl(ocelotlView.getStatComposite());
		updateData();
	}
	
	private void disposeImages() {
		Iterator<Image> it = images.values().iterator();
		while (it.hasNext()) {
			it.next().dispose();
		}
		images.clear();
	}

	@Override
	public void deleteDiagram() {
		if (tableViewer != null)
			tableViewer.setInput(null);
	}

	public void updateTableData() {
		if (tableViewer != null)
			tableViewer.setInput(statProvider);
	}
	
	@Override
	public void updateData() {
		statProvider.computeData();
		updateTableData();
		tableViewer.refresh();
	}

	@Override
	public void resizeDiagram() {
		tableViewer.refresh();
		compositeTable.redraw();
		compositeTable.update();
	}

	public void createPartControl(Composite parent) {
		compositeTable = parent;
		GridLayout gl_compositeTable = new GridLayout(1, false);
		 
		compositeTable.setLayout(gl_compositeTable);

		// Filter
		/*textFilter = new Text(compositeTable, SWT.BORDER);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		textFilter.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (nameFilter == null || tableViewer == null)
						return;
					nameFilter.setSearchText(textFilter.getText());
					tableViewer.refresh();
				}
			}
		});*/
		
		// Table
		tableViewer = new TableViewer(compositeTable, SWT.FILL | SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		tableViewer.setContentProvider(new StatContentProvider());
		comparator = new OcelotlStatisticsColumnComparator();
		tableViewer.setComparator(comparator);
		createColumns();
		Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		table.setBounds(0, 0, 1000, 550);
		tableViewer.getTable().setSortColumn(tableViewer.getTable().getColumn(1));
		tableViewer.getTable().setSortDirection(SWT.UP);
	}

	private void createColumns() {
		for (final OcelotlStatisticsTableColumn col : OcelotlStatisticsTableColumn
				.values()) {
			TableViewerColumn elemsViewerCol = new TableViewerColumn(
					tableViewer, SWT.NONE);

			if (col.equals(OcelotlStatisticsTableColumn.NAME)) {
				// add a filter for this column
				//nameFilter = new OcelotlStatisticsTableRowFilter(col);
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
					comparator.setColumn(col);
					tableViewer.getTable().setSortDirection(
							comparator.getDirection());
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

	public void dispose() {
		for (Control c : ocelotlView.getStatComposite().getChildren())
			c.dispose();
	}

	public class OcelotlStatisticsColumnComparator extends ViewerComparator {
		private OcelotlStatisticsTableColumn col = OcelotlStatisticsTableColumn.PERCENTAGE;
		private int direction = SWT.DOWN;

		public int getDirection() {
			return direction;
		}

		public void setColumn(OcelotlStatisticsTableColumn col) {
			if (this.col.equals(col)) {
				// Same column as last sort: toggle the direction
				direction = (direction == SWT.UP) ? SWT.DOWN : SWT.UP;
			} else {
				// New column: do an ascending sort
				this.col = col;
				direction = SWT.UP;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			TableRow r1 = (TableRow) e1;
			TableRow r2 = (TableRow) e2;

			int rc = 0;
			try {
				if (this.col.equals(OcelotlStatisticsTableColumn.OCCURRENCES)) {
					// number comparison
					// If using a decimal separator, then parse with the local
					// separator (dot or comma)
					NumberFormat format = NumberFormat.getInstance();
					Double v1 = format.parse(r1.get(this.col)).doubleValue();
					Double v2 = format.parse(r2.get(this.col)).doubleValue();
					rc = v1.compareTo(v2);
				} else if (this.col
						.equals(OcelotlStatisticsTableColumn.PERCENTAGE)) {
					// percentage comparison 'xx.xx %'
					NumberFormat format = NumberFormat.getInstance();
					Double v1 = format.parse(r1.get(this.col).split(" ")[0])
							.doubleValue();
					Double v2 = format.parse(r2.get(this.col).split(" ")[0])
							.doubleValue();
					rc = v1.compareTo(v2);
				} else {
					// string comparison
					String v1 = r1.get(this.col);
					String v2 = r2.get(this.col);
					rc = v1.compareTo(v2);
				}
			} catch (SoCTraceException e) {
				e.printStackTrace();
				rc = 0;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == SWT.DOWN) {
				rc = -rc;
			}
			return rc;
		}
	}

}

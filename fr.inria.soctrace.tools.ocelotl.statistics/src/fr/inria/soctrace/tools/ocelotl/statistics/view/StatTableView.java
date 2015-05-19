/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Youenn Corre <youenn.corret@inria.fr>
 ******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.statistics.view;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.model.TableRow;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.statistics.IStatisticsProvider;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.StatisticsProvider;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.SummaryStat.SummaryStatModel;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.statview.StatView;

public class StatTableView extends StatView {

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
	 * Column comparator
	 */
	private OcelotlStatisticsColumnComparator comparator;
	private HashMap<Integer, OcelotlStatisticsTableColumn> columnIndex = new HashMap<Integer, OcelotlStatisticsTableColumn>();

	private Composite compositeTable;

	/**
	 * Constructor
	 */
	public StatTableView(OcelotlView theView) {
		super(theView);
		dispose();
		
		// Register update to synchronize traces
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
			
		createPartControl(ocelotlView.getStatComposite());
	}
	
	public class StatTableViewThread extends Thread {
		  public StatTableViewThread(){
		    super();
		  }
		  public void run(){
		            Display.getDefault().asyncExec(new Runnable() {
		               public void run() {
		     			  if (statProvider.getMicroMode() != null) {
		  					statProvider.computeData();
		  					updateTableData();
		  					// Needed for correct redraw of the table
		  					compositeTable.layout();
		  				}
		               }
		            });
		         

		  }       
		}
	
	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		// If color has changed
		if ((topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED))) {
			if (statProvider != null && statProvider.getTableData() != null && tableViewer != null) {
				// Update colors and the table
				statProvider.updateColor();
				updateTableData();
			}
		}
	}
	
	@Override
	public String getStatDataToCSV() {
		StringBuffer output = new StringBuffer();
		for (ITableRow aRow : statProvider.getStatData()) {
			SummaryStatModel aStatRow = (SummaryStatModel) aRow;
			for (ITableColumn aColumn : aStatRow.getFields().keySet()) {
				output.append(aStatRow.getFields().get(aColumn)
						+ OcelotlConstants.CSVDelimiter);
			}
			// Remove the last csv delimiter
			output.deleteCharAt(output
					.lastIndexOf(OcelotlConstants.CSVDelimiter));
			output.append("\n");
		}

		return output.toString();
	}

	@Override
	public void createDiagram() {
		statProvider.setMicroMode(ocelotlView.getCore().getMicroModel());
		updateData();
	}
	
	@Override
	public void deleteDiagram() {
		if (tableViewer != null) 
			tableViewer.setInput(null);
	}

	/**
	 * If data has changed
	 */
	public void updateTableData() {
		if (tableViewer != null && !tableViewer.getTable().isDisposed()) {
			tableViewer.setInput(statProvider);
			tableViewer.refresh();
		}
	}
	
	@Override
	public void updateData() {
		StatTableViewThread thread= new StatTableViewThread();
		thread.start();
	}//TODO make a thread 

	@Override
	public void resizeDiagram() {
		compositeTable.redraw();
		compositeTable.update();
		compositeTable.layout();
	}

	/**
	 * Create the widgets for the statistics operators using table
	 * 
	 * @param parent
	 *            Composite parent widget
	 */
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
		ColumnViewerToolTipSupport.enableFor(tableViewer);

		Table table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableViewer.getTable().addListener(SWT.Resize, new ResizeListener());
		// needed to set a correct table width
		compositeTable.layout();
		
		createColumns();
		comparator = new OcelotlStatisticsColumnComparator();
		tableViewer.setComparator(comparator);
		
		// Default sorting of the table
		tableViewer.getTable().setSortColumn(tableViewer.getTable().getColumn(ocelotlView.getOcelotlParameters().getSortTableSettings().getColumnNumber()));
		tableViewer.getTable().setSortDirection(ocelotlView.getOcelotlParameters().getSortTableSettings().getDirection());
	}

	private void createColumns() {
		int cpt = 0;
		columnIndex = new HashMap<Integer, OcelotlStatisticsTableColumn>();

		// For each column
		for (final OcelotlStatisticsTableColumn col : OcelotlStatisticsTableColumn
				.values()) {
			TableViewerColumn elemsViewerCol = new TableViewerColumn(
					tableViewer, SWT.NONE);
			
			// Text alignment in the column
			int alignment;

			// If it is the column name
			if (col.equals(OcelotlStatisticsTableColumn.NAME)) {
				// add a filter for this column
				// nameFilter = new OcelotlStatisticsTableRowFilter(col);
				// tableViewer.addFilter(nameFilter);

				// the label provider also puts the image
				elemsViewerCol
						.setLabelProvider(new OcelotlStatisticsTableRowLabelImageProvider(
								col));
				alignment = SWT.LEFT;
			} else {
				OcelotlStatisticsTableRowLabelProvider labelProvider = new OcelotlStatisticsTableRowLabelProvider(
						col);
				elemsViewerCol.setLabelProvider(labelProvider);
				alignment = SWT.RIGHT;
			}

			final TableColumn elemsTableCol = elemsViewerCol.getColumn();

			elemsTableCol
					.setWidth(Math.max((int) (tableViewer.getTable().getClientArea().width * ocelotlView
							.getOcelotlParameters().getSortTableSettings()
							.getColumnWidthWeight()[cpt]), (int) (tableViewer.getTable().getClientArea().width * 0.05)));

			elemsTableCol.setText(col.getHeader());
			elemsTableCol.setAlignment(alignment);
			// Set sorting comparator when clicking on a column header
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
			
			elemsTableCol.addControlListener(new ControlListener() {
				
				@Override
				public void controlResized(ControlEvent e) {
					// Modified the weight of each column according to their new
					// width
					double areaWidth = tableViewer.getTable().getClientArea().width;
					// Avoid complication due to small number
					if (areaWidth > 4.0) {
						// Set a minimal and maximal value to avoid column
						// disappearing
						for (int anIndex : columnIndex.keySet())
							if (columnIndex.get(anIndex).getHeader()
									.equals(col.getHeader())) {
								ocelotlView.getOcelotlParameters()
										.getSortTableSettings()
										.getColumnWidthWeight()[anIndex] = Math
										.min(Math
												.max(0.05,
														(double) elemsTableCol
																.getWidth()
																/ areaWidth),
												0.95);
							}
					}
				}
				
				@Override
				public void controlMoved(ControlEvent e) {
				}
			});
			columnIndex.put(cpt, col);
			cpt++;
		}
	}

	public StatisticsProvider getStatProvider() {
		return statProvider;
	}

	public void setStatProvider(IStatisticsProvider statProvider) {
		this.statProvider = (StatisticsProvider) statProvider;
	}

	/**
	 * Delete the old widgets
	 */
	public void dispose() {
		if (topics != null)
			topics.unregisterAll();

		for (Control c : ocelotlView.getStatComposite().getChildren())
			c.dispose();
	}
	
	/**
	 * Resize the columns dynamically
	 */
	private class ResizeListener implements Listener {

		@Override
		public void handleEvent(Event event) {
			Table table = (Table) event.widget;
			int columnCount = table.getColumnCount();
			if (columnCount == 0)
				return;
			int totalAreaWidth = table.getClientArea().width;
			TableColumn[] columns = table.getColumns();
			for (int i = 0; i < columns.length; i++) {
				columns[i].setWidth((int) (totalAreaWidth * ocelotlView
						.getOcelotlParameters().getSortTableSettings()
						.getColumnWidthWeight()[i]));
			}
		}
	}

	/**
	 * Class used to sort the column (copied and adapted from
	 * StatisticsColumnComparator in Framesoc)
	 */
	public class OcelotlStatisticsColumnComparator extends ViewerComparator {
		private OcelotlStatisticsTableColumn col = columnIndex.get(ocelotlView
				.getOcelotlParameters().getSortTableSettings()
				.getColumnNumber());
		private int direction = ocelotlView.getOcelotlParameters()
				.getSortTableSettings().getDirection();

		public int getDirection() {
			return direction;
		}

		public void setColumn(OcelotlStatisticsTableColumn aCol) {
			if (this.col.equals(aCol)) {
				// Same column as last sort: toggle the direction
				direction = (direction == SWT.UP) ? SWT.DOWN : SWT.UP;
			} else {
				// New column: do an ascending sort
				this.col = aCol;
				direction = SWT.UP;
			}

			// Get the column number and save it into the index
			TableColumn[] columns = tableViewer.getTable().getColumns();
			for (int i = 0; i < columns.length; i++)
				if (columns[i].getText().equals(aCol.getHeader()))
					ocelotlView.getOcelotlParameters().getSortTableSettings()
							.setColumnNumber(i);

			// Get the sorting direction
			ocelotlView.getOcelotlParameters().getSortTableSettings()
					.setDirection(direction);
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			TableRow r1 = (TableRow) e1;
			TableRow r2 = (TableRow) e2;

			int rc = 0;
			try {
				if (this.col.equals(OcelotlStatisticsTableColumn.VALUE)) {
					// Number comparison
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

	@Override
	public void removeDiagram() {
		dispose();
		dispose=true;	
	}
	
	public boolean isDisposed(){
		return dispose;
	}

}

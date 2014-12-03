package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.model.TableRow;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.MicroscopicDescription;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.statistics.view.OcelotlStatisticsTableColumn;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class SummaryStat extends StatisticsProvider {

	Microscopic3DDescription microModel;
	HashMap<String, Double> data;
	HashMap<String, Double> proportions;
	List<ITableRow> statData;

	public SummaryStat(OcelotlView aView) {
		super(aView);
	}
	
	@Override
	public void computeData() {
		int i;
		data = new HashMap<String, Double>();
		double total = 0.0;

		setupTimeRegion();

		// Get the corresponding time slices
		int startingSlice = (int) microModel.getTimeSliceManager()
				.getTimeSlice(timeRegion.getTimeStampStart() + 2);
		int endingSlice = (int) microModel.getTimeSliceManager().getTimeSlice(
				timeRegion.getTimeStampEnd());
		
		// Get data from the microscopic model
		for (i = startingSlice; i <= endingSlice; i++) {
			for (EventProducer ep : microModel.getMatrix().get(i).keySet()) {
				// Get only the spatially selected elements
				if (!isInSpatialSelection(ep)) 
					continue;
				
				for (String et : microModel.getMatrix().get(i).get(ep).keySet()) {
					// If first time we meet the event type
					if (!data.containsKey(et)) {
						// Initialize to 0
						data.put(et, 0.0);
					}
					data.put(et, microModel.getMatrix().get(i).get(ep).get(et)
							+ data.get(et));
				}
			}
		}

		// Compute the total number of value to compute percentage
		for (double aValue : data.values()) {
			total = total + aValue;
		}

		statData = new ArrayList<ITableRow>();

		// Create the data objects for the table
		for (String ep : data.keySet()) {
			double proportion = 0.0;
			// If there was no value in the selected zone, let proportion at 0
			if (total != 0)
				proportion = (data.get(ep) / total) * 100;
			
			statData.add(new SummaryStatModel(ep, data.get(ep),
					proportion, FramesocColorManager
							.getInstance().getEventTypeColor(ep).getSwtColor()));
		}
	}

	/**
	 * Get the currently selected time region from the ocelotl view
	 */
	protected void setupTimeRegion() {
		TimeRegion currentTimeregion = microModel.getTimeSliceManager()
				.getTimeRegion();

		// If we perform a reset on the timestamp, make sure we don't take
		// values higher than the current region
		Long startingDate = Math.max(currentTimeregion.getTimeStampStart(),
				Long.valueOf(ocelotlview.getTextTimestampStart().getText()));
		Long endingDate = Math.min(currentTimeregion.getTimeStampEnd(),
				Long.valueOf(ocelotlview.getTextTimestampEnd().getText()));

		timeRegion = new TimeRegion(startingDate, endingDate);
	}

	protected boolean isInSpatialSelection(EventProducer ep) {
		if (ocelotlview.getOcelotlParameters().isSpatialSelection()
				&& (!ocelotlview.getOcelotlParameters()
						.getSpatiallySelectedProducers().contains(ep)))
			return false;

		return true;
	}

	@Override
	public List<ITableRow> getTableData() {
		return statData;
	}
	
	public void setMicroMode(MicroscopicDescription aMicroModel) {
		microModel = (Microscopic3DDescription) aMicroModel;
	}

	@Override
	public void updateColor() {
		for (ITableRow aRow : statData) {
			SummaryStatModel aStat = (SummaryStatModel) aRow;
			try {
				aStat.setColor(FramesocColorManager
						.getInstance()
						.getEventTypeColor(
								aStat.get(OcelotlStatisticsTableColumn.NAME))
						.getSwtColor());
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Class used to format the data for the table:
	 * it has four fields
	 * - the name
	 * - the number of occurrences
	 * - the proportion
	 * - the color of the event in framesoc
	 *
	 */
	public class SummaryStatModel extends TableRow {
		/**
		 * Color for the name cell image
		 */
		protected Color color;

		public void setColor(Color color) {
			this.color = color;
		}

		public SummaryStatModel(String aName, double aValue,
				double aProportion, Color aColor) {
			this.fields.put(OcelotlStatisticsTableColumn.NAME, aName);

			NumberFormat occurenceFormatter = new DecimalFormat("#0.00E00");
			this.fields.put(OcelotlStatisticsTableColumn.OCCURRENCES,
					occurenceFormatter.format(aValue));

			NumberFormat percentFormatter = new DecimalFormat("#0.000");
			this.fields.put(OcelotlStatisticsTableColumn.PERCENTAGE,
					percentFormatter.format(aProportion) + " %");
			this.color = aColor;
		}

		/**
		 * @return the color
		 */
		public Color getColor() {
			return color;
		}
	}

}

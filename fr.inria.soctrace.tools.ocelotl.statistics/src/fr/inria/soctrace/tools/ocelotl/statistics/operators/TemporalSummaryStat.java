package fr.inria.soctrace.tools.ocelotl.statistics.operators;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.TableRow;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.core.microdesc.Microscopic3DDescription;
import fr.inria.soctrace.tools.ocelotl.core.timeregion.TimeRegion;
import fr.inria.soctrace.tools.ocelotl.statistics.view.OcelotlStatisticsTableColumn;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class TemporalSummaryStat extends StatisticsProvider {

	Microscopic3DDescription microModel;
	HashMap<String, Double> data;
	HashMap<String, Double> proportions;
	List<SummaryStatModel> statData;

	public TemporalSummaryStat(OcelotlView aView) {
		super(aView);
		microModel = (Microscopic3DDescription) ocelotlview.getOcelotlCore()
				.getMicroModel();
	}

	@Override
	public void computeData() {
		int i;
		data = new HashMap<String, Double>();
		double total = 0.0;

		setupTimeRegion();

		int startingSlice = (int) microModel.getTimeSliceManager()
				.getTimeSlice(timeRegion.getTimeStampStart());
		int endingSlice = (int) microModel.getTimeSliceManager().getTimeSlice(
				timeRegion.getTimeStampEnd());

		for (i = startingSlice; i <= endingSlice; i++) {
			for (EventProducer ep : microModel.getMatrix().get(i).keySet()) {
				for (String et : microModel.getMatrix().get(i).get(ep).keySet()) {
					if (!data.containsKey(et)) {
						data.put(et, 0.0);
					}
					microModel.getMatrix().get(i).get(ep).get(et);
					data.put(et, microModel.getMatrix().get(i).get(ep).get(et)
							+ data.get(et));
				}
			}
		}

		for (double aValue : data.values()) {
			total = total + aValue;
		}

		statData = new ArrayList<SummaryStatModel>();

		for (String ep : data.keySet()) {
			statData.add(new SummaryStatModel(ep, data.get(ep), (data.get(ep)
					/ total) * 100.0, FramesocColorManager.getInstance()
					.getEventTypeColor(ep).getSwtColor()));
		}
	}

	private void setupTimeRegion() {
		Long startingDate = Long.valueOf(ocelotlview.getTextTimestampStart()
				.getText());
		Long endingDate = Long.valueOf(ocelotlview.getTextTimestampEnd()
				.getText());

		timeRegion = new TimeRegion(startingDate, endingDate);
	}

	@Override
	public List<SummaryStatModel> getTableData() {
		return statData;
	}

	@Override
	public void updateColor() {
		for (SummaryStatModel aStat : statData) {
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
			NumberFormat formatter = new DecimalFormat("#0.000");
			this.fields.put(OcelotlStatisticsTableColumn.OCCURRENCES,
					String.valueOf(aValue));

			this.fields.put(OcelotlStatisticsTableColumn.PERCENTAGE,
					formatter.format(aProportion) + " %");
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

package fr.inria.soctrace.tools.ocelotl.statistics.view;

import java.util.Map;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.tools.ocelotl.statistics.operators.TemporalSummaryStat.SummaryStatModel;

public class OcelotlStatisticsTableRowLabelProvider extends OwnerDrawLabelProvider  {

	/**
	 * Managed column
	 */
	protected ITableColumn col;
	
	/**
	 * References to the images (cache).
	 * The ownership is to the statistics view.
	 */
	protected Map<String, Image> images;
	

	protected void measure(Event event, Object element) {
		// nothing to do
	}
	
	/**
	 * Constructor
	 * @param col ITableColumn the provider is related to.
	 * @param images 
	 */
	public OcelotlStatisticsTableRowLabelProvider(ITableColumn col,
			Map<String, Image> images) {
		this.col = col;
		this.images = images;
	}

	@Override
	protected void paint(Event event, Object element) {
		
		String text = "";
		try {
			text = ((ITableRow) element).get(col);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		
		Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
		Image img = null;
		if (images.containsKey(text)) {
			img = images.get(text);
		} else {
			img = new Image(event.display, bounds.height/2, bounds.height/2);
			GC gc = new GC(img);
			SummaryStatModel row = (SummaryStatModel)element;
			Color swtColor = row.getColor();
			/* Problem:
			 * - when I change the color associated to a type in the color manager,
			 *   the color manager disposes the old color associated to that type
			 * - the color, however, was cached in the statistic table row object
			 * - such row was not completed here, due to the exception, thus it was 
			 *   probably requested twice
			 */
			if (!swtColor.isDisposed()) 
				gc.setBackground(swtColor);
		    gc.fillRectangle(0, 0, bounds.height/2, bounds.height/2);
		    gc.dispose();			
			images.put(text, img);			
		}
		
	    // center image and text on y
		bounds.height = bounds.height / 2 - img.getBounds().height / 2;
		int imgy = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;
		int texty = bounds.y + 3;
		event.gc.drawText(text, bounds.x + img.getBounds().width + 10, texty, true);
		event.gc.drawImage(img, 5 + bounds.x, imgy);
				
	}
}

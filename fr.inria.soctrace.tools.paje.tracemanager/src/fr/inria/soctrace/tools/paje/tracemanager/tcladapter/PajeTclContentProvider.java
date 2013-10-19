
package fr.inria.soctrace.tools.paje.tracemanager.tcladapter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;

import com.st.tchartslite.viewer.ITChartsContentProvider;

import fr.inria.soctrace.framesoc.ui.tcl.ITChartsInput;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsRow;
import fr.inria.soctrace.framesoc.ui.tcl.ITChartsEvent;

/**
 * Paje content provider for Tcl Gantt
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeTclContentProvider implements ITChartsContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ITChartsInput) {
			Object[] items = ((ITChartsInput) inputElement).getTChartsRows().toArray();
			return items;
		} else
			return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITChartsRow) {
			Object[] items = ((ITChartsRow) parentElement).getChildrenRows().toArray();
			return items;
		} else
			return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ITChartsRow)
			return ((ITChartsRow) element).getParent();
		else
			return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITChartsRow)
			return !((ITChartsRow) element).getChildrenRows().isEmpty();
		else
			return false;
	}

	@Override
	public void dispose() {
		// Nothing to do
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getStartTime(Object inputElement) {
		if (inputElement instanceof ITChartsInput)
			return ((ITChartsInput) inputElement).getStartTime();
		else
			return -1;
	}

	@Override
	public long getEndTime(Object inputElement) {
		if (inputElement instanceof ITChartsInput)
			return ((ITChartsInput) inputElement).getEndTime();
		else
			return -1;
	}

	@Override
	public List<Object> getEvents(Object element) {
		if (element instanceof ITChartsRow) {
			List<Object> events = new ArrayList<Object>();
			List<ITChartsEvent> l = ((ITChartsRow) element).getEvents();
			if (l != null)
				events.addAll(l);
			else
				events = null;

			return events;
		} else
			return null;
	}

	@Override
	public long getEventStartTime(Object event) {
		if (event instanceof ITChartsEvent)
			return ((ITChartsEvent) event).getStartTime();
		else
			return -1;
	}

	@Override
	public long getEventEndTime(Object event) {
		if (event instanceof ITChartsEvent)
			return ((ITChartsEvent) event).getEndTime();
		else
			return -1;
	}

	@Override
	public List<Object> getOutlinkedEvents(Object event) {
		if (event instanceof ITChartsEvent) {
			List<Object> outlinkedEvents = new ArrayList<Object>();
			List<ITChartsEvent> l = ((ITChartsEvent) event).getOutlinkedEvents();
			if (l != null)
				outlinkedEvents.addAll(l);
			else
				outlinkedEvents = null;

			return outlinkedEvents;
		} else
			return null;
	}

}

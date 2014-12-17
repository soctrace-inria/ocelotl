package fr.inria.soctrace.tools.ocelotl.statistics.view;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fr.inria.soctrace.tools.ocelotl.statistics.operators.StatisticsProvider;

/**
 * Generic table content provider.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class StatContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		StatisticsProvider statProvider = (StatisticsProvider) inputElement;

		return statProvider.getTableData().toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

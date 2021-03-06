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
package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlParameters;

public class ActionHistory {

	private OcelotlView						ocelotlView;
	private LinkedList<OcelotlParameters>	parameterHistory;
	private Integer							currentHistoryIndex;

	public ActionHistory(OcelotlView aView) {
		ocelotlView = aView;
		parameterHistory = new LinkedList<OcelotlParameters>();
		currentHistoryIndex = 0;
	}

	public Integer getCurrentHistoryIndex() {
		return currentHistoryIndex;
	}

	public void setCurrentHistoryIndex(Integer currentHistoryIndex) {
		this.currentHistoryIndex = currentHistoryIndex;

		// Update the history buttons in the view
		ocelotlView.getPrevZoom().setEnabled(currentHistoryIndex > 0);
		ocelotlView.getNextZoom().setEnabled(parameterHistory.size() > 0 && currentHistoryIndex < parameterHistory.size() - 1);
	}

	/**
	 * Save the current history, if necessary
	 */
	public void saveHistory() {
		// If first run
		if (parameterHistory.size() == 0) {
			// Add the current parameter
			parameterHistory.add(new OcelotlParameters(ocelotlView.getOcelotlParameters()));
			return;
		}

		// If there were changes
		if (checkForChanges()) {
			// Are we at the end of the history
			if (parameterHistory.size() - 1 != currentHistoryIndex) {
				// If not, we remove everything after this point
				deleteOldHistory();
			}

			// Add the current parameters to history
			parameterHistory.add(new OcelotlParameters(ocelotlView.getOcelotlParameters()));
			setCurrentHistoryIndex(currentHistoryIndex + 1);
		}
	}

	/**
	 * Check for changes from previous registered parameters For now we only
	 * deal with zoom (temporal and spatial)
	 * 
	 * @return true if there was a change, false otherwise
	 */
	public boolean checkForChanges() {

		// If timestamps have changed
		if (!ocelotlView.getTimeRegion().compareTimeRegion(parameterHistory.get(currentHistoryIndex).getTimeRegion()))
			return true;

		// If event producers selection have changed
		if (ocelotlView.getOcelotlParameters().getSpatiallySelectedProducers().size() != parameterHistory.get(currentHistoryIndex).getSpatiallySelectedProducers().size())
			return true;
		
		// Special case for false leaves
		if (ocelotlView.getOcelotlParameters().getSelectedEventProducerNodes().size() != parameterHistory.get(currentHistoryIndex).getSelectedEventProducerNodes().size())
			return true;

		for (EventProducer anEP : ocelotlView.getOcelotlParameters().getSpatiallySelectedProducers())
			if (!parameterHistory.get(currentHistoryIndex).getSpatiallySelectedProducers().contains(anEP))
				return true;

		return false;
	}

	/**
	 * Clean the history
	 */
	public void reset() {
		parameterHistory = new LinkedList<OcelotlParameters>();
		setCurrentHistoryIndex(0);
	}

	/**
	 * Delete all saved history after the current index
	 */
	private void deleteOldHistory() {
		while (parameterHistory.size() - 1 > currentHistoryIndex) {
			parameterHistory.removeLast();
		}
	}

	/**
	 * Restore the previous history parameters
	 */
	public void restorePrevHistory() {
		setCurrentHistoryIndex(currentHistoryIndex - 1);
		restoreHistory();
	}

	/**
	 * Restore the next history parameters
	 */
	public void restoreNextHistory() {
		setCurrentHistoryIndex(currentHistoryIndex + 1);
		restoreHistory();
	}

	/**
	 * Restore the history
	 */
	public void restoreHistory() {
		// Update parameters
		ocelotlView.setTimeRegion(parameterHistory.get(currentHistoryIndex).getTimeRegion());
		ocelotlView.getOcelotlParameters().setSpatialSelection(parameterHistory.get(currentHistoryIndex).isSpatialSelection());
		ocelotlView.getOcelotlParameters().setSpatiallySelectedProducers(parameterHistory.get(currentHistoryIndex).getSpatiallySelectedProducers());
		ocelotlView.getOcelotlParameters().setSelectedEventProducerNodes(parameterHistory.get(currentHistoryIndex).getSelectedEventProducerNodes());
		
		// Trigger a redraw
		ocelotlView.getBtnRun().notifyListeners(SWT.Selection, new Event());
	}
}

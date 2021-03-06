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

import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Filter for a ITableRow object field. The column corresponding to the field is
 * passed to the constructor.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class OcelotlStatisticsTableRowFilter extends ViewerFilter {

	private ITableColumn col;
	private String searchString;

	public OcelotlStatisticsTableRowFilter(ITableColumn col) {
		this.col = col;
	}

	public void setSearchText(String s) {
		this.searchString = ".*" + s + ".*"; // use this one if you want
												// substrings by default
		// this.searchString = s; // use this one if you want pure expressions
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		ITableRow row = (ITableRow) element;
		try {
			if (row.get(col).matches(searchString)) {
				return true;
			}
		} catch (PatternSyntaxException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Wrong search string",
					"The expression used as search string is not valid: "
							+ searchString);
			searchString = "";
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return false;
	}
}
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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fr.inria.soctrace.tools.ocelotl.statistics.operators.StatisticsProvider;

/**
 * Generic table content provider.
 */
public class StatContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		StatisticsProvider statProvider = (StatisticsProvider) inputElement;
		Object[] result = new Object[1];
		try{
		result = statProvider.getTableData().toArray();
		}
		catch(NullPointerException e){
		}
		return result;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

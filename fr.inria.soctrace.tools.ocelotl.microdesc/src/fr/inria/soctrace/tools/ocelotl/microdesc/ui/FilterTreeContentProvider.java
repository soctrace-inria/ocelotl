/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk - Initial API and implementation
 *******************************************************************************/
package fr.inria.soctrace.tools.ocelotl.microdesc.ui;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fr.inria.soctrace.tools.ocelotl.core.model.SimpleEventProducerHierarchy.SimpleEventProducerNode;

/**
 * Content Provider for the event producer tree
 * 
 * @version 1.0
 * @author Yuriy Vashchuk
 */
public class FilterTreeContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof SimpleEventProducerNode) {
			return getChildren(inputElement);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		ArrayList<SimpleEventProducerNode> result = new ArrayList<SimpleEventProducerNode>();

		if (hasChildren(parentElement)) {
			result.addAll(((SimpleEventProducerNode) parentElement)
					.getChildrenNodes());
		}
		return result.toArray();
	}

	@Override
	public Object getParent(Object element) {
		return ((SimpleEventProducerNode) element).getParentNode();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((SimpleEventProducerNode) element).getChildrenNodes().size() != 0;
	}

}

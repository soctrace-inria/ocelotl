/* =====================================================================
 * Ocelotl Visualization Tool
 * =====================================================================
 * 
 * Ocelotl is a FrameSoC plug in which enables to visualize a trace 
 * overview by using a time aggregation technique
 *
 * (C) Copyright 2013 INRIA
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Damien Dosimont <damien.dosimont@imag.fr>
 *     Generoso Pagano <generoso.pagano@inria.fr>
 */

package fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.ui;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.tools.ocelotl.core.config.ITraceTypeConfig;
import fr.inria.soctrace.tools.ocelotl.timeaggregop.generic.config.StateDistributionConfig;
import fr.inria.soctrace.tools.ocelotl.ui.com.eclipse.wb.swt.SWTResourceManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.ISettingApplicationWindow;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;


public abstract class StateDistributionBaseView extends ApplicationWindow implements ISettingApplicationWindow {

	private class EventTypeLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventType) element).getName();
		}
	}


	private class RemoveSelectionAdapter extends SelectionAdapter {

		private final ListViewer	viewer;

		public RemoveSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			final Object obj = selection.getFirstElement();
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.remove(obj);
			viewer.refresh(false);
		}
	}

	private class TypesSelectionAdapter extends SelectionAdapter {

		// all - input
		java.util.List<Object> diff(final java.util.List<EventType> all, final java.util.List<EventType> input) {
			final java.util.List<Object> tmp = new LinkedList<>();
			for (final Object oba : all)
				tmp.add(oba);
			tmp.removeAll(input);
			return tmp;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (ocelotlView.getConfDataLoader().getCurrentTrace() == null)
				return;
			final ListSelectionDialog dialog = new ListSelectionDialog(getShell(), diff(ocelotlView.getConfDataLoader().getTypes(), config.getTypes()), new ArrayContentProvider(), new EventTypeLabelProvider(), "Select Event Types");
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				config.getTypes().add((EventType) o);
			listViewerEventTypes.setInput(config.getTypes());
		}
	}

	protected OcelotlView	ocelotlView;

	protected ListViewer	listViewerIdleStates;

	protected ListViewer	listViewerEventTypes;

	protected StateDistributionConfig	config;

	public StateDistributionBaseView(final Shell shell) {
		super(shell);
		ocelotlView = null;
		config = null;
	}

	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Pajé Aggregation Operator Settings");
	}

	@Override
	public Control createContents(final Composite parent) {
		// parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final SashForm sashFormGlobal = new SashForm(parent, SWT.VERTICAL);
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		final Group groupEventTypes = new Group(sashFormGlobal, SWT.NONE);
		groupEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		groupEventTypes.setText("Set GenericState Event Types");
		final GridLayout gl_groupEventTypes = new GridLayout(2, false);
		gl_groupEventTypes.horizontalSpacing = 0;
		groupEventTypes.setLayout(gl_groupEventTypes);

		listViewerEventTypes = new ListViewer(groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventTypes.setContentProvider(new ArrayContentProvider());
		listViewerEventTypes.setLabelProvider(new EventTypeLabelProvider());
		listViewerEventTypes.setComparator(new ViewerComparator());
		final List listEventTypes = listViewerEventTypes.getList();
		listEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		listEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final ScrolledComposite scrCompositeEventTypeButtons = new ScrolledComposite(groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventTypeButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		scrCompositeEventTypeButtons.setExpandHorizontal(true);
		scrCompositeEventTypeButtons.setExpandVertical(true);

		final Composite compositeEventTypeButtons = new Composite(scrCompositeEventTypeButtons, SWT.NONE);
		compositeEventTypeButtons.setLayout(new GridLayout(1, false));

		final Button btnAddEventTypes = new Button(compositeEventTypeButtons, SWT.NONE);
		btnAddEventTypes.setText("Add");
		btnAddEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnAddEventTypes.setImage(null);
		btnAddEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnAddEventTypes.addSelectionListener(new TypesSelectionAdapter());

		final Button btnRemoveEventTypes = new Button(compositeEventTypeButtons, SWT.NONE);
		btnRemoveEventTypes.setText("Remove");
		btnRemoveEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		btnRemoveEventTypes.setImage(null);
		btnRemoveEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scrCompositeEventTypeButtons.setContent(compositeEventTypeButtons);
		scrCompositeEventTypeButtons.setMinSize(compositeEventTypeButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveEventTypes.addSelectionListener(new RemoveSelectionAdapter(listViewerEventTypes));

		final Composite OK = new Composite(sashFormGlobal, SWT.NONE);
		OK.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		OK.setLayout(new FormLayout());

		final Button buttonOK = new Button(OK, SWT.NONE);
		final FormData fd_buttonOK = new FormData();
		fd_buttonOK.bottom = new FormAttachment(100, -10);
		fd_buttonOK.right = new FormAttachment(100, -10);
		buttonOK.setLayoutData(fd_buttonOK);
		buttonOK.setText("OK");
		buttonOK.setFont(SWTResourceManager.getFont("Cantarell", 8, SWT.NORMAL));
		buttonOK.setImage(null);
		buttonOK.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				close();
			}
		});
		setParameters();
		return sashFormGlobal;

	}
	
	abstract public void setParameters();

	@Override
	public void init(final OcelotlView ocelotlView, final ITraceTypeConfig config) {
		this.ocelotlView = ocelotlView;
		this.config = (StateDistributionConfig) config;
	}

}

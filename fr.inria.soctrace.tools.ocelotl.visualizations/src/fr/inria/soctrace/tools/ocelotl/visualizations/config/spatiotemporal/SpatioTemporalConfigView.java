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
package fr.inria.soctrace.tools.ocelotl.visualizations.config.spatiotemporal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
import fr.inria.soctrace.tools.ocelotl.core.config.IVisuConfig;
import fr.inria.soctrace.tools.ocelotl.ui.views.IVisualizationWindow;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class SpatioTemporalConfigView extends Dialog implements IVisualizationWindow {

	// Model entity combo
	private Combo comboModelEntity;

	// Filter text
	private Text textFilter;

	// The viewer
	private TableViewer tableViewer;

	// Entity managed
	private ModelEntity entity;

	// Edit button
	private Button btnEdit;

	// Reset button
	protected Button btnReset;
	private TabFolder tabFolder;

	// Color images
	private Map<String, Image> images;
	
	private class Entity  {
		String name;
		ModelEntity entity;
		Entity(String name, ModelEntity entity) {
			this.name = name;
			this.entity = entity;
		}
	}
	
	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Spatiotemporal Visualization Settings");
	}
	
	private Map<Integer, Entity> entities;
	private OcelotlView ocelotlView;
	private SpatioTemporalConfig config;
	private final static String ET_NAME = "Event Types";
	private final static String EP_NAME = "Event Producers";
	protected ListViewer listViewerEventTypes;
			
	/**
	 * Constructor
	 * @param parentShell shell
	 */
	public SpatioTemporalConfigView(Shell parentShell) {
		super(parentShell);
		this.images = new HashMap<String, Image>();
		this.entities = new TreeMap<Integer, Entity>();
		this.entities.put(0, new Entity(ET_NAME, ModelEntity.EVENT_TYPE));
		this.entities.put(1, new Entity(EP_NAME, ModelEntity.EVENT_PRODUCER));
	}

	@Override
    protected Control createDialogArea(Composite parent) {
    	Composite all = (Composite) super.createDialogArea(parent);
        
		final SashForm sashFormGlobal = new SashForm(all, SWT.VERTICAL);
		sashFormGlobal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));
    	
		// Aggregation settings
		final TabItem tbtmColorParameters = new TabItem(tabFolder, 0);
		tbtmColorParameters.setText("Set Colors");
		
		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmColorParameters.setControl(sashFormAdvancedParameters);
		
        comboModelEntity = new Combo(sashFormAdvancedParameters, SWT.READ_ONLY);
        comboModelEntity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        Iterator<Entry<Integer, Entity>> it = entities.entrySet().iterator();
        while (it.hasNext()) {
        	Entry<Integer, Entity> e = it.next();
        	comboModelEntity.add(e.getValue().name, e.getKey());
        	comboModelEntity.select(e.getKey()); // select the last
        	entity = e.getValue().entity;
        }
        comboModelEntity.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		entity = entities.get(comboModelEntity.getSelectionIndex()).entity;
        		textFilter.setText("");
        		tableViewer.setInput(getNames());
        		tableViewer.setSelection(null);
        		tableViewer.refresh(true);
        		textFilter.setText("");
        	}
		});
        
        textFilter = new Text(sashFormAdvancedParameters, SWT.BORDER);
        textFilter.addModifyListener(new ModifyListener() {
        	@Override
			public void modifyText(ModifyEvent e) {
        		tableViewer.refresh();
        	}
        });
        textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    	
        Composite composite = new Composite(sashFormAdvancedParameters, SWT.NONE);
        GridLayout gl_composite = new GridLayout(2, false);
        gl_composite.verticalSpacing = 0;
        gl_composite.marginWidth = 0;
        gl_composite.marginHeight = 0;
        composite.setLayout(gl_composite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        Composite names = new Composite(composite, SWT.NONE);
        names.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        GridLayout gl_names = new GridLayout(1, false);
        gl_names.horizontalSpacing = 0;
        gl_names.marginHeight = 0;
        gl_names.marginWidth = 0;
        gl_names.verticalSpacing = 0;
        names.setLayout(gl_names);
        names.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        // list
        tableViewer = new TableViewer(names, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        Table table = tableViewer.getTable();
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_table.widthHint = 422;
        table.setLayoutData(gd_table);
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        	@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection.size() <= 0) {
					btnEdit.setEnabled(false);				
				}
				if (selection.size() == 1) {
					btnEdit.setEnabled(true);
				} else {
					btnEdit.setEnabled(false);					
				}
        	}
        });
        
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setLabelProvider(new RowLabelProvider());
        tableViewer.setSorter(new ViewerSorter());
        tableViewer.addFilter(new RowFilter());
        tableViewer.setInput(getNames());
        
        // buttons
        Composite compositeButtons = new Composite(composite, SWT.NONE);
        compositeButtons.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
        compositeButtons.setLayout(new GridLayout(1, false));
        compositeButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        
        btnEdit = new Button(compositeButtons, SWT.NONE);
        btnEdit.setEnabled(false);
        btnEdit.setToolTipText("Edit Color");
        btnEdit.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
        btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				Iterator<?> it = selection.iterator();
				while (it.hasNext()) {
					String name = (String) it.next();
					ColorDialog dialog = new ColorDialog(getShell());
					FramesocColor c = getColor(name);
					dialog.setRGB(new RGB(c.red, c.green, c.blue));
					RGB rgb = dialog.open();
					setColor(name, new FramesocColor(rgb.red, rgb.green, rgb.blue));
					disposeImages();
					btnReset.setEnabled(true);
					tableViewer.refresh(true);					
				}
			}
		});
        
        btnReset = new Button(compositeButtons, SWT.NONE);
        btnReset.setEnabled(false);
        btnReset.setToolTipText("Reload from Configuration File");
        btnReset.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/load.png"));
        btnReset.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		loadColors();
				disposeImages();
				btnReset.setEnabled(false);
        		tableViewer.refresh(true);
        	}
        });

		sashFormAdvancedParameters.setWeights(new int[] { 1, 1, 10 });
        
		// Event Type selection
		final TabItem tbtmEventSelection = new TabItem(tabFolder, SWT.NONE);
		tbtmEventSelection.setText("Event Type Selection");
		
		final Group groupEventTypes = new Group(tabFolder, SWT.NONE);
		tbtmEventSelection.setControl(groupEventTypes);
		groupEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		groupEventTypes.setText("Set Event Types");
		final GridLayout gl_groupEventTypes = new GridLayout(2, false);
		gl_groupEventTypes.horizontalSpacing = 0;
		groupEventTypes.setLayout(gl_groupEventTypes);

		listViewerEventTypes = new ListViewer(groupEventTypes, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		listViewerEventTypes.setContentProvider(new ArrayContentProvider());
		listViewerEventTypes.setLabelProvider(new EventTypeLabelProvider());
		listViewerEventTypes.setComparator(new ViewerComparator());
		final List listEventTypes = listViewerEventTypes.getList();
		listEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		listEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		final ScrolledComposite scrCompositeEventTypeButtons = new ScrolledComposite(
				groupEventTypes, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrCompositeEventTypeButtons.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, false, false, 1, 1));
		scrCompositeEventTypeButtons.setExpandHorizontal(true);
		scrCompositeEventTypeButtons.setExpandVertical(true);

		final Composite compositeEventTypeButtons = new Composite(
				scrCompositeEventTypeButtons, SWT.NONE);
		compositeEventTypeButtons.setLayout(new GridLayout(1, false));

		final Button btnAddEventTypes = new Button(compositeEventTypeButtons,
				SWT.NONE);
		btnAddEventTypes.setText("Add");
		btnAddEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnAddEventTypes.setImage(null);
		btnAddEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		btnAddEventTypes.addSelectionListener(new TypesSelectionAdapter());

		final Button btnRemoveEventTypes = new Button(
				compositeEventTypeButtons, SWT.NONE);
		btnRemoveEventTypes.setText("Remove");
		btnRemoveEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnRemoveEventTypes.setImage(null);
		btnRemoveEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		scrCompositeEventTypeButtons.setContent(compositeEventTypeButtons);
		scrCompositeEventTypeButtons.setMinSize(compositeEventTypeButtons
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		btnRemoveEventTypes.addSelectionListener(new RemoveSelectionAdapter(
				listViewerEventTypes));
		Button btnResetEventTypes = new Button(compositeEventTypeButtons,
				SWT.NONE);
		btnResetEventTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		btnResetEventTypes.setText("Reset");
		btnResetEventTypes.addSelectionListener(new ResetSelectionAdapter(
				listViewerEventTypes));
		btnResetEventTypes.setFont(SWTResourceManager.getFont("Cantarell", 11,
				SWT.NORMAL));
		btnResetEventTypes.setImage(null);
		
		java.util.List<EventType> displayedEventTypes = new ArrayList<EventType>();
		displayedEventTypes.addAll(config.getTypes());
		displayedEventTypes.removeAll(config.getUndisplayedTypes());
		listViewerEventTypes.setInput(displayedEventTypes);
		
        return composite;
    }	
    
    @Override
    public boolean close() {
    	disposeImages();
    	return super.close();
    }
    
    private void disposeImages() {
       	for (Image img: images.values()) {
    		img.dispose();
    	}
       	images.clear();
    }
    
    @SuppressWarnings("unchecked")
	@Override
    protected void okPressed() {
    	saveColors();
    	ColorsChangeDescriptor des = new ColorsChangeDescriptor();
    	des.setEntity(entity);
    	FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED, des);
    	config.getUndisplayedTypes().clear();
    	config.getUndisplayedTypes().addAll(ocelotlView.getOcelotlParameters().getTraceTypeConfig()
						.getTypes());
    	config.getUndisplayedTypes().removeAll((java.util.List<EventType>) listViewerEventTypes.getInput());
    	super.okPressed();
    }
    
    @Override
    protected void cancelPressed() {
    	loadColors();
    	ColorsChangeDescriptor des = new ColorsChangeDescriptor();
    	des.setEntity(entity);
    	FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED, des);
    	super.cancelPressed();
    }
    
	protected FramesocColor getColor(String name) {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			return FramesocColorManager.getInstance().getEventTypeColor(name);
		else
			return FramesocColorManager.getInstance().getEventProducerColor(name);
	}
		
	private void setColor(String name, FramesocColor color) {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			FramesocColorManager.getInstance().setEventTypeColor(name, color);
		else
			FramesocColorManager.getInstance().setEventProducerColor(name, color);
	}

	private void saveColors() {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			FramesocColorManager.getInstance().saveEventTypeColors();
		else 
			FramesocColorManager.getInstance().saveEventProducerColors();
	}
	
	private void loadColors() {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			FramesocColorManager.getInstance().loadEventTypeColors();
		else 
			FramesocColorManager.getInstance().loadEventProducerColors();
	}

	private Collection<String> getNames() {
		if (entity.equals(ModelEntity.EVENT_TYPE))
			return config.getDisplayedTypeNames();
		else
			return config.getProducerNames();
	}

    @Override
	protected Point getInitialSize() {
		return new Point(504, 464);
	}
    
    public class RowFilter extends ViewerFilter {
    	@Override
    	public boolean select(Viewer viewer, Object parentElement, Object element) {
    		String row = (String) element;
    		if (textFilter.getText().equals(""))
    			return true;
    		try {
    			if (row.matches(".*"+textFilter.getText()+".*")) {
    				return true;
    			}
    		} catch(PatternSyntaxException e) {
    			MessageDialog.openError(Display.getDefault().getActiveShell(), "Wrong search string", 
    					"The expression used as search string is not valid: " + textFilter.getText());
    			textFilter.setText("");
    		} 
    		return false;
    	}
    } 

    public class RowLabelProvider extends OwnerDrawLabelProvider {
    	@Override
    	protected void paint(Event event, Object element) {
    		String name = (String) element;
    		Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
    		Image img = null;
    		if (images.containsKey(name)) {
    			img = images.get(name);
    		} else {
    			img = new Image(event.display, bounds.height/2, bounds.height/2);
    			GC gc = new GC(img);
    			Color border = new Color(event.display, 0, 0 ,0);
    			gc.setBackground(border);
    		    gc.fillRectangle(0, 0, bounds.height/2, bounds.height/2);
    		    gc.setBackground(getColor(name).getSwtColor());
    		    gc.fillRectangle(1, 1, bounds.height/2-2, bounds.height/2-2);
    		    gc.dispose();			
    		    border.dispose();
    			images.put(name, img);			
    		}
    		
    	    // center image and text on y
    		bounds.height = bounds.height / 2 - img.getBounds().height / 2;
    		int imgy = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;
    		int texty = bounds.y + 3;
    		event.gc.drawText(name, bounds.x + img.getBounds().width + 5, texty, true);
    		event.gc.drawImage(img, bounds.x, imgy);
    	}
    	
		@Override
		protected void measure(Event event, Object element) {
			// nothing to do
		}
	}

	private class EventTypeLabelProvider extends LabelProvider {

		@Override
		public String getText(final Object element) {
			return ((EventType) element).getName();
		}
	}

	@Override
	public void init(OcelotlView ocelotlView, IVisuConfig aConfig) {
		this.ocelotlView = ocelotlView;
		this.config = (SpatioTemporalConfig) aConfig;
		config.getTypes().clear();
		config.getTypes().addAll(
				ocelotlView.getOcelotlParameters().getTraceTypeConfig()
						.getTypes());
		config.setProducers(ocelotlView.getOcelotlParameters().getCurrentProducers());
		config.checkForFilteredType(ocelotlView.getOcelotlParameters().getTraceTypeConfig().getTypes());
	}
	
	private class TypesSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (ocelotlView.getConfDataLoader().getCurrentTrace() == null)
				return;
			final ListSelectionDialog dialog = new ListSelectionDialog(
					getShell(), config.getUndisplayedTypes(),
					new ArrayContentProvider(), new EventTypeLabelProvider(),
					"Select Event Types");
			if (dialog.open() == Window.CANCEL)
				return;
			for (final Object o : dialog.getResult())
				config.getUndisplayedTypes().remove((EventType) o);
			
			java.util.List<EventType> displayedEventTypes = new ArrayList<EventType>();
			displayedEventTypes.addAll(config.getTypes());
			displayedEventTypes.removeAll(config.getUndisplayedTypes());
			listViewerEventTypes.setInput(displayedEventTypes);
		}
	}
	
	protected java.util.List<EventType> getEventTypes() {
		java.util.List<EventType> types = new ArrayList<EventType>();
		types.addAll(ocelotlView.getOcelotlParameters().getTraceTypeConfig().getTypes());	
		return types;
	}
	
	private class RemoveSelectionAdapter extends SelectionAdapter {
		private final ListViewer viewer;

		public RemoveSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final IStructuredSelection selection = (IStructuredSelection) viewer
					.getSelection();
			final Object obj = selection.getFirstElement();
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.remove(obj);
			viewer.refresh(false);
		}
	}

	private class ResetSelectionAdapter extends SelectionAdapter {
		private final ListViewer viewer;

		public ResetSelectionAdapter(final ListViewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			final Collection<?> c = (Collection<?>) viewer.getInput();
			c.clear();
			viewer.refresh(false);
		}
	}
	
}

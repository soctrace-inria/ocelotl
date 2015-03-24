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

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import fr.inria.soctrace.tools.ocelotl.core.ParameterStrategy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacachePolicy;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.HasChanged;
import fr.inria.soctrace.tools.ocelotl.core.exceptions.OcelotlException;
import fr.inria.soctrace.tools.ocelotl.core.parameters.OcelotlDefaultParameterConstants;
import fr.inria.soctrace.tools.ocelotl.core.settings.OcelotlSettings;

public class OcelotlSettingsView extends Dialog {

	private OcelotlView							ocelotlView;
	private OcelotlSettings						settings;
	private Combo								parameterPStrategy;
	private Button								btnDeleteDataCache;
	private Text								datacacheDirectory;
	private Button								btnChangeCacheDirectory;
	private Button								btnDataCacheEnabled;
	private Button								btnDichoCacheEnabled;
	private Button								btnRadioButton, btnRadioButton_1, btnRadioButton_2, btnRadioButton_3;
	private HashMap<DatacachePolicy, Button>	cachepolicy	= new HashMap<DatacachePolicy, Button>();
	private Spinner								cacheTimeSliceValue;
	private TabFolder							tabFolder;
	private Button								btnNormalize;
	private Button								btnIncreasingQualities;
	private Button								btnDecreasingQualities;
	private Spinner								spinnerEventSize;
	private Spinner								spinnerDivideDbQuery;
	private Spinner								spinnerThread;
	private Spinner								dataCacheSize;
	private Font								cantarell8;
	private Text								textThreshold;
	private Text								snapshotDirectory;
	private Button								btnChangeSnapshotDirectory;
	private DatacachePolicy						currentSelectedDatacachePolicy;
	private String								currentDatacacheDir;
	private Text								snapshotWidth;
	private Text								snapshotHeight;
	private Spinner								xAxisHeight;
	private Spinner								yAxisWidth;
	private Spinner								qualCurveWidth;
	private Spinner								qualCurveHeight;
	private Button								btnEditBgOverviewDisplay;
	private Button								btnEditFgOverviewDisplay;
	private Button								btnEditBgOverviewSelected;
	private Button								btnEditFgOverviewSelected;
	private HashMap<Button, Color>				btnColorMap;
	private Spinner								textOverviewDisplayAlpha;
	private Spinner								textOverviewSelectionAlpha;
	private Button								btnEnableOverview;
	private Spinner								spinnerMaxAggLeaves;
	private Button								btnEnableLeavesAgg;
	private Spinner								spinnerOverviewMaxAggLeaves;
	private Button								btnOverviewEnableLeavesAgg;
	
	private Button								btnEditBgMainDisplay;
	private Button								btnEditFgMainDisplay;
	private Button								btnEditBgMainSelected;
	private Button								btnEditFgMainSelected;
	private Spinner								textMainDisplayAlpha;
	private Spinner								textMainSelectionAlpha;
	private Button								saveSettingsButton;
	private Button								btnEnableVisualAggregation;

	public OcelotlSettingsView(final OcelotlView ocelotlView) {
		super(ocelotlView.getSite().getShell());
		this.ocelotlView = ocelotlView;
		settings = ocelotlView.getOcelotlParameters().getOcelotlSettings();
		currentSelectedDatacachePolicy = settings.getCachePolicy();
		currentDatacacheDir = "";
		btnColorMap = new HashMap<Button, Color>();
	}

	public void openDialog() {
		this.open();
	}

	private class cachePolicyListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (btnRadioButton.getSelection()) {
				currentSelectedDatacachePolicy = DatacachePolicy.CACHEPOLICY_SLOW;
			}
			if (btnRadioButton_1.getSelection()) {
				currentSelectedDatacachePolicy = DatacachePolicy.CACHEPOLICY_FAST;
			}
			if (btnRadioButton_2.getSelection()) {
				currentSelectedDatacachePolicy = DatacachePolicy.CACHEPOLICY_ASK;
			}
			if (btnRadioButton_3.getSelection()) {
				currentSelectedDatacachePolicy = DatacachePolicy.CACHEPOLICY_AUTO;
			}
		}
	}

	private class DeleteDataCache extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			// Ask user confirmation
			if (MessageDialog.openConfirm(getShell(), "Delete cached data", "This will delete all cached data and it cannot be cancelled. Do you want to continue ?"))
				ocelotlView.getOcelotlParameters().getDichotomyCache().deleteCache();
				ocelotlView.getOcelotlParameters().getDataCache().deleteCache();
		}
	}

	public void modifyDataCacheSize() {
		try {
			if (Integer.valueOf(dataCacheSize.getText()) < 0) {
				ocelotlView.getOcelotlParameters().getDataCache().setCacheMaxSize(-1);
			} else {
				// Set the cache size at the entered value converted from
				// Megabytes to bytes
				ocelotlView.getOcelotlParameters().getDataCache().setCacheMaxSize(Long.valueOf(dataCacheSize.getText()) * 1000000);
			}
		} catch (final NumberFormatException err) {
			dataCacheSize.setSelection((int) ocelotlView.getOcelotlParameters().getDataCache().getCacheMaxSize());
		} catch (OcelotlException e1) {
			MessageDialog.openInformation(getShell(), "Error", e1.getMessage());
		}
	}

	private class ModifySnapshotDirectory extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			String newSnapDir = dialog.open();
			// Did the user cancel?
			if (newSnapDir != null) {
				// Is the directory valid
				if (ocelotlView.getSnapshot().checkSnapDirectoryValidity(newSnapDir)) {

					// Update the displayed path
					snapshotDirectory.setText(newSnapDir);
				} else {
					MessageDialog.openInformation(getShell(), "Error", "Invalid snapshot directory: the specified directory cannot be created or do not have the write acces rights.");
				}
			}
		}
	}

	private class ModifyDatacacheDirectory extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			String newCacheDir = dialog.open();
			// Did the user cancel?
			if (newCacheDir != null) {
				// Is the directory valid
				if (ocelotlView.getOcelotlParameters().getDataCache().checkCacheDirectoryValidity(newCacheDir)) {
					currentDatacacheDir = newCacheDir;

					// Update the displayed path
					datacacheDirectory.setText(newCacheDir);
				} else {
					MessageDialog.openInformation(getShell(), "Error", "Invalid datacache directory: the specified directory cannot be created or do not have the read acces rights.");
				}
			}
		}
	}

	/**
	 * If necessary, update the cache directory
	 */
	private void updateCacheDir() {
		// Was there change in the datacache directory ?
		if (!currentDatacacheDir.isEmpty()) {
			// If so, update the current datacache path
			ocelotlView.getOcelotlParameters().getDataCache().setCacheDirectory(currentDatacacheDir);
			ocelotlView.getOcelotlParameters().getDichotomyCache().setCacheDirectory(currentDatacacheDir);
		}
	}

	private class EnableCacheListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			boolean cacheActivation = btnDataCacheEnabled.getSelection();
			btnRadioButton.setEnabled(cacheActivation);
			btnRadioButton_1.setEnabled(cacheActivation);
			btnRadioButton_2.setEnabled(cacheActivation);
			btnRadioButton_3.setEnabled(cacheActivation);
			cacheTimeSliceValue.setEnabled(cacheActivation);
		}
	}
	
	private class EnableOverviewListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			boolean overviewActivation = btnEnableOverview.getSelection();
			
			btnEditBgOverviewDisplay.setEnabled(overviewActivation);
			btnEditFgOverviewDisplay.setEnabled(overviewActivation);
			btnEditBgOverviewSelected.setEnabled(overviewActivation);
			btnEditFgOverviewSelected.setEnabled(overviewActivation);
			textOverviewDisplayAlpha.setEnabled(overviewActivation);
			textOverviewSelectionAlpha.setEnabled(overviewActivation);
			btnOverviewEnableLeavesAgg.setEnabled(overviewActivation);
			btnOverviewEnableLeavesAgg.notifyListeners(SWT.Selection, new Event());
		}
	}

	private class OverviewPreAggregListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			boolean preAggegActivation = btnOverviewEnableLeavesAgg.getSelection() && btnOverviewEnableLeavesAgg.getEnabled();

			spinnerOverviewMaxAggLeaves.setEnabled(preAggegActivation);
		}
	}

	private class PreAggregListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			boolean preAggegActivation = btnEnableLeavesAgg.getSelection();
			
			spinnerMaxAggLeaves.setEnabled(preAggegActivation);
		}
	}


	private class EditColorSelection extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (btnColorMap.containsKey(e.getSource())) {
				// Get the currently saved color in the map
				Color c = btnColorMap.get(e.getSource());
				ColorDialog colorDialog = new ColorDialog(getShell());
				// Set the default color of the color dialog to the current color
				colorDialog.setRGB(new RGB(c.getRed(), c.getGreen(), c.getBlue()));
				RGB rgb = colorDialog.open();
				// If a color was selected
				if (rgb != null)
					// Save the color in the map
					btnColorMap.put((Button) e.getSource(), new Color(Display.getDefault(), rgb.red, rgb.green, rgb.blue));
			}
		}
	}

	private class ThresholdModifyListener implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {

			try {
				if (Float.parseFloat(textThreshold.getText()) < Float.MIN_VALUE || Float.parseFloat(textThreshold.getText()) > 1)
					textThreshold.setText(String.valueOf(OcelotlDefaultParameterConstants.Threshold));
			} catch (final NumberFormatException err) {
				textThreshold.setText(String.valueOf(OcelotlDefaultParameterConstants.Threshold));
			}
		}
	}
	
	public void modifyThreshold() {

		double textThresholdValue = Double.parseDouble(textThreshold.getText());
		if (settings.getThresholdPrecision() != textThresholdValue) {
			settings.setThresholdPrecision(textThresholdValue);

			if (ocelotlView.getHasChanged() == HasChanged.NOTHING || ocelotlView.getHasChanged() == HasChanged.PARAMETER)
				ocelotlView.setHasChanged(HasChanged.THRESHOLD);
		}
	}

	private class IncreasingQualityRadioSelectionAdapter extends SelectionAdapter {

		@Override
		public void widgetSelected(final SelectionEvent e) {
			btnDecreasingQualities.setSelection(!btnIncreasingQualities.getSelection());
		}
	}

	public void modifyIncreasingQuality() {
		if (settings.getIncreasingQualities() != btnIncreasingQualities.getSelection()) {
			ocelotlView.getOcelotlParameters().setGrowingQualities(btnIncreasingQualities.getSelection());
			settings.setIncreasingQualities(btnIncreasingQualities.getSelection());
		}
	}

	public void modifyNormalize() {
		if (settings.isNormalizedCurve() != btnNormalize.getSelection()) {
			settings.setNormalizedCurve(btnNormalize.getSelection());

			if (ocelotlView.getHasChanged() != HasChanged.ALL)
				ocelotlView.setHasChanged(HasChanged.NORMALIZE);
		}
	}

	/**
	 * Update the overview selection colors
	 */
	public void updateOverviewColors() {
		ocelotlView.getOverView().setDisplayBGColor(btnColorMap.get(btnEditBgOverviewDisplay));
		ocelotlView.getOverView().setDisplayFGColor(btnColorMap.get(btnEditFgOverviewDisplay));
		ocelotlView.getOverView().setDisplayAlphaValue(settings.getOverviewDisplayAlphaValue());
		ocelotlView.getOverView().setSelectFGColor(btnColorMap.get(btnEditFgOverviewSelected));
		ocelotlView.getOverView().setSelectBGColor(btnColorMap.get(btnEditBgOverviewSelected));
		ocelotlView.getOverView().setSelectAlphaValue(settings.getOverviewSelectionAlphaValue());
	}
	
	/**
	 * Update the main selection colors
	 */
	public void updateMainSelectionColors() {
		if (ocelotlView.getTimeLineView() != null) {
			ocelotlView.getTimeLineView().setActiveColorBG(btnColorMap.get(btnEditBgMainDisplay));
			ocelotlView.getTimeLineView().setActiveColorFG(btnColorMap.get(btnEditFgMainDisplay));
			ocelotlView.getTimeLineView().setActiveColorAlpha(settings.getMainDisplayAlphaValue());
			ocelotlView.getTimeLineView().setPotentialColorBG(btnColorMap.get(btnEditBgMainSelected));
			ocelotlView.getTimeLineView().setPotentialColorFG(btnColorMap.get(btnEditFgMainSelected));
			ocelotlView.getTimeLineView().setPotentialColorAlpha(settings.getMainSelectionAlphaValue());
		}
	}
	
	private class SaveSettingsListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			MessageDialog.openInformation(getShell(), "Settings saved", "Settings were saved as default Ocelolt settings.");

			setSettings();
			settings.saveSettings();
		}
	}

	/**
	 * Make sure that the entered number is a positive integer
	 */
	private class NumericTextFieldVerifyListener implements VerifyListener {

		@Override
		public void verifyText(VerifyEvent e) {

			Text text = (Text) e.getSource();

			// Get old text and create new text by using the VerifyEvent.text
			final String oldS = text.getText();
			String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

			boolean isValid = true;
			try {
				int res = Integer.parseInt(newS);
				if (res <= 0) {
					isValid = false;
				}
			} catch (NumberFormatException ex) {
				isValid = false;
			}

			// If not valid do not update the text
			if (!isValid)
				e.doit = false;
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite all = (Composite) super.createDialogArea(parent);

		final SashForm sashFormGlobal = new SashForm(all, SWT.VERTICAL);
		sashFormGlobal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashFormGlobal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		cantarell8 = new Font(sashFormGlobal.getDisplay(), new FontData("Cantarell", 8, SWT.NORMAL));

		tabFolder = new TabFolder(sashFormGlobal, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Cantarell", 9, SWT.NORMAL));

		// Aggregation settings
		final TabItem tbtmAdvancedParameters = new TabItem(tabFolder, 0);
		tbtmAdvancedParameters.setText("Aggregation");

		final SashForm sashFormAdvancedParameters = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormAdvancedParameters.setFont(cantarell8);
		tbtmAdvancedParameters.setControl(sashFormAdvancedParameters);
		
		final SashForm sashFormQualityCurve = new SashForm(sashFormAdvancedParameters, SWT.VERTICAL);
		sashFormQualityCurve.setFont(cantarell8);

		final Group groupQualityCurveSettings = new Group(sashFormQualityCurve, SWT.NONE);
		groupQualityCurveSettings.setFont(cantarell8);
		groupQualityCurveSettings.setText("Quality Curve Settings");
		groupQualityCurveSettings.setLayout(new GridLayout(4, false));
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnNormalize = new Button(groupQualityCurveSettings, SWT.CHECK);
		btnNormalize.setFont(cantarell8);
		btnNormalize.setSelection(settings.isNormalizedCurve());
		btnNormalize.setText("Normalize Qualities");
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnIncreasingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnIncreasingQualities.setFont(cantarell8);
		btnIncreasingQualities.setText("Complexity gain (green)\nInformation gain (red)");
		btnIncreasingQualities.addSelectionListener(new IncreasingQualityRadioSelectionAdapter());
		btnIncreasingQualities.setSelection(settings.getIncreasingQualities());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		btnDecreasingQualities = new Button(groupQualityCurveSettings, SWT.RADIO);
		btnDecreasingQualities.setText("Complexity reduction (green)\nInformation loss (red)");
		btnDecreasingQualities.setFont(cantarell8);
		btnDecreasingQualities.addSelectionListener(new IncreasingQualityRadioSelectionAdapter());
		btnDecreasingQualities.setSelection(!settings.getIncreasingQualities());
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		final Label lblThreshold = new Label(groupQualityCurveSettings, SWT.NONE);
		lblThreshold.setFont(cantarell8);
		lblThreshold.setText("X Axis Maximal Precision");

		textThreshold = new Text(groupQualityCurveSettings, SWT.BORDER);
		GridData gd_textThreshold = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textThreshold.widthHint = 100;
		textThreshold.setLayoutData(gd_textThreshold);
		textThreshold.setFont(cantarell8);
		textThreshold.addModifyListener(new ThresholdModifyListener());
		textThreshold.setText(String.valueOf(settings.getThresholdPrecision()));
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);
		new Label(groupQualityCurveSettings, SWT.NONE);

		final SashForm sashFormParameterP = new SashForm(sashFormAdvancedParameters, SWT.VERTICAL);
		sashFormParameterP.setFont(cantarell8);

		final Group groupParameterSettings = new Group(sashFormParameterP, SWT.NONE);
		groupParameterSettings.setFont(cantarell8);
		groupParameterSettings.setText("Parameter p Settings");
		groupParameterSettings.setLayout(new GridLayout(2, false));
		
		final Label lblParameterPStrategy= new Label(groupParameterSettings, SWT.NONE);
		lblParameterPStrategy.setFont(cantarell8);
		lblParameterPStrategy.setText("Default Parameter Value:");
		
		parameterPStrategy = new Combo(groupParameterSettings, SWT.READ_ONLY);
		GridData gd_parameterPStrategy = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		parameterPStrategy.setLayoutData(gd_parameterPStrategy);
		parameterPStrategy.setFont(cantarell8);
		parameterPStrategy.setToolTipText("Parameter Default Value Strategy");
		
		final SashForm sashFormVisualAggregate = new SashForm(sashFormAdvancedParameters, SWT.VERTICAL);
		sashFormVisualAggregate.setFont(cantarell8);
		
		final Group groupVisualAggregate = new Group(sashFormVisualAggregate, SWT.NONE);
		groupVisualAggregate.setFont(cantarell8);
		groupVisualAggregate.setText("Visual Aggregation");
		groupVisualAggregate.setLayout(new GridLayout(3, false));
		
		btnEnableVisualAggregation = new Button(groupVisualAggregate, SWT.CHECK);
		btnEnableVisualAggregation.setText("Enable Visual Aggregation");
		btnEnableVisualAggregation.setSelection(settings.isUseVisualAggregate());
		btnEnableVisualAggregation.setFont(cantarell8);
		btnEnableVisualAggregation.setToolTipText("Aggregate Producers that Are Too Small to Display");

		Label labelWarningImg = new Label (groupVisualAggregate, SWT.NONE);
		labelWarningImg.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/warn_tsk.gif"));
		
		Label labelWarningText = new Label (groupVisualAggregate, SWT.NONE);
		labelWarningText.setFont(cantarell8);
		labelWarningText.setText("Disabling this option can lead to discrepancies in the visualization");
			
		sashFormAdvancedParameters.setWeights(new int[] { 2, 1, 1 });
				
		// Datacache settings
		final TabItem tbtmOcelotlSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmOcelotlSettings.setText("Cache");

		final SashForm sashFormSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormSettings.setFont(cantarell8);
		tbtmOcelotlSettings.setControl(sashFormSettings);

		final Group groupDataCacheSettings = new Group(sashFormSettings, SWT.NONE);
		groupDataCacheSettings.setFont(cantarell8);
		groupDataCacheSettings.setText("Caches Settings");
		groupDataCacheSettings.setLayout(new GridLayout(3, false));

		final Label lblDataCacheSize = new Label(groupDataCacheSettings, SWT.NONE);
		lblDataCacheSize.setFont(cantarell8);
		lblDataCacheSize.setText("MB Caches Size (-1=unlimited):");
		
		dataCacheSize = new Spinner(groupDataCacheSettings, SWT.BORDER);
		dataCacheSize.setValues(0, -1, 99999999, 0, 1, 10);
		dataCacheSize.setFont(cantarell8);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 100;
		dataCacheSize.setLayoutData(gd_text);
		new Label(groupDataCacheSettings, SWT.NONE);

		final Label lblDataCacheDirectory = new Label(groupDataCacheSettings, SWT.NONE);
		lblDataCacheDirectory.setFont(cantarell8);
		lblDataCacheDirectory.setText("Caches directory:");

		final GridData gd_dataCacheDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dataCacheDir.widthHint = 100;

		datacacheDirectory = new Text(groupDataCacheSettings, SWT.BORDER);
		datacacheDirectory.setLayoutData(gd_dataCacheDir);
		datacacheDirectory.setFont(cantarell8);
		datacacheDirectory.setEditable(false);
		datacacheDirectory.setText(ocelotlView.getOcelotlParameters().getDataCache().getCacheDirectory());
		
		btnChangeCacheDirectory = new Button(groupDataCacheSettings, SWT.PUSH);
		btnChangeCacheDirectory.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnChangeCacheDirectory.setToolTipText("Change Caches Directory");
		btnChangeCacheDirectory.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/fldr_obj.gif"));
		btnChangeCacheDirectory.setFont(cantarell8);
		btnChangeCacheDirectory.addSelectionListener(new ModifyDatacacheDirectory());

		new Label(groupDataCacheSettings, SWT.NONE);
		btnDeleteDataCache = new Button(groupDataCacheSettings, SWT.PUSH);
		btnDeleteDataCache.setToolTipText("Empty Caches");
		btnDeleteDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/delete_obj.gif"));
		btnDeleteDataCache.setText("Empty Caches");
		btnDeleteDataCache.setFont(cantarell8);
		btnDeleteDataCache.addSelectionListener(new DeleteDataCache());
		new Label(groupDataCacheSettings, SWT.NONE);
		
		btnDataCacheEnabled = new Button(groupDataCacheSettings, SWT.CHECK);
		btnDataCacheEnabled.setFont(cantarell8);
		btnDataCacheEnabled.setText("Data Cache Enabled");
		btnDataCacheEnabled.setSelection(settings.isDataCacheActivated());
		btnDataCacheEnabled.addSelectionListener(new EnableCacheListener());
		new Label(groupDataCacheSettings, SWT.NONE);
		new Label(groupDataCacheSettings, SWT.NONE);
		
		if (settings.getCacheSize() > 0) {
			dataCacheSize.setSelection((int) (settings.getCacheSize() / 1000000));
		} else {
			dataCacheSize.setSelection((int) settings.getCacheSize());
		}

		Label lblCacheTimeSlices = new Label(groupDataCacheSettings, SWT.NONE);
		lblCacheTimeSlices.setText("Data Cache time slices:");
		lblCacheTimeSlices.setFont(cantarell8);
		lblCacheTimeSlices.setToolTipText("Number of Time Slices Used When Generating Data Cache");

		cacheTimeSliceValue = new Spinner(groupDataCacheSettings, SWT.BORDER);
		cacheTimeSliceValue.setValues(0, 0, 99999999, 0, 1, 10);
		cacheTimeSliceValue.setFont(cantarell8);
		GridData gd_cacheTimeSliceValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_cacheTimeSliceValue.widthHint = 100;
		cacheTimeSliceValue.setLayoutData(gd_cacheTimeSliceValue);
		cacheTimeSliceValue.setSelection(settings.getCacheTimeSliceNumber());
		new Label(groupDataCacheSettings, SWT.NONE);

		Label lblCachePolicy = new Label(groupDataCacheSettings, SWT.NONE);
		lblCachePolicy.setText("Data Cache policy");
		lblCachePolicy.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);
		new Label(groupDataCacheSettings, SWT.NONE);

		btnRadioButton = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton.addSelectionListener(new cachePolicyListener());
		btnRadioButton.setText("Precise (slow)");
		btnRadioButton.setFont(cantarell8);

		btnRadioButton_1 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_1.addSelectionListener(new cachePolicyListener());
		btnRadioButton_1.setText("Fast");
		btnRadioButton_1.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);

		btnRadioButton_2 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_2.addSelectionListener(new cachePolicyListener());
		btnRadioButton_2.setText("Ask me");
		btnRadioButton_2.setFont(cantarell8);

		btnRadioButton_3 = new Button(groupDataCacheSettings, SWT.RADIO);
		btnRadioButton_3.addSelectionListener(new cachePolicyListener());
		btnRadioButton_3.setText("Auto.");
		btnRadioButton_3.setFont(cantarell8);
		new Label(groupDataCacheSettings, SWT.NONE);

		cachepolicy.put(DatacachePolicy.CACHEPOLICY_SLOW, btnRadioButton);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_FAST, btnRadioButton_1);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_ASK, btnRadioButton_2);
		cachepolicy.put(DatacachePolicy.CACHEPOLICY_AUTO, btnRadioButton_3);
		cachepolicy.get(settings.getCachePolicy()).setSelection(true);
		sashFormSettings.setWeights(new int[] { 1 });
		btnDataCacheEnabled.notifyListeners(SWT.Selection, new Event());
		
		btnDichoCacheEnabled = new Button(groupDataCacheSettings, SWT.CHECK);
		btnDichoCacheEnabled.setFont(cantarell8);
		btnDichoCacheEnabled.setText("Dichotomy Cache Enabled");
		btnDichoCacheEnabled.setSelection(settings.isDichoCacheActivated());

		// Advanced settings
		final TabItem tbtmAdvancedSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmAdvancedSettings.setText("Advanced");

		SashForm advancedSettingsSashForm = new SashForm(tabFolder, SWT.VERTICAL);
		tbtmAdvancedSettings.setControl(advancedSettingsSashForm);
		final Group grpCacheManagement = new Group(advancedSettingsSashForm, SWT.NONE);
		grpCacheManagement.setFont(cantarell8);
		grpCacheManagement.setText("Iterator Management");
		grpCacheManagement.setLayout(new GridLayout(2, false));

		final Label lblPageSize = new Label(grpCacheManagement, SWT.NONE);
		lblPageSize.setFont(cantarell8);
		lblPageSize.setText("Event Number Retrieved by Threads");

		spinnerEventSize = new Spinner(grpCacheManagement, SWT.BORDER);
		spinnerEventSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerEventSize.setFont(cantarell8);
		spinnerEventSize.setMinimum(OcelotlDefaultParameterConstants.MIN_EVENTS_PER_THREAD);
		spinnerEventSize.setMaximum(OcelotlDefaultParameterConstants.MAX_EVENTS_PER_THREAD);
		spinnerEventSize.setSelection(settings.getEventsPerThread());

		final Group grpDivideDbQuery = new Group(advancedSettingsSashForm, SWT.NONE);
		grpDivideDbQuery.setFont(cantarell8);
		grpDivideDbQuery.setText("Query Management");
		grpDivideDbQuery.setLayout(new GridLayout(2, false));

		final Label lblDivideDbQueries = new Label(grpDivideDbQuery, SWT.NONE);
		lblDivideDbQueries.setFont(cantarell8);
		lblDivideDbQueries.setText("Event Producers per Query (0=All)");

		spinnerDivideDbQuery = new Spinner(grpDivideDbQuery, SWT.BORDER);
		spinnerDivideDbQuery.setFont(cantarell8);
		spinnerDivideDbQuery.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerDivideDbQuery.setMinimum(OcelotlDefaultParameterConstants.MIN_EVENT_PRODUCERS_PER_QUERY);
		spinnerDivideDbQuery.setMaximum(OcelotlDefaultParameterConstants.MAX_EVENT_PRODUCERS_PER_QUERY);
		spinnerDivideDbQuery.setSelection(settings.getMaxEventProducersPerQuery());

		final Group grpMultiThread = new Group(advancedSettingsSashForm, SWT.NONE);
		grpMultiThread.setFont(cantarell8);
		grpMultiThread.setText("Multi Threading");
		grpMultiThread.setLayout(new GridLayout(2, false));

		final Label lblThread = new Label(grpMultiThread, SWT.NONE);
		lblThread.setFont(cantarell8);
		lblThread.setText("Working Threads");

		spinnerThread = new Spinner(grpMultiThread, SWT.BORDER);
		spinnerThread.setFont(cantarell8);
		spinnerThread.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerThread.setMinimum(OcelotlDefaultParameterConstants.MIN_NUMBER_OF_THREAD);
		spinnerThread.setMaximum(OcelotlDefaultParameterConstants.MAX_NUMBER_OF_THREAD);
		spinnerThread.setSelection(settings.getNumberOfThread());
		
		final Group grpAggregateLeaves = new Group(advancedSettingsSashForm, SWT.NONE);
		grpAggregateLeaves.setFont(cantarell8);
		grpAggregateLeaves.setText("Leaves Aggregation");
		grpAggregateLeaves.setLayout(new GridLayout(2, false));
		
		btnEnableLeavesAgg = new Button(grpAggregateLeaves, SWT.CHECK);
		btnEnableLeavesAgg.setFont(cantarell8);
		btnEnableLeavesAgg.setSelection(settings.isAggregateLeaves());
		btnEnableLeavesAgg.setText("Enable Leaves Aggregation");
		btnEnableLeavesAgg.addSelectionListener(new PreAggregListener());
		new Label(grpAggregateLeaves, SWT.NONE);

		final Label lblAggLeaves = new Label(grpAggregateLeaves, SWT.NONE);
		lblAggLeaves.setFont(cantarell8);
		lblAggLeaves.setText("Max. Number of Leaves");

		spinnerMaxAggLeaves = new Spinner(grpAggregateLeaves, SWT.BORDER);
		spinnerMaxAggLeaves.setFont(cantarell8);
		spinnerMaxAggLeaves.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerMaxAggLeaves.setMinimum(OcelotlDefaultParameterConstants.MIN_NUMBER_OF_AGGLEAVES);
		spinnerMaxAggLeaves.setMaximum(OcelotlDefaultParameterConstants.MAX_NUMBER_OF_AGGLEAVES);
		spinnerMaxAggLeaves.setSelection(settings.getMaxNumberOfLeaves());
		advancedSettingsSashForm.setWeights(new int[] { 1, 1, 1, 1 });
		btnEnableLeavesAgg.notifyListeners(SWT.Selection, new Event());
		
		// Selection settings
		final TabItem tbtSelectionSettings = new TabItem(tabFolder, SWT.NONE);
		tbtSelectionSettings.setText("Selection");

		final SashForm sashFormSelectionSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormSelectionSettings.setFont(cantarell8);
		tbtSelectionSettings.setControl(sashFormSelectionSettings);

		final Group groupSelectionSettings = new Group(sashFormSelectionSettings, SWT.NONE);
		groupSelectionSettings.setFont(cantarell8);
		groupSelectionSettings.setText("Selection Settings");
		groupSelectionSettings.setLayout(new GridLayout(2, false));
		
		final Label lblSelectionBgDisplay = new Label(groupSelectionSettings, SWT.NONE);
		lblSelectionBgDisplay.setFont(cantarell8);
		lblSelectionBgDisplay.setText("Display Background");

		btnEditBgMainDisplay = new Button(groupSelectionSettings, SWT.NONE);
		btnEditBgMainDisplay.setToolTipText("Edit Color");
		btnEditBgMainDisplay.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditBgMainDisplay.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditBgMainDisplay, settings.getMainDisplayBgColor());

		final Label lblSelectionFgDisplay = new Label(groupSelectionSettings, SWT.NONE);
		lblSelectionFgDisplay.setFont(cantarell8);
		lblSelectionFgDisplay.setText("Display Foreground");

		btnEditFgMainDisplay = new Button(groupSelectionSettings, SWT.NONE);
		btnEditFgMainDisplay.setToolTipText("Edit Color");
		btnEditFgMainDisplay.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditFgMainDisplay.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditFgMainDisplay, settings.getMainDisplayFgColor());
		
		final Label lblSelectionDisplayAlpha = new Label(groupSelectionSettings, SWT.NONE);
		lblSelectionDisplayAlpha.setFont(cantarell8);
		lblSelectionDisplayAlpha.setText("Display Transparency");

		textMainDisplayAlpha = new Spinner(groupSelectionSettings, SWT.BORDER);
		textMainDisplayAlpha.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textMainDisplayAlpha.setIncrement(1);
		textMainDisplayAlpha.setMaximum(255);
		textMainDisplayAlpha.setMinimum(0);
		textMainDisplayAlpha.setFont(cantarell8);
		textMainDisplayAlpha.setSelection(settings.getMainDisplayAlphaValue());
		textMainDisplayAlpha.setToolTipText("Display Alpha Value (0 - 255)");
	
		final Label lblSelectionBgSelect = new Label(groupSelectionSettings, SWT.NONE);
		lblSelectionBgSelect.setFont(cantarell8);
		lblSelectionBgSelect.setText("Selection Background");

		btnEditBgMainSelected = new Button(groupSelectionSettings, SWT.NONE);
		btnEditBgMainSelected.setToolTipText("Edit Color");
		btnEditBgMainSelected.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditBgMainSelected.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditBgMainSelected, settings.getMainSelectionBgColor());
		
		final Label lblSelectionFgSelect = new Label(groupSelectionSettings, SWT.NONE);
		lblSelectionFgSelect.setFont(cantarell8);
		lblSelectionFgSelect.setText("Selection Foreground");

		btnEditFgMainSelected = new Button(groupSelectionSettings, SWT.NONE);
		btnEditFgMainSelected.setToolTipText("Edit Color");
		btnEditFgMainSelected.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditFgMainSelected.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditFgMainSelected, settings.getMainSelectionFgColor());
		
		final Label lblSelectionAlpha = new Label(groupSelectionSettings, SWT.NONE);
		lblSelectionAlpha.setFont(cantarell8);
		lblSelectionAlpha.setText("Selection Transparency");

		textMainSelectionAlpha = new Spinner(groupSelectionSettings, SWT.BORDER);
		textMainSelectionAlpha.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textMainSelectionAlpha.setIncrement(1);
		textMainSelectionAlpha.setMaximum(255);
		textMainSelectionAlpha.setMinimum(0);
		textMainSelectionAlpha.setFont(cantarell8);
		textMainSelectionAlpha.setSelection(settings.getMainSelectionAlphaValue());
		textMainSelectionAlpha.setToolTipText("Selection Alpha Value (0 - 255)");
		
		// Overview settings
		final TabItem tbtOverviewSettings = new TabItem(tabFolder, SWT.NONE);
		tbtOverviewSettings.setText("Overview");

		final SashForm sashFormOverviewSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormOverviewSettings.setFont(cantarell8);
		tbtOverviewSettings.setControl(sashFormOverviewSettings);


		final Group groupOverviewSettings = new Group(sashFormOverviewSettings, SWT.NONE);
		groupOverviewSettings.setFont(cantarell8);
		groupOverviewSettings.setText("Overview Settings");
		groupOverviewSettings.setLayout(new GridLayout(2, false));
		
		btnEnableOverview = new Button(groupOverviewSettings, SWT.CHECK);
		btnEnableOverview.setFont(cantarell8);
		btnEnableOverview.setSelection(settings.isEnableOverview());
		btnEnableOverview.setText("Display Overview");
		btnEnableOverview.addSelectionListener(new EnableOverviewListener());
		new Label(groupOverviewSettings, SWT.NONE);
		
		final Label lblBgDisplay = new Label(groupOverviewSettings, SWT.NONE);
		lblBgDisplay.setFont(cantarell8);
		lblBgDisplay.setText("Display Background");

		btnEditBgOverviewDisplay = new Button(groupOverviewSettings, SWT.NONE);
		btnEditBgOverviewDisplay.setToolTipText("Edit Color");
		btnEditBgOverviewDisplay.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditBgOverviewDisplay.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditBgOverviewDisplay, settings.getOverviewDisplayBgColor());

		final Label lblFgDisplay = new Label(groupOverviewSettings, SWT.NONE);
		lblFgDisplay.setFont(cantarell8);
		lblFgDisplay.setText("Display Foreground");

		btnEditFgOverviewDisplay = new Button(groupOverviewSettings, SWT.NONE);
		btnEditFgOverviewDisplay.setToolTipText("Edit Color");
		btnEditFgOverviewDisplay.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditFgOverviewDisplay.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditFgOverviewDisplay, settings.getOverviewDisplayFgColor());
		
		final Label lblDisplayAlpha = new Label(groupOverviewSettings, SWT.NONE);
		lblDisplayAlpha.setFont(cantarell8);
		lblDisplayAlpha.setText("Display Transparency");

		textOverviewDisplayAlpha = new Spinner(groupOverviewSettings, SWT.BORDER);
		textOverviewDisplayAlpha.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textOverviewDisplayAlpha.setIncrement(1);
		textOverviewDisplayAlpha.setMaximum(255);
		textOverviewDisplayAlpha.setMinimum(0);
		textOverviewDisplayAlpha.setFont(cantarell8);
		textOverviewDisplayAlpha.setSelection(settings.getOverviewDisplayAlphaValue());
		textOverviewDisplayAlpha.setToolTipText("Display Alpha Value (0 - 255)");
	
		final Label lblBgSelect = new Label(groupOverviewSettings, SWT.NONE);
		lblBgSelect.setFont(cantarell8);
		lblBgSelect.setText("Selection Background");

		btnEditBgOverviewSelected = new Button(groupOverviewSettings, SWT.NONE);
		btnEditBgOverviewSelected.setToolTipText("Edit Color");
		btnEditBgOverviewSelected.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditBgOverviewSelected.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditBgOverviewSelected, settings.getOverviewSelectionBgColor());
		
		final Label lblFgSelect = new Label(groupOverviewSettings, SWT.NONE);
		lblFgSelect.setFont(cantarell8);
		lblFgSelect.setText("Selection Foreground");

		btnEditFgOverviewSelected = new Button(groupOverviewSettings, SWT.NONE);
		btnEditFgOverviewSelected.setToolTipText("Edit Color");
		btnEditFgOverviewSelected.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditFgOverviewSelected.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditFgOverviewSelected, settings.getOverviewSelectionFgColor());
		
		final Label lblOverviewSelectionAlpha = new Label(groupOverviewSettings, SWT.NONE);
		lblOverviewSelectionAlpha.setFont(cantarell8);
		lblOverviewSelectionAlpha.setText("Selection Transparency");

		textOverviewSelectionAlpha = new Spinner(groupOverviewSettings, SWT.BORDER);
		textOverviewSelectionAlpha.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textOverviewSelectionAlpha.setIncrement(1);
		textOverviewSelectionAlpha.setMaximum(255);
		textOverviewSelectionAlpha.setMinimum(0);
		textOverviewSelectionAlpha.setFont(cantarell8);
		textOverviewSelectionAlpha.setSelection(settings.getOverviewSelectionAlphaValue());
		textOverviewSelectionAlpha.setToolTipText("Selection Alpha Value (0 - 255)");

		btnOverviewEnableLeavesAgg = new Button(groupOverviewSettings, SWT.CHECK);
		btnOverviewEnableLeavesAgg.setFont(cantarell8);
		btnOverviewEnableLeavesAgg.setSelection(settings.isOverviewAggregateLeaves());
		btnOverviewEnableLeavesAgg.setText("Enable Leaves Aggregation for Overview");
		btnOverviewEnableLeavesAgg.addSelectionListener(new OverviewPreAggregListener());
		new Label(groupOverviewSettings, SWT.NONE);
		
		final Label lblOverviewAggLeaves = new Label(groupOverviewSettings, SWT.NONE);
		lblOverviewAggLeaves.setFont(cantarell8);
		lblOverviewAggLeaves.setText("Max. Number of Leaves");

		spinnerOverviewMaxAggLeaves = new Spinner(groupOverviewSettings, SWT.BORDER);
		spinnerOverviewMaxAggLeaves.setFont(cantarell8);
		spinnerOverviewMaxAggLeaves.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerOverviewMaxAggLeaves.setMinimum(OcelotlDefaultParameterConstants.MIN_NUMBER_OF_AGGLEAVES);
		spinnerOverviewMaxAggLeaves.setMaximum(OcelotlDefaultParameterConstants.MAX_NUMBER_OF_AGGLEAVES);
		spinnerOverviewMaxAggLeaves.setSelection(settings.getOverviewMaxNumberOfLeaves());
		btnEnableOverview.notifyListeners(SWT.Selection, new Event());
		
		// Snapshot settings
		final TabItem tbtMiscSettings = new TabItem(tabFolder, SWT.NONE);
		tbtMiscSettings.setText("Snapshot");

		final SashForm sashFormMiscSettings = new SashForm(tabFolder, SWT.VERTICAL);
		tbtMiscSettings.setControl(sashFormMiscSettings);
		sashFormMiscSettings.setFont(cantarell8);

		final Group groupMiscSettings = new Group(sashFormMiscSettings, SWT.NONE);
		groupMiscSettings.setFont(cantarell8);
		groupMiscSettings.setText("Snapshot Settings");
		groupMiscSettings.setLayout(new GridLayout(3, false));

		final Label lblSnapshotDirectory = new Label(groupMiscSettings, SWT.NONE);
		lblSnapshotDirectory.setFont(cantarell8);
		lblSnapshotDirectory.setText("Snapshot Directory:");

		final GridData gd_MiscDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_MiscDir.widthHint = 100;

		snapshotDirectory = new Text(groupMiscSettings, SWT.BORDER);
		snapshotDirectory.setLayoutData(gd_MiscDir);
		snapshotDirectory.setFont(cantarell8);
		snapshotDirectory.setEditable(false);
		snapshotDirectory.setText(settings.getSnapShotDirectory());

		btnChangeSnapshotDirectory = new Button(groupMiscSettings, SWT.PUSH);
		btnChangeSnapshotDirectory.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnChangeSnapshotDirectory.setToolTipText("Change Snapshot Directory");
		btnChangeSnapshotDirectory.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/fldr_obj.gif"));
		btnChangeSnapshotDirectory.setFont(cantarell8);

		Label lblsnapshotWidth = new Label(groupMiscSettings, SWT.NONE);
		lblsnapshotWidth.setFont(cantarell8);
		lblsnapshotWidth.setText("Main View Snapshot Width");

		snapshotWidth = new Text(groupMiscSettings, SWT.BORDER);
		snapshotWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		snapshotWidth.setFont(cantarell8);
		snapshotWidth.setToolTipText("Width of the Generated Image of the Main Diagram in Pixels");
		snapshotWidth.setText(String.valueOf(settings.getSnapshotXResolution()));
		snapshotWidth.addVerifyListener(new NumericTextFieldVerifyListener());
		new Label(groupMiscSettings, SWT.NONE);

		Label lblsnapshotHeight = new Label(groupMiscSettings, SWT.NONE);
		lblsnapshotHeight.setFont(cantarell8);
		lblsnapshotHeight.setText("Main View Snapshot Height");

		snapshotHeight = new Text(groupMiscSettings, SWT.BORDER);
		snapshotHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		snapshotHeight.setText(String.valueOf(settings.getSnapshotYResolution()));
		snapshotHeight.setFont(cantarell8);
		snapshotHeight.setToolTipText("Height of the Generated Image of the Main Diagram in Pixels");
		snapshotHeight.addVerifyListener(new NumericTextFieldVerifyListener());
		new Label(groupMiscSettings, SWT.NONE);

		Label lblxAxisHeight = new Label(groupMiscSettings, SWT.NONE);
		lblxAxisHeight.setFont(cantarell8);
		lblxAxisHeight.setText("X Axis Height");
		
		xAxisHeight = new Spinner(groupMiscSettings, SWT.BORDER);
		xAxisHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xAxisHeight.setIncrement(1);
		xAxisHeight.setMaximum(100000);
		xAxisHeight.setMinimum(10);
		xAxisHeight.setFont(cantarell8);
		xAxisHeight.setSelection(settings.getxAxisYResolution());
		xAxisHeight.setToolTipText("Height of the Generated Image of the X Axis in Pixels (10 - 100000)");
		new Label(groupMiscSettings, SWT.NONE);
		
		Label lblyAxisWidth = new Label(groupMiscSettings, SWT.NONE);
		lblyAxisWidth.setFont(cantarell8);
		lblyAxisWidth.setText("Y Axis Width");
		
		yAxisWidth = new Spinner(groupMiscSettings, SWT.BORDER);
		yAxisWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		yAxisWidth.setIncrement(1);
		yAxisWidth.setMaximum(100000);
		yAxisWidth.setMinimum(10);
		yAxisWidth.setFont(cantarell8);
		yAxisWidth.setSelection(settings.getyAxisXResolution());
		yAxisWidth.setToolTipText("Width of the Generated Image of the Y Axis in Pixels (10 - 100000)");
		new Label(groupMiscSettings, SWT.NONE);
			
		Label lblQualCurvesWidth = new Label(groupMiscSettings, SWT.NONE);
		lblQualCurvesWidth.setFont(cantarell8);
		lblQualCurvesWidth.setText("Quality Curves Width");
		
		qualCurveWidth = new Spinner(groupMiscSettings, SWT.BORDER);
		qualCurveWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		qualCurveWidth.setIncrement(1);
		qualCurveWidth.setMaximum(100000);
		qualCurveWidth.setMinimum(10);
		qualCurveWidth.setFont(cantarell8);
		qualCurveWidth.setSelection(settings.getQualCurveXResolution());
		qualCurveWidth.setToolTipText("Width of the Generated Image of the Quality Curves in Pixels (10 - 100000)");
		new Label(groupMiscSettings, SWT.NONE);
		
		Label lblQualCurvesHeight = new Label(groupMiscSettings, SWT.NONE);
		lblQualCurvesHeight.setFont(cantarell8);
		lblQualCurvesHeight.setText("Quality Curves Height");
		
		qualCurveHeight = new Spinner(groupMiscSettings, SWT.BORDER);
		qualCurveHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		qualCurveHeight.setIncrement(1);
		qualCurveHeight.setMaximum(100000);
		qualCurveHeight.setMinimum(10);
		qualCurveHeight.setFont(cantarell8);
		qualCurveHeight.setSelection(settings.getQualCurveYResolution());
		qualCurveHeight.setToolTipText("Height of the Generated Image of the Quality Curves in Pixels (10 - 100000)");
		new Label(groupMiscSettings, SWT.NONE);
		
		btnChangeSnapshotDirectory.addSelectionListener(new ModifySnapshotDirectory());
		
		initSettings();
		
		return sashFormGlobal;
	}

	@Override
	protected void okPressed() {
		setSettings();
		super.okPressed();
	}

	/**
	 * Set a customize title for the setting window
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Ocelotl Settings");
	}

	protected void initSettings() {
		ArrayList<String> sortedStrategyName = new ArrayList<String>();
		sortedStrategyName.addAll(ParameterStrategy.availableStrategies.values());
		java.util.Collections.sort(sortedStrategyName, Collator.getInstance());
		for(String strategyName: sortedStrategyName)
			parameterPStrategy.add(strategyName);
		
		// Set current value
		parameterPStrategy.setText(ParameterStrategy.availableStrategies.get(ocelotlView.getOcelotlParameters().getOcelotlSettings().getParameterPPolicy()));
	}
	
	/**
	 * Save all the settings into the configuration file
	 */
	void setSettings() {
		// Cache settings
		settings.setDataCacheActivated(btnDataCacheEnabled.getSelection());
		settings.setDichoCacheActivated(btnDichoCacheEnabled.getSelection());
		settings.setCacheTimeSliceNumber(Integer.valueOf(cacheTimeSliceValue.getText()));
		modifyDataCacheSize();
		updateCacheDir();
		settings.setCachePolicy(currentSelectedDatacachePolicy);

		// Parameter P strategy
		settings.setParameterPPolicy(ocelotlView.getParameterPPolicy().getStrategy(parameterPStrategy.getText()));

		// Advanced settings
		settings.setNumberOfThread(Integer.valueOf(spinnerThread.getText()));
		settings.setMaxEventProducersPerQuery(Integer.valueOf(spinnerDivideDbQuery.getText()));
		settings.setEventsPerThread(Integer.valueOf(spinnerEventSize.getText()));

		boolean hasChangedAll = false;
		if (settings.setAggregateLeaves(btnEnableLeavesAgg.getSelection()))
			hasChangedAll = true;

		if (settings.setMaxNumberOfLeaves(Integer.valueOf(spinnerMaxAggLeaves.getText())))
			hasChangedAll = true;

		if (hasChangedAll)
			ocelotlView.setHasChanged(HasChanged.ALL);
		
		settings.setUseVisualAggregate(btnEnableVisualAggregation.getSelection());
		
		// Curve settings
		modifyThreshold();
		modifyNormalize();
		modifyIncreasingQuality();

		// Snapshot.
		settings.setSnapShotDirectory(snapshotDirectory.getText());
		settings.setSnapshotXResolution(Integer.valueOf(snapshotWidth.getText()));
		settings.setSnapshotYResolution(Integer.valueOf(snapshotHeight.getText()));
		settings.setxAxisYResolution(Integer.valueOf(xAxisHeight.getText()));
		settings.setyAxisXResolution(Integer.valueOf(yAxisWidth.getText()));
		settings.setQualCurveXResolution(Integer.valueOf(qualCurveWidth.getText()));
		settings.setQualCurveYResolution(Integer.valueOf(qualCurveHeight.getText()));
		
		//Overview colors
		settings.setEnableOverview(btnEnableOverview.getSelection());
		settings.setOverviewDisplayBgColor(btnColorMap.get(btnEditBgOverviewDisplay));
		settings.setOverviewDisplayFgColor(btnColorMap.get(btnEditFgOverviewDisplay));
		settings.setOverviewDisplayAlphaValue(Integer.valueOf(textOverviewDisplayAlpha.getText()));
		settings.setOverviewSelectionBgColor(btnColorMap.get(btnEditBgOverviewSelected));
		settings.setOverviewSelectionFgColor(btnColorMap.get(btnEditFgOverviewSelected));
		settings.setOverviewSelectionAlphaValue(Integer.valueOf(textOverviewSelectionAlpha.getText()));
		settings.setOverviewAggregateLeaves(btnOverviewEnableLeavesAgg.getSelection());
		settings.setOverviewMaxNumberOfLeaves(Integer.valueOf(spinnerOverviewMaxAggLeaves.getText()));
		updateOverviewColors();
		
		// Main selection colors
		settings.setMainDisplayBgColor(btnColorMap.get(btnEditBgMainDisplay));
		settings.setMainDisplayFgColor(btnColorMap.get(btnEditFgMainDisplay));
		settings.setMainDisplayAlphaValue(Integer.valueOf(textMainDisplayAlpha.getText()));
		settings.setMainSelectionBgColor(btnColorMap.get(btnEditBgMainSelected));
		settings.setMainSelectionFgColor(btnColorMap.get(btnEditFgMainSelected));
		settings.setMainSelectionAlphaValue(Integer.valueOf(textMainSelectionAlpha.getText()));
		updateMainSelectionColors();
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}
	
	/**
	 * Add button to save the settings
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Change parent layout data to fill the whole bar
		parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		saveSettingsButton = createButton(parent, IDialogConstants.NO_ID, "Save Settings", false);
		saveSettingsButton.setToolTipText("Set Current Settings as Default Ocelotl Settings");
		saveSettingsButton.addSelectionListener(new SaveSettingsListener());
		
		// Create a spacer label
		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Update layout of the parent composite to count the spacer
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns++;
		layout.makeColumnsEqualWidth = false;

		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
	}

}

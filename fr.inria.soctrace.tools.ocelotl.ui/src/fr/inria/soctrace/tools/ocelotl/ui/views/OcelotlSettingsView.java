package fr.inria.soctrace.tools.ocelotl.ui.views;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.Dialog;
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
	private Button								btnCacheEnabled;
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
	private Button								btnEditBgDisplay;
	private Button								btnEditFgDisplay;
	private Button								btnEditBgSelected;
	private Button								btnEditFgSelected;
	private HashMap<Button, Color>				btnColorMap;
	private Spinner								textDisplayAlpha;
	private Spinner								textSelectionAlpha;
	private Button								btnEnableOverview;

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

	/**
	 * If necessary, update the snapshot directory
	 */
	private void modifySnapshotDir() {
		// Was there change in the datacache directory ?
		if (!snapshotDirectory.getText().equals(settings.getSnapShotDirectory()))
			// If so, update the current datacache path
			ocelotlView.getSnapshot().setSnapshotDirectory(snapshotDirectory.getText());
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
		if (!currentDatacacheDir.isEmpty())
			// If so, update the current datacache path
			ocelotlView.getOcelotlParameters().getDataCache().setCacheDirectory(currentDatacacheDir);
	}

	private class EnableCacheListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			boolean cacheActivation = btnCacheEnabled.getSelection();

			btnDeleteDataCache.setEnabled(cacheActivation);
			datacacheDirectory.setEnabled(cacheActivation);
			btnChangeCacheDirectory.setEnabled(cacheActivation);
			btnRadioButton.setEnabled(cacheActivation);
			btnRadioButton_1.setEnabled(cacheActivation);
			btnRadioButton_2.setEnabled(cacheActivation);
			btnRadioButton_3.setEnabled(cacheActivation);
			cacheTimeSliceValue.setEnabled(cacheActivation);
			dataCacheSize.setEnabled(cacheActivation);
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
		ocelotlView.getOverView().setDisplayBGColor(btnColorMap.get(btnEditBgDisplay));
		ocelotlView.getOverView().setDisplayFGColor(btnColorMap.get(btnEditFgDisplay));
		ocelotlView.getOverView().setDisplayAlphaValue(settings.getOverviewDisplayAlphaValue());
		ocelotlView.getOverView().setSelectFGColor(btnColorMap.get(btnEditFgSelected));
		ocelotlView.getOverView().setSelectBGColor(btnColorMap.get(btnEditBgSelected));
		ocelotlView.getOverView().setSelectAlphaValue(settings.getOverviewSelectionAlphaValue());
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
		parameterPStrategy.setToolTipText("Parameter Default vValue Strategy");
			
		sashFormAdvancedParameters.setWeights(new int[] { 2, 1 });
		
		// Datacache settings
		final TabItem tbtmOcelotlSettings = new TabItem(tabFolder, SWT.NONE);
		tbtmOcelotlSettings.setText("Cache");

		final SashForm sashFormSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormSettings.setFont(cantarell8);
		tbtmOcelotlSettings.setControl(sashFormSettings);

		final Group groupDataCacheSettings = new Group(sashFormSettings, SWT.NONE);
		groupDataCacheSettings.setFont(cantarell8);
		groupDataCacheSettings.setText("Data Cache Settings");
		groupDataCacheSettings.setLayout(new GridLayout(3, false));

		btnCacheEnabled = new Button(groupDataCacheSettings, SWT.CHECK);
		btnCacheEnabled.setFont(cantarell8);
		btnCacheEnabled.setText("Cache Enabled");
		btnCacheEnabled.setSelection(settings.isCacheActivated());
		btnCacheEnabled.addSelectionListener(new EnableCacheListener());

		btnDeleteDataCache = new Button(groupDataCacheSettings, SWT.PUSH);
		btnDeleteDataCache.setToolTipText("Empty Cache");
		btnDeleteDataCache.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/delete_obj.gif"));
		btnDeleteDataCache.setText("Empty Cache");
		btnDeleteDataCache.setFont(cantarell8);
		btnDeleteDataCache.addSelectionListener(new DeleteDataCache());
		new Label(groupDataCacheSettings, SWT.NONE);

		final Label lblDataCacheDirectory = new Label(groupDataCacheSettings, SWT.NONE);
		lblDataCacheDirectory.setFont(cantarell8);
		lblDataCacheDirectory.setText("Data cache directory:");

		final GridData gd_dataCacheDir = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_dataCacheDir.widthHint = 100;

		datacacheDirectory = new Text(groupDataCacheSettings, SWT.BORDER);
		datacacheDirectory.setLayoutData(gd_dataCacheDir);
		datacacheDirectory.setFont(cantarell8);
		datacacheDirectory.setEditable(false);
		datacacheDirectory.setText(ocelotlView.getOcelotlParameters().getDataCache().getCacheDirectory());

		btnChangeCacheDirectory = new Button(groupDataCacheSettings, SWT.PUSH);
		btnChangeCacheDirectory.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnChangeCacheDirectory.setToolTipText("Change Cache Directory");
		btnChangeCacheDirectory.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.tools.ocelotl.ui", "icons/obj16/fldr_obj.gif"));
		btnChangeCacheDirectory.setFont(cantarell8);
		btnChangeCacheDirectory.addSelectionListener(new ModifyDatacacheDirectory());

		final Label lblDataCacheSize = new Label(groupDataCacheSettings, SWT.NONE);
		lblDataCacheSize.setFont(cantarell8);
		lblDataCacheSize.setText("MB Data cache size (-1=unlimited):");

		dataCacheSize = new Spinner(groupDataCacheSettings, SWT.BORDER);
		dataCacheSize.setValues(0, -1, 99999999, 0, 1, 10);
		dataCacheSize.setFont(cantarell8);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 100;
		dataCacheSize.setLayoutData(gd_text);
		new Label(groupDataCacheSettings, SWT.NONE);

		if (settings.getCacheSize() > 0) {
			dataCacheSize.setSelection((int) (settings.getCacheSize() / 1000000));
		} else {
			dataCacheSize.setSelection((int) settings.getCacheSize());
		}

		Label lblCacheTimeSlices = new Label(groupDataCacheSettings, SWT.NONE);
		lblCacheTimeSlices.setText("Cache time slices:");
		lblCacheTimeSlices.setFont(cantarell8);
		lblCacheTimeSlices.setToolTipText("Number of Time Slices Used When Generating Cache");

		cacheTimeSliceValue = new Spinner(groupDataCacheSettings, SWT.BORDER);
		cacheTimeSliceValue.setValues(0, 0, 99999999, 0, 1, 10);
		cacheTimeSliceValue.setFont(cantarell8);
		GridData gd_cacheTimeSliceValue = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_cacheTimeSliceValue.widthHint = 100;
		cacheTimeSliceValue.setLayoutData(gd_cacheTimeSliceValue);
		cacheTimeSliceValue.setSelection(settings.getCacheTimeSliceNumber());
		new Label(groupDataCacheSettings, SWT.NONE);

		Label lblCachePolicy = new Label(groupDataCacheSettings, SWT.NONE);
		lblCachePolicy.setText("Cache policy");
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
		btnCacheEnabled.notifyListeners(SWT.Selection, new Event());

		// Thread settings
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
		advancedSettingsSashForm.setWeights(new int[] { 1, 1, 1 });

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
		lblsnapshotWidth.setText("Snapshot Width");

		snapshotWidth = new Text(groupMiscSettings, SWT.BORDER);
		snapshotWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		snapshotWidth.setFont(cantarell8);
		snapshotWidth.setToolTipText("Width of the Generated Image in Pixels");
		snapshotWidth.setText(String.valueOf(settings.getSnapshotXResolution()));
		snapshotWidth.addVerifyListener(new NumericTextFieldVerifyListener());
		new Label(groupMiscSettings, SWT.NONE);

		Label lblsnapshotHeight = new Label(groupMiscSettings, SWT.NONE);
		lblsnapshotHeight.setFont(cantarell8);
		lblsnapshotHeight.setText("Snapshot Height");

		snapshotHeight = new Text(groupMiscSettings, SWT.BORDER);
		snapshotHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		snapshotHeight.setText(String.valueOf(settings.getSnapshotYResolution()));
		snapshotHeight.setFont(cantarell8);
		snapshotHeight.setToolTipText("Height of the Generated Image in Pixels");
		snapshotHeight.addVerifyListener(new NumericTextFieldVerifyListener());
		new Label(groupMiscSettings, SWT.NONE);

		btnChangeSnapshotDirectory.addSelectionListener(new ModifySnapshotDirectory());

		// Overview settings
		final TabItem tbtOverviewSettings = new TabItem(tabFolder, SWT.NONE);
		tbtOverviewSettings.setText("Overview");

		final SashForm sashFormOverviewSettings = new SashForm(tabFolder, SWT.VERTICAL);
		sashFormOverviewSettings.setFont(cantarell8);
		tbtOverviewSettings.setControl(sashFormOverviewSettings);


		final Group groupOverviewSettings = new Group(sashFormOverviewSettings, SWT.NONE);
		groupOverviewSettings.setFont(cantarell8);
		groupOverviewSettings.setText("Snapshot Settings");
		groupOverviewSettings.setLayout(new GridLayout(2, false));
		
		btnEnableOverview = new Button(groupOverviewSettings, SWT.CHECK);
		btnEnableOverview.setFont(cantarell8);
		btnEnableOverview.setSelection(settings.isEnableOverview());
		btnEnableOverview.setText("Display Overview");
		new Label(groupOverviewSettings, SWT.NONE);
		
		final Label lblBgDisplay = new Label(groupOverviewSettings, SWT.NONE);
		lblBgDisplay.setFont(cantarell8);
		lblBgDisplay.setText("Display Background");

		btnEditBgDisplay = new Button(groupOverviewSettings, SWT.NONE);
		btnEditBgDisplay.setToolTipText("Edit Color");
		btnEditBgDisplay.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditBgDisplay.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditBgDisplay, settings.getOverviewDisplayBgColor());

		final Label lblFgDisplay = new Label(groupOverviewSettings, SWT.NONE);
		lblFgDisplay.setFont(cantarell8);
		lblFgDisplay.setText("Display Foreground");

		btnEditFgDisplay = new Button(groupOverviewSettings, SWT.NONE);
		btnEditFgDisplay.setToolTipText("Edit Color");
		btnEditFgDisplay.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditFgDisplay.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditFgDisplay, settings.getOverviewDisplayFgColor());
		
		final Label lblDisplayAlpha = new Label(groupOverviewSettings, SWT.NONE);
		lblDisplayAlpha.setFont(cantarell8);
		lblDisplayAlpha.setText("Display Transparency");

		textDisplayAlpha = new Spinner(groupOverviewSettings, SWT.BORDER);
		textDisplayAlpha.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textDisplayAlpha.setIncrement(1);
		textDisplayAlpha.setMaximum(255);
		textDisplayAlpha.setMinimum(0);
		textDisplayAlpha.setFont(cantarell8);
		textDisplayAlpha.setSelection(settings.getOverviewDisplayAlphaValue());
		textDisplayAlpha.setToolTipText("Display Alpha Value (0 - 255)");
	
		final Label lblBgSelect = new Label(groupOverviewSettings, SWT.NONE);
		lblBgSelect.setFont(cantarell8);
		lblBgSelect.setText("Selection Background");

		btnEditBgSelected = new Button(groupOverviewSettings, SWT.NONE);
		btnEditBgSelected.setToolTipText("Edit Color");
		btnEditBgSelected.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditBgSelected.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditBgSelected, settings.getOverviewSelectionBgColor());
		
		final Label lblFgSelect = new Label(groupOverviewSettings, SWT.NONE);
		lblFgSelect.setFont(cantarell8);
		lblFgSelect.setText("Selection Foreground");

		btnEditFgSelected = new Button(groupOverviewSettings, SWT.NONE);
		btnEditFgSelected.setToolTipText("Edit Color");
		btnEditFgSelected.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui", "icons/edit2.png"));
		btnEditFgSelected.addSelectionListener(new EditColorSelection());
		btnColorMap.put(btnEditFgSelected, settings.getOverviewSelectionFgColor());
		
		final Label lblSelectionAlpha = new Label(groupOverviewSettings, SWT.NONE);
		lblSelectionAlpha.setFont(cantarell8);
		lblSelectionAlpha.setText("Selection Transparency");

		textSelectionAlpha = new Spinner(groupOverviewSettings, SWT.BORDER);
		textSelectionAlpha.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textSelectionAlpha.setIncrement(1);
		textSelectionAlpha.setMaximum(255);
		textSelectionAlpha.setMinimum(0);
		textSelectionAlpha.setFont(cantarell8);
		textSelectionAlpha.setSelection(settings.getOverviewSelectionAlphaValue());
		textSelectionAlpha.setToolTipText("Selection Alpha Value (0 - 255)");

		initSettings();
		
		return sashFormGlobal;
	}

	@Override
	protected void okPressed() {
		saveSettings();
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
	void saveSettings() {
		// Cache settings
		settings.setCacheActivated(btnCacheEnabled.getSelection());
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

		// Curve settings
		modifyThreshold();
		modifyNormalize();
		modifyIncreasingQuality();

		// Misc.
		modifySnapshotDir();
		settings.setSnapshotXResolution(Integer.valueOf(snapshotWidth.getText()));
		settings.setSnapshotYResolution(Integer.valueOf(snapshotHeight.getText()));
		
		//Overview colors
		settings.setEnableOverview(btnEnableOverview.getSelection());
		settings.setOverviewDisplayBgColor(btnColorMap.get(btnEditBgDisplay));
		settings.setOverviewDisplayFgColor(btnColorMap.get(btnEditFgDisplay));
		settings.setOverviewDisplayAlphaValue(Integer.valueOf(textDisplayAlpha.getText()));
		settings.setOverviewSelectionBgColor(btnColorMap.get(btnEditBgSelected));
		settings.setOverviewSelectionFgColor(btnColorMap.get(btnEditFgSelected));
		settings.setOverviewSelectionAlphaValue(Integer.valueOf(textSelectionAlpha.getText()));
		updateOverviewColors();
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

}

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

package fr.inria.soctrace.tools.ocelotl.ui.snapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.utils.FilenameValidator;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;
import fr.inria.soctrace.tools.ocelotl.ui.views.QualityView;
import fr.inria.soctrace.tools.ocelotl.ui.views.TimeAxisView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.AggregatedView;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.timelineview.TimeLineViewWrapper;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisView;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisViewManager;
import fr.inria.soctrace.tools.ocelotl.ui.views.unitAxisView.UnitAxisViewWrapper;

public class Snapshot {

	private static final Logger	logger	= LoggerFactory.getLogger(Snapshot.class);

	// Directory where all the snapshots are saved
	private String				snapshotDirectory;
	private OcelotlView			theView;

	public Snapshot(String directory, OcelotlView aView) {
		theView = aView;
		snapshotDirectory = theView.getOcelotlParameters().getOcelotlSettings().getSnapShotDirectory();
	}

	public String getSnapshotDirectory() {
		return snapshotDirectory;
	}

	/**
	 * Call all the methods that create a snapshot of the current state of Ocelotl
	 */
	public void takeSnapShot() {
		// Create and set directory
		String currentDirPath = createDirectory();

		// Save the currently displayed diagram as an image
		snapShotDiagram(currentDirPath);
		// Save the currently displayed quality curves as an image
		snapShotQualityCurve(currentDirPath);
		// Save the currently displayed axes 
		snapShotAxes(currentDirPath);
		// Save the current parameters in a text file
		saveConfig(currentDirPath);
		// Save the current parameter P values (+ gain/loss) in a .csv
		saveParameterValues(currentDirPath);
		// Take a snapshot of the statistics
		saveStatistics(currentDirPath);
		
		// Create a symbolic link to the trace file
		// createSymLink(currentDirPath);
	}

	/**
	 * Create a png image of the diagram
	 * @param dirPath
	 */
	public void snapShotDiagram(String dirPath) {
		Shell dialogMainView = new Shell(Display.getDefault());
		dialogMainView.setSize(theView.getOcelotlParameters().getOcelotlSettings().getSnapshotXResolution() + 2, theView.getOcelotlParameters().getOcelotlSettings().getSnapshotYResolution() + 2);

		// Init drawing display zone
		Composite compositeMainView = new Composite(dialogMainView, SWT.BORDER);
		compositeMainView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		// Make sure we remove the title bar from the size in order to
		// display it fully
		compositeMainView.setSize(dialogMainView.getSize().x, dialogMainView.getSize().y);
		compositeMainView.setLayout(new FillLayout());
	
		TimeLineViewWrapper mainViewWrapper = new TimeLineViewWrapper(theView);
		mainViewWrapper.init(compositeMainView);
		TimeLineViewManager mainViewManager = new TimeLineViewManager(theView);
		AggregatedView mainView = (AggregatedView) mainViewManager.create();
		mainViewWrapper.setView(mainView);
		mainView.createDiagram(theView.getCore().getLpaggregManager(), theView.getOcelotlParameters().getTimeRegion(),  theView.getCore().getVisuOperator());
		mainView.getSelectFigure().draw(theView.getTimeRegion(), -1, -1);
		mainView.setSelectTime(((AggregatedView) theView.getTimeLineView()).getSelectTime());
		mainView.setCurrentlySelectedNode(((AggregatedView) theView.getTimeLineView()).getCurrentlySelectedNode());
		mainView.drawSelection();
		compositeMainView.layout();
		
		createSnapshotFor(mainView.getRoot(), dirPath + "/diagram.png");
	}

	/**
	 * Create a png image of the quality curve
	 * @param dirPath
	 */
	public void snapShotQualityCurve(String dirPath) {
		Shell dialogQualityView = new Shell(Display.getDefault());
		dialogQualityView.setSize(theView.getOcelotlParameters().getOcelotlSettings().getQualCurveXResolution() + 2, theView.getOcelotlParameters().getOcelotlSettings().getQualCurveYResolution() + 2);

		// Init drawing display zone
		Composite compositeQualityView = new Composite(dialogQualityView, SWT.BORDER);
		compositeQualityView.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeQualityView.setFont(SWTResourceManager.getFont("Cantarell", 11, SWT.NORMAL));
		compositeQualityView.setSize(dialogQualityView.getSize().x, dialogQualityView.getSize().y);
		compositeQualityView.setLayout(new FillLayout());
		
		QualityView aQualityView = new QualityView(theView);
		aQualityView.initDiagram(compositeQualityView);
		aQualityView.createDiagram();
		compositeQualityView.layout();
		
		createSnapshotFor(aQualityView.getRoot(), dirPath + "/curves.png");
	}

	/**
	 * Create png images of the axes
	 * @param dirPath
	 */
	public void snapShotAxes(String dirPath) {
		// Time axis snapshot
		Shell dialogTimeAxis = new Shell(Display.getDefault());
		dialogTimeAxis.setSize(theView.getOcelotlParameters().getOcelotlSettings().getSnapshotXResolution() + 2, theView.getOcelotlParameters().getOcelotlSettings().getxAxisYResolution() + 2);

		// Init drawing display zone
		Composite compositeOverview = new Composite(dialogTimeAxis, SWT.BORDER);
		compositeOverview.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeOverview.setSize(dialogTimeAxis.getSize().x, dialogTimeAxis.getSize().y);
		compositeOverview.setLayout(new FillLayout());
		
		TimeAxisView aTimeAxisView = new TimeAxisView();
		aTimeAxisView.initDiagram(compositeOverview);
		// If the selected time region is identical to the whole displayed time
		// region
		if (theView.getOcelotlParameters().getTimeRegion().compareTimeRegion(theView.getTimeRegion()))
			// Do not draw the selection
			aTimeAxisView.createDiagram(theView.getOcelotlParameters().getTimeRegion());
		else
			aTimeAxisView.createDiagram(theView.getOcelotlParameters().getTimeRegion(), theView.getTimeRegion(), false);
		
		compositeOverview.layout();
		createSnapshotFor(aTimeAxisView.getRoot(), dirPath + "/XAxis.png");
	
		// Y axis snapshot
		Shell dialogUnitAxis = new Shell(Display.getDefault());
		dialogUnitAxis.setSize(theView.getOcelotlParameters().getOcelotlSettings().getyAxisXResolution() + 2, theView.getOcelotlParameters().getOcelotlSettings().getSnapshotYResolution() + 2);

		// Init drawing display zone
		Composite compositeUnitAxis = new Composite(dialogUnitAxis, SWT.BORDER);
		compositeUnitAxis.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		compositeUnitAxis.setSize(dialogUnitAxis.getSize().x, dialogUnitAxis.getSize().y);
		compositeUnitAxis.setLayout(new FillLayout());
	
		UnitAxisViewWrapper aUnitAxisWrapper = new UnitAxisViewWrapper(theView);
		aUnitAxisWrapper.init(compositeUnitAxis);
		UnitAxisViewManager aUnitAxisManager = new UnitAxisViewManager(theView);
		UnitAxisView unitAxisView = aUnitAxisManager.create();
		aUnitAxisWrapper.setView(unitAxisView);
		unitAxisView.createDiagram(theView.getCore().getVisuOperator());
		unitAxisView.select(theView.getUnitAxisView().getOriginY(), theView.getUnitAxisView().getCornerY(), true);
		compositeUnitAxis.layout();
		createSnapshotFor(unitAxisView.getRoot(), dirPath + "/YAxis.png");

		// Reupdate with the current timeline view in order to reset back to
		// correct selection values
		theView.getTimeLineView().drawSelection();
	}

	/**
	 * Save the actual configuration (trace name, number of slice , start and
	 * end timestamps, used operators, parameter, gain and loss)
	 */
	public String saveConfig(String aDirPath) {
		String config = getParameters();
		PrintWriter writer;
		
		try {
			writer = new PrintWriter(aDirPath + "/parameters.txt", "UTF-8");

			writer.print(config);

			// Close the fd
			writer.flush();
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return config;
	}

	/**
	 * Format the parameters as a string
	 * 
	 * @return the formatted string
	 */
	public String getParameters() {
		List<DLPQuality> qualities = theView.getCore().getLpaggregManager().getQualities();
		List<Double> parameters = theView.getCore().getLpaggregManager().getParameters();
		double gain = 0;
		double loss = 0;

		// Look for the gain and loss value
		for (int i = 0; i < parameters.size(); i++) {
			if (theView.getOcelotlParameters().getParameter() <= parameters.get(i)) {
				gain = qualities.get(i).getGain();
				loss = qualities.get(i).getLoss();
				break;
			}
		}

		StringBuffer output = new StringBuffer();
		output.append("Trace name: ");
		output.append(theView.getOcelotlParameters().getTrace().getAlias());
		output.append("\nNumber of slices: ");
		output.append(theView.getOcelotlParameters().getTimeSlicesNumber());
		output.append("\nDisplayed Start timestamp: ");
		output.append(theView.getTextDisplayedStart().getText());
		output.append("\nDisplayed End timestamp: ");
		output.append(theView.getTextDisplayedEnd().getText());
		output.append("\nSelected Start timestamp: ");
		output.append(theView.getTextTimestampStart().getText());
		output.append("\nSelected End timestamp: ");
		output.append(theView.getTextTimestampEnd().getText());
		output.append("\nMetric type: ");
		output.append(theView.getOcelotlParameters().getMicroModelType());
		output.append("\nAggregation Operator: ");
		output.append(theView.getOcelotlParameters().getDataAggOperator());
		output.append("\nVisualization Operator: ");
		output.append(theView.getOcelotlParameters().getVisuOperator());
		output.append("\nStatistics Operator: ");
		output.append(theView.getOcelotlParameters().getStatOperator());
		if (theView.getCore().getVisuOperator().getMaxValue() > 0) {
			output.append("\nMax Amplitude Value: ");
			output.append(theView.getCore().getVisuOperator().getMaxValue());
		}
		output.append("\nParameter: ");
		output.append(theView.getOcelotlParameters().getParameter());
		output.append("\nGain: ");
		output.append(gain);
		output.append(" - Loss: ");
		output.append(loss);

		return output.toString();
	}

	/**
	 * Create a unique directory for the current snapshot
	 */
	public String createDirectory() {
		String dirName = "";
		File dir = new File(snapshotDirectory);
		
		// Check if the general directory exists
		if (!dir.exists()) {
			logger.debug("Snapshot directory (" + snapshotDirectory + ") does not exist and will be created now.");

			// Create the general snapshot directory
			if (!dir.mkdirs()) {
				logger.error("Failed to create cache directory: " + snapshotDirectory + ".");
			}
		}

		Date aDate = new Date(System.currentTimeMillis());
		String convertedDate = new SimpleDateFormat("dd-MM-yyyy_HHmmss_z").format(aDate);
		
		String fileName = theView.getOcelotlParameters().getTrace().getAlias() + "_" + convertedDate;
		fileName = FilenameValidator.checkNameValidity(fileName);
			
		dirName = snapshotDirectory + "/" + fileName;

		// Create the specific snapshot directory
		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Failed to create cache directory: " + dirName + ".");
		}

		return dirName;
	}
	
	/**
	 * Save the the current parameter P values (+ gain/loss) in a .csv
	 */
	public void saveParameterValues(String aDirPath) {
		StringBuffer output = new StringBuffer();
		List<DLPQuality> qualities = theView.getCore().getLpaggregManager().getQualities();
		List<Double> parameters = theView.getCore().getLpaggregManager().getParameters();
		
		// CSV header
		output.append("PARAMETER" + OcelotlConstants.CSVDelimiter + "GAIN" + OcelotlConstants.CSVDelimiter + "LOSS\n");

		// Get all parameters, gain and loss values
		for (int i = 0; i < parameters.size(); i++) {
			output.append(parameters.get(i) + OcelotlConstants.CSVDelimiter);
			output.append(qualities.get(i).getGain() + OcelotlConstants.CSVDelimiter);
			output.append(qualities.get(i).getLoss() + "\n");
		}

		// Save into a file
		PrintWriter writer;

		try {
			writer = new PrintWriter(aDirPath + "/parameterPValues.csv", "UTF-8");
			writer.print(output.toString());

			// Close the fd
			writer.flush();
			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveStatistics(String aDirPath) {
		String stats = theView.getStatView().getStatDataToCSV();

		if (stats.isEmpty()) {
			logger.debug("Failed to convert statisitics in CSV format.");
			return;
		}

		// Save into a file
		PrintWriter writer;

		try {
			writer = new PrintWriter(aDirPath + "/statistics.csv", "UTF-8");
			writer.print(stats);

			// Close the fd
			writer.flush();
			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create a symbolic link to the trace file
	 * @param aDirPath
	 */
	public void createSymLink(String aDirPath) {
		Process p;
		try {
			p = Runtime.getRuntime().exec("ln -s " + aDirPath + "/A\\ Link\\ to\\ the\\ trace");
			p.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Create an image from the Figure given in argument
	 * 
	 * @param figure
	 *            Figure from which the image is created
	 * @param fileName
	 *            Path where to save the image
	 */
	public void createSnapshotFor(Figure figure, String fileName) {
		byte[] imageBytes = createImage(figure, SWT.IMAGE_PNG);
		
		if (imageBytes == null) {
			logger.debug("Image generation failed: snapshot image will not be created");
			return;
		}

		try {
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(imageBytes);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generate the image
	 * 
	 * @param figure
	 *            Figure from which the image is created
	 * @param format
	 *            format of the generated image
	 * @return an array of bytes corresponding to an image
	 */
	private byte[] createImage(Figure figure, int format) {
		Device device = Display.getCurrent();
		Rectangle r = figure.getBounds();
		
		if (r.width <= 0 || r.height <= 0) {
			logger.debug("Size of figure is 0: stopping generation");
			return null;
		}

		ByteArrayOutputStream result = new ByteArrayOutputStream();

		Image image = null;
		GC gc = null;
		Graphics g = null;
		try {
			image = new Image(device, r.width, r.height);
			gc = new GC(image);
			g = new SWTGraphics(gc);
			g.translate(r.x * -1, r.y * -1);

			figure.paint(g);

			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(result, format);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (g != null) {
				g.dispose();
			}
			if (gc != null) {
				gc.dispose();
			}
			if (image != null) {
				image.dispose();
			}
		}
		return result.toByteArray();
	}
	
	/**
	 * Check that the snapshot directory is a valid one, i.e. does it exist and can
	 * it be written in
	 * 
	 * @param snapDirectory
	 *            path to the new snap directory
	 * @return true if valid, false otherwise
	 */
	public boolean checkSnapDirectoryValidity(String snapDirectory) {

		// Check the existence of the cache directory
		File dir = new File(snapDirectory);
		if (!dir.exists()) {
			logger.debug("Snapshot directory (" + snapDirectory + ") does not exist and will be created now.");

			// Create the directory
			if (!dir.mkdirs()) {
				logger.error("Failed to create snapshot directory: " + snapDirectory + ".");

				if (this.snapshotDirectory.isEmpty()) {
					logger.error("The current snapshot directory is still: " + this.snapshotDirectory);
				}
				return false;
			}
		}

		// Check that we have at least the reading rights
		if (!dir.canWrite()) {
			logger.error("The application does not have the rights to write in the given directory: " + snapDirectory + ".");

			if (this.snapshotDirectory.isEmpty()) {
				logger.error("The cache will be turned off.");
			} else {
				logger.error("The current cache directory is still: " + this.snapshotDirectory);
			}
			return false;
		}

		return true;
	}

}

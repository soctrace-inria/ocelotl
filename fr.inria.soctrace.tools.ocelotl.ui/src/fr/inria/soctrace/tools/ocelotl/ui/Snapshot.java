package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class Snapshot {

	private static final Logger	logger	= LoggerFactory.getLogger(Snapshot.class);

	// Directory where all the snapshots are saved
	private String				snapshotDirectory;
	private OcelotlView			theView;

	public Snapshot(String directory, OcelotlView aView) {
		snapshotDirectory = directory;
		theView = aView;
	}

	public String getSnapshotDirectory() {
		return snapshotDirectory;
	}

	public void setSnapshotDirectory(String snapshotDirectory) {
		this.snapshotDirectory = snapshotDirectory;
		theView.getParams().getOcelotlSettings().setSnapShotDirectory(this.snapshotDirectory);
	}

	/**
	 * Call all the methods that create a snapshot of the current state of Ocelotl
	 */
	public void takeSnapShot() {
		// Create directory
		String currentDirPath = createDirectory();

		// Save the currently displayed diagram as an image
		snapShotDiagram(currentDirPath);
		// Save the currently displayed quality curves as an image
		snapShotQualityCurve(currentDirPath);
		// Save the the current parameters in a text file
		saveConfig(currentDirPath);
		
		// Create a symbolic link to the trace file
		// createSymLink(currentDirPath);
	}

	/**
	 * Create a png image of the diagram
	 * @param dirPath
	 */
	public void snapShotDiagram(String dirPath) {
		theView.getTimeLineView().createSnapshotFor(dirPath + "/diagram.png");
	}

	public void snapShotDiagramWithName(String dirPath) {
		theView.getTimeLineView().createSnapshotFor(dirPath);
		}
	
	/**
	 * Create a png image of the quality curve
	 * @param dirPath
	 */
	public void snapShotQualityCurve(String dirPath) {
		createSnapshotFor(theView.getQualityView().getRoot(), dirPath + "/curves.png");
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
		for (int i = 0; i < parameters.size() - 1; i++) {
			if (theView.getParams().getParameter() < parameters.get(i)) {
				if (i > 0)
					i = i - 1;

				gain = qualities.get(i).getGain();
				loss = qualities.get(i).getLoss();
				break;
			}
		}

        String dbDir = Configuration.getInstance().get(SoCTraceProperty.sqlite_db_directory);
		dbDir = dbDir + theView.getParams().getTrace().getDbName();
		
		StringBuffer output = new StringBuffer();
		output.append("Trace name: ");
		output.append(theView.getParams().getTrace().getAlias());
		output.append("\nTrace path: ");
		output.append(dbDir);
		output.append("\nNumber of slices: ");
		output.append(theView.getParams().getTimeSlicesNumber());
		output.append("\nStart timestamp: ");
		output.append(theView.getParams().getTimeRegion().getTimeStampStart());
		output.append("\nEnd timestamp: ");
		output.append(theView.getParams().getTimeRegion().getTimeStampEnd());
		output.append("\nTime Operator: ");
		output.append(theView.getParams().getTimeAggOperator());
		output.append("\nSpace Operator: ");
		output.append(theView.getParams().getSpaceAggOperator());
		output.append("\nParameter: ");
		output.append(theView.getParams().getParameter());
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

			// Create the general snaphot directory
			if (!dir.mkdirs()) {
				logger.error("Failed to create cache directory: " + snapshotDirectory + ".");
			}
		}

		Date aDate = new Date(System.currentTimeMillis());
		dirName = snapshotDirectory + "/" + theView.getParams().getTrace().getAlias() + "_" + aDate.toString();

		// Create the specific snaphot directory
		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Failed to create cache directory: " + dirName + ".");
		}

		return dirName;
	}
	
	/**
	 * Create a symbolic link to the trace file
	 * @param aDirPath
	 */
		public void createSymLink(String aDirPath) {
		// TODO create symbolic link
		Process p;
	
		String dbDir = Configuration.getInstance().get(SoCTraceProperty.sqlite_db_directory);
		dbDir = dbDir + theView.getParams().getTrace().getDbName();
		
		String commandDirPath = aDirPath.replace(" ", "\\" + " ");
		
		try {
			p = Runtime.getRuntime().exec("ln -s " + dbDir + " " + commandDirPath + "/A\\ Link\\ to\\ the\\ trace");
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

}

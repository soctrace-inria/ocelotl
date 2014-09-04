package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

public class Snapshot {

	private static final Logger	logger	= LoggerFactory.getLogger(Snapshot.class);

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

	public void takeSnapShot() {
		// Create directory
		String currentDirPath = createDirectory();

		snapShotDiagram(currentDirPath);
		snapShotQualityCurve(currentDirPath);
		saveConfig(currentDirPath);
		//createSymLink(currentDirPath);
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
		theView.getQualityView().createSnapshotFor(dirPath + "/curves.png");
	}

	/**
	 * Save the actual configuration
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
		if (!dir.exists()) {
			logger.debug("Snapshot directory (" + snapshotDirectory + ") does not exist and will be created now.");

			// Create the directory
			if (!dir.mkdirs()) {
				logger.error("Failed to create cache directory: " + snapshotDirectory + ".");
			}
		}

		Date aDate = new Date(System.currentTimeMillis());
		dirName = snapshotDirectory + "/" + theView.getParams().getTrace().getAlias() + "_" + aDate.toString();

		dir = new File(dirName);
		if (!dir.mkdirs()) {
			logger.error("Failed to create cache directory: " + dirName + ".");
		}

		return dirName;
	}
	
	/**
	 * Create a symbolic link to the trace
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

}

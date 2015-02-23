package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Testbench class used for the benchmarking of the query and their optimization
 * 
 */
public class TestBench5 extends TestBench {

	public final int	TraceNamePos			= 0;
	public final int	TraceIDPos				= 1;
	public final int	CacheActivatedPos		= 2;
	public final int	NumberOfTimeSlicePos	= 4;
	public final int	StartTimestampPos		= 5;
	public final int	EndTimeStampPos			= 6;
	public final int	TimeAggregatorPos		= 3;
	public final int	DataAggregatorPos		= 3;
	public final int	NumberOfRepetetionPos	= 8;
	public final int	ParameterPos			= 9;
	public final int	testbenchHeaderSize		= 10;

	public TestBench5(String aFilePath, OcelotlView aView) {
		super(aFilePath, aView);
	}

	// Header: "TRACE; CACHE_USED; TS; MICROMODEL_TIME; QUALITY_TIME;
	// DICHO_TIME; PART_DISPLAY_TIME; CACHE_SIZE

	@Override
	public void parseFile() {
		File aFile = new File(aConfFile);

		if (aFile.canRead() && aFile.isFile()) {
			BufferedReader bufFileReader;
			try {
				bufFileReader = new BufferedReader(new FileReader(aFile));

				String line;
				// Get header
				line = bufFileReader.readLine();

				while ((line = bufFileReader.readLine()) != null) {
					if (line.isEmpty() || line.length() < testbenchHeaderSize)
						continue;

					String[] header = line.split(OcelotlConstants.CSVDelimiter);
					TestParameters params = new TestParameters();

					// Name
					params.setTraceName(header[TraceNamePos]);
					// Database unique ID
					params.setTraceID(Integer.parseInt(header[TraceIDPos]));
					// Cache activation
					params.setActivateCache(Boolean.parseBoolean(header[CacheActivatedPos]));
					// Time Aggregation Operator
					params.setMicroModelType("States");
					// Time Aggregation Operator
					params.setDataAggOperator(header[DataAggregatorPos]);
					// Number of time Slices
					params.getTimeSlicesNumber().add(Integer.parseInt(header[NumberOfTimeSlicePos]));

					if (header.length > 5) {
						for (int i = 5; i < header.length; i++)
							params.getTimeSlicesNumber().add(Integer.parseInt(header[i]));
					}

					testParams.add(params);
				}

				bufFileReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void launchTest() {

		if (!testParams.isEmpty()) {
			statData = "TRACE; CACHE_USED; AGG OPERATOR; TS; MICROMODEL_TIME; QUALITY_TIME; DICHO_TIME; PART_DISPLAY_TIME; CACHE_SIZE\n";
			String fileDir = aConfFile.substring(0, aConfFile.lastIndexOf("/") + 1);
			Date aDate = new Date(System.currentTimeMillis());
			String dirName = testParams.get(0).getTraceName() + "_" + aDate.toString();
			dirName = dirName.replace(" ", "_");
			testDirectory = fileDir + dirName;
			File dir = new File(testDirectory);
			dir.mkdirs();

			for (TestParameters aTest : testParams) {
				aTest.setDirectory(dir.getAbsolutePath());
				noCacheTime = 0;
				cacheTime = 0;
				
				// Fill the other parameters
				Trace theTrace = null;
				for (Trace aTrace : theView.getConfDataLoader().getTraces()) {
					if (aTrace.getId() == aTest.getTraceID())
						theTrace = aTrace;
				}

				aTest.setStartTimestamp(theTrace.getMinTimestamp());
				aTest.setEndTimestamp(theTrace.getMaxTimestamp());
				aTest.getParameters().add(1.0);
				aTest.setVisuOperator("null");
				aTest.setDatacacheStrat(DatacacheStrategy.DATACACHE_PROPORTIONAL);

				for (int i = 0; i < aTest.getTimeSlicesNumber().size(); i++) {
					aTest.setNbTimeSlice(aTest.getTimeSlicesNumber().get(i));
					theView.loadFromParam(aTest, aTest.isActivateCache());
					statData = statData + getStatData();
					writeStat();
				}
			}

			// Call the script to compare the image
			try {
				Process p = new ProcessBuilder("/home/youenn/projects/testBenchOcelotl/compare.sh", testDirectory).start();
				p.waitFor();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * Write the stat data for the current test
	 */
	public String getStatData() {
		String stat = "";
		BufferedReader bufFileReader;
		String line;
		boolean usedCache = false;

		int microscopicModel = 0;
		int computeQualities = 0;
		int computeDicho = 0;
		int computePartAndDisplay = 0;

		try {
			bufFileReader = new BufferedReader(new FileReader("/home/youenn/traces/eclipse_output.txt"));

			while ((line = bufFileReader.readLine()) != null) {
				if (line.isEmpty())
					continue;

				if (line.contains("[DATACACHE] Found ")) {
					usedCache = true;
				}
				if (line.contains("[Total Time for Microscopic Rebuilding]")) {
					String computation = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					microscopicModel = Integer.valueOf(computation);
				}
				if (line.contains("[Compute qualities]")) {
					String computation = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					computeQualities = Integer.valueOf(computation);
				}
				if (line.contains("[Compute Dichotomy]")) {
					String computation = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					computeDicho = Integer.valueOf(computation);
				}
				if (line.contains("[Compute parts and display]")) {
					String computation = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					computePartAndDisplay = Integer.valueOf(computation);
				}
			}
			
			// TRACE; CACHE_USED; AGG OPERATOR; TS; MICROMODEL_TIME; QUALITY_TIME; DICHO_TIME;
			// PART_DISPLAY_TIME;
			stat = theView.aTestTrace.getAlias() + ";" + usedCache + ";" +  theView.getOcelotlParameters().getDataAggOperator() + ";" + theView.getOcelotlParameters().getTimeSlicesNumber() + ";" + microscopicModel + ";" + computeQualities + ";" + computeDicho + "; " + computePartAndDisplay + "; " + "\n";

			bufFileReader.close();

			// Delete the output file
			PrintWriter writer = new PrintWriter(new File("/home/youenn/traces/eclipse_output.txt"));
			writer.print("");
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return stat;
	}
}
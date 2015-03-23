package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Testbench class used for the benchmarking of the new query system (filtering
 * done in java instead of in DB)
 */
public class TestBench6 extends TestBench {

	public final int	TraceNamePos			= 0;
	public final int	TraceIDPos				= 1;
	public final int	TimeAggregatorPos		= 2;
	public final int	StartTimestampPos		= 3;
	public final int	EndTimeStampPos			= 4;
	public final int	numberOfThreadPos		= 5;
	public final int	eventPerthreadPos		= 6;
	public final int	filteredEventTypePos	= 7;
	public final int	filteredEventProdPos	= 8;
	
	public final int	CacheActivatedPos		= 2;
	public final int	NumberOfTimeSlicePos	= 4;
	public final int	DataAggregatorPos		= 3;
	public final int	NumberOfRepetetionPos	= 8;
	public final int	ParameterPos			= 9;
	public final int	testbenchHeaderSize		= 10;

	public TestBench6(String aFilePath, OcelotlView aView) {
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
					params.setActivateCache(false);
					// Time Aggregation Operator
					params.setMicroModelType(header[TimeAggregatorPos]);
					// Data Aggregation Operator
					params.setDataAggOperator("Temporal Aggregation");
					params.setStartTimestamp(Long.parseLong(header[StartTimestampPos]));
					params.setEndTimestamp(Long.parseLong(header[EndTimeStampPos]));
					params.setNumberOfThreads(Integer.parseInt(header[numberOfThreadPos]));
					params.setEventPerThread(Integer.parseInt(header[eventPerthreadPos]));
					String[] fET = header[filteredEventTypePos].split(",");
					if(fET.length > 1 || (fET.length > 0 && !fET[0].equals(" ")))
						params.setFilteredEventType(Arrays.asList(fET));
					
					String[] fEP = header[filteredEventProdPos].split(",");
					if(fEP.length > 1 || (fEP.length > 0 && !fEP[0].equals(" ")))
						params.setFilteredEventProducer(Arrays.asList(fEP));
					
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
			statData = "TRACE; QUERY_TYPE; NB_THREAD; NB_EVENT_PER_THREAD; TS_START; TS_END; Filtered ET; filtered EP; MICROMODEL_TIME\n";
			String fileDir = aConfFile.substring(0, aConfFile.lastIndexOf("/") + 1);
			Date aDate = new Date(System.currentTimeMillis());
			String dirName = testParams.get(0).getTraceName() + "_" + aDate.toString();
			dirName = dirName.replace(" ", "_");
			testDirectory = fileDir + dirName;
			File dir = new File(testDirectory);
			dir.mkdirs();

			for (TestParameters aTest : testParams) {
				aTest.setDirectory(dir.getAbsolutePath());
				aTest.getParameters().add(1.0);
				aTest.setVisuOperator("null");
				aTest.setDatacacheStrat(DatacacheStrategy.DATACACHE_PROPORTIONAL);
				aTest.setNbTimeSlice(100);
				
				theView.loadFromParam(aTest, false);
				
				statData = statData + getStatData(aTest);
				writeStat();
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
	public String getStatData(TestParameters aTest) {
		String stat = "";
		BufferedReader bufFileReader;
		String line;

		int microscopicModel = -1;

		try {
			bufFileReader = new BufferedReader(new FileReader("/home/youenn/traces/eclipse_output.txt"));

			while ((line = bufFileReader.readLine()) != null) {
				if (line.isEmpty())
					continue;

				if (line.contains("[Total Time for Microscopic Rebuilding]")) {
					String computation = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					microscopicModel = Integer.valueOf(computation);
				}
			}
			
			// TRACE; QUERY_TYPE; NB_THREAD; NB_EVENT_PER_THREAD; TS_START; TS_END; Filtered ET; filtered EP; MICROMODEL_TIME
			stat = theView.aTestTrace.getAlias() + ";" + theView.getOcelotlParameters().getMicroModelType() + ";" + theView.getOcelotlParameters().getNumberOfThread() + ";" + theView.getOcelotlParameters().getEventsPerThread() + ";"
					+ theView.getTimeRegion().getTimeStampStart() + ";" + theView.getTimeRegion().getTimeStampEnd() + ";" 
					+ aTest.getFilteredEventType().toString() + ";" + aTest.getFilteredEventProducer().toString() + ";"
					+ microscopicModel + "\n";
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
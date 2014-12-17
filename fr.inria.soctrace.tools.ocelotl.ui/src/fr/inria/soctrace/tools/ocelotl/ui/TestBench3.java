package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.DatacacheStrategy;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Let me zoom zoom zen in my bench bench bench
 * 
 * bench file format: //trace;traceID //cache strategy; TimeSlice; Time stamps;
 * Operator; parameter(s)
 */
public class TestBench3 extends TestBench {

	List<TestParameters>	testParams	= new ArrayList<TestParameters>();
	String					testDirectory;
	String					statData;
	int						noCacheTime;
	int						cacheTime;

	public TestBench3(String aFilePath, OcelotlView aView) {
		super(aFilePath, aView);
	}

	@Override
	public void parseFile() {
		File aFile = new File(aConfFile);
		List<TestParameters>	tmpParams	= new ArrayList<TestParameters>();

		if (aFile.canRead() && aFile.isFile()) {
			BufferedReader bufFileReader;

			try {
				bufFileReader = new BufferedReader(new FileReader(aFile));

				String line;
				int traceID = -1;
				String traceName = "";

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
					// rebuilding strategy
					params.setDatacacheStrat(DatacacheStrategy.valueOf(header[CacheStrategyPos]));
					// Number of time Slices
					params.setNbTimeSlice(Integer.parseInt(header[NumberOfTimeSlicePos]));
					// Start timestamp
					params.setStartTimestamp(Long.parseLong(header[StartTimestampPos]));
					// End timestamp
					params.setEndTimestamp(Long.parseLong(header[EndTimeStampPos]));
					// Time Aggregation Operator
					params.setTimeAggOperator(header[TimeAggregatorPos]);
					// Number of repetitions
					params.setNumberOfRepetition(Integer.parseInt(header[NumberOfRepetetionPos]));

					// Parameter value
					params.getParameters().add(Double.parseDouble(header[ParameterPos]));

					if (header.length > testbenchHeaderSize) {
						for (int i = testbenchHeaderSize; i < header.length; i++)
							params.getTimeSlicesCollection().add(Integer.parseInt(header[i]));
					}

					tmpParams.add(params);
				}
				
				bufFileReader.close();
				
				for (TestParameters aParam : tmpParams) {
					// Duplicate test for all traces
					for (Trace aTrace : theView.getConfDataLoader().getTraces()) {
						TestParameters params = new TestParameters();
						params.setTraceID(aTrace.getId());
						params.setTraceName(aTrace.getAlias());
						
						params.setActivateCache(aParam.isActivateCache());
						// rebuilding strategy
						params.setDatacacheStrat(aParam.getDatacacheStrat());
						// Number of time Slices
						params.setNbTimeSlice(aParam.getNbTimeSlice());
						// Start timestamp
						params.setStartTimestamp(aTrace.getMinTimestamp());
						// End timestamp
						params.setEndTimestamp(aTrace.getMaxTimestamp());
						// Time Aggregation Operator
						params.setTimeAggOperator(aParam.getTimeAggOperator());
						// Number of repetitions
						params.setNumberOfRepetition(aParam.getNumberOfRepetition());
						// Parameter value
						params.getParameters().add(0.0);

						params.setTimeSlicesCollection(aParam.getTimeSlicesCollection());

						testParams.add(params);
					}
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void launchTest() {

		if (!testParams.isEmpty()) {
			statData = "";
			String fileDir = aConfFile.substring(0, aConfFile.lastIndexOf("/") + 1);
			Date aDate = new Date(System.currentTimeMillis());
			String dirName = testParams.get(0).getTraceName() + "_" + aDate.toString();
			dirName = dirName.replace(" ", "_");
			testDirectory = fileDir + dirName;
			File dir = new File(testDirectory);
			dir.mkdirs();

			int j;
			for (TestParameters aTest : testParams) {
				aTest.setDirectory(dir.getAbsolutePath());
				
				statData = statData + aTest.toString().replace("_", ";") + "\n";
				statData = statData + "Number of events; Number of Producer; number of Time Slices; Microscopic Model; Compute Qualities;Compute Dichotomies; Compute best cuts + display\n";
				
				for (int i = 0; i < aTest.getTimeSlicesCollection().size(); i++) {
					// for (int i = 660; i <= 1000; i = i +20) {
					aTest.setNbTimeSlice(aTest.getTimeSlicesCollection().get(i));
					//for (j = 16; j <= 583; j = j + 2) {
					//	aTest.setNbEventProd(j);
					//	 for ( j = 0; j < 10;
					//	 j++) {
						theView.loadFromParam(aTest, false);
						statData = statData + getStatData(aTest);
						writeStat();
						
						theView.loadFromParam(aTest, true);
						statData = statData + getStatData(aTest);
						writeStat();
					//	 }
					//}
				}

				statData = statData + "\n";
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

	public String getStatData(TestParameters aTest) {
		String stat = "";
		BufferedReader bufFileReader;
		String line;
		int saveMatrixTime = 0;
		int loadMatrixTime = 0;
		int loadDirtyMatrixTime = 0;
		double dirtyTS = 0;
		double usedTS = 0;
		double ratio = 0.0;
		int computationTime = 0;

		int microscopicModel = 0;
		int computeQualities = 0;
		int computeDicho = 0;
		int computePartAndDisplay = 0;

		try {
			bufFileReader = new BufferedReader(new FileReader("/home/youenn/traces/eclipse_output.txt"));

			while ((line = bufFileReader.readLine()) != null) {
				if (line.isEmpty())
					continue;

				if (line.contains("[DATACACHE - Save the matrix to cache]")) {
					String saveMatrix = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					saveMatrixTime = Integer.valueOf(saveMatrix);
				}
				if (line.contains("[Load matrix from cache]")) {
					String loadMatrix = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					loadMatrixTime = Integer.valueOf(loadMatrix);
					cacheTime = loadMatrixTime;
				}
				if (line.contains("[Load matrix from cache (dirty)]")) {
					String loadMatrix = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					loadDirtyMatrixTime = Integer.valueOf(loadMatrix);
					cacheTime = loadDirtyMatrixTime;
				}
				if (line.contains("[DATACACHE] Found ")) {
					String dirtyTimeslicesNumber = line.substring(line.indexOf("Found ") + 6, line.indexOf(" dirty Timeslices"));
					String usedCachedTimeSlices = line.substring(line.indexOf("among ") + 6, line.indexOf(" used cache"));
					String computedDirtyRatio = line.substring(line.indexOf("ratio of") + 8, line.indexOf(")."));

					dirtyTS = Double.valueOf(dirtyTimeslicesNumber);
					usedTS = Double.valueOf(usedCachedTimeSlices);
					ratio = Double.valueOf(computedDirtyRatio);
				}
				if (line.contains("[TOTAL (QUERIES + COMPUTATION)")) {
					String computation = line.substring(line.indexOf("Delta: ") + 7, line.indexOf(" ms"));
					computationTime = Integer.valueOf(computation);
					noCacheTime = computationTime;
				}
				if (line.contains("[Microscopic Rebuilding]")) {
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

			stat = theView.aTestTrace.getNumberOfEvents() + ";" + aTest.getNbTimeSlice() + ";" + aTest.getNbEventProd() + ";" + microscopicModel + ";" + computeQualities + ";" + computeDicho + ";" +  computePartAndDisplay+ "\n";
			//saveMatrixTime + ";" + loadMatrixTime + ";" + loadDirtyMatrixTime + ";" + dirtyTS + ";" + usedTS + ";" + ratio + ";" + computationTime + "\n";

			bufFileReader.close();

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

	@Override
	public void writeStat() {
		PrintWriter writer;
		try {
			writer = new PrintWriter(testDirectory + "/result.csv", "UTF-8");

			writer.print(statData);

			// Close the fd
			writer.flush();
			writer.close();

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

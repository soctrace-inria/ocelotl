package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Let me zoom zoom zen in my bench bench bench
 * //trace
 * 
 * //cache activated ?; TimeSlice; Time stamps; Operator; parameter 
 */
public class TestBench {

	String aConfFile;
	OcelotlView theView;
	List<TestParameters> testParams = new ArrayList<TestParameters>();
	String testDirectory;
	String statData;
	int noCacheTime;
	int cacheTime;
	
	public TestBench(String aFilePath, OcelotlView aView)
	{
		aConfFile = aFilePath;
		theView = aView;
		
		String fileDir = aFilePath.substring(0, aFilePath.lastIndexOf("/") + 1);
		testDirectory = fileDir + System.currentTimeMillis();
		File dir = new File(testDirectory);
		dir.mkdirs();
	}
	
	public void parseFile()
	{
		File aFile = new File(aConfFile);
		
		if (aFile.canRead() && aFile.isFile()) {
			BufferedReader bufFileReader;

			try {
				bufFileReader = new BufferedReader(new FileReader(aFile));

				String line;
				int traceID = -1;
				String traceName = "";
				
				// Get header
				line = bufFileReader.readLine();
				if (line != null) {
					String[] header = line.split(OcelotlConstants.CSVDelimiter);

					// Name
					traceName = header[0];
					// Database unique ID
					traceID = Integer.parseInt(header[1]);
				}
				
				while ((line = bufFileReader.readLine()) != null) {
					if (line.isEmpty())
						continue;

					String[] header = line.split(OcelotlConstants.CSVDelimiter);
					TestParameters params = new TestParameters();

					// Name
					params.setTraceName(traceName);
					// Database unique ID
					params.setTraceID(traceID);
					// Number of time Slices
					params.setNbTimeSlice(Integer.parseInt(header[1]));
					// Start timestamp
					params.setStartTimestamp(Long.parseLong(header[2]));
					// End timestamp
					params.setEndTimestamp(Long.parseLong(header[3]));
					// Time Aggregation Operator
					params.setTimeAggOperator(header[4]);
					// Parameter value
					params.setParameter(Double.parseDouble(header[5]));

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
		statData = "";
		
		for (TestParameters aTest : testParams) {
			noCacheTime = 0;
			cacheTime = 0;
			theView.loadFromParam(aTest, false);
			String spaceLess = aTest.toString().replace(" ", "_");
			statData = statData + aTest.toString().replace("_", ";") + "\n";
			statData = statData + "Save matrix; Load matrix; Load matrix (dirty); dirt Timeslice; used TS; ratio; queries+computation; speedup\n";
			theView.snapShotDiagram(testDirectory + "/" + spaceLess + ".png");
			statData = statData + getStatData();
			
			theView.loadFromParam(aTest, true);
			theView.snapShotDiagram(testDirectory + "/" + spaceLess + "_noCache.png");
			statData = statData + getStatData();
		}

		writeStat();
		
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
	
	public String getStatData()
	{
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
		
		try {
	
			bufFileReader = new BufferedReader(new FileReader("/home/youenn/traces/eclipse_output.txt"));
			
			while ((line = bufFileReader.readLine()) != null) {
				if (line.isEmpty())
					continue;

				if(line.contains("[DATACACHE - Save the matrix to cache]"))
				{
					String saveMatrix = line.substring(line.indexOf("Delta: ")+ 7, line.indexOf(" ms"));
					saveMatrixTime = Integer.valueOf(saveMatrix);
				}
				if(line.contains("[Load matrix from cache]"))
				{
					String loadMatrix = line.substring(line.indexOf("Delta: ")+ 7, line.indexOf(" ms"));
					loadMatrixTime = Integer.valueOf(loadMatrix);
					cacheTime = loadMatrixTime;
				}
				if(line.contains("[Load matrix from cache (dirty)]"))
				{
					String loadMatrix = line.substring(line.indexOf("Delta: ")+ 7, line.indexOf(" ms"));
					loadDirtyMatrixTime = Integer.valueOf(loadMatrix);
					cacheTime = loadDirtyMatrixTime;
				}
				if(line.contains("[DATACACHE] Found "))
				{
					String dirtyTimeslicesNumber = line.substring(line.indexOf("Found ")+ 6, line.indexOf(" dirty Timeslices"));
					String usedCachedTimeSlices = line.substring(line.indexOf("among ")+ 6, line.indexOf(" used cache"));
					String computedDirtyRatio = line.substring(line.indexOf("ratio of")+ 8, line.indexOf(")."));
					
					dirtyTS = Double.valueOf(dirtyTimeslicesNumber);
					usedTS = Double.valueOf(usedCachedTimeSlices);
					ratio = Double.valueOf(computedDirtyRatio);
				}
				
				if(line.contains("[TOTAL (QUERIES + COMPUTATION)"))
				{
					String computation = line.substring(line.indexOf("Delta: ")+ 7, line.indexOf(" ms"));
					computationTime = Integer.valueOf(computation);
					noCacheTime = computationTime;
				}
			}
			
			if (noCacheTime > 0 && cacheTime > 0) {
				stat = saveMatrixTime + ";" + loadMatrixTime + ";" + loadDirtyMatrixTime + ";" + dirtyTS + ";" + usedTS + ";" + ratio + ";" + computationTime + ";" + ((double) (noCacheTime)) / ((double) cacheTime) + "\n";

			} else {
				stat = saveMatrixTime + ";" + loadMatrixTime + ";" + loadDirtyMatrixTime + ";" + dirtyTS + ";" + usedTS + ";" + ratio + ";" + computationTime + "\n";
			}
			
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
	
	public void writeStat()
	{
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

package fr.inria.soctrace.tools.ocelotl.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;

import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants;
import fr.inria.soctrace.tools.ocelotl.ui.views.OcelotlView;

/**
 * Let me zoom zoom zen in my bench bench bench
 * //trace
 * 
 * //cache activated ?; TimeSlice; Time stamps; Operator; parameter 
 * 
 */
public class TestBench {

	String aConfFile;
	OcelotlView theView;
	List<TestParameters> testParams = new ArrayList<TestParameters>();
	
	public TestBench(String aFilePath, OcelotlView aView)
	{
		aConfFile = aFilePath;
		theView = aView;
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
		for (TestParameters aTest : testParams) {
			theView.loadFromParam(aTest);
		}
	}

}

package fr.inria.soctrace.tools.ocelotl.core.parameters;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

public class UnitManager {

	static String getUnit(OcelotlParameters parameters, String string){
		String unit = "";
		String[] splitUnit = string.split(" ");
		for (int i = 0; i < splitUnit.length; i++) {
			String word = "";
			if (splitUnit[i].startsWith("%")){
			switch (splitUnit[i]) {
			case "%TIME":
				// Get the time unit of the trace
				String timeUnit = TimeUnit.getLabel(parameters.getTrace().getTimeUnit());
				word = timeUnit;
				break;
			case "%NONE":
			case "%UNKNOWN":
			default:
				break;
			}			}
			else{
				word = splitUnit[i];
			}
			unit = unit + " " + word;
			}
		if (unit.equals(" "))
			unit="";
		return unit;
		}
	
	
}

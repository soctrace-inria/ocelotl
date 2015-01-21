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
package fr.inria.soctrace.tools.ocelotl.core;

import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.lpaggreg.quality.DLPQuality;
import fr.inria.soctrace.tools.ocelotl.core.constants.OcelotlConstants.ParameterPPolicy;
import fr.inria.soctrace.tools.ocelotl.core.dataaggregmanager.IDataAggregManager;

public class ParameterStrategy {

	public static HashMap<ParameterPPolicy, String> availableStrategies;
	
	public ParameterStrategy() {
		availableStrategies = new HashMap<ParameterPPolicy, String>();
		availableStrategies.put(ParameterPPolicy.PARAMETERP_STRATEGY_ONE,
				"Always One");
		availableStrategies.put(ParameterPPolicy.PARAMETERP_STRATEGY_ZERO,
				"Always Zero");
		availableStrategies.put(
				ParameterPPolicy.PARAMETERP_STRATEGY_LARGEST_DIFF,
				"Largest Gap in Loss or Gain");
		availableStrategies.put(
				ParameterPPolicy.PARAMETERP_STRATEGY_LARGEST_SUM_DIFF,
				"Largest Gap between Loss and Gain (After)");
		availableStrategies.put(
				ParameterPPolicy.PARAMETERP_STRATEGY_LARGEST_SUM_DIFF2,
				"Largest Gap between Loss and Gain (Before)");
	}

	/**
	 * Return the initial value of the parameter p according to the selected
	 * strategy
	 * 
	 * @param aDataAggregManager
	 *            the data aggregation manager used to find the parameter
	 * @param aPolicy
	 *            the policy to chose the value of the parameter p
	 * @return the found value of parameter p
	 */
	public double computeInitialParameter(
			IDataAggregManager aDataAggregManager, ParameterPPolicy aPolicy) {
		switch (aPolicy) {
		case PARAMETERP_STRATEGY_ONE:
			return 1.0;
		case PARAMETERP_STRATEGY_ZERO:
			return 0.0;
		case PARAMETERP_STRATEGY_LARGEST_SUM_DIFF:
			return largestDiffOftheSum(aDataAggregManager);
		case PARAMETERP_STRATEGY_LARGEST_SUM_DIFF2:
			return largestDiffOftheSum2(aDataAggregManager);
		case PARAMETERP_STRATEGY_LARGEST_DIFF:
			return largestDiff(aDataAggregManager);
		default:
			return 1.0;
		}
	}

	/**
	 * Search for the parameter that has the largest gap (sum of the differences
	 * in gain and loss values) between two consecutive gain and loss values
	 * 
	 * @param aDataAggregManager
	 *            the data aggregation manager used to find the parameter
	 * 
	 * @return the corresponding parameter value, or 1.0 as default
	 */
	public double largestDiffOftheSum(IDataAggregManager aDataAggregManager) {
		double diffG = 0.0, diffL = 0.0;
		double sumDiff = 0.0;
		double maxDiff = 0.0;
		int indexMaxQual = -1;
		int i;
		ArrayList<DLPQuality> qual = (ArrayList<DLPQuality>) aDataAggregManager
				.getQualities();
		for (i = 1; i < qual.size(); i++) {
			// Compute the difference for the gain and the loss
			diffG = Math.abs(qual.get(i - 1).getGain() - qual.get(i).getGain());
			diffL = Math.abs(qual.get(i - 1).getLoss() - qual.get(i).getLoss());

			// Compute the diff between both
			sumDiff = Math.abs(diffG - diffL);

			if (sumDiff > maxDiff) {
				maxDiff = sumDiff;
				indexMaxQual = i - 1;
			}
		}
		return aDataAggregManager.getParameters().get(indexMaxQual + 1);
	}

	/**
	 * Find the largest difference, whether gain or loss
	 * 
	 * @param aDataAggregManager
	 *            the data aggregation manager used to find the parameter
	 * @return the corresponding parameter value, or 1.0 as default
	 */
	public double largestDiff(IDataAggregManager aDataAggregManager) {
		double diffG = 0.0, diffL = 0.0;
		double maxDiff = 0.0;
		int indexMaxQual = -1;
		int i;
		ArrayList<DLPQuality> qual = (ArrayList<DLPQuality>) aDataAggregManager
				.getQualities();
		for (i = 1; i < qual.size(); i++) {
			// Compute the difference for the gain and the loss
			diffG = Math.abs(qual.get(i - 1).getGain() - qual.get(i).getGain());
			diffL = Math.abs(qual.get(i - 1).getLoss() - qual.get(i).getLoss());

			if (diffG > maxDiff) {
				maxDiff = diffG;
				indexMaxQual = i - 1;
			}

			if (diffL > maxDiff) {
				maxDiff = diffL;
				indexMaxQual = i - 1;
			}
		}
		return aDataAggregManager.getParameters().get(indexMaxQual + 1);
	}
	
	public double largestDiffOftheSum2(IDataAggregManager aDataAggregManager) {
		double diffG = 0.0, diffL = 0.0;
		double sumDiff = 0.0;
		double maxDiff = 0.0;
		ArrayList<DLPQuality> qual = (ArrayList<DLPQuality>) aDataAggregManager.getQualities();
		int indexMaxQual = qual.size()-1;
		int i;
		for (i = 0; i < qual.size()-1; i++) {
			// Compute the difference for the gain and the loss
			diffG = Math.abs(qual.get(i + 1).getGain() - qual.get(i).getGain());
			diffL = Math.abs(qual.get(i + 1).getLoss() - qual.get(i).getLoss());

			// Compute sum of both
			sumDiff = Math.abs(diffG - diffL);

			if (sumDiff > maxDiff) {
				maxDiff = sumDiff;
				indexMaxQual = i;
			}
		}
			return aDataAggregManager.getParameters().get(indexMaxQual);
	}
	
	public ParameterPPolicy getStrategy(String aStrategyName) {
		for (ParameterPPolicy aPolicy : availableStrategies.keySet()) {
			if (availableStrategies.get(aPolicy).equals(aStrategyName))
				return aPolicy;
		}

		return ParameterPPolicy.PARAMETERP_STRATEGY_ONE;
	}

}

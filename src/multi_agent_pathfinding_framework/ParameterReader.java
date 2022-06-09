/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.nio.file.Path;
import java.nio.file.Paths;

/* This class simply reads the arguments given to the main function and tries
 * to interpret them as parameter values.
 * Make sure the order of the parameters given match the expectations of
 * the readParameters() function.
 */
public class ParameterReader {

	/* We expect the following parameters:
	 * 0: algorithm
	 * 1: input path
	 * 2: output path
	 * 3: time horizon (maximum plan length)
	 * 4: runtime limit
	 * 5: trial limit (number of algorithm runs allowed)
	 * 6: direction change frequency (number of time steps after which the
	 *    direction of edges change. 0 in case of a static graph)
	 */
	public Parameters readParameters(String[] args) {
		
		if (args.length != 7) {
			
			throw new java.lang.IllegalArgumentException(
					"Incorrect number of arguments.\n" +
					"    * 0: algorithm "
					+ "(CA_STAR | TokenPassing |"
					+ " HierarchicalPlanner | EnhancedHierarchicalPlanner |"
					+ " RuntimeReplanner | AlternatingRuntimeReplanner)\n" + 
					"    * 1: input path \n" + 
					"    * 2: output path \n" +  
					"    * 3: time horizon (int: maximum plan length) \n" +
					"    * 4: runtime limit (int: seconds) \n" +
					"    * 5: trial limit "
					+ "(int: number of algorithm runs allowed)\n" + 
					"    * 6: direction change frequency "
					+ "(int: frequency with which edge directions change)");
		}
		
		/* This may throw an IllegalArgumentException if "algorithm" doesn't
		 * match "CA_STAR" or any of the other supported algorithms specified
		 * in MAPFAlgorithm. */
		MAPFAlgorithm algorithm = MAPFAlgorithm.valueOf(args[0]);
		
		/* Those paths don't necessarily exist in the file system. */
		Path inputPath = Paths.get(args[1]);
		Path outputPath = Paths.get(args[2]);
		
		
		/* Time horizon. */
		int timeHorizon = 0;
		
		try {
			timeHorizon = Integer.parseInt(args[3]);
		}
		catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Time Horizon argument (4th"
					+ " argument) has to be a positive integer describing how"
					+ " long plans are allowed to get.");
		}
		
		if (timeHorizon < 1) {
			throw new IllegalArgumentException("Time Horizon argument (4th"
					+ " argument) has to be a positive integer describing how"
					+ " long plans are allowed to get.");
		}
		
		/* Runtime limit. */
		int runtimeLimit = 0;
		
		try {
			runtimeLimit = Integer.parseInt(args[4]);
		}
		catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Runtime limit argument (5th"
					+ " argument) has to be a positive integer describing how"
					+ " many seconds the planning process may take.");
		}
		
		if (runtimeLimit < 1) {
			throw new IllegalArgumentException("Runtime limit argument (5th"
					+ " argument) has to be a positive integer describing how"
					+ " many seconds the planning process may take.");
		}
		
		
		/* Trial limit. */
		int trialLimit = 0;
		
		try {
			trialLimit = Integer.parseInt(args[5]);
		}
		catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Trial limit argument (6th"
					+ " argument) has to be a positive integer describing how"
					+ " many times the algorithm may run.");
		}
		
		if (trialLimit < 1) {
			throw new IllegalArgumentException("Trial limit argument (6th"
					+ " argument) has to be a positive integer describing how"
					+ " many times the algorithm may run.");
		}
		
		
		/* Direction Change Frequency */
		int directionChangeFrequency = 0;
		
		try {
			
			directionChangeFrequency = Integer.parseInt(args[6]);
		}
		catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Direction change frequency "
					+ "(7th argument) has to be a positive integer describing"
					+ " how long it takes for the edges in the map to change "
					+ "their direction. Use 0 in case of a static map.");
		}
		
		if (trialLimit < 0) {
			throw new IllegalArgumentException("Direction change frequency "
					+ "(7th argument) has to be a positive integer describing"
					+ " how long it takes for the edges in the map to change "
					+ "their direction. Use 0 in case of a static map.");
		}			
		
		return new Parameters(algorithm,
						      inputPath,
						      outputPath,
						      timeHorizon,
						      runtimeLimit,
						      trialLimit,
						      directionChangeFrequency);
	}
}

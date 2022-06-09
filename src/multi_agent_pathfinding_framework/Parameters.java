/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.nio.file.Path;

/* A Parameters object contains all the information handed as arguments to
 * the main function in a structured manner. */
public class Parameters {
	
	/* The multi-agent pathfinding algorithm that shall be used for the given
	 * scenario(s). */
	private final MAPFAlgorithm algorithm;
	
    /* Path to scenario input file or directory */
	private final Path inputPath;
	
	/* Path to scenario output directory */
	private final Path outputPath;
	
	/* Defines the maximum plan length.
	 * Once it's reached without finding a solution, the planning process
	 * can stop (and possibly start over). */
	private final int timeHorizon;
	
	/* Defines the maximum runtime in seconds.
	 * Once it's reached without finding a solution, the planning process
	 * is cancelled. */
	private final int runtimeLimit;
	
	/* Defines how often the chosen algorithm may run
	 * (if it's nondeterministic or different agent orders are possible like
	 * in the case of CA*). */
	private final int trialLimit;	
	
	/* In a dynamic graph, the directions of the edges change over time.
	 * This variable defines how many time steps it takes until the
	 * directions change. Use 0 for a static graph where edges are not
	 * supposed to change direction at all. */
	private final int directionChangeFrequency;
	
	
	public Parameters(MAPFAlgorithm algorithm,
			          Path inputPath,
			          Path outputPath,
			          int timeHorizon,
			          int runtimeLimit,
			          int trialLimit,
			          int directionChangeFrequency) {
		
		this.algorithm = algorithm;
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.timeHorizon = timeHorizon;
		this.runtimeLimit = runtimeLimit;
		this.trialLimit = trialLimit;
		this.directionChangeFrequency = directionChangeFrequency;
	}
	
	public MAPFAlgorithm algorithm() { return algorithm; }
	
	public Path inputPath() { return inputPath; }
	
	public Path outputPath() { return outputPath; }
	
	public int timeHorizon() { return timeHorizon; }
	
	public int runtimeLimit() { return runtimeLimit; }
	
	public int trialLimit() { return trialLimit; }
	
	public int directionChangeFrequency() { return directionChangeFrequency; }
}

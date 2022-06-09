/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* Names the algorithms supported by this framework. */
public enum MAPFAlgorithm {

	CA_STAR,
	TokenPassing,
	EnhancedHierarchicalPlanner,
	RuntimeReplanner,
	AlternatingRuntimeReplanner,
	TrafficSimulator;
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* Describes the format of a scenario file.
 * In classic scenarios, each agent has a start and a single goal position.
 * In dynamic scenarios, there's a number of tasks each with a variable 
 * number of positions that have to be reached consecutively. 
 * The assignment of tasks to agents is not given in dynamic scenarios. */
public enum YamlFileType {
	
	CLASSIC,
	DYNAMIC;
}

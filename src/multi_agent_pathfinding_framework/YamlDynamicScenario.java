/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashSet;

/* This class is used to load the content of a yaml file into memory in a
 * structured way. It allows tasks with multiple positions that have to be
 * reached and that are not fixed to a specific agent. 
 * 
 * The first line of the file has to be:
 * 
 * !!multi_agent_pathfinding_framework.YamlDynamicFile
 * 
 * The following content may look like:
 * 
 * agents:
 * -   name: agent0
 *     start: [10, 29]
 * -   name: agent1
 *     start: [27, 0]
 * -   name: agent2
 *     start: [23, 29]
 * tasks:
 * -   available: 0
 *     positions: [1, 4, 2, 5, 1, 8, 22, 13]
 * -   available: 7
 *     positions: [2, 5]
 * map:
 *     dimensions: [32, 32]
 *     obstacles:
 *     - [0, 0]
 *     - [6, 0]
 *     parkingSpots:
 *     - [0, 4]
 *     edges:
 *     - [0, 1, 0, 2]
 *     - [0, 1, 1, 1]
 *  
 *  Note that TABs are not allowed! Use regular spaces.
 *  The obstacles section is optional.
 */
public class YamlDynamicScenario {

	/* Contains a set of agents, each having a name and start position. */
	public YamlDynamicAgent[] agents;
	
	
	public HashSet<YamlDynamicTask> tasks;
	
	/* Contains three keys:
	 * 
	 * dimensions: native int array with x and y dimensions.
	 * 
	 * obstacles: HashSet of native int arrays with x and y coordinates of
	 *            obstacles.
	 * 
	 * edges: HashSet of native int arrays with x1, y1, x2 and y2 coordinates
	 *        of edges pointing from (x1, y1) to (x2, y2).
	 */
	public YamlDynamicMap map;
	
	public YamlDynamicScenario() {}
	
	public YamlDynamicAgent[] agents() { return agents; }
	
	public HashSet<YamlDynamicTask> tasks() { return tasks; }
	
	public YamlDynamicMap map() { return map; }
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* This class is used to load the content of a yaml file into memory in a
 * structured way. 
 * 
 * The first line of the file has to be:
 * 
 * !!multi_agent_pathfinding_framework.YamlClassicFile
 * 
 * The following content may look like:
 * 
 * agents:
 * -   goal: [52, 60]
 *     name: agent0
 *     start: [21, 20]
 * -   goal: [55, 60]
 *     name: agent1
 *     start: [42, 14]
 * map:
 *     dimensions: [64, 64]
 *     obstacles:
 *     - [4, 7]
 *     - [2, 6]
 *     - [8, 11]
 *     edges:
 *     - [1, 0, 2, 0]
 *     - [2, 0, 3, 0]
 *     - [3, 0, 4, 0]
 *     - [4, 0, 5, 0]
 *  
 *  Note that TABs are not allowed! Use regular spaces.
 *  The obstacles section is optional.
 */
public class YamlClassicScenario {

	/* Contains a set of agents, each having a name, start position and goal
	 * position. */
	public YamlClassicAgent[] agents;
	
	/* Contains two to three keys:
	 * 
	 * dimensions: native int array with x and y dimensions.
	 * 
	 * obstacles: HashSet of native int arrays with x and y coordinates of
	 *            obstacles.
	 * 
	 * edges: HashSet of native int arrays with x1, y1, x2 and y2 coordinates
	 *        of edges pointing from (x1, y1) to (x2, y2).
	 */
	public YamlClassicMap map;
	
	public YamlClassicScenario() {}
	
	public YamlClassicMap map() { return map; }
	
	public YamlClassicAgent[] agents() { return agents; } 
}

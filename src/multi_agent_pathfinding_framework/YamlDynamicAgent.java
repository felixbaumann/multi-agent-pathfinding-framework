/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* This class is used to load the content of the agents-section of a dynamic
 * yaml file into memory in a structured way.
 * 
 * The section may look like:
 * 
 * agents:
 * -   name: agent0
 *     start: [21, 20]
 * -   name: agent1
 *     start: [42, 14]
 *     
 *  Note that TABs are not allowed! Use regular spaces.
 */
public class YamlDynamicAgent {
	
	/* Name is optional. */
	public String name = "";
	
	/* Start coordinates of the agent (x, y). */
	public int start[];
	
	public YamlDynamicAgent() {}
	
	public String name() { return name; }
	
	public int[] start() { return start; }
}

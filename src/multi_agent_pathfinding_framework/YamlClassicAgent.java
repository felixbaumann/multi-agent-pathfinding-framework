/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* This class is used to load the content of the agents-section of a classic
 * yaml file into memory in a structured way.
 * 
 * The section may look like:
 * 
 * agents:
 * -   goal: [52, 60]
 *     name: agent0
 *     start: [21, 20]
 * -   goal: [55, 60]
 *     name: agent1
 *     start: [42, 14]
 *     
 *  Note that TABs are not allowed! Use regular spaces.
 */
public class YamlClassicAgent {

	/* Name is optional. */
	public String name = "";
	
	/* Start coordinates of the agent (x, y). */
	public int start[];
	
	/* Goal coordinates of the agent (x, y). */
	public int goal[];
	
	public YamlClassicAgent() {}
	
	public String name() { return name; }
	
	public int[] start() { return start; }
	
	public int[] goal() { return goal; }
}

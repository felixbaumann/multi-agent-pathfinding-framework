/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashSet;

/* This class is used to load the content of the map-section of a dynamic yaml
 * file into memory in a structured way.
 * 
 * The section may look like:
 * 
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
 *  The obstacles section is optional and so it the parkingSpots section.
 * */
public class YamlDynamicMap {

	/* Two-element int array with x and y dimensions. */
	public int dimensions[] = {-1, -1};
	
	/* Set of two-element int arrays, each containing an x and y coordinate of
	 * an obstacle. */
	public HashSet<int[]> obstacles = new HashSet<int[]>();	
	
	/* Set of two-element int arrays, each containing an x and y coordinate of
	 * a parking spot. That's a position on the map dedicated for agents without
	 * a current task. It should be chosen such that the position doesn't occur
	 * in any task and occupying it does not block any other agents. */
	public HashSet<int[]> parkingSpots = new HashSet<int[]>();	
	
	/* Set of four-element int arrays, each containing x1, y1, x2 and y2
	 * coordinates of edges pointing from (x1, y1) to (x2, y2). */
	public HashSet<int[]> edges = new HashSet<int[]>();
	
	public YamlDynamicMap() {}
	
	public int[] dimensions() { return dimensions; }
	
	public HashSet<int[]> obstacles() { return obstacles; }
	
	public HashSet<int[]> parkingSpots() { return parkingSpots; }
	
	public HashSet<int[]> edges() { return edges; }
}

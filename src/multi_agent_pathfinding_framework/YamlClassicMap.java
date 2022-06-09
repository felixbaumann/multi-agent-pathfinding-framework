/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashSet;

/* This class is used to load the content of the map-section of a classic yaml
 * file into memory in a structured way.
 * 
 * The section may look like:
 * 
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
 * */
public class YamlClassicMap {

	/* Two-element int array with x and y dimensions. */
	public int dimensions[] = {-1, -1};
	
	/* Set of two-element int arrays, each containing an x and y coordinate of
	 * an obstacle. */
	public HashSet<int[]> obstacles = new HashSet<int[]>();
	
	/* Set of four-element int arrays, each containing x1, y1, x2 and y2
	 * coordinates of edges pointing from (x1, y1) to (x2, y2).
	 */
	public HashSet<int[]> edges = new HashSet<int[]>();
	
	public YamlClassicMap() {}
	
	public int[] dimensions() { return dimensions; }
	
	public HashSet<int[]> obstacles() {return obstacles; }
	
	public HashSet<int[]> edges() { return edges; }	
}

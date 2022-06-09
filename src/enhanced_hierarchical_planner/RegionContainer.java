/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.ArrayList;
import java.util.HashSet;

import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Position;

/* The map is divided into regions of (almost) equal sizes.
 * Each region has an index given as 
 * 
 *  -----------------------
 * |      |      |     |   |
 * |   8  |   9  |  10 |11 |
 * |      |      |     |   |
 *  -----------------------
 * |      |      |     |   |
 * |   4  |   5  |  6  | 7 |
 * |      |      |     |   |
 *  -----------------------
 * |   0  |   1  |  2  | 3 |
 *  -----------------------
 *  
 * With the origin of the coordinate system being in the left lower corner.
 * 
 * This class contains and provides access to these regions.
 * 
 */
public class RegionContainer {

	private final ArrayList<Region> regions;

	/* Number of regions in a row. */
	private int horizontalCount;
	
	/* Number of positions in a single region horizontally. */
	private int horizontalRegionSize;
	
	/* Number of positions in a single region vertically. */
	private int verticalRegionSize;
	
	/* Edges that connect two regions. */
	public HashSet<Edge> borderEdges;
	
	
	public RegionContainer(ArrayList<Region> regions,
					   int horizontalCount,
					   int horizontalRegionSize,
					   int verticalRegionSize) {
		
		this.regions = regions;
		this.horizontalCount = horizontalCount;
		this.horizontalRegionSize = horizontalRegionSize;
		this.verticalRegionSize = verticalRegionSize;
		this.borderEdges = new HashSet<Edge>();
	}
	
	
	/* Returns the region with the given id. */
	public Region region(int id) { return regions.get(id); }
	
	
	/* Returns the region of the given position. */
	public Region region(Position position) {
		
		/* Find the horizontal region index h such that
		 * horizontalSize * h < position.x < horizontalSize * (h + 1).
		 * Remember that Java truncates the fractional part of the result
		 * of any int/int division. */
		int horizontalRegion = position.x / horizontalRegionSize;
		
		/* Find the vertical region index v such that
		 * verticalSize * v < position.y < verticalSize * (v + 1). */
		int verticalRegion = position.y / verticalRegionSize;
		 	
		return region(verticalRegion * horizontalCount + horizontalRegion);	
	}
	
	
	/* Add a low level edge that crosses the border between two regions. */
	public void addBorderEdge(Edge edge) { borderEdges.add(edge); }
	
	
	/* Check whether the given low level edge crosses the border between
	 * two regions. */
	public boolean isBorderEdge(Edge edge) {
		
		return borderEdges.contains(edge);
	}
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.HashSet;

import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Position;

/* Regions represent rectangular sections of a map and are used by
 * the hierarchical planner. An agent may traverse multiple regions and even
 * the same region multiple times. Each time is a seperate traversal though.
 */
public class Region {

	/* Edges starting and ending within this region. */
	private HashSet<Edge> internalEdges = new HashSet<Edge>();
	
	/* Facilitates finding the region in a list.
	 * Region indices are given as 
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
	 * With the origin of the coordinate system being in the left lower
	 * corner. */
	public final int regionIndex;
	
	/* Each region has a position in a seperate high-level coordinate
	 * system. */
	public final Position highLevelPosition;
	
	/* Smallest and largest x and y coordinates that combined create positions
	 * that still belong to this region. */
	public final int minX, minY, maxX, maxY;

	
	public Region(int index,
			      Position highLevelPosition,
			      int minX,
			      int minY,
			      int maxX,
			      int maxY) {
		
		this.regionIndex = index;	
		this.highLevelPosition = highLevelPosition;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}
	
	/* An internal edge starts and ends in this region. */
	public void addInternalEdge(Edge edge) {
		
		this.internalEdges.add(edge);
	}
	
	
	public boolean internalEdge(Edge edge) {
		
		return internalEdges.contains(edge);
	}
	
	
	/* Returns whether the given position lies within this region. */
	public boolean contains(Position position) {
		
		return minX <= position.x
			&& minY <= position.y
			&& maxX >= position.x
			&& maxY >= position.y;
	}
	
	
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof Region)) return false;
		
		return ((Region) object).regionIndex == this.regionIndex;
	}
	
	
	@Override
	public int hashCode() { return regionIndex; }
}

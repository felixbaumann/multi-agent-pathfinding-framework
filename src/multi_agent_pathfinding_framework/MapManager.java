/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;


/* This class wraps a map within a scenario and provides functionality for
 * both static and dynamic map graphs.
 * In a static map graph, the direction of each edge is fixed. Be it a
 * unidirectional (directed) or bidirectional (undirected) edge.
 * In a dynamic map graph, the direction of an edge may change over time. */
public class MapManager {

	/* Map contains edges, possible obstacles and dimensions. */
	public final Map map;
	
	/* Defines how often the edges in the map change their direction.
	 * 0 means static edges that never change. */
	public final int directionChangeFrequency;
	
	/* True of the edges never change their direction. */
	private final boolean staticGraph;
	
	public MapManager(Map map, int directionChangeFreq) {
		
		this.map = map;
		
		this.directionChangeFrequency = directionChangeFreq;
		
		staticGraph = (directionChangeFreq == 0);		
	}
	
	
	/* This function checks whether the passing of the given edge is allowed
	 * at the given time.
	 * 
	 * In case of a static graph, an existing edge is enough.
	 * In case of a dynamic graph, the direction of the edge must
	 * match the legal direction. This depends on the current time,
	 * the location of the edge and finally the frequency of directional
	 * changes of edges on the map.
	 * 
	 * At time 0, the first horizontal edge leads from (0,0) to (1,0) and
	 * the first vertical edge leads from (0,1) to (0,0), given that those
	 * positions are not blocked.
	 * 
	 * directionChangeFreq defines how many of the following horizontal edges
	 * ((1,0),(2,0)), ((2,0),(3,0)), ... also point in this rightwards
	 * direction (including the first one mentioned above).
	 * 
	 * Afterwards, the direction is inverted for the next directionChangeFreq
	 * edges and so on.
	 *  
	 * Furthermore, the direction of edges in even rows matches the direction
	 * of their respective partner edges in other even rows.
	 * The direction of edges in odd rows complements them. 
	 * 
	 * The direction of all edges are inverted after every directionChangeFreq
	 * time steps.
	 * 
	 * From this design follows, that each criterium that is not met inverts
	 * the direction of a specific edge. The criteria are described by integer
	 * numbers below. Whether the criterium is met is encoded in the number
	 * being even or odd. Since an even number of unmet criteria can even each
	 * other out, it's possible to just sum them up and check the parity of
	 * the result.
	 */
	public boolean passagePermitted(TimedEdge timedEdge) {
		
		Edge edge = timedEdge.edge;				
		
		/* There has to be an edge in the first place. */
		if (!map.edges.contains(edge)) return false;
		
		/* In a scenario with a static map that's a sufficient requirement. */
		if (staticGraph) return true;
		
		/* The direction of the edges is inverted after each time frame. */
		int timeframe = timedEdge.time / directionChangeFrequency;
				
		/* Horizontal edge. */
		if (horizontal(edge)) {			
			
			int section = Math.min(edge.source.x, edge.target.x)
				/ directionChangeFrequency;
			
			int row = edge.source.y;
			
			int rightwards = rightwards(edge);									
			
			return odd(timeframe + section + row + rightwards);									
		}
		
		/* Vertical edge. */
		else {
			
			int section = Math.min(edge.source.y, edge.target.y)
				/ directionChangeFrequency;
			
			int column = edge.source.x;
			
			int upwards = upwards(edge);
			
			return !odd(timeframe + section + column + upwards);								
		}		
	}

	
	/* Checks whether the given edge is an horizontal edge. */
	private boolean horizontal(Edge edge) {
		
		return edge.source.y == edge.target.y;
	}
	
	
	/* Given a horizontal edge, this function returns 1 if the edge points
	 * to the right. */
	private int rightwards(Edge edge) {
		
		return (edge.source.x < edge.target.x) ? 1 : 0;
	}
	
	
	/* Given a vertical edge, this function returns 1 if the edge points
	 * upwards. */
	private int upwards(Edge edge) {
		
		return (edge.source.y < edge.target.y) ? 1 : 0;
	}
	
	
	/* Checks whether the given number is odd. */
	private boolean odd(int number) {
		
		return (number & 1) == 1;
	}
}

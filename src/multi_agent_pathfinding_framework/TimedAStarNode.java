/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashMap;

/* Represents a node in a timed A* search.
 * That means a search where wait operations are possible. Thus, the cost of a
 * node (g-score) is fixed and given by the time the position is reached.
 * If the position can be reached sooner, that's a whole new search node. */
public class TimedAStarNode implements Comparable<TimedAStarNode>{

	public final TimedPosition timedPosition;
	
	public final Position position;
	
	public final int time;
	
	public int fScore;
	
	public TimedAStarNode(TimedPosition timedPosition, Position goal) {
		
		this.timedPosition = timedPosition;
		this.position = timedPosition.position();
		this.time = timedPosition.t;
		this.fScore 
		    = timedPosition.t + manhattanDistance(timedPosition, goal);
	}
	
	
	/* Given two positions (source and target),
	 * computes the Manhattan distance between those. */
	private int manhattanDistance(TimedPosition source, Position target) {
		
		return Math.abs(source.x - target.x)
				+ Math.abs(source.y - target.y);
	}

	
	/* This constructor creates a timed A* node that does not use
	 * the Manhattan distance as heuristic but the given true distances. */
	public TimedAStarNode(TimedPosition timedPosition, Position goal,
						  HashMap<Edge, Integer> trueDistances)
								 throws DistanceTableException {
		
		this.timedPosition = timedPosition;
		this.position = timedPosition.position();
		this.time = timedPosition.t;
		
		Edge pair = new Edge(position, goal);
		
		if (!trueDistances.containsKey(pair)) {
		
			throw new DistanceTableException("True distance table lookup "
					+ "failed. Most likely, the distance to a position"
					+ "was requested that is not part of the table."
					+ "In the context of TokenPassing that is a position"
					+ "which is not an endpoint.");
		}		
		this.fScore = timedPosition.t + trueDistances.get(pair);
	}	
	

	/* Allows the nodes to be sorted according to the fscore in a min-heap. */
	@Override
	public int compareTo(TimedAStarNode otherNode) {
		
		return Integer.compare(this.fScore, otherNode.fScore);
	}
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedPosition;

/* Represents a node in a timed A* search.
 * That means a search where wait operations are possible.
 * Thus, the cost of node (g-score) is not fixed and can be updated if
 * a shorter way is found. 
 * 
 * Note that this class slightly differs from the TimedAStarNode class in
 * the framework. */
public class TimedAStarNode implements Comparable<TimedAStarNode>{

	/* The position this node represents. */
	public final TimedPosition timedPosition;
	
	/* The position the whole A* search is aiming at. */
	public final Position goal;
	
	/* Tentative cost of this node given as the sum of actions it took to get
	 * here from the start node. */
	private int cost;
	
	/* Tentative cost + heuristic to the goal. */
	private int fScore;
	
	public TimedAStarNode(TimedPosition timedPosition, int cost,
						  Position goal) {
		
		this.timedPosition = timedPosition;
		this.goal = goal;
		this.cost = cost;
		this.fScore
			= cost + manhattanDistance(timedPosition.position(), goal);
	}
	
	
	public int cost() { return cost; }
	
	public int fScore() { return fScore; }
	
	
	/* Given two positions (source and target),
	 * computes the Manhattan distance between those. */
	private int manhattanDistance(Position source, Position target) {
		
		return Math.abs(source.x - target.x)
				+ Math.abs(source.y - target.y);
	}


	/* Allows the nodes to be sorted according to the fscore in a min-heap. */
	@Override
	public int compareTo(TimedAStarNode otherNode) {
		
		return Integer.compare(this.fScore, otherNode.fScore);
	}
	
	/* Hash code should only depend on the position. */
	@Override
	public int hashCode() { return timedPosition.hashCode(); }
	
	
	/* Two timed A* nodes are considered to be the same if they refer to the
	 * same position. */
	@Override
	public boolean equals(final Object object) {
		
		if (!(object instanceof TimedAStarNode)) return false;
		
	    return ((TimedAStarNode) object).timedPosition.equals(
	    		this.timedPosition);
	}
}

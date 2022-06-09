/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* Represents a node in a regular, untimed A* search.
 * That means a search where wait operations are impossible.
 * Thus, the cost of node (g-score) is not fixed and can be updated if
 * a shorter way is found. */
public class RegularAStarNode implements Comparable<RegularAStarNode>{

	/* The position this node represents. */
	public final Position position;
	
	/* The position the whole A* search is aiming at. */
	public final Position goal;
	
	/* Tentative cost of this node given as the sum of actions it took to get
	 * here from the start node. */
	private int cost;
	
	/* Tentative cost + heuristic to the goal. */
	private int fScore;
	
	
	public RegularAStarNode(Position position, int cost, Position goal) {
		
		this.position = position;
		
		this.goal = goal;
		
		updateCost(cost);
	}
	
	public void updateCost(int cost) {
		
		this.cost = cost;
		
		this.fScore = cost + manhattanDistance(position, goal);
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
	public int compareTo(RegularAStarNode otherNode) {
		
		return Integer.compare(this.fScore, otherNode.fScore);
	}
	
	/* Hash code should only depend on the position. */
	public int hashCode() { return position.hashCode(); }
	
	
	/* Two regular A* nodes are considered to be the same if they refer to the
	 * same position. */
	public boolean equals(final Object object) {
		
		if (!(object instanceof RegularAStarNode)) return false;
		
	    if (!((RegularAStarNode) object).position.equals(this.position)) {
	    	
	    	return false;
	    }	    
	    
        return true;
	}
}

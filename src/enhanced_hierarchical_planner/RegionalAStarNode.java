/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import multi_agent_pathfinding_framework.Position;

/* Represents a node in an untimed A* search over a region graph.
 * That means a regular search without wait operations.
 * Thus, the cost of a node (g-score) can change if a shorter way there is
 * found. The node refers to a specific region and has a cost that describes
 * how many steps on the region graph it takes to reach this region from a
 * given start region. */
public class RegionalAStarNode implements Comparable<RegionalAStarNode>{

	/* Region represented by this A*-node. */
	public final Region region;
	
	/* Goal region of the entire A*-search. */
	private final Region goal;
	
	/* Currently lowest cost for reaching this region from the start
	 * position. */
	private int cost;
	
	/* Sum of the cost to reach this region and the heuristic for reaching
	 * to goal from here. */
	public int fScore;
	
	
	public RegionalAStarNode(Region region, int cost, Region goal) {
		
		this.region = region;
		this.goal = goal;
		this.cost = cost;
		this.fScore = cost + manhattanDistance(region.highLevelPosition,
				                               goal.highLevelPosition);
	}
	
	
	/* Given two positions (source and target),
	 * computes the Manhattan distance between those. */
	private int manhattanDistance(Position source, Position target) {
		
		return Math.abs(source.x - target.x)
				+ Math.abs(source.y - target.y);
	}


	public void setCost(int cost) {
		
		this.cost = cost;
		
		this.fScore = cost + manhattanDistance(region.highLevelPosition,
                goal.highLevelPosition);
	}
	
	
	public int getCost() { return cost; }
	
	
	/* Allows the nodes to be sorted according to the fscore in a min-heap. */
	@Override
	public int compareTo(RegionalAStarNode otherNode) {
		
		return Integer.compare(this.fScore, otherNode.fScore);
	}
}

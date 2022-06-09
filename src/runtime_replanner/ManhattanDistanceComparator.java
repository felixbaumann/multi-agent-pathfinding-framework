/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import java.util.Comparator;

import multi_agent_pathfinding_framework.Position;

/* Compares two positions with respect to their Manhattan distance to some
 * predefined goal location. */
public class ManhattanDistanceComparator implements Comparator<Position> {

	private final Position goal;
	
	public ManhattanDistanceComparator(Position goal) { this.goal = goal; }
	
	
	@Override
	public int compare(Position pos1, Position pos2) {
		
		Integer distancePos1 = manhattanDistance(pos1);
		Integer distancePos2 = manhattanDistance(pos2);
		
		return distancePos1.compareTo(distancePos2);
	}
	
	
	private int manhattanDistance(Position pos) {
		
		return Math.abs(goal.x - pos.x) + Math.abs(goal.y - pos.y);
	}
}

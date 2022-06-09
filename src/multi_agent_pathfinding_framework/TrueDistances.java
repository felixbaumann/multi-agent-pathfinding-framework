package multi_agent_pathfinding_framework;

import java.util.ArrayList;
import java.util.HashMap;

/* This class is used to efficiently compute the true distances of all
 * positions on a map to one target position. */
public class TrueDistances {
	
	/* Returns a Hashmap with one entry for each position on the map.
	 * The keys a position pairs (edges) of the form (position, target).
	 * The target is always identical. Just the given parameter.
	 * The value for a key is the true distance on the graph from
	 * the respective position to the given target. */
	public HashMap<Edge, Integer> trueDistances(Map map, Position target) {
		
		HashMap<Position, Integer> trueDistances
		    = new HashMap<Position, Integer>();
	
		/* Note that we never actually remove elements from the open list,
		 * since popping the first element would require every other one
		 * to be shifted one step forward. Instead, we use a pointer. */
		ArrayList<Position> open
			= new ArrayList<Position>();					
		
		open.add(target);
		
		trueDistances.put(target, 0);
		
		int pointer = 0;
		
		while (pointer < open.size()) {
			
			Position current = open.get(pointer);
			
			int distance = trueDistances.get(current);
			
			ArrayList<Position> predecessors = getPredecessors(map, current);
			
			for (Position predecessor : predecessors) {
				
				/* If a predecessor (position) has not been found before,
				 * its shortest path to the target includes the current
				 * position and the distance is respectively by 1 higher
				 * than the distance of the current position. */
				if (!trueDistances.containsKey(predecessor)) {
					
					trueDistances.put(predecessor, distance + 1);
					
					open.add(predecessor);					
				}								
			}			
			pointer++;
		}
		
		return edgeMap(trueDistances, target);
	}
	
	
	/* Returns all nodes FROM which an edge points to the current
	 * position. */
	private ArrayList<Position> getPredecessors(Map map, Position current) {
		
		ArrayList<Position> predecessors = new ArrayList<Position>();
		
		/* Positions to the right, left, top and bottom. */
		Position candidates[] = { new Position(current.x + 1, current.y),
								  new Position(current.x - 1, current.y),
								  new Position(current.x, current.y + 1),
								  new Position(current.x, current.y - 1) };
		
		/* A candidate position is a true predecessor if there's an edge
		 * in the graph pointing from the candidate to the current position.
		 */
		for (Position candidate : candidates) {
			
			if (map.edges.contains(new Edge(candidate, current))) {
				
				predecessors.add(candidate);
			}			
		}		
		return predecessors;		
	}
		
	
	/* Converts a given distance map of the form Position -> distance
	 * to an equivalent map of the form
	 * Edge(Position, TargetPosition) -> distance.*/
	private HashMap<Edge, Integer> edgeMap(
			HashMap<Position, Integer> distances,
			Position target) {
		
		HashMap<Edge, Integer> result = new HashMap<Edge, Integer>();
		
		for (Position position : distances.keySet()) {
			
			result.put(new Edge(position, target), distances.get(position));
		}
		return result;
	}	
}

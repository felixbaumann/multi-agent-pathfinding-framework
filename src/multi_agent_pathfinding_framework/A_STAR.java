/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

/* Regular A* without dependencies on other agents who may block positions. */
public class A_STAR {	
		
	/* Runs A* to find the shortest way from the given start to the given
	 * goal. Does not track the path or the actions though.
	 * Just returns the cost of the shortest path.
	 * This method is basically a heuristic called the true distance
	 * heuristic. It's admissible but not as optimistic as Manhattan.
	 * Returns -1 if the goal is unreachable. */
	public int minCost(Map map, Position start, Position goal) {

		/* Set of nodes ready for expansion. Ordered as a min-heap according
		 * to the fscore (passed time + heuristic value). */
		PriorityQueue<RegularAStarNode> openQueue
		    = new PriorityQueue<RegularAStarNode>();
		
		/* Same nodes as in the open queue but accessible by their positions.
		 */
		HashMap<Position, RegularAStarNode> openMap
			= new HashMap<Position, RegularAStarNode>();
		
		/* Set of positions already expanded.
		 * Avoids expansion of equivalent nodes. */
		HashSet<Position> closedSet = new HashSet<Position>();
		
		/* Pass the start position and also the goal (target) of the agent
		 * which is needed to compute the heuristic value of the position
		 * and thus also of the node.
		 * The 0 refers to the cumulative cost. */
		RegularAStarNode node = new RegularAStarNode(start, 0, goal);
		
		
		openQueue.add(node);
		openMap.put(node.position, node);
		
		while(openQueue.size() > 0) {
			
			/* Remove the most promising open node (smallest f-score) from
			 * the queue. */
		    RegularAStarNode currentNode = openQueue.poll();
		    openMap.remove(currentNode.position);
		    
		    /* Check whether the current node references the goal position. */
		    if (currentNode.position.equals(goal)) {
		    	
		    	return currentNode.cost();
		    }
		    
		    closedSet.add(currentNode.position);
		    
		    /* The HashMap would contain the predecessors in a regular
		     * planning process. Since we're not interested in recreating the
		     * path in this function, we don't have to keep track of those
		     * predecessors. */
		    expandNode(map, currentNode, openQueue, openMap, closedSet,
		    		new HashMap<Position, Position>(),
		    		new HashSet<Position>());
		}
		
		/* The target is unreachable. */
		return -1;
	}

	
	/* Run A* to find the shortest way from the given start to the given goal.
	 * Use this functio if there are no restricted positions. */
	public ArrayList<Position> AStar(Map map, Position start, Position goal) {
		
	    return AStar(map, start, goal, new HashSet<Position>());	
	}
	
	
	/* Run A* to find the shortest way from the given start to the given goal.
	 * Use this function if there's a set of positions that must not be
	 * used on the path. */
	public ArrayList<Position> AStar(Map map, Position start, Position goal,
			                         HashSet<Position> forbidden) {
		
		/* If the start or goal position are not allowed to be part of the
		 * path, this search cannot possibly be successful. */
		if (forbidden.contains(start) || forbidden.contains(goal)) {
			
			return null;
		}
		
		/* Set of nodes ready for expansion. Ordered as a min-heap according
		 * to the fscore (passed time + heuristic value). */
		PriorityQueue<RegularAStarNode> openQueue
		    = new PriorityQueue<RegularAStarNode>();
		
		/* Same nodes as in the open queue but accessible by their positions.
		 */
		HashMap<Position, RegularAStarNode> openMap
			= new HashMap<Position, RegularAStarNode>();
		
		/* Set of positions already expanded.
		 * Avoids expansion of equivalent nodes. */
		HashSet<Position> closedSet = new HashSet<Position>();
		
		/* In order to reconstruct the path if the search is successful,
		 * we need to keep track of the used edges, i.e. from which
		 * position a position was reached. */
		HashMap<Position, Position> predecessors
		    = new HashMap<Position, Position>();		
		
		/* Pass the start position and also the goal (target) of the agent
		 * which is needed to compute the heuristic value of the position
		 * and thus also of the node.
		 * The 0 refers to the cumulative cost. */
		RegularAStarNode node = new RegularAStarNode(start, 0, goal);
		
		
		openQueue.add(node);
		openMap.put(node.position, node);
		
		while (openQueue.size() > 0) {
			
			/* Remove the most promising open node (smallest f-score) from
			 * the queue. */
		    RegularAStarNode currentNode = openQueue.poll();
		    openMap.remove(currentNode.position);
		    
		    /* Check whether the current node references the goal position. */
		    if (currentNode.position.equals(goal)) {		    	

		    	return reconstructPath(currentNode, predecessors);
		    }
		    
		    closedSet.add(currentNode.position);
		    
		    expandNode(map, currentNode, openQueue, openMap, closedSet,
		    		   predecessors, forbidden);
		}
		
		/* The target is unreachable. */
		return null;
	}
	
	
	/* Once the goal is reached, the path from the start node has to be
	 * reconstructed. This is done here by starting at the goal node and
	 * using the map "predecessors" to iteratively find the
	 * previous position until the start node is found.
	 * 
	 *  "node" is the goal node that has been reached and from which the path
	 * back to the start shall be reconstructed. */
	private ArrayList<Position> reconstructPath(RegularAStarNode node,
			HashMap<Position, Position> predecessors) {
		
		Position position = node.position;
		
		Position previousPosition;
		
		ArrayList<Position> plan = new ArrayList<Position>();
				
		/* Add the goal. */
		plan.add(position);
		
		/* Reconstruct the path by following the previous positions and noting
		 * the actions leading from there. */
		while (predecessors.containsKey(position)) {
			
			previousPosition = predecessors.get(position);
			
			plan.add(previousPosition);
			
			position = previousPosition;			
		}
		
		/* Remove the last timedPosition of the plan. Since the plan is still
		 * reversed, this is the start position of the task which is already
		 * part of the agent's plan from the previous task or from the
		 * initialization in case it's the first task */		
		plan.remove(plan.size() - 1);
		
		/* Since the plan was noted starting at the target, it has to be
		 * reversed. */
		Collections.reverse(plan);
		
		return plan;
	}

	
	/* Consider all existing neighbors of the given node and add them
	 * to the open queue (and map) if they haven't been expanded already.
	 * If they already are in the open queue, update their cost if necessary.
	 */
	private void expandNode(Map map, RegularAStarNode givenNode,
			PriorityQueue<RegularAStarNode> openQueue,
			HashMap<Position, RegularAStarNode> openMap,
			HashSet<Position> closedSet,
			HashMap<Position, Position> predecessors,
			HashSet<Position> forbidden) {
		
		ArrayList<RegularAStarNode> neighbors
		    = getNeighbors(map, givenNode, forbidden);
		
		for (RegularAStarNode candidate : neighbors) {
			
			/* Already expanded. */
			if (closedSet.contains(candidate.position)) { continue; }
			
			/* Schedule for expansion. */
			if (!openMap.containsKey(candidate.position)) {
				
				openQueue.add(candidate);
				openMap.put(candidate.position, candidate);
				predecessors.put(candidate.position, givenNode.position);
			}
			
			/* Neighbor already in the open queue. */
			else {
				
				RegularAStarNode presentNode = openMap.get(candidate.position);
				
				/* If the node in the queue has a higher cost than the node
				 * from the new path, replace it. */
				if (presentNode.cost() > candidate.cost()) {

					openMap.put(candidate.position, candidate);
					openQueue.remove(presentNode);
					openQueue.add(candidate);
					predecessors.put(candidate.position, givenNode.position);
				}
			}		
		}
	}
	
	
	/* Consider the 4 potential neighbors in each direction of the given node.
	 * Each of those is a proper neighbor iff there's an edge leading to it.
	 * Return all proper neighbors as new nodes with an incremented cost. */
	private ArrayList<RegularAStarNode> getNeighbors(Map map,
			RegularAStarNode givenNode, HashSet<Position> forbidden) {
		
        ArrayList<RegularAStarNode> neighbors
            = new ArrayList<RegularAStarNode>();
		
        Position pos = givenNode.position;
        Position posNorth = new Position(pos.x, pos.y + 1);
        Position posSouth = new Position(pos.x, pos.y - 1);
        Position posEast = new Position(pos.x + 1, pos.y);
        Position posWest = new Position(pos.x - 1, pos.y);
        
        		
		if (map.edges.contains(new Edge(pos, posNorth))
			&& !forbidden.contains(posNorth)) {
			
			neighbors.add(new RegularAStarNode(
					posNorth, givenNode.cost() + 1, givenNode.goal));
		}
		
		if (map.edges.contains(new Edge(pos, posSouth))
			&& !forbidden.contains(posSouth)) {
			
			neighbors.add(new RegularAStarNode(
					posSouth, givenNode.cost() + 1, givenNode.goal));
		}
		
		if (map.edges.contains(new Edge(pos, posEast))
			&& !forbidden.contains(posEast)) {
			
			neighbors.add(new RegularAStarNode(
					posEast, givenNode.cost() + 1, givenNode.goal));
		}

		if (map.edges.contains(new Edge(pos, posWest))
			&& !forbidden.contains(posWest)) {
			
			neighbors.add(new RegularAStarNode(
					posWest, givenNode.cost() + 1, givenNode.goal));
		}

		return neighbors;
	}
}

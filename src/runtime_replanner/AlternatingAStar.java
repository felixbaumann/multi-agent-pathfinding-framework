/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;

import multi_agent_pathfinding_framework.MapManager;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.Scenario;
import multi_agent_pathfinding_framework.TimedEdge;
import multi_agent_pathfinding_framework.TimedPosition;
import multi_agent_pathfinding_framework.Timeout;

/* Alternating A*.
 * 
 * This class provides an A* search without dependencies on other agents who
 * may block positions. 
 * However, it exploits the nature of the dynamic graph, i.e. that edge
 * directions change with a given fixed frequency to drastically reduce the
 * search space. While a regular timed A* search has a search space with
 * #positionsOnTheMap * timehorizon states, this variant's space has only
 * #positionsOnTheMap * frequency * 2 states. */
public class AlternatingAStar {

	/* Runs A* to find the shortest way from the given start to the given
	 * goal. */
	public ArrayList<Position> alternatingAStar(Scenario scenario,
												Position start,
												Position goal,
												int startTime,
												long runtimeLimit)
													throws TimeoutException {
		
		/* Set of nodes ready for expansion. Ordered as a min-heap according
		 * to the fscore (passed time + heuristic value). */
		PriorityQueue<TimedAStarNode> openQueue
		    = new PriorityQueue<TimedAStarNode>();
		
		/* Same nodes as in the open queue but accessible by their positions.
		 */
		HashMap<TimedPosition, TimedAStarNode> openMap
			= new HashMap<TimedPosition, TimedAStarNode>();
		
		/* Set of positions already expanded.
		 * Avoids expansion of equivalent nodes. */
		HashSet<TimedPosition> closedSet = new HashSet<TimedPosition>();
		
		/* In order to reconstruct the path if the search is successful,
		 * we need to keep track of the used edges, i.e. from which
		 * position a position was reached. */
		HashMap<TimedPosition, TimedPosition> predecessors
		    = new HashMap<TimedPosition, TimedPosition>();		
		
		/* An edge points for directionChangeFrequency in one direction and
		 * afterwards for just as long in the opposite direction.
		 * That means, after a period of twice this frequency the situation
		 * repeats itself. That means, it'll take just as many waiting
		 * operations until an edge can be used.
		 * In case of a static graph, this period is 1 though.*/
		int modulo
		    = Math.max(1, scenario.mapManager.directionChangeFrequency * 2);
		
		/* Pass the start position and also the goal (target) of the agent
		 * which is needed to compute the heuristic value of the position
		 * and thus also of the node.
		 * It's import to start with a cost equal to the time the A* search is
		 * started since the cost is also interpreted as time stamp and used
		 * to determine the current direction of an edge when looking for
		 * accessible neighbors. */
		TimedAStarNode node = new TimedAStarNode(new TimedPosition(
			start,
			startTime % modulo),
			startTime,
			goal);		
		
		openQueue.add(node);
		openMap.put(node.timedPosition, node);
		
		while(openQueue.size() > 0) {
			
			/* Cancel the process if it exceeds the runtime limit. */
			Timeout.checkForTimeout(runtimeLimit);
			
			/* Remove the most promising open node (smallest f-score) from
			 * the queue. */
			TimedAStarNode currentNode = openQueue.poll();
		    openMap.remove(currentNode.timedPosition);
		    
		    /* Check whether the current node references the goal position. */
		    if (currentNode.timedPosition.position().equals(goal)) {		    	

		    	return reconstructPath(currentNode, predecessors);
		    }
		    
		    closedSet.add(currentNode.timedPosition);
		    
		    /* Consider up to 5 neighbors. */
		    expandNode(scenario.mapManager,
		    		   currentNode,
		    		   openQueue,
		    		   openMap,
		    		   closedSet,
		    		   predecessors,
		    		   modulo);
		}
		
		/* The target is unreachable. */
		return null;
	}
	
	
	/* Once the goal is reached, the path from the start node has to be
	 * reconstructed. This is done here by starting at the goal node and
	 * using the map "predecessors" to iteratively find the
	 * previous position until the start node is found.
	 * 
	 * "node" is the goal node that has been reached and from which the path
	 * back to the start shall be reconstructed. */
	private ArrayList<Position> reconstructPath(TimedAStarNode node,
			HashMap<TimedPosition, TimedPosition> predecessors) {
		
		TimedPosition position = node.timedPosition;
		
		TimedPosition previousPosition;
		
		ArrayList<Position> plan = new ArrayList<Position>();
				
		/* Add the goal. */
		plan.add(position.position());
		
		/* Reconstruct the path by following the previous positions and noting
		 * the actions leading from there. */
		while (predecessors.containsKey(position)) {
			
			previousPosition = predecessors.get(position);
			
			plan.add(previousPosition.position());
			
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
	private void expandNode(
			MapManager mapManager,
			TimedAStarNode givenNode,
			PriorityQueue<TimedAStarNode> openQueue,
			HashMap<TimedPosition, TimedAStarNode> openMap,
			HashSet<TimedPosition> closedSet,
			HashMap<TimedPosition, TimedPosition> predecessors,
			int modulo) {
		
		ArrayList<TimedAStarNode> neighbors = getNeighbors(mapManager,
														   givenNode,
														   modulo);
		
		for (TimedAStarNode candidate : neighbors) {
			
			/* Already expanded. */
			if (closedSet.contains(candidate.timedPosition)) { continue; }
			
			/* Schedule for expansion. */
			if (!openMap.containsKey(candidate.timedPosition)) {
				
				openQueue.add(candidate);
				openMap.put(candidate.timedPosition, candidate);
				predecessors.put(candidate.timedPosition,
								 givenNode.timedPosition);
			}
			
			/* Neighbor already in the open queue. */
			else {
				
				TimedAStarNode presentNode
					= openMap.get(candidate.timedPosition);
				
				/* If the node in the queue has a higher cost than the node
				 * from the new path, replace it. */
				if (presentNode.cost() > candidate.cost()) {

					openMap.put(candidate.timedPosition, candidate);
					openQueue.remove(presentNode);
					openQueue.add(candidate);
					predecessors.put(candidate.timedPosition,
									 givenNode.timedPosition);
				}
			}		
		}
	}
	
	
	/* Consider the 4 potential neighbors in each direction of the given node.
	 * Each of those is a proper neighbor iff there's an edge leading to it.
	 * Return all proper neighbors as new nodes with an incremented cost. 
	 * Also include the same position as a neighbor since waiting is an option
	 * as well. */
	private ArrayList<TimedAStarNode> getNeighbors(MapManager mapManager,
												   TimedAStarNode givenNode,
												   int modulo) {
		
        ArrayList<TimedAStarNode> neighbors
            = new ArrayList<TimedAStarNode>();
        
        int time = givenNode.cost();		
        
        /* The directions of the edges changes periodically with a given
         * frequency.
         * The search space of the timed A* search can be reduced drastically
         * by considering two nodes with the same position and time stamps
         * that differ by a multiple of twice the frequency as equal.
         * For instance, given a frequency of 10, being at a certain position
         * at time 3, 23 or 43 makes no difference with respect to available
         * edges. */
        
        TimedPosition pos = givenNode.timedPosition;                
        
        TimedPosition[] candidates = new TimedPosition[4];
        
        /* North. */
        candidates[0]
        		= new TimedPosition(pos.x, pos.y + 1, (time + 1) % modulo);
        
        /* South. */
        candidates[1] 
        		= new TimedPosition(pos.x, pos.y - 1, (time + 1) % modulo);
        
        /* East. */
        candidates[2] 
        		= new TimedPosition(pos.x + 1, pos.y, (time + 1) % modulo);
        
        /* West. */
        candidates[3] 
        		= new TimedPosition(pos.x - 1, pos.y, (time + 1) % modulo);
                
        
        for (TimedPosition candidate : candidates) {
        	
        	if (mapManager.passagePermitted(new TimedEdge(pos, candidate))) {
    			
    			neighbors.add(new TimedAStarNode(candidate,
    											 givenNode.cost() + 1,
    											 givenNode.goal));
    		}
        }
        
        /* Waiting is always allowed as well. */
        TimedPosition posWait
        	= new TimedPosition(pos.x, pos.y, (time + 1) % modulo);
                
        neighbors.add(new TimedAStarNode(posWait,
        								 givenNode.cost() + 1,
        								 givenNode.goal));
        
		return neighbors;
	}
}
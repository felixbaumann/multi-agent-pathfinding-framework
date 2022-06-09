package token_passing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;

import cooperative_a_star.ReservationTable;
import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.DistanceTableException;
import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.MapManager;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedAStarNode;
import multi_agent_pathfinding_framework.TimedEdge;
import multi_agent_pathfinding_framework.TimedPosition;
import multi_agent_pathfinding_framework.Timeout;

public class TrueDistanceAStar {
	/* Given a map, a start and goal (target) position
	 * as well as a reservation table of blocked positions at certain points
	 * in time, this method performs an A* search from start to target along
	 * the edges of the map. Positions present in the reservation table are
	 * avoided at the given time points.
	 * The admissible heuristic used is the true distance.
	 * 
	 * If the instance is not solvable, the planning would not terminate since
	 * agents can simply wait forever. Therefore a timeHorizon has to be
	 * provided that limits how long plans are allowed to get.
	 */
	public Plan trueDistanceAStar(MapManager mapManager,
					   TimedPosition start,
			           Position target,
			           ReservationTable table,
			           int timeHorizon,
			           long runtimeLimit,
			           Agent agent,
			           HashMap<Edge, Integer> trueDistances)
			        		   throws TimeoutException,
			        		   		  DistanceTableException {
		
		/* This set will contain all distinct timed positions (x, y, t)
		 * ever created to avoid expanding the same node (same position and
		 * same time) several times. */
		HashSet<TimedPosition> visits = new HashSet<TimedPosition>();
		
		/* Set of nodes ready for expansion. Ordered as a min-heap according
		 * to the fscore (passed time + heuristic value). */
		PriorityQueue<TimedAStarNode> openSet
		    = new PriorityQueue<TimedAStarNode>();
		
		/* Pass the start position and also the goal (target) of the agent
		 * which is needed to compute the heuristic value of the timed
		 * position and thus also of the node. */
		TimedAStarNode node
			= new TimedAStarNode(start, target, trueDistances);
		
		visits.add(start);
		
		openSet.add(node);
		
		/* In order to reconstruct the path if the search is successful,
		 * we need to keep track of the used edges, i.e. from which timed
		 * position a timed position was reached. */
		HashMap<TimedPosition, TimedPosition> predecessors
		    = new HashMap<TimedPosition, TimedPosition>();
		
		while (!openSet.isEmpty()) {
			
			/* Cancel the process if it exceeds the runtime limit. */
			Timeout.checkForTimeout(runtimeLimit);
			
			/* Remove the most promising open node (smallest f-score) from
			 * the queue. */
		    TimedAStarNode currentNode = openSet.poll();
			
		    /* Check whether the current node references the goal position. */
		    if (currentNode.timedPosition.position().equals(target)
		    	&& table.isFreeForever(currentNode.timedPosition)) {

		    	return reconstructPath(currentNode, predecessors,
		    						   table, agent);
		    }
			
		    /* Obtain all neighbor-nodes. These are nodes reachable by
		     * a single legal action. An action is legal if the target
		     * position is not already reserved and an edge leading from
		     * the current node to the target node exists. */
			ArrayList<TimedAStarNode> neighbors = getNeighbors(mapManager,
					table, currentNode, target, predecessors, trueDistances);
			
			for (TimedAStarNode neighbor : neighbors) {
				
				/* Check whether the potential plan gets way too long. */
				if (neighbor.timedPosition.t > timeHorizon) { return null; }
				
				/* If the neighbor is a position which we haven't visited at
				 * this time point already, we put it up for expansion. */
				if (!visits.contains(neighbor.timedPosition)) {
					
					visits.add(neighbor.timedPosition);
					openSet.add(neighbor);
				}
			}
		}		
		return null;
	}
	
	
	/* This function computes the neighbors of a given node from the
	 * A*-search. A neighbor is a node one time step later whose position
	 * can be reached by a single legal action. That means either a move
	 * along an edge or a wait operation. Neither the edge nor the node
	 * may be occupied according to the reservation table.
	 * 
	 * "node" is the node whose neighbors are requested.
	 * 
	 * The neighbors returned will already be added to the predecessors map
	 * which will allow to trace back the path to the goal once it's reached.
	 */
	private ArrayList<TimedAStarNode> getNeighbors(
			MapManager mapManager,
			ReservationTable table,
			TimedAStarNode node,
			Position goal,
			HashMap<TimedPosition, TimedPosition> predecessors,
			HashMap<Edge, Integer> trueDistances)
				throws DistanceTableException {
		
		/* This list will store up to 5 neighbors depending on which actions
		 * are possible in the given situation. */
	    ArrayList<TimedAStarNode> neighbors = new ArrayList<TimedAStarNode>();
			    
	    /* Movement to the right. */
	    TimedPosition candidateRight = new TimedPosition(
	    		node.timedPosition.x + 1,
	    		node.timedPosition.y,
	    		node.timedPosition.t + 1);
	    
	    /* Check whether a movement to the right is legal. */
	    if (table.isFree(candidateRight)
	    		&& table.isFree(node.timedPosition, candidateRight)
	    		&& mapManager.passagePermitted(
	    				new TimedEdge(node.timedPosition, candidateRight))) {

	    	neighbors.add(new TimedAStarNode(candidateRight, goal,
	    									 trueDistances));
	    	
	    	predecessors.put(candidateRight, node.timedPosition);
	    }
	    
	   
	    /* Movement to the left. */
	    TimedPosition candidateLeft = new TimedPosition(
	    		node.timedPosition.x - 1,
	    		node.timedPosition.y,
	    		node.timedPosition.t + 1);
	    
	    /* Check whether a movement to the left is legal. */
	    if (table.isFree(candidateLeft) 
	    		&& table.isFree(node.timedPosition, candidateLeft)
	    		&& mapManager.passagePermitted(
	    				new TimedEdge(node.timedPosition, candidateLeft))) {

	    	neighbors.add(new TimedAStarNode(candidateLeft, goal,
	    									 trueDistances));
	    	
	    	predecessors.put(candidateLeft, node.timedPosition);
	    }
	    
	    
	    /* Movement upwards. */
	    TimedPosition candidateTop = new TimedPosition(
	    		node.timedPosition.x,
	    		node.timedPosition.y + 1,
	    		node.timedPosition.t + 1);
	    
	    /* Check whether a movement upwards is legal. */
	    if (table.isFree(candidateTop) 
	    		&& table.isFree(node.timedPosition, candidateTop)
	    		&& mapManager.passagePermitted(
	    				new TimedEdge(node.timedPosition, candidateTop))) {

	    	neighbors.add(new TimedAStarNode(candidateTop, goal,
	    									 trueDistances));
	    	
	    	predecessors.put(candidateTop, node.timedPosition);
	    }
	    
	    /* Movement downwards. */
	    TimedPosition candidateBottom = new TimedPosition(
	    		node.timedPosition.x,
	    		node.timedPosition.y - 1,
	    		node.timedPosition.t + 1);
	    
	    /* Check whether a movement to the bottom is legal. */
	    if (table.isFree(candidateBottom)
	    		&& table.isFree(node.timedPosition, candidateBottom)
	    		&& mapManager.passagePermitted(
	    				new TimedEdge(node.timedPosition, candidateBottom))) {

	    	neighbors.add(new TimedAStarNode(candidateBottom, goal,
	    									 trueDistances));
	    	
	    	predecessors.put(candidateBottom, node.timedPosition);
	    }
	    
	    
	    /* Waiting. */
	    TimedPosition candidateWait = new TimedPosition(
	    		node.timedPosition.x,
	    		node.timedPosition.y,
	    		node.timedPosition.t + 1);
	    
	    /* Check whether waiting is legal. Not the case if the position is
	     * reserved in the next time step. */
	    if (table.isFree(candidateWait)) {
	    	
	    	neighbors.add(new TimedAStarNode(candidateWait, goal,
	    									 trueDistances));
	    	
	    	predecessors.put(candidateWait, node.timedPosition);
	    }
		
	    /* Return a list with 0 to 5 neighbors. */
	    return neighbors;
	}
	
	
	/* Once the goal is reached, the path from the start node has to be
	 * reconstructed. This is done here by starting at the goal node and
	 * using the map "predecessors" to iteratively find the
	 * previous timed position until the start node is found.
	 * 
	 *  "node" is the goal node that has been reached and from which the path
	 * back to timestep 0 shall be reconstructed. */
	private Plan reconstructPath(
			TimedAStarNode node,
			HashMap<TimedPosition, TimedPosition> predecessors,
			ReservationTable table,
			Agent agent) {
				
		TimedPosition timedPosition = node.timedPosition;
		TimedPosition previousTimedPosition;
		
		/* Reserve the goal position permanently. */
		table.reserve(agent, timedPosition, true);
		
		Plan plan = new Plan(agent);
		
		plan.add(timedPosition);
		
		/* Reconstruct the path by following the previous positions.
		 * Also, inform the reservation table. */
		while (predecessors.containsKey(timedPosition)) {
			
			previousTimedPosition = predecessors.get(timedPosition);
			
			/* Reserve the edge used from the previous position. */
			table.reserve(agent, previousTimedPosition, timedPosition);
			
			/* Reserve the previous position. */
			table.reserve(agent, previousTimedPosition, false);
			
			plan.add(previousTimedPosition);
			
			timedPosition = previousTimedPosition;
		} 
		
		/* Remove the last timedPosition of the plan. Since the plan is still
		 * reversed, this is the start position of the task which is already
		 * part of the agent's plan from the previous task or from the
		 * initialization in case it's the first task */
		plan.removeLastPos(false);
		
		/* Since the plan was noted starting at the target, it has to be
		 * reversed. */
		Collections.reverse(plan.plan());
		
		return plan;
	}
}

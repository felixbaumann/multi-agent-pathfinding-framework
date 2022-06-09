/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedAStarNode;
import multi_agent_pathfinding_framework.TimedEdge;
import multi_agent_pathfinding_framework.TimedPosition;
import multi_agent_pathfinding_framework.Timeout;

/* This class provides an A*-search for Conflict Based Search (CBS).
 * That means, an A*-search that honors given constraints. */
public class AStarForCBS {
	
	private Agent agent;
	
	private Traversal traversal;
	
	private Region region;
	
	private Position goal;
	
	private HashSet<VertexConstraint> vertexConstraints;
	
	private HashSet<EdgeConstraint> edgeConstraints;
	
	private HashMap<TimedPosition, TimedPosition> predecessors;
	
	/* Given a map, a start (startTimedPosition) and goal (target) position
	 * as well as a reservation table of blocked positions at certain points
	 * in time, this method performs an A* search from source to target along
	 * the edges of the map. Positions present in the reservation table are
	 * avoided at the given time points.
	 * The admissible heuristic used is the Manhattan distance.
	 * 
	 * If the instance is not solvable, the planning would not terminate since
	 * agents can simply wait forever. Therefore a timeHorizon has to be
	 * provided that limits how long plans are allowed to get.
	 */
	 public Plan A_STAR(EnhancedHighLevelPlan highLevelPlan,			 
			 			Traversal traversal,					
			            Position goal,
			            HashSet<VertexConstraint> vertexConstraints,
			            HashSet<EdgeConstraint> edgeConstraints,		            
			            TimedPosition startTimedPosition,
			            int timeHorizon,                   
                        long runtimeLimit) throws TimeoutException {

		 this.traversal = traversal;
		 this.agent = highLevelPlan.agent;
		 this.region = traversal.region;
		 this.goal = goal;
		 this.vertexConstraints = vertexConstraints;
		 this.edgeConstraints = edgeConstraints;
		 
		 /* In order to reconstruct the path if the search is successful,
		 * we need to keep track of the used edges, i.e. from which timed
		 * position a timed position was reached. */
		this.predecessors = new HashMap<TimedPosition, TimedPosition>();
			
		/* This set will contain all distinct timed positions (x, y, t)
		 * ever created to avoid expanding the same node (same position and
		 * same time) several times. */
		HashSet<TimedPosition> visits
		    = new HashSet<TimedPosition>();
		
		/* Set of nodes ready for expansion. Ordered as a min-heap according
		 * to the fscore (passed time + heuristic value). */
		PriorityQueue<TimedAStarNode> openSet
		    = new PriorityQueue<TimedAStarNode>();
		
		/* Pass the start position and also the goal (target) of the agent
		 * which is needed to compute the heuristic value of the timed
		 * position and thus also of the node. */
		TimedAStarNode node
		    = new TimedAStarNode(startTimedPosition, goal);
		
		visits.add(startTimedPosition);
		
		openSet.add(node);
		
		while (!openSet.isEmpty()) {
			
			/* Cancel the process if it exceeds the runtime limit. */
			Timeout.checkForTimeout(runtimeLimit);
			
			/* Remove the most promising open node (smallest f-score) from
			 * the queue. */
		    TimedAStarNode currentNode = openSet.poll();
			
		    /* Check whether the current node references the goal position. */
		    if (currentNode.position.equals(goal)) {

		    	return reconstructPath(currentNode,
		    						   traversal);
		    }
			
		    /* Obtain all neighbor-nodes. These are nodes reachable by
		     * a single legal action. An action is legal if the target
		     * position is not already reserved and an edge leading from
		     * the current node to the target node exists. */
			ArrayList<TimedAStarNode> neighbors = getNeighbors(currentNode);
			
			for (TimedAStarNode neighbor : neighbors) {
				
				/* Check whether the potential plan gets way too long. */
				if (neighbor.time > timeHorizon) { return null; }
				
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
	private ArrayList<TimedAStarNode> getNeighbors(TimedAStarNode node) {
		
		TimedPosition hereAndNow = node.timedPosition;
	    
		/* All movements that are potentially possible. */
	    TimedPosition candidates[] = new TimedPosition[4];
	    	    	   	    	    
	    /* Movement to the right. */
	    candidates[0] = new TimedPosition(
	    		hereAndNow.x + 1, hereAndNow.y, hereAndNow.t + 1);
	    
	    /* Movement to the left. */
	    candidates[1] = new TimedPosition(
	    		hereAndNow.x - 1, hereAndNow.y, hereAndNow.t + 1);
	    
	    /* Movement upwards. */
	    candidates[2] = new TimedPosition(
	    		hereAndNow.x, hereAndNow.y + 1, hereAndNow.t + 1);
	    
	    /* Movement downwards. */
	    candidates[3] = new TimedPosition(
	    		hereAndNow.x, hereAndNow.y - 1, hereAndNow.t + 1);
	      	    
	    
	    /* This list will store up to 5 neighbors depending on which actions
		 * are possible in the given situation. */
	    ArrayList<TimedAStarNode> neighbors = new ArrayList<TimedAStarNode>();
		
	    
	    /* Check which movements are legal. */
	    for (TimedPosition candidate : candidates) {
	    	
	    	if (movementLegal(hereAndNow, candidate)) {
		    	
		    	neighbors.add(new TimedAStarNode(candidate, goal));
		    	
		    	predecessors.put(candidate, hereAndNow);
		    }
	    }
	    
	    /* Waiting. */
	    TimedPosition candidateWait = new TimedPosition(
	    		hereAndNow.x, hereAndNow.y, hereAndNow.t + 1);	  
	    	    		    		    
	    /* Check whether waiting is legal. Not the case if the position is
	     * reserved in the next time step. */
	    if (!vertexConstraints.contains(
	    		new VertexConstraint(traversal, candidateWait))) {
	    	
	    	neighbors.add(new TimedAStarNode(candidateWait, goal));
	    	
	    	predecessors.put(candidateWait, hereAndNow);
	    }
		
	    /* Return a list with 0 to 5 neighbors. */
	    return neighbors;
	}
	
	
	/* Checks whether a movement from the given source to the given target at
	 * the given time is legal for the given agent. */
	private boolean movementLegal(TimedPosition source,
			                      TimedPosition target) {
		
		/* Is there even an actual edge between those positions? */
		boolean edgeExists
		    = region.internalEdge(new Edge(source.position(),
		    		                       target.position()));
		
		if (!edgeExists) { return false; }
		
		/* Is the target position forbidden for the agent at this time? */
		boolean vertexConstrained
		    = vertexConstraints.contains(new VertexConstraint(traversal,
		    												  target));
		
		/* Is the edge back or forth forbidden for the agent at this time? */
		boolean edgeConstrained
		    = edgeConstraints.contains(new EdgeConstraint(traversal,
		    		new TimedEdge(source, target)))
				|| edgeConstraints.contains(new EdgeConstraint(traversal,
					new TimedEdge(target, source)));
			
		return !vertexConstrained && !edgeConstrained;		
	}
	
	
	/* Once the goal is reached, the path from the start node has to be
	 * reconstructed. This is done here by starting at the goal node and
	 * using the predecessor map to iteratively find the
	 * previous action and timed position until the start node is found.
	 * 
	 *  "node" is the goal node that has been reached and from which the path
	 * back to timestep 0 shall be reconstructed.
	 * 
	 * Note that the regionIndex refers to the index of the region in
	 * the agent's highLevelPlan, not the region's id. */
	private Plan reconstructPath(
			TimedAStarNode node,
			Traversal traversal) {
				
		TimedPosition timedPosition = node.timedPosition;
		TimedPosition previousTimedPosition;		
		
		Plan plan = new Plan(agent);
		
		plan.add(timedPosition);
		
		/* Reconstruct the path by following the previous positions.
		 * Also, inform the reservation table. */
		while (predecessors.containsKey(timedPosition)) {
			
			previousTimedPosition = predecessors.get(timedPosition);			
			
			plan.add(previousTimedPosition);
			
			timedPosition = previousTimedPosition;
		}
		
		/* Since the plan was noted starting at the target, it has to be
		 * reversed. */
		Collections.reverse(plan.plan());
				
		return plan;		
	}
}

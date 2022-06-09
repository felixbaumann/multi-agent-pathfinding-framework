/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;

import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedEdge;
import multi_agent_pathfinding_framework.TimedPosition;
import multi_agent_pathfinding_framework.Timeout;

/* Conflict-Based Search implementation for low level pathfinding within
 * a given region. 
 * 
 * Conflict-Based Search has been introduced by
 * 
 * G. Sharon, R. Stern, A. Felner, and N. Sturtevant in 
 * "Conflict-based search for optimal multi-agent path finding" in
 * Proceedings of the Twenty-Sixth AAAI Conference on Artificial Intelligence,
 * 2012. */
public class ConflictBasedSearch {
	
	/* Tree whose nodes contain constraints inherited from the parent node as
	 * well as a (possibly conflicting) solution. */
	private PriorityQueue<ConstraintTreeNode> tree;	
	
	/* Limits the length of individual plans. */
	private int timeHorizon;
	
	/* Limits the runtime of the entire MAPF process. */
	private long runtimeLimit;
	
	/* Traversals ready to be planned. */
	private ArrayList<Traversal> traversals;
	
	/* Maps traversals to the index they have in the traversals list above. */
	private HashMap<Traversal, Integer> localTraversalIndex;
	
	
	/* Main function.
	 * 
	 * Performs a conflict based search that includes all the given traversals
	 * but does not change anything at any plan before the given startTime.
	 * 
	 * traversals		are only the traversals in this region */
	public CommonPlan conflictBasedSearch(
			ArrayList<Traversal> traversals,
			int startTime,
			int timeHorizon,
			long runtimeLimit) throws TimeoutException {		
		
		this.traversals = traversals;
		this.localTraversalIndex = createLocalTraversalIndex(traversals);
		this.timeHorizon = timeHorizon;
		this.runtimeLimit = runtimeLimit;
		this.tree = new PriorityQueue<ConstraintTreeNode>();		
	
		/* Create a root node. A node contains a plan for each traversal of
		 * this region. That means, if an agent traverses this region twice,
		 * it will get two plans. */
		ConstraintTreeNode root
		    = new ConstraintTreeNode(null, null, traversals);
		
		for (Traversal traversal : traversals) {
				
			root.solution.addPlan(traversal.plan);
		}
		
		root.cost = root.solution.planLength();
		
		tree.add(root);
		
		while (!tree.isEmpty()) {
			
			/* Cancel the process if it exceeds the runtime limit. */
			Timeout.checkForTimeout(runtimeLimit);
			
			ConstraintTreeNode node = tree.poll();
			
			Conflict conflict = validate(node, startTime);
			
			if (conflict == null) { return node.solution; }


			/* Define a constraint for the first of the conflicting
			 * agents, let him replan and add the resulting new node
			 * to the tree. */
			createChildNodeFromConflict(node, conflict, true);										

			/* Define a constraint for the second of the conflicting
			 * agents, let him replan and add the resulting new node
			 * to the tree. */
			createChildNodeFromConflict(node, conflict, false);
		}
		return null;
	}
		
	
	/* This function creates a child node in the ConstraintTree
	 * given a parent node and a conflict.
	 * The parameter firstAgent defines whether the first agent (true) or
	 * the second agent (false) of the conflict shall get an additional
	 * constraint. */
	private void createChildNodeFromConflict(ConstraintTreeNode node,
			                                 Conflict conflict,
			                                 boolean firstAgent)
			                                		 throws TimeoutException {
				
		/* Define a constraint for the first of the conflicting agents,
		 * let him replan and add the resulting new node to the tree. */						
		Constraint constraint = createConstraint(conflict, firstAgent);
		
		ConstraintTreeNode child
		    = new ConstraintTreeNode(node, constraint, traversals);				
		
		/* Use the low-level to replan for the given conflicting agent. */
		Plan plan = pathfinding(constraint.traversal.highLevelPlan,
				                child.vertexConstraints,
				                child.edgeConstraints,
				                constraint.traversal,
				                conflict.time - 1);
				
		if (plan == null) { return; }
		
		/* Replace the plan of this traversal in the solution. */
		child.solution.set(localTraversalIndex.get(
				constraint.traversal), plan);
		
		child.cost = child.solution.planLength();
		
		tree.add(child);
	}
	
	
	/* Returns the time of the latest TimedPosition in any of the individual
	 * plans of the given CommonPlan. */
	private int getLastTime(CommonPlan plans) {
		
		int max = 0;
		
		for (Plan plan : plans.commonPlan()) {
			
			max = Math.max(max, plan.lastTimedPosition(0).t);
		}
		return max;
	}
	
	
	/* Find the first conflict after the given startTime in the solution of
	 * the given node.
	 * Return null if there is no conflict. */
	private Conflict validate(ConstraintTreeNode node, int startTime) {		
		
		CommonPlan solution = node.solution;
		
		int lastTimeStamp = getLastTime(solution);
		
		for (int time = startTime; time < lastTimeStamp; time++) {
		
			/* Note which positions and edges are used in the current time
			 * step and by which traversal. */
			HashMap<Position, Traversal> positionClaims
			    = new HashMap<Position, Traversal>();
			
			HashMap<Edge, Traversal> edgeClaims
			    = new HashMap<Edge, Traversal>();
			
			/* Consider all traversals of this region. */
			for (int index = 0; index < traversals.size(); index++) {		
				
				Traversal traversal = traversals.get(index);
				
				Plan plan = solution.get(index);
				
				/* Position of the current traversal at the current time. */
				Position pos;
				
				/* If this is an agent's last traversal, assume it will stay
				 * at its target. */
				if (traversal.isGoalRegion) {
					
					pos = plan.position(time, true);
				}
				
				else {
					
					pos = plan.position(time, false);
				}

				if (pos == null) { continue; }
								
				/* If a previous traversal already claimed this position,
				 * there's a conflict. */
				if (positionClaims.containsKey(pos)) {
					
					return new VertexConflict(positionClaims.get(pos),
											  traversal,
											  new TimedPosition(pos, time));
				}				
				
				/* Otherwise claim the position. */	
				positionClaims.put(pos, traversal);
				

				/* Position of the current traversal at the next time. */
				Position nextPos = plan.position(time + 1, false);
				
				if (nextPos == null) { continue; }
				
				/* Reverse of the edge used next by the current traversal.
				 * There's no necessity to check the non-reversed edge since
				 * this would have already caused a vertex conflict above. */
				Edge edge = new Edge(pos, nextPos);
				
				Edge revEdge = new Edge(nextPos, pos);
				
				/* If a previous traversal already claimed this edge, there's
				 * a conflict. */
				if (edgeClaims.containsKey(edge) || 
					edgeClaims.containsKey(revEdge)) {
					
					Traversal trav = (edgeClaims.containsKey(edge) ? 
						edgeClaims.get(edge) : edgeClaims.get(revEdge));
															
					return new EdgeConflict(trav,
											traversal,
											new TimedEdge(
												new TimedPosition(pos, time),
												new TimedPosition(nextPos,
																  time + 1)));
				}			
				
				else { edgeClaims.put(edge, traversal); }
			}
		}		
		return null;
	}

	
	/* Identifies start and target of the agent of the given LowLevelPlan
	 * in the current region and uses A* to compute a plan if possible. */
	private Plan pathfinding(EnhancedHighLevelPlan highLevelPlan,
			HashSet<VertexConstraint> vertexConstraints,
			HashSet<EdgeConstraint> edgeConstraints,
			Traversal traversal,
			int startTime) throws TimeoutException {
		
		AStarForCBS aStar = new AStarForCBS();
		
		/* Old start time and place. */
		TimedPosition start = traversal.plan.plan().get(0);
		
		/* The agent is not allowed to replan this traversal entirely.
		 * Any steps up until 'startTime' are fixed. Note this plan here. */
		ArrayList<TimedPosition> fixedPlanPart
		    = new ArrayList<TimedPosition>();
		
		/* Time when the old plan for this traversal started. */
		int time = start.t;
		
		
		/* The new startTime may actually be earlier than the old one.
		 * Then it's impossible to find a valid plan for this traversal.
		 * The agent would have to go through its previous region faster. */
		if (startTime < time) { return null; }
		
		
		/* Copy part of the old plan to match the new startTime for
		 * replanning. This includes at least the old start timedPosition. */
		while (startTime >= time) {
			
			/* The part of the plan that was already executed is fixed
			 * and is not open for replanning anymore.
			 * Get those positions and append them to the fixed plan. */
			Position position
			    = traversal.plan.position(time, traversal.isGoalRegion);
			
			/* If in the old plan, the agent would have entered, traversed
			 * and left the region before this new start time, the agent
			 * can't conflict with anyone nor can parts of his plan be
			 * replanned. The entire old plan is fixed. Thus, just
			 * return the old plan. */
			if (position == null) { return traversal.plan; }
			
			/* If this is the agent's goal region and its plan finished
			 * before startTime, fill it up at the end to make it possible
			 * to go out of the way. */
			fixedPlanPart.add(new TimedPosition(position, time));
			
			time++;
		}
		
		/* (Possibly) redefine the start for the new search.
		 * That happens if the new start time is later than the old one. */
		start = last(fixedPlanPart);
		
		Position target = traversal.target;
		
		Plan newPlan = aStar.A_STAR(highLevelPlan,
							traversal,				    
						    target,
						    vertexConstraints,
						    edgeConstraints,
						    start,
						    timeHorizon,
						    runtimeLimit);
		
		if (newPlan == null) { return null; }
		
		/* Combine plans. Do not include the start position of the plan
		 * computed by A* since it's already been added in the while-loop
		 * above. */
		for (int index = 1; index < newPlan.length(); index++) {
			
			fixedPlanPart.add(newPlan.plan().get(index));			
		}
		return new Plan(traversal.agent, fixedPlanPart);
	}   

	
	/* Given a conflict and the information whether the first agent should be
	 * constrained (firstAgent=true) or the second one (firstAgent=false),
	 * this function creates a VertexConstraint or EdgeConstraint as
	 * appropriate. */
	private Constraint createConstraint(Conflict conflict,
			                            boolean firstAgent) {
		
		/* Vertex. */
		if (conflict instanceof VertexConflict) {
			
			return new VertexConstraint(
				firstAgent ? conflict.traversal1 : conflict.traversal2,
					((VertexConflict) conflict).timedPosition);
		}
		
		/* Edge. */
		return new EdgeConstraint(
				firstAgent ? conflict.traversal1 : conflict.traversal2,
						((EdgeConflict) conflict).timedEdge);				
	}
	
	
	/* Maps traversals to the index they have in the traversals list. */
	private HashMap<Traversal, Integer> createLocalTraversalIndex(
			ArrayList<Traversal> traversals) {
		
		HashMap<Traversal, Integer> map = new HashMap<Traversal, Integer>();
		
		for (int index = 0; index < traversals.size(); index++) {
			
			map.put(traversals.get(index), index);
		}
		return map;
	}
	
	
	/* Return the last element. */
	private TimedPosition last(ArrayList<TimedPosition> plan) {
		
		return plan.get(plan.size() - 1);
	}
}

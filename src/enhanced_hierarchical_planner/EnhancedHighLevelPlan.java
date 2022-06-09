/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.ArrayList;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedPosition;

/* A HighLevelPlan of a specific agent represents the sequence of regions this
 * agent has to traverse on his way from its start position to its goal
 * position.
 * Since a region may be traversed multiple times, the plan is instead noted
 * as a sequence of traversals to avoid duplicates.
 * Moreover, the HighLevelPlan includes the information which RegionEdges are
 * used to move from one region to the next. This provides the regional
 * planner with the start position (target of a RegionEdge) and the goal
 * position (source of the next RegionEdge) of the agent with respect to this
 * region. */
public class EnhancedHighLevelPlan {

	public final Agent agent;
	
	/* An agent may traverse a number of regions. */
	public final ArrayList<Traversal> plan
	    = new ArrayList<Traversal>();
	
	/* Same plan as stored in the traversals. */
	public Plan lowLevelPlan;
	
	
	/* Given a sequence of low level positions that leads a given agent from
	 * its start to its overall goal position, this constructor creates
	 * a HighLevelPlan that divides the position sequence into multiple
	 * traversals, each of which describes how the agent should traverse
	 * a specific region. */
	public EnhancedHighLevelPlan(RegionContainer graph,
								 Agent agent,
								 ArrayList<Position> positions) {
		this.agent = agent;
		
		if (positions == null) { return; }
		
		if (positions.size() == 0) { return; }
		
		lowLevelPlan = new Plan(agent, positions, 0);
		
		/* Initial position and region. */
		Position pos = positions.get(0);
		
		Region region = graph.region(pos); 				
		
		/* Positions of the current traversal. */
		ArrayList<Position> traversalPositions = new ArrayList<Position>();
		
		traversalPositions.add(pos);
		
		int traversalIndex = 0;	
		
		/* Time when the current traversal starts. */
		int startTime = 0;
		
		/* For each following position in the plan, check whether it
		 * belongs to a new region and if so, add this region as a new
		 * traversal to the plan. */
		for (int index = 1; index < positions.size(); index++) {
			
			Position nextPos = positions.get(index);
			
			Region nextRegion = graph.region(nextPos);
						
			/* nextPos lies in a new region. */
			if (!region.equals(nextRegion)) {
			
				/* Wrap up the region that's just been completed. */
				plan.add(new Traversal(agent, region,
						 traversalPositions,
					     traversalIndex,
					     startTime,
					     false,
					     this));
				
				startTime = index;
			
				region = nextRegion;
				
				traversalIndex++;
                
				/* Start a new traversal. */
				traversalPositions = new ArrayList<Position>();
			}		
			traversalPositions.add(nextPos);
		}
		
		/* Create the last traversal leading to the goal. */
		plan.add(new Traversal(agent, region,
					 traversalPositions,
				     traversalIndex,
				     startTime,
				     true,
				     this));
		
		
		/* Create successor and predecessor pointers in the traversals. */
		for (int index = 0; index < plan.size() - 1; index++) {
			
			plan.get(index).successor = plan.get(index + 1);
			plan.get(index + 1).predecessor = plan.get(index);
		}		
	}	

	
	/* Number of traversals. */
	public int length() { return plan.size(); }
	
	
	/* The plan for the given traversal was changed to newPartialPlan.
	 * However, the plan noted in traversal is still the old one and
	 * more importantly, the overall lowLevelPlan also contains
	 * the old plan.
	 * In order to replace the old part within the lowLevelPlan with the
	 * new one, we have to consider their different lengths. */
	public void insertPlan(Traversal traversal,
			               Plan newPartialPlan) {
		
		/* Size which the previous plan for this traversal had. */
		int oldPartialPlanSize = traversal.plan.plan().size();
		
		/* Defines by how many steps the future part of the plan is delayed.
		 */
		int delay = newPartialPlan.length() - oldPartialPlanSize;
		
		/* If the plan is consistent, the length of all the preceding
		 * traversal-plans (including the start at t=0) sum up to
		 * the start time of the given traversal. */
		int precedingPlanLength =  newPartialPlan.plan().get(0).t;

		/* It's more efficient to just create a new plan with the prefix
		 * of the old lowLevelPlan, followed by the newPartialPlan and
		 * finally the delayed suffix of the plan (linear time) than
		 * removing the oldPartialPlan from the existing lowLevelPlan
		 * and inserting the newPartialPlan since this would require
		 * a lot of shifting (quadratic time). */
		Plan newLowLevelPlan = new Plan(traversal.agent);
		
		/* Copy the prefix. */
		for (int index = 0; index < precedingPlanLength; index++) {
			
			newLowLevelPlan.add(lowLevelPlan.plan().get(index));;
		}
		
		/* Append the newPartialPlan. */
        for (int index = 0; index < newPartialPlan.length(); index++) {
			
			newLowLevelPlan.add(newPartialPlan.plan().get(index));;
		}
        
        /* Append the delayed suffix. */
        for (int index = precedingPlanLength + oldPartialPlanSize;
        	 index < lowLevelPlan.length(); index++) {
        	
        	TimedPosition old = lowLevelPlan.plan().get(index);
        	
        	TimedPosition delayed
        	    = new TimedPosition(old.position(), old.t + delay);
        	
        	newLowLevelPlan.add(delayed);
        }

        /* Replace the old overall plan by the one we just constructed. */
		lowLevelPlan = newLowLevelPlan;	
	
		
		/* Go forwards and update the time stamps.*/
		Traversal currentTraversal = traversal.successor;
		
        while (currentTraversal != null) {
			
			currentTraversal.plan.delay(delay, 0);
			
			currentTraversal = currentTraversal.successor;
		}
	}
	
	
	/* Checks whether this plan leads through the given region. */
	public boolean containsRegion(Region givenRegion) {
		
		for (Traversal traversal : plan) {
			
			if (traversal.region.equals(givenRegion)) { return true; }
		}
		return false;
	}
		
	
	public String print() {
		
		String string = "\nRegions in this HighLevelPlan: ";
		
		for (Traversal traversal : plan) {
			
			string += (traversal.region.regionIndex + ", ");
		}		
		return string;
	}
}

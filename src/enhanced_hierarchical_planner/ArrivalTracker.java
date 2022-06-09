/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.HashMap;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedPosition;

/* This class tracks when goal positions are reached.
 * From this moment on, no other agent should ever be at that position.
 * To determine whether or not it is indeed a different agent,
 * the class also tracks which agent belongs to a given goal position. */
public class ArrivalTracker {

	/* Maps each goal position to the agent whose goal it is. */
	private final HashMap<Position, Agent> goalAgents
	    = new HashMap<Position, Agent>();
	
	/* Maps each goal position to the time the respective agent reaches it. */
	private final HashMap<Position, Integer> goalTimes
	    = new HashMap<Position, Integer>();
	
		
	public ArrivalTracker(Agent[] agents) {
		
		/* Note the overall goals of all agents. */
	    for (Agent agent : agents) {
	    	
	    	goalAgents.put(agent.task().targets()[0], agent);	    	
	    }		
	}
	
	
	/* Use this function to inform the ArrivalTracker that the given
	 * timedPosition is the time and place some agent reaches its goal. */
	public void goalReachedAt(TimedPosition timedPosition) {
		
		goalTimes.put(timedPosition.position(), timedPosition.t);
	}
	
	
	/* Returns whether the given position is the overall goal of the given
	 * agent. */
	public boolean goalReached(Agent agent, Position position) {
		
		if (goalAgents.containsKey(position)) {
			
			return goalAgents.get(position).equals(agent);
		}		
		return false;
	}
	
	
	/* Given an agent A. Checks whether there is a different agent A' whose
	 * goal is the given position and reached it before the given agent A.
	 * Since this someone won't move away from its goal, agent A must not
	 * occupy the position at the given time.*/
	public VertexConstraint goalRestriction(Traversal traversal,
											TimedPosition timedPosition) {
		
		/* The given position is not anyone's goal. No goal conflict. */
		if (!goalAgents.containsKey(timedPosition.position())) {
			
			return null;
		}
		
		/* The given position is the given agent's own goal. Obviously this
		 * does not conflict with itself. */
		if (goalAgents.get(timedPosition.position()).equals(
				traversal.agent)) {
			
			return null;
		}
		
		/* The given position is someone's goal but this someone's arrival is
		 * not fixed yet. */
		if (!goalTimes.containsKey(timedPosition.position())) {
			
			return null;
		}
		
		/* The given position is someone's goal but this someone will not
		 * have reached it by the given time. */
		if (goalTimes.get(timedPosition.position()) > timedPosition.t) {
			
			return null;
		}				
		
		/* Someone else already occupied the given position permanently 
		 * from an earlier time on. */
		return new VertexConstraint(traversal, timedPosition);
	}
}

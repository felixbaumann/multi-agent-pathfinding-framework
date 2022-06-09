/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import java.util.ArrayList;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;


/* Represents the traversal of a specific agent of a specific region at a
 * certain time. Since an agent may traverse the same region multiple times,
 * there may be multiple traversals for the same region and agent. */
public class Traversal {		
	
	public final Agent agent;
	
	public final Region region;
	
	/* Position where the agent enters this region.
	 * Alternatively, the start position of the agent if this is its first
	 * traversal. */
	public final Position start;
	
	/* Last position before the agent leaves this region.
	 * Alternatively, the goal position of the agent if this is its last
	 * traversal. */
	public final Position target;
	
	/* Index of this traversal in the agent's high level plan. */
	public final int traversalIndex;
	
	/* True if this is the goal region AND the traversal in which the goal is
	 * finally reached. */
    public final boolean isGoalRegion;    
    
    /* A plan that describes the traversal, i.e. that leads the agent from
     * start to target. */
    public Plan plan;
        
    /* Pointer towards the plan this traversal belongs to. */
    public EnhancedHighLevelPlan highLevelPlan;
    
    public Traversal predecessor = null;
    public Traversal successor = null;
	
	
	public Traversal(Agent agent,
					 Region region,
					 ArrayList<Position> positions,
					 int traversalIndex,					 
					 int startTime,
					 boolean isGoal,
					 EnhancedHighLevelPlan highLevelPlan) {
		
		this.agent = agent;
		this.region = region;
		this.start = positions.get(0);
		this.target = positions.get(positions.size() - 1);
		this.traversalIndex = traversalIndex;
		this.isGoalRegion = isGoal;
		this.plan = new Plan(agent, positions, startTime);
		this.highLevelPlan = highLevelPlan;
	}	
	
	
	public void setPlan(Plan plan) {
		
		this.plan = plan; 
	}
	
	
	@Override
	public boolean equals(Object other) {
		
		if (!(other instanceof Traversal)) return false;		
		
		return ((Traversal) other).agent.equals(this.agent)
				&& ((Traversal) other).traversalIndex == this.traversalIndex;
	}
	
	
	@Override
	public int hashCode() { 
		
		return agent.hashCode() + 123 * traversalIndex;
	}
}

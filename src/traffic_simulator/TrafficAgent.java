package traffic_simulator;

import java.util.ArrayList;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;


/* Agent class variant for the traffic simulator. 
 * The main difference to a regular agent is that this class contains
 * the agent's plan - both a timed and an untimed version.
 * The untimed version is the one determined by a simple A* search.
 * The timed version is the actual sequence of timed positions that
 * the agent experienced on its way to the goal. */
public class TrafficAgent extends Agent {

	/* Describes whether the agent is still on its way to its goal. */
	public boolean active = true;
	
	/* Sequence of positions that leads the agent from its start to its
	 * goal position. This sequence will be computed before the execution
	 * of the plan. */
	public ArrayList<Position> untimedPlan;
	
	/* Sequence of timed positions undertaken to lead the agent from its
	 * start to its goal. This sequence is created during execution.
	 * The same position (but not timed position) may occur several times
	 * if waiting turns out to be necessary. */
	public Plan timedPlan;
	
	/* "position" field of the parent class must not be changed during
	 * execution since it's required to remain equal to the start position
	 * for the validator. */
	public Position currentPosition;
	
	public Position goalPosition;
	
	/* Points to the index of the current Position in the untimed plan. */
	public int untimedPlanPointer = 0;
	
	/* Given a regular agent from a scenario, create an equivalent
	 * TrafficAgent. */
	public TrafficAgent(Agent agent) {
		
		super(agent.id(), agent.name(), agent.position(), agent.task());
		
		currentPosition = agent.position();
		
		goalPosition = agent.task().targets()[0];
		
		timedPlan = new Plan(agent);
	}
		
	
	/* Yields the next position the agent planned to occupy according
	 * to its untimed plan.
	 * Make sure the agent did not already reach its goal since this
	 * would throw an index out of bounds exception. */
	public Position nextPosition() {
		
		return untimedPlan.get(untimedPlanPointer + 1);
	}		
}

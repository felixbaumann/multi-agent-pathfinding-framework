package traffic_simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

import multi_agent_pathfinding_framework.A_STAR;
import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.Scenario;
import multi_agent_pathfinding_framework.TimedPosition;
import multi_agent_pathfinding_framework.Timeout;

/* Simple fully decentralized MAPF planner where each agent computes
 * an untimed shortest path to its goal and executes the next step in
 * this path in each time step if possible and waits otherwise.
 * Scales extremely well with respect to the number of agents.
 * Complete if there are no undirected edges except any that
 * lead into a deadend, the sets of start- and goal positions
 * are disjunct and there's a path from each agent's start to its
 * goal position that does not pass through any other agent's goal
 * position. */
public class TrafficSimulator {

	
	public CommonPlan trafficSimulation(Scenario scenario,
									   int timeHorizon,			   
			                           long runtimeLimit)
			                        		   throws TimeoutException {

		/* Once all agents reach their goal this will be set to true. */
		boolean finished = false;
		
		TrafficAgent agents[] = createTrafficAgents(scenario.agents());

		HashMap<Position, TrafficAgent> occupiedPositions
		    = startPositions(agents);
				
		/* Do an untimed A* search for each agent and store the resulting
		 * plan in the TrafficAgent.
		 * If this fails, at least one start-goal pair is unconnected. */
		if (!formUntimedPlans(scenario, agents)) { return null; }		
		
		
		/* The start position has to be part of the final timed plan. */
		for (TrafficAgent agent : agents) {
			
			agent.timedPlan.add(new TimedPosition(agent.position(), 0));
		}		
		
		/* Time step. */
		int now = 1;
		
		/* Stop once all agents reached their goal or the time horizon is
		 * reached. */
		while(!finished && now < timeHorizon) {		
		
			Timeout.checkForTimeout(runtimeLimit);
			
			/* Once there's no agent left that can do its next move in
			 * this time step, this will be false and the loop below stops. */
			boolean movementActive = true;
		
			/* Check all agents repeatedly whether they can do their next
			 * step. Once everyone (that can) moved, note the actions and
			 * proceed with the next time step.*/
			while (movementActive) {
			
				/* Assume nobody will move anymore. */
			    movementActive = false;
			
			    for (TrafficAgent agent : agents) {
			    	
			    	/* Agent already reached its goal or moved in this time
			    	 * step. */
			    	if (!agent.active) { continue; }
			    	
			    	/* Next position in the agents untimed plan. */
			    	Position target = agent.nextPosition();
			    	
			    	/* Next position of the agent is free. */
			    	if (!occupiedPositions.containsKey(target)) {
			    		
			    		occupiedPositions.remove(agent.currentPosition);
			    		occupiedPositions.put(target, agent);
			    		agent.currentPosition = target;
			    		agent.untimedPlanPointer++;
			    		agent.active = false;
			    		movementActive = true;
			    	}			    				    	
			    }							
			}			
			
			/* If there are any cyclic dependencies of agents that did not
			 * move in this time step already, move them all by one step.
			 */
			cyclicSteps(agents, occupiedPositions);
			
			/* For each agent, note its movement or waiting in its timed plan
			 * and check whether it' still active (or reached its goal.). */
			protocolMovements(agents, now);						
		    
		    /* Check whether all agents are inactive now. */
		    finished = planningComplete(agents);
		    
		    /* Next time step. */
		    now++;
		}				
		
		/* All agents reached the goal. */
		if (finished) { return commonPlan(agents); }
		
		/* At least one agent did not make it before the time horizon
		 * was reached. */
		else { return null; }
	}
		
	
	/* There may be cyclic dependencies among the agents, meaning
	 * four or more agents are standing in a cylce without any gaps and
	 * eachs wishes to do a step forward in the cycle. Since nobody can
	 * start, this is a deadlock.
	 * We have to identify these cycles and move the agents accordingly. */
	private void cyclicSteps(TrafficAgent agents[],
			HashMap<Position, TrafficAgent> occupiedPositions) {
						
		for (TrafficAgent agent : agents) {
			
			/* Agents at a goal should not be in anyone's way.
			 * Agents that already made a move in this time step
			 * are apparently not part of a cycle. */
			if (!agent.active) { continue; }
			
			/* Someone has to be in this agent's way. And someone may be in
			 * that agent's way and so on... */			
			TrafficAgent currentAgent = agent;			
			
			/* Keep track of which agents are part of this blockade. */
			HashSet<TrafficAgent> blockingAgents
			    = new HashSet<TrafficAgent>();
			
			blockingAgents.add(currentAgent);
			
			while (true) {
				
			    currentAgent
			        = occupiedPositions.get(currentAgent.nextPosition());
			
			    /* There's an agent in the way who made a move.
			     * Thus, this is not a cycle. */
			    if (!currentAgent.active) { break; }
			    			    
			    /* There's an agent blocking whom we already identified as
			     * blocked. That's a cycle. */
			    if (blockingAgents.contains(currentAgent)) {
			    	
			    	doCycleStep(currentAgent, occupiedPositions);
			    	break;
			    }
			    /* The agent did not make a move and is blocked itself.
			     * Continue the line to find the agent blocking it. */
			    blockingAgents.add(currentAgent);
			}
		}
	}
	
	
	
	/* There's a cycle of agents that block each other.
	 * Move each agent one step forward manually. */
	private void doCycleStep(TrafficAgent agent,
			HashMap<Position, TrafficAgent> occupiedPositions) {

		
		TrafficAgent currentAgent = agent;
		
		/* Move the agents one by one. Stop once we reach the given initial
		 * agent again.*/
		do {	
			/* The next agent is the one blocking the current one. */
			TrafficAgent nextAgent
			    = occupiedPositions.get(currentAgent.nextPosition());
			
			/* Move the current agent manually. */
		    Position target = currentAgent.nextPosition();
		    
		    /* No need to remove the previously claimed position from the map
		     * since it'll be claimed by another agent of the cycle. */
    	    occupiedPositions.put(target, currentAgent);
    	    
    		currentAgent.currentPosition = target;
    		
    		currentAgent.untimedPlanPointer++;
    		
    		currentAgent.active = false;
    		
    		/* Proceed with the blocking agent. */
    		currentAgent = nextAgent; 
    		
    	} while (!agent.equals(currentAgent));
	}
			
	
	/* For each agent in the scenario, perform an untimed A* search from start
	 * to goal and note the resulting plan in the respective trafficAgent's
	 * plan. Do not include positions that are goal
	 * positions of other agents. Returns true if successful. */
	private boolean formUntimedPlans(Scenario scenario,
									 TrafficAgent trafficAgents[]) {
		
		/* Collect the goal positions. Those are off limits for all agent
		 * but the one whose goal it is. */
		HashSet<Position> goalPositions = goalPositions(trafficAgents);
		
		A_STAR star = new A_STAR();
		
		for (int index = 0; index < trafficAgents.length; index++) {
			
			Agent agent = scenario.agents()[index];
			
			/* The agent is allowed to (and has) to include its own goal
			 * position in the plan. Thus, remove it temporarily from the
			 * restricted position set. */
			Position goal = trafficAgents[index].goalPosition;
			
			goalPositions.remove(goal);
			
			/* Form an untimed plan that does not include goal positions
			 * of other agents. */
			ArrayList<Position> plan
				= star.AStar(scenario.map(),
							 agent.position(),
							 goal,
							 goalPositions);
			
			/* Add the goal again so the next agent can't step on it. */
			goalPositions.add(goal);
			
			/* Planning successful. */
			if (plan != null) {
				
				/* Add the start position. */
				plan.add(0, agent.position());
				
				trafficAgents[index].untimedPlan = plan;				
			}
			
			/* Impossible to reach the goal from the agent's initial
			 * position. */
			else { return false; }			
		} 
		return true;
	}
	
	
	/* For each agent, note its movement or waiting in its timed plan and 
	 * check whether it' still active (or reached its goal.). */
	private void protocolMovements(TrafficAgent agents[], int now) {
		
		/* Prepare all agents for the next time step and note
		 * their movement or waiting in their timed plans. */
		for (TrafficAgent agent : agents) {
		
			/* If the agent is not at its goal, it should continue its way. */
			if (!atGoal(agent)) {
				
				agent.active = true;
			}
			
            /* If the agent reached its goal at an earlier time, don't
             * add anything to its plan anymore. */
			if (!atGoal(agent) || moved(agent, now)) {
				
				agent.timedPlan.add(
						new TimedPosition(agent.currentPosition, now));				
			}								
		}
	}
		
	
	/* Return true if the agents current position is different from its
	 * previous position. */
	private boolean moved(TrafficAgent agent, int now) {
		
		/* If this is the first time step, the protocol is empty.
		 * Compare the agents current position to its initial one. */
	    if (agent.timedPlan.plan().isEmpty()) {
	    	
	    	return !agent.currentPosition.equals(agent.position()); 
	    }
	    /* Otherwise, compare the agent's current position to its previous
	     * one in the protocol. */
	    else {
	    	
	    	return !agent.currentPosition.equals(
	    			agent.timedPlan.lastTimedPosition(now).position());
	    }
		
	}
			
	
	/* Is the agent at its goal? */
	private boolean atGoal(TrafficAgent agent) {
		
		return agent.currentPosition.equals(agent.goalPosition);
	}
	
		
	/* Returns true if all agents are inactive. */
	private boolean planningComplete(TrafficAgent agents[]) {
		
		for (TrafficAgent agent : agents) {
	    	
	    	if (agent.active) { return false; }
	    }
		return true;
	}
	
	
	/* Given a list of TrafficAgents, merge all their timed plans
	 * to a CommonPlan. */
	private CommonPlan commonPlan(TrafficAgent agents[]) {
		
		CommonPlan commonPlan = new CommonPlan();
		
		for (TrafficAgent agent : agents) {
			
			commonPlan.addPlan(agent.timedPlan);
		}
		return commonPlan;
	}
	
	
	/* Convert a given array of Agents to an array of equivalent
	 * TrafficAgents. */
	private TrafficAgent[] createTrafficAgents(Agent agents[]) {
		
		TrafficAgent trafficAgents[] = new TrafficAgent[agents.length];
		
		for (int index = 0; index < agents.length; index++) {
			
			trafficAgents[index] = new TrafficAgent(agents[index]);
		}
		return trafficAgents;
	}
	
	
	/* Return a map with the start positions of the given agents. */
	private HashMap<Position, TrafficAgent> startPositions(
			TrafficAgent agents[]) {
		
		HashMap<Position, TrafficAgent> occupiedPositions
		    = new HashMap<Position, TrafficAgent>();
		
		for (TrafficAgent agent : agents) {

			occupiedPositions.put(agent.position(), agent);
		}		
		return occupiedPositions;
	}
	
	
	/* Return a set with the goal positions of the given agents. */
	private HashSet<Position> goalPositions(
			TrafficAgent agents[]) {
		
		HashSet<Position> goalPositions
		    = new HashSet<Position>();
		
		for (TrafficAgent agent : agents) {

			goalPositions.add(agent.goalPosition);
		}		
		return goalPositions;
	}
}

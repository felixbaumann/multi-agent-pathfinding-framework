/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package token_passing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;

import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.DistanceTableException;

import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Map;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.Scenario;
import multi_agent_pathfinding_framework.Task;
import multi_agent_pathfinding_framework.TimedPosition;
import multi_agent_pathfinding_framework.Timeout;
import multi_agent_pathfinding_framework.TrueDistances;

/* Token Passing (TP) MAPF algorithm.
 * 
 * Proposed by Hang Ma, T.K. Satish Kumar, Jiaoyang Li and Sven Koenig in
 * "Lifelong Multi-Agent Path Finding For Online Pickup And Delivery Tasks"
 * appearing in
 * Proc. of the 16th International Conference on
 * Autonomous Agents and Multiagent Systems (AAMAS 2017),
 * S. Das, E. Durfee, K. Larson, M. Winiko (eds.),
 * May 8{12, 2017, São Paulo, Brazil. 
 */
public class TokenPassing {

	private final Scenario scenario;
	
	/* Endpoints are pickup or delivery locations of tasks, initial positions
	 * of agents as well as dedicated parking spots. */
	private HashSet<Position> endpoints = new HashSet<Position>();
	
	/* Token passing uses true distances as heuristic for pathfinindig.
	 * Since only paths to endpoints are required, the true distance from
	 * every position on the map to every endpoint is computed in an offline
     * preprocessing stage.
     * There's an implementation for a pair of positions
     * (arbitrary position on the map, some endpoint) which is an edge.
     * It's not relevant here whether such an edge actually exists on the
     * map. */
	private HashMap<Edge, Integer> distances = new HashMap<Edge, Integer>();
	
	private Token token;
	
	/* Tracks time steps. */
	private int now = 0;
	
	/* Last time in the scenario when a task becomes available. */
	private final int taskTimeHorizon;
	
	
	public TokenPassing(Scenario scenario, long runtimeLimit)
			throws TimeoutException {
		
		this.scenario = scenario;
		
		identifyEndpoints();
		
		computeDistances(scenario.map(), runtimeLimit);
		
		this.token = new Token(scenario);
		
        this.taskTimeHorizon = lastTaskTime(scenario.tasks());
	}
	
	
	/* Function performing the online token passing.
	 * Endpoints are already identified and distances are already computed. */
	public CommonPlan tokenPassing(int timeHorizon, long runtimeLimit)
			throws TimeoutException, DistanceTableException {
		
		/* Initialize the token for every agent with the
		 * trivial path: just stay where you are forever. */
		initializeTrivialPaths();		
		
		/* Cancel the execution once the time limit is reached. */
		while (now <= timeHorizon) {

			addNewTasks(now);
			
			HashSet<Agent> freeAgents = token.freeAgentsCopy();
			
			/* Let each agent try to get a task.
			 * If he fails, let the others try before assigning
			 * the trivial path or a path to an endpoint to make sure he won't
			 * be in someone with an actual job's way. */
			for (Agent agent : freeAgents) {
				
				/* Try to plan for the best available task.
				 * If successful proceed with the next agent. */
				if (planForTask(agent, timeHorizon, runtimeLimit)) {
					
					continue;
				}
				
				// DEBUG Put this block back in
                /* Since planning for a task failed, check whether the agent
                 * can just stay where it is.
                 * Note that the blocking() test already releases all of the
				 * agent's reservations and cancels its future plan.*
				if (!blocking(agent)) {
					
					setTrivialPath(agent);
					
					continue;
				}
				*/
				// END DEBUG
			
			
			// DEBUG Remove this block
			}	
						
			/* Now deal with the agents who were neither busy with a task
			 * nor managed to get a new one. */
			freeAgents = token.freeAgentsCopy();
			
			for (Agent agent : freeAgents) {

				/* Try to stay here for now. */
				TimedPosition next
			        = new TimedPosition(agent.position(), now + 1);
			
				/* Cancel all future reservations of the agent to make sure it doesn't
		    	 * block itself. */
		    	token.table.cancelReservations(agent);
		    	
		    	/* Cancel */
		    	token.cutPlan(agent, now);
		    	
		    	/* Add the current time and position. */
		    	addCurrentTimePosition(agent);
		    	
			    if (token.table.isFree(next)) {
				
				    token.table.reserve(agent, next, false);
				
				    token.plans.get(agent).add(next);					
				
				    continue;
			    }
			    // END DEBUG
			
				/* There's no task available for the agent and it's also in
				 * the way since it's at the delivery location of some task.
				 * Thus, move, get out the way. */
				if (planForEndpoint(agent, timeHorizon, runtimeLimit)) {
					
					continue;
				}
				
				
				// DEBUG Put this block back in
				/* It's impossible to reach an endpoint.
				 * At least try to stay here for a single time step. *
				TimedPosition next
				    = new TimedPosition(agent.position(), now + 1);
				
				if (token.table.isFree(next)) {
					
					token.table.reserve(agent, next, false);
					
					token.plans.get(agent).add(next);					
					
					continue;
				}
				*/
				// END DEBUG	
				
				
				
				
				/* The planning was not successful since no legal behavior for
				 * the currrent agent was found. */
                return null;
			}
							
			/* Let agents do one step in their plans so their current position
			 * actually changes. */
			now++;
			step();

			/* If all tasks of the scenario are completed, the procedure was
			 * successful. */
			if (token.availableTasks.isEmpty() && 
				token.claimedTasks.isEmpty() && 
				now > taskTimeHorizon) { 
				
				return token.assembleCommonPlan();
			}
		}

		/* Procedure failed.
		 * Tasks could not be completed before the timeHorizon was reached. */
		return null;
	}
	
	
	/* Tries to form a plan for the given agent to reach the closest
	 * endpoint available. Returns true if successful and false if no
	 * endpoint is both available and reachable. */
	private boolean planForEndpoint(Agent agent,
			                        int timeHorizon,
			                        long runtimeLimit)
			                        		throws TimeoutException,
			                        		       DistanceTableException {
		
		/* Endpoints are in a min-heap according to their vicinity to the
		 * agent's current position. */
		PriorityQueue<Position> endpointCandidates = chooseEndpoints(agent);
		
		/* No endpoints both available and reachable. */
		if (endpointCandidates.isEmpty()) {
		
			addCurrentTimePosition(agent);
			
			return false;
		}
		
		/* A* using the true distance metric. */
		TrueDistanceAStar star = new TrueDistanceAStar();
		
		Plan plan = null;
		
		/* Plan to reach the current endpoint. If successful, ignore all the
		 * following ones. */
		while (!endpointCandidates.isEmpty()) {
		
			Position endpoint = endpointCandidates.poll();
			
		    plan = star.trueDistanceAStar(scenario.mapManager,
		    			new TimedPosition(agent.position(), now),
				        endpoint, token.table, timeHorizon,
				        runtimeLimit, agent, distances);
		
		    if (plan != null) { break; }		   
		}

		/* Adds the current timedPosition to the agent's plan if necessary. */
		addCurrentTimePosition(agent);
		
		/* Not a single available endpoint is reachable. */
		if (plan == null) { return false; }

		/* Update the plan including the result of the A*
		 * search. Any previous plan has already been deleted
		 * during the blocking() call.*/
	    token.plans.get(agent).add(plan);
	    
	    return true;
	}
	
	
	/* Tries to form a plan for the given agent for the available task whose
	 * pickup location is closest to the agent while still being solvable.
	 * If there is no task available whose pickup and consecutively delivery
	 * location can be reached, returns false. */
	private boolean planForTask(Agent agent,
			                    int timeHorizon,
			                    long runtimeLimit)
			                    	throws TimeoutException,
										   DistanceTableException {
		
		TrueDistanceAStar star = new TrueDistanceAStar();
		
		PriorityQueue<Task> taskCandidates = getTaskCandidates(agent);
			
		/* Cancel any reservations for the future of this agent. */
		token.table.cancelReservations(agent);
			
		/* Remove any previous plan for the future. */
		token.cutPlan(agent, now + 1);

		Plan plan = null;
			
		/* Since the candidate list is ordered by preference, the first
		 * solvable task is also the closest one. */
		while (!taskCandidates.isEmpty()) {
			
			Task task = taskCandidates.poll();
			
		    /* 1. Plan to reach the pickup location. */
			plan = star.trueDistanceAStar(scenario.mapManager,
				new TimedPosition(agent.position(), now),
				task.targets()[0], token.table, timeHorizon,
				runtimeLimit, agent, distances);
			
			if (plan == null ) { continue; }
	    
			addCurrentTimePosition(agent);
			
			/* Update the plan including the result of the A* search. */
		    token.plans.get(agent).add(plan);
			
		    /* 2. Plan to reach the delivery location. */
		    TimedPosition deliveryStart = plan.lastTimedPosition(now);
			
			plan = star.trueDistanceAStar(scenario.mapManager, deliveryStart,
					task.targets()[1], token.table, timeHorizon,
					runtimeLimit, agent, distances);
			
			/* No path to the delivery location found. */
			if (plan == null) {
				
				/* Cancel reservations for the plan to the pickup location. */
				token.table.cancelReservations(agent);
				
				/* Remove the plan to the pickup location. */
				token.cutPlan(agent, now + 1);
				
				continue;
			}	
	
			/* Update the plan including the result of the A* search. */
			token.plans.get(agent).add(plan);
			
			/* Assign the task to the agent. */
			token.claimTask(agent, task);
			
			return true;			
		}
			
		/* No task available or reachable. */
		addCurrentTimePosition(agent);
		
		return false;
	}
	
	
	/* Adds the current time and position of the agent to its plan if it's not
	 * already present. Use this function to keep the integrity of the plan.
	 */
	private void addCurrentTimePosition(Agent agent) {

		Plan plan = token.plans.get(agent);
		
		/* Make sure a plan exists. */
		if (plan == null) {
			
			plan = new Plan(agent);
			
			token.plans.put(agent, plan);
		}		
		
		/* This is supposed to be the last entry in the current plan. */
		TimedPosition positionNow = new TimedPosition(agent.position(), now);
		
		/* An empty plan obviously misses this entry. */
		if (plan.length() == 0) {
			
			plan.add(positionNow);
			
			return;
		}
		
		TimedPosition lastTimedPosition = plan.plan().get(plan.length() - 1);
		
		/* If the last entry in the plan differs in position or time,
		 * add the new entry. Otherwise it's already present. */
		if (!positionNow.equals(lastTimedPosition)) { plan.add(positionNow); }
	}

	
	/* Endpoints are pickup or delivery locations of tasks, initial positions
	 * of agents as well as dedicated parking spots. */
	private void identifyEndpoints() {
		
		/* 1. Pickup and delivery locations of tasks.*/
		for (Task task : scenario.tasks()) {
			
			for (Position position : task.targets()) {
				
				endpoints.add(position);
			}
		}
		
		/* 2. Initial positions of agents. */
		for (Agent agent : scenario.agents()) {
			
			endpoints.add(agent.position());
		}
		
		/* 3. Parking spots. */		
		endpoints.addAll(scenario.map().parkingSpots);
		
		return;
	}

	
    /* Compute the true distance from every position on the map with an
     * outgoing edge to every endpoint of the scenario.
     * This yields a better heuristic for MAPF than the simple Manhattan
     * distance. */
	private void computeDistances(Map map, long runtimeLimit)
			throws TimeoutException {
		
		TrueDistances trueDistances = new TrueDistances();		
		
		for (Position endpoint : endpoints) {		
			
			distances.putAll(trueDistances.trueDistances(map, endpoint));
			
			Timeout.checkForTimeout(runtimeLimit);			
		}
	}

	
	/* Returns the last time a task will become available in this scenario. */
    private int lastTaskTime(HashSet<Task> tasks) {
    	
    	int last = 0;
    	
    	for (Task task : tasks) {
    		
    		last = Math.max(last, task.availabilityTime());
    	}    	
    	return last;
    }

	
    /* Add all tasks that become available at the given time to the task set
     * in the token. */
    private void addNewTasks(int time) {
    	
    	if (token.tasksByTime.containsKey(time)) {
    	
    	    ArrayList<Task> newTasks = token.tasksByTime.get(time);
    	
    	    token.availableTasks.addAll(newTasks);
    	    
    	    for (Task task : newTasks) {
    	    	
    	    	token.addDeliveryLocationsOfAvailableTask(task.targets()[1]);
    	    }
    	}    	    	 
    }

    
    /* Returns all available tasks for which holds that neither their pickup
     * nor their delivery location is currently the end of any other agent's
     * path. That means, the tasks that won't be blocked by another agent.
     * They're sorted by the vicinity of their pickup location to the given
     * agent. */
    private PriorityQueue<Task> getTaskCandidates(Agent agent) {
    	
    	PriorityQueue<Task> candidates = new PriorityQueue<Task>(
    		new TaskPickupComparator(distances, agent.position()));
    	
    	taskLoop:
    	for (Task task : token.availableTasks) {
    		
    		Position pickupLocation = task.targets()[0];
    		Position deliveryLocation = task.targets()[1];
    		    		
    		for (Agent otherAgent : scenario.agents()) {
    			
    			/* Skip tasks with pickup or delivery locations that are
        		 * potentially infinitely long occupied by a different
        		 * agent. */
    			if (otherAgent.equals(agent)) { continue; }
    			
    			Position destination = token.plans.get(otherAgent)
    					.lastTimedPosition(now).position();
    			
    			if (destination.equals(pickupLocation) ||
    				destination.equals(deliveryLocation)) {
    			
    				continue taskLoop;
    			}	
    		}    		
    		candidates.add(task);
    	}    	    	
    	
    	return candidates;
    }
    

    /* Checks whether the given agent would either ruin some other agent's
     * plan or would make an available task impossible to solve if he stayed
     * here forever.
     * Note that either way the agent's reservations are cancelled and so is
     * its future plan. */
    private boolean blocking(Agent agent) {
    	
    	/* Cancel all future reservations of the agent to make sure it doesn't
    	 * block itself. */
    	token.table.cancelReservations(agent);
    	
    	/* Cancel */
    	token.cutPlan(agent, now);
    	
    	/* Add the current time and position. */
    	addCurrentTimePosition(agent);
    	
    	/* Check whether no other agent plans to visit this position in the
    	 * future. */
    	if (!token.table.restingAllowed(agent.position(), now)) {
    		return true;
    	}
    	
    	/* Check whether this is the delivery location of a task no agent has
    	 * claimed yet (but would eventually). */
    	return token.isDeliveryLocationOfAnAvailableTask(agent.position());
    }
    
    
    /* Let agents do one step according to their plans. */
    private void step() {    	
    	
    	for (Agent agent : scenario.agents()) {
    		
    		Position newPosition
    		    = token.plans.get(agent).position(now, true);
    		
    		agent.setPosition(newPosition);    		
    		
    		/* Check whether the agent reaches the pickup location of its
    		 * current task. */
    		Task task = agent.task();
    		
    		if (task != null) {
    			
    			if (!task.started()
    				&& agent.position().equals(task.targets()[0])) {
    				
    				task.setStarted(now);
    			}
    		}    		
    		
    		/* If this is the last step of the plan, the task has to be
    		 * complete and the agent free again. */
    		if (token.plans.get(agent).lastTimedPosition(now).equals(
    				new TimedPosition(newPosition, now))) {
    			
    			token.setTaskComplete(agent, now);
    		}
    	}
    }

    
    /* Make every agent stay where it currently is. */
    private void initializeTrivialPaths() {
    	
    	for (Agent agent : scenario.agents()) { setTrivialPath(agent); }
    }
    
    
    /* Set the agent's plan to the trivial path being just staying where it
     * already is. Updates the agent's reservations in the process. */
    private void setTrivialPath(Agent agent) {
  
    	TimedPosition currentPosition
    	    = new TimedPosition(agent.position(), now);
    	
    	ArrayList<TimedPosition> plan = token.plans.get(agent).plan();
    	
    	/* Remove the future part of the agent's plan. */
    	token.cutPlan(agent, now);
    	
    	/* Instead plan to just stay at the current position. */
    	plan.add(currentPosition);

    	/* Cancel any reservations of this agent for time steps not already
    	 * passed. */
    	token.table.cancelReservations(agent);
    	
    	/* Reserve the current location forever. */
    	token.table.reserve(agent, currentPosition, true);
    }

    
    /* This function proposes some endpoints which are not the delivery
     * location of any available or claimed task. It's thus a position
     * recommended for parking free agents. Staying there may still block
     * agents on their way elsewhere though. */
    private PriorityQueue<Position> chooseEndpoints(Agent agent) {
    	
    	/* Copy the endpoint set. */
    	HashSet<Position> endpointSet = new HashSet<Position>();
    	
    	endpointSet.addAll(endpoints);
    	
    	/* Remove all endpoints from the copy that are delivery locations of
    	 * available tasks. */
    	for (Task task : token.availableTasks) {
    		
    		endpointSet.remove(task.targets()[1]);
    	}
    	
    	/* Pick all of the remaining endpoints at which resting is currently
    	 * allowed and put them in a min-heap. */
    	PriorityQueue<Position> endpointCandidates
    	    = new PriorityQueue<Position>(
    	    		new EndpointComparator(distances, agent.position()));
    	
        for (Position endpoint : endpointSet) {
        	
        	/* If no other agent is currently heading to this endpoint
    		 * (for whatever reason) it may be a good location to rest */
    		if (token.table.restingAllowed(endpoint, now)) {
    			
    			endpointCandidates.add(endpoint);
    		}
        }     	
        return endpointCandidates;
    } 
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package token_passing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import cooperative_a_star.ReservationTable;
import multi_agent_pathfinding_framework.Agent;
import multi_agent_pathfinding_framework.CommonPlan;
import multi_agent_pathfinding_framework.Plan;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.Scenario;
import multi_agent_pathfinding_framework.Task;
import multi_agent_pathfinding_framework.TimedPosition;

/* A token in this context is the extension of a reservation table that is
 * passed around by the agents at runtime.
 * 
 * It contains a regular reservation table like the one used for CA* that
 * contains reservations of positions and edges.
 * Moreover, the token stores the individual plans of all the agents as well
 * as information which tasks are available or are currently dealt with by
 * agents.
 */
public class Token {

	/* All agents in correct order. */
	private Agent[] agents;
	
	/* Agents that are currently not assigned to a task. */
	private HashSet<Agent> freeAgents = new HashSet<Agent>();
	
	public ReservationTable table;
	
	public HashMap<Agent, Plan> plans = new HashMap<Agent, Plan>();
	
	/* Stores all tasks, sorted by the time they get available. */
	public HashMap<Integer, ArrayList<Task>> tasksByTime
	    = new HashMap<Integer, ArrayList<Task>>(); 
	
	/* Tasks that already came up but are not assigned to an agent yet. */
	public HashSet<Task> availableTasks = new HashSet<Task>();
	
	/* Tasks that are assigned to some agent and not yet completed. */
    public HashSet<Task> claimedTasks = new HashSet<Task>();
    
    /* Maintain a set of the delivery locations of currently available tasks.
     * This allows to check whether an endpoint is a bad choice for resting
     * since someone would otherwise claim the task soon. */
    private HashSet<Position> deliveryLocationsOfAvailableTasks
        = new HashSet<Position>();
	
    	
	public Token(Scenario scenario) {
		
		this.agents = scenario.agents();		
		
		this.table = new ReservationTable(agents);
		
		for (Agent agent : agents) {
			
		    freeAgents.add(agent);
			
			plans.put(agent, new Plan(agent));
		}
		
		sortTasksByTime(scenario.tasks());
	}
	
	
	/* Returns the agents that are currently not assigned to a task.*/
	public HashSet<Agent> freeAgents() { return freeAgents; }
	
	
	/* Returns a copy of the set of free agents. */
	public HashSet<Agent> freeAgentsCopy() {
		
		HashSet<Agent> copy = new HashSet<Agent>();
		
		copy.addAll(freeAgents);
		
		return copy;
	}
	
	/* Remove the whole future of the given agent's plan including
	 * the position of the current time.
	 * 
	 * Example: A cut at time 3 will prune the plan
	 * [p0, p1, p2, p3, p4] to [p0, p1, p2]. */
	public void cutPlan(Agent agent, int time) {
		
		ArrayList<TimedPosition> plan = plans.get(agent).plan();
		
		while (plan.size() > time) {
			
			plan.remove(plan.size() - 1);
		}
	}
	
	
	/* Once an agent plans to perform a task, it should claim it.
	 * Make sure the task isn't claimed already.
	 * Check the availableTasks set. */
	public void claimTask(Agent agent, Task task) {
		
		agent.setTask(task);
		
		availableTasks.remove(task);
		
		claimedTasks.add(task);
		
		freeAgents.remove(agent);
		
		deliveryLocationsOfAvailableTasks.remove(task.targets()[1]);
	}
	
	
	/* Releases the claim of an agent on its task.
	 * Make sure the task is claimed right now.
	 * Check the claimedTasks set.*/
	public void unclaimTask(Agent agent) {
		
		if (agent.task() == null) { return; }
		
		availableTasks.add(agent.task());
		
		claimedTasks.remove(agent.task());
		
		agent.setTask(null);
		
		freeAgents.add(agent);
	}

	/* Once a task is completed, its delivery location doesn't have to be kept
	 *  free anymore. Also, the agent is free again. */
    public void setTaskComplete(Agent agent, int now) {
        
    	Task task = agent.task();
    	
    	if (task == null) { return; }
    	
    	task.setComplete(now);
    	
    	claimedTasks.remove(task);
		
		agent.setTask(null);
		
		freeAgents.add(agent);
    }

    
    /* Puts all given task into a map that maps availability times to the
	 * tasks that become available at this time. */
	private void sortTasksByTime(HashSet<Task> tasks) {
		
		for (Task task : tasks) {
			
			int time = task.availabilityTime();
			
			if (!tasksByTime.containsKey(time)) {
				
				tasksByTime.put(time, new ArrayList<Task>());
			}
			
			tasksByTime.get(time).add(task);
		}
	}
	
	
	/* Checks for a delivery location in constant time. */
	public boolean isDeliveryLocationOfAnAvailableTask(Position position) {
		
		return deliveryLocationsOfAvailableTasks.contains(position);
	}
	
	
	/* Make sure to call this whenever a new task becomes available. */
	public void addDeliveryLocationsOfAvailableTask(Position position) {
		
		deliveryLocationsOfAvailableTasks.add(position);
	}

	
    /* Combine the agents' plans in the correct order. */
    public CommonPlan assembleCommonPlan() {

    	CommonPlan commonPlan = new CommonPlan();
    	
    	for (Agent agent : agents) {
    		
    		commonPlan.addPlan(plans.get(agent));
    	}
    	return commonPlan;
    }
}

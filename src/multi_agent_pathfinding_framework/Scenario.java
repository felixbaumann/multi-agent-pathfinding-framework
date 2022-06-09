/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.HashSet;


/* Standard representation of a scenario consisting of a standard map,
 * a number of standard agents and a set of standard tasks. */
public class Scenario {
	
	/* A MapManager contains a map and informs about the existence
	 * and usability of edges. In particular, in case of dynamic edges,
	 * i.e. edges that change their direction over time, the MapManager
	 * will decide whether an edge is available in a given direction at
	 * a given time. */
	public MapManager mapManager;
	
	/* Agents have a unique id, a startposition and optionally a name.
	 * On top of that, they may have a task (which could be shared among
	 * several agents though). */
	private Agent agents[];
	
	
	/* A task is a sequence of positions that have to be reached in the given
	 * order by some agent. */
	private HashSet<Task> tasks;	
	
	
	/* Create a scenario given a map, agents and task.
	 * The parameter dirChangeFreq defines the frequency with which edges
	 * on the map change their direction.
	 * Use 0 for static edges that never change. */
	public Scenario(Map map, Agent agents[], HashSet<Task> tasks,
			        int dirChangeFreq) {
		
		this.mapManager = new MapManager(map, dirChangeFreq);
		
		this.agents = agents;
		
		this.tasks = tasks;
	}
	
	
	public Scenario(YamlClassicScenario yamlClassicScenario)
			throws CorruptedFileException {
		
		/* Convert the yamlClassicMap to a standard map. */
		this.mapManager = new MapManager(new Map(yamlClassicScenario.map()), 0);
		
		/* Prepare a native agent array of suitable size. */
		int size = yamlClassicScenario.agents().length;
		
		agents = new Agent[size];
		
		int index = 0;
		
		tasks = new HashSet<Task>();
		
		/* Convert each yamlClassicAgent to a standard agent. */
		for (YamlClassicAgent yamlClassicAgent : 
			yamlClassicScenario.agents()) {
						
			Agent agent = new Agent(yamlClassicAgent);
			
			agents[index] = agent;
			
			/* A classic agent has a single task that only he can do.
			 * This task should still be referenceable from the scenario
			 * though. */
			tasks.add(agent.task());
			
			index++;
		}
	}
	
	public Scenario(YamlDynamicScenario yamlDynamicScenario)
			throws CorruptedFileException {
		
		/* Convert the yamlClassicMap to a standard map. */
		this.mapManager = new MapManager(new Map(yamlDynamicScenario.map()), 0);
		
		/* Prepare a native agent array of suitable size. */
		int size = yamlDynamicScenario.agents().length;
		
		agents = new Agent[size];
		
		int index = 0;
		
		/* Convert each yamlDynamicAgent to a standard agent. */
		for (YamlDynamicAgent yamlDynamicAgent : 
			yamlDynamicScenario.agents()) {
						
			Agent agent = new Agent(yamlDynamicAgent);
			
			agents[index] = agent;
			
			index++;
		}		
		
		tasks = new HashSet<Task>();

		/* Convert yamlDynamicTasks to standard tasks. */
		for (YamlDynamicTask yamlDynamicTask : yamlDynamicScenario.tasks()) {
			
			tasks.add(new Task(yamlDynamicTask.positions(),
					           yamlDynamicTask.available()));
			
		}
	}
	
	
	/* Given the id of an agent. Returns the index of this agent in the
	 * agent array. Those values will usually be identical, but differ
	 * if several scenarios are created in the same session:
	 * If the first scenario has n agents, the first agent in the second
	 * scenario will get the id n instead of 0. */
	public int indexOfAgent(int agentId) {
		
		for (int index = 0; index < agents.length; index++) {
			
			if (agents[index].id() == agentId) { return index; }
		}		
		return -1;
	}
	
	
	public Map map() { return mapManager.map; }
	
	public Agent[] agents() { return agents; }
	
	public HashSet<Task> tasks() { return tasks; }
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* Standard representation of an agent in MAPF settings. */
public class Agent {

	private static int id_tracker = 0;
	
	/* Unique id used to identify an agent. */
	private final int id;
	
	/* Optional name of the agent. */
	private String name = null;
	
	/* Current (start) coordinates of the agent. */
	private Position position;
	
	/* Current task of the agent. */
	private Task task = null;
	
	
	public Agent(Position startposition) {
		this.id = id_tracker;
		id_tracker++;
		
		this.position = startposition;
	}
	
	
	public Agent(YamlClassicAgent yamlClassicAgent) 
			throws CorruptedFileException {
		
		/* Id. */
		this.id = id_tracker;
		id_tracker++;
		
		/* Start position. */
		if (yamlClassicAgent.start().length == 2) {
			
			position = new Position(yamlClassicAgent.start[0],
					yamlClassicAgent.start[1]);

		}
		else { throw new CorruptedFileException(
				"The start position of one of the agents does not consist of "
				+ "two coordinates [x, y]."); }
		
		/* Name. */
		name = yamlClassicAgent.name();
		
		/* Goal position.
		 *
		 * A classic agent only has a single goal so this is a list with
		 * a single entry which is a coordinate pair. */		
		Position goals[] = { new Position(yamlClassicAgent.goal()[0],
				yamlClassicAgent.goal()[1]) };
		
		task = new Task(goals);
	}
	
	
	/* Note that the initial goal of an agent from a yamlDynamicAgent is null.
	 */
	public Agent(YamlDynamicAgent yamlDynamicAgent) 
			throws CorruptedFileException {
		
		/* Id. */
		this.id = id_tracker;
		id_tracker++;
		
		/* Start position. */
		if (yamlDynamicAgent.start().length == 2) {
			
			position = new Position(yamlDynamicAgent.start[0],
					yamlDynamicAgent.start[1]);

		}
		else { throw new CorruptedFileException(
				"The start position of one of the agents does not consist of "
				+ "two coordinates [x, y]."); }
		
		/* Name. */
		name = yamlDynamicAgent.name();
	}
	
	
	/* Returns a copy of this agent with the same value for all fields
	 * including the id. The returned agent is identical to but independend
	 * from this one. */
	public Agent deepCopy() {
		
		return new Agent(id, name, position, task);
	}
	
	/* Constructor for deep copy only. Note that deep copied agents even share
	 * the same id. */
	public Agent(int id, String name, Position position, Task task) {
		
		this.id = id;
		this.position = position;

		if (name != null) {
		
			this.name = name;
		}
		
		if (task != null) {
			
			this.task = task.deepCopy();
		}
	}

	
	public int id() { return id; }
	
	public void setName(String name) { this.name = name; }
	
	public String name() { return name; }
	
	public void setPosition(Position position) {

		this.position = position;
	}
	
	public Position position() { return this.position; }
	
	public void setTask(Task task) { this.task = task; }
	
	public Task task() { return task; }
	
	public static void resetIdTracker() { id_tracker = 0; }
}

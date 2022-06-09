/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* A task is a sequence of positions that have to be reached by an agent in
 * the given order. A simple task is just reaching a single goal position.
 */
public class Task {
	
	private static int id_tracker = 0;
	
	/* Unique id used to identify a task. */
	private final int id;
	
	/* A task doesn't have to be available right from the beginning of the
	 * scenario but might come in later. Agents are not allowed to start a
	 * task before its availabilityTime. */
	private final int availabilityTime;
	
	/* Describes when an agent already started this task. In that case,
	 * no other agent can claim this task. A value of -1 indicates that
	 * nobody started it. Obviously, this is only relevant in
	 * settings where a specific task can be done by an arbitrary agent. */
	private int startingTime = -1;
	
	/* Describes when the task has been completed by any agent.
	 * A value of -1 indicates that the task has not been completed yet. */
	private int completionTime = -1;
	
	/* A list of positions that have to be reached in order to complete
	 * the task. */
	private Position[] targets;
	
	
	public Task(Position[] targets) {
		id = id_tracker;
		id_tracker++;
		
		this.targets = targets;
		
		this.availabilityTime = 0;
	}
	
	
	public Task(Position[] targets, int availabilityTime) {
		id = id_tracker;
		id_tracker++;
		
		this.targets = targets;
		
		this.availabilityTime = availabilityTime;
	}

	/* Returns a copy of this task with the same value for all fields
	 * including the id. The returned task is identical to but independend
	 * from this one. */
	public Task deepCopy() {
		
		return new Task(this.id, this.availabilityTime, this.targets);
	}
	
	/* Constructor for deep copy only. Note that deep copied tasks even share
	 * the same id. */
	private Task(int id, int availabilityTime, Position[] targets) {
		
		this.id = id;
		
		this.availabilityTime = availabilityTime;
		
		Position[] positions = new Position[targets.length];
		
		for (int index = 0; index < targets.length; index++) {
			
			positions[index] = targets[index];
		}
		
		this.targets = positions;
	}
	
	public Position[] targets() { return targets; }
	
	public int id() { return id; }
	
	public int availabilityTime() { return availabilityTime; }
	
	public boolean started() { return startingTime != -1; }
	
	public int startingTime() { return startingTime; }
	
	public void setStarted(int time) { startingTime = time; }
	
	public boolean complete() { return completionTime != -1; }
	
	public int completionTime() { return completionTime; }
	
	public void setComplete(int time) { completionTime = time; }
	
	public String print() {
		
		String string = "Task " + id + ", available at "
		    + availabilityTime + " with targets: ";
		
		for (Position target : targets) {
			
			string += (target.print() + ", "); 
		}		
		return string;
	}
}

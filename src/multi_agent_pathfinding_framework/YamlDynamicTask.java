/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* A dynamic task consists of a number of positions that have to be reached
 * consecutively in order to complete the task.
 * The task is not necessarily available right from the beginning.
 * Instead the task comes up at a specified time.
 * Depending on the problem setting, agents are aware of the task and
 * are allowed to consciously move towards the first position before the
 * task is available or are surprised by the task entirely. */
public class YamlDynamicTask {

	/* Specifies the timestep when this task comes in. That's the earliest
	 * time when any agent is allowed to start working on it. */
	public int available;
	
	/* List of coordinates that have to be reached in order to complete the
	 * task. Interpret pairwise, i.e. [4, 5, 6, 7, 8, 9] means positions
	 * (4, 5), (6, 7), (8, 9). */
	public int positions[];
	
	public YamlDynamicTask() {}
	
	public int available() { return available; }
	
	/* Returns the target positions that have to be reached. */
	public Position[] positions() throws CorruptedFileException {
		
		/* There has to be an even number of coordinates,
		 * so make sure the last bit is odd. */
		if ((positions.length & 1) == 1) {
			
			throw new CorruptedFileException("One of the tasks has an odd"
			+ " number of coordinates. This makes pairing them properly for"
			+ " creating positions impossible.");			
		}
		
		/* Number of coordinate pairs. */
		int count = positions.length / 2;
		
		/* Target positions to be reached. */
		Position[] targets = new Position[count]; 
		
		/* Create position objects from pairs of coordinates. */
		for (int index = 0; index < count; index++) {
			
			targets[index] = new Position(positions[index * 2],
					                      positions[index * 2 + 1]);
		}
		
		return targets;
	}
}

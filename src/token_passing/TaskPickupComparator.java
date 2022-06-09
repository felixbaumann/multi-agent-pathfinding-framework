/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package token_passing;

import java.util.Comparator;
import java.util.HashMap;

import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.Task;

/* Compares two tasks with respect to how close their pickup location is from
 * a given position of some agent.
 * Make sure those pickup locations actually show up in the distances map. */
public class TaskPickupComparator implements Comparator<Task> {

	private HashMap<Edge, Integer> distances;
	
	private Position agentPosition;
	
	public TaskPickupComparator(HashMap<Edge, Integer> distances,
							    Position agentPosition) {
		
		this.distances = distances;
		this.agentPosition = agentPosition;
	}

	@Override
	public int compare(Task task1, Task task2) {
		
		Integer distancePickupTask1 = distances.get(
				new Edge(agentPosition, task1.targets()[0]));
		
		Integer distancePickupTask2 = distances.get(
				new Edge(agentPosition, task2.targets()[0]));
			
		return distancePickupTask1.compareTo(distancePickupTask2);
	}
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package token_passing;

import java.util.Comparator;
import java.util.HashMap;

import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.Position;

/* Compares two endpoints with respect to how close they are from a given
 * position of some agent.
 * Make sure those endpoints actually show up in the distances map. */
public class EndpointComparator implements Comparator<Position> {

	/* Edge is used here to represent a position pair,
	 * not necessarily an actual edge on the map. */
	private HashMap<Edge, Integer> distances;
	
	private Position agentPosition;
	
	public EndpointComparator(HashMap<Edge, Integer> distances,
							  Position agentPosition) {
		
		this.distances = distances;
		this.agentPosition = agentPosition;
	}

	@Override
	public int compare(Position endpoint1, Position endpoint2) {
		
		Integer distanceEndpoint1
		    = distances.get(new Edge(agentPosition, endpoint1));
		
		Integer distanceEndpoint2
		    = distances.get(new Edge(agentPosition, endpoint2));

		return distanceEndpoint1.compareTo(distanceEndpoint2);
	}
}

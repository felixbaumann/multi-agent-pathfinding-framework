/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* Representation of a timed edge which is an edge at a certain time step.
 * A timed edge with time t can be reserved if it's used between the time
 * steps t and t + 1. */
public class TimedEdge {

	public final Edge edge;
	
	public final int time;
	
	/* An edge pointing from the given start position to the given target
	 * position at the time of the given start position. */
	public TimedEdge(TimedPosition startPos,
			         TimedPosition targetPos) {
		
		this.time = startPos.t;
		this.edge = new Edge(new Position(startPos.x, startPos.y),
				             new Position(targetPos.x, targetPos.y));
	}
	
	
	public TimedEdge(int time, Edge edge) {
		
		this.time = time;
		this.edge = edge;
	}
	
	
	/* Two edges are equal if they point from an equal source position to an
	 * equal target position. */
	public boolean equals(final Object object) {
		
		if (!(object instanceof TimedEdge)) return false;
		
	    if (!((TimedEdge) object).edge.equals(this.edge)) return false;
	    
	    if (((TimedEdge) object).time != this.time) return false;
	    
	    return true;	
	}
	
	
	public int hashCode() { return edge.hashCode() * time; }
}

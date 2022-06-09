/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package cooperative_a_star;

import multi_agent_pathfinding_framework.TimedEdge;

/* Since an edge can only be occupied by one agent at a time,
 * algorithms like CA* make use of reservations. An agent reserves all the
 * edges it will use at the planned times and makes sure its
 * own plan does not conflict with reservations of other agents.
 * 
 * Note that it may be necessary to create two reservations for each edge
 * used. One in the direction the edge is used and one for the opposite
 * direction to avoid edge conflicts. Alternatively, one could just make one
 * reservation in the opposite direction since two agents using an edge in the
 * same direction is already covered by a node conflict right before that. */
public class ReservationEdge extends Reservation {
	
	private TimedEdge reservedEdge;
	
	public ReservationEdge(TimedEdge reservedEdge) {
		this.reservedEdge = reservedEdge;
	}

	
	public int time() { return reservedEdge.time; }
	
	
	@Override
	public int hashCode() { return reservedEdge.hashCode(); }
	
	
	@Override
	public boolean equals(final Object object) {

		if (!(object instanceof ReservationEdge)) return false;
		
		if (!((ReservationEdge) object).reservedEdge.equals(reservedEdge)) {
			
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public String print() {
		
		return "(Edge: " + reservedEdge.edge.print() + 
			   ", " + reservedEdge.time + ")";
	}
}

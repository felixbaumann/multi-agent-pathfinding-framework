/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package cooperative_a_star;

import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedPosition;

/* Since a position can only be occupied by one agent at a time,
 * algorithms like CA* make use of reservations. An agent reserves all the
 * positions it will occupy at the planned times and makes sure its
 * own plan does not conflict with reservations of other agents. */
public class ReservationPosition extends Reservation {

	private TimedPosition reservedPosition;
	
	public ReservationPosition(TimedPosition position) {
		
		this.reservedPosition = position;
	}
	
	public int time() { return reservedPosition.t; }
	
	
	@Override
	public int hashCode() {
		
		return reservedPosition.hashCode();
	}

	
	@Override
	public boolean equals(final Object object) {

		if (!(object instanceof ReservationPosition)) return false;
		
		if (!((ReservationPosition) object).reservedPosition.equals(
				reservedPosition)) {
			return false;
		}
		
		return true;
	}
	
	
	public Position position() { return reservedPosition.position(); }
	
	
	@Override
	public String print() {
		
		return "(Temp: " + reservedPosition.print() + ")";
	}
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package cooperative_a_star;

import multi_agent_pathfinding_framework.Position;

/* Since a position can only be occupied by one agent at a time,
 * algorithms like CA* make use of reservations. An agent reserves all the
 * positions it will occupy at the planned times and makes sure its
 * own plan does not conflict with reservations of other agents.
 * 
 * Moreover, agents remain at their goal positions once their plan is complete
 * and they have to reserve this goal position for eternity.
 * This requires to specify their goal position as done by this reservation
 * object combined with some map that stores when this goal position will be
 * reached. */
public class ReservationFinalPosition extends Reservation {

	private final Position reservedPosition;
	
	public ReservationFinalPosition(Position position) {
		
		this.reservedPosition = position;
	}
	
	
	@Override
	public int hashCode() {
		
		return reservedPosition.hashCode();
	}

	@Override
	public boolean equals(final Object object) {

		if (!(object instanceof ReservationFinalPosition)) return false;
		
		if (!((ReservationFinalPosition) object).reservedPosition.equals(
				reservedPosition)) return false;
		
		return true;
	}

	public Position position() { return reservedPosition; }
	
	
	@Override
	public String print() {
		
		return "(Final: " + reservedPosition.print() + ")";
	}
}

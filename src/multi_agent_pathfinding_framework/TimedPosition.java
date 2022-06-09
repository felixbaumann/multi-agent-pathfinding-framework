/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* A position on a grid map given by discrete x and y coordinates combined
 * with a specific time step. A timed position can be occupied by an agent
 * and thus reserved in a reservation table. */
public class TimedPosition {

	public final int x;
	public final int y;
	public final int t;
	
	public TimedPosition(final int x, final int y, final int t) {
		this.x = x;
		this.y = y;
		this.t = t;
	}
	
	public TimedPosition(final Position position, final int t) {
		this.x = position.x;
		this.y = position.y;
		this.t = t;
		
	}
	
	/* Hash code should only depend on x, y and t value so TimedPosition
	 * objects with the same coordinates at the same time yield the same hash
	 * code. */
	public int hashCode() { return (x << 23) + (y << 11) + t; }
	
	
	public boolean equals(final Object object) {
		
		if (!(object instanceof TimedPosition)) return false;
		
	    if (((TimedPosition) object).x != this.x) return false;
	    
	    if (((TimedPosition) object).y != this.y) return false;
	    
	    if (((TimedPosition) object).t != this.t) return false;
	    
	    return true; 
	}
	
	public Position position() { return new Position(x, y); }
	
	public String print() {
		
		return "(x = " + x + ", y = " + y + ", t = " + t + ")";
	}
}

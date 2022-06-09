/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* A position on a grid map given by discrete x and y coordinates. */
public class Position {

	public final int x;
	public final int y;
	
	public Position(final int x, final int y) {
		this.x = x;
		this.y = y;
	}
	
	
	/* Hash code should only depend on x and y value so position objects with
	 * the same coordinates yield the same hash code. */
	public int hashCode() {
		
		return (x << 16) + y;
	}
	
	
	public boolean equals(final Object object) {
		
		if (!(object instanceof Position)) return false;
		
	    if (((Position) object).x != x) return false;
	    
	    if (((Position) object).y != y) return false;
	    
	    return true;		
	}
	
	public String print() { return "(x = " + x + ", y = " + y + ")"; }
}

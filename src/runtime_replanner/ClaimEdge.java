/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import multi_agent_pathfinding_framework.Position;

/* A ClaimEdge represent the announcement that an agent plans to use
 * a specific edge in the next time step. */
public class ClaimEdge extends Claim {

	/* Positions adjacent to the edge. Note that their order should not
	 * matter. */
	public final Position position1;
	public final Position position2;
	
	
	public ClaimEdge(Position position1, Position position2) {
		
		this.position1 = position1;
		this.position2 = position2;
	}
		
	
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof ClaimEdge)) return false;
		
		/* Same position order. */
		if (((ClaimEdge) object).position1.equals(this.position1) &&
			((ClaimEdge) object).position2.equals(this.position2)) {
		    	
		    	return true;
		}
						
		/* Different (yet acceptable) position order. */
		if (((ClaimEdge) object).position1.equals(this.position2) &&
			((ClaimEdge) object).position2.equals(this.position1)) {
			
			return true;
		}		
		return false;
	}
	
	/* Commutativity guarantees that the order of the positions does not
	 * influence the hash code. */
	@Override
	public int hashCode() {
		
		return position1.hashCode() + position2.hashCode();
	}
}

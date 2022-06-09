/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import multi_agent_pathfinding_framework.Position;

/* A ClaimPosition represent the announcement that an agent plans to reach
 * a specific position in the next time step. */
public class ClaimPosition extends Claim {

	public final Position position;
	
	
	public ClaimPosition(Position position) {
		
		this.position = position;
	}
		
	
	@Override
	public boolean equals(Object object) {
		
		if (!(object instanceof ClaimPosition)) return false;
		
		return ((ClaimPosition) object).position.equals(this.position);
	}
	
	
	@Override
	public int hashCode() { return position.hashCode(); }
}

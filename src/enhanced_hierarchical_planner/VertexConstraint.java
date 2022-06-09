/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedPosition;

/* A constraint rules out that a traversal uses a specific edge or position
 * at a specific time. It is used to resolve conflicts. */
public class VertexConstraint extends Constraint {

	public final TimedPosition timedPosition;
	
	public final Position position;
	
	public final int time;
	
	
	public VertexConstraint(Traversal traversal,
							TimedPosition timedPosition) {
		
		this.traversal = traversal;		
		this.timedPosition = timedPosition;
		this.position = timedPosition.position();
		this.time = timedPosition.t;
	}
	
	
	@Override
	public boolean equals(Object other) {
		
		if (!(other instanceof VertexConstraint)) return false;
		
		return ((VertexConstraint) other).traversal.equals(this.traversal)
			&& ((VertexConstraint) other).timedPosition.equals(
					                                   this.timedPosition);
	}
	
	
	@Override
	public int hashCode() {
		return (traversal.hashCode() << 16) + timedPosition.hashCode();
	}
}




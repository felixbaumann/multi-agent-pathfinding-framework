/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.TimedEdge;

/* An EdgeConstraint rules out that a traversal uses a specific edge
 * at a specific time. It is used to resolve conflicts. */
public class EdgeConstraint extends Constraint{

	public final TimedEdge timedEdge;
	
	public final Edge edge;
	
	public final int time;
	
	public EdgeConstraint(Traversal traversal, TimedEdge timedEdge) {
		
		this.traversal = traversal;		
		this.timedEdge = timedEdge;
		this.edge = timedEdge.edge;
		this.time = timedEdge.time;
	}
	
	
	@Override
	public boolean equals(Object other) {
		
		if (!(other instanceof EdgeConstraint)) return false;
		
		return ((EdgeConstraint) other).traversal.equals(this.traversal)
			&& ((EdgeConstraint) other).timedEdge.equals(this.timedEdge);
	}
	
	
	@Override
	public int hashCode() {
		return (traversal.hashCode() << 16) + timedEdge.hashCode();
	}
}




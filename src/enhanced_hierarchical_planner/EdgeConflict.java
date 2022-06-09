/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import multi_agent_pathfinding_framework.Edge;
import multi_agent_pathfinding_framework.TimedEdge;

/* An EdgeConflict occurs if two traversals plan to use the same edge
 * at the same time. */
public class EdgeConflict extends Conflict {

	public final TimedEdge timedEdge;
	
	/* Edge and time store the same value as in the timedEdge.
	 * Just for accessibility convenience. */
	public final Edge edge;
	
	public EdgeConflict(Traversal traversal1, Traversal traversal2,
						TimedEdge timedEdge) {
		
		this.traversal1 = traversal1;
		this.traversal2 = traversal2;
		this.timedEdge = timedEdge;
		this.edge = timedEdge.edge;
		this.time = timedEdge.time;
	}
	
}

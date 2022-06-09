/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import multi_agent_pathfinding_framework.Position;
import multi_agent_pathfinding_framework.TimedPosition;


/* A VertexConflict occurs if two traversals plan to use the same vertex
 * (position) at some time. */
public class VertexConflict extends Conflict {

	public final TimedPosition timedPosition;
	
	/* Same as the position in the timedPosition.
	 * Just for accessibility convenience. */
	public final Position position;	
	
	public VertexConflict(Traversal traversal1, Traversal traversal2,
			              TimedPosition timedPosition) {
		
		this.traversal1 = traversal1;
		this.traversal2 = traversal2;
		this.timedPosition = timedPosition;
		this.position = timedPosition.position();
		this.time = timedPosition.t;
	}
	
}

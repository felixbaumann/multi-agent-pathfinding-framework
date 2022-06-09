/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

/* A conflict between two traversals occurs if they plan to use the same edge
 * or position at some time. */
public abstract class Conflict {

	public Traversal traversal1;
	public Traversal traversal2;
	
	public int time;
}

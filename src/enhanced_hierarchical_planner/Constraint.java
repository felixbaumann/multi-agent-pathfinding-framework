/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

/* A constraint rules out that a traversal uses a specific edge or position
 * at a specific time. It is used to resolve conflicts. */
public abstract class Constraint {

	public Traversal traversal;
}

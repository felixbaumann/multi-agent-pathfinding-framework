/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* Indicates that a computed plan has some flaw like
 * a node- or an edge conflict, a jump in time or space,
 * disrespects an obstacle, does not start in the agent's initial position or
 * does not lead to the goal. */
public class InvalidPlanException extends Exception {

	private static final long serialVersionUID = -3031838480813161209L;

	public InvalidPlanException(String someErrorMessage) {
		super(someErrorMessage);
	}
	
}

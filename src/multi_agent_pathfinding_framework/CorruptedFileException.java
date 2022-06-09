/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

/* Indicates an input file with a scenario that does not follow
 * the ClassicScenario or DynamicScenario format properly. */
public class CorruptedFileException extends Exception {

	private static final long serialVersionUID = -345029638498642893L;

	public CorruptedFileException(String someErrorMessage) {
		
		super(someErrorMessage);
	}
}

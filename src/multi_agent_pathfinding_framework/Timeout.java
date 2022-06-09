/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.concurrent.TimeoutException;

public class Timeout {

	/* Checks whether a given time stamp that limits a process has already
	 * been exceeded and if so throws an appropriate exception. */
	public static void checkForTimeout(long runtimeLimit)
			throws TimeoutException {
		
		if (System.nanoTime() > runtimeLimit) {
			throw new TimeoutException();
		}
	}	
}

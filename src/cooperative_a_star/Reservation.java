/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package cooperative_a_star;

/* Since an edge or position can only be occupied by one agent at a time,
 * algorithms like CA* make use of reservations. An agent reserves all the
 * positions and edges it will use at the planned times and makes sure its
 * own plan does not conflict with reservations of other agents. */
public abstract class Reservation {
	
	public abstract boolean equals(final Object object);
	
	public abstract int hashCode();
	
	public abstract String print();
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

public class Pair<S, T> {

	public final S first;	
	public final T second;
	
	public Pair(S first, T second) {
		
		this.first = first;
		this.second = second;
	}
	
	public boolean equals(Object other) {
		
		if (!(other instanceof Pair<?, ?>)) return false;
		
		return ((Pair<?, ?>) other).first == this.first
			&& ((Pair<?, ?>) other).second == this.second;
	}
	
	public int hashCode() {
		
		return (first.hashCode() << 16) + second.hashCode();
	}
}

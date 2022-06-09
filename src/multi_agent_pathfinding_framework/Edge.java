/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;


/* Standard representation of a directed edge on a standard map. */
public class Edge {
	
	public final Position source;
	public final Position target;
	
	/* True if this is a copy of a directed edge but leading in the opposite
	 * direction. Together they form an undirected edge. */
	public final boolean copy;
	
	
	/* Directed edge from source to target. */
	public Edge(Position source, Position target) {
		
		this.source = source;
		this.target = target;
		this.copy = false;
	}
	
	
	/* Directed edge from source to target. */
	public Edge(Position source, Position target, boolean copy) {
		
		this.source = source;
		this.target = target;
		this.copy = copy;
	}
		
	
	/* Two edges are equal if they point from an equal source position to an
	 * equal target position. */
	public boolean equals(final Object object) {
		
		if (!(object instanceof Edge)) return false;
		
	    if (!((Edge) object).source.equals(this.source)) return false;
	    
	    if (!((Edge) object).target.equals(this.target)) return false;
	    
	    return true;	
	}
	
	
	/* Hash code should only depend on the positions so equal edges get
	 * the same hash code. */
	public int hashCode() {
		
		return (source.hashCode() << 16) + target.hashCode();
	}
	
	public String print() {
		
		return "[" + source.print() + ", " + target.print() + "]";
	}
}

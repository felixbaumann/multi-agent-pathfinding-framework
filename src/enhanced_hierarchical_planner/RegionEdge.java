/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package enhanced_hierarchical_planner;

import multi_agent_pathfinding_framework.Edge;

/* A RegionEdge is a directed edge on the high level graph where each vertex
 * is an entire region. It includes a directed low level edge of the actual
 * map. */
public class RegionEdge {
	
	public final Region sourceRegion;
	
	public final Region targetRegion;
	
	public final Edge edge;
	
	public RegionEdge(Region source, Region target, Edge edge) {
		
		this.sourceRegion = source;
		
		this.targetRegion = target; 
		
		this.edge = edge;
	}
	
	
	@Override
	public boolean equals(final Object object) {
		
		if (!(object instanceof RegionEdge)) return false;
		
		if (!((RegionEdge) object).sourceRegion.equals(this.sourceRegion)) {
			
			return false;
		}
		
		if (!((RegionEdge) object).targetRegion.equals(this.targetRegion)) {
			
			return false;
		}
		
		return ((RegionEdge) object).edge.equals(this.edge);		
	}
	
	
	@Override
	public int hashCode() {
		
		return (sourceRegion.hashCode() << 20)
			 + (targetRegion.hashCode() << 10) + edge.hashCode();
	}
}

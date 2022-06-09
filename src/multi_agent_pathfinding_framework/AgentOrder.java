/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package multi_agent_pathfinding_framework;

import java.util.ArrayList;
import java.util.List;

/* For some algorithms the order in which plans for the agents are computed
 * matters. Instances of this class represent an agent order stored as an
 * array of their ids. */
public class AgentOrder {

	public final int agentOrder[];

	
	public AgentOrder(List<Agent> order) {

	    agentOrder = new int[order.size()];

	    for (int index = 0; index < order.size(); index++) {

	        agentOrder[index] = order.get(index).id();
	    }
	}
	
		
	public AgentOrder(Agent[] agents) {
		
		agentOrder = new int[agents.length];
		
		for (int index = 0; index < agents.length; index++) {
			
			agentOrder[index] = agents[index].id();
		}		
	}
	
	
	public AgentOrder(ArrayList<Integer> order) {
				
		agentOrder = order.stream().mapToInt(id -> id).toArray();					
	}
	
	
	public ArrayList<Integer> getCopyAsList() {
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		for (int id : agentOrder) { ids.add(id); }		
		
		return ids;
	}
	
	
	/* Check whether both objects are AgentOrder objects with the same number
	 * of agents and the same agent id at each index. */
	public boolean equals(final Object object) {
		
	    if (!(object instanceof AgentOrder)) return false;
	    
	    if (!(((AgentOrder) object
	    		).agentOrder.length == this.agentOrder.length)) return false;
	    
	    for (int index = 0; index < agentOrder.length; index++) {
	    	
	    	if (agentOrder[index] != ((AgentOrder)object
	    			).agentOrder[index]) return false;
	    	
	    }	    
	    return true;
	}

	
	/* Create a hashCode from the agentIds such that their positions in
	 * the list matter. */
	public int hashCode() {
		
		int hashCode = 0;
		
		for (int id : agentOrder) {
			
			hashCode = (17 * hashCode) + id;
		}
		return hashCode;
	}
	
	
	public int getAgentIdByOrderIndex(int index) { return agentOrder[index]; }
}

/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

import java.util.HashMap;
import java.util.HashSet;

import multi_agent_pathfinding_framework.Agent;

/* Container for claims on positions and edges for the movements in the next
 * time step. Allows to look up the existence of a claim on a certain position
 * or edge in constant time. Moreover, offers to look up or remove the claims
 * of a given agent.
 * Agents use those claims to avoid collisions. */
public class ClaimContainer {
	
	/* Claims on positions or edges. */
	public final HashSet<Claim> claims
	    = new HashSet<Claim>();
	
	/* Agent ID to the agent's claim. */
	public final HashMap<Integer, ClaimPair> claimsByAgent
	    = new HashMap<Integer, ClaimPair>();  
	
	
	public ClaimContainer(Agent agents[]) {
		
		for (Agent agent : agents) {
			
			claimsByAgent.put(agent.id(), null);			
		}
	}
	
	
	/* Check for the existence of a given claim. */
	public boolean contains(Claim claim) {
		
		if (claim == null) return false;
		
		return claims.contains(claim);
	}
	
	
	/* Checks whether no claims on the given position and edge already exist.
	 */
	public boolean noClaimsOn(ClaimPosition claimPos, ClaimEdge claimEdge) {
				
		return (!claims.contains(claimPos) || claimPos == null) &&
			   (!claims.contains(claimEdge) || claimEdge == null);
	}
	
	
	/* Add a given pair of claims for an agent. */
	public void addClaims(Agent agent,
			              ClaimPosition claimPos,
			              ClaimEdge claimEdge) {
		
		/* If the agent already submitted claims, remove those first. */
		removeClaims(agent);
		
		claimsByAgent.put(agent.id(), new ClaimPair(claimPos, claimEdge));
		
		if (claimPos != null) { claims.add(claimPos); }
		
		if (claimEdge != null) { claims.add(claimEdge); }
	}
	
	
	/* Remove the claims made by the given agent. */
	public void removeClaims(Agent agent) {
		
		ClaimPair claimPair = claimsByAgent.get(agent.id());
		
		claimsByAgent.put(agent.id(), null);
		
		if (claimPair != null) {
		
			if (claimPair.claimPosition != null) {
				
				claims.remove(claimPair.claimPosition);
			}
		
		    if (claimPair.claimEdge != null) {
			
			    claims.remove(claimPair.claimEdge);
		    }
		}
	}
}

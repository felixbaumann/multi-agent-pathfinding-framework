/* Copyright 2021 Felix Baumann (felix.baumann.freiburg@gmail.com)
 * University of Freiburg. All rights reserved. */

package runtime_replanner;

/* Wraps a claim by an agent on a position and possibly on an edge. */
public class ClaimPair {

	public ClaimPosition claimPosition;
	
	public ClaimEdge claimEdge;
	
	public ClaimPair(ClaimPosition claimPosition, ClaimEdge claimEdge) {
		
		this.claimPosition = claimPosition;
		this.claimEdge = claimEdge;		
	}
	
	
	@Override
	public boolean equals(Object other) {
		
		if (!(other instanceof ClaimPair)) return false;
		
		if (!((ClaimPair) other).claimPosition.equals(this.claimPosition)) {
			
			return false;
		}
		
		/* If exactly one claimEdge is null, the pairs are not equal. */
		if (((ClaimPair) other).claimEdge == null ^ this.claimEdge == null) {
			
			return false;
		}
		
		/* Both edges null is okay. */
		if (((ClaimPair) other).claimEdge == null && this.claimEdge == null) {
			
			return true;
		}
		
		/* Unequal edges. */
		if (!((ClaimPair) other).claimEdge.equals(this.claimEdge)) { 
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		return 31 * claimPosition.hashCode() + 17 * claimEdge.hashCode();
	}
}
